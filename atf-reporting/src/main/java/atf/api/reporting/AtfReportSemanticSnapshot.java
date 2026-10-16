package atf.api.reporting;

import atf.api.core.reporting.AtfReportStatus;

import java.util.List;
import java.util.Objects;

/**
 * Immutable semantic population shared by every ATF report renderer.
 *
 * <p>The snapshot contains report facts only. It deliberately has no HTML,
 * CSS, Extent or browser types, so a renderer can be compared with another
 * renderer without scraping its output.</p>
 */
public record AtfReportSemanticSnapshot(List<ClassSnapshot> classes) {
    public AtfReportSemanticSnapshot {
        classes = List.copyOf(Objects.requireNonNull(classes, "classes"));
    }

    public int scenarioCount() {
        return classes.stream().mapToInt(value -> value.scenarios().size()).sum();
    }

    public int phaseCount() {
        return classes.stream().flatMap(value -> value.scenarios().stream())
                .mapToInt(value -> value.phases().size()).sum();
    }

    public int requestCount() {
        return classes.stream().flatMap(value -> value.scenarios().stream())
                .flatMap(value -> value.phases().stream())
                .mapToInt(value -> (int) value.nodes().stream()
                        .filter(node -> node instanceof RequestSnapshot).count()).sum();
    }

    public int validationCount() {
        return classes.stream().flatMap(value -> value.scenarios().stream())
                .flatMap(value -> value.phases().stream())
                .flatMap(value -> value.nodes().stream())
                .filter(node -> node instanceof RequestSnapshot)
                .mapToInt(node -> ((RequestSnapshot) node).validations().size()).sum();
    }

    public record ClassSnapshot(String name, String packageName,
                                List<ScenarioSnapshot> scenarios,
                                AtfReportStatus status) {
        public ClassSnapshot {
            name = required(name, "name");
            packageName = packageName == null ? "" : packageName;
            scenarios = List.copyOf(Objects.requireNonNull(scenarios, "scenarios"));
            status = Objects.requireNonNull(status, "status");
        }
    }

    public record ScenarioSnapshot(String id, String name, List<String> tags,
                                   List<PhaseSnapshot> phases, AtfReportStatus status) {
        public ScenarioSnapshot {
            id = required(id, "id");
            name = name == null ? "" : name;
            tags = List.copyOf(Objects.requireNonNull(tags, "tags"));
            phases = List.copyOf(Objects.requireNonNull(phases, "phases"));
            status = Objects.requireNonNull(status, "status");
        }
    }

    public record PhaseSnapshot(String name, List<NodeSnapshot> nodes,
                                List<String> detailNames, AtfReportStatus status) {
        public PhaseSnapshot {
            name = name == null ? "" : name;
            nodes = List.copyOf(Objects.requireNonNull(nodes, "nodes"));
            detailNames = List.copyOf(Objects.requireNonNull(detailNames, "detailNames"));
            status = Objects.requireNonNull(status, "status");
        }
    }

    public sealed interface NodeSnapshot permits RequestSnapshot, ValidationSnapshot {
        String title();
        AtfReportStatus status();
    }

    public record RequestSnapshot(String title, String method, String path,
                                  List<String> detailNames, List<String> jsonLabels,
                                  boolean executionPresent, int uiActionCount,
                                  int screenshotCount, List<ValidationSnapshot> validations,
                                  AtfReportStatus status) implements NodeSnapshot {
        public RequestSnapshot {
            title = title == null ? "" : title;
            method = method == null ? "" : method;
            path = path == null ? "" : path;
            detailNames = List.copyOf(Objects.requireNonNull(detailNames, "detailNames"));
            jsonLabels = List.copyOf(Objects.requireNonNull(jsonLabels, "jsonLabels"));
            if (uiActionCount < 0 || screenshotCount < 0) {
                throw new IllegalArgumentException("Evidence counts must be non-negative");
            }
            validations = List.copyOf(Objects.requireNonNull(validations, "validations"));
            status = Objects.requireNonNull(status, "status");
        }
    }

    public record ValidationSnapshot(String title, List<String> assertionNames,
                                     int uiActionCount, int screenshotCount,
                                     AtfReportStatus status) implements NodeSnapshot {
        public ValidationSnapshot {
            title = title == null ? "" : title;
            assertionNames = List.copyOf(Objects.requireNonNull(assertionNames, "assertionNames"));
            if (uiActionCount < 0 || screenshotCount < 0) {
                throw new IllegalArgumentException("Evidence counts must be non-negative");
            }
            status = Objects.requireNonNull(status, "status");
        }
    }

    private static String required(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return value;
    }
}
