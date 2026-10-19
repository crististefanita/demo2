package atf.api.reporting.html;
import atf.api.testkit.AtfUnitTest;

import atf.api.core.reporting.contract.AtfAssertion;
import atf.api.core.reporting.contract.AtfReportStatus;
import atf.api.core.http.request.HttpMethod;
import atf.api.core.http.response.ApiOperationLabel;
import atf.api.mock.MockRoutePaths;
import atf.api.reporting.html.contract.HtmlReportStaticContract;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Component contract: semantic input → fallback HTML → static artifact audit.
 * No Playwright or external server is needed to validate the reporting module.
 */
@AtfUnitTest
class HtmlReportComponentContractTest {
    @Test
    void auditsThePublishedHtmlThroughRendererIndependentArtifactChecks() throws Exception {
        Path report = Files.createTempDirectory("atf-html-component-").resolve("report.html");
        try (var session = new HtmlReportSession(report)) {
            var scenario = session.startScenario("ComponentReportTests",
                    "API evidence component contract", List.of("api", "component"));
            var phase = scenario.startPhase("TEST BODY");
            var request = phase.startRequest(
                    ApiOperationLabel.format(HttpMethod.GET, MockRoutePaths.userById("1"), null),
                    HttpMethod.GET.value(), MockRoutePaths.userById("1"));
            request.detail("HTTP STATUS", "200");
            request.json("REQUEST JSON", "null");
            request.json("RESPONSE JSON", "{\"id\":1,\"name\":\"ATF User\"}");
            var validation = request.startValidation("Validate response contract");
            validation.assertions(List.of(new AtfAssertion("HTTP status", "200", "200", true)));
            validation.complete(AtfReportStatus.PASS);
            request.complete(AtfReportStatus.PASS);
            phase.complete(AtfReportStatus.PASS);
            scenario.complete(AtfReportStatus.PASS);
        }

        var evaluation = HtmlReportStaticContract.evaluate(Files.readString(report));

        assertTrue(Files.isRegularFile(report));
        assertTrue(evaluation.isCompliant(), () -> "Component violations: " + evaluation.violations());
        assertTrue(evaluation.satisfies("request validation roles"));
        assertTrue(evaluation.satisfies("json evidence channels"));
        assertTrue(evaluation.satisfies("runtime asset"));
    }
}
