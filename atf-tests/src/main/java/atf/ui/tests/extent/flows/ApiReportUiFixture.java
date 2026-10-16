package atf.ui.tests.extent.flows;

import atf.api.ui.config.UiConfiguration;
import atf.ui.tests.extent.audit.ApiReportContractValidation;
import atf.ui.tests.extent.composition.ApiReportAuditSetup;
import atf.ui.tests.extent.audit.MockReportContractAuditor;
import atf.ui.tests.extent.locators.ReportUiLocators;
import atf.ui.tests.extent.support.ReportAuditContext;
import atf.ui.tests.extent.support.ReportAuditInputs;
import atf.ui.tests.extent.support.UiAuditFlow;
import atf.ui.tests.extent.plan.UiReportAuditSpec;

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
        UiReportAuditSpec spec = UiReportAuditSpec.legacy(auditSuffix,
                ApiReportSetupFlow.families(owner, configuration), tags,
                setupDescription, failureTitle, failureTarget);
        return new ApiReportUiFixture(UiReportAuditFixture.open(owner, configuration, spec));
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
