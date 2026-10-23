package atf.api.reporting.support.request;

import atf.api.reporting.api.ReportRequest;
import atf.api.reporting.api.ReportResponse;
import atf.api.reporting.api.ReportTransportProvenance;
import atf.api.reporting.api.evidence.ApiExchangeEvidence;
import atf.api.reporting.api.evidence.ReportBodyAvailability;
import atf.api.reporting.api.request.ReportRequestHeaders;
import atf.api.reporting.console.AtfConsoleSink;
import atf.api.reporting.contract.AtfReportPorts;
import atf.api.reporting.contract.AtfAssertion;
import atf.api.reporting.contract.AtfReportStatus;
import atf.api.reporting.extent.session.ExtentReportSession;
import atf.api.reporting.html.HtmlReportSession;
import atf.api.reporting.model.ReportExecutionEvidence;
import atf.api.reporting.semantic.AtfReportSemanticSnapshot;
import atf.api.reporting.support.TestReportPaths;
import atf.api.reporting.ui.UiExtentReportSession;
import atf.api.reporting.ui.validation.UiApiValidationEvidence;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static atf.api.reporting.contract.AtfGherkinKeyword.THEN;
import static atf.api.reporting.api.evidence.ReportBodyAvailability.ABSENT;
import static atf.api.reporting.api.evidence.ReportBodyAvailability.AVAILABLE;
import static atf.api.reporting.api.evidence.ReportBodyAvailability.NOT_CAPTURED;

public final class ObservedRequestFixture {
    public enum Renderer { EXTENT, FALLBACK, UI_API }
    public enum Sample {
        OBSERVED_ABSENT("", ABSENT, "{\"response\":true}", AVAILABLE),
        OBSERVED_UNCAPTURED("capture unavailable", NOT_CAPTURED, "capture unavailable", NOT_CAPTURED),
        OBSERVED_NULL("null", AVAILABLE, "null", AVAILABLE),
        OBSERVED_EXPLICIT("{\"value\":\"ok\",\"password\":\"request-secret\"}", AVAILABLE, "{\"response\":true}", AVAILABLE),
        CONFIGURED_DEFAULTS("{}", AVAILABLE, "{}", AVAILABLE),
        EMPTY_RESPONSE("", ABSENT, "", ABSENT);

        public final String body;
        public final ReportBodyAvailability availability;
        final String response;
        final ReportBodyAvailability responseAvailability;

        Sample(String body, ReportBodyAvailability availability, String response,
               ReportBodyAvailability responseAvailability) {
            this.body = body; this.availability = availability;
            this.response = response; this.responseAvailability = responseAvailability;
        }

        public ReportRequest request() {
            Map<String, String> headers = this == OBSERVED_EXPLICIT
                    ? Map.of("Accept", "text/csv", "Content-Type", "application/json; charset=UTF-8",
                            "Authorization", "Bearer header-secret")
                    : Map.of("X-Observed", "yes");
            var headerEvidence = this == CONFIGURED_DEFAULTS
                    ? ReportRequestHeaders.configured(Map.of("Accept", "application/json",
                            "Content-Type", "application/json"), headers)
                    : ReportRequestHeaders.observed(headers);
            return ReportRequest.builder("GET", "/observed/" + name().toLowerCase(java.util.Locale.ROOT))
                    .headers(headerEvidence).body(body, availability).build();
        }

        public ReportExecutionEvidence execution() {
            var request = request();
            var response = new ReportResponse(this == EMPTY_RESPONSE ? 204 : 200, this.response,
                    Map.of("Content-Type", "application/json"), Duration.ofMillis(3),
                    "https://fixture.invalid" + request.path(), responseAvailability);
            return new ReportExecutionEvidence(response, 1, Duration.ofMillis(3), List.of(), List.of(),
                    request, ReportTransportProvenance.ACCEPTANCE_MOCK);
        }
    }

    public record Published(Path path, String html, AtfReportSemanticSnapshot snapshot) { }
    private ObservedRequestFixture() { }

    public static Published publish(Renderer renderer, List<Sample> samples) throws Exception {
        Path requested = TestReportPaths.report("observed-request-" + renderer + "-" + UUID.randomUUID() + ".html");
        Path actual = requested;
        AtfReportSemanticSnapshot snapshot;
        if (renderer == Renderer.UI_API) {
            try (var session = UiExtentReportSession.openConfigured(requested.getParent(),
                    "ui-observed-request-evidence.html", AtfConsoleSink.noOp())) {
                actual = session.reportPath();
                for (Sample sample : samples) {
                    var scenario = session.startScenario("ObservedRequestContract", "audit.component",
                            sample.name(), List.of("component", "controlled-evidence"));
                    var phase = scenario.startPhase("TEST BODY");
                    var execution = sample.execution();
                    int expectedStatus = sample == Sample.EMPTY_RESPONSE ? 204 : 200;
                    var api = new UiApiValidationEvidence(List.of(new AtfAssertion("Captured HTTP status",
                            Integer.toString(expectedStatus), Integer.toString(execution.response().status()),
                            expectedStatus == execution.response().status())), List.of(
                            new UiApiValidationEvidence.Exchange(execution, List.of())));
                    phase.apiTrace(THEN, "the captured request is published without invented evidence",
                            sample.request().path(), api, AtfReportStatus.PASS);
                    phase.complete(AtfReportStatus.PASS);
                    scenario.complete(AtfReportStatus.PASS);
                }
                snapshot = session.semanticSnapshot();
            }
        } else {
            try (AtfReportPorts.Session session = renderer == Renderer.EXTENT
                    ? new ExtentReportSession(requested) : new HtmlReportSession(requested)) {
                for (Sample sample : samples) {
                    var scenario = session.startScenario("ObservedRequestContract", sample.name(), List.of("component"));
                    var phase = scenario.startPhase("TEST BODY");
                    var request = sample.request();
                    try (var node = phase.startRequest("captured request", request.method(), request.path())) {
                        ApiExchangeEvidence.publish(node, sample.execution());
                        node.complete(AtfReportStatus.PASS);
                    }
                    phase.complete(AtfReportStatus.PASS);
                    scenario.complete(AtfReportStatus.PASS);
                }
                snapshot = session instanceof ExtentReportSession extent ? extent.semanticSnapshot()
                        : ((HtmlReportSession) session).semanticSnapshot();
            }
        }
        String html = Files.readString(actual).replaceAll("(?is)<script\\b[^>]*>.*?</script>|<style\\b[^>]*>.*?</style>", "");
        return new Published(actual, html, snapshot);
    }

    public static AtfReportSemanticSnapshot.RequestSnapshot exchange(Published published) {
        var checkpoint = (AtfReportSemanticSnapshot.RequestSnapshot) published.snapshot()
                .classes().getFirst().scenarios().getFirst().phases().getFirst().nodes().getFirst();
        return checkpoint.validations().isEmpty() ? checkpoint : checkpoint.validations().getFirst().requests().getFirst();
    }
}
