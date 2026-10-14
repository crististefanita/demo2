package atf.api.reporting;

import atf.api.core.reporting.AtfAssertion;
import atf.api.core.reporting.AtfReportStatus;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HtmlReportViewsTest {
    @Test
    void projectionsReadTheSameSemanticDocumentWithoutRebuildingVerdicts() {
        var document = new HtmlReportSemanticDocument();
        var scenario = document.startScenario("UsersTests", "user contract", List.of("api", "contract"));
        var phase = new HtmlReportSemanticDocument.PhaseModel("TEST BODY");
        var request = new HtmlReportSemanticDocument.RequestModel("item", "GET", "/users/1");
        request.status = AtfReportStatus.FAIL;
        var validation = new HtmlReportSemanticDocument.ValidationModel("Validate user");
        validation.assertions = List.of(new AtfAssertion("HTTP status", "201 Created", "200 OK", false));
        validation.status = AtfReportStatus.FAIL;
        phase.children.add(request);
        phase.children.add(validation);
        phase.status = AtfReportStatus.FAIL;
        scenario.phases.add(phase);
        scenario.status = AtfReportStatus.FAIL;

        var scenarios = document.scenarios();
        var tests = new HtmlReportTestsView().render(scenarios);
        var category = new HtmlReportCategoryView().render(scenarios);
        var fails = new HtmlReportFailsView().render(scenarios);
        var overview = new HtmlReportOverviewView().render(scenarios);

        assertAll(
                () -> assertTrue(tests.indexOf("class=\"request\"") < tests.indexOf("class=\"validation\"")),
                () -> assertTrue(category.contains("class=\"tag\">api</span>")),
                () -> assertTrue(fails.contains("HTTP status") && fails.contains("200 OK")),
                () -> assertTrue(overview.contains("<strong>1</strong>")));
    }
}
