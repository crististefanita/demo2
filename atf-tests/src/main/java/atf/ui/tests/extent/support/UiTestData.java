package atf.ui.tests.extent.support;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

/** Shared typed data for configurable report UI tests. */
public final class UiTestData {
    public static final String API_FAMILIES_PROPERTY = "atf.ui.api.families";
    public static final String AUDIT_SUITE_PROPERTY = "atf.ui.audit.suite";
    public static final String AUDIT_RULES_PROPERTY = "atf.ui.audit.rules";
    public static final String AUDIT_DEPTH_PROPERTY = "atf.ui.audit.depth";
    public static final String USERS_FAMILY = "users";
    public static final String POSTS_FAMILY = "posts";
    public static final String REQUESTS_FAMILY = "requests";
    public static final String HEADERS_FAMILY = "headers";
    public static final String LIFECYCLE_FAMILY = "lifecycle";
    public static final String FLOWS_FAMILY = "flows";
    public static final String NEGATIVE_FAMILY = "negative";
    public static final String USERS_OWNER = "UsersApiTests";
    public static final String POSTS_OWNER = "PostsApiTests";
    public static final String REQUESTS_OWNER = "RequestVariantsApiTests";
    public static final String HEADERS_OWNER = "ApiHeadersAndEdgeCasesTests";
    public static final String LIFECYCLE_OWNER = "LifecycleApiScenarios";
    public static final String FLOWS_OWNER = "ApiRequestFlowsTests";
    public static final String NEGATIVE_OWNER = "ApiNegativeMatrixTests";
    public static final String TAG_UI = "ui";
    public static final String TAG_API = "api";
    public static final String TAG_MOCK_CONTRACT = "mock-contract";
    public static final String TAG_EXPLORATORY = "exploratory";
    public static final String TAG_REPORTING = "reporting";

    private UiTestData() { }

    public enum ApiFamily {
        USERS(USERS_FAMILY, USERS_OWNER),
        POSTS(POSTS_FAMILY, POSTS_OWNER),
        REQUESTS(REQUESTS_FAMILY, REQUESTS_OWNER),
        HEADERS(HEADERS_FAMILY, HEADERS_OWNER),
        FLOWS(FLOWS_FAMILY, FLOWS_OWNER),
        NEGATIVE(NEGATIVE_FAMILY, NEGATIVE_OWNER),
        LIFECYCLE(LIFECYCLE_FAMILY, LIFECYCLE_OWNER);

        private final String key;
        private final String ownerName;

        ApiFamily(String key, String ownerName) {
            this.key = key;
            this.ownerName = ownerName;
        }

        public String key() { return key; }

        public String ownerName() { return ownerName; }

        public static Optional<ApiFamily> fromKey(String value) {
            if (value == null || value.isBlank()) return Optional.empty();
            String normalized = value.trim().toLowerCase(Locale.ROOT);
            for (ApiFamily family : values()) {
                if (family.key.equals(normalized)) return Optional.of(family);
            }
            return Optional.empty();
        }
    }

    public static List<String> defaultTags() {
        return List.of(TAG_UI, TAG_API, TAG_MOCK_CONTRACT);
    }
}
