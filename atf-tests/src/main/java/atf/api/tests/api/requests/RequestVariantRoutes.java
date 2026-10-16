package atf.api.tests.api.requests;

import atf.api.core.http.ApiQueryParameter;
import atf.api.core.http.ApiQueryParameters;
import atf.api.core.http.ApiRequest;
import atf.api.core.http.ApiRoute;
import atf.api.core.http.AuthRequirement;
import atf.api.core.http.AuthType;
import atf.api.core.http.HttpHeader;
import atf.api.core.http.HttpMethod;
import atf.api.core.http.MediaTypes;
import atf.api.core.http.ApiOperationLabel;
import atf.api.execution.routing.RoutePaths;
import atf.api.tests.api.data.ApiFixtureData;

import java.util.List;
import java.util.Map;
import java.util.Set;

/** Request shapes shared by focused API tests and their Extent scenarios. */
final class RequestVariantRoutes {
    private static final ApiRoute SEARCH = route(
            "complex-search", HttpMethod.GET.value(), RoutePaths.SEARCH, "read", "bearer", AuthType.BEARER,
            Set.of("api", "requests", "query", "bearer"));
    private static final ApiRoute INVALID_SEARCH = route(
            "invalid-search-query", HttpMethod.GET.value(), RoutePaths.SEARCH, "read", "bearer", AuthType.BEARER,
            Set.of("api", "requests", "query", "negative"));
    private static final ApiRoute MISSING_PAGE_SEARCH = route(
            "missing-page-search", HttpMethod.GET.value(), RoutePaths.SEARCH, "read", "bearer", AuthType.BEARER,
            Set.of("api", "requests", "query", "missing"));
    private static final ApiRoute EMPTY_PAGE_SEARCH = route(
            "empty-page-search", HttpMethod.GET.value(), RoutePaths.SEARCH, "read", "bearer", AuthType.BEARER,
            Set.of("api", "requests", "query", "empty"));
    private static final ApiRoute DUPLICATE_TAG_SEARCH = route(
            "duplicate-tag-search", HttpMethod.GET.value(), RoutePaths.SEARCH, "read", "bearer", AuthType.BEARER,
            Set.of("api", "requests", "query", "duplicate"));
    private static final ApiRoute BULK_IMPORT = route(
            "bulk-import", HttpMethod.POST.value(), RoutePaths.BULK_IMPORT, "create", "api-key", AuthType.API_KEY,
            Set.of("api", "requests", "body", "api-key"));
    private static final ApiRoute PROFILE_UPDATE = route(
            "profile-update", HttpMethod.PUT.value(), RoutePaths.PROFILE_BY_ID, "update", "basic", AuthType.BASIC,
            Set.of("api", "requests", "body", "basic"));
    private static final ApiRoute PREFERENCES = route(
            "preferences", HttpMethod.PATCH.value(), RoutePaths.PREFERENCES, "update", "custom", AuthType.CUSTOM,
            Set.of("api", "requests", "query", "custom"));

    private RequestVariantRoutes() { }

    static ApiRequest searchRequest() {
        return SEARCH.request(Map.of(), Map.of("page", "2", "include", "details"),
                Map.of(HttpHeader.ACCEPT.value(), MediaTypes.APPLICATION_JSON));
    }

    static ApiRequest invalidSearchQueryRequest() {
        return INVALID_SEARCH.request(Map.of(), Map.of("page", "invalid", "include", "details"),
                Map.of(HttpHeader.ACCEPT.value(), MediaTypes.APPLICATION_JSON));
    }

    static ApiRequest missingPageSearchRequest() {
        return MISSING_PAGE_SEARCH.request(Map.of(),
                ApiQueryParameters.of(new ApiQueryParameter("include", "details")), searchHeaders());
    }

    static ApiRequest emptyPageSearchRequest() {
        return EMPTY_PAGE_SEARCH.request(Map.of(), ApiQueryParameters.of(
                new ApiQueryParameter("page", ""), new ApiQueryParameter("include", "details")), searchHeaders());
    }

    static ApiRequest duplicateTagSearchRequest() {
        return DUPLICATE_TAG_SEARCH.request(Map.of(), ApiQueryParameters.of(
                new ApiQueryParameter("tag", "quality"), new ApiQueryParameter("tag", "owner")), searchHeaders());
    }

    static ApiRequest bulkImportRequest() {
        return BULK_IMPORT.requestBody(bulkImportBody(), Map.of(), Map.of("mode", "validate"),
                Map.of(HttpHeader.CONTENT_TYPE.value(), MediaTypes.APPLICATION_JSON,
                        HttpHeader.X_CLIENT.value(), "atf-java"));
    }

    static ApiRequest profileUpdateRequest() {
        return PROFILE_UPDATE.requestBody(profileUpdateBody(), Map.of("id", ApiFixtureData.PROFILE_ID), Map.of("expand", "permissions"),
                Map.of(HttpHeader.CONTENT_TYPE.value(), MediaTypes.APPLICATION_JSON));
    }

    static ApiRequest preferencesRequest() {
        return PREFERENCES.requestBody(preferencesBody(), Map.of(), Map.of("locale", ApiFixtureData.LOCALE),
                Map.of(HttpHeader.CONTENT_TYPE.value(), MediaTypes.APPLICATION_JSON,
                        HttpHeader.X_ATF_CLIENT.value(), "java"));
    }

    static String searchTitle() {
        return ApiOperationLabel.format(HttpMethod.GET, RoutePaths.SEARCH, "query + bearer + complex response");
    }

    static String invalidSearchQueryTitle() {
        return ApiOperationLabel.format(HttpMethod.GET, RoutePaths.SEARCH, "invalid query contract");
    }

    static String missingPageSearchTitle() {
        return ApiOperationLabel.format(HttpMethod.GET, RoutePaths.SEARCH, "missing query parameter contract");
    }

    static String emptyPageSearchTitle() {
        return ApiOperationLabel.format(HttpMethod.GET, RoutePaths.SEARCH, "empty query parameter contract");
    }

    static String duplicateTagSearchTitle() {
        return ApiOperationLabel.format(HttpMethod.GET, RoutePaths.SEARCH, "duplicate query parameter contract");
    }

    static String bulkImportTitle() {
        return ApiOperationLabel.format(HttpMethod.POST, RoutePaths.BULK_IMPORT, "large body + api-key");
    }

    static String profileUpdateTitle() {
        return ApiOperationLabel.format(HttpMethod.PUT, RoutePaths.PROFILE_BY_ID, "nested body + basic");
    }

    static String preferencesTitle() {
        return ApiOperationLabel.format(HttpMethod.PATCH, RoutePaths.PREFERENCES, "custom auth + params");
    }

    static List<String> searchTags() {
        return List.of("api", "requests", "query", "bearer", "complex-body");
    }

    static List<String> invalidSearchQueryTags() {
        return List.of("api", "requests", "query", "negative");
    }

    static List<String> missingPageSearchTags() {
        return List.of("api", "requests", "query", "missing");
    }

    static List<String> emptyPageSearchTags() {
        return List.of("api", "requests", "query", "empty");
    }

    static List<String> duplicateTagSearchTags() {
        return List.of("api", "requests", "query", "duplicate");
    }

    static List<String> bulkImportTags() {
        return List.of("api", "requests", "body", "api-key", "large-payload");
    }

    static List<String> profileUpdateTags() {
        return List.of("api", "requests", "body", "basic", "nested-json");
    }

    static List<String> preferencesTags() {
        return List.of("api", "requests", "query", "custom", "parameters");
    }

    static atf.api.validation.contract.ResponseContract searchContract() {
        return new atf.api.validation.contract.ResponseContract()
                .status(200)
                .headerPresent(HttpHeader.X_MOCK_SCENARIO.value())
                .jsonPathType("$.meta", "object")
                .jsonPathEquals("$.meta.page", "2")
                .jsonPathType("$.items", "array")
                .jsonArraySize("$.items", 3)
                .jsonPathExists("$.items[0].profile.department")
                .jsonPathExists("$.items[0].history[1].actor")
                .jsonPathEquals("$.audit.redactionApplied", "true")
                .jsonPathAbsent("$.items[0].password");
    }

    static atf.api.validation.contract.ResponseContract invalidSearchQueryContract() {
        return new atf.api.validation.contract.ResponseContract()
                .status(400)
                .jsonPathType("$.error", "object")
                .jsonPathEquals("$.error.code", "INVALID_QUERY")
                .jsonPathEquals("$.error.parameter", "page")
                .jsonPathExists("$.error.message");
    }

    static atf.api.validation.contract.ResponseContract missingPageSearchContract() {
        return new atf.api.validation.contract.ResponseContract()
                .status(422)
                .jsonPathEquals("$.error.code", "MISSING_QUERY")
                .jsonPathEquals("$.error.parameter", "page")
                .jsonPathExists("$.error.message");
    }

    static atf.api.validation.contract.ResponseContract emptyPageSearchContract() {
        return new atf.api.validation.contract.ResponseContract()
                .status(422)
                .jsonPathEquals("$.error.code", "EMPTY_QUERY")
                .jsonPathEquals("$.error.parameter", "page")
                .jsonPathExists("$.error.message");
    }

    static atf.api.validation.contract.ResponseContract duplicateTagSearchContract() {
        return new atf.api.validation.contract.ResponseContract()
                .status(200)
                .jsonPathType("$.meta.tags", "array")
                .jsonArraySize("$.meta.tags", 2)
                .jsonPathEquals("$.meta.tags[0]", "quality")
                .jsonPathEquals("$.meta.tags[1]", "owner")
                .jsonPathEquals("$.audit.duplicateKeys", "true");
    }

    static atf.api.validation.contract.ResponseContract bulkImportContract() {
        return new atf.api.validation.contract.ResponseContract()
                .status(202)
                .jsonPathType("$.job", "object")
                .jsonPathEquals("$.accepted", "true")
                .jsonPathEquals("$.summary.valid", "2")
                .jsonArraySize("$.items", 3)
                .jsonPathExists("$.items[2].warnings[0].code");
    }

    static atf.api.validation.contract.ResponseContract profileUpdateContract() {
        return new atf.api.validation.contract.ResponseContract()
                .status(200)
                .jsonPathEquals("$.profile.locale", ApiFixtureData.LOCALE)
                .jsonPathExists("$.permissions.inherited[0].permissions[0]")
                .jsonPathType("$.changes", "array")
                .jsonArraySize("$.changes", 2);
    }

    static atf.api.validation.contract.ResponseContract preferencesContract() {
        return new atf.api.validation.contract.ResponseContract()
                .status(200)
                .jsonPathEquals("$.preferences.locale", ApiFixtureData.LOCALE)
                .jsonPathEquals("$.preferences.notifications.digest.frequency", "daily")
                .jsonPathExists("$.preferences.columns[3]");
    }

    static String bulkImportBody() {
        return """
                {
                  "source": "intellij",
                  "validateOnly": true,
                  "options": {"deduplicate": true, "notify": ["qa@example.test", "owner@example.test"], "mapping": {"legacyRole": "role", "legacyEmail": "email"}},
                  "records": [
                    {"externalId": "u-001", "profile": {"name": "%s", "email": "%s"}, "roles": ["reader", "writer"], "metadata": {"region": "eu", "labels": ["smoke", "contract"]}},
                    {"externalId": "u-002", "profile": {"name": "ATF Analyst", "email": "analyst@example.test"}, "roles": ["reader", "reviewer"], "metadata": {"region": "eu", "labels": ["regression", "nested"]}},
                    {"externalId": "u-003", "profile": {"name": "ATF Auditor", "email": "auditor@example.test"}, "roles": ["auditor"], "metadata": {"region": "ro", "labels": ["audit"], "flags": {"legacy": false, "verified": true}}}
                  ],
                  "audit": {"requestedBy": "atf-test", "reason": "complex payload contract", "correlationId": "corr-20261015-001"}
                }
                """.formatted(ApiFixtureData.USER_NAME, ApiFixtureData.USER_EMAIL);
    }

    static String profileUpdateBody() {
        return """
                {
                  "profile": {"displayName": "%s", "locale": "%s", "timezone": "%s", "contacts": {"email": true, "phone": false}},
                  "permissions": {"requested": ["read", "write"], "remove": ["export"], "inherit": true},
                  "preferences": {"theme": "dark", "density": "comfortable", "dashboard": {"widgets": ["health", "activity", "audit"]}},
                  "reason": "profile contract test"
                }
                """.formatted(ApiFixtureData.USER_NAME, ApiFixtureData.LOCALE, ApiFixtureData.TIMEZONE);
    }

    static String preferencesBody() {
        return """
                {
                  "locale": "%s",
                  "theme": "dark",
                  "notifications": {"email": true, "sms": false, "digest": {"frequency": "daily", "hour": 9, "timezone": "%s"}},
                  "columns": ["id", "name", "status", "updatedAt"],
                  "filters": {"status": ["active", "pending"], "owners": ["qa", "engineering"], "includeArchived": false}
                }
                """.formatted(ApiFixtureData.LOCALE, ApiFixtureData.TIMEZONE);
    }

    private static Map<String, String> searchHeaders() {
        return Map.of(HttpHeader.ACCEPT.value(), MediaTypes.APPLICATION_JSON);
    }

    private static ApiRoute route(String name, String method, String path, String operation,
                                  String authProfile, AuthType authType, Set<String> tags) {
        return new ApiRoute(name, method, path, "request-variants", operation, authProfile,
                authType, AuthRequirement.REQUIRED, "required", tags);
    }
}
