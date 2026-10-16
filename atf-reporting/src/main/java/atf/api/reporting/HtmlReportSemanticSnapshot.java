package atf.api.reporting;

import atf.api.core.http.ApiExecutionResult;
import atf.api.core.reporting.AtfAssertion;
import atf.api.core.reporting.AtfReportStatus;

import java.util.List;

/** Immutable renderer-neutral projection captured once before fallback views render. */
final class HtmlReportSemanticSnapshot {
    private final List<Scenario> scenarios;

    HtmlReportSemanticSnapshot(List<Scenario> scenarios) {
        this.scenarios = List.copyOf(scenarios);
    }

    List<Scenario> scenarios() { return scenarios; }

    record Scenario(String id, String testClass, String scenario, List<String> tags,
                    List<Phase> phases, AtfReportStatus status) {
        Scenario {
            tags = List.copyOf(tags);
            phases = List.copyOf(phases);
        }
    }

    record Phase(String name, List<Detail> details, List<Node> children, AtfReportStatus status) {
        Phase {
            details = List.copyOf(details);
            children = List.copyOf(children);
        }
    }

    sealed interface Node permits Request, Validation {
        AtfReportStatus status();
    }

    record Request(String title, String method, String path, List<Detail> details,
                   List<Json> json, ApiExecutionResult execution, List<Node> children,
                   AtfReportStatus status) implements Node {
        Request {
            details = List.copyOf(details);
            json = List.copyOf(json);
            children = List.copyOf(children);
        }
    }

    record Validation(String title, List<AtfAssertion> assertions,
                      AtfReportStatus status) implements Node {
        Validation { assertions = List.copyOf(assertions); }
    }

    record Detail(String name, String value) { }
    record Json(String label, String value) { }
}
