package atf.ui.tests.extent.support.api;
import atf.ui.tests.extent.support.audit.api.ApiReportContractValidation;
import atf.ui.tests.extent.support.audit.mock.MockReportContractAuditor;
import atf.ui.tests.extent.support.audit.ReportAuditContext;
import atf.ui.tests.extent.support.audit.ReportAuditInputs;
import atf.ui.tests.extent.support.lifecycle.UiAuditFlow;

import atf.ui.tests.extent.support.config.UiConfiguration;
import atf.ui.tests.extent.support.api.ApiReportAuditSetup;
import atf.ui.tests.extent.support.locators.ReportUiLocators;
import atf.ui.tests.extent.component.plan.UiReportAuditSpec;

import java.util.List;
import java.util.Objects;

/**
 * Immutable composition boundary shared by API-to-UI contract tests.
 *
 * <p>It centralizes setup annotation/YAML resolution, read-only report
 * lifecycle, semantic navigation and mock-backed validation. Individual tests
 * therefore express only the owner/screen intent and never repeat the same
 * fixture wiring.</p>
 */
public final class ApiReportUiFixture implements AutoCloseable {
    private final UiReportAuditFixture delegate;

    private ApiReportUiFixture(UiReportAuditFixture delegate) {
        this.delegate = delegate;
    }

    public static ApiReportUiFixture open(Class<?> owner, UiConfiguration configuration,
                                          String auditSuffix, List<String> tags,
                                          String setupDescription, String failureTitle,
                                          String failureTarget) {
        ApiReportAuditSetup.Selection selection = ApiReportAuditSetup.selection(owner, configuration);
        UiReportAuditSpec spec = UiReportAuditSpec.legacy(auditSuffix,
                selection.families(), tags,
                setupDescription, failureTitle, failureTarget);
        return new ApiReportUiFixture(UiReportAuditFixture.open(owner, configuration, spec, selection));
    }

    public ApiReportAuditSetup.Result setup() { return delegate.setup(); }

    public ReportAuditContext context() { return delegate.context(); }

    public UiAuditFlow audit() { return delegate.audit(); }

    public ApiReportUiFlow flow() { return delegate.flow(); }

    public ApiReportContractValidation validation() { return delegate.validation(); }

    @Override
    public void close() {
        delegate.close();
    }
}
