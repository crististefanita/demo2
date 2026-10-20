package atf.api.tests.governance;

import atf.api.testkit.AtfIntegrationTest;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/** Explicit entry point for the governance checks that inspect the complete reactor. */
@AtfIntegrationTest
@Suite
@SelectClasses({
        ArchitectureModuleBoundaryTest.class,
        ArchitectureOwnershipBoundaryTest.class,
        AtfPackageCohesionGovernanceTest.class,
        AtfSourceReadabilityGovernanceTest.class,
        ConstantsOwnershipGovernanceTest.class,
        DynamicPathDocumentationArchitectureTest.class,
        DocumentationDiagramGovernanceTest.class
})
class WholeAtfGovernanceSuiteTest {
}
