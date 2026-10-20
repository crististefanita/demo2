package atf.ui.practiceshop.support;

import atf.api.reporting.model.ReportExecutionEvidence;
import atf.api.reporting.contract.AtfAssertion;
import atf.api.ui.playwright.PlaywrightActionSequence;
import atf.api.ui.playwright.UiScreenValidation;
import com.microsoft.playwright.Page;

import java.util.Collection;
import java.util.Objects;

/**
 * Publishes dynamic, renderer-neutral audit observations for one purchase
 * checkpoint. Assertions keep the verdict; observations explain the evidence
 * boundary that produced it.
 */
public final class PracticeShopAuditObservations {
    private PracticeShopAuditObservations() {
    }

    public static void recordCheckpoint(PlaywrightActionSequence actions,
                                        Page page,
                                        PracticeShopPurchaseCheckpoint checkpoint,
                                        UiScreenValidation.Evidence screenshot) {
        Objects.requireNonNull(actions, "actions");
        Objects.requireNonNull(page, "page");
        Objects.requireNonNull(checkpoint, "checkpoint");
        Objects.requireNonNull(screenshot, "screenshot");

        recordScreenContract(actions, page, checkpoint.title(), checkpoint.assertions(), screenshot,
                checkpoint.passed());

        int exchange = 0;
        for (ReportExecutionEvidence evidence : checkpoint.apiEvidence()) {
            if (evidence == null) {
                continue;
            }
            var request = evidence.request();
            var response = evidence.response();
            if (request == null || response == null) continue;
            exchange++;
            String route = request.method() + " " + request.path();
            actions.observation(
                    "Audit API exchange #" + exchange,
                    route,
                    "HTTP response and execution evidence are present",
                    "status=" + response.status()
                            + "; auth=" + safe(request.authProfile())
                            + "; transport=" + evidence.provenance().reportLabel()
                            + "; attempts=" + evidence.attempts());
        }
    }

    /** Adds the same screen audit to the account/catalog compatibility path. */
    public static void recordScreenContract(PlaywrightActionSequence actions,
                                            Page page,
                                            String title,
                                            Collection<AtfAssertion> assertions,
                                            UiScreenValidation.Evidence screenshot,
                                            boolean passed) {
        Objects.requireNonNull(actions, "actions");
        Objects.requireNonNull(page, "page");
        Objects.requireNonNull(screenshot, "screenshot");

        actions.observation(
                "Audit screen contract",
                title,
                "all component assertions pass",
                "status=" + (passed ? "PASS" : "FAIL")
                        + "; assertions=" + (assertions == null ? 0 : assertions.size())
                        + "; screenshot=" + (screenshot.hasScreenshotPayload() ? "valid PNG" : "not captured")
                        + "; url=" + safeUrl(page));
    }

    private static String safeUrl(Page page) {
        try {
            return safe(page.url());
        } catch (RuntimeException ignored) {
            return "unavailable";
        }
    }

    private static String safe(String value) {
        return value == null || value.isBlank() ? "not declared" : value.trim();
    }
}
