package atf.api.ui.config;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

/** Declarative, immutable input for one reusable UI audit suite. */
public record UiAuditSuiteConfiguration(String id, List<String> scenarioFamilies,
                                        List<String> tabs, List<String> validators,
                                        String depth, String source) {
    public UiAuditSuiteConfiguration {
        id = required(id, "id");
        scenarioFamilies = normalizedList(scenarioFamilies, "scenarioFamilies");
        if (scenarioFamilies.isEmpty()) {
            throw new IllegalArgumentException("scenarioFamilies must not be empty for " + id);
        }
        tabs = normalizedList(tabs, "tabs");
        validators = normalizedList(validators, "validators");
        depth = required(depth, "depth").toLowerCase(Locale.ROOT);
        source = required(source, "source");
    }

    private static List<String> normalizedList(List<String> values, String field) {
        Objects.requireNonNull(values, field);
        if (values.stream().anyMatch(value -> value == null || value.isBlank())) {
            throw new IllegalArgumentException(field + " must not contain blank values");
        }
        return values.stream().map(value -> value.trim().toLowerCase(Locale.ROOT)).distinct().toList();
    }

    private static String required(String value, String field) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(field + " must not be blank");
        return value.trim();
    }
}
