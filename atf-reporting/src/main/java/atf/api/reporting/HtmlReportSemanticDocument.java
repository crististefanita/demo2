package atf.api.reporting;

import atf.api.core.http.ApiExecutionResult;
import atf.api.core.reporting.AtfAssertion;
import atf.api.core.reporting.AtfReportStatus;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Mutable semantic document built by the reporting lifecycle.
 *
 * <p>The document owns ordering and identity only. It deliberately does not
 * know about HTML, CSS, JavaScript or filesystem publication. This keeps the
 * lifecycle facade free to coordinate scopes while renderers consume a stable
 * suite/class/scenario/phase/request/validation model.</p>
 */
final class HtmlReportSemanticDocument {
    private final List<ScenarioModel> scenarios = new ArrayList<>();
    private final AtomicInteger nextId = new AtomicInteger();

    ScenarioModel startScenario(String testClass, String scenario, Collection<String> tags) {
        var model = new ScenarioModel(
                "test-id=" + nextId.incrementAndGet(),
                testClass,
                scenario,
                tags);
        scenarios.add(model);
        return model;
    }

    List<ScenarioModel> scenarios() {
        return List.copyOf(scenarios);
    }

    interface NodeModel {
        AtfReportStatus status();
    }

    static final class ScenarioModel {
        final String id;
        final String testClass;
        final String scenario;
        final List<String> tags;
        final List<PhaseModel> phases = new ArrayList<>();
        AtfReportStatus status = AtfReportStatus.INFO;

        ScenarioModel(String id, String testClass, String scenario, Collection<String> tags) {
            this.id = id;
            this.testClass = testClass == null ? "" : testClass;
            this.scenario = scenario == null ? "" : scenario;
            this.tags = tags == null
                    ? List.of()
                    : tags.stream().filter(Objects::nonNull).distinct().toList();
        }

        AtfReportStatus status() {
            return status;
        }
    }

    static final class PhaseModel {
        final String name;
        final List<String[]> details = new ArrayList<>();
        final List<NodeModel> children = new ArrayList<>();
        AtfReportStatus status = AtfReportStatus.INFO;

        PhaseModel(String name) {
            this.name = name == null ? "" : name;
        }
    }

    static final class RequestModel implements NodeModel {
        final String title;
        final String method;
        final String path;
        final List<String[]> details = new ArrayList<>();
        final List<String[]> json = new ArrayList<>();
        final List<NodeModel> children = new ArrayList<>();
        ApiExecutionResult execution;
        AtfReportStatus status = AtfReportStatus.INFO;

        RequestModel(String title, String method, String path) {
            this.title = title;
            this.method = method;
            this.path = path;
        }

        @Override
        public AtfReportStatus status() {
            return status;
        }
    }

    static final class ValidationModel implements NodeModel {
        final String title;
        List<AtfAssertion> assertions = List.of();
        AtfReportStatus status = AtfReportStatus.INFO;

        ValidationModel(String title) {
            this.title = title;
        }

        @Override
        public AtfReportStatus status() {
            return status;
        }
    }
}
