package atf.api.tests;

import atf.api.core.http.ApiExecutionResult;
import atf.api.core.http.ApiRequest;
import atf.api.core.http.ApiResponse;
import atf.api.core.http.ApiTransport;
import atf.api.core.reporting.AtfReportPorts;
import atf.api.core.reporting.AtfReportStatus;
import atf.api.execution.ApiModule;
import atf.api.execution.ApiModuleFactory;
import atf.api.execution.lifecycle.ApiScenarioLifecycleRunner;
import atf.api.execution.lifecycle.ApiScenarioOutcome;
import atf.api.execution.model.UserRequest;
import atf.api.validation.contract.ContractValidators;
import atf.api.validation.contract.ResponseContract;
import atf.api.validation.publishing.ResponseContractReporter;
import atf.api.reporting.ExtentReportSession;
import atf.api.reporting.ReportArtifactInspector;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
    private static final ResponseContractReporter VALIDATOR = new ResponseContractReporter();

    @Test
    void executesUsersPostsAndLifecycleScenariosIntoOneAuditableReport() throws Exception {
        Instant startedAt = Instant.now();
        Path report = TestReportPaths.timestampedReport("api-atf-java-report.html");
        ApiModule api = ApiModuleFactory.fromTransport(new AcceptanceTransport(), BASE_URI);
        ApiScenarioLifecycleRunner lifecycle = new ApiScenarioLifecycleRunner();

        try (var session = new ExtentReportSession(report)) {
            publishUsers(session, api, lifecycle);
            publishPosts(session, api, lifecycle);
            publishLifecycle(session, api, lifecycle);
        }

        assertTrue(Files.isRegularFile(report), "Acceptance execution must publish an HTML report");
        String html = Files.readString(report);
        var inspection = ReportArtifactInspector.inspectFresh(report, startedAt);
        assertTrue(inspection.isCompliant(), () -> "Acceptance report violations: " + inspection.violations());
        assertAll(
                () -> assertTrue(html.contains("UsersApiTests")),
                () -> assertTrue(html.contains("PostsApiTests")),
                () -> assertTrue(html.contains("ApiLifecycleAndCombinationsTests")),
                () -> assertTrue(html.contains("GET /public/v2/users")),
                () -> assertTrue(html.contains("GET /public/v2/posts")),
                () -> assertTrue(html.contains("POST /public/v2/users")),
                () -> assertTrue(html.contains("<td>URL</td>")),
                () -> assertTrue(html.contains("https://atf.local/public/v2/users")),
                () -> assertFalse(html.contains("ROUTE TEMPLATE")),
                () -> assertTrue(html.contains("TECHNICAL TRACE")),
                () -> assertTrue(html.contains("RESPONSE DURATION (ms)")),
                () -> assertTrue(html.contains("function normalizeEvidenceDisclosures()")),
                () -> assertTrue(html.contains("helm-validation-table")),
                () -> assertFalse(html.contains("[+] expand")),
                () -> assertFalse(html.contains("[-] collapse")),
                () -> assertTrue(html.contains("TEST BODY")),
                () -> assertTrue(html.contains("TEARDOWN")),
                () -> assertTrue(html.contains("id=\"nav-fails\"")),
                () -> assertFalse(html.contains("spark/")));
    }

    private static void publishUsers(AtfReportPorts.Session session, ApiModule api,
                                     ApiScenarioLifecycleRunner lifecycle) {
        run(lifecycle, session, ApiScenarioPlanBuilder.scenario(
                "UsersApiTests", "GET /public/v2/users — collection contract",
                List.of("api", "users", "contract", "smoke")),
                phase -> publish(phase, "GET /public/v2/users", api.users().getCollection(),
                        ContractValidators.userCollection(), false, null));

        run(lifecycle, session, ApiScenarioPlanBuilder.scenario(
                "UsersApiTests", "GET /public/v2/users/{id} — item contract",
                List.of("api", "users", "contract")),
                phase -> publish(phase, "GET /public/v2/users/1", api.users().get(1),
                        ContractValidators.userItem(), false, null));

        run(lifecycle, session, ApiScenarioPlanBuilder.scenario(
                "UsersApiTests", "POST /public/v2/users — missing authentication",
                List.of("api", "users", "negative", "auth")),
                phase -> {
                    var payload = new UserRequest("ATF user", "atf@example.test", "male", "active");
                    publish(phase, "POST /public/v2/users", api.users().create(payload),
                            new ResponseContract().status(401).contentTypeContains("application/json")
                                    .bodyContains("Authentication required"), false,
                            "{\"name\":\"ATF user\",\"email\":\"atf@example.test\"}");
                });

        run(lifecycle, session, ApiScenarioPlanBuilder.scenario(
                "UsersApiTests", "GET /public/v2/users — soft contract warning",
                List.of("api", "users", "reporting", "soft-assert")),
                phase -> publish(phase, "GET /public/v2/users", api.users().getCollection(),
                        new ResponseContract().status(201).contentTypeContains("application/json"), true, null));
    }

    private static void publishPosts(AtfReportPorts.Session session, ApiModule api,
                                     ApiScenarioLifecycleRunner lifecycle) {
        run(lifecycle, session, ApiScenarioPlanBuilder.scenario(
                "PostsApiTests", "GET /public/v2/posts — collection contract",
                List.of("api", "posts", "contract")),
                phase -> publish(phase, "GET /public/v2/posts", api.posts().getCollection(),
                        ContractValidators.postCollection(), false, null));

        run(lifecycle, session, ApiScenarioPlanBuilder.scenario(
                "PostsApiTests", "GET /public/v2/posts/{id} — item contract",
                List.of("api", "posts", "contract")),
                phase -> publish(phase, "GET /public/v2/posts/101", api.posts().get(101),
                        ContractValidators.postItem(), false, null));

        run(lifecycle, session, ApiScenarioPlanBuilder.scenario(
                "PostsApiTests", "GET /public/v2/users/{userId}/posts — relationship contract",
                List.of("api", "posts", "relationship", "contract")),
                phase -> publish(phase, "GET /public/v2/users/1/posts", api.posts().forUser(1),
                        ContractValidators.relationshipCollection("1"), false, null));

        run(lifecycle, session, ApiScenarioPlanBuilder.scenario(
                "PostsApiTests", "GET /public/v2/posts/{unknownId} — not found contract",
                List.of("api", "posts", "negative", "contract")),
                phase -> publish(phase, "GET /public/v2/posts/999999", api.posts().get(999999),
                        new ResponseContract().status(404).contentTypeContains("application/json")
                                .bodyContains("Not found"), false, null));
    }

    private static void publishLifecycle(AtfReportPorts.Session session, ApiModule api,
                                         ApiScenarioLifecycleRunner lifecycle) {
        ApiScenarioOutcome outcome = lifecycle.run(session,
                ApiScenarioPlanBuilder.scenario(
                        "ApiLifecycleAndCombinationsTests", "Users API — setup → test → teardown",
                        List.of("api", "users", "lifecycle"))
                        .setup(phase -> publish(phase, "GET /public/v2/users", api.users().getCollection(),
                                ContractValidators.userCollection(), false, null))
                        .body(phase -> publish(phase, "GET /public/v2/users/1", api.users().get(1),
                                new ResponseContract().status(201).contentTypeContains("application/json"), false, null))
                        .teardown(phase -> publish(phase, "GET /public/v2/posts", api.posts().getCollection(),
                                ContractValidators.postCollection(), false, null))
                        .build());
        assertEquals(AtfReportStatus.FAIL, outcome.status(), "lifecycle failure must remain visible in the report");
    }

    private static void run(ApiScenarioLifecycleRunner lifecycle, AtfReportPorts.Session session,
                            ApiScenarioPlanBuilder.PlanBuilder plan,
                            ApiScenarioPlanBuilder.PhaseAction body) {
        lifecycle.run(session, plan.body(body).build());
    }

    private static void publish(AtfReportPorts.Phase phase, String title, ApiExecutionResult execution,
                                ResponseContract contract, boolean warningMode, String requestJson) {
        var response = execution.response();
        var request = phase.startRequest(title, executionRequestMethod(title), URI.create(response.url()).getPath());
        request.detail("Duration", response.duration().toMillis() + " ms");
        request.detail("URL", response.url());
        request.detail("HTTP STATUS", Integer.toString(response.status()));
        request.detail("Content-Type", String.valueOf(response.header("Content-Type")));
        request.detail("Attempt", Integer.toString(execution.attempts()));
        if (requestJson != null) request.json("REQUEST JSON", requestJson);
        if (!response.body().isBlank()) request.json("RESPONSE JSON", response.body());
        atf.api.validation.evaluation.ValidationDecision decision;
        try {
            decision = VALIDATOR.validate(request, "Contract validation", response, contract, warningMode);
            request.complete(decision.status());
        } catch (RuntimeException failure) {
            request.complete(AtfReportStatus.FAIL);
            throw failure;
        } finally {
            request.close();
        }
        if (decision.status() == AtfReportStatus.WARNING) {
            throw new ApiScenarioLifecycleRunner.WarningSignal(decision.message());
        }
    }

    private static String executionRequestMethod(String title) {
        return title.substring(0, title.indexOf(' '));
    }

    private static final class AcceptanceTransport implements ApiTransport {
        @Override
        public CompletableFuture<ApiResponse> send(ApiRequest request, String url, String authProfile) {
            String path = request.path();
            String body;
            int status;
            if ("GET".equals(request.method()) && "/public/v2/users".equals(path)) {
                status = 200;
                body = "[{\"id\":1,\"name\":\"ATF User\",\"email\":\"atf@example.test\"}]";
            } else if ("GET".equals(request.method()) && "/public/v2/users/1".equals(path)) {
                status = 200;
                body = "{\"id\":1,\"name\":\"ATF User\",\"email\":\"atf@example.test\"}";
            } else if ("POST".equals(request.method()) && "/public/v2/users".equals(path)) {
                status = 401;
                body = "{\"message\":\"Authentication required\"}";
            } else if ("GET".equals(request.method()) && "/public/v2/posts".equals(path)) {
                status = 200;
                body = "[{\"id\":101,\"user_id\":1,\"title\":\"ATF post\",\"body\":\"contract\"}]";
            } else if ("GET".equals(request.method()) && "/public/v2/posts/101".equals(path)) {
                status = 200;
                body = "{\"id\":101,\"user_id\":1,\"title\":\"ATF post\",\"body\":\"contract\"}";
            } else if ("GET".equals(request.method()) && "/public/v2/users/1/posts".equals(path)) {
                status = 200;
                body = "[{\"id\":101,\"user_id\":1,\"title\":\"ATF post\"}]";
            } else if ("GET".equals(request.method()) && "/public/v2/posts/999999".equals(path)) {
                status = 404;
                body = "{\"message\":\"Not found\"}";
            } else {
                status = 500;
                body = "{\"message\":\"Unexpected fake route\"}";
            }
            return CompletableFuture.completedFuture(new ApiResponse(status, body,
                    Map.of("Content-Type", "application/json"), Duration.ofMillis(12), url));
        }
    }

    /** Small test-only facade that keeps the acceptance test readable. */
    private static final class ApiScenarioPlanBuilder {
        private ApiScenarioPlanBuilder() { }
        static PlanBuilder scenario(String testClass, String scenario, List<String> tags) {
            return new PlanBuilder(testClass, scenario, tags);
        }
        @FunctionalInterface interface PhaseAction extends atf.api.execution.lifecycle.ApiScenarioPlan.PhaseAction { }
        static final class PlanBuilder {
            private final String testClass;
            private final String scenario;
            private final List<String> tags;
            private PhaseAction setup;
            private PhaseAction body;
            private PhaseAction teardown;
            PlanBuilder(String testClass, String scenario, List<String> tags) {
                this.testClass = testClass; this.scenario = scenario; this.tags = tags;
            }
            PlanBuilder setup(PhaseAction value) { setup = value; return this; }
            PlanBuilder body(PhaseAction value) { body = value; return this; }
            PlanBuilder teardown(PhaseAction value) { teardown = value; return this; }
            atf.api.execution.lifecycle.ApiScenarioPlan build() {
                var builder = atf.api.execution.lifecycle.ApiScenarioPlan.scenario(testClass, scenario).tags(tags);
                if (setup != null) builder.setup(setup);
                if (body != null) builder.body(body);
                if (teardown != null) builder.teardown(teardown);
                return builder.build();
            }
        }
    }
}
