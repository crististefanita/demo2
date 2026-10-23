package atf.ui.autoex.page;

import atf.api.reporting.contract.AtfAssertion;
import atf.ui.autoex.config.AutoExConfiguration;
import atf.ui.autoex.locator.AutoExSelectors;
import atf.ui.autoex.support.AutoExPurchaseSession;
import atf.ui.autoex.support.AutoExAccountCleanupAction;
import atf.ui.autoex.support.AutoExAccountCleanupDecision;
import atf.ui.autoex.support.AutoExTestAccount;
import atf.ui.autoex.support.AutoExPurchaseTraceStep;

import java.util.List;

/** Registration during checkout and deterministic disposal of the synthetic account. */
public final class AccountPage extends AutoExPage {
    public AccountPage(AutoExPurchaseSession run) { super(run); }

    public void registerFromCheckout(AutoExTestAccount account) {
        clickLast(AutoExSelectors.REGISTER_LOGIN, "registration form opens from checkout");
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
        click(AutoExSelectors.CREATE_ACCOUNT, "disposable account is created on the practice site");

        String result = text(AutoExSelectors.ACCOUNT_RESULT_HEADING, "account-creation result is read");
        run.checkpoint(AutoExPurchaseTraceStep.ACCOUNT_CREATED,
                List.of(new AtfAssertion("Account creation", "ACCOUNT CREATED!", result,
                        result.contains("ACCOUNT CREATED!"))));
        click(AutoExSelectors.CONTINUE, "created account session continues to the store");
    }

    public String deliveryAddress() {
        return text(AutoExSelectors.DELIVERY_ADDRESS, "delivery address is read");
    }

    public String billingAddress() {
        return text(AutoExSelectors.BILLING_ADDRESS, "billing address is read");
    }

    public String accountDeletionResult() {
        run.dismissGoogleVignetteIfPresent();
        click(AutoExSelectors.DELETE_ACCOUNT, "disposable account is deleted after the demo order");
        String result = text(AutoExSelectors.ACCOUNT_RESULT_HEADING, "account-deletion result is read");
        run.checkpoint(AutoExPurchaseTraceStep.ACCOUNT_DELETED,
                List.of(new AtfAssertion("Account cleanup", "ACCOUNT DELETED!", result,
                        result.contains("ACCOUNT DELETED!"))));
        click(AutoExSelectors.CONTINUE, "deleted account flow returns to the store");
        run.clearFailureCleanup();
        return result;
    }

    private void deleteIfCreatedAndLoggedIn(AutoExTestAccount account) {
        boolean creationConfirmed = accountCreationWasConfirmed();
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
        accountDeletionResult();
    }

    private boolean accountCreationWasConfirmed() {
        return visible(AutoExSelectors.ACCOUNT_RESULT_HEADING)
                && run.page().locator(AutoExSelectors.ACCOUNT_RESULT_HEADING)
                .innerText().contains("ACCOUNT CREATED!");
    }
}
