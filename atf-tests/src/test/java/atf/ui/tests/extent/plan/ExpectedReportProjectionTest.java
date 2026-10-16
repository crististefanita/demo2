package atf.ui.tests.extent.plan;

import atf.api.mock.AtfMockConfiguration;
import atf.api.tests.api.posts.PostsApiScenarios;
import atf.api.tests.api.users.UsersApiScenarios;
import atf.api.tests.support.ApiScenarioSelection;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExpectedReportProjectionTest {
    @Test
    void buildsImmutableExpectedShapeBeforeDomInspection() {
        ApiScenarioManifest manifest = ApiScenarioManifest.from(
                List.of("users", "posts"), AtfMockConfiguration.defaults(), List.of(
                        new ApiScenarioSelection(UsersApiScenarios.class,
                                UsersApiScenarios.GET_COLLECTION, "users", UsersApiScenarios.REPORT_OWNER,
                                UsersApiScenarios.scenarioTitle(UsersApiScenarios.GET_COLLECTION)),
                        new ApiScenarioSelection(PostsApiScenarios.class,
                                PostsApiScenarios.CREATE_POST, "posts", PostsApiScenarios.REPORT_OWNER,
                                PostsApiScenarios.scenarioTitle(PostsApiScenarios.CREATE_POST))));

        ExpectedReportProjection projection = manifest.expectedProjection();

        assertEquals(2, projection.scenarioCount());
        assertEquals(List.of("Tests", "Overview", "Categories", "Fails"), projection.tabs());
        assertTrue(projection.requiredEvidence().contains("Response details"));
        assertTrue(projection.scenarioTitles().get(0).contains("GET /public/v2/users"));
    }

    @Test
    void exactSelectionRejectsExtraOwnersAndScenarioTitles() {
        var projection = projection();
        var exact = snapshot(List.of("UsersApiTests", "PostsApiTests"),
                List.of("GET /public/v2/users — collection contract",
                        "POST /public/v2/posts — create contract"));

        assertTrue(projection.matches(exact));
        assertFalse(projection.matches(snapshot(
                List.of("UsersApiTests", "PostsApiTests", "LifecycleApiScenarios"),
                exact.scenarioTitles())));
        assertFalse(projection.matches(snapshot(exact.classNames(),
                List.of("GET /public/v2/users — collection contract",
                        "POST /public/v2/posts — create contract",
                        "GET /public/v2/users — unexpected extra"))));
    }

    private static ExpectedReportProjection projection() {
        return new ExpectedReportProjection(
                List.of(new ExpectedReportProjection.Owner("UsersApiTests", "users"),
                        new ExpectedReportProjection.Owner("PostsApiTests", "posts")),
                List.of("GET /public/v2/users — collection contract",
                        "POST /public/v2/posts — create contract"),
                2, 2, 2, List.of(), List.of("Tests"));
    }

    private static ReportAuditSnapshot.Tests snapshot(List<String> owners, List<String> titles) {
        return new ReportAuditSnapshot.Tests(owners.size(), titles.size(), titles.size(),
                titles.size(), titles.size(), owners, titles);
    }
}
