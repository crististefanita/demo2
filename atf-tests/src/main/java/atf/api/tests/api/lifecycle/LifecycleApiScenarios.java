package atf.api.tests.api.lifecycle;

import atf.api.core.reporting.AtfReportPorts;
import atf.api.core.reporting.AtfReportStatus;
import atf.api.execution.ApiModule;
import atf.api.execution.lifecycle.ApiScenarioLifecycleRunner;
import atf.api.execution.lifecycle.ApiScenarioOutcome;
import atf.api.tests.support.ApiAcceptancePlanBuilder;
import atf.api.tests.support.ApiAcceptancePublisher;
import atf.api.validation.contract.ContractValidators;
import atf.api.validation.contract.ResponseContract;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Setup/body/teardown combinations that prove lifecycle ownership. */
public final class LifecycleApiScenarios {
    private LifecycleApiScenarios() { }

    public static void publish(AtfReportPorts.Session session, ApiModule api,
                               ApiScenarioLifecycleRunner lifecycle) {
        ApiScenarioOutcome outcome = lifecycle.run(session,
                ApiAcceptancePlanBuilder.scenario(
                        "ApiLifecycleAndCombinationsTests", "Users API — setup → test → teardown",
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
    }
}
