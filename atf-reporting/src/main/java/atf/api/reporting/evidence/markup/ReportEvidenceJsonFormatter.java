package atf.api.reporting.evidence.markup;

import atf.api.reporting.configuration.ReportingSettings;
import atf.api.reporting.markup.AtfReportLabels;
import atf.api.reporting.markup.ExtentReportNodeSupport;
import atf.api.reporting.model.ReportJsonBody;
import atf.api.reporting.model.ReportJsonBodyFormatter;
import atf.api.reporting.security.SensitiveDataRedactor;

import java.util.Objects;

/** Shared JSON-evidence policy used by Extent and fallback HTML adapters. */
public final class ReportEvidenceJsonFormatter {
    private ReportEvidenceJsonFormatter() {
    }

    public static String label(ReportJsonBody body) {
        return body == null || body.label().isBlank()
                ? AtfReportLabels.JSON_BODY
                : body.label();
    }

    public static String displayBody(ReportJsonBody body,
                                    SensitiveDataRedactor redactor,
                                    ReportingSettings settings) {
        Objects.requireNonNull(body, "body");
        Objects.requireNonNull(redactor, "redactor");
        Objects.requireNonNull(settings, "settings");
        String redacted = redactor.redactJson(body.body());
        return ExtentReportNodeSupport.displayBody(ReportJsonBodyFormatter.format(redacted), settings);
    }
}
