package atf.ui.tests.extent.flows;

import atf.api.ui.config.UiConfiguration;
import atf.ui.tests.extent.audit.ApiReportContractValidation;
import atf.ui.tests.extent.audit.MockReportContractAuditor;
import atf.ui.tests.extent.composition.ApiReportAuditSetup;
import atf.ui.tests.extent.locators.ReportUiLocators;
import atf.ui.tests.extent.plan.UiReportAuditSpec;
import atf.ui.tests.extent.support.ReportAuditContext;
import atf.ui.tests.extent.support.ReportAuditInputs;
import atf.ui.tests.extent.support.UiAuditFlow;

import java.util.Objects;

/**
 * Generic composition root for one Extent-report UI audit.
 *
 * <p>The input is a spec, not a test class hierarchy. It resolves the API
 * collection once, creates one read-only report context and exposes the
 * existing semantic flows and validators to dynamic rules.</p>
 */
public final class UiReportAuditFixture implements AutoCloseable {
    private final UiReportAuditSpec spec;
    private final ApiReportAuditSetup.Result setup;
    private final ReportAuditContext context;
    private final UiAuditFlow audit;
    private final ApiReportUiFlow flow;
    private final ApiReportExplorationFlow exploration;
    private final ApiReportContractValidation validation;

    private UiReportAuditFixture(UiReportAuditSpec spec,
                                 ApiReportAuditSetup.Result setup,
                                 ReportAuditContext context,
                                 UiAuditFlow audit,
                                 ApiReportUiFlow flow,
                                 ApiReportExplorationFlow exploration,
                                 ApiReportContractValidation validation) {
        this.spec = spec;
        this.setup = setup;
        this.context = context;
        this.audit = audit;
        this.flow = flow;
        this.exploration = exploration;
        this.validation = validation;
    }

    public static UiReportAuditFixture open(Class<?> owner, UiConfiguration configuration,
                                            UiReportAuditSpec spec) {
        Objects.requireNonNull(owner, "owner");
        Objects.requireNonNull(configuration, "configuration");
        Objects.requireNonNull(spec, "spec");
        ApiReportAuditSetup.Result setup = new ApiReportAuditSetup()
                .generate(owner, configuration, spec.apiFamilies());
        ReportAuditContext context = ReportAuditContext.open(configuration,
                ReportAuditInputs.generated(setup.report()).resolve(configuration), spec.id());
        UiAuditFlow audit = new UiAuditFlow(context, configuration,
                owner.getSimpleName(), owner.getPackageName(), spec.tags(),
                spec.setupDescription(), spec.failureTitle(), spec.failureTarget());
        ReportUiLocators locators = new ReportUiLocators();
        ApiReportUiFlow flow = new ApiReportUiFlow(audit, locators);
        ApiReportExplorationFlow exploration = new ApiReportExplorationFlow(audit, locators);
        ApiReportContractValidation validation = new ApiReportContractValidation(locators,
                new MockReportContractAuditor(setup.mock()));
        return new UiReportAuditFixture(spec, setup, context, audit, flow,
                exploration, validation);
    }

    public UiReportAuditSpec spec() { return spec; }

    public ApiReportAuditSetup.Result setup() { return setup; }

    public ReportAuditContext context() { return context; }

    public UiAuditFlow audit() { return audit; }

    public ApiReportUiFlow flow() { return flow; }

    public ApiReportExplorationFlow exploration() { return exploration; }

    public ApiReportContractValidation validation() { return validation; }

    @Override
    public void close() {
        context.close();
    }
}
