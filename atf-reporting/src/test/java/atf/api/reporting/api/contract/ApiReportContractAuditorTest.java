package atf.api.reporting.api.contract;
import atf.api.testkit.AtfComponentTest;

import atf.api.core.http.request.HttpMethod;
import atf.api.reporting.support.MockApiReportContractFixtures.ViolationSignal;
import atf.api.mock.MockRoute;
import org.junit.jupiter.api.Test;

import java.util.List;

import static atf.api.reporting.support.MockApiReportContractFixtures.OK_BODY;
import static atf.api.reporting.support.MockApiReportContractFixtures.REQUEST_BODY;
import static atf.api.reporting.support.MockApiReportContractFixtures.acceptedInput;
import static atf.api.reporting.support.MockApiReportContractFixtures.bodyDriftInput;
import static atf.api.reporting.support.MockApiReportContractFixtures.putInput;
import static atf.api.reporting.support.MockApiReportContractFixtures.responseTreeInput;
import static atf.api.reporting.support.MockApiReportContractFixtures.responseTreePostRoute;
import static atf.api.reporting.support.MockApiReportContractFixtures.transportDriftInput;
import static atf.api.reporting.support.MockApiReportContractFixtures.transportGetRoute;
import static atf.api.reporting.support.MockApiReportContractFixtures.acceptedPostRoute;
import static atf.api.reporting.support.MockApiReportContractFixtures.bodyDriftGetRoute;
import static atf.api.reporting.support.MockApiReportContractFixtures.putRoute;
import static atf.api.reporting.support.MockApiReportContractFixtures.unresolvedInput;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Verifies the reporting module directly against the mock catalog port. */
@AtfComponentTest
class ApiReportContractAuditorTest {
    @Test
    void reconcilesRenderedRequestResponseAndExecutionMetadataWithMockCatalog() {
        var result = new ApiReportContractAuditor(catalog(acceptedPostRoute())).inspect(acceptedInput());

        assertTrue(result.compliant(), () -> result.violations().toString());
    }

    @Test
    void rejectsTransportMetadataDriftEvenWhenJsonBodiesStillMatch() {
        var result = new ApiReportContractAuditor(catalog(transportGetRoute())).inspect(transportDriftInput());

        assertAll(
                () -> assertFalse(result.compliant()),
                () -> assertTrue(ViolationSignal.AUTH_PROFILE.occursIn(result.violations())),
                () -> assertTrue(ViolationSignal.QUERY.occursIn(result.violations())),
                () -> assertTrue(ViolationSignal.REQUEST_HEADER.occursIn(result.violations())),
                () -> assertTrue(ViolationSignal.RESPONSE_HEADER.occursIn(result.violations())),
                () -> assertTrue(ViolationSignal.EXECUTION_STATUS.occursIn(result.violations())));
    }

    @Test
    void comparesResponseAsJsonTreeIndependentOfFormattingAndFieldOrder() {
        var result = new ApiReportContractAuditor(catalog(responseTreePostRoute())).inspect(responseTreeInput());

        assertAll(
                () -> assertTrue(result.compliant(), () -> result.violations().toString()),
                () -> assertTrue(result.route().isPresent()));
    }

    @Test
    void reportsStatusAndBodyDriftWithoutHardcodedPayloadTokens() {
        var result = new ApiReportContractAuditor(catalog(bodyDriftGetRoute())).inspect(bodyDriftInput());

        assertAll(
                () -> assertFalse(result.compliant()),
                () -> assertTrue(ViolationSignal.EXPECTED_STATUS.occursIn(result.violations())),
                () -> assertTrue(ViolationSignal.BODY_DRIFT.occursIn(result.violations())));
    }

    @Test
    void requiresJsonRequestAndResponseEvidenceForPutRoutes() {
        var auditor = new ApiReportContractAuditor(catalog(putRoute()));
        var missingRequest = auditor.inspect(putInput("null", OK_BODY));
        var missingResponse = auditor.inspect(putInput(REQUEST_BODY, "null"));

        assertAll(
                () -> assertFalse(missingRequest.compliant()),
                () -> assertTrue(ViolationSignal.REQUEST_JSON_BODY.occursIn(missingRequest.violations())),
                () -> assertFalse(missingResponse.compliant()),
                () -> assertTrue(ViolationSignal.RESPONSE_JSON_BODY.occursIn(missingResponse.violations())));
    }

    @Test
    void rejectsRequestsThatCannotBeResolvedToConfiguredCatalog() {
        var result = new ApiReportContractAuditor(catalog()).inspect(unresolvedInput());

        assertAll(
                () -> assertFalse(result.compliant()),
                () -> assertTrue(ViolationSignal.NO_RESPONSE_CONTRACT.occursIn(result.violations())));
    }

    private static ApiReportContractCatalog catalog(MockRoute... routes) {
        return () -> List.of(routes).stream()
                .map(route -> new ApiReportRouteContract(route.method(), route.path(), route.status(),
                        route.body(), route.headers(), route.authProfile(), route.queryParameters(),
                        route.requiredHeaders(), route.requestBodyMode(), route.expectedRequestBody()))
                .toList();
    }
}
