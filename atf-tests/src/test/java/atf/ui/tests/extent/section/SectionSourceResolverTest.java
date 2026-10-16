package atf.ui.tests.extent.section;

import atf.api.ui.config.UiBrowserConfiguration;
import atf.api.ui.config.UiConfiguration;
import atf.api.ui.config.UiScreenValidationConfiguration;
import atf.ui.tests.extent.section.plan.ExtentSection;
import atf.ui.tests.extent.section.plan.ExtentSectionCheck;
import atf.ui.tests.extent.section.plan.SectionAuditSpec;
import atf.ui.tests.extent.section.plan.UiExtentReportSource;
import atf.ui.tests.extent.section.plan.UiExtentSectionAudit;
import atf.ui.tests.extent.section.source.ReportSourceResolver;
import atf.ui.tests.extent.section.source.ResolvedReportSource;
import atf.ui.tests.extent.support.ReportAuditInputKind;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SectionSourceResolverTest {
    @AfterEach
    void clearProperty() {
        System.clearProperty("atf.section.report");
    }

    @Test
    void staticPathIsConfiguredFromJvmOrUiConfigurationAndFingerprinted() throws Exception {
        Path report = Files.createTempFile("section-source-", ".html");
        Files.writeString(report, "<!doctype html><html><body>report</body></html>");
        UiConfiguration configuration = new UiConfiguration(
                UiBrowserConfiguration.defaults(), Files.createTempDirectory("section-output-"),
                "ui-", List.of("lifecycle"), UiScreenValidationConfiguration.defaults(),
                Optional.of(report), Map.of());
        SectionAuditSpec spec = spec(UiExtentReportSource.STATIC_PATH, "atf.ui.report");

        ResolvedReportSource resolved = ReportSourceResolver.resolveStaticSource(
                getClass(), configuration, spec);

        assertEquals(report.toAbsolutePath().normalize(), resolved.input().report());
        assertEquals(ReportAuditInputKind.EXISTING, resolved.input().kind());
        assertEquals(64, resolved.identity().sha256().length());
        assertTrue(resolved.readOnly());
    }

    @Test
    void malformedStaticReportFailsBeforeDebugUiStarts() throws Exception {
        Path report = Files.createTempFile("section-source-invalid-", ".html");
        Files.writeString(report, "<html><body>not a doctype report</body></html>");
        System.setProperty("atf.section.report", report.toString());

        assertThrows(IllegalArgumentException.class, () ->
                ReportSourceResolver.resolveStaticSource(getClass(), null,
                        spec(UiExtentReportSource.STATIC_PATH, "atf.section.report")));
    }

    @Test
    void generatedAnnotationRequiresExplicitApiSetup() {
        assertThrows(IllegalArgumentException.class,
                () -> SectionAuditSpec.resolve(MissingApiSetup.class));
    }

    private static SectionAuditSpec spec(UiExtentReportSource source, String property) {
        return new SectionAuditSpec("source-test", source, "", property,
                java.util.Set.of(ExtentSection.TESTS),
                java.util.Set.of(ExtentSectionCheck.STRUCTURE),
                atf.ui.tests.extent.section.plan.ExtentJourney.NONE);
    }

    @UiExtentSectionAudit(source = UiExtentReportSource.GENERATED_API,
            sections = {ExtentSection.TESTS})
    private static final class MissingApiSetup { }
}
