package atf.ui.tests.practiceshop.component.configuration;

import atf.api.core.http.protocol.ApiUris;
import atf.api.testkit.AtfComponentTest;
import atf.api.reporting.evidence.ReportScreenshotPolicy;
import atf.api.reporting.evidence.profile.ReportEvidenceCaptureProvider;
import atf.api.reporting.evidence.profile.ReportEvidenceCaptureSource;
import atf.api.reporting.evidence.profile.ReportEvidenceFlowMode;
import atf.api.reporting.model.ReportScreenshot;
import atf.ui.practiceshop.config.ShopConfigurationLoader;
import atf.ui.practiceshop.support.reporting.ShopScreenshotPolicy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Component contract for the Practice Shop checkpoint screenshot policy. */
@AtfComponentTest
class ShopScreenshotPolicyTest {
    private static final String ONE_PIXEL_PNG =
            "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNk+A8AAQUBAScY42YAAAAASUVORK5CYII=";

    @Test
    void usesTheConfiguredPolicyWithoutChangingTheGenericEvidenceShape() {
        var allCheckpoints = ShopScreenshotPolicy.profile(ReportScreenshotPolicy.EACH_CHECKPOINT);
        var disabled = ShopScreenshotPolicy.profile(ReportScreenshotPolicy.NONE);

        assertEquals(ReportEvidenceFlowMode.SCREEN_CHECKPOINTS, allCheckpoints.flowMode());
        assertEquals(ReportEvidenceCaptureSource.BROWSER, disabled.captureSource());
        assertEquals(ReportScreenshotPolicy.EACH_CHECKPOINT, allCheckpoints.screenshotPolicy());
        assertEquals(ReportScreenshotPolicy.NONE, disabled.screenshotPolicy());
    }

    @Test
    void policyFiltersTheCaptureProviderBeforeBrowserWork() {
        AtomicInteger captures = new AtomicInteger();
        ReportEvidenceCaptureProvider provider = new ReportEvidenceCaptureProvider() {
            @Override public ReportEvidenceCaptureSource source() {
                return ReportEvidenceCaptureSource.BROWSER;
            }

            @Override public ReportScreenshot capture(String title, String target) {
                captures.incrementAndGet();
                return new ReportScreenshot(title, ONE_PIXEL_PNG);
            }
        };

        var enabled = ShopScreenshotPolicy.profile(ReportScreenshotPolicy.EACH_CHECKPOINT);
        var disabled = ShopScreenshotPolicy.profile(ReportScreenshotPolicy.NONE);
        assertTrue(enabled.capture(ReportScreenshotPolicy.CapturePoint.CHECKPOINT,
                "UI checkpoint", ApiUris.httpsEndpoint("example.test").toString(),
                provider).isPresent());
        assertTrue(disabled.capture(ReportScreenshotPolicy.CapturePoint.CHECKPOINT,
                "UI checkpoint", ApiUris.httpsEndpoint("example.test").toString(),
                provider).isEmpty());
        assertEquals(1, captures.get(), "YAML-prohibited captures must not invoke Playwright");
    }

    @Test
    void yamlCanExplicitlyProhibitScreenshotsEvenWhenTheLegacySwitchAllowsThem(@TempDir Path temp)
            throws Exception {
        Path yaml = temp.resolve("practice-shop.yaml");
        Files.writeString(yaml, """
                practiceShop:
                  baseUrl: https://practicesoftwaretesting.com
                  screenshotOnFailure: true
                  reporting:
                    screenshotPolicy: none
                """);

        var configuration = ShopConfigurationLoader.load(yaml);

        assertEquals(ReportScreenshotPolicy.NONE, configuration.reporting().screenshotPolicy());
    }

    @Test
    void legacyYamlCanStillRestrictCapturesWhenNoTypedPolicyIsPresent(@TempDir Path temp)
            throws Exception {
        Path yaml = temp.resolve("practice-shop.yaml");
        Files.writeString(yaml, """
                practiceShop:
                  baseUrl: https://practicesoftwaretesting.com
                  screenshotOnFailure: false
                """);

        var configuration = ShopConfigurationLoader.load(yaml);

        assertEquals(ReportScreenshotPolicy.FAILURE_ONLY, configuration.reporting().screenshotPolicy());
    }
}
