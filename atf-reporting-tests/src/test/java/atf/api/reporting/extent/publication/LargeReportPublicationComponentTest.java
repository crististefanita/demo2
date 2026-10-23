package atf.api.reporting.extent.publication;

import atf.api.reporting.support.TestReportPaths;
import atf.api.reporting.support.publication.LargeReportPublicationProbe;
import atf.api.testkit.AtfComponentTest;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Bounded-heap child JVM proof for inline PNG publication. */
@AtfComponentTest
class LargeReportPublicationComponentTest {
    @Test
    void publishesLargeUnicodeAndRepeatedInlinePngsWithinBoundedHeap() throws Exception {
        Path report = TestReportPaths.report("large-publication-memory-probe.html");
        Path log = report.resolveSibling("large-publication-memory-probe.log");
        Files.createDirectories(report.getParent());
        String java = Path.of(System.getProperty("java.home"), "bin", "java").toString();
        var command = List.of(java, "-Xms64m", "-Xmx384m", "-cp",
                System.getProperty("java.class.path"),
                LargeReportPublicationProbe.class.getName(), report.toString());
        Process child = new ProcessBuilder(command)
                .redirectErrorStream(true)
                .redirectOutput(log.toFile())
                .start();
        assertTrue(child.waitFor(180, TimeUnit.SECONDS),
                () -> "bounded publication did not finish; inspect " + log);
        String output = Files.readString(log);
        assertEquals(0, child.exitValue(), () -> "bounded publication failed: " + output);
        assertTrue(output.contains("PEAK_HEAP_BYTES="), () -> "missing heap observation: " + output);
        assertTrue(Files.size(report) > 9_000_000, "published report must retain representative Unicode volume");
        String html = Files.readString(report);
        assertTrue(html.contains("evidence-end-marker"), "the final evidence must survive publication");
        assertTrue(html.contains("id=\"atf-report-theme\""), "theme must be fully published");
        assertTrue(html.contains("id=\"atf-report-behaviour\""), "runtime must be fully published");
        assertTrue(html.contains("atf-screenshot-screenshot-1"), "the first PNG must stay actionable");
        assertTrue(html.contains("atf-screenshot-screenshot-2"), "the distinct PNG must stay actionable");
        assertTrue(html.contains("</html>"), "the report must end as a complete document");
    }
}
