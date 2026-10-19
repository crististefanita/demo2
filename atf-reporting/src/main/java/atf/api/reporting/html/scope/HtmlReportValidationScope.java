package atf.api.reporting.html.scope;

import atf.api.reporting.console.AtfConsoleEvent;
import atf.api.reporting.console.AtfConsoleSink;
import atf.api.reporting.contract.AtfAssertion;
import atf.api.reporting.contract.AtfReportPorts;
import atf.api.reporting.contract.AtfReportStatus;
import atf.api.reporting.ui.AtfUiActionRecord;
import atf.api.reporting.html.HtmlReportSemanticDocument;
import atf.api.reporting.semantic.AtfReportSemanticRecorder;

import java.util.Collection;

/** Coordinates assertions and UI evidence for one validation leaf. */
public final class HtmlReportValidationScope implements AtfReportPorts.Validation {
    private final HtmlReportSemanticDocument.ValidationModel model;
    private final String consoleName;
    private final AtfReportSemanticRecorder.ValidationHandle semantic;
    private final AtfConsoleSink console;
    private final long startedAt = System.nanoTime();

    public HtmlReportValidationScope(HtmlReportSemanticDocument.ValidationModel model,
                                     String consoleName,
                                     AtfReportSemanticRecorder.ValidationHandle semantic,
                                     AtfConsoleSink console) {
        this.model = model;
        this.consoleName = consoleName;
        this.semantic = semantic;
        this.console = console;
    }

    @Override
    public void assertions(Collection<AtfAssertion> assertions) {
        model.assertions(assertions);
        semantic.assertions(assertions);
    }

    @Override
    public void uiActions(Collection<AtfUiActionRecord> actions) {
        model.addUiActions(actions);
        semantic.uiActions(actions);
    }

    @Override
    public void screenshot(String title, String base64Png) {
        model.addScreenshot();
        semantic.screenshot();
    }

    @Override
    public void mediaScreenshot(String title, String base64Png) {
        model.addScreenshot();
        semantic.screenshot();
    }

    @Override
    public void complete(AtfReportStatus status) {
        model.complete(status);
        semantic.complete(status);
        console.publish(AtfConsoleEvent.completed(AtfConsoleEvent.Scope.VALIDATION,
                consoleName, status, HtmlReportScopeTiming.elapsedMillis(startedAt)));
    }

    @Override
    public void close() {
    }
}
