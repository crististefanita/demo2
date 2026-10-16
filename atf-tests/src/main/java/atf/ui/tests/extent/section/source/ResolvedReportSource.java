package atf.ui.tests.extent.section.source;

import atf.api.reporting.ReportArtifactIdentity;
import atf.ui.tests.extent.section.plan.UiExtentReportSource;
import atf.ui.tests.extent.support.ReportAuditInput;

import java.nio.file.Path;
import java.util.Objects;

/** Fingerprinted, immutable report input resolved before browser lifecycle. */
public record ResolvedReportSource(UiExtentReportSource source,
                                   ReportAuditInput input,
                                   ReportArtifactIdentity identity) {
    public ResolvedReportSource {
        source = Objects.requireNonNull(source, "source");
        input = Objects.requireNonNull(input, "input");
        identity = Objects.requireNonNull(identity, "identity");
        Path inputPath = input.report().toAbsolutePath().normalize();
        if (!inputPath.equals(identity.path())) {
            throw new IllegalArgumentException("Report identity path differs from input path");
        }
    }

    public boolean readOnly() {
        return true;
    }
}
