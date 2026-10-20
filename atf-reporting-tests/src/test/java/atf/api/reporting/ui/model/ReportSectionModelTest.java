package atf.api.reporting.ui.model;
import atf.api.testkit.AtfUnitTest;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@AtfUnitTest
class ReportSectionModelTest {
    @Test
    void defaultExtentModelCoversTabsAndEvidenceSurfaces() {
        String html = "id=\"dashboard-view\" id=\"test-view\" id=\"category-view\" "
                + "id=\"atf-fails-view\" data-atf-evidence-type=\"report-setup\" "
                + "data-atf-evidence-type=\"screen-validation\"";

        assertAll(
                () -> assertTrue(ReportSectionModel.extentUiDefaults().stream()
                        .allMatch(section -> section.isPresentIn(html))),
                () -> assertTrue(ReportSectionModel.Section.SCREEN_VALIDATION.displayName()
                        .contains("Screen")),
                () -> assertEquals("id=\"test-view\"", ReportSectionMarker.id(
                        atf.api.reporting.markup.vocabulary.AtfReportHtmlVocabulary.ViewId.TESTS)
                        .token()),
                () -> assertEquals("data-atf-evidence-type=\"screen-validation\"",
                        ReportSectionMarker.attribute(
                        atf.api.reporting.markup.vocabulary.AtfReportHtmlVocabulary.Attribute.EVIDENCE_TYPE,
                        "screen-validation").token()),
                () -> assertTrue(ReportSectionModel.Section.TESTS.viewId()
                        == atf.api.reporting.markup.vocabulary.AtfReportHtmlVocabulary.ViewId.TESTS),
                () -> assertFalse(new ReportSectionModel(
                        ReportSectionModel.Section.TESTS, "Tests", java.util.List.of(
                                java.util.List.of(ReportSectionMarker.text("missing"))))
                        .isPresentIn(html)));
    }
}
