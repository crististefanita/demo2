package atf.api.tests.api.requests;

import atf.api.core.http.ApiRequest;
import atf.api.core.http.ApiRoute;
import atf.api.core.http.AuthRequirement;
import atf.api.core.http.AuthType;

import java.util.List;
import java.util.Map;
import java.util.Set;

/** Request shapes shared by focused API tests and their Extent scenarios. */
final class RequestVariantRoutes {
    private static final ApiRoute SEARCH = route(
            "complex-search", "GET", "/public/v2/search", "read", "bearer", AuthType.BEARER,
            Set.of("api", "requests", "query", "bearer"));
    private static final ApiRoute BULK_IMPORT = route(
            "bulk-import", "POST", "/public/v2/bulk-import", "create", "api-key", AuthType.API_KEY,
            Set.of("api", "requests", "body", "api-key"));
    private static final ApiRoute PROFILE_UPDATE = route(
            "profile-update", "PUT", "/public/v2/profiles/{id}", "update", "basic", AuthType.BASIC,
            Set.of("api", "requests", "body", "basic"));
    private static final ApiRoute PREFERENCES = route(
            "preferences", "PATCH", "/public/v2/preferences", "update", "custom", AuthType.CUSTOM,
            Set.of("api", "requests", "query", "custom"));

    private RequestVariantRoutes() { }

    static ApiRequest searchRequest() {
        return SEARCH.request(Map.of(), Map.of("page", "2", "include", "details"),
                Map.of("Accept", "application/json"));
    }

    static ApiRequest bulkImportRequest() {
        return BULK_IMPORT.requestBody(bulkImportBody(), Map.of(), Map.of("mode", "validate"),
                Map.of("Content-Type", "application/json", "X-Client", "atf-java"));
    }

    static ApiRequest profileUpdateRequest() {
        return PROFILE_UPDATE.requestBody(profileUpdateBody(), Map.of("id", 1), Map.of("expand", "permissions"),
                Map.of("Content-Type", "application/json"));
    }

    static ApiRequest preferencesRequest() {
        return PREFERENCES.requestBody(preferencesBody(), Map.of(), Map.of("locale", "ro-RO"),
                Map.of("Content-Type", "application/json", "X-Atf-Client", "java"));
    }

    static String searchTitle() {
        return "GET /public/v2/search — query + bearer + complex response";
    }

    static String bulkImportTitle() {
        return "POST /public/v2/bulk-import — large body + api-key";
    }

    static String profileUpdateTitle() {
        return "PUT /public/v2/profiles/{id} — nested body + basic";
    }

    static String preferencesTitle() {
        return "PATCH /public/v2/preferences — custom auth + params";
    }

    static List<String> searchTags() {
        return List.of("api", "requests", "query", "bearer", "complex-body");
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
                .headerPresent("X-Mock-Scenario")
                .jsonPathEquals("$.meta.page", "2")
                .jsonPathExists("$.items[0].profile.department")
                .jsonPathExists("$.items[0].history[1].actor")
                .bodyContains("redactionApplied");
    }

    static atf.api.validation.contract.ResponseContract bulkImportContract() {
        return new atf.api.validation.contract.ResponseContract()
                .status(202)
                .jsonPathEquals("$.accepted", "true")
                .jsonPathEquals("$.summary.valid", "2")
                .jsonPathExists("$.items[2].warnings[0].code");
    }

    static atf.api.validation.contract.ResponseContract profileUpdateContract() {
        return new atf.api.validation.contract.ResponseContract()
                .status(200)
                .jsonPathEquals("$.profile.locale", "ro-RO")
                .jsonPathExists("$.permissions.inherited[0].permissions[0]")
                .bodyContains("changes");
    }

    static atf.api.validation.contract.ResponseContract preferencesContract() {
        return new atf.api.validation.contract.ResponseContract()
                .status(200)
                .jsonPathEquals("$.preferences.locale", "ro-RO")
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
                    {"externalId": "u-001", "profile": {"name": "ATF User", "email": "atf@example.test"}, "roles": ["reader", "writer"], "metadata": {"region": "eu", "labels": ["smoke", "contract"]}},
                    {"externalId": "u-002", "profile": {"name": "ATF Analyst", "email": "analyst@example.test"}, "roles": ["reader", "reviewer"], "metadata": {"region": "eu", "labels": ["regression", "nested"]}},
                    {"externalId": "u-003", "profile": {"name": "ATF Auditor", "email": "auditor@example.test"}, "roles": ["auditor"], "metadata": {"region": "ro", "labels": ["audit"], "flags": {"legacy": false, "verified": true}}}
                  ],
                  "audit": {"requestedBy": "atf-test", "reason": "complex payload contract", "correlationId": "corr-20261015-001"}
                }
                """;
    }

    static String profileUpdateBody() {
        return """
                {
                  "profile": {"displayName": "ATF User", "locale": "ro-RO", "timezone": "Europe/Bucharest", "contacts": {"email": true, "phone": false}},
                  "permissions": {"requested": ["read", "write"], "remove": ["export"], "inherit": true},
                  "preferences": {"theme": "dark", "density": "comfortable", "dashboard": {"widgets": ["health", "activity", "audit"]}},
                  "reason": "profile contract test"
                }
                """;
    }

    static String preferencesBody() {
        return """
                {
                  "locale": "ro-RO",
                  "theme": "dark",
                  "notifications": {"email": true, "sms": false, "digest": {"frequency": "daily", "hour": 9, "timezone": "Europe/Bucharest"}},
                  "columns": ["id", "name", "status", "updatedAt"],
                  "filters": {"status": ["active", "pending"], "owners": ["qa", "engineering"], "includeArchived": false}
                }
                """;
    }

    private static ApiRoute route(String name, String method, String path, String operation,
                                  String authProfile, AuthType authType, Set<String> tags) {
        return new ApiRoute(name, method, path, "request-variants", operation, authProfile,
                authType, AuthRequirement.REQUIRED, "required", tags);
    }
}
