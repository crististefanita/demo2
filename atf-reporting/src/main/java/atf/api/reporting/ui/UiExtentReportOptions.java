package atf.api.reporting.ui;

import atf.api.reporting.console.AtfConsoleSink;
import atf.api.reporting.evidence.profile.ReportEvidenceProfile;

import java.nio.file.Path;
import java.util.Objects;

/** Typed composition options for the canonical UI report session entry point. */
public record UiExtentReportOptions(Path outputDirectory,
                                    String fileName,
                                    AtfConsoleSink console,
                                    ReportEvidenceProfile evidenceProfile,
                                    boolean timestampEnabled) {
    public UiExtentReportOptions {
        outputDirectory = Objects.requireNonNull(outputDirectory, "outputDirectory");
        if (fileName == null || fileName.isBlank()) {
            throw new IllegalArgumentException("fileName must not be blank");
        }
        console = Objects.requireNonNull(console, "console");
        evidenceProfile = Objects.requireNonNull(evidenceProfile, "evidenceProfile");
    }

    public static Builder builder(Path outputDirectory) {
        return new Builder(outputDirectory);
    }

    /** Small builder for consumers that need only one or two overrides. */
    public static final class Builder {
        private final Path outputDirectory;
        private String fileName = UiExtentReportSession.DEFAULT_FILE_NAME;
        private AtfConsoleSink console = AtfConsoleSink.noOp();
        private ReportEvidenceProfile evidenceProfile = ReportEvidenceProfile.defaults();
        private boolean timestampEnabled = true;

        private Builder(Path outputDirectory) {
            this.outputDirectory = Objects.requireNonNull(outputDirectory, "outputDirectory");
        }

        public Builder fileName(String value) {
            fileName = value;
            return this;
        }

        public Builder console(AtfConsoleSink value) {
            console = value;
            return this;
        }

        public Builder evidenceProfile(ReportEvidenceProfile value) {
            evidenceProfile = value;
            return this;
        }

        public Builder timestampEnabled(boolean value) {
            timestampEnabled = value;
            return this;
        }

        public UiExtentReportOptions build() {
            return new UiExtentReportOptions(outputDirectory, fileName, console,
                    evidenceProfile, timestampEnabled);
        }
    }
}
