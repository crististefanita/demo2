package atf.ui.tests.practiceshop.support;

import atf.api.testkit.AtfUnitTest;
import atf.api.reporting.evidence.ReportScreenshotPolicy;
import atf.api.reporting.evidence.profile.ReportEvidenceCaptureSource;
import atf.api.reporting.evidence.profile.ReportEvidenceFlowMode;
import atf.api.reporting.evidence.profile.ReportUiActionDetailsMode;
import atf.ui.practiceshop.config.PracticeShopApiCheckpoint;
import atf.ui.practiceshop.config.PracticeShopConfigurationLoader;
import atf.ui.practiceshop.config.PracticeShopCredentials;
import atf.ui.practiceshop.config.PracticeShopReportConfiguration;
import atf.ui.practiceshop.config.PracticeShopValidationProfile;
import atf.ui.practiceshop.flow.PracticeShopPurchaseFlow;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@AtfUnitTest
class PracticeShopE2eTestSupportTest {
    @Test
    void purchaseJourneysDeclareDistinctEvidenceProfiles() throws ClassNotFoundException {
        assertEquals(PracticeShopE2eTestSupport.HarnessProfile.UI_ONLY,
                Class.forName("atf.ui.tests.practiceshop.e2e.UiOnlyPurchaseE2eTest")
                        .getAnnotation(PracticeShopE2eTestSupport.WithE2eHarness.class).profile());
        assertEquals(PracticeShopE2eTestSupport.HarnessProfile.UI_API,
                Class.forName("atf.ui.tests.practiceshop.e2e.PurchaseE2eTest")
                        .getAnnotation(PracticeShopE2eTestSupport.WithE2eHarness.class).profile());

        var singleFlow = Class.forName(
                        "atf.ui.tests.practiceshop.e2e.UiOnlySingleFlowPurchaseE2eTest")
                .getAnnotation(PracticeShopE2eTestSupport.WithE2eHarness.class);
        assertEquals(PracticeShopE2eTestSupport.HarnessProfile.UI_ONLY, singleFlow.profile());
        assertEquals(PracticeShopE2eTestSupport.ReportFlow.UNIFIED_FLOW,
                singleFlow.reportFlow());
        var reportProfile = PracticeShopE2eTestSupport.reportProfile(
                PracticeShopConfigurationLoader.load(), singleFlow);
        assertEquals(ReportEvidenceFlowMode.UNIFIED_FLOW, reportProfile.flowMode());
        assertEquals(ReportScreenshotPolicy.EACH_CHECKPOINT, reportProfile.screenshotPolicy());
        assertEquals(ReportEvidenceCaptureSource.BROWSER, reportProfile.captureSource());
        assertEquals(ReportUiActionDetailsMode.INCLUDED_COLLAPSED, reportProfile.actionDetailsMode());

        var yamlConfiguration = PracticeShopConfigurationLoader.load();
        var yamlDisabled = new atf.ui.practiceshop.config.PracticeShopConfiguration(
                yamlConfiguration.baseUrl(), yamlConfiguration.accountFile(), yamlConfiguration.productName(),
                yamlConfiguration.productPath(), yamlConfiguration.checkout(), yamlConfiguration.browser(),
                yamlConfiguration.uiRetry(), yamlConfiguration.validationProfile(), yamlConfiguration.api(),
                yamlConfiguration.uiReportDirectory(), yamlConfiguration.apiReportDirectory(),
                yamlConfiguration.authenticatedE2eEnabled(), new PracticeShopReportConfiguration(
                yamlConfiguration.reporting().systemBodyPreviewCharacters(), ReportScreenshotPolicy.NONE));
        assertEquals(ReportScreenshotPolicy.NONE,
                PracticeShopE2eTestSupport.reportProfile(yamlDisabled, singleFlow).screenshotPolicy(),
                "class annotations may choose report flow but cannot override the YAML screenshot policy");

        var newAccountPurchaseClass = Class.forName(
                "atf.ui.tests.practiceshop.e2e.NewAccountPurchaseE2eTest");
        var newAccountPurchaseHarness = newAccountPurchaseClass
                .getAnnotation(PracticeShopE2eTestSupport.WithE2eHarness.class);
        assertEquals(ReportScreenshotPolicy.EACH_CHECKPOINT,
                PracticeShopE2eTestSupport.reportProfile(PracticeShopConfigurationLoader.load(),
                        newAccountPurchaseHarness).screenshotPolicy());

        var summaryOnly = PracticeShopE2eTestSupport.reportProfile(
                PracticeShopConfigurationLoader.load(), SummaryOnlyActions.class
                        .getAnnotation(PracticeShopE2eTestSupport.WithE2eHarness.class));
        assertEquals(ReportUiActionDetailsMode.SUMMARY_ONLY, summaryOnly.actionDetailsMode());
        var included = PracticeShopE2eTestSupport.reportProfile(
                PracticeShopConfigurationLoader.load(), IncludedActions.class
                        .getAnnotation(PracticeShopE2eTestSupport.WithE2eHarness.class));
        assertEquals(ReportUiActionDetailsMode.INCLUDED_COLLAPSED, included.actionDetailsMode());

        var authenticated = Class.forName(
                        "atf.ui.tests.practiceshop.e2e.AuthenticatedPurchaseE2eTest")
                .getAnnotation(PracticeShopTestSupport.AuthenticatedE2e.class);
        var authenticatedHarness = Class.forName(
                        "atf.ui.tests.practiceshop.e2e.AuthenticatedPurchaseE2eTest")
                .getAnnotation(PracticeShopE2eTestSupport.WithE2eHarness.class);
        assertEquals(ReportScreenshotPolicy.EACH_CHECKPOINT,
                PracticeShopE2eTestSupport.reportProfile(PracticeShopConfigurationLoader.load(),
                        authenticatedHarness).screenshotPolicy());
        assertEquals(PracticeShopTestSupport.AccountSetupMode.REGISTER_PER_RUN, authenticated.accountSetup());
        assertEquals(PracticeShopTestSupport.RegistrationProfileMode.SYNTHETIC,
                authenticated.registrationProfile());

        var configured = PracticeShopConfigurationLoader.load();
        var uiOnly = PracticeShopE2eTestSupport.configurationForHarnessProfile(
                configured, PracticeShopE2eTestSupport.HarnessProfile.UI_ONLY);
        var uiApi = PracticeShopE2eTestSupport.configurationForHarnessProfile(
                configured.withValidationProfile(PracticeShopValidationProfile.UI_ONLY),
                PracticeShopE2eTestSupport.HarnessProfile.UI_API);

        assertEquals(PracticeShopValidationProfile.UI_ONLY, uiOnly.validationProfile());
        assertTrue(uiOnly.api().evidenceBinding().checkpoints().isEmpty());
        assertEquals(PracticeShopValidationProfile.UI_API, uiApi.validationProfile());
        assertTrue(uiApi.api().evidenceBinding().includes(PracticeShopApiCheckpoint.PRODUCT));
        assertTrue(uiApi.api().evidenceBinding().includes(PracticeShopApiCheckpoint.REGISTRATION));
        assertTrue(uiApi.api().evidenceBinding().includes(PracticeShopApiCheckpoint.ORDER));
    }

    @Test
    void terminalPurchaseScreenRequiresTheObservedOrderAndInvoiceConfirmation() {
        var confirmed = PracticeShopE2eTestSupport.E2eJourney.purchaseConfirmationAssertions(
                new PracticeShopPurchaseFlow.PurchaseResult(
                        "Pliers", "2", "Cash on Delivery", "Thanks for your order! INV-123"));
        var missingInvoice = PracticeShopE2eTestSupport.E2eJourney.purchaseConfirmationAssertions(
                new PracticeShopPurchaseFlow.PurchaseResult(
                        "Pliers", "2", "Cash on Delivery", "Thanks for your order!"));
        var absentConfirmation = PracticeShopE2eTestSupport.E2eJourney.purchaseConfirmationAssertions(
                new PracticeShopPurchaseFlow.PurchaseResult(
                        "Pliers", "2", "Cash on Delivery", null));

        assertEquals(1, confirmed.size());
        assertTrue(confirmed.getFirst().passed());
        assertFalse(missingInvoice.getFirst().passed());
        assertFalse(absentConfirmation.getFirst().passed());
        assertEquals("no confirmation was observed", absentConfirmation.getFirst().actual());
    }

    @PracticeShopE2eTestSupport.WithE2eHarness(
            actionDetails = PracticeShopE2eTestSupport.ActionDetailsMode.SUMMARY_ONLY)
    private static class SummaryOnlyActions { }

    @PracticeShopE2eTestSupport.WithE2eHarness(
            actionDetails = PracticeShopE2eTestSupport.ActionDetailsMode.INCLUDED_COLLAPSED)
    private static class IncludedActions { }

    @Test
    void closesThePerMethodResourceOnlyOnce() throws Exception {
        AtomicInteger closes = new AtomicInteger();
        var resource = new PracticeShopE2eTestSupport.E2eResource(closes::incrementAndGet);

        resource.close();
        resource.close();

        assertEquals(1, closes.get());
    }

    @Test
    void keepsTheBusinessFailurePrimaryWhenTeardownAlsoFails() {
        IOException teardown = new IOException("teardown failed");
        var resource = new PracticeShopE2eTestSupport.E2eResource(() -> { throw teardown; });
        AssertionError business = new AssertionError("observed business failure");

        assertDoesNotThrow(() -> PracticeShopE2eTestSupport.closeAfterTest(resource, business));

        assertEquals(1, business.getSuppressed().length);
        assertSame(teardown, business.getSuppressed()[0]);
    }

    @Test
    void reportsTeardownFailureWhenTheTestItselfPassed() {
        IOException teardown = new IOException("teardown failed");
        var resource = new PracticeShopE2eTestSupport.E2eResource(() -> { throw teardown; });

        assertSame(teardown, assertThrows(IOException.class,
                () -> PracticeShopE2eTestSupport.closeAfterTest(resource, null)));
    }

    @Test
    void classRunRegistrationCacheCreatesOnceAndReturnsTheSameCredentials() {
        var cache = new PracticeShopAuthenticatedAccountTestSupport.ClassRunAccountCache();
        var first = new PracticeShopCredentials("live@practice-shop.invalid-domain", "secret", "ATF", "One");
        var second = new PracticeShopCredentials("live-two@practice-shop.invalid-domain", "secret", "ATF", "Two");

        assertNull(cache.get("class-run"));
        assertSame(first, cache.putIfAbsent("class-run", first));
        assertSame(first, cache.putIfAbsent("class-run", second));
        assertSame(first, cache.get("class-run"));
    }

    @Test
    void uiOnlyRegistrationStillReportsTheObservedBrowserResponseStatus() {
        var success = PracticeShopAuthenticatedAccountTestSupport.registrationAssertions(201, java.util.List.of());
        var failure = PracticeShopAuthenticatedAccountTestSupport.registrationAssertions(409, java.util.List.of());

        assertEquals("REGISTER_PER_RUN", success.get(0).actual());
        assertEquals("HTTP 201", success.get(1).actual());
        assertTrue(success.get(1).passed());
        assertTrue(success.get(2).passed());
        assertEquals("HTTP 409", failure.get(1).actual());
        assertFalse(failure.get(1).passed());
        assertFalse(failure.get(2).passed());

        var inconsistent = PracticeShopAuthenticatedAccountTestSupport.registrationAssertions(201,
                java.util.List.of(new atf.api.reporting.contract.AtfAssertion(
                        "Registration journal", "registration confirmed", "no matching exchange", false)));
        assertTrue(inconsistent.get(1).passed());
        assertFalse(inconsistent.get(2).passed());
    }
}
