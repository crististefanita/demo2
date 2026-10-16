package atf.ui.tests.extent.plan;

import atf.api.mock.AtfMockConfiguration;
import atf.api.tests.api.posts.PostsApiScenarios;
import atf.api.tests.api.users.UsersApiScenarios;
import atf.api.tests.support.ApiScenarioSelection;
import atf.ui.tests.extent.support.UiTestData;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExpectedReportProjectionTest {
    private static final List<String> USERS_POSTS_TITLES = List.of(
            UsersApiScenarios.GET_COLLECTION_TITLE, PostsApiScenarios.CREATE_POST_TITLE);
    @Test
    void buildsImmutableExpectedShapeBeforeDomInspection() {
        ApiScenarioManifest manifest = ApiScenarioManifest.from(
                List.of(UiTestData.USERS_FAMILY, UiTestData.POSTS_FAMILY), AtfMockConfiguration.defaults(), List.of(
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
        assertTrue(projection.scenarioTitles().contains(UsersApiScenarios.GET_COLLECTION_TITLE));
    }

    @Test
    void exactSelectionRejectsExtraOwnersAndScenarioTitles() {
        var projection = projection();
        var exact = snapshot(UiTestData.ApiFamily.USERS.ownerName(), UiTestData.ApiFamily.POSTS.ownerName(),
                USERS_POSTS_TITLES);

        assertTrue(projection.matches(exact));
        assertFalse(projection.matches(snapshot(
                List.of(UiTestData.USERS_OWNER, UiTestData.POSTS_OWNER, UiTestData.LIFECYCLE_OWNER),
                exact.scenarioTitles())));
        assertFalse(projection.matches(snapshot(exact.classNames(),
                List.of(UsersApiScenarios.GET_COLLECTION_TITLE, PostsApiScenarios.CREATE_POST_TITLE,
                        UsersApiScenarios.GET_COLLECTION_WARNING_TITLE))));
    }

    private static ExpectedReportProjection projection() {
        return new ExpectedReportProjection(
                List.of(new ExpectedReportProjection.Owner(UiTestData.USERS_OWNER, UiTestData.USERS_FAMILY),
                        new ExpectedReportProjection.Owner(UiTestData.POSTS_OWNER, UiTestData.POSTS_FAMILY)),
                USERS_POSTS_TITLES,
                2, 2, 2, List.of(), List.of("Tests"));
    }

    private static ReportAuditSnapshot.Tests snapshot(String firstOwner, String secondOwner,
                                                      List<String> titles) {
        return snapshot(List.of(firstOwner, secondOwner), titles);
    }

    private static ReportAuditSnapshot.Tests snapshot(List<String> owners, List<String> titles) {
        return new ReportAuditSnapshot.Tests(owners.size(), titles.size(), titles.size(),
                titles.size(), titles.size(), owners, titles);
    }
}
