package atf.api.tests.api.reporting;

import atf.api.tests.api.lifecycle.LifecycleApiScenarios;
import atf.api.tests.api.posts.PostsApiScenarios;
import atf.api.tests.api.requests.RequestVariantsApiScenarios;
import atf.api.tests.api.headers.ApiHeadersAndEdgeCaseScenarios;
import atf.api.tests.api.users.UsersApiScenarios;
import atf.api.execution.ApiModule;
import atf.api.execution.ApiModuleFactory;
import atf.api.execution.lifecycle.ApiScenarioLifecycleRunner;
import atf.api.reporting.ExtentReportSession;
import atf.api.reporting.ReportArtifactInspector;
import atf.api.mock.AtfMockTransport;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Deterministic executable API suite used to inspect the public Extent tree.
 *
 * <p>The transport is injected at the composition root, while routes,
 * services, contracts, lifecycle and reporting are the production objects.
 * The resulting report intentionally contains the same three test-class
 * owners as the .NET acceptance suite: Users, Posts and lifecycle scenarios.
 * It is a report-producing acceptance test, not a synthetic HTML fixture.</p>
 */
class ApiAcceptanceReportTest {
    private static final URI BASE_URI = URI.create("https://atf.local");
    @Test
    void executesUsersPostsAndLifecycleScenariosIntoOneAuditableReport() throws Exception {
        Instant startedAt = Instant.now();
        Path report;
        ApiModule api = ApiModuleFactory.fromTransport(AtfMockTransport.fromProjectConfiguration(), BASE_URI);
        ApiScenarioLifecycleRunner lifecycle = new ApiScenarioLifecycleRunner();

        try (var session = ExtentReportSession.openConfigured()) {
            report = session.reportPath();
            UsersApiScenarios.publish(session, api, lifecycle);
            PostsApiScenarios.publish(session, api, lifecycle);
            RequestVariantsApiScenarios.publish(session, api, lifecycle);
            ApiHeadersAndEdgeCaseScenarios.publish(session, api, lifecycle);
            LifecycleApiScenarios.publish(session, api, lifecycle);
        }

        assertTrue(Files.isRegularFile(report), "Acceptance execution must publish an HTML report");
        String html = Files.readString(report);
        var inspection = ReportArtifactInspector.inspectFresh(report, startedAt);
        assertTrue(inspection.isCompliant(), () -> "Acceptance report violations: " + inspection.violations());
        assertAll(
                () -> assertTrue(html.contains("UsersApiTests")),
                () -> assertTrue(html.contains("PostsApiTests")),
                () -> assertTrue(html.contains("ApiLifecycleAndCombinationsTests")),
                () -> assertTrue(html.contains("RequestVariantsApiTests")),
                () -> assertTrue(html.contains("GET /public/v2/users")),
                () -> assertTrue(html.contains("GET /public/v2/posts")),
                () -> assertTrue(html.contains("POST /public/v2/users")),
                () -> assertTrue(html.contains("GET /public/v2/search")),
                () -> assertTrue(html.contains("POST /public/v2/bulk-import")),
                () -> assertTrue(html.contains("PUT /public/v2/profiles/{id}")),
                () -> assertTrue(html.contains("PATCH /public/v2/preferences")),
                () -> assertTrue(html.contains("ApiHeadersAndEdgeCasesTests")),
                () -> assertTrue(html.contains("atf.api.tests.api.headers")),
                () -> assertTrue(html.contains("atf.api.tests.api.users")),
                () -> assertTrue(html.contains("atf.api.tests.api.posts")),
                () -> assertTrue(html.contains("atf.api.tests.api.requests")),
                () -> assertTrue(html.contains("atf.api.tests.api.lifecycle")),
                () -> assertTrue(html.contains("GET /public/v2/catalog — request + response headers")),
                () -> assertTrue(html.contains("GET /public/v2/catalog/export — rate limit response")),
                () -> assertTrue(html.contains("GET /public/v2/catalog/42 — conditional 304")),
                () -> assertTrue(html.contains("<td>HTTP STATUS</td>")),
                () -> assertTrue(html.contains("AUTH TYPE")),
                () -> assertTrue(html.contains("QUERY PARAMS")),
                () -> assertTrue(html.contains("RESPONSE HEADERS")),
                () -> assertTrue(html.contains("X-RateLimit-Remaining")),
                () -> assertTrue(html.contains("Retry-After")),
                () -> assertTrue(html.contains("large-payload")),
                () -> assertTrue(html.contains("<td>response.url</td>")),
                () -> assertTrue(html.contains("https://atf.local/public/v2/users")),
                () -> assertTrue(html.contains("request-json-body")),
                () -> assertTrue(html.contains("response-json-body")),
                () -> assertTrue(html.contains("REQUEST JSON")),
                () -> assertTrue(html.contains("RESPONSE JSON")),
                () -> assertTrue(html.contains("\"source\" : \"intellij\"")),
                () -> assertTrue(html.contains("\"job\" : {")),
                () -> assertTrue(html.contains("\"accepted\" : true")),
                () -> assertTrue(html.contains("\"gender\" : \"male\"")),
                () -> assertTrue(html.contains("\"status\" : \"active\"")),
                () -> assertFalse(html.contains("ROUTE TEMPLATE")),
                () -> assertTrue(html.contains("TECHNICAL TRACE")),
                () -> assertTrue(html.contains("response.duration.ms")),
                () -> assertTrue(html.contains("Execution evidence · 1 attempts")),
                () -> assertTrue(html.contains("execution-table")),
                () -> assertTrue(html.contains("function normalizeEvidenceDisclosures()")),
                () -> assertTrue(html.contains("helm-validation-table")),
                () -> assertFalse(html.contains("[+] expand")),
                () -> assertFalse(html.contains("[-] collapse")),
                () -> assertTrue(html.contains("TEST BODY")),
                () -> assertTrue(html.contains("TEARDOWN")),
                () -> assertTrue(html.contains("PRIMARY: ")),
                () -> assertTrue(html.contains("SECONDARY: ")),
                () -> assertTrue(html.contains("teardown cleanup failed")),
                () -> assertFalse(html.contains("teardown-secret-token-1234567890")),
                () -> assertTrue(html.contains("Authorization: Bearer ***REDACTED***")),
                () -> assertTrue(html.contains("Users API — retry → body fail → teardown fail")),
                () -> assertTrue(html.contains("Execution evidence · 2 attempts")),
                () -> assertTrue(html.contains("retry.events")),
                () -> assertTrue(html.contains("retry teardown cleanup failed")),
                () -> assertFalse(html.contains("retry-secret-token-1234567890")),
                () -> assertMockRequestAndResponseBodiesAreSeparate(html),
                () -> assertBulkImportRequestAndResponseBodiesAreOrdered(html),
                () -> assertFailedLifecycleRequestRetainsEvidence(html),
                () -> assertRetryLifecycleEvidenceIsOrdered(html),
                () -> assertTrue(html.contains("id=\"nav-fails\"")),
                () -> assertFalse(html.contains("spark/")));
    }

    private static void assertFailedLifecycleRequestRetainsEvidence(String html) {
        int lifecycle = html.indexOf("Users API — setup → test → teardown");
        int request = html.indexOf("GET /public/v2/users/1", lifecycle);
        int requestDetails = html.indexOf("Request details · 1 fields", request);
        int responseDetails = responseDetailsIndexAfter(html, request);
        int responseJson = html.indexOf("RESPONSE JSON", request);
        int validation = html.indexOf("Contract validation", request);

        assertTrue(lifecycle >= 0 && request > lifecycle,
                "lifecycle scenario must contain its failed request");
        assertTrue(requestDetails > request && responseDetails > requestDetails && responseJson > responseDetails,
                "a failed API request must retain request context, response trace and response JSON in order");
        assertTrue(validation > responseJson,
                "validation must remain after the request-owned evidence");
    }

    private static void assertRetryLifecycleEvidenceIsOrdered(String html) {
        int scenario = html.indexOf("Users API — retry → body fail → teardown fail");
        int request = html.indexOf("GET /public/v2/users/1", scenario);
        int execution = html.indexOf("Execution evidence · 2 attempts", request);
        int retryEvents = html.indexOf("retry.events", execution);
        int validation = html.indexOf("Contract validation", request);
        int teardown = html.indexOf("retry teardown cleanup failed", validation);

        assertTrue(scenario >= 0 && request > scenario,
                "retry combination must own a concrete body request");
        assertTrue(execution > request && retryEvents > execution,
                "retry history must remain inside request-owned execution evidence");
        assertTrue(validation > retryEvents && teardown > validation,
                "validation must remain a request sibling before teardown cause");
    }

    private static void assertMockRequestAndResponseBodiesAreSeparate(String html) {
        int request = html.indexOf("POST /public/v2/users");
        int requestDetails = html.indexOf("Request details · 1 fields", request);
        int requestBody = html.indexOf("request-json-body", request);
        int responseDetails = responseDetailsIndexAfter(html, request);
        int responseBody = html.indexOf("response-json-body", request);
        int execution = html.indexOf("Execution evidence · 1 attempts", request);

        assertTrue(request >= 0 && requestDetails > request && requestBody > requestDetails
                        && responseDetails > requestBody && responseBody > responseDetails
                        && execution > responseBody,
                "mock request and response bodies must be ordered in separate expandable evidence sections");
    }

    private static void assertBulkImportRequestAndResponseBodiesAreOrdered(String html) {
        int request = html.indexOf("POST /public/v2/bulk-import");
        int requestDetails = html.indexOf("Request details · 5 fields", request);
        int requestBody = html.indexOf("request-json-body", request);
        int responseDetails = html.indexOf("Response details · 3 fields", request);
        int responseBody = html.indexOf("response-json-body", request);
        int execution = html.indexOf("Execution evidence · 1 attempts", request);

        assertTrue(request >= 0 && requestDetails > request && requestBody > requestDetails
                        && responseDetails > requestBody && responseBody > responseDetails
                        && execution > responseBody,
                "bulk-import request and response JSON must remain ordered inside their request node");
    }

    private static int responseDetailsIndexAfter(String html, int requestIndex) {
        int threeFields = html.indexOf("Response details · 3 fields", requestIndex);
        int twoFields = html.indexOf("Response details · 2 fields", requestIndex);
        if (threeFields < 0) {
            return twoFields;
        }
        if (twoFields < 0) {
            return threeFields;
        }
        return Math.min(threeFields, twoFields);
    }

}
