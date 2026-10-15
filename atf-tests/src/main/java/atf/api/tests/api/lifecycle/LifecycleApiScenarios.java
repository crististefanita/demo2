package atf.api.tests.api.lifecycle;

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
    private LifecycleApiScenarios() { }

    public static void publish(AtfReportPorts.Session session, ApiModule api,
                               ApiScenarioLifecycleRunner lifecycle) {
        ApiScenarioOutcome outcome = lifecycle.run(session,
                ApiAcceptancePlanBuilder.scenario(
                        "ApiLifecycleAndCombinationsTests", LifecycleApiScenarios.class.getPackageName(),
                        "Users API — setup → test → teardown",
                        List.of("api", "users", "lifecycle"))
                        .setup(phase -> ApiAcceptancePublisher.publish(phase, "GET /public/v2/users",
                                api.users().getCollection(), ContractValidators.userCollection(), false, null))
                        .body(phase -> ApiAcceptancePublisher.publish(phase, "GET /public/v2/users/1",
                                api.users().get(1),
                                new ResponseContract().status(201).contentTypeContains("application/json"),
                                false, null))
                        .teardown(phase -> {
                            ApiAcceptancePublisher.publish(phase, "GET /public/v2/posts",
                                    api.posts().getCollection(), ContractValidators.postCollection(), false, null);
                            throw new IllegalStateException(
                                    "teardown cleanup failed: Authorization: Bearer teardown-secret-token-1234567890");
                        })
                        .build());
        assertEquals(AtfReportStatus.FAIL, outcome.status(),
                "lifecycle failure must remain visible in the report");

        publishRetryBodyAndTeardownFailure(session, lifecycle);
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
            if (request.path().equals("/public/v2/users/1")
                    && bodyAttempts.incrementAndGet() == 1) {
                return CompletableFuture.completedFuture(new ApiResponse(
                        503, "{\"message\":\"retryable service response\"}",
                        Map.of("Content-Type", "application/json", "Retry-After", "0"),
                        Duration.ofMillis(3), url));
            }
            String body = request.path().equals("/public/v2/users/1")
                    ? "{\"id\":1,\"name\":\"ATF User\",\"email\":\"atf@example.test\"}"
                    : "[{\"id\":101,\"user_id\":1,\"title\":\"ATF post\"}]";
            return CompletableFuture.completedFuture(new ApiResponse(
                    200, body, Map.of("Content-Type", "application/json"),
                    Duration.ofMillis(12), url));
        };
        ApiModule retryingApi = ApiModuleFactory.fromTransport(
                transport, URI.create("https://atf.local"), Duration.ofSeconds(1), Set.of(503), 2);

        ApiScenarioOutcome outcome = lifecycle.run(session,
                ApiAcceptancePlanBuilder.scenario(
                                "ApiLifecycleAndCombinationsTests", LifecycleApiScenarios.class.getPackageName(),
                                "Users API — retry → body fail → teardown fail",
                                List.of("api", "users", "lifecycle", "retry", "negative"))
                        .setup(phase -> ApiAcceptancePublisher.publish(phase, "GET /public/v2/users",
                                retryingApi.users().getCollection(), ContractValidators.userCollection(), false, null))
                        .body(phase -> ApiAcceptancePublisher.publish(phase, "GET /public/v2/users/1",
                                retryingApi.users().get(1),
                                new ResponseContract().status(201).contentTypeContains("application/json"),
                                false, null))
                        .teardown(phase -> {
                            ApiAcceptancePublisher.publish(phase, "GET /public/v2/posts",
                                    retryingApi.posts().getCollection(), ContractValidators.postCollection(), false, null);
                            throw new IllegalStateException(
                                    "retry teardown cleanup failed: Authorization: Bearer retry-secret-token-1234567890");
                        })
                        .build());
        assertEquals(AtfReportStatus.FAIL, outcome.status(),
                "retry/body/teardown combination must remain a failed scenario");
    }
}
