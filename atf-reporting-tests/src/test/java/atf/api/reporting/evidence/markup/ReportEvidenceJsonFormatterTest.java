package atf.api.reporting.evidence.markup;

import atf.api.reporting.configuration.ReportingSettings;
import atf.api.reporting.model.ReportJsonBody;
import atf.api.reporting.security.SensitiveDataRedactor;
import atf.api.testkit.AtfUnitTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@AtfUnitTest
class ReportEvidenceJsonFormatterTest {
    @Test
    void appliesTheSameLabelRedactionPrettyPrintAndDisplayLimitPolicy() {
        var body = new ReportJsonBody("REQUEST JSON", "{\"token\":\"raw-token\",\"name\":\"Ada\"}");

        String display = ReportEvidenceJsonFormatter.displayBody(
                body,
                new SensitiveDataRedactor(),
                new ReportingSettings(240, 500));

        assertAll(
                () -> assertTrue(ReportEvidenceJsonFormatter.label(body).equals("REQUEST JSON")),
                () -> assertTrue(display.contains("\"token\" : \"***REDACTED***\"")),
                () -> assertTrue(display.contains("\n  \"name\" : \"Ada\"")),
                () -> assertFalse(display.contains("raw-token")));
    }

    @Test
    void keepsTheDisplayLimitAfterFormatting() {
        String display = ReportEvidenceJsonFormatter.displayBody(
                new ReportJsonBody("RESPONSE JSON", "{\"long\":\"123456789\"}"),
                new SensitiveDataRedactor(),
                new ReportingSettings(240, 12));

        assertTrue(display.contains("… [truncated; showing first 12 of "));
    }

    @Test
    void zeroBodyLimitPublishesTheCompleteRedactedBody() {
        String display = ReportEvidenceJsonFormatter.displayBody(
                new ReportJsonBody("RESPONSE JSON", "{\"payload\":\"0123456789\"}"),
                new SensitiveDataRedactor(),
                new ReportingSettings(240, ReportingSettings.DISABLE_BODY_DISPLAY_LIMIT));

        assertTrue(display.contains("\"payload\" : \"0123456789\""));
        assertFalse(display.contains("truncated"));
    }
}
