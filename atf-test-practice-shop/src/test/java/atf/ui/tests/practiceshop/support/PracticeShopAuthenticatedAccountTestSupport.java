package atf.ui.tests.practiceshop.support;

import atf.api.reporting.contract.AtfAssertion;
import atf.api.reporting.contract.AtfGherkinKeyword;
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

        if (setup.accountSetupMode() == PracticeShopTestSupport.AccountSetupMode.REGISTER_PER_RUN) {
            PracticeShopRegistrationProfile profile = setup.registrationProfileFile() == null
                    ? PracticeShopRegistrationProfile.synthetic()
                    : PracticeShopRegistrationProfile.load(setup.registrationProfileFile());
            credentials = CLASS_RUN_ACCOUNTS.get(setup.accountRunScope());
            if (credentials == null) {
                PracticeShopRegistrationData registration = profile.newRegistrationData();
                var registrationPage = new PracticeShopRegistrationPage(
                        setup.page(), setup.locators(), setup.actions());
                registrationPage.open(setup.configuration());
                registrationPage.fill(registration);
                long sequenceFence = setup.apiNetwork().sequenceMarker();
                int responseStatus;
                try {
                    responseStatus = registrationPage.submitAndAwaitApiResponse(
                            setup.configuration().api().endpoint());
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
                assertions.addAll(registrationAssertions(responseStatus,
                        setup.apiNetwork().registrationConfirmed(sequenceFence,
                                setup.configuration().api().endpoint())));
                apiEvidence.addAll(setup.apiNetwork().evidence(PracticeShopApiCheckpoint.REGISTRATION));
                setup.checkpoint(AtfGherkinKeyword.GIVEN, "a new account is registered",
                        assertions, apiEvidence);
                credentials = profile.credentials(registration);
                CLASS_RUN_ACCOUNTS.putIfAbsent(setup.accountRunScope(), credentials);
            } else {
                setup.checkpoint(AtfGherkinKeyword.GIVEN,
                        "the account registered earlier in this class-run is reused",
                        List.of(new AtfAssertion("Class-run account", "a registered account is available",
                                "the run-scoped account is reused; credentials are redacted", true)), List.of());
            }
        } else {
            credentials = PracticeShopCredentials.load(setup.configuration().accountFile());
            boolean credentialsAvailable = credentials.hasLiveAccountCandidate();
            setup.checkpoint(AtfGherkinKeyword.GIVEN, "the configured local account fixture is loaded",
                    List.of(new AtfAssertion("Local account fixture", "valid credentials are available",
                            credentialsAvailable ? "local credentials are available; values are redacted"
                                    : "local credentials are incomplete", credentialsAvailable)), List.of());
        }

        new PracticeShopAccountFlow(setup.locators()).loginAndAssertAccount(
                setup.page(), setup.actions(), setup.configuration(), credentials);
        assertions.addAll(new PracticeShopPageValidator(setup.locators())
                .validate(setup.page(), PracticeShopAccountPage.class, "My account")
                .assertions());
        assertions.addAll(setup.apiNetwork().loginConfirmed(setup.configuration().api(), credentials));
        apiEvidence.addAll(setup.apiNetwork().evidence(PracticeShopApiCheckpoint.LOGIN));
        setup.checkpoint(AtfGherkinKeyword.AND, "the account is signed in and its session is confirmed",
                assertions, apiEvidence);
        setup.complete();
        return credentials;
    }

    static List<AtfAssertion> registrationAssertions(int responseStatus,
                                                       List<AtfAssertion> journalAssertions) {
        boolean responseAccepted = responseStatus >= 200 && responseStatus < 300;
        boolean journalConfirmed = journalAssertions == null
                || journalAssertions.stream().allMatch(AtfAssertion::passed);
        boolean successful = responseAccepted && journalConfirmed;
        List<AtfAssertion> assertions = new ArrayList<>(List.of(
                new AtfAssertion("Account setup mode", "REGISTER_PER_RUN", "REGISTER_PER_RUN", true),
                new AtfAssertion("Registration browser response", "HTTP 2xx",
                        "HTTP " + responseStatus, responseAccepted),
                new AtfAssertion("Login readiness", "registered account details are ready for sign-in",
                        successful ? "credentials retained for the following login; values redacted"
                                : "registration was not accepted; sign-in must not start", successful)));
        if (journalAssertions != null) assertions.addAll(journalAssertions);
        return List.copyOf(assertions);
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
