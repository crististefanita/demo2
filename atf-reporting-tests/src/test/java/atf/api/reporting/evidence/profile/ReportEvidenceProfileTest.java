package atf.api.reporting.evidence.profile;

import atf.api.reporting.evidence.ReportScreenshotPolicy;
import atf.api.testkit.AtfUnitTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@AtfUnitTest
class ReportEvidenceProfileTest {
    @Test
    void mapsTheThreeIndependentEvidenceAxes() {
        var profile = ReportEvidenceProfile.fromConfig(
                "unified-flow", "final-and-failure", "mock");

        assertEquals(ReportEvidenceFlowMode.UNIFIED_FLOW, profile.flowMode());
        assertEquals(ReportScreenshotPolicy.FINAL_AND_FAILURE, profile.screenshotPolicy());
        assertEquals(ReportEvidenceCaptureSource.DETERMINISTIC_MOCK, profile.captureSource());
        assertEquals("unified-flow", profile.flowMode().configValue());
        assertEquals("mock", profile.captureSource().configValue());
    }

    @Test
    void builderAndRoundTripKeepTheProfileImmutable() {
        var profile = ReportEvidenceProfile.builder()
                .flowMode(ReportEvidenceFlowMode.UNIFIED_FLOW)
                .screenshotPolicy(ReportScreenshotPolicy.FINAL_ONLY)
                .captureSource(ReportEvidenceCaptureSource.BROWSER)
                .build();

        assertEquals(profile, profile.toBuilder().build());
        assertTrue(profile.captures(ReportScreenshotPolicy.CapturePoint.FINAL));
    }

    @Test
    void rejectsUnknownVocabularyInsteadOfGuessing() {
        assertThrows(IllegalArgumentException.class,
                () -> ReportEvidenceProfile.fromConfig("unified", "final-only", "mock"));
        assertThrows(IllegalArgumentException.class,
                () -> ReportEvidenceProfile.fromConfig("unified-flow", "final-only", "fixture"));
    }
}
