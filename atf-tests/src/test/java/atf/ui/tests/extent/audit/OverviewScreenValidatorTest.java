package atf.ui.tests.extent.audit;

import atf.api.core.reporting.AtfAssertion;
import atf.api.mock.AtfMockConfiguration;
import atf.ui.tests.extent.support.UiTestData;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OverviewScreenValidatorTest {
    @Test
    void buildsDynamicExpectationFromScreenTestsYamlFamiliesAndMockCatalog() {
        var tests = new OverviewScreenValidator.ScreenTestsSnapshot(
                5, 21, 21, List.of(
                        "PostsApiTests", "UsersApiTests", "RequestVariantsApiTests",
                        UiTestData.HEADERS_OWNER, UiTestData.LIFECYCLE_OWNER));

        var expected = OverviewScreenValidator.fromSetup(
                tests, List.of(UiTestData.POSTS_FAMILY, UiTestData.USERS_FAMILY,
                        UiTestData.REQUESTS_FAMILY, UiTestData.HEADERS_FAMILY, UiTestData.LIFECYCLE_FAMILY),
                AtfMockConfiguration.defaults());

        assertEquals(5, expected.configuredFamilies().size());
        assertEquals(5, expected.expectedOwnerNames().size());
        assertEquals(9, expected.mockRouteCount());
        assertTrue(expected.expectedOwnerNames().contains("UsersApiTests"));
    }

    @Test
    void resultIsReusableByAnyScreenTestWithoutReimplementingAssertions() {
        var tests = new OverviewScreenValidator.ScreenTestsSnapshot(
                2, 7, 9, List.of("UsersApiTests", "PostsApiTests"));
        var expected = OverviewScreenValidator.fromScreenTests(tests);
        var result = new OverviewScreenValidator.Result(
                new OverviewScreenValidator.Observation(9, 9, 2, 7, 7, 1, true),
                expected,
                List.of(new AtfAssertion("Overview contract", "valid", "valid", true)));

        assertTrue(result.valid());
        assertEquals("[]", result.failureSummary());
    }

    @Test
    void failureSummaryKeepsTheActualDynamicValueForExtentEvidence() {
        var expected = OverviewScreenValidator.fromScreenTests(
                new OverviewScreenValidator.ScreenTestsSnapshot(2, 7, 0, List.of()));
        var result = new OverviewScreenValidator.Result(
                new OverviewScreenValidator.Observation(8, 9, 1, 7, 6, 1, false),
                expected,
                List.of(new AtfAssertion("Overview sections", "9/9", "8/9", false)));

        assertFalse(result.valid());
        assertTrue(result.failureSummary().contains("Overview sections"));
        assertTrue(result.failureSummary().contains("8/9"));
    }
}
