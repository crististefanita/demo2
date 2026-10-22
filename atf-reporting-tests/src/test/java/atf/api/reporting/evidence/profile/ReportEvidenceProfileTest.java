package atf.api.reporting.evidence.profile;

import atf.api.reporting.evidence.ReportScreenshotPolicy;
import atf.api.reporting.model.ReportScreenshot;
import atf.api.reporting.support.ReportTestScreenshots;
import atf.api.testkit.AtfUnitTest;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@AtfUnitTest
class ReportEvidenceProfileTest {
    @Test
    void mapsTheThreeIndependentEvidenceAxes() {
        var profile = ReportEvidenceProfile.fromConfig(
                "unified-flow", "final-and-failure", "mock", "summary-only");

        assertEquals(ReportEvidenceFlowMode.UNIFIED_FLOW, profile.flowMode());
        assertEquals(ReportScreenshotPolicy.FINAL_AND_FAILURE, profile.screenshotPolicy());
        assertEquals(ReportEvidenceCaptureSource.DETERMINISTIC_MOCK, profile.captureSource());
        assertEquals("unified-flow", profile.flowMode().configValue());
        assertEquals("mock", profile.captureSource().configValue());
        assertEquals(ReportUiActionDetailsMode.SUMMARY_ONLY, profile.actionDetailsMode());
    }

    @Test
    void builderAndRoundTripKeepTheProfileImmutable() {
        var profile = ReportEvidenceProfile.builder()
                .flowMode(ReportEvidenceFlowMode.UNIFIED_FLOW)
                .screenshotPolicy(ReportScreenshotPolicy.FINAL_ONLY)
                .captureSource(ReportEvidenceCaptureSource.BROWSER)
                .actionDetailsMode(ReportUiActionDetailsMode.SUMMARY_ONLY)
                .build();

        assertEquals(profile, profile.toBuilder().build());
        assertTrue(profile.captures(ReportScreenshotPolicy.CapturePoint.FINAL));
        assertEquals(ReportUiActionDetailsMode.SUMMARY_ONLY, profile.toBuilder().build().actionDetailsMode());
    }

    @Test
    void rejectsUnknownVocabularyInsteadOfGuessing() {
        assertThrows(IllegalArgumentException.class,
                () -> ReportEvidenceProfile.fromConfig("unified", "final-only", "mock"));
        assertThrows(IllegalArgumentException.class,
                () -> ReportEvidenceProfile.fromConfig("unified-flow", "final-only", "fixture"));
    }

    @Test
    void filtersBeforeCallingTheProviderAndRequiresProvenanceAndValidPng() {
        var profile = ReportEvidenceProfile.fromConfig(
                "unified-flow", "final-only", "mock");
        var calls = new AtomicInteger();
        ReportEvidenceCaptureProvider valid = provider(
                ReportEvidenceCaptureSource.DETERMINISTIC_MOCK,
                ReportTestScreenshots.ONE_PIXEL_PNG, calls);

        assertTrue(profile.capture(ReportScreenshotPolicy.CapturePoint.CHECKPOINT,
                "checkpoint", "#checkpoint", valid).isEmpty());
        assertEquals(0, calls.get());
        assertEquals("final", profile.capture(ReportScreenshotPolicy.CapturePoint.FINAL,
                "final", "#final", valid).orElseThrow().title());
        assertEquals(1, calls.get());
        assertThrows(IllegalArgumentException.class,
                () -> profile.capture(ReportScreenshotPolicy.CapturePoint.FINAL,
                        "final", " ", valid));
        assertThrows(IllegalArgumentException.class,
                () -> profile.capture(ReportScreenshotPolicy.CapturePoint.FINAL,
                        "final", "#final", provider(ReportEvidenceCaptureSource.BROWSER,
                                ReportTestScreenshots.ONE_PIXEL_PNG, calls)));
        assertThrows(IllegalArgumentException.class,
                () -> profile.capture(ReportScreenshotPolicy.CapturePoint.FINAL,
                        "final", "#final", provider(ReportEvidenceCaptureSource.DETERMINISTIC_MOCK,
                                "AQID", calls)));
        assertEquals(2, calls.get(), "only permitted captures with matching sources reach a provider");
    }

    private static ReportEvidenceCaptureProvider provider(ReportEvidenceCaptureSource source,
                                                          String payload, AtomicInteger calls) {
        return new ReportEvidenceCaptureProvider() {
            @Override public ReportEvidenceCaptureSource source() { return source; }
            @Override public ReportScreenshot capture(String title, String target) {
                calls.incrementAndGet();
                return new ReportScreenshot(title, payload);
            }
        };
    }
}
