package atf.ui.tests.extent.section.validate;

import atf.ui.tests.extent.plan.ReportAuditSnapshot;
import atf.ui.tests.extent.support.ReportAuditChecks;
import atf.ui.tests.extent.support.ValidationResult;

import java.util.List;
import java.util.Objects;

/** Pure contract for request/response/execution evidence and JSON payloads. */
public final class EvidenceSectionValidator {
    private final JsonEvidenceValidator jsonValidator = new JsonEvidenceValidator();

    public ValidationResult validate(ReportAuditSnapshot.RequestEvidence snapshot) {
        Objects.requireNonNull(snapshot, "snapshot");
        ReportAuditSnapshot.JsonEvidence json = snapshot.jsonEvidence();
        ValidationResult structure = ValidationResult.of(List.of(
                ReportAuditChecks.check("Evidence tables", "request + response + execution",
                        snapshot.requestTableCount() + " + " + snapshot.responseTableCount()
                                + " + " + snapshot.executionTableCount(),
                        snapshot.tablesVisible() && snapshot.requestTableCount() > 0
                                && snapshot.responseTableCount() > 0 && snapshot.executionTableCount() > 0),
                ReportAuditChecks.check("JSON evidence present", "request and response bodies",
                        json.requestBody() + " / " + json.responseBody(),
                        json.requestBody() != null && !json.requestBody().isBlank()
                                && json.responseBody() != null && !json.responseBody().isBlank())));
        return structure.merge(jsonValidator.validate(json));
    }
}
