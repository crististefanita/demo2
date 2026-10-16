package atf.ui.tests.extent.section;

import atf.ui.tests.extent.section.module.SectionAuditRegistry;
import atf.ui.tests.extent.section.plan.ExtentSection;
import atf.ui.tests.extent.section.runtime.SectionAuditFixture;
import org.junit.jupiter.api.DynamicContainer;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.DynamicTest;

import static org.junit.jupiter.api.Assertions.assertTrue;

/** Small orchestrator; section behavior lives in registry modules. */
public final class SectionAuditDynamicEngine {
    private SectionAuditDynamicEngine() { }

    public static DynamicNode container(SectionAuditFixture fixture) {
        SectionAuditRegistry registry = new SectionAuditRegistry();
        fixture.spec().validatePlan();
        return DynamicContainer.dynamicContainer(fixture.spec().id(),
                fixture.spec().sections().stream()
                        .map(section -> DynamicTest.dynamicTest(section.displayName(),
                                () -> {
                                    var result = registry.module(section).audit(fixture, fixture.spec());
                                    assertTrue(result.valid(), result.validation()::failureSummary);
                                }))
                        .toList());
    }
}

