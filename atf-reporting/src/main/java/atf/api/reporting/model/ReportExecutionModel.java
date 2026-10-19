package atf.api.reporting.model;

import atf.api.core.http.response.ApiExecutionResult;
import atf.api.core.reporting.contract.AtfAssertion;
import atf.api.core.reporting.contract.AtfReportStatus;

import java.util.List;
import java.util.Objects;

/**
 * Immutable renderer-neutral execution tree for one report.
 *
 * <p>The tree is the single source of truth for the fallback report views.
 * Section projections are derived from the same immutable population, so
 * Overview, Categories and Fails cannot silently disagree with Tests.</p>
 */
public record ReportExecutionModel(List<Scenario> scenarios) {
    public ReportExecutionModel {
        scenarios = List.copyOf(Objects.requireNonNull(scenarios, "scenarios"));
    }

    public Overview overview() {
        return new Overview(
                scenarios.size(),
                scenarios.stream().filter(scenario -> scenario.status() == AtfReportStatus.PASS).count(),
                scenarios.stream().filter(scenario -> scenario.status() == AtfReportStatus.FAIL).count(),
                scenarios.stream().filter(scenario -> scenario.status() == AtfReportStatus.SKIP).count());
    }

    public Categories categories() {
        return new Categories(scenarios.stream()
                .flatMap(scenario -> scenario.tags().stream())
                .distinct()
                .sorted()
                .toList());
    }

    public Failures failures() {
        var failures = new java.util.ArrayList<Failure>();
        for (var scenario : scenarios) {
            for (var phase : scenario.phases()) {
                collectFailures(failures, scenario, phase.children());
            }
        }
        return new Failures(failures);
    }

    private static void collectFailures(List<Failure> failures, Scenario scenario,
                                        List<Node> nodes) {
        for (var node : nodes) {
            if (node instanceof Request request) {
                collectFailures(failures, scenario, request.children());
            } else if (node instanceof Validation validation) {
                for (var assertion : validation.assertions()) {
                    if (assertion.resultStatus() == AtfReportStatus.FAIL
                            || (!assertion.passed()
                            && validation.status() == AtfReportStatus.FAIL)) {
                        failures.add(new Failure(scenario.id(), assertion.name(), assertion.actual()));
                    }
                }
            }
        }
    }

    public record Overview(long tests, long passed, long failed, long skipped) {
        public Overview {
            if (tests < 0 || passed < 0 || failed < 0 || skipped < 0) {
                throw new IllegalArgumentException("Overview counts must be non-negative");
            }
        }
    }

    public record Categories(List<String> tags) {
        public Categories {
            tags = List.copyOf(Objects.requireNonNull(tags, "tags"));
        }
    }

    public record Failures(List<Failure> items) {
        public Failures {
            items = List.copyOf(Objects.requireNonNull(items, "items"));
        }

        public boolean isEmpty() {
            return items.isEmpty();
        }
    }

    public record Failure(String scenarioId, String assertionName, String actual) {
        public Failure {
            scenarioId = required(scenarioId, "scenarioId");
            assertionName = assertionName == null ? "" : assertionName;
            actual = actual == null ? "" : actual;
        }
    }

    public record Scenario(String id, String testClass, String packageName, String scenario,
                           List<String> tags, List<Phase> phases, AtfReportStatus status) {
        public Scenario(String id, String testClass, String scenario,
                        List<String> tags, List<Phase> phases, AtfReportStatus status) {
            this(id, testClass, "", scenario, tags, phases, status);
        }

        public Scenario {
            id = required(id, "id");
            testClass = testClass == null ? "" : testClass;
            packageName = packageName == null ? "" : packageName;
            scenario = scenario == null ? "" : scenario;
            tags = List.copyOf(Objects.requireNonNull(tags, "tags"));
            phases = List.copyOf(Objects.requireNonNull(phases, "phases"));
            status = Objects.requireNonNull(status, "status");
        }
    }

    public record Phase(String name, List<ReportField> details,
                        List<Node> children, AtfReportStatus status) {
        public Phase {
            name = name == null ? "" : name;
            details = List.copyOf(Objects.requireNonNull(details, "details"));
            children = List.copyOf(Objects.requireNonNull(children, "children"));
            status = Objects.requireNonNull(status, "status");
        }
    }

    public sealed interface Node permits Request, Validation {
        AtfReportStatus status();
    }

    public record Request(String title, String method, String path,
                          List<ReportField> details, List<ReportJsonBody> json,
                          ApiExecutionResult execution, List<Node> children,
                          AtfReportStatus status, int uiActionCount, int screenshotCount) implements Node {
        public Request(String title, String method, String path,
                       List<ReportField> details, List<ReportJsonBody> json,
                       ApiExecutionResult execution, List<Node> children,
                       AtfReportStatus status) {
            this(title, method, path, details, json, execution, children, status, 0, 0);
        }

        public Request {
            title = title == null ? "" : title;
            method = method == null ? "" : method;
            path = path == null ? "" : path;
            details = List.copyOf(Objects.requireNonNull(details, "details"));
            json = List.copyOf(Objects.requireNonNull(json, "json"));
            children = List.copyOf(Objects.requireNonNull(children, "children"));
            status = Objects.requireNonNull(status, "status");
            if (uiActionCount < 0 || screenshotCount < 0) {
                throw new IllegalArgumentException("Evidence counts must be non-negative");
            }
        }
    }

    public record Validation(String title, List<AtfAssertion> assertions,
                             AtfReportStatus status, int uiActionCount, int screenshotCount) implements Node {
        public Validation(String title, List<AtfAssertion> assertions, AtfReportStatus status) {
            this(title, assertions, status, 0, 0);
        }

        public Validation {
            title = title == null ? "" : title;
            assertions = List.copyOf(Objects.requireNonNull(assertions, "assertions"));
            status = Objects.requireNonNull(status, "status");
            if (uiActionCount < 0 || screenshotCount < 0) {
                throw new IllegalArgumentException("Evidence counts must be non-negative");
            }
        }
    }

    private static String required(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return value;
    }
}
