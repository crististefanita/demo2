package atf.api.reporting.semantic;
import atf.api.reporting.contract.AtfAssertion;
import atf.api.reporting.contract.AtfReportStatus;

import atf.api.reporting.ui.AtfUiActionRecord;
import atf.api.reporting.model.ReportTagNormalizer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Mutable lifecycle-side recorder that emits one immutable semantic snapshot. */
public final class AtfReportSemanticRecorder {
    private final Map<String, ClassBuilder> classes = new LinkedHashMap<>();
    private int nextScenarioId;

    public ScenarioHandle startScenario(String testClass, String testPackage,
                                        String scenario, Collection<String> tags) {
        String className = testClass == null || testClass.isBlank() ? "API tests" : testClass;
        ClassBuilder owner = classes.computeIfAbsent(className,
                key -> new ClassBuilder(key, testPackage));
        String id = "test-id=" + ++nextScenarioId;
        ScenarioBuilder builder = new ScenarioBuilder(id, scenario, tags);
        owner.scenarios.add(builder);
        return new ScenarioHandle(builder);
    }

    public AtfReportSemanticSnapshot snapshot() {
        return new AtfReportSemanticSnapshot(classes.values().stream()
                .map(ClassBuilder::snapshot).toList());
    }

    public final class ScenarioHandle {
        private final ScenarioBuilder scenario;

        private ScenarioHandle(ScenarioBuilder scenario) {
            this.scenario = scenario;
        }

        public String id() { return scenario.id; }

        public PhaseHandle startPhase(String name) {
            PhaseBuilder phase = new PhaseBuilder(name);
            scenario.phases.add(phase);
            return new PhaseHandle(phase);
        }

        public void complete(AtfReportStatus status) {
            scenario.status = Objects.requireNonNull(status, "status");
        }
    }

    public final class PhaseHandle {
        private final PhaseBuilder phase;

        private PhaseHandle(PhaseBuilder phase) {
            this.phase = phase;
        }

        public RequestHandle startRequest(String title, String method, String path) {
            RequestBuilder request = new RequestBuilder(title, method, path);
            phase.nodes.add(request);
            return new RequestHandle(request);
        }

        public void detail(String name) {
            phase.detailNames.add(name == null || name.isBlank() ? "DETAIL" : name);
        }

        public void complete(AtfReportStatus status) {
            phase.status = Objects.requireNonNull(status, "status");
        }
    }

    public final class RequestHandle {
        private final RequestBuilder request;

        private RequestHandle(RequestBuilder request) {
            this.request = request;
        }

        public void detail(String name) {
            request.detailNames.add(name == null || name.isBlank() ? "DETAIL" : name);
        }

        public void json(String label) {
            request.jsonLabels.add(label == null || label.isBlank() ? "JSON" : label);
        }

        public void execution() { request.executionPresent = true; }

        public void uiActions(Collection<AtfUiActionRecord> actions) {
            request.uiActionCount += actions == null ? 0 : actions.size();
        }

        public void screenshot() { request.screenshotCount++; }

        public ValidationHandle startValidation(String title) {
            ValidationBuilder validation = new ValidationBuilder(title);
            request.validations.add(validation);
            return new ValidationHandle(validation);
        }

        public void complete(AtfReportStatus status) {
            request.status = Objects.requireNonNull(status, "status");
        }
    }

    public final class ValidationHandle {
        private final ValidationBuilder validation;

        private ValidationHandle(ValidationBuilder validation) {
            this.validation = validation;
        }

        public void assertions(Collection<AtfAssertion> assertions) {
            if (assertions != null) {
                validation.assertionNames.addAll(assertions.stream()
                        .filter(Objects::nonNull).map(AtfAssertion::name).toList());
            }
        }

        public void uiActions(Collection<AtfUiActionRecord> actions) {
            validation.uiActionCount += actions == null ? 0 : actions.size();
        }

        public void screenshot() { validation.screenshotCount++; }

        public void complete(AtfReportStatus status) {
            validation.status = Objects.requireNonNull(status, "status");
        }
    }

    private static final class ClassBuilder {
        private final String name;
        private final String packageName;
        private final List<ScenarioBuilder> scenarios = new ArrayList<>();
        private AtfReportStatus status = AtfReportStatus.INFO;

        private ClassBuilder(String name, String packageName) {
            this.name = name;
            this.packageName = packageName == null ? "" : packageName;
        }

        private AtfReportSemanticSnapshot.ClassSnapshot snapshot() {
            AtfReportStatus aggregate = status;
            for (ScenarioBuilder scenario : scenarios) {
                aggregate = AtfReportStatus.worst(aggregate, scenario.status);
            }
            return new AtfReportSemanticSnapshot.ClassSnapshot(name, packageName,
                    scenarios.stream().map(ScenarioBuilder::snapshot).toList(), aggregate);
        }
    }

    private static final class ScenarioBuilder {
        private final String id;
        private final String name;
        private final List<String> tags;
        private final List<PhaseBuilder> phases = new ArrayList<>();
        private AtfReportStatus status = AtfReportStatus.INFO;

        private ScenarioBuilder(String id, String name, Collection<String> tags) {
            this.id = id;
            this.name = name == null ? "" : name;
            this.tags = ReportTagNormalizer.normalize(tags);
        }

        private AtfReportSemanticSnapshot.ScenarioSnapshot snapshot() {
            return new AtfReportSemanticSnapshot.ScenarioSnapshot(id, name, tags,
                    phases.stream().map(PhaseBuilder::snapshot).toList(), status);
        }
    }

    private static final class PhaseBuilder {
        private final String name;
        private final List<NodeBuilder> nodes = new ArrayList<>();
        private final List<String> detailNames = new ArrayList<>();
        private AtfReportStatus status = AtfReportStatus.INFO;

        private PhaseBuilder(String name) { this.name = name == null ? "" : name; }

        private AtfReportSemanticSnapshot.PhaseSnapshot snapshot() {
            return new AtfReportSemanticSnapshot.PhaseSnapshot(name,
                    nodes.stream().map(NodeBuilder::snapshot).toList(), detailNames, status);
        }
    }

    private sealed interface NodeBuilder permits RequestBuilder, ValidationBuilder {
        AtfReportSemanticSnapshot.NodeSnapshot snapshot();
    }

    private static final class RequestBuilder implements NodeBuilder {
        private final String title;
        private final String method;
        private final String path;
        private final List<String> detailNames = new ArrayList<>();
        private final List<String> jsonLabels = new ArrayList<>();
        private final List<ValidationBuilder> validations = new ArrayList<>();
        private boolean executionPresent;
        private int uiActionCount;
        private int screenshotCount;
        private AtfReportStatus status = AtfReportStatus.INFO;

        private RequestBuilder(String title, String method, String path) {
            this.title = title == null ? "" : title;
            this.method = method == null ? "" : method;
            this.path = path == null ? "" : path;
        }

        @Override public AtfReportSemanticSnapshot.RequestSnapshot snapshot() {
            return new AtfReportSemanticSnapshot.RequestSnapshot(title, method, path,
                    detailNames, jsonLabels, executionPresent, uiActionCount,
                    screenshotCount, validations.stream().map(ValidationBuilder::snapshot).toList(), status);
        }
    }

    private static final class ValidationBuilder implements NodeBuilder {
        private final String title;
        private final List<String> assertionNames = new ArrayList<>();
        private int uiActionCount;
        private int screenshotCount;
        private AtfReportStatus status = AtfReportStatus.INFO;

        private ValidationBuilder(String title) { this.title = title == null ? "" : title; }

        @Override public AtfReportSemanticSnapshot.ValidationSnapshot snapshot() {
            return new AtfReportSemanticSnapshot.ValidationSnapshot(title, assertionNames,
                    uiActionCount, screenshotCount, status);
        }
    }
}
