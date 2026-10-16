package atf.ui.tests.extent.section.extract;

import atf.ui.tests.extent.locators.ReportUiLocators;
import atf.ui.tests.extent.plan.ReportAuditSnapshot;
import com.microsoft.playwright.Locator;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Browser adapter that extracts raw request/response JSON facts without validating them. */
public final class ApiJsonEvidenceExtractor {
    private final ReportUiLocators locators;

    public ApiJsonEvidenceExtractor(ReportUiLocators locators) {
        this.locators = Objects.requireNonNull(locators, "locators");
    }

    public ReportAuditSnapshot.JsonEvidence extract(Locator requestNode) {
        Objects.requireNonNull(requestNode, "requestNode");
        List<String> violations = new ArrayList<>();
        String requestBody = body(locators.requestJsonBodyElements(requestNode), "request", violations);
        String responseBody = body(locators.responseJsonBodyElements(requestNode), "response", violations);
        return new ReportAuditSnapshot.JsonEvidence(requestBody, responseBody, violations);
    }

    private static String body(Locator bodies, String side, List<String> violations) {
        if (bodies.count() != 1) {
            violations.add(side + " JSON body count must be exactly 1 but was " + bodies.count());
            return "";
        }
        Locator body = bodies.first();
        String tagName = String.valueOf(body.evaluate("element => element.tagName"));
        String value = "TEXTAREA".equalsIgnoreCase(tagName)
                ? body.inputValue().trim() : body.innerText().trim();
        if (value.isBlank()) violations.add(side + " JSON body must not be blank");
        return value;
    }
}
