package atf.api.reporting.ui.audit.section;
import atf.api.core.reporting.contract.AtfAssertion;
import atf.api.reporting.audit.ReportAuditChecks;
import atf.api.reporting.audit.ValidationResult;
import atf.api.reporting.audit.ReportAuditSnapshot;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Pure JSON evidence contract; it has no browser, locator or renderer dependency. */
public final class JsonEvidenceValidator {
    private static final ObjectMapper JSON = new ObjectMapper();

    public ValidationResult validate(ReportAuditSnapshot.JsonEvidence snapshot) {
        Objects.requireNonNull(snapshot, "snapshot");
        List<atf.api.core.reporting.contract.AtfAssertion> assertions = new ArrayList<>();
        assertions.add(ReportAuditChecks.check("Request JSON", "valid JSON document",
                snapshot.requestBody(), validJson(snapshot.requestBody())));
        assertions.add(ReportAuditChecks.check("Response JSON", "valid JSON document",
                snapshot.responseBody(), validJson(snapshot.responseBody())));
        assertions.add(ReportAuditChecks.check("Extraction violations", "0",
                snapshot.violations().toString(), snapshot.violations().isEmpty()));
        return ValidationResult.of(assertions);
    }

    private static boolean validJson(String value) {
        if (value == null || value.isBlank()) return false;
        try {
            JSON.readTree(value);
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }
}
