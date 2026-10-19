package atf.api.reporting.extent.session;
import atf.api.testkit.AtfUnitTest;
import atf.api.reporting.extent.session.ExtentReportFinalizer;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@AtfUnitTest
class ExtentReportFinalizerBoundaryTest {
    @Test
    void publicFacadeRejectsMissingArtifactBeforePublication() {
        Path missing = Path.of("target", "does-not-exist", "report.html");

        assertThrows(java.io.IOException.class,
                () -> ExtentReportFinalizer.finalizeReport(missing));
    }

    @Test
    void publicFacadePublishesThemeRuntimeAndLeavesNoGeneratedAssets() throws Exception {
        Path root = Files.createTempDirectory("atf-finalizer-boundary-");
        Path report = root.resolve("report.html");
        try {
            Files.writeString(report, """
                    <!doctype html>
                    <html><head></head>
                    <body class="spa -report dark">
                    <div class="container-fluid p-4 view dashboard-view"></div>
                    </body></html>
                    """);

            ExtentReportFinalizer.finalizeReport(report);
            String html = Files.readString(report);

            assertAll(
                    () -> assertTrue(html.contains("id=\"atf-report-theme\"")),
                    () -> assertTrue(html.contains("id=\"atf-report-behaviour\"")),
                    () -> assertTrue(html.contains("atf-overview-default")),
                    () -> assertTrue(html.contains("id=\"atf-fails-view\"")),
                    () -> assertFalse(Files.exists(root.resolve("spark"))));
        } finally {
            deleteTree(root);
        }
    }

    private static void deleteTree(Path root) throws Exception {
        if (!Files.exists(root)) return;
        try (var paths = Files.walk(root)) {
            paths.sorted(java.util.Comparator.reverseOrder()).forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (java.io.IOException error) {
                    throw new java.io.UncheckedIOException(error);
                }
            });
        }
    }
}
