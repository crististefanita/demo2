package atf.api.reporting.html.scope;

import atf.api.core.http.response.ApiExecutionResult;
import atf.api.core.reporting.console.AtfConsoleEvent;
import atf.api.core.reporting.console.AtfConsoleSink;
import atf.api.core.reporting.contract.AtfReportPorts;
import atf.api.core.reporting.contract.AtfReportStatus;
import atf.api.core.ui.AtfUiActionRecord;
import atf.api.reporting.html.HtmlReportSemanticDocument;
import atf.api.reporting.semantic.AtfReportSemanticRecorder;

import java.util.Collection;

/** Coordinates request evidence and its nested validation port. */
public final class HtmlReportRequestScope implements AtfReportPorts.Request {
    private final HtmlReportSemanticDocument.RequestModel model;
    private final String consoleName;
    private final AtfReportSemanticRecorder.RequestHandle semantic;
    private final AtfConsoleSink console;
    private final long startedAt = System.nanoTime();
    private boolean closed;

    public HtmlReportRequestScope(HtmlReportSemanticDocument.RequestModel model,
                                  String consoleName,
                                  AtfReportSemanticRecorder.RequestHandle semantic,
                                  AtfConsoleSink console) {
        this.model = model;
        this.consoleName = consoleName;
        this.semantic = semantic;
        this.console = console;
    }

    @Override
    public void detail(String name, String value) {
        model.addDetail(name, value);
        semantic.detail(name);
    }

    @Override
    public void json(String label, String json) {
        model.addJson(label, json);
        semantic.json(label);
    }

    @Override
    public void execution(ApiExecutionResult result) {
        model.execution(result);
        semantic.execution();
    }

    @Override
    public void screenshot(String title, String base64Png) {
        model.addScreenshot();
        semantic.screenshot();
    }

    @Override
    public void uiActions(Collection<AtfUiActionRecord> actions) {
        model.addUiActions(actions);
        semantic.uiActions(actions);
    }

    @Override
    public AtfReportPorts.Validation startValidation(String title) {
        ensureOpen();
        var validation = model.addValidation(title);
        String validationName = title == null ? "Validation" : title;
        console.publish(AtfConsoleEvent.started(AtfConsoleEvent.Scope.VALIDATION, validationName));
        return new HtmlReportValidationScope(validation, validationName,
                semantic.startValidation(validationName), console);
    }

    @Override
    public void complete(AtfReportStatus status) {
        model.complete(status);
        semantic.complete(status);
        console.publish(AtfConsoleEvent.completed(AtfConsoleEvent.Scope.REQUEST,
                consoleName, status, HtmlReportScopeTiming.elapsedMillis(startedAt)));
    }

    @Override
    public void close() {
        closed = true;
    }

    private void ensureOpen() {
        if (closed) {
            throw new IllegalStateException("Request is closed");
        }
    }
}
