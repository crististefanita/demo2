package atf.api.validation;
import atf.api.testkit.AtfUnitTest;

import atf.api.core.reporting.contract.AtfReportStatus;
import atf.api.validation.evaluation.ValidationFailureEvidence;
import atf.api.validation.evaluation.ValidationTable;
import atf.api.validation.evaluation.ValidationOutcomePolicy;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@AtfUnitTest

class ValidationFailureEvidenceTest {
    @Test
    void exposesOneAssertionAsCauseExpectedAndActual() {
        var table = new ValidationTable();
        table.add("HTTP status", "201", "200", false);

        var decision = ValidationOutcomePolicy.strict().decide(table);
        ValidationFailureEvidence evidence = decision.failureEvidence();

        assertEquals(AtfReportStatus.FAIL, decision.status());
        assertEquals("Contract assertion failed: HTTP status", evidence.cause());
        assertEquals("HTTP status", evidence.assertions());
        assertEquals("201", evidence.expected());
        assertEquals("200", evidence.actual());
        assertEquals("HTTP status", evidence.details().get("Assertion"));
    }

    @Test
    void keepsMultipleAssertionPairsReadable() {
        var table = new ValidationTable();
        table.add("HTTP status", "201", "200", false);
        table.add("Header X-Required", "present", "<missing>", false);

        ValidationFailureEvidence evidence = ValidationOutcomePolicy.strict()
                .decide(table).failureEvidence();

        assertEquals("HTTP status; Header X-Required", evidence.assertions());
        assertEquals("HTTP status = 201; Header X-Required = present", evidence.expected());
        assertEquals("HTTP status = 200; Header X-Required = <missing>", evidence.actual());
    }

    @Test
    void isUnavailableForPassingDecision() {
        var table = new ValidationTable();
        table.add("HTTP status", "200", "200", true);

        var decision = ValidationOutcomePolicy.strict().decide(table);

        assertThrows(IllegalStateException.class, decision::failureEvidence);
    }
}
