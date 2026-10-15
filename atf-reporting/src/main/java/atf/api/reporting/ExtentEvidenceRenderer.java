package atf.api.reporting;

import atf.api.core.config.ReportingSettings;
import atf.api.core.http.ApiExecutionResult;
import atf.api.core.reporting.AtfAssertion;
import atf.api.core.security.SensitiveDataRedactor;
import atf.api.core.ui.AtfUiActionRecord;
import com.aventstack.extentreports.ExtentTest;

import java.util.Collection;
import java.util.List;

/** Coordinates specialized evidence renderers without owning Extent tree lifecycle. */
final class ExtentEvidenceRenderer {
    private final ExtentTransportEvidenceRenderer transport;
    private final ExtentUiEvidenceRenderer ui;

    ExtentEvidenceRenderer(ReportingSettings settings, SensitiveDataRedactor redactor) {
        this.transport = new ExtentTransportEvidenceRenderer(settings, redactor);
        this.ui = new ExtentUiEvidenceRenderer(settings, redactor);
    }

    void renderDetails(ExtentTest node, List<String[]> details) {
        transport.renderDetails(node, details);
    }

    void renderRequestDetails(ExtentTest node, List<String[]> details, List<String[]> json) {
        transport.renderRequestDetails(node, details, json);
    }

    void renderResponseDetails(ExtentTest node, List<String[]> details, List<String[]> json) {
        transport.renderResponseDetails(node, details, json);
    }

    void renderExecution(ExtentTest node, ApiExecutionResult execution) {
        transport.renderExecution(node, execution);
    }

    void renderScreenshots(ExtentTest node, List<String[]> screenshots) {
        transport.renderScreenshots(node, screenshots);
    }

    void renderUiActions(ExtentTest node, List<AtfUiActionRecord> actions) {
        ui.renderUiActions(node, actions);
    }

    void renderUiActions(ExtentTest node, List<AtfUiActionRecord> actions,
                         List<String[]> screenshots) {
        ui.renderUiActions(node, actions, screenshots);
    }

    void renderUiActionsAsMedia(ExtentTest node, List<AtfUiActionRecord> actions) {
        ui.renderUiActionsAsMedia(node, actions);
    }

    void renderAssertions(ExtentTest node, Collection<AtfAssertion> assertions) {
        ui.renderAssertions(node, assertions);
    }
}
