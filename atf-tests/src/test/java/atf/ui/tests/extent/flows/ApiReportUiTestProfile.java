package atf.ui.tests.extent.flows;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import atf.ui.tests.extent.support.UiTestData;

/**
 * Declarative profile for an API-to-UI report contract test.
 *
 * <p>The enum is the single source of truth for the catalog family key and
 * the owner name rendered by Extent. Adding another API family therefore
 * requires a profile entry, not a second copy of setup/report strings.</p>
 */
public final class ApiReportUiTestProfile {
    public static final String USERS_FAMILY = UiTestData.USERS_FAMILY;
    public static final String POSTS_FAMILY = UiTestData.POSTS_FAMILY;
    public static final String REQUESTS_FAMILY = UiTestData.REQUESTS_FAMILY;
    public static final String HEADERS_FAMILY = UiTestData.HEADERS_FAMILY;
    public static final String LIFECYCLE_FAMILY = UiTestData.LIFECYCLE_FAMILY;

    public static final ApiReportUiTestProfile USERS_POSTS = profile(
            "users-posts",
            "UsersApiTests and PostsApiTests mock-backed API evidence",
            UiTestData.ApiFamily.USERS, UiTestData.ApiFamily.POSTS);

    public static final ApiReportUiTestProfile REQUESTS_HEADERS = profile(
            "requests-headers",
            "RequestVariantsApiTests and ApiHeadersAndEdgeCasesTests mock-backed API evidence",
            UiTestData.ApiFamily.REQUESTS, UiTestData.ApiFamily.HEADERS);

    private final String reportKey;
    private final String auditTitle;
    private final List<UiTestData.ApiFamily> families;
    private final List<String> tags;

    private ApiReportUiTestProfile(String reportKey, String auditTitle,
                                   List<UiTestData.ApiFamily> families) {
        this.reportKey = Objects.requireNonNull(reportKey, "reportKey");
        this.auditTitle = Objects.requireNonNull(auditTitle, "auditTitle");
        this.families = List.copyOf(Objects.requireNonNull(families, "families"));
        List<String> resolvedTags = new ArrayList<>(List.of(UiTestData.TAG_UI, UiTestData.TAG_API));
        resolvedTags.addAll(this.families.stream().map(UiTestData.ApiFamily::key).toList());
        resolvedTags.add(UiTestData.TAG_MOCK_CONTRACT);
        this.tags = List.copyOf(resolvedTags);
    }

    private static ApiReportUiTestProfile profile(String reportKey, String auditTitle,
                                                  UiTestData.ApiFamily... families) {
        return new ApiReportUiTestProfile(reportKey, auditTitle, Arrays.asList(families));
    }

    String reportKey() {
        return reportKey;
    }

    String auditTitle() {
        return auditTitle;
    }

    List<UiTestData.ApiFamily> families() {
        return families;
    }

    List<String> tags() {
        return tags;
    }

    List<String> ownerNames() {
        return families.stream().map(UiTestData.ApiFamily::ownerName).toList();
    }

    List<String> scenarioTitles() {
        return families.stream().flatMap(family -> family.scenarioTitles().stream()).toList();
    }

    String setupDescription() {
        return "the " + String.join(" and ", ownerNames())
                + " API setup report is loaded read-only";
    }

    String failureMessage() {
        return auditTitle + " failed";
    }

    String screenTitle(String ownerName) {
        return ownerName + " matches mock JSON contracts";
    }

    String reportLabel() {
        return "API Extent report";
    }

}
