package atf.ui.tests.extent.audit;

import atf.api.core.http.HttpMethod;
import atf.api.core.http.JsonBodyPolicy;
import org.junit.jupiter.api.Test;

import java.util.List;

import static atf.ui.tests.extent.audit.MockReportContractFixtures.ACCEPTED_STATUS;
import static atf.ui.tests.extent.audit.MockReportContractFixtures.BODY_MISMATCH;
import static atf.ui.tests.extent.audit.MockReportContractFixtures.EMPTY_OBJECT;
import static atf.ui.tests.extent.audit.MockReportContractFixtures.FALLBACK_STATUS;
import static atf.ui.tests.extent.audit.MockReportContractFixtures.OK_BODY;
import static atf.ui.tests.extent.audit.MockReportContractFixtures.OK_STATUS;
import static atf.ui.tests.extent.audit.MockReportContractFixtures.REQUEST_BODY;
import static atf.ui.tests.extent.audit.MockReportContractFixtures.ViolationSignal;
import static atf.ui.tests.extent.audit.MockReportContractFixtures.acceptedEvidence;
import static atf.ui.tests.extent.audit.MockReportContractFixtures.acceptedRenderedEvidence;
import static atf.ui.tests.extent.audit.MockReportContractFixtures.acceptedPostRoute;
import static atf.ui.tests.extent.audit.MockReportContractFixtures.bodyDriftGetRoute;
import static atf.ui.tests.extent.audit.MockReportContractFixtures.configuration;
import static atf.ui.tests.extent.audit.MockReportContractFixtures.emptyRenderedEvidence;
import static atf.ui.tests.extent.audit.MockReportContractFixtures.operation;
import static atf.ui.tests.extent.audit.MockReportContractFixtures.pagedUrl;
import static atf.ui.tests.extent.audit.MockReportContractFixtures.putRoute;
import static atf.ui.tests.extent.audit.MockReportContractFixtures.responseTreeEvidence;
import static atf.ui.tests.extent.audit.MockReportContractFixtures.responseTreePostRoute;
import static atf.ui.tests.extent.audit.MockReportContractFixtures.transportDriftEvidence;
import static atf.ui.tests.extent.audit.MockReportContractFixtures.transportGetRoute;
import static atf.ui.tests.extent.audit.MockReportContractFixtures.url;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Verifies that rendered API evidence reconciles with the mock contract. */
class MockReportContractAuditorTest {
    @Test
    void reconcilesRenderedRequestResponseAndExecutionMetadataWithTheMockRoute() {
        var result = new MockReportContractAuditor(configuration(acceptedPostRoute())).inspect(
                operation(HttpMethod.POST),
                MockReportContractFixtures.acceptedUrl(),
                ACCEPTED_STATUS,
                acceptedEvidence(),
                acceptedRenderedEvidence());

        assertTrue(result.compliant(), () -> result.violations().toString());
    }

    @Test
    void rejectsTransportMetadataDriftEvenWhenTheJsonBodiesStillMatch() {
        var result = new MockReportContractAuditor(configuration(transportGetRoute())).inspect(
                operation(HttpMethod.GET), pagedUrl(), OK_STATUS,
                new ApiJsonEvidenceAuditor.Result(JsonBodyPolicy.NULL_DOCUMENT, OK_BODY, List.of()),
                transportDriftEvidence());

        assertAll(
                () -> assertFalse(result.compliant()),
                () -> assertTrue(ViolationSignal.AUTH_PROFILE.occursIn(result.violations())),
                () -> assertTrue(ViolationSignal.QUERY.occursIn(result.violations())),
                () -> assertTrue(ViolationSignal.REQUEST_HEADER.occursIn(result.violations())),
                () -> assertTrue(ViolationSignal.RESPONSE_HEADER.occursIn(result.violations())),
                () -> assertTrue(ViolationSignal.EXECUTION_STATUS.occursIn(result.violations())));
    }

    @Test
    void comparesTheRenderedResponseAsAJsonTreeFromTheMockRoute() {
        var result = new MockReportContractAuditor(configuration(responseTreePostRoute()))
                .inspect(operation(HttpMethod.POST),
                        url(MockReportContractFixtures.DEMO_PATH),
                        ACCEPTED_STATUS, responseTreeEvidence());

        assertAll(
                () -> assertTrue(result.compliant(), () -> result.violations().toString()),
                () -> assertTrue(result.route().isPresent()));
    }

    @Test
    void reportsStatusAndBodyDriftWithoutHardcodedPayloadTokens() {
        var result = new MockReportContractAuditor(configuration(bodyDriftGetRoute()))
                .inspect(operation(HttpMethod.GET), url(MockReportContractFixtures.DEMO_PATH),
                        FALLBACK_STATUS,
                        new ApiJsonEvidenceAuditor.Result(JsonBodyPolicy.NULL_DOCUMENT,
                                BODY_MISMATCH, List.of()));

        assertAll(
                () -> assertFalse(result.compliant()),
                () -> assertTrue(ViolationSignal.EXPECTED_STATUS.occursIn(result.violations())),
                () -> assertTrue(ViolationSignal.BODY_DRIFT.occursIn(result.violations())));
    }

    @Test
    void requiresJsonRequestEvidenceForPostAndPutRoutes() {
        var auditor = new MockReportContractAuditor(configuration(putRoute()));
        var missingRequest = auditor.inspect(operation(HttpMethod.PUT),
                url(MockReportContractFixtures.DEMO_PATH), OK_STATUS,
                new ApiJsonEvidenceAuditor.Result(JsonBodyPolicy.NULL_DOCUMENT, OK_BODY, List.of()));
        var missingResponse = auditor.inspect(operation(HttpMethod.PUT),
                url(MockReportContractFixtures.DEMO_PATH), OK_STATUS,
                new ApiJsonEvidenceAuditor.Result(REQUEST_BODY, JsonBodyPolicy.NULL_DOCUMENT, List.of()));

        assertAll(
                () -> assertFalse(missingRequest.compliant()),
                () -> assertTrue(ViolationSignal.REQUEST_JSON_BODY
                        .occursIn(missingRequest.violations())),
                () -> assertFalse(missingResponse.compliant()),
                () -> assertTrue(ViolationSignal.RESPONSE_JSON_BODY
                        .occursIn(missingResponse.violations())));
    }

    @Test
    void rejectsReportRequestsThatCannotBeResolvedToTheConfiguredMockCatalog() {
        var result = new MockReportContractAuditor(configuration())
                .inspect(operation(HttpMethod.GET), url(MockReportContractFixtures.DEMO_PATH),
                        FALLBACK_STATUS,
                        new ApiJsonEvidenceAuditor.Result(JsonBodyPolicy.NULL_DOCUMENT, EMPTY_OBJECT, List.of()),
                        emptyRenderedEvidence());

        assertAll(
                () -> assertFalse(result.compliant()),
                () -> assertTrue(ViolationSignal.NO_RESPONSE_CONTRACT.occursIn(result.violations())));
    }
}
