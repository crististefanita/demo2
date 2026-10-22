package atf.ui.tests.practiceshop.support;

import atf.api.testkit.AtfUnitTest;
import atf.api.reporting.evidence.ReportScreenshotPolicy;
import atf.api.reporting.evidence.profile.ReportEvidenceCaptureSource;
import atf.api.reporting.evidence.profile.ReportEvidenceFlowMode;
import atf.api.reporting.evidence.profile.ReportUiActionDetailsMode;
import atf.ui.practiceshop.config.ShopApiCheckpoint;
import atf.ui.practiceshop.config.ShopConfigurationLoader;
import atf.ui.practiceshop.config.ShopCredentials;
import atf.ui.practiceshop.config.ShopReportConfiguration;
import atf.ui.practiceshop.config.ShopValidationProfile;
import atf.ui.practiceshop.flow.ShopPurchaseFlow;
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
class ShopE2eTestSupportTest {
    @Test
    void purchaseJourneysDeclareDistinctEvidenceProfiles() throws ClassNotFoundException {
        assertEquals(ShopE2eTestSupport.HarnessProfile.UI_ONLY,
                Class.forName("atf.ui.tests.practiceshop.e2e.ShopUiOnlyPurchaseE2eTest")
                        .getAnnotation(ShopE2eTestSupport.WithE2eHarness.class).profile());
        assertEquals(ShopE2eTestSupport.HarnessProfile.UI_API,
                Class.forName("atf.ui.tests.practiceshop.e2e.ShopPurchaseE2eTest")
                        .getAnnotation(ShopE2eTestSupport.WithE2eHarness.class).profile());

        var singleFlow = Class.forName(
                        "atf.ui.tests.practiceshop.e2e.ShopUiOnlySingleFlowPurchaseE2eTest")
                .getAnnotation(ShopE2eTestSupport.WithE2eHarness.class);
        assertEquals(ShopE2eTestSupport.HarnessProfile.UI_ONLY, singleFlow.profile());
        assertEquals(ShopE2eTestSupport.ReportFlow.UNIFIED_FLOW,
                singleFlow.reportFlow());
        var reportProfile = ShopE2eTestSupport.reportProfile(
                ShopConfigurationLoader.load(), singleFlow);
        assertEquals(ReportEvidenceFlowMode.UNIFIED_FLOW, reportProfile.flowMode());
        assertEquals(ReportScreenshotPolicy.EACH_CHECKPOINT, reportProfile.screenshotPolicy());
        assertEquals(ReportEvidenceCaptureSource.BROWSER, reportProfile.captureSource());
        assertEquals(ReportUiActionDetailsMode.INCLUDED_COLLAPSED, reportProfile.actionDetailsMode());
        assertFalse(atf.ui.practiceshop.support.ShopE2eContext.Scenario
                .shouldCaptureCheckpointScreenshot(reportProfile, true, true),
                "unified flow reserves its one screenshot for the final screen");
        assertTrue(atf.ui.practiceshop.support.ShopE2eContext.Scenario
                .shouldCaptureCheckpointScreenshot(reportProfile, true, false),
                "failed checkpoints still retain failure screenshots");
        assertFalse(atf.ui.practiceshop.support.ShopE2eContext.Scenario
                .shouldCaptureCheckpointScreenshot(reportProfile, false, false));

        var yamlConfiguration = ShopConfigurationLoader.load();
        var yamlDisabled = new atf.ui.practiceshop.config.ShopConfiguration(
                yamlConfiguration.baseUrl(), yamlConfiguration.accountFile(), yamlConfiguration.productName(),
                yamlConfiguration.productPath(), yamlConfiguration.checkout(), yamlConfiguration.browser(),
                yamlConfiguration.uiRetry(), yamlConfiguration.validationProfile(), yamlConfiguration.api(),
                yamlConfiguration.uiReportDirectory(), yamlConfiguration.apiReportDirectory(),
                yamlConfiguration.authenticatedE2eEnabled(), new ShopReportConfiguration(
                yamlConfiguration.reporting().systemBodyPreviewCharacters(), ReportScreenshotPolicy.NONE));
        assertEquals(ReportScreenshotPolicy.NONE,
                ShopE2eTestSupport.reportProfile(yamlDisabled, singleFlow).screenshotPolicy(),
                "class annotations may choose report flow but cannot override the YAML screenshot policy");

        var newAccountPurchaseClass = Class.forName(
                "atf.ui.tests.practiceshop.e2e.ShopNewAccountPurchaseE2eTest");
        var newAccountPurchaseHarness = newAccountPurchaseClass
                .getAnnotation(ShopE2eTestSupport.WithE2eHarness.class);
        assertEquals(ReportScreenshotPolicy.EACH_CHECKPOINT,
                ShopE2eTestSupport.reportProfile(ShopConfigurationLoader.load(),
                        newAccountPurchaseHarness).screenshotPolicy());

        var summaryOnly = ShopE2eTestSupport.reportProfile(
                ShopConfigurationLoader.load(), SummaryOnlyActions.class
                        .getAnnotation(ShopE2eTestSupport.WithE2eHarness.class));
        assertEquals(ReportUiActionDetailsMode.SUMMARY_ONLY, summaryOnly.actionDetailsMode());
        var included = ShopE2eTestSupport.reportProfile(
                ShopConfigurationLoader.load(), IncludedActions.class
                        .getAnnotation(ShopE2eTestSupport.WithE2eHarness.class));
        assertEquals(ReportUiActionDetailsMode.INCLUDED_COLLAPSED, included.actionDetailsMode());

        var authenticated = Class.forName(
                        "atf.ui.tests.practiceshop.e2e.ShopAuthenticatedPurchaseE2eTest")
                .getAnnotation(ShopTestSupport.AuthenticatedE2e.class);
        var authenticatedHarness = Class.forName(
                        "atf.ui.tests.practiceshop.e2e.ShopAuthenticatedPurchaseE2eTest")
                .getAnnotation(ShopE2eTestSupport.WithE2eHarness.class);
        assertEquals(ReportScreenshotPolicy.EACH_CHECKPOINT,
                ShopE2eTestSupport.reportProfile(ShopConfigurationLoader.load(),
                        authenticatedHarness).screenshotPolicy());
        assertEquals(ShopTestSupport.AccountSetupMode.REUSE_FIXTURE, authenticated.accountSetup());
        assertEquals("atf-test-practice-shop/src/test/resources/"
                        + "practice-software-testing-account.local.properties",
                authenticated.accountFile());
        assertEquals(ShopTestSupport.RegistrationProfileMode.LOCAL,
                authenticated.registrationProfile());

        var configured = ShopConfigurationLoader.load();
        var uiOnly = ShopE2eTestSupport.configurationForHarnessProfile(
                configured, ShopE2eTestSupport.HarnessProfile.UI_ONLY);
        var uiApi = ShopE2eTestSupport.configurationForHarnessProfile(
                configured.withValidationProfile(ShopValidationProfile.UI_ONLY),
                ShopE2eTestSupport.HarnessProfile.UI_API);

        assertEquals(ShopValidationProfile.UI_ONLY, uiOnly.validationProfile());
        assertTrue(uiOnly.api().evidenceBinding().checkpoints().isEmpty());
        assertEquals(ShopValidationProfile.UI_API, uiApi.validationProfile());
        assertTrue(uiApi.api().evidenceBinding().includes(ShopApiCheckpoint.PRODUCT));
        assertTrue(uiApi.api().evidenceBinding().includes(ShopApiCheckpoint.REGISTRATION));
        assertTrue(uiApi.api().evidenceBinding().includes(ShopApiCheckpoint.ORDER));
    }

    @Test
    void suiteHarnessCompositionPreservesTheConfiguredScreenshotPolicy() {
        var yamlConfiguration = ShopConfigurationLoader.load();
        for (ReportScreenshotPolicy policy : ReportScreenshotPolicy.values()) {
            var configured = yamlConfiguration.withScreenshotPolicy(policy);
            for (ShopE2eTestSupport.HarnessProfile harnessProfile
                    : ShopE2eTestSupport.HarnessProfile.values()) {
                var composed = ShopE2eTestSupport.configurationForHarnessProfile(
                        configured, harnessProfile);
                assertEquals(policy, ShopE2eTestSupport.reportProfile(composed, null)
                                .screenshotPolicy(),
                        () -> harnessProfile + " must preserve YAML screenshotPolicy=" + policy);
            }
        }
    }

    @Test
    void suiteExecutionIsDetectedFromItsJUnitPlatformRootWithoutVmProperties() {
        assertTrue(ShopTestSupport.suiteModeEnabled(
                "[engine:junit-platform-suite]/[suite:ShopE2eSuite]/[engine:junit-jupiter]"));
        assertFalse(ShopTestSupport.suiteModeEnabled("[engine:junit-jupiter]"));
    }

    @Test
    void terminalPurchaseScreenRequiresTheObservedOrderAndInvoiceConfirmation() {
        var confirmed = ShopE2eTestSupport.E2eJourney.purchaseConfirmationAssertions(
                new ShopPurchaseFlow.PurchaseResult(
                        "Pliers", "2", "Cash on Delivery", "Thanks for your order! INV-123"));
        var missingInvoice = ShopE2eTestSupport.E2eJourney.purchaseConfirmationAssertions(
                new ShopPurchaseFlow.PurchaseResult(
                        "Pliers", "2", "Cash on Delivery", "Thanks for your order!"));
        var absentConfirmation = ShopE2eTestSupport.E2eJourney.purchaseConfirmationAssertions(
                new ShopPurchaseFlow.PurchaseResult(
                        "Pliers", "2", "Cash on Delivery", null));

        assertEquals(1, confirmed.size());
        assertTrue(confirmed.getFirst().passed());
        assertFalse(missingInvoice.getFirst().passed());
        assertFalse(absentConfirmation.getFirst().passed());
        assertEquals("no confirmation was observed", absentConfirmation.getFirst().actual());
    }

    @ShopE2eTestSupport.WithE2eHarness(
            actionDetails = ShopE2eTestSupport.ActionDetailsMode.SUMMARY_ONLY)
    private static class SummaryOnlyActions { }

    @ShopE2eTestSupport.WithE2eHarness(
            actionDetails = ShopE2eTestSupport.ActionDetailsMode.INCLUDED_COLLAPSED)
    private static class IncludedActions { }

    @Test
    void closesThePerMethodResourceOnlyOnce() throws Exception {
        AtomicInteger closes = new AtomicInteger();
        var resource = new ShopE2eTestSupport.E2eResource(closes::incrementAndGet);

        resource.close();
        resource.close();

        assertEquals(1, closes.get());
    }

    @Test
    void keepsTheBusinessFailurePrimaryWhenTeardownAlsoFails() {
        IOException teardown = new IOException("teardown failed");
        var resource = new ShopE2eTestSupport.E2eResource(() -> { throw teardown; });
        AssertionError business = new AssertionError("observed business failure");

        assertDoesNotThrow(() -> ShopE2eTestSupport.closeAfterTest(resource, business));

        assertEquals(1, business.getSuppressed().length);
        assertSame(teardown, business.getSuppressed()[0]);
    }

    @Test
    void reportsTeardownFailureWhenTheTestItselfPassed() {
        IOException teardown = new IOException("teardown failed");
        var resource = new ShopE2eTestSupport.E2eResource(() -> { throw teardown; });

        assertSame(teardown, assertThrows(IOException.class,
                () -> ShopE2eTestSupport.closeAfterTest(resource, null)));
    }

    @Test
    void classRunRegistrationCacheCreatesOnceAndReturnsTheSameCredentials() {
        var cache = new ShopAuthenticatedAccountTestSupport.ClassRunAccountCache();
        var first = new ShopCredentials("live@practice-shop.invalid-domain", "secret", "ATF", "One");
        var second = new ShopCredentials("live-two@practice-shop.invalid-domain", "secret", "ATF", "Two");

        assertNull(cache.get("class-run"));
        assertSame(first, cache.putIfAbsent("class-run", first));
        assertSame(first, cache.putIfAbsent("class-run", second));
        assertSame(first, cache.get("class-run"));
    }

    @Test
    void uiOnlyRegistrationStillReportsTheObservedBrowserResponseStatus() {
        var success = ShopAuthenticatedAccountTestSupport.registrationAssertions(201, java.util.List.of());
        var failure = ShopAuthenticatedAccountTestSupport.registrationAssertions(409, java.util.List.of());

        assertEquals("REGISTER_PER_RUN", success.get(0).actual());
        assertEquals("HTTP 201", success.get(1).actual());
        assertTrue(success.get(1).passed());
        assertTrue(success.get(2).passed());
        assertEquals("HTTP 409", failure.get(1).actual());
        assertFalse(failure.get(1).passed());
        assertFalse(failure.get(2).passed());

        var inconsistent = ShopAuthenticatedAccountTestSupport.registrationAssertions(201,
                java.util.List.of(new atf.api.reporting.contract.AtfAssertion(
                        "Registration journal", "registration confirmed", "no matching exchange", false)));
        assertTrue(inconsistent.get(1).passed());
        assertFalse(inconsistent.get(2).passed());
    }
}
