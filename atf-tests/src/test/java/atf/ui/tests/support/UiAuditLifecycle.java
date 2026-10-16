package atf.ui.tests.support;

import atf.api.core.reporting.AtfAssertion;
import atf.api.core.reporting.AtfReportStatus;
import atf.api.core.ui.AtfUiActionRecord;
import atf.api.reporting.UiExtentReportSession;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Owns the completion boundary for one UI audit scenario.
 *
 * <p>The exploratory test remains responsible for Gherkin steps and DOM
 * observations. This component owns the invariant that a failure publishes
 * the already executed actions before the phase and scenario are completed.</p>
 */
public final class UiAuditLifecycle {
    private final UiExtentReportSession.UiScenario scenario;
    private final UiExtentReportSession.UiPhase body;
    private boolean bodyCompleted;

    public UiAuditLifecycle(UiExtentReportSession.UiScenario scenario,
                            UiExtentReportSession.UiPhase body) {
        this.scenario = Objects.requireNonNull(scenario, "scenario");
        this.body = Objects.requireNonNull(body, "body");
    }

    public void screenValidation(String title, String target, String screenshot,
                                  Collection<AtfAssertion> assertions,
                                  Collection<AtfUiActionRecord> actions,
                                  AtfReportStatus status) {
        body.screenValidation(title, target, screenshot,
                assertions == null ? List.of() : assertions,
                actions == null ? List.of() : actions, status);
    }

    public void pass() {
        completeBody(AtfReportStatus.PASS);
        scenario.complete(AtfReportStatus.PASS);
    }

    public void fail(String title, String target,
                     Collection<AtfUiActionRecord> executedActions,
                     Throwable failure) {
        body.failureEvidence(title, target,
                executedActions == null ? List.of() : executedActions, failure);
        completeBody(AtfReportStatus.FAIL);
        scenario.complete(AtfReportStatus.FAIL);
    }

    private void completeBody(AtfReportStatus status) {
        if (!bodyCompleted) {
            body.complete(status);
            bodyCompleted = true;
        }
    }
}
