package atf.api.reporting.extent.session;
import atf.api.testkit.AtfUnitTest;

import atf.api.reporting.support.TestReportPaths;
import atf.api.reporting.support.ApiReportTestUrls;
import atf.api.reporting.contract.AtfAssertion;
import atf.api.reporting.contract.AtfReportStatus;
import atf.api.reporting.artifact.ReportArtifactInspector;
import atf.api.reporting.configuration.ReportingSettings;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@AtfUnitTest
class ExtentReportSessionLifecycleTest {
    @Test
    void extentJsonBodyUsesConfiguredLimitAfterRedactionAndKeepsCodeBlockContract() throws Exception {
        Path root = Files.createTempDirectory("atf-extent-body-limit-");
        Path report = root.resolve("bounded.html");
        try {
            try (var session = new ExtentReportSession(report, new ReportingSettings(240, 40))) {
                var scenario = session.startScenario("BodyTests", "bounded response", List.of("api"));
                var phase = scenario.startPhase("TEST BODY");
                var request = phase.startRequest("large response", "GET", ApiReportTestUrls.GENERIC_USERS);
                request.json("RESPONSE JSON", "{\"api_key\":\"raw-token\",\"payload\":\"012345678901234567890123456789\"}");
                request.complete(AtfReportStatus.PASS);
                phase.complete(AtfReportStatus.PASS);
                scenario.complete(AtfReportStatus.PASS);
            }

            var html = Files.readString(report);

            assertAll(
                    () -> assertTrue(html.contains("response-json-body")),
                    () -> assertTrue(html.contains("helm-scroll-block-body")),
                    () -> assertTrue(html.contains("… [truncated; showing first 40 of ")),
                    () -> assertTrue(html.contains("***REDACTED***")),
                    () -> assertFalse(html.contains("raw-token")),
                    () -> assertFalse(html.contains("012345678901234567890123456789\"}")));
        } finally {
            deleteTree(root);
        }
    }

    @Test
    void flushKeepsTheFinalPathPrivateUntilClosePublishesTheCompleteArtifact() throws Exception {
        Path root = Files.createTempDirectory("atf-incomplete-flush-");
        Path report = root.resolve("report.html");
        try {
            try (var session = new ExtentReportSession(report)) {
                var scenario = session.startScenario("FlushTests", "incomplete flush", List.of("reporting"));
                scenario.complete(AtfReportStatus.PASS);
                session.flush();

                assertFalse(Files.exists(report), "flush must not expose the private staged report");
                try (var children = Files.list(root)) {
                    assertTrue(children.anyMatch(path ->
                            path.getFileName().toString().startsWith(".atf-extent-session-")));
                }
            }
            assertTrue(Files.exists(report), "close must publish the finalized report");
        } finally {
            deleteTree(root);
        }
    }

    @Test
    void configuredSessionPublishesOneTimestampedExtentArtifact() throws Exception {
        Path root = Files.createTempDirectory("atf-configured-timestamped-report-");
        try {
            Path config = root.resolve("atf.yaml");
            Files.writeString(config, """
                    reporting:
                      extent:
                        outputDirectory: reports
                        fileName: api-atf-extent-report.html
                        timestampEnabled: true
                        timestampPattern: yyyy-MM-dd_HH-mm-ss-SSS
                        timestampZone: UTC
                    """);

            Path reportPath;
            try (var session = ExtentReportSession.openConfigured(config)) {
                reportPath = session.reportPath();
                var scenario = session.startScenario("ConfiguredReportTests", "timestamped report", List.of("reporting"));
                scenario.complete(AtfReportStatus.PASS);

                assertTrue(reportPath.getFileName().toString()
                        .matches("api-atf-extent-report_\\d{4}-\\d{2}-\\d{2}_\\d{2}-\\d{2}-\\d{2}-\\d{3}\\.html"));
                assertFalse(Files.exists(reportPath), "the final artifact is published only on close");
            }

            assertTrue(Files.exists(reportPath));
            assertTrue(Files.readString(reportPath).contains("timestamped report"));
        } finally {
            deleteTree(root);
        }
    }

    @Test
    void parallelSessionsPublishIndependentAutonomousReports() throws Exception {
        Path root = Files.createTempDirectory("atf-parallel-reports-");
        Path first = root.resolve("parallel-one.html");
        Path second = root.resolve("parallel-two.html");
        try {
            try (var executor = Executors.newFixedThreadPool(2)) {
                var firstRun = executor.submit(() -> publishParallelScenario(first, "parallel one"));
                var secondRun = executor.submit(() -> publishParallelScenario(second, "parallel two"));
                firstRun.get(30, TimeUnit.SECONDS);
                secondRun.get(30, TimeUnit.SECONDS);
            }

            String firstHtml = Files.readString(first);
            String secondHtml = Files.readString(second);
            boolean sessionDirectoriesRemain;
            try (var children = Files.list(root)) {
                sessionDirectoriesRemain = children.anyMatch(path ->
                        path.getFileName().toString().startsWith(".atf-extent-session-"));
            }
            assertTrue(Files.exists(first));
            assertTrue(Files.exists(second));
            assertAll(
                    () -> assertTrue(firstHtml.contains("parallel one")),
                    () -> assertTrue(secondHtml.contains("parallel two")),
                    () -> assertFalse(firstHtml.contains("spark/")),
                    () -> assertFalse(secondHtml.contains("spark/")),
                    () -> assertFalse(sessionDirectoriesRemain));
        } finally {
            deleteTree(root);
        }
    }

    @Test
    void sessionRejectsCrossThreadMutationInsteadOfRacingExtentState() throws Exception {
        Path report = TestReportPaths.report("extent-thread-confined.html");
        try (var session = new ExtentReportSession(report);
             var executor = Executors.newSingleThreadExecutor()) {
            var failure = executor.submit(() -> assertThrows(IllegalStateException.class,
                    () -> session.startScenario("ParallelTests", "cross-thread mutation", List.of("parallel"))))
                    .get(10, TimeUnit.SECONDS);
            assertNotNull(failure);
        }
    }

    @Test
    void competingSessionsKeepTheSharedFinalPathAtomic() throws Exception {
        Path root = Files.createTempDirectory("atf-shared-report-");
        Path report = root.resolve("shared.html");
        try {
            try (var executor = Executors.newFixedThreadPool(2)) {
                var firstRun = executor.submit(() -> publishParallelEvidenceScenario(report, "shared one"));
                var secondRun = executor.submit(() -> publishParallelEvidenceScenario(report, "shared two"));
                firstRun.get(30, TimeUnit.SECONDS);
                secondRun.get(30, TimeUnit.SECONDS);
            }

            var inspection = ReportArtifactInspector.inspect(report);
            assertTrue(inspection.isCompliant(), () -> "Shared report violations: " + inspection.violations());
            String html = Files.readString(report);
            assertTrue(html.contains("shared one") || html.contains("shared two"));
            try (var children = Files.list(root)) {
                assertFalse(children.anyMatch(path ->
                        path.getFileName().toString().startsWith(".atf-extent-session-")));
            }
        } finally {
            deleteTree(root);
        }
    }

    @Test
    void flushKeepsIntermediateStatePrivateUntilClosePublishesTheAutonomousArtifact() throws Exception {
        Path root = Files.createTempDirectory("atf-flush-boundary-");
        Path report = root.resolve("flush-boundary.html");
        try {
            var session = new ExtentReportSession(report);
            try {
                var scenario = session.startScenario("FlushTests", "flush does not publish partial output",
                        List.of("reporting", "lifecycle"));
                var phase = scenario.startPhase("TEST BODY");
                phase.startRequest("health", "GET", ApiReportTestUrls.GENERIC_HEALTH).complete(AtfReportStatus.PASS);
                phase.complete(AtfReportStatus.PASS);
                scenario.complete(AtfReportStatus.PASS);

                session.flush();

                assertFalse(Files.exists(report), "flush must not publish a partial final artifact");
                try (var children = Files.list(root)) {
                    assertTrue(children.anyMatch(path ->
                            path.getFileName().toString().startsWith(".atf-extent-session-")),
                            "flush should keep the private staging workspace alive until close");
                }
            } finally {
                session.close();
            }

            assertTrue(Files.isRegularFile(report));
            assertTrue(Files.readString(report).contains("flush does not publish partial output"));
            assertFalse(Files.exists(root.resolve("spark")));
            try (var children = Files.list(root)) {
                assertFalse(children.anyMatch(path ->
                        path.getFileName().toString().startsWith(".atf-extent-session-")));
            }
        } finally {
            deleteTree(root);
        }
    }

    @Test
    void failedPublicationStillCleansPrivateWorkspaceAndPreservesBlockedDestination() throws Exception {
        Path root = Files.createTempDirectory("atf-blocked-publication-");
        Path blockedDestination = root.resolve("blocked-report");
        Files.createDirectories(blockedDestination);
        Path sentinel = blockedDestination.resolve("sentinel.txt");
        Files.writeString(sentinel, "destination-owned");
        try {
            var session = new ExtentReportSession(blockedDestination);
            var scenario = session.startScenario("PublicationTests", "blocked destination", List.of("reporting"));
            scenario.complete(AtfReportStatus.PASS);

            assertThrows(IllegalStateException.class, session::close,
                    "a non-empty destination directory must not be replaced by a report file");

            assertTrue(Files.isDirectory(blockedDestination));
            assertEquals("destination-owned", Files.readString(sentinel));
            try (var children = Files.list(root)) {
                assertFalse(children.anyMatch(path ->
                        path.getFileName().toString().startsWith(".atf-extent-session-")),
                        "a failed publication must not leak the private workspace");
            }
        } finally {
            deleteTree(root);
        }
    }

    private static void publishParallelScenario(Path report, String name) {
        try (var session = new ExtentReportSession(report)) {
            var scenario = session.startScenario("ParallelTests", name, List.of("parallel"));
            var body = scenario.startPhase("TEST BODY");
            var request = body.startRequest("health", "GET", ApiReportTestUrls.GENERIC_HEALTH);
            request.complete(AtfReportStatus.PASS);
            body.complete(AtfReportStatus.PASS);
            scenario.complete(AtfReportStatus.PASS);
        }
    }

    private static void publishParallelEvidenceScenario(Path report, String name) {
        try (var session = new ExtentReportSession(report)) {
            var scenario = session.startScenario("ParallelTests", name, List.of("parallel"));
            var body = scenario.startPhase("TEST BODY");
            var request = body.startRequest("health", "GET", ApiReportTestUrls.GENERIC_HEALTH);
            request.detail("response.duration.ms", "1");
            request.json("RESPONSE JSON", "{\"status\":\"ok\"}");
            var validation = request.startValidation("Validate health response");
            validation.assertions(List.of(new AtfAssertion("HTTP status", "200", "200", true)));
            validation.complete(AtfReportStatus.PASS);
            request.complete(AtfReportStatus.PASS);
            body.complete(AtfReportStatus.PASS);
            scenario.complete(AtfReportStatus.PASS);
        }
    }

    private static void deleteTree(Path root) throws Exception {
        if (!Files.exists(root)) return;
        try (var paths = Files.walk(root)) {
            paths.sorted(java.util.Comparator.reverseOrder()).forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (java.io.IOException error) {
                    throw new RuntimeException(error);
                }
            });
        }
    }

}

