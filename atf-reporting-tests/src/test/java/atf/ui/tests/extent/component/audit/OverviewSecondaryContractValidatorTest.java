package atf.ui.tests.extent.component.audit;

import atf.api.testkit.AtfComponentTest;
import atf.ui.tests.extent.support.audit.screen.OverviewSecondaryContractValidator;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@AtfComponentTest
class OverviewSecondaryContractValidatorTest {
    @Test
    void requiresExactlyTwoSecondaryZonesAndKeepsThemCollapsed() {
        assertTrue(OverviewSecondaryContractValidator.collapsedSecondaryZones(2, 0));
        assertFalse(OverviewSecondaryContractValidator.collapsedSecondaryZones(2, 1));
        assertFalse(OverviewSecondaryContractValidator.collapsedSecondaryZones(1, 0));
    }

    @Test
    void requiresBothSecondaryZonesToExpandWithVisibleContentAndRestoreClosedState() {
        assertTrue(OverviewSecondaryContractValidator.expandableSecondaryZones(2, 2, 2));
        assertFalse(OverviewSecondaryContractValidator.expandableSecondaryZones(2, 2, 1));
        assertFalse(OverviewSecondaryContractValidator.expandableSecondaryZones(2, 1, 2));
        assertTrue(OverviewSecondaryContractValidator.restoredSecondaryZones(0));
        assertFalse(OverviewSecondaryContractValidator.restoredSecondaryZones(1));
    }

    @Test
    void reconcilesOverviewPackagesWithTheTestsTabProjection() {
        assertTrue(OverviewSecondaryContractValidator.packageProjectionMatches(
                List.of("atf.ui.tests.extent.section"),
                List.of("atf.ui.tests.extent.section")));
        assertFalse(OverviewSecondaryContractValidator.packageProjectionMatches(
                List.of("atf.ui.tests.extent.section"), List.of("Unspecified")));
    }
}
