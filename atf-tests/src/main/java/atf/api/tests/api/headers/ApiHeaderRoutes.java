package atf.api.tests.api.headers;

import atf.api.core.http.ApiRequest;
import atf.api.core.http.ApiRoute;
import atf.api.core.http.AuthRequirement;
import atf.api.core.http.AuthType;

import java.util.List;
import java.util.Map;
import java.util.Set;

/** Request shapes for transport headers, caching and server-side limits. */
final class ApiHeaderRoutes {
    private static final ApiRoute CATALOG_PAGE = route(
            "catalog-page", "GET", "/public/v2/catalog", "read", "bearer", AuthType.BEARER,
            Set.of("api", "headers", "pagination", "response-headers"));
    private static final ApiRoute EXPORT = route(
            "catalog-export", "GET", "/public/v2/catalog/export", "read", "api-key", AuthType.API_KEY,
            Set.of("api", "headers", "rate-limit", "negative"));
    private static final ApiRoute NOT_MODIFIED = route(
            "catalog-cache", "GET", "/public/v2/catalog/42", "read", "custom", AuthType.CUSTOM,
            Set.of("api", "headers", "cache", "conditional"));

    private ApiHeaderRoutes() { }

    static ApiRequest catalogPageRequest() {
        return CATALOG_PAGE.request(Map.of(), Map.of("page", "3", "pageSize", "2"),
                Map.of("Accept", "application/json", "X-Correlation-Id", "corr-headers-001"));
    }

    static ApiRequest exportRequest() {
        return EXPORT.request(Map.of(), Map.of("format", "csv"),
                Map.of("Accept", "text/csv", "X-Client-Version", "2.4"));
    }

    static ApiRequest notModifiedRequest() {
        return NOT_MODIFIED.request(Map.of(), Map.of("include", "summary"),
                Map.of("Accept", "application/json", "If-None-Match", "W/catalog-42"));
    }

    static String catalogPageTitle() {
        return "GET /public/v2/catalog — request + response headers";
    }

    static String exportTitle() {
        return "GET /public/v2/catalog/export — rate limit response";
    }

    static String notModifiedTitle() {
        return "GET /public/v2/catalog/42 — conditional 304";
    }

    static List<String> catalogPageTags() {
        return List.of("api", "headers", "pagination", "response-headers");
    }

    static List<String> exportTags() {
        return List.of("api", "headers", "rate-limit", "negative");
    }

    static List<String> notModifiedTags() {
        return List.of("api", "headers", "cache", "conditional");
    }

    static atf.api.validation.contract.ResponseContract catalogPageContract() {
        return new atf.api.validation.contract.ResponseContract()
                .status(200)
                .headerPresent("X-Request-Id")
                .headerPresent("X-RateLimit-Remaining")
                .headerPresent("ETag")
                .jsonPathEquals("$.page", "3")
                .jsonPathExists("$.links.self");
    }

    static atf.api.validation.contract.ResponseContract exportContract() {
        return new atf.api.validation.contract.ResponseContract()
                .status(429)
                .headerPresent("Retry-After")
                .headerPresent("X-RateLimit-Remaining")
                .jsonPathEquals("$.error.code", "RATE_LIMITED")
                .bodyContains("Export quota exhausted");
    }

    static atf.api.validation.contract.ResponseContract notModifiedContract() {
        return new atf.api.validation.contract.ResponseContract()
                .status(304)
                .headerPresent("ETag")
                .headerPresent("Cache-Control");
    }

    private static ApiRoute route(String name, String method, String path, String operation,
                                  String authProfile, AuthType authType, Set<String> tags) {
        return new ApiRoute(name, method, path, "headers", operation, authProfile,
                authType, AuthRequirement.REQUIRED, "required", tags);
    }
}
