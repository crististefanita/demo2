package atf.api.tests.api.lifecycle;

import atf.api.core.http.HttpHeader;
import atf.api.core.http.ApiOperationLabel;
import atf.api.core.http.HttpMethod;
import atf.api.core.http.MediaTypes;
import atf.api.execution.routing.RoutePaths;

import atf.api.core.reporting.AtfReportPorts;
import atf.api.core.reporting.AtfReportStatus;
import atf.api.core.http.ApiResponse;
import atf.api.core.http.ApiTransport;
import atf.api.execution.ApiModule;
import atf.api.execution.ApiModuleFactory;
import atf.api.execution.lifecycle.ApiScenarioLifecycleRunner;
import atf.api.execution.lifecycle.ApiScenarioOutcome;
import atf.api.tests.support.ApiAcceptancePlanBuilder;
import atf.api.tests.support.ApiAcceptancePublisher;
import atf.api.tests.support.ApiEnvironment;
import atf.api.tests.api.data.ApiFixtureData;
import atf.api.validation.contract.ContractValidators;
import atf.api.validation.contract.ResponseContract;

import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Setup/body/teardown combinations that prove lifecycle ownership. */
public final class LifecycleApiScenarios {
    public static final String REPORT_OWNER = LifecycleApiScenarios.class.getSimpleName();
    public static final String LIFECYCLE_MATRIX = "lifecycleMatrix";
    private static final String USER_DETAIL_PATH = RoutePaths.USERS + "/1";
    private static final String JSON_ASSERTION_PATH = "$.name";
    private static final String WRONG_USER_NAME = "Expected Contract Name";
    private static final String MISSING_RESPONSE_HEADER = "X-Required-Header";
    private static final String TEARDOWN_AUTHORIZATION_FAILURE =
            "Authorization header was not available";

    private LifecycleApiScenarios() { }

    public static void publishSelected(AtfReportPorts.Session session, ApiModule api,
                                       ApiScenarioLifecycleRunner lifecycle, String method) {
        if (LIFECYCLE_MATRIX.equals(method)) {
            publish(session, api, lifecycle);
            return;
        }
        throw new IllegalArgumentException("Unknown lifecycle API scenario method: " + method);
    }

    public static String scenarioTitle(String method) {
        if (LIFECYCLE_MATRIX.equals(method)) return "Users API — setup → test → teardown";
        throw new IllegalArgumentException("Unknown lifecycle API scenario method: " + method);
    }

    public static void publish(AtfReportPorts.Session session, ApiModule api,
                               ApiScenarioLifecycleRunner lifecycle) {
        ApiScenarioOutcome outcome = lifecycle.run(session,
                ApiAcceptancePlanBuilder.scenario(
                        REPORT_OWNER, LifecycleApiScenarios.class.getPackageName(),
                        "Users API — setup → test → teardown",
                        List.of("api", "users", "lifecycle"))
                        .setup(phase -> ApiAcceptancePublisher.publish(phase, ApiOperationLabel.format(HttpMethod.GET, RoutePaths.USERS, null),
                                api.users().getCollection(), ContractValidators.userCollection(), false, null))
                        .body(phase -> ApiAcceptancePublisher.publish(phase, ApiOperationLabel.format(HttpMethod.GET, RoutePaths.USERS + "/1", null),
                                api.users().get(1),
                                new ResponseContract().status(201).contentTypeContains(MediaTypes.APPLICATION_JSON),
                                false, null))
                        .teardown(phase -> {
                            ApiAcceptancePublisher.publish(phase, ApiOperationLabel.format(HttpMethod.GET, RoutePaths.POSTS, null),
                                    api.posts().getCollection(), ContractValidators.postCollection(), false, null);
                            throw new IllegalStateException(
                                    "teardown cleanup failed: " + TEARDOWN_AUTHORIZATION_FAILURE);
                        })
                        .build());
        assertEquals(AtfReportStatus.FAIL, outcome.status(),
                "lifecycle failure must remain visible in the report");

        publishRetryBodyAndTeardownFailure(session, lifecycle);
        publishAssertionFailureVariants(session, api, lifecycle);
    }

    /**
     * Keeps the retry + body failure + teardown failure combination visible in
     * the real Extent artifact. The transport is deterministic and scoped to
     * this scenario, so parallel report sessions cannot share retry state.
     */
    private static void publishRetryBodyAndTeardownFailure(AtfReportPorts.Session session,
                                                            ApiScenarioLifecycleRunner lifecycle) {
        var bodyAttempts = new AtomicInteger();
        ApiTransport transport = (request, url, authProfile) -> {
            if (request.path().equals(USER_DETAIL_PATH)
                    && bodyAttempts.incrementAndGet() == 1) {
                return CompletableFuture.completedFuture(new ApiResponse(
                        503, "{\"message\":\"retryable service response\"}",
                        Map.of(HttpHeader.CONTENT_TYPE.value(), MediaTypes.APPLICATION_JSON,
                                HttpHeader.RETRY_AFTER.value(), "0"),
                        Duration.ofMillis(3), url));
            }
            String body;
            if (request.path().equals(USER_DETAIL_PATH)) {
                body = "{\"id\":%d,\"name\":\"%s\",\"email\":\"%s\"}"
                        .formatted(ApiFixtureData.USER_ID, ApiFixtureData.USER_NAME, ApiFixtureData.USER_EMAIL);
            } else if (request.path().equals(RoutePaths.USERS)) {
            body = "[{\"id\":%d,\"name\":\"%s\",\"email\":\"%s\"}]"
                    .formatted(ApiFixtureData.USER_ID, ApiFixtureData.USER_NAME, ApiFixtureData.USER_EMAIL);
            } else {
                body = ("[{\"id\":%d,\"user_id\":%d,\"title\":\"%s\",\"body\":\"%s\"},"
                        + "{\"id\":%d,\"user_id\":%d,\"title\":\"%s\","
                        + "\"body\":\"%s\",\"status\":\"published\"}]")
                        .formatted(ApiFixtureData.EXISTING_POST_ID, ApiFixtureData.USER_ID, ApiFixtureData.EXISTING_POST_TITLE, ApiFixtureData.POST_BODY,
                                ApiFixtureData.CREATED_POST_ID, ApiFixtureData.USER_ID,
                                ApiFixtureData.CREATED_POST_TITLE, ApiFixtureData.POST_BODY);
            }
            return CompletableFuture.completedFuture(new ApiResponse(
                    200, body, Map.of(HttpHeader.CONTENT_TYPE.value(), MediaTypes.APPLICATION_JSON),
                    Duration.ofMillis(12), url));
        };
        ApiModule retryingApi = ApiModuleFactory.fromTransport(
                transport, ApiEnvironment.MOCK_BASE_URI, Duration.ofSeconds(1), Set.of(503), 2);

        ApiScenarioOutcome outcome = lifecycle.run(session,
                ApiAcceptancePlanBuilder.scenario(
                                REPORT_OWNER, LifecycleApiScenarios.class.getPackageName(),
                                "Users API — retry → body fail → teardown fail",
                                List.of("api", "users", "lifecycle", "retry", "negative"))
                        .setup(phase -> ApiAcceptancePublisher.publish(phase, ApiOperationLabel.format(HttpMethod.GET, RoutePaths.USERS, null),
                                retryingApi.users().getCollection(), ContractValidators.userCollection(), false, null))
                        .body(phase -> ApiAcceptancePublisher.publish(phase, ApiOperationLabel.format(HttpMethod.GET, RoutePaths.USERS + "/1", null),
                                retryingApi.users().get(1),
                                new ResponseContract().status(201).contentTypeContains(MediaTypes.APPLICATION_JSON),
                                false, null))
                        .teardown(phase -> {
                            ApiAcceptancePublisher.publish(phase, ApiOperationLabel.format(HttpMethod.GET, RoutePaths.POSTS, null),
                                    retryingApi.posts().getCollection(), ContractValidators.postCollection(), false, null);
                            throw new IllegalStateException(
                                "retry teardown cleanup failed: " + TEARDOWN_AUTHORIZATION_FAILURE);
                        })
                        .build());
        assertEquals(AtfReportStatus.FAIL, outcome.status(),
                "retry/body/teardown combination must remain a failed scenario");
    }

    /**
     * Publishes isolated assertion failures so the report contract proves more
     * than one generic lifecycle exception shape. Each scenario has one clear
     * contract mismatch and a successful teardown, making expected/actual
     * evidence unambiguous in the Tests and Fails tabs.
     */
    private static void publishAssertionFailureVariants(AtfReportPorts.Session session,
                                                         ApiModule api,
                                                         ApiScenarioLifecycleRunner lifecycle) {
        publishAssertionFailureScenario(session, api, lifecycle,
                "Users API — JSON assertion mismatch",
                new ResponseContract()
                        .status(200)
                        .contentTypeContains(MediaTypes.APPLICATION_JSON)
                        .jsonPathEquals(JSON_ASSERTION_PATH, WRONG_USER_NAME));
        publishAssertionFailureScenario(session, api, lifecycle,
                "Users API — header assertion mismatch",
                new ResponseContract()
                        .status(200)
                        .contentTypeContains(MediaTypes.APPLICATION_JSON)
                        .headerPresent(MISSING_RESPONSE_HEADER));
    }

    private static void publishAssertionFailureScenario(AtfReportPorts.Session session,
                                                         ApiModule api,
                                                         ApiScenarioLifecycleRunner lifecycle,
                                                         String title,
                                                         ResponseContract contract) {
        ApiScenarioOutcome outcome = lifecycle.run(session,
                ApiAcceptancePlanBuilder.scenario(
                                REPORT_OWNER, LifecycleApiScenarios.class.getPackageName(),
                                title,
                                List.of("api", "users", "lifecycle", "negative", "assertion"))
                        .body(phase -> ApiAcceptancePublisher.publish(phase,
                                ApiOperationLabel.format(HttpMethod.GET, USER_DETAIL_PATH, null),
                                api.users().get(1), contract, false, null))
                        .teardown(phase -> ApiAcceptancePublisher.publish(phase,
                                ApiOperationLabel.format(HttpMethod.GET, RoutePaths.POSTS, null),
                                api.posts().getCollection(), ContractValidators.postCollection(), false, null))
                        .build());
        assertEquals(AtfReportStatus.FAIL, outcome.status(),
                "assertion failure must remain visible for: " + title);
    }
}
