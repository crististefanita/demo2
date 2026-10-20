package atf.api.reporting.contract;

import atf.api.testkit.AtfUnitTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

/** Locks the renderer-neutral status vocabulary used by every evidence adapter. */
@AtfUnitTest
class AtfReportStatusTest {
    @Test
    void exposesOneCanonicalMarkerAndResultForEveryStatus() {
        assertAll(
                () -> assertEquals("ℹ️", AtfReportStatus.INFO.marker()),
                () -> assertEquals("✅ PASS", AtfReportStatus.PASS.resultMarker()),
                () -> assertEquals("⚠️ WARNING", AtfReportStatus.WARNING.resultMarker()),
                () -> assertEquals("⏭️ SKIP", AtfReportStatus.SKIP.resultMarker()),
                () -> assertEquals("❌ FAIL", AtfReportStatus.FAIL.resultMarker()),
                () -> assertEquals(AtfReportStatus.INFO, AtfReportStatus.orInfo(null)));
    }
}
