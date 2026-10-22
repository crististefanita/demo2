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

/** Keeps account registration/reuse and login inside the report's explicit SETUP phase. */
public final class PracticeShopAuthenticatedAccountSetup {
    private PracticeShopAuthenticatedAccountSetup() { }

    public static PracticeShopCredentials prepare(PracticeShopE2eTestSupport.E2eSetup setup) {
        PracticeShopCredentials credentials;
        List<AtfAssertion> assertions = new ArrayList<>();
        List<atf.api.reporting.model.ReportExecutionEvidence> apiEvidence = new ArrayList<>();
        String title;

        if (setup.accountSetupMode() == PracticeShopTestSupport.AccountSetupMode.REGISTER_PER_RUN) {
            PracticeShopRegistrationProfile profile = PracticeShopRegistrationProfile.load(
                    setup.registrationProfileFile());
            PracticeShopRegistrationData registration = profile.newRegistrationData();
            var registrationPage = new PracticeShopRegistrationPage(
                    setup.page(), setup.locators(), setup.actions());
            registrationPage.open(setup.configuration());
            registrationPage.fill(registration);
            long sequenceFence = setup.apiNetwork().sequenceMarker();
            registrationPage.submitAndAwaitApiResponse(setup.configuration().api().endpoint());
            assertions.addAll(setup.apiNetwork().registrationConfirmed(sequenceFence));
            apiEvidence.addAll(setup.apiNetwork().evidence(PracticeShopApiCheckpoint.REGISTRATION));
            credentials = profile.credentials(registration);
            title = "a new account is registered and signed in";
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
}
