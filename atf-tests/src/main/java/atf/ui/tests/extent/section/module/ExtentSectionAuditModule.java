package atf.ui.tests.extent.section.module;

import atf.ui.tests.extent.section.plan.ExtentSection;
import atf.ui.tests.extent.section.plan.SectionAuditSpec;
import atf.ui.tests.extent.section.runtime.SectionAuditFixture;
import atf.ui.tests.extent.section.result.SectionAuditResult;

/** One plug-and-play vertical slice for one report section. */
public interface ExtentSectionAuditModule {
    ExtentSection section();

    SectionAuditResult audit(SectionAuditFixture fixture, SectionAuditSpec spec);
}

