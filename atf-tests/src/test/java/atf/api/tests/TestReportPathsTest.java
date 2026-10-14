package atf.api.tests;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TestReportPathsTest {
    @Test
    void createsSafeTimestampedReportNameWithoutChangingOutputDirectory() {
        System.setProperty("atf.test.reportDirectory", "target/parallel-reports");
        try {
            Path report = TestReportPaths.timestampedReport(
                    "api-atf-extent-report.html",
                    Instant.parse("2026-10-14T11:22:33.456Z"));

            assertEquals("api-atf-extent-report_2026-10-14_14-22-33-456.html",
                    report.getFileName().toString());
            assertTrue(report.toString().replace('\\', '/').contains("target/parallel-reports/"));
        } finally {
            System.clearProperty("atf.test.reportDirectory");
        }
    }
}
