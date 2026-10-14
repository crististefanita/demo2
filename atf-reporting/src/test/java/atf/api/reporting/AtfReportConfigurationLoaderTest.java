package atf.api.reporting;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;

class AtfReportConfigurationLoaderTest {
    @Test
    void resolvesRelativeOutputFromYamlLocationInsteadOfWorkingDirectory() throws Exception {
        Path project = Files.createTempDirectory("atf-config-");
        Path config = project.resolve("config").resolve("atf.yaml");
        Files.createDirectories(config.getParent());
        Files.writeString(config, """
                reporting:
                  extent:
                    outputDirectory: reports/extent
                    fileName: intellij-run.html
                """);

        var settings = AtfReportConfigurationLoader.load(config);

        assertEquals(project.resolve("config/reports/extent").toAbsolutePath().normalize(), settings.outputDirectory());
        assertEquals("intellij-run.html", settings.fileName());
        assertEquals(settings.outputDirectory().resolve("intellij-run.html"), settings.reportPath());
        assertTrue(settings.timestampEnabled());
    }

    @Test
    void resolvesAConfigurableTimestampBeforeTheFileExtension() throws Exception {
        Path project = Files.createTempDirectory("atf-timestamp-config-");
        Path config = project.resolve("atf.yaml");
        Files.writeString(config, """
                reporting:
                  extent:
                    outputDirectory: reports
                    fileName: api-atf-extent-report.html
                    timestampEnabled: true
                    timestampPattern: yyyy-MM-dd_HH-mm-ss-SSS
                    timestampZone: UTC
                """);

        var settings = AtfReportConfigurationLoader.load(config);
        Path resolved = settings.resolveReportPath(Clock.fixed(
                Instant.parse("2026-10-14T12:34:56.789Z"), ZoneOffset.UTC));

        assertEquals(project.resolve("reports/api-atf-extent-report_2026-10-14_12-34-56-789.html")
                .toAbsolutePath().normalize(), resolved);
    }

    @Test
    void keepsStablePathWhenTimestampingIsDisabled() {
        var settings = new ExtentReportConfiguration(Path.of("target"), "report.html",
                false, "yyyy-MM-dd_HH-mm-ss-SSS", "UTC");

        assertEquals(settings.reportPath(), settings.resolveReportPath(Clock.fixed(
                Instant.parse("2026-10-14T12:34:56Z"), ZoneOffset.UTC)));
    }

    @Test
    void rejectsInvalidTimestampConfiguration() {
        assertAll(
                () -> assertThrows(IllegalArgumentException.class, () ->
                        new ExtentReportConfiguration(Path.of("target"), "report.html", true, "", "UTC")),
                () -> assertThrows(IllegalArgumentException.class, () ->
                        new ExtentReportConfiguration(Path.of("target"), "report.html", true, "yyyy", "Not/AZone")),
                () -> assertThrows(IllegalArgumentException.class, () ->
                        new ExtentReportConfiguration(Path.of("target"), "report.html", true, "yyyy/MM/dd", "UTC")),
                () -> assertThrows(IllegalStateException.class, () -> {
                    Path config = Files.createTempFile("atf-invalid-timestamp-", ".yaml");
                    try {
                        Files.writeString(config, "reporting:\n  extent:\n    timestampEnabled: maybe\n");
                        AtfReportConfigurationLoader.load(config);
                    } finally {
                        Files.deleteIfExists(config);
                    }
                }));
    }

    @Test
    void usesSafeDefaultsWhenNoYamlIsPresent() {
        var settings = ExtentReportConfiguration.defaults(Path.of("C:/atf"));

        assertEquals(Path.of("C:/atf/target").toAbsolutePath().normalize(), settings.outputDirectory());
        assertEquals("api-atf-extent-report.html", settings.fileName());
    }

    @Test
    void discoversConfigurationFromAParentDirectory() throws Exception {
        Path project = Files.createTempDirectory("atf-discovery-");
        Path nested = project.resolve("atf-tests/target");
        Files.createDirectories(nested);
        Path config = project.resolve("atf.yml");
        Files.writeString(config, "reporting:\n  extent:\n    fileName: discovered.html\n");

        assertEquals(config.toAbsolutePath().normalize(), AtfReportConfigurationLoader.discover(nested));
    }

    @Test
    void rejectsAPathAsTheConfiguredFileName() {
        assertThrows(IllegalArgumentException.class,
                () -> new ExtentReportConfiguration(Path.of("target"), "nested/report.html"));
    }
}
