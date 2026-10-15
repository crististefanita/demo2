package atf.api.reporting;

import atf.api.core.reporting.AtfReportStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Locks the class-root package metadata consumed by the Tests tab and Overview. */
class ExtentReportClassMetadataTest {
    @Test
    void rendersJavaPackageUnderTheSelectedClassInTheCentralDetail(@TempDir Path temp) throws Exception {
        Path report = temp.resolve("package.html");
        try (var session = new ExtentReportSession(report)) {
            var scenario = session.startScenario(
                    "ApiHeadersAndEdgeCasesTests",
                    "atf.api.tests.api.headers",
                    "GET /public/v2/catalog — request + response headers",
                    List.of("api", "headers"));
            scenario.complete(AtfReportStatus.PASS);
        }

        String html = Files.readString(report);
        assertTrue(html.contains("atf.api.tests.api.headers"),
                "the package must travel with the Extent class root");
        assertTrue(html.contains("function normalizeTestPackageMetadata()"),
                "the Tests tab must project the package into the central detail header");
        assertTrue(html.contains("atf-test-package"),
                "the package projection needs a semantic CSS/DOM hook");
    }

    @Test
    void legacyScenarioStartDoesNotInventPackageMetadata(@TempDir Path temp) throws Exception {
        Path report = temp.resolve("legacy.html");
        try (var session = new ExtentReportSession(report)) {
            var scenario = session.startScenario("LegacyTests", "legacy scenario", List.of("api"));
            scenario.complete(AtfReportStatus.PASS);
        }

        String html = Files.readString(report);
        assertFalse(html.contains("author=\"Unspecified\""),
                "legacy callers must not receive fabricated package metadata");
    }
}
