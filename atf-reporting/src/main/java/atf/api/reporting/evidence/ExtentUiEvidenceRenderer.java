package atf.api.reporting.evidence;

import atf.api.reporting.configuration.ReportingSettings;
import atf.api.reporting.contract.AtfAssertion;
import atf.api.reporting.evidence.ui.ExtentUiActionRenderer;
import atf.api.reporting.evidence.ui.ExtentUiAssertionRenderer;
import atf.api.reporting.evidence.ui.ExtentUiObservationRenderer;
import atf.api.reporting.model.ReportScreenshot;
import atf.api.reporting.security.SensitiveDataRedactor;
import atf.api.reporting.ui.AtfUiActionRecord;
import atf.api.reporting.ui.AtfUiEvidenceSource;
import com.aventstack.extentreports.ExtentTest;

import java.util.Collection;
import java.util.List;

/** Coordinates specialized UI evidence renderers without owning their markup. */
public final class ExtentUiEvidenceRenderer {
    private final ExtentUiActionRenderer actionRenderer;
    private final ExtentUiObservationRenderer observationRenderer;
    private final ExtentUiAssertionRenderer assertionRenderer;

    ExtentUiEvidenceRenderer(ReportingSettings settings, SensitiveDataRedactor redactor) {
        actionRenderer = new ExtentUiActionRenderer(settings);
        observationRenderer = new ExtentUiObservationRenderer(settings);
        assertionRenderer = new ExtentUiAssertionRenderer(settings, redactor);
    }

    void renderUiActions(ExtentTest node, List<AtfUiActionRecord> actions) {
        renderUiActions(node, actions, List.of());
    }

    void renderUiActions(ExtentTest node, List<AtfUiActionRecord> actions,
                         List<ReportScreenshot> screenshots) {
        if (actions == null || actions.isEmpty()) return;
        actionRenderer.render(node, actions.stream()
                .filter(action -> action.source() == AtfUiEvidenceSource.ACTION).toList(), screenshots);
        observationRenderer.render(node, actions.stream()
                .filter(action -> action.source() == AtfUiEvidenceSource.OBSERVE).toList());
    }

    /** Renders the action table only; the caller adds native media separately. */
    void renderUiActionsAsMedia(ExtentTest node, List<AtfUiActionRecord> actions) {
        if (actions == null || actions.isEmpty()) return;
        actionRenderer.renderAsMedia(node, actions.stream()
                .filter(action -> action.source() == AtfUiEvidenceSource.ACTION).toList());
        observationRenderer.render(node, actions.stream()
                .filter(action -> action.source() == AtfUiEvidenceSource.OBSERVE).toList());
    }

    void renderAssertions(ExtentTest node, Collection<AtfAssertion> assertions) {
        assertionRenderer.render(node, assertions);
    }
}
