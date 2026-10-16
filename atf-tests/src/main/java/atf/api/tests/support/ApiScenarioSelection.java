package atf.api.tests.support;

import java.util.Objects;

/** Immutable resolved identity for one API scenario selected by a UI audit. */
public record ApiScenarioSelection(Class<?> ownerType, String method, String family,
                                   String reportOwner, String scenarioTitle) {
    public ApiScenarioSelection {
        ownerType = Objects.requireNonNull(ownerType, "ownerType");
        method = required(method, "method");
        family = required(family, "family");
        reportOwner = required(reportOwner, "reportOwner");
        scenarioTitle = required(scenarioTitle, "scenarioTitle");
    }

    public String reference() {
        return ownerType.getSimpleName() + "." + method;
    }

    private static String required(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return value.trim();
    }
}
