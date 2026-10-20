package atf.ui.tests.extent.support.audit.screen;

import atf.api.reporting.audit.ReportAuditChecks;
import atf.api.reporting.audit.ValidationResult;
import atf.api.reporting.contract.AtfAssertion;
import atf.ui.tests.extent.support.locators.ReportUiLocators;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Reconciles the two secondary Overview disclosures and the package projection
 * with the package labels rendered under the Tests-tab owner names.
 *
 * <p>The browser adapter owns only DOM extraction. The equality rules are
 * exposed as small pure methods so component tests can protect the contract
 * without starting a browser or adding another E2E test.</p>
 */
public final class OverviewSecondaryContractValidator {
    private static final int REQUIRED_SECONDARY_ZONE_COUNT = 2;
    private final ReportUiLocators locators;

    public OverviewSecondaryContractValidator() {
        this(new ReportUiLocators());
    }

    public OverviewSecondaryContractValidator(ReportUiLocators locators) {
        this.locators = Objects.requireNonNull(locators, "locators");
    }

    public ValidationResult validate(Page testsPage, Page overviewPage) {
        Objects.requireNonNull(testsPage, "testsPage");
        Objects.requireNonNull(overviewPage, "overviewPage");
        Locator overview = locators.overviewView(overviewPage);
        int zones = locators.overviewSecondaryZones(overview).count();
        int openZones = locators.overviewOpenSecondaryZones(overview).count();
        Set<String> testPackages = textValues(locators.testPackageNodes(testsPage));
        Set<String> overviewPackages = packageAuditNames(overview);

        List<AtfAssertion> assertions = List.of(
                ReportAuditChecks.check("ATF secondary Overview zones", "2 collapsed disclosures",
                        zones + " found / " + openZones + " open",
                        collapsedSecondaryZones(zones, openZones)),
                ReportAuditChecks.check("Test packages", testPackages.toString(),
                        overviewPackages.toString(),
                        packageProjectionMatches(testPackages, overviewPackages)));
        return ValidationResult.of(assertions);
    }

    public static boolean collapsedSecondaryZones(int discovered, int open) {
        return discovered == REQUIRED_SECONDARY_ZONE_COUNT && open == 0;
    }

    public static boolean packageProjectionMatches(Collection<String> tests,
                                                   Collection<String> overview) {
        return normalizedValues(tests).equals(normalizedValues(overview));
    }

    private Set<String> packageAuditNames(Locator overview) {
        LinkedHashSet<String> names = new LinkedHashSet<>();
        Locator rows = locators.overviewPackageRows(overview);
        for (int index = 0; index < rows.count(); index++) {
            Locator cells = locators.tableCells(rows.nth(index));
            if (cells.count() > 0) addValue(names, cells.nth(0).textContent());
        }
        return names;
    }

    private static Set<String> textValues(Locator nodes) {
        LinkedHashSet<String> values = new LinkedHashSet<>();
        for (int index = 0; index < nodes.count(); index++) {
            addValue(values, nodes.nth(index).textContent());
        }
        return values;
    }

    private static void addValue(Set<String> values, String raw) {
        if (raw != null && !raw.isBlank()) values.add(raw.trim());
    }

    private static Set<String> normalizedValues(Collection<String> values) {
        LinkedHashSet<String> normalized = new LinkedHashSet<>();
        if (values != null) {
            values.stream().filter(Objects::nonNull).map(String::trim)
                    .filter(value -> !value.isBlank()).forEach(normalized::add);
        }
        return normalized;
    }
}
