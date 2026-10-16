package atf.ui.tests.extent.section.validate;

import atf.api.core.reporting.AtfAssertion;
import atf.ui.tests.extent.section.plan.ExtentSection;
import atf.ui.tests.extent.section.plan.ExtentSectionCheck;
import atf.ui.tests.extent.support.ValidationResult;

import java.util.List;
import java.util.Objects;
import java.util.Set;

/** Maps declarative section checks to the assertions owned by a section. */
public final class SectionAuditCheckPolicy {
    private SectionAuditCheckPolicy() { }

    public static ValidationResult select(ExtentSection section,
                                          Set<ExtentSectionCheck> requested,
                                          ValidationResult validation) {
        Objects.requireNonNull(section, "section");
        Objects.requireNonNull(requested, "requested");
        Objects.requireNonNull(validation, "validation");
        List<AtfAssertion> selected = validation.assertions().stream()
                .filter(assertion -> requested.stream().anyMatch(check -> owns(section, check, assertion.name())))
                .toList();
        Set<String> allAssertionNames = validation.assertions().stream()
                .map(AtfAssertion::name).collect(java.util.stream.Collectors.toSet());
        Set<String> selectedNames = selected.stream().map(AtfAssertion::name)
                .collect(java.util.stream.Collectors.toSet());
        List<String> selectedViolations = validation.violations().stream()
                .filter(violation -> allAssertionNames.stream().noneMatch(name -> violation.startsWith(name + ":"))
                        || selectedNames.stream().anyMatch(name -> violation.startsWith(name + ":")))
                .toList();
        // A raw violation has no assertion name; never hide it behind annotation selection.
        return new ValidationResult(selected.stream().allMatch(AtfAssertion::passed)
                && selectedViolations.isEmpty(), selected, selectedViolations);
    }

    private static boolean owns(ExtentSection section, ExtentSectionCheck check, String name) {
        return switch (section) {
            case OVERVIEW -> check == (name.equals("Expected report projection")
                    ? ExtentSectionCheck.CROSS_SECTION : ExtentSectionCheck.STRUCTURE);
            case TESTS -> check == ExtentSectionCheck.STRUCTURE
                    || (check == ExtentSectionCheck.CROSS_SECTION && name.contains("projection"));
            case CATEGORIES -> (name.equals("Category surface")
                    ? check == ExtentSectionCheck.STYLE : check == ExtentSectionCheck.STRUCTURE);
            case FAILS -> (name.equals("Collapsed details")
                    ? check == ExtentSectionCheck.COLLAPSED_BY_DEFAULT
                    : (name.contains("deep-link") || name.contains("failure metric"))
                    ? check == ExtentSectionCheck.CROSS_SECTION
                    : check == ExtentSectionCheck.STRUCTURE);
            case EVIDENCE -> (name.equals("Request JSON") || name.equals("Response JSON")
                    || name.equals("JSON violations"))
                    ? (check == ExtentSectionCheck.JSON_CONTRACT
                    || check == ExtentSectionCheck.MOCK_RECONCILIATION)
                    : check == ExtentSectionCheck.STRUCTURE;
            case SCREEN_VALIDATION -> (name.equals("Inline screenshot")
                    ? check == ExtentSectionCheck.SCREENSHOT_LINK
                    : (name.equals("Sibling rows") || name.equals("Vertical composition"))
                    ? check == ExtentSectionCheck.INLINE_ALIGNMENT
                    : check == ExtentSectionCheck.STRUCTURE);
        };
    }
}
