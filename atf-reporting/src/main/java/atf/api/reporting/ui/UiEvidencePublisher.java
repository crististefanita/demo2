package atf.api.reporting.ui;
import atf.api.core.reporting.contract.AtfAssertion;
import atf.api.core.reporting.contract.AtfGherkinKeyword;
import atf.api.core.reporting.contract.AtfReportPorts;
import atf.api.core.reporting.contract.AtfReportStatus;

import atf.api.core.ui.AtfUiActionRecord;
import atf.api.reporting.markup.AtfReportEvidenceKeys;

import java.util.Collection;
import java.util.List;

/**
 * Publishes the concrete UI evidence attached to one report phase.
 *
 * <p>{@link UiExtentReportSession} owns lifecycle state; this component owns
 * the evidence contract. Keeping those responsibilities separate prevents
 * screenshot, action and assertion rules from being duplicated across the
 * normal, media and failure paths.</p>
 */
public final class UiEvidencePublisher {
    private static final String SCREEN_VALIDATION = "Screen validation";

    private final AtfReportPorts.Phase phase;

    UiEvidencePublisher(AtfReportPorts.Phase phase) {
        this.phase = phase;
    }

    void publishOutcome(String keyword, String title, String target,
                       AtfReportStatus status) {
        String stepTitle = stepTitle(keyword, title);
        var request = phase.startRequest(stepTitle, "", "");
        try {
            request.detail(AtfReportEvidenceKeys.REQUEST_ACTION, stepTitle);
            detailTarget(request, target);
            request.detail(AtfReportEvidenceKeys.OUTCOME_TYPE, AtfReportEvidenceKeys.ARTIFACT_PUBLICATION);
            request.complete(defaultStatus(status, AtfReportStatus.PASS));
        } finally {
            request.close();
        }
    }

    /**
     * Publishes lifecycle/setup evidence without misclassifying it as a UI
     * validation. Setup proves that the input artifact is available; the
     * browser assertions belong to the TEST BODY Screen validation card.
     */
    void publishSetup(String keyword, String title, String target,
                      Collection<AtfAssertion> assertions, AtfReportStatus status) {
        String stepTitle = stepTitle(keyword, title);
        var request = phase.startRequest(stepTitle, "", "");
        try {
            request.detail(AtfReportEvidenceKeys.REQUEST_ACTION, stepTitle);
            detailTarget(request, target);
            request.detail(AtfReportEvidenceKeys.EVIDENCE_TYPE, AtfReportEvidenceKeys.REPORT_SETUP);
            request.detail(AtfReportEvidenceKeys.SETUP_RESULT,
                    defaultStatus(status, AtfReportStatus.PASS).displayValue());
            if (assertions != null) {
                int index = 0;
                for (AtfAssertion assertion : assertions) {
                    if (assertion == null) continue;
                    index++;
                    request.detail(AtfReportEvidenceKeys.setupCheck(index),
                            assertion.name() + " | expected=" + assertion.expected()
                                    + " | actual=" + assertion.actual()
                                    + " | result=" + assertion.resultStatus().displayValue());
                }
            }
            request.complete(defaultStatus(status, AtfReportStatus.PASS));
        } finally {
            request.close();
        }
    }

    void publishAction(String keyword, String title, String target, String base64Png,
                       Collection<AtfAssertion> assertions, AtfReportStatus status,
                       String validationTitle, Collection<AtfUiActionRecord> actions) {
        String stepTitle = stepTitle(keyword, title);
        var request = phase.startRequest(stepTitle, "", "");
        try {
            request.detail(AtfReportEvidenceKeys.REQUEST_ACTION, stepTitle);
            detailTarget(request, target);
            boolean visualValidation = SCREEN_VALIDATION.equals(validationTitle);
            if (visualValidation) {
                request.detail(AtfReportEvidenceKeys.EVIDENCE_TYPE, AtfReportEvidenceKeys.SCREEN_VALIDATION);
                request.detail(AtfReportEvidenceKeys.EVIDENCE_POLICY, "one Base64 screenshot link per visual state");
            }
            if (visualValidation || (assertions != null && !assertions.isEmpty())) {
                var validation = request.startValidation(validationTitle);
                publishActions(validation, actions);
                publishScreenshot(validation, title, base64Png, status, "UI assertion");
                validation.assertions(assertions);
                validation.complete(defaultStatus(status, AtfReportStatus.PASS));
            }
            request.complete(defaultStatus(status, AtfReportStatus.PASS));
        } finally {
            request.close();
        }
    }

    void publishScreenValidationAsMedia(String title, String target, String base64Png,
                                        Collection<AtfAssertion> assertions,
                                        Collection<AtfUiActionRecord> actions,
                                        AtfReportStatus status) {
        String stepTitle = stepTitle(AtfGherkinKeyword.THEN.text(), title);
        var request = phase.startRequest(stepTitle, "", "");
        try {
            request.detail("request.action", stepTitle);
            detailTarget(request, target);
            request.detail("evidence.type", "screen-validation");
            request.detail("evidence.policy", "one visible Base64 image inside one safe clickable link");
            var validation = request.startValidation(SCREEN_VALIDATION);
            publishActions(validation, actions);
            if (hasText(base64Png)) {
                validation.mediaScreenshot("UI screenshot — " + title + " — "
                        + defaultStatus(status, AtfReportStatus.PASS), base64Png);
            }
            validation.assertions(assertions);
            validation.complete(defaultStatus(status, AtfReportStatus.PASS));
            request.complete(defaultStatus(status, AtfReportStatus.PASS));
        } finally {
            request.close();
        }
    }

    void publishFailure(String title, String target, Collection<AtfUiActionRecord> actions,
                        Throwable failure, String base64Png) {
        String safeTitle = !hasText(title) ? "UI audit failure" : title.trim();
        String cause = failure == null ? "failure recorded" : failure.getClass().getSimpleName()
                + ": " + (failure.getMessage() == null ? "" : failure.getMessage());
        var request = phase.startRequest(AtfGherkinKeyword.THEN.text() + " " + safeTitle, "", "");
        try {
            request.detail(AtfReportEvidenceKeys.REQUEST_ACTION, safeTitle);
            detailTarget(request, target);
            request.detail(AtfReportEvidenceKeys.EVIDENCE_TYPE, AtfReportEvidenceKeys.UI_FAILURE);
            request.detail(AtfReportEvidenceKeys.EVIDENCE_POLICY, !hasText(base64Png)
                    ? "preserve executed actions; screenshot disabled or unavailable"
                    : "preserve executed actions; screenshot captured by policy");
            var validation = request.startValidation("Failure evidence");
            publishActions(validation, actions);
            if (hasText(base64Png)) validation.screenshot("UI failure screenshot", base64Png);
            validation.assertions(List.of(new AtfAssertion(
                    "Failure cause", "no failure", cause, false)));
            validation.completeFailure(AtfReportStatus.FAIL);
            request.complete(AtfReportStatus.FAIL);
        } finally {
            request.close();
        }
    }

    private static void publishActions(AtfReportPorts.Validation validation,
                                       Collection<AtfUiActionRecord> actions) {
        if (actions != null && !actions.isEmpty()) validation.uiActions(actions);
    }

    private static void publishScreenshot(AtfReportPorts.Validation validation, String title,
                                          String base64Png, AtfReportStatus status,
                                          String prefix) {
        if (hasText(base64Png)) {
            validation.screenshot(prefix + " — " + title + " — "
                    + defaultStatus(status, AtfReportStatus.PASS), base64Png);
        }
    }

    private static void detailTarget(AtfReportPorts.Request request, String target) {
        if (hasText(target)) request.detail(AtfReportEvidenceKeys.REQUEST_TARGET, target);
    }

    private static String stepTitle(String keyword, String title) {
        return ((keyword == null ? "" : keyword.trim()) + " "
                + (title == null ? "" : title.trim())).trim();
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private static AtfReportStatus defaultStatus(AtfReportStatus status, AtfReportStatus fallback) {
        return status == null ? fallback : status;
    }
}
