package atf.api.reporting.model;

import java.util.Objects;

/** Immutable JSON evidence item; the body is kept as authored for rendering. */
public record ReportJsonBody(String label, String body) {
    public ReportJsonBody {
        label = requireText(label, "label");
        body = Objects.requireNonNullElse(body, "");
    }

    public boolean isPresent() {
        return !body.isBlank();
    }

    private static String requireText(String value, String field) {
        if (Objects.requireNonNull(value, field).isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return value;
    }
}
