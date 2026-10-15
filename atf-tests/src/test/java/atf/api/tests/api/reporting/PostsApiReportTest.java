package atf.api.tests.api.reporting;

import atf.api.execution.ApiModule;
import atf.api.execution.ApiModuleFactory;
import atf.api.execution.lifecycle.ApiScenarioLifecycleRunner;
import atf.api.reporting.ExtentReportSession;
import atf.api.tests.api.posts.PostsApiScenarios;
import atf.api.mock.AtfMockTransport;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Thin IntelliJ-friendly report host for the post scenarios.
 *
 * <p>The scenario definition remains in {@code PostsApiScenarios}; this class
 * only supplies the composition root and the configured Extent session. Run
 * this test directly and the root {@code atf.yaml} controls the timestamped
 * report destination.</p>
 */
class PostsApiReportTest {
    @Test
    void publishesPostsScenariosToTheConfiguredExtentReport() throws Exception {
        ApiModule api = ApiModuleFactory.fromTransport(
                AtfMockTransport.fromProjectConfiguration(), URI.create("https://atf.local"));
        Path report;

        try (var session = ExtentReportSession.openConfigured()) {
            report = session.reportPath();
            PostsApiScenarios.publish(session, api, new ApiScenarioLifecycleRunner());
        }

        assertTrue(Files.isRegularFile(report));
        assertTrue(Files.readString(report).contains("PostsApiTests"));
    }
}
