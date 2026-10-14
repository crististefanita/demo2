package atf.api.tests;

import java.nio.file.Path;
import java.nio.file.Files;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/** Test-only output boundary; keeps isolated validation runs away from live artifacts. */
final class TestReportPaths {
    private static final String OUTPUT_PROPERTY = "atf.test.reportDirectory";
    private static final DateTimeFormatter REPORT_TIMESTAMP = DateTimeFormatter
            .ofPattern("yyyy-MM-dd_HH-mm-ss-SSS")
            .withZone(ZoneId.systemDefault());

    private TestReportPaths() { }

    static Path report(String fileName) {
        String configured = System.getProperty(OUTPUT_PROPERTY);
        Path directory = configured == null || configured.isBlank()
                ? Path.of("artifacts", "reports")
                : Path.of(configured.trim());
        return directory.resolve(fileName).toAbsolutePath().normalize();
    }

    /**
     * Creates a uniquely named public report for a run. Keeping the timestamp
     * in the file name prevents a later execution from replacing a report that
     * is still open in the MCP tab or belongs to a parallel run.
     */
    static Path timestampedReport(String baseFileName) {
        return timestampedReport(baseFileName, Instant.now());
    }

    static Path timestampedReport(String baseFileName, Instant timestamp) {
        String trimmed = baseFileName == null ? "" : baseFileName.trim();
        int extension = trimmed.lastIndexOf('.');
        String stem = extension > 0 ? trimmed.substring(0, extension) : trimmed;
        String suffix = extension > 0 ? trimmed.substring(extension) : ".html";
        return report(stem + "_" + REPORT_TIMESTAMP.format(timestamp) + suffix);
    }

    /**
     * Creates an isolated report path for a contract test.  Auxiliary tests
     * must never overwrite the canonical Extent artifact produced by the
     * public report-producing test; doing so makes the report depend on Maven
     * test order.
     */
    static Path privateReport(String fileName) throws IOException {
        Path directory = Files.createTempDirectory("atf-report-test-");
        return directory.resolve(fileName).toAbsolutePath().normalize();
    }
}
