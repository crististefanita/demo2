package atf.ui.tests.practiceshop.support;

import atf.api.testkit.AtfUnitTest;
import atf.api.reporting.evidence.ReportScreenshotPolicy;
import atf.api.reporting.evidence.profile.ReportEvidenceCaptureSource;
import atf.api.reporting.evidence.profile.ReportEvidenceFlowMode;
import atf.ui.practiceshop.config.PracticeShopApiCheckpoint;
import atf.ui.practiceshop.config.PracticeShopConfigurationLoader;
import atf.ui.practiceshop.config.PracticeShopCredentials;
import atf.ui.practiceshop.config.PracticeShopValidationProfile;
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
                Class.forName("atf.ui.tests.practiceshop.e2e.PracticeShopPurchaseUiOnlyE2eTest")
                        .getAnnotation(PracticeShopE2eTestSupport.WithE2eHarness.class).profile());
        assertEquals(PracticeShopE2eTestSupport.HarnessProfile.UI_API,
                Class.forName("atf.ui.tests.practiceshop.e2e.PracticeShopPurchaseE2eTest")
                        .getAnnotation(PracticeShopE2eTestSupport.WithE2eHarness.class).profile());

        var singleFlow = Class.forName(
                        "atf.ui.tests.practiceshop.e2e.PracticeShopPurchaseUiOnlySingleFlowE2eTest")
                .getAnnotation(PracticeShopE2eTestSupport.WithE2eHarness.class);
        assertEquals(PracticeShopE2eTestSupport.HarnessProfile.UI_ONLY, singleFlow.profile());
        assertEquals(PracticeShopE2eTestSupport.ReportFlow.UNIFIED_FLOW,
                singleFlow.reportFlow());
        assertEquals(PracticeShopE2eTestSupport.ScreenshotMode.FINAL_ONLY,
                singleFlow.screenshots());
        var reportProfile = PracticeShopE2eTestSupport.reportProfile(
                PracticeShopConfigurationLoader.load(), singleFlow);
        assertEquals(ReportEvidenceFlowMode.UNIFIED_FLOW, reportProfile.flowMode());
        assertEquals(ReportScreenshotPolicy.FINAL_ONLY, reportProfile.screenshotPolicy());
        assertEquals(ReportEvidenceCaptureSource.BROWSER, reportProfile.captureSource());
        assertEquals(ReportScreenshotPolicy.NONE,
                PracticeShopE2eTestSupport.reportProfile(PracticeShopConfigurationLoader.load(), singleFlow,
                        PracticeShopTestSupport.AccountSetupMode.REGISTER_PER_RUN).screenshotPolicy());

        var authenticated = Class.forName(
                        "atf.ui.tests.practiceshop.e2e.PracticeShopAuthenticatedPurchaseE2eTest")
                .getAnnotation(PracticeShopTestSupport.AuthenticatedE2e.class);
        assertEquals(PracticeShopTestSupport.AccountSetupMode.REGISTER_PER_RUN, authenticated.accountSetup());

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

        assertEquals("HTTP 201", success.getFirst().actual());
        assertTrue(success.getFirst().passed());
        assertEquals("HTTP 409", failure.getFirst().actual());
        assertFalse(failure.getFirst().passed());
    }
}
