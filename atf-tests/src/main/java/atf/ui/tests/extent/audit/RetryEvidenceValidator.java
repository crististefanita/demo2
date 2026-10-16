package atf.ui.tests.extent.audit;

import atf.ui.tests.extent.plan.RetryEvidenceSnapshot;

import java.util.ArrayList;
import java.util.List;

/** Pure reconciliation rules for rendered retry metadata. */
public final class RetryEvidenceValidator {
    public List<String> validate(RetryEvidenceSnapshot snapshot) {
        if (snapshot == null) return List.of("retry snapshot is missing");
        List<String> violations = new ArrayList<>();
        if (snapshot.attempts() != snapshot.retryEvents() + 1) {
            violations.add("attempts must equal 1 + retry.events: attempts="
                    + snapshot.attempts() + ", retry.events=" + snapshot.retryEvents());
        }
        if (snapshot.history().size() != snapshot.attempts()) {
            violations.add("attempt history must contain every attempt: history="
                    + snapshot.history().size() + ", attempts=" + snapshot.attempts());
        }
        if (!snapshot.history().isEmpty()) {
            var last = snapshot.history().getLast();
            if (last.number() != snapshot.attempts()) {
                violations.add("last attempt number must equal attempts: last="
                        + last.number() + ", attempts=" + snapshot.attempts());
            }
            if (snapshot.finalStatus() != last.status()) {
                violations.add("last attempt status must equal response status: last="
                        + last.status() + ", response=" + snapshot.finalStatus());
            }
        }
        return List.copyOf(violations);
    }
}
