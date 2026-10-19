package atf.ui.tests.extent.support.section.runtime;

import atf.api.core.reporting.contract.AtfAssertion;
import atf.api.reporting.audit.ReportAuditChecks;
import atf.api.reporting.audit.ValidationResult;
import atf.api.reporting.ui.contract.ReportUiContract;
import atf.ui.tests.extent.support.section.module.SectionAuditRegistry;
import atf.api.reporting.ui.audit.plan.ExtentSectionGherkin;
import atf.ui.tests.extent.support.section.publish.ExtentSectionAuditPublisher;
import atf.ui.tests.extent.support.locators.ReportUiLocators;
import atf.ui.tests.extent.support.uireport.browser.UiReportStyleValidator;
import com.microsoft.playwright.Page;
import atf.ui.tests.extent.support.section.runtime.SectionAuditFixture;
import org.junit.jupiter.api.DynamicContainer;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.DynamicTest;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static atf.api.reporting.audit.ReportAuditChecks.status;

/**
 * JUnit adapter for section audits; the audit plan and modules remain
 * renderer-neutral production-side components.
 *
 * <p>The class deliberately lives in test support rather than beside the
 * section model. It owns only dynamic-node construction and JUnit assertions;
 * it is not itself an executable test.</p>
 */
public final class SectionAuditDynamicEngine {
    private static final ExtentSectionAuditPublisher PUBLISHER = new ExtentSectionAuditPublisher();

    private SectionAuditDynamicEngine() { }

    public static DynamicNode container(SectionAuditFixture fixture) {
        SectionAuditRegistry registry = new SectionAuditRegistry();
        fixture.spec().validatePlan();
        List<DynamicNode> nodes = new ArrayList<>();
        nodes.add(renderHealth(fixture));
        nodes.addAll(fixture.spec().sections().stream()
                        .map(section -> DynamicTest.dynamicTest(ExtentSectionGherkin.proposition(section),
                                () -> {
                                    var result = PUBLISHER.publish(fixture, fixture.spec(),
                                            registry.module(section));
                                    assertTrue(result.valid(), result.validation()::failureSummary);
                                }))
                        .toList());
        return DynamicContainer.dynamicContainer(fixture.spec().id(), nodes.stream());
    }

    /**
     * Every generated API report gets the same browser boundary as a static
     * report.  Section modules validate semantics; this gate validates the
     * actual rendered surface and lifecycle diagnostics once, without making
     * every section duplicate the same CSS/console checks.
     */
    private static DynamicTest renderHealth(SectionAuditFixture fixture) {
        return DynamicTest.dynamicTest("GIVEN generated API report WHEN every tab is opened "
                        + "THEN render health and browser diagnostics are valid",
                () -> fixture.audit().auditResult("Generated report render health", audit -> {
                    ReportUiLocators locators = new ReportUiLocators();
                    UiReportStyleValidator validator = new UiReportStyleValidator(locators);
                    List<AtfAssertion> assertions = new ArrayList<>();
                    List<String> violations = new ArrayList<>();
                    Page lastPage = null;
                    for (ReportUiContract.Tab tab : ReportUiContract.Tab.values()) {
                        lastPage = fixture.audit().openTab(tab.hash());
                        UiReportStyleValidator.Result result = validator.inspect(lastPage, tab);
                        violations.addAll(result.violations());
                        assertions.add(ReportAuditChecks.check(tab.name() + " render health",
                                "zero render violations", result.violations().isEmpty()
                                        ? "valid" : result.violations().toString(), result.valid()));
                    }
                    List<String> diagnostics = fixture.context().browserDiagnostics().violations();
                    violations.addAll(diagnostics);
                    assertions.add(ReportAuditChecks.check("Browser diagnostics",
                            "zero console/page/request errors", diagnostics.isEmpty()
                                    ? "valid" : diagnostics.toString(), diagnostics.isEmpty()));
                    boolean valid = violations.isEmpty();
                    audit.screen("Generated API report render health", "all report tabs",
                            fixture.audit().capture(lastPage, "generated report render health"),
                            assertions, status(valid));
                    assertTrue(valid, () -> "Generated report render violations: " + violations);
                    return null;
                }));
    }
}
