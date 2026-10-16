package atf.api.tests.api.flows;

import atf.api.core.reporting.AtfReportStatus;
import atf.api.core.http.HttpMethod;
import atf.api.execution.routing.RoutePaths;
import atf.api.execution.data.JsonPayloads;
import atf.api.tests.support.junit.ApiResourceTestBase;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Multi-request API flows: mutate, read back, then validate each response. */
class ApiRequestFlowsTests extends ApiResourceTestBase {
    @Test
    void createsPostReadsItBackAndValidatesBothResponses() {
        var created = fixture().api().posts().create(ApiRequestFlowFixtures.CREATED_POST);
        var createdDecision = validate(operation(HttpMethod.POST, RoutePaths.POSTS, "flow create"),
                created, ApiRequestFlowFixtures.createdPostMutationContract());
        String createdId = ApiFlowAssertions.resourceId(created, "POST");

        var collection = fixture().api().posts().getCollection();
        var collectionDecision = validate(operation(HttpMethod.GET, RoutePaths.POSTS, "flow collection correlation"),
                collection, ApiRequestFlowFixtures.createdPostCollectionContract(createdId));
        var readBack = fixture().api().posts().get(Integer.parseInt(createdId));
        var readDecision = validate(operation(HttpMethod.GET, RoutePaths.POSTS + "/" + createdId, "flow read-back"),
                readBack, ApiRequestFlowFixtures.createdPostReadModelContract(createdId));
        ApiFlowAssertions.postReadBack(created, collection, readBack, null);

        assertAll(
                () -> assertTrue(JsonPayloads.write(ApiRequestFlowFixtures.CREATED_POST).startsWith("{")),
                () -> assertEquals(201, created.response().status()),
                () -> assertEquals(AtfReportStatus.PASS, collectionDecision.status()),
                () -> assertEquals(200, readBack.response().status()),
                () -> assertEquals(AtfReportStatus.PASS, createdDecision.status()),
                () -> assertEquals(AtfReportStatus.PASS, readDecision.status()));
    }

    @Test
    void updatesProfileReadsItBackAndValidatesBothResponses() {
        var updated = fixture().api().executor().execute(ApiRequestFlowFixtures.profileUpdateRequest());
        var updatedDecision = validate(operation(HttpMethod.PUT, RoutePaths.PROFILES + "/1", "flow update"),
                updated, ApiRequestFlowFixtures.updatedProfileMutationContract());

        var readBack = fixture().api().executor().execute(ApiRequestFlowFixtures.profileReadRequest());
        var readDecision = validate(operation(HttpMethod.GET, RoutePaths.PROFILES + "/1", "flow read-back"),
                readBack, ApiRequestFlowFixtures.profileReadModelContract());
        ApiFlowAssertions.profileReadBack(updated, readBack, null);

        assertAll(
                () -> assertEquals(200, updated.response().status()),
                () -> assertEquals(200, readBack.response().status()),
                () -> assertEquals(AtfReportStatus.PASS, updatedDecision.status()),
                () -> assertEquals(AtfReportStatus.PASS, readDecision.status()));
    }
}
