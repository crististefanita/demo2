package atf.api.reporting.model.diagnostic;

import atf.api.reporting.model.ReportField;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/** Explicit execution diagnostics, distinct from internal phase bookkeeping and HTTP contracts. */
public record ReportPhaseDiagnostic(String phase, Kind kind, String message) {
    public ReportPhaseDiagnostic {
        phase = phase == null ? "" : phase;
        Objects.requireNonNull(kind, "kind");
        message = message == null ? "" : message;
    }

    /** Delimited compatibility seam for phase details emitted by existing lifecycle adapters. */
    public static List<ReportPhaseDiagnostic> from(String phase, List<ReportField> details) {
        return details.stream().filter(Objects::nonNull)
                .flatMap(field -> Kind.forLabel(field.name()).stream()
                        .map(kind -> new ReportPhaseDiagnostic(phase, kind, field.value())))
                .toList();
    }

    public enum Kind {
        EXECUTION_FAILURE("Failure cause"),
        JUNIT_FAILURE("JUnit failure"),
        CONTRACT_ASSOCIATION("API contract association");

        private final String label;

        Kind(String label) { this.label = label; }

        public String label() { return label; }

        private static Optional<Kind> forLabel(String label) {
            String normalized = label == null ? "" : label.trim();
            return Arrays.stream(values()).filter(kind -> kind.label.equalsIgnoreCase(normalized)).findFirst();
        }
    }
}
