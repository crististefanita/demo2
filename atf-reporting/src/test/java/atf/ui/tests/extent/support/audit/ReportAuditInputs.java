package atf.ui.tests.extent.support.audit;

import atf.api.reporting.ui.audit.input.ReportAuditInput;
import atf.ui.tests.extent.support.config.UiConfiguration;

import java.nio.file.Path;
import java.util.Objects;

/** Named input sources keep report origin policy out of exploratory test flows. */
public final class ReportAuditInputs {
    private ReportAuditInputs() {
    }

    public static ReportAuditInputSource existing() {
        return configuration -> {
            Objects.requireNonNull(configuration, "configuration");
            Path report = configuration.auditInputReport().orElseThrow(() ->
                    new IllegalStateException("No existing Extent report configured; set -Datf.ui.report "
                            + "or ui.audit.inputReport in atf.yaml"));
            return ReportAuditInput.existing(report);
        };
    }

    public static ReportAuditInputSource generated(Path report) {
        ReportAuditInput input = ReportAuditInput.apiGenerated(report);
        return configuration -> {
            Objects.requireNonNull(configuration, "configuration");
            return input;
        };
    }
}
