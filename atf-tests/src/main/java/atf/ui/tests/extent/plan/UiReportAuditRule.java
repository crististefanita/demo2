package atf.ui.tests.extent.plan;

import java.util.Locale;
import java.util.EnumSet;
import java.util.Set;
import atf.ui.tests.extent.section.plan.ExtentSection;

/**
 * Closed vocabulary for one reusable Extent-report UI audit rule.
 *
 * <p>The enum is deliberately independent of a concrete test class. A suite
 * selects rules as data and the dynamic factory composes the executable
 * nodes.</p>
 */
@Deprecated(forRemoval = false)
public enum UiReportAuditRule {
    SETUP("SETUP / input and manifest"),
    TESTS("TESTS / dynamic structure"),
    TESTS_DEEP("TESTS / selected scenarios and element evidence"),
    REQUEST_CONTRACT("TESTS / request-response JSON and mock contract"),
    OVERVIEW("OVERVIEW / totals and metadata"),
    OVERVIEW_DEEP("OVERVIEW / selected projection and display contract"),
    CATEGORIES("CATEGORIES / dynamic annotations and links"),
    FAILS("FAILS / empty-state or failure navigation"),
    FAILURE_FLOW("FAILS / failure navigation and collapsed-state contract"),
    FAILURE_DETAILS("TESTS / failed phase details and collapsed technical trace"),
    DURATIONS("TESTS / dynamic duration contract"),
    RESPONSIVE("TESTS / responsive badge geometry"),
    STYLE("ALL TABS / CSS surface and layout contract"),
    EVIDENCE("TESTS / request-response-execution evidence"),
    CROSS_TAB("CROSS-TAB / Tests to Overview, Categories and Fails");

    private final String displayName;

    UiReportAuditRule(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }

    /**
     * Explicit compatibility alias to the canonical section registry. The
     * legacy rule vocabulary remains readable for historical YAML/tests, but
     * new suites should declare {@link atf.ui.tests.extent.section.plan.UiExtentSectionAudit}.
     */
    public Set<ExtentSection> canonicalSections() {
        EnumSet<ExtentSection> sections = EnumSet.noneOf(ExtentSection.class);
        switch (this) {
            case OVERVIEW, OVERVIEW_DEEP -> sections.add(ExtentSection.OVERVIEW);
            case TESTS, TESTS_DEEP, REQUEST_CONTRACT, DURATIONS, RESPONSIVE ->
                    sections.add(ExtentSection.TESTS);
            case CATEGORIES -> sections.add(ExtentSection.CATEGORIES);
            case FAILS, FAILURE_FLOW -> sections.add(ExtentSection.FAILS);
            case EVIDENCE -> sections.add(ExtentSection.EVIDENCE);
            case STYLE -> sections.addAll(EnumSet.allOf(ExtentSection.class));
            case CROSS_TAB -> sections.addAll(EnumSet.of(ExtentSection.OVERVIEW,
                    ExtentSection.TESTS, ExtentSection.CATEGORIES, ExtentSection.FAILS));
            case SETUP, FAILURE_DETAILS -> { }
        }
        return Set.copyOf(sections);
    }

    public boolean isCanonicalAlias() {
        return !canonicalSections().isEmpty();
    }

    public static UiReportAuditRule parse(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("UI report audit rule must not be blank");
        }
        String normalized = value.trim().replace('-', '_').replace(' ', '_')
                .toUpperCase(Locale.ROOT);
        if ("DURATION".equals(normalized)) normalized = "DURATIONS";
        try {
            return valueOf(normalized);
        } catch (IllegalArgumentException error) {
            throw new IllegalArgumentException("Unknown UI report audit rule: " + value,
                    error);
        }
    }
}
