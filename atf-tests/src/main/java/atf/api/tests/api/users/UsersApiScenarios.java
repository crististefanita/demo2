package atf.api.tests.api.users;

import atf.api.core.reporting.AtfReportPorts;
import atf.api.execution.ApiModule;
import atf.api.execution.data.JsonPayloads;
import atf.api.execution.lifecycle.ApiScenarioLifecycleRunner;
import atf.api.execution.model.UserRequest;
import atf.api.tests.support.ApiAcceptancePlanBuilder;
import atf.api.tests.support.ApiAcceptancePublisher;
import atf.api.validation.contract.ContractValidators;
import atf.api.validation.contract.ResponseContract;

import java.util.List;

/** User-resource scenarios that appear in the public acceptance artifact. */
public final class UsersApiScenarios {
    private UsersApiScenarios() { }

    public static void publish(AtfReportPorts.Session session, ApiModule api,
                               ApiScenarioLifecycleRunner lifecycle) {
        run(lifecycle, session, ApiAcceptancePlanBuilder.scenario(
                "UsersApiTests", "GET /public/v2/users — collection contract",
                List.of("api", "users", "contract", "smoke")),
                phase -> ApiAcceptancePublisher.publish(phase, "GET /public/v2/users",
                        api.users().getCollection(), ContractValidators.userCollection(), false, null));

        run(lifecycle, session, ApiAcceptancePlanBuilder.scenario(
                "UsersApiTests", "GET /public/v2/users/{id} — item contract",
                List.of("api", "users", "contract")),
                phase -> ApiAcceptancePublisher.publish(phase, "GET /public/v2/users/1", api.users().get(1),
                        ContractValidators.userItem(), false, null));

        run(lifecycle, session, ApiAcceptancePlanBuilder.scenario(
                "UsersApiTests", "POST /public/v2/users — missing authentication",
                List.of("api", "users", "negative", "auth")),
                phase -> {
                    var payload = new UserRequest("ATF user", "atf@example.test", "male", "active");
                    ApiAcceptancePublisher.publish(phase, "POST /public/v2/users", api.users().create(payload),
                            new ResponseContract().status(401).contentTypeContains("application/json")
                                    .bodyContains("Authentication required"), false,
                            JsonPayloads.write(payload));
                });

        run(lifecycle, session, ApiAcceptancePlanBuilder.scenario(
                "UsersApiTests", "GET /public/v2/users — soft contract warning",
                List.of("api", "users", "reporting", "soft-assert")),
                phase -> ApiAcceptancePublisher.publish(phase, "GET /public/v2/users",
                        api.users().getCollection(),
                        new ResponseContract().status(201).contentTypeContains("application/json"), true, null));
    }

    private static void run(ApiScenarioLifecycleRunner lifecycle, AtfReportPorts.Session session,
                            ApiAcceptancePlanBuilder plan,
                            atf.api.execution.lifecycle.ApiScenarioPlan.PhaseAction body) {
        lifecycle.run(session, plan.body(body).build());
    }
}
