package atf.ui.autoex.support;

import atf.api.reporting.contract.AtfAssertion;
import atf.api.ui.playwright.PlaywrightActionSequence;

import java.util.List;
import java.util.Objects;

/** Emits the terminal UI contract as audit evidence, separate from browser actions and assertions. */
public final class AutoExScreenAuditObservations {
    private AutoExScreenAuditObservations() { }

    public static void recordTerminalScreenContract(PlaywrightActionSequence actions,
                                                    String title,
                                                    String url,
                                                    List<AtfAssertion> assertions,
                                                    boolean screenshotCaptured,
                                                    boolean passed) {
        Objects.requireNonNull(actions, "actions");
        int assertionCount = assertions == null ? 0 : assertions.size();
        String safeUrl = url == null || url.isBlank() ? "unavailable" : url.trim();
        actions.observation("Audit screen contract", title,
                "all component assertions pass",
                "status=" + (passed ? "PASS" : "FAIL")
                        + "; assertions=" + assertionCount
                        + "; screenshot=" + (screenshotCaptured ? "captured" : "not captured")
                        + "; url=" + safeUrl);
    }
}
