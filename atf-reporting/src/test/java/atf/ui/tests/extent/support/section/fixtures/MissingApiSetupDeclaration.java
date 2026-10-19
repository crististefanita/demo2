package atf.ui.tests.extent.support.section.fixtures;

import atf.api.reporting.ui.audit.plan.ExtentSection;
import atf.api.reporting.ui.audit.plan.UiExtentReportSource;
import atf.api.reporting.ui.audit.plan.UiExtentSectionAudit;

/** Invalid generated-source declaration used by source-resolution tests. */
@UiExtentSectionAudit(source = UiExtentReportSource.GENERATED_API,
        sections = {ExtentSection.TESTS})
public final class MissingApiSetupDeclaration {
    private MissingApiSetupDeclaration() {
    }
}
