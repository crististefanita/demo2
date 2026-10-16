package atf.ui.tests.extent.plan;

import atf.api.ui.config.UiConfiguration;
import atf.api.ui.config.UiAuditSuiteConfiguration;
import atf.ui.tests.extent.flows.ApiReportSetupFlow;
import atf.ui.tests.extent.support.UiTestData;

import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/** Immutable, configurable input for one generated-report audit suite. */
public record UiReportAuditSpec(String id, List<String> apiFamilies,
                                Set<UiReportAuditRule> rules, List<String> tags,
                                String setupDescription, String failureTitle,
                                String failureTarget) {
    public static final String SUITE_PROPERTY = "atf.ui.audit.suite";
    public static final String RULES_PROPERTY = "atf.ui.audit.rules";
    public static final String DEPTH_PROPERTY = "atf.ui.audit.depth";

    private static final Set<UiReportAuditRule> DEEP_RULES =
            Set.copyOf(EnumSet.allOf(UiReportAuditRule.class));
    private static final Set<UiReportAuditRule> SMOKE_RULES = Set.of(
            UiReportAuditRule.SETUP, UiReportAuditRule.TESTS,
            UiReportAuditRule.REQUEST_CONTRACT, UiReportAuditRule.OVERVIEW);

    public UiReportAuditSpec {
        id = required(id, "id");
        apiFamilies = normalizeFamilies(apiFamilies);
        if (apiFamilies.isEmpty()) throw new IllegalArgumentException("apiFamilies must not be empty");
        rules = Set.copyOf(Objects.requireNonNull(rules, "rules"));
        if (rules.isEmpty()) throw new IllegalArgumentException("rules must not be empty");
        tags = List.copyOf(Objects.requireNonNull(tags, "tags"));
        setupDescription = required(setupDescription, "setupDescription");
        failureTitle = required(failureTitle, "failureTitle");
        failureTarget = required(failureTarget, "failureTarget");
    }

    /** Resolves the common JVM/YAML/annotation family selection once. */
    public static UiReportAuditSpec resolve(Class<?> owner, UiConfiguration configuration) {
        Objects.requireNonNull(owner, "owner");
        Objects.requireNonNull(configuration, "configuration");
        List<String> families = ApiReportSetupFlow.families(owner, configuration);
        String requestedId = System.getProperty(SUITE_PROPERTY);
        String id = requestedId == null || requestedId.isBlank()
                ? owner.getSimpleName().replaceAll("(?<!^)([A-Z])", "-$1").toLowerCase(Locale.ROOT)
                : requestedId.trim();
        UiAuditSuiteConfiguration suite = configuration.auditSuite(requestedId).orElse(null);
        Set<UiReportAuditRule> rules = resolveRules(owner, suite);
        List<String> tags = new java.util.ArrayList<>(UiTestData.defaultTags());
        tags.addAll(families);
        return new UiReportAuditSpec(id, families, rules, new LinkedHashSet<>(tags).stream().toList(),
                "the " + String.join(" and ", families)
                        + " API setup report is loaded read-only",
                "UI Extent audit failed", "generated API report");
    }

    public static UiReportAuditSpec legacy(String id, Collection<String> families,
                                           Collection<String> tags,
                                           String setupDescription, String failureTitle,
                                           String failureTarget) {
        List<String> resolvedTags = tags == null ? UiTestData.defaultTags() : List.copyOf(tags);
        return new UiReportAuditSpec(id, normalizeFamilies(families), DEEP_RULES, resolvedTags,
                setupDescription, failureTitle, failureTarget);
    }

    public boolean includes(UiReportAuditRule rule) {
        return rules.contains(Objects.requireNonNull(rule, "rule"));
    }

    private static Set<UiReportAuditRule> resolveRules(Class<?> owner,
                                                       UiAuditSuiteConfiguration suite) {
        UiExtentAudit annotation = owner.getAnnotation(UiExtentAudit.class);
        if (annotation != null && annotation.rules().length > 0) {
            return Set.of(annotation.rules());
        }
        String raw = System.getProperty(RULES_PROPERTY);
        if (raw != null && !raw.isBlank()) {
            return Arrays.stream(raw.split(","))
                    .map(UiReportAuditRule::parse)
                    .collect(Collectors.toCollection(() -> EnumSet.noneOf(UiReportAuditRule.class)));
        }
        String configuredDepth = System.getProperty(DEPTH_PROPERTY);
        if (configuredDepth != null && !configuredDepth.isBlank()) {
            return rulesForDepth(configuredDepth);
        }
        if (suite != null && !suite.validators().isEmpty()) {
            return suite.validators().stream().map(UiReportAuditRule::parse)
                    .collect(Collectors.toCollection(() -> EnumSet.noneOf(UiReportAuditRule.class)));
        }
        return rulesForDepth(suite == null ? "deep" : suite.depth());
    }

    private static Set<UiReportAuditRule> rulesForDepth(String configuredDepth) {
        String depth = configuredDepth.trim().toLowerCase(Locale.ROOT);
        return switch (depth) {
            case "smoke", "fast" -> SMOKE_RULES;
            case "deep", "full" -> DEEP_RULES;
            default -> throw new IllegalArgumentException("Unknown UI audit depth: " + depth);
        };
    }

    private static List<String> normalizeFamilies(Collection<String> values) {
        if (values == null) return List.of();
        return values.stream().filter(Objects::nonNull)
                .map(value -> value.trim().toLowerCase(Locale.ROOT))
                .filter(value -> !value.isBlank()).distinct().toList();
    }

    private static String required(String value, String field) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(field + " must not be blank");
        return value.trim();
    }
}
