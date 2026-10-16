package atf.api.reporting;

import atf.api.core.http.ApiExecutionResult;
import atf.api.core.http.ApiResponse;
import atf.api.core.reporting.AtfAssertion;
import atf.api.core.reporting.AtfReportStatus;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AtfReportSemanticSnapshotTest {
    @Test
    void extentAndFallbackEmitTheSameImmutableSemanticPopulation() throws Exception {
        Path root = Files.createTempDirectory("atf-semantic-snapshot-");
        try {
            var extent = new ExtentReportSession(root.resolve("extent.html"));
            publish(extent);
            var extentSnapshot = extent.semanticSnapshot();
            extent.close();

            var fallback = new HtmlReportSession(root.resolve("fallback.html"));
            publish(fallback);
            var fallbackSnapshot = fallback.semanticSnapshot();
            fallback.close();

            assertEquals(extentSnapshot, fallbackSnapshot);
            assertEquals(1, extentSnapshot.classes().size());
            assertEquals(1, extentSnapshot.scenarioCount());
            assertEquals(1, extentSnapshot.requestCount());
            assertEquals(1, extentSnapshot.validationCount());
            assertTrue(extentSnapshot.classes().getFirst().scenarios().getFirst()
                    .phases().getFirst().nodes().getFirst() instanceof AtfReportSemanticSnapshot.RequestSnapshot);
        } finally {
            deleteTree(root);
        }
    }

    @Test
    void snapshotCollectionsAreImmutableAtEveryNestedLevel() {
        var recorder = new AtfReportSemanticRecorder();
        var scenario = recorder.startScenario("Tests", null, "scenario", List.of("api"));
        var phase = scenario.startPhase("TEST BODY");
        var request = phase.startRequest("read", "GET", "/users/1");
        request.json("RESPONSE JSON");
        var validation = request.startValidation("Validate response");
        validation.assertions(List.of(new AtfAssertion("status", "200", "200", true)));
        validation.complete(AtfReportStatus.PASS);
        request.complete(AtfReportStatus.PASS);
        phase.complete(AtfReportStatus.PASS);
        scenario.complete(AtfReportStatus.PASS);

        var snapshot = recorder.snapshot();
        assertThrowsUnsupported(snapshot.classes());
        assertThrowsUnsupported(snapshot.classes().getFirst().scenarios());
        assertThrowsUnsupported(snapshot.classes().getFirst().scenarios().getFirst().phases());
    }

    private static void publish(atf.api.core.reporting.AtfReportPorts.Session session) {
        var scenario = session.startScenario("Tests", "scenario", List.of("api", "contract"));
        var phase = scenario.startPhase("TEST BODY");
        var request = phase.startRequest("read", "GET", "/users/1");
        request.detail("HTTP STATUS", "200");
        request.json("REQUEST JSON", "null");
        request.json("RESPONSE JSON", "{\"id\":1}");
        request.execution(new ApiExecutionResult(
                new ApiResponse(200, "ok", Map.of(), Duration.ofMillis(4), "https://atf.local/users/1"),
                1, Duration.ofMillis(4), List.of(), List.of()));
        var validation = request.startValidation("Validate response");
        validation.assertions(List.of(new AtfAssertion("HTTP status", "200", "200", true)));
        validation.complete(AtfReportStatus.PASS);
        request.complete(AtfReportStatus.PASS);
        phase.complete(AtfReportStatus.PASS);
        scenario.complete(AtfReportStatus.PASS);
    }

    private static void assertThrowsUnsupported(List<?> values) {
        org.junit.jupiter.api.Assertions.assertThrows(UnsupportedOperationException.class,
                () -> values.clear());
    }

    private static void deleteTree(Path root) throws Exception {
        if (Files.notExists(root)) return;
        try (var paths = Files.walk(root)) {
            paths.sorted(java.util.Comparator.reverseOrder()).forEach(path -> {
                try { Files.deleteIfExists(path); }
                catch (java.io.IOException error) { throw new RuntimeException(error); }
            });
        }
    }
}
