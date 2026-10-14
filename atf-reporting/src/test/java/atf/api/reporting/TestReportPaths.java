package atf.api.reporting;

import java.nio.file.Path;

/** Test-only output boundary for isolated Maven validation runs. */
final class TestReportPaths {
    private TestReportPaths() { }

    static Path report(String fileName) {
        String configured = System.getProperty("atf.test.reportDirectory");
        Path directory = configured == null || configured.isBlank()
                ? Path.of("target")
                : Path.of(configured.trim()).resolve("unit");
        return directory.resolve(fileName).toAbsolutePath().normalize();
    }
}
