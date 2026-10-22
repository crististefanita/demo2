package atf.ui.tests.practiceshop.support;

import atf.api.reporting.contract.AtfAssertion;
import atf.ui.practiceshop.config.PracticeShopApiCheckpoint;
import atf.ui.practiceshop.config.PracticeShopCredentials;
import atf.ui.practiceshop.config.PracticeShopRegistrationData;
import atf.ui.practiceshop.config.PracticeShopRegistrationProfile;
import atf.ui.practiceshop.flow.PracticeShopAccountFlow;
import atf.ui.practiceshop.pages.PracticeShopAccountPage;
import atf.ui.practiceshop.pages.PracticeShopRegistrationPage;
import atf.ui.practiceshop.support.PracticeShopPageValidator;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/** Keeps account registration/reuse and login inside the report's explicit SETUP phase. */
public final class PracticeShopAuthenticatedAccountTestSupport {
    private static final ClassRunAccountCache CLASS_RUN_ACCOUNTS = new ClassRunAccountCache();

    private PracticeShopAuthenticatedAccountTestSupport() { }

    public static PracticeShopCredentials prepare(PracticeShopE2eTestSupport.E2eSetup setup) {
        PracticeShopCredentials credentials;
        List<AtfAssertion> assertions = new ArrayList<>();
        List<atf.api.reporting.model.ReportExecutionEvidence> apiEvidence = new ArrayList<>();
        String title;

        if (setup.accountSetupMode() == PracticeShopTestSupport.AccountSetupMode.REGISTER_PER_RUN) {
            PracticeShopRegistrationProfile profile = PracticeShopRegistrationProfile.load(
                    setup.registrationProfileFile());
            credentials = CLASS_RUN_ACCOUNTS.get(setup.accountRunScope());
            if (credentials == null) {
                PracticeShopRegistrationData registration = profile.newRegistrationData();
                var registrationPage = new PracticeShopRegistrationPage(
                        setup.page(), setup.locators(), setup.actions());
                registrationPage.open(setup.configuration());
                registrationPage.fill(registration);
                long sequenceFence = setup.apiNetwork().sequenceMarker();
                try {
                    registrationPage.submitAndAwaitApiResponse(setup.configuration().api().endpoint());
                } catch (RuntimeException | AssertionError uncertainSubmission) {
                    List<AtfAssertion> failedSetup = new ArrayList<>(
                            setup.apiNetwork().registrationConfirmed(sequenceFence,
                                    setup.configuration().api().endpoint()));
                    failedSetup.add(new AtfAssertion("Registration submission outcome",
                            "one successful browser submission response is observed",
                            "registration outcome is unsuccessful or uncertain; the form will not be replayed", false));
                    setup.fail("new account registration could not be verified",
                            failedSetup,
                            setup.apiNetwork().evidence(PracticeShopApiCheckpoint.REGISTRATION));
                    throw uncertainSubmission;
                }
                assertions.addAll(setup.apiNetwork().registrationConfirmed(sequenceFence,
                        setup.configuration().api().endpoint()));
                apiEvidence.addAll(setup.apiNetwork().evidence(PracticeShopApiCheckpoint.REGISTRATION));
                credentials = profile.credentials(registration);
                CLASS_RUN_ACCOUNTS.putIfAbsent(setup.accountRunScope(), credentials);
                title = "a new account is registered and signed in";
            } else {
                title = "the class-run account is reused and signed in";
            }
        } else {
            credentials = PracticeShopCredentials.load(setup.configuration().accountFile());
            title = "the configured account is signed in";
        }

        new PracticeShopAccountFlow(setup.locators()).loginAndAssertAccount(
                setup.page(), setup.actions(), setup.configuration(), credentials);
        assertions.addAll(new PracticeShopPageValidator(setup.locators())
                .validate(setup.page(), PracticeShopAccountPage.class, "My account")
                .assertions());
        assertions.addAll(setup.apiNetwork().loginConfirmed(setup.configuration().api(), credentials));
        apiEvidence.addAll(setup.apiNetwork().evidence(PracticeShopApiCheckpoint.LOGIN));
        setup.authenticatedSessionEstablished();
        setup.complete(title, assertions, apiEvidence);
        return credentials;
    }

    /**
     * Run-scoped cache: registration is performed once, while each method
     * still opens its own browser and proves login independently.
     */
    static final class ClassRunAccountCache {
        private final ConcurrentMap<String, PracticeShopCredentials> accounts = new ConcurrentHashMap<>();

        PracticeShopCredentials get(String scope) {
            return accounts.get(scope);
        }

        PracticeShopCredentials putIfAbsent(String scope, PracticeShopCredentials credentials) {
            PracticeShopCredentials existing = accounts.putIfAbsent(scope, credentials);
            return existing == null ? credentials : existing;
        }
    }
}
