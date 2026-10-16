package atf.ui.tests.extent.section;

import atf.ui.tests.extent.section.module.SectionAuditRegistry;
import atf.ui.tests.extent.section.plan.ExtentSection;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class SectionAuditRegistryTest {
    @Test
    void everySupportedSectionHasOnePlugAndPlayModule() {
        SectionAuditRegistry registry = new SectionAuditRegistry();
        for (ExtentSection section : ExtentSection.values()) {
            assertDoesNotThrow(() -> registry.module(section));
        }
    }
}
