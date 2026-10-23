package atf.ui.autoex.page;

import atf.api.reporting.contract.AtfAssertion;
import atf.ui.autoex.config.AutoExConfiguration;
import atf.ui.autoex.locator.AutoExRoutes;
import atf.ui.autoex.locator.AutoExSelectors;
import atf.ui.autoex.support.account.AutoExAccountCleanupAction;
import atf.ui.autoex.support.account.AutoExAccountCleanupDecision;
import atf.ui.autoex.support.account.AutoExTestAccount;
import atf.ui.autoex.support.trace.AutoExCustomerTraceStep;
import atf.ui.autoex.support.journey.AutoExPurchaseSession;
import atf.ui.autoex.support.journey.AutoExPurchaseTraceStep;

import java.net.URI;
import java.util.List;

/** Registration during checkout and deterministic disposal of the synthetic account. */
public final class AccountPage extends AutoExPage {
    private static final String CREATED_MESSAGE = "ACCOUNT CREATED!";
    private static final String DELETED_MESSAGE = "ACCOUNT DELETED!";
    private boolean creationConfirmed;
    private boolean creationAttempted;

    public AccountPage(AutoExPurchaseSession run) { super(run); }

    public void registerFromCheckout(AutoExTestAccount account) {
        clickLast(AutoExSelectors.REGISTER_LOGIN, "registration form opens from checkout");
        register(account);
    }

    public void registerFromStore(AutoExTestAccount account) {
        click(AutoExSelectors.HEADER_LOGIN, "registration form opens from the store header");
        register(account);
    }

    private void register(AutoExTestAccount account) {
        fill(AutoExSelectors.SIGNUP_NAME, account.signupName(), "synthetic account name is entered");
        fill(AutoExSelectors.SIGNUP_EMAIL, account.email(), "unique test email is entered");
        click(AutoExSelectors.SIGNUP_BUTTON, "signup details are submitted");
        AutoExConfiguration.Registration profile = account.profile();
        click(AutoExSelectors.ACCOUNT_GENDER_MR, "demo customer title is selected");
        fill(AutoExSelectors.ACCOUNT_PASSWORD, account.password(), "generated test password is entered");
        select(AutoExSelectors.ACCOUNT_DAYS, profile.birthDay(), "birth day is selected");
        select(AutoExSelectors.ACCOUNT_MONTHS, profile.birthMonth(), "birth month is selected");
        select(AutoExSelectors.ACCOUNT_YEARS, profile.birthYear(), "birth year is selected");
        fill(AutoExSelectors.ACCOUNT_FIRST_NAME, profile.firstName(), "synthetic first name is entered");
        fill(AutoExSelectors.ACCOUNT_LAST_NAME, profile.lastName(), "synthetic last name is entered");
        fill(AutoExSelectors.ACCOUNT_COMPANY, profile.company(), "test company is entered");
        fill(AutoExSelectors.ACCOUNT_ADDRESS_1, profile.address1(), "test address is entered");
        fill(AutoExSelectors.ACCOUNT_ADDRESS_2, profile.address2(), "secondary test address is entered");
        select(AutoExSelectors.ACCOUNT_COUNTRY, profile.country(), "country is selected");
        fill(AutoExSelectors.ACCOUNT_STATE, profile.state(), "test state is entered");
        fill(AutoExSelectors.ACCOUNT_CITY, profile.city(), "test city is entered");
        fill(AutoExSelectors.ACCOUNT_POSTAL_CODE, profile.postalCode(), "test postal code is entered");
        fill(AutoExSelectors.ACCOUNT_MOBILE, profile.mobileNumber(), "test phone number is entered");
        run.onFailureCleanup(() -> deleteIfCreatedAndLoggedIn(account));
        creationAttempted = true;
        click(AutoExSelectors.CREATE_ACCOUNT, "disposable account is created on the practice site");

        String result = text(AutoExSelectors.ACCOUNT_RESULT_HEADING, "account-creation result is read");
        creationConfirmed = result.contains(CREATED_MESSAGE);
        run.checkpoint(AutoExPurchaseTraceStep.ACCOUNT_CREATED,
                List.of(new AtfAssertion("Account creation", CREATED_MESSAGE, result, creationConfirmed)));
        click(AutoExSelectors.CONTINUE, "created account session continues to the store");
    }

    public String loggedInName() {
        return text(AutoExSelectors.LOGGED_IN_NAME, "signed-in customer identity is read");
    }

    public void login(AutoExTestAccount account) {
        if (!visible(AutoExSelectors.LOGIN_EMAIL)) {
            click(AutoExSelectors.HEADER_LOGIN, "existing customer login form opens");
        }
        fill(AutoExSelectors.LOGIN_EMAIL, account.email(), "disposable customer email is entered");
        fill(AutoExSelectors.LOGIN_PASSWORD, account.password(), "disposable customer password is entered");
        click(AutoExSelectors.LOGIN_BUTTON, "existing customer signs in");
        run.waitForText(AutoExSelectors.LOGGED_IN_NAME, account.signupName(),
                "the signed-in session belongs to this test's disposable customer");
    }

    public boolean logout() {
        click(AutoExSelectors.LOGOUT, "the disposable customer signs out");
        run.waitForText(AutoExSelectors.LOGIN_BUTTON, "Login", "the customer login form is ready");
        return visible(AutoExSelectors.LOGIN_EMAIL) && !visible(AutoExSelectors.DELETE_ACCOUNT);
    }

    public String deliveryAddress(String expectedDisplayName) {
        run.waitForText(AutoExSelectors.DELIVERY_ADDRESS, expectedDisplayName,
                "delivery address contains the configured customer name");
        return text(AutoExSelectors.DELIVERY_ADDRESS, "delivery address is read");
    }

    public String billingAddress(String expectedDisplayName) {
        run.waitForText(AutoExSelectors.BILLING_ADDRESS, expectedDisplayName,
                "billing address contains the configured customer name");
        return text(AutoExSelectors.BILLING_ADDRESS, "billing address is read");
    }

    public String accountDeletionResult(AutoExTestAccount account) {
        run.dismissGoogleVignetteIfPresent();
        String identity = loggedInName();
        if (!account.signupName().equals(identity)) {
            throw new IllegalStateException("Refusing account deletion: signed-in customer is not this run's identity");
        }
        click(AutoExSelectors.DELETE_ACCOUNT, "this run's disposable customer account is deleted");
        String result = text(AutoExSelectors.ACCOUNT_RESULT_HEADING, "account-deletion result is read");
        boolean deleted = result.contains(DELETED_MESSAGE);
        if (deleted) run.clearFailureCleanup();
        run.checkpoint(AutoExPurchaseTraceStep.ACCOUNT_DELETED,
                List.of(new AtfAssertion("Disposable customer identity", account.signupName(), identity,
                                account.signupName().equals(identity)),
                        new AtfAssertion("Account cleanup", DELETED_MESSAGE, result, deleted)));
        click(AutoExSelectors.CONTINUE, "deleted account flow returns to the store");
        return result;
    }

    private void deleteIfCreatedAndLoggedIn(AutoExTestAccount account) {
        if (accountResultContains(DELETED_MESSAGE)) {
            run.clearFailureCleanup();
            return;
        }
        creationConfirmed |= accountCreationWasConfirmed();
        recoverObservedVignette();
        recoverUnavailableStoreDocument();
        try {
            cleanupFromCurrentPage(account);
        } catch (RuntimeException | AssertionError firstFailure) {
            if (!onVignette()) throw firstFailure;
            try {
                recoverObservedVignette();
                cleanupFromCurrentPage(account);
            } catch (RuntimeException | AssertionError recoveryFailure) {
                firstFailure.addSuppressed(recoveryFailure);
                throw firstFailure;
            }
        }
    }

    private void cleanupFromCurrentPage(AutoExTestAccount account) {
        if (accountResultContains(DELETED_MESSAGE)) {
            run.clearFailureCleanup();
            return;
        }
        if ((creationConfirmed || creationAttempted) && !visible(AutoExSelectors.DELETE_ACCOUNT)
                && !accountCreationWasConfirmed()) {
            login(account);
        }
        AutoExAccountCleanupAction action = AutoExAccountCleanupDecision.resolve(
                creationConfirmed, visible(AutoExSelectors.CONTINUE),
                visible(AutoExSelectors.DELETE_ACCOUNT));
        if (action == AutoExAccountCleanupAction.CONTINUE_THEN_DELETE) {
            click(AutoExSelectors.CONTINUE, "finish account setup before failure cleanup");
        }
        run.dismissGoogleVignetteIfPresent();
        if (action == AutoExAccountCleanupAction.NO_OP) return;
        if (action == AutoExAccountCleanupAction.CONFIRMED_BUT_UNAVAILABLE
                || !visible(AutoExSelectors.DELETE_ACCOUNT)) {
            throw new IllegalStateException(
                    "Disposable account creation was confirmed, but its cleanup action is unavailable: "
                            + account.email());
        }
        accountDeletionResult(account);
    }

    private boolean accountCreationWasConfirmed() {
        return accountResultContains(CREATED_MESSAGE);
    }

    private boolean accountResultContains(String expected) {
        var heading = run.page().locator(AutoExSelectors.ACCOUNT_RESULT_HEADING);
        return heading.count() == 1 && heading.isVisible() && heading.innerText().contains(expected);
    }

    private boolean onVignette() {
        return run.currentUrl().contains(AutoExRoutes.GOOGLE_VIGNETTE_FRAGMENT);
    }

    private void recoverObservedVignette() {
        String interruptedUrl = run.currentUrl();
        if (!interruptedUrl.contains(AutoExRoutes.GOOGLE_VIGNETTE_FRAGMENT)) return;
        var home = AutoExRoutes.HOME.resolve(run.configuration().baseUrl());
        run.navigate(home);
        String recoveredUrl = run.currentUrl();
        run.checkpoint(AutoExCustomerTraceStep.CLEANUP_RECOVERED,
                List.of(new AtfAssertion("Observed cleanup interruption",
                                AutoExRoutes.GOOGLE_VIGNETTE_FRAGMENT, interruptedUrl,
                                interruptedUrl.contains(AutoExRoutes.GOOGLE_VIGNETTE_FRAGMENT)),
                        new AtfAssertion("Cleanup recovery destination", home.toString(), recoveredUrl,
                                home.toString().equals(recoveredUrl))));
    }

    private void recoverUnavailableStoreDocument() {
        String interruptedUrl = run.currentUrl();
        URI base = run.configuration().baseUrl();
        if (base.getHost().equalsIgnoreCase(URI.create(interruptedUrl).getHost())) return;
        URI home = AutoExRoutes.HOME.resolve(base);
        run.navigate(home);
        String recoveredUrl = run.currentUrl();
        run.checkpoint(AutoExCustomerTraceStep.CLEANUP_DOCUMENT_RECOVERED,
                List.of(new AtfAssertion("Interrupted store document", "outside the configured store origin",
                                interruptedUrl, !base.getHost().equalsIgnoreCase(URI.create(interruptedUrl).getHost())),
                        new AtfAssertion("Cleanup recovery destination", home.toString(), recoveredUrl,
                                home.toString().equals(recoveredUrl))));
    }
}
