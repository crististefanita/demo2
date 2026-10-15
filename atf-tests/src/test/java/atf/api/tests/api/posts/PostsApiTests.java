package atf.api.tests.api.posts;

import atf.api.core.reporting.AtfReportStatus;
import atf.api.tests.support.ApiResourceTestBase;
import atf.api.validation.contract.ContractValidators;
import atf.api.validation.contract.ResponseContract;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Readable post-resource API tests: collections, items and relationships. */
class PostsApiTests extends ApiResourceTestBase {

    @Test
    void getsPostCollectionAndValidatesItsContract() {
        var execution = fixture().api().posts().getCollection();
        var decision = validate("GET /public/v2/posts — collection contract", execution,
                ContractValidators.postCollection());

        assertAll(
                () -> assertEquals(200, execution.response().status()),
                () -> assertTrue(execution.response().body().contains("\"id\":101")),
                () -> assertEquals(AtfReportStatus.PASS, decision.status()));
    }

    @Test
    void getsPostByIdAndValidatesTheItemContract() {
        var execution = fixture().api().posts().get(101);
        var decision = validate("GET /public/v2/posts/101 — item contract", execution,
                ContractValidators.postItem());

        assertAll(
                () -> assertEquals(200, execution.response().status()),
                () -> assertTrue(execution.response().body().contains("\"user_id\":1")),
                () -> assertEquals(AtfReportStatus.PASS, decision.status()));
    }

    @Test
    void getsPostsForAUserAndValidatesTheRelationshipContract() {
        var execution = fixture().api().posts().forUser(1);
        var decision = validate("GET /public/v2/users/1/posts — relationship contract", execution,
                ContractValidators.relationshipCollection("1"));

        assertAll(
                () -> assertEquals(200, execution.response().status()),
                () -> assertTrue(execution.response().url().endsWith("/public/v2/users/1/posts")),
                () -> assertEquals(AtfReportStatus.PASS, decision.status()));
    }

    @Test
    void preservesNotFoundAsAnExplicitNegativeContract() {
        var execution = fixture().api().posts().get(999999);
        var decision = validate("GET /public/v2/posts/999999 — not found contract", execution,
                new ResponseContract().status(404)
                        .contentTypeContains("application/json")
                        .bodyContains("Not found"));

        assertAll(
                () -> assertEquals(404, execution.response().status()),
                () -> assertEquals(AtfReportStatus.PASS, decision.status()));
    }
}
