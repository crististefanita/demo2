package atf.api.reporting.html;

import atf.api.testkit.AtfUnitTest;
import atf.api.reporting.contract.AtfReportStatus;
import atf.api.reporting.support.ApiReportTestUrls;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

@AtfUnitTest
class HtmlReportSemanticSnapshotMapperTest {

    @Test
    void mapsLifecycleModelsToAnImmutableRendererTreeWithoutRendererKnowledge() {
        var document = new HtmlReportSemanticDocument();
        var scenario = document.startScenario("UsersTests", "read user", List.of("api"));
        var phase = scenario.addPhase("TEST BODY");
        var request = phase.addRequest("GET user", "GET", ApiReportTestUrls.GENERIC_USER_ONE);
        request.addDetail("response.status", "200");
        var validation = request.addValidation("Validate response");
        validation.complete(AtfReportStatus.PASS);
        request.complete(AtfReportStatus.PASS);
        phase.complete(AtfReportStatus.PASS);
        scenario.complete(AtfReportStatus.PASS);

        var snapshot = HtmlReportSemanticSnapshotMapper.map(document.scenarios());
        var mappedRequest = assertInstanceOf(
                atf.api.reporting.model.ReportExecutionModel.Request.class,
                snapshot.scenarios().get(0).phases().get(0).children().get(0));

        assertAll(
                () -> assertEquals("UsersTests", snapshot.scenarios().get(0).testClass()),
                () -> assertEquals("response.status", mappedRequest.details().get(0).name()),
                () -> assertEquals("Validate response",
                        ((atf.api.reporting.model.ReportExecutionModel.Validation)
                                mappedRequest.children().get(0)).title()),
                () -> assertEquals(AtfReportStatus.PASS, snapshot.scenarios().get(0).status())
        );
    }
}
