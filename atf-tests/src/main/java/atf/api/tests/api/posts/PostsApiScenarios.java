package atf.api.tests.api.posts;

import atf.api.core.reporting.AtfReportPorts;
import atf.api.execution.ApiModule;
import atf.api.execution.lifecycle.ApiScenarioLifecycleRunner;
import atf.api.tests.support.ApiAcceptancePlanBuilder;
import atf.api.tests.support.ApiAcceptancePublisher;
import atf.api.validation.contract.ContractValidators;
import atf.api.validation.contract.ResponseContract;

import java.util.List;

/** Post-resource and relationship scenarios in the public acceptance artifact. */
public final class PostsApiScenarios {
    private PostsApiScenarios() { }

    public static void publish(AtfReportPorts.Session session, ApiModule api,
                               ApiScenarioLifecycleRunner lifecycle) {
        run(lifecycle, session, plan(
                "GET /public/v2/posts — collection contract",
                List.of("api", "posts", "contract")),
                phase -> ApiAcceptancePublisher.publish(phase, "GET /public/v2/posts",
                        api.posts().getCollection(), ContractValidators.postCollection(), false, null));

        run(lifecycle, session, plan(
                "GET /public/v2/posts/{id} — item contract",
                List.of("api", "posts", "contract")),
                phase -> ApiAcceptancePublisher.publish(phase, "GET /public/v2/posts/101", api.posts().get(101),
                        ContractValidators.postItem(), false, null));

        run(lifecycle, session, plan(
                "GET /public/v2/users/{userId}/posts — relationship contract",
                List.of("api", "posts", "relationship", "contract")),
                phase -> ApiAcceptancePublisher.publish(phase, "GET /public/v2/users/1/posts",
                        api.posts().forUser(1), ContractValidators.relationshipCollection("1"), false, null));

        run(lifecycle, session, plan(
                "GET /public/v2/posts/{unknownId} — not found contract",
                List.of("api", "posts", "negative", "contract")),
                phase -> ApiAcceptancePublisher.publish(phase, "GET /public/v2/posts/999999",
                        api.posts().get(999999),
                        new ResponseContract().status(404).contentTypeContains("application/json")
                                .bodyContains("Not found"), false, null));
    }

    private static void run(ApiScenarioLifecycleRunner lifecycle, AtfReportPorts.Session session,
                            ApiAcceptancePlanBuilder plan,
                            atf.api.execution.lifecycle.ApiScenarioPlan.PhaseAction body) {
        lifecycle.run(session, plan.body(body).build());
    }

    private static ApiAcceptancePlanBuilder plan(String scenario, List<String> tags) {
        return ApiAcceptancePlanBuilder.scenario("PostsApiTests", PostsApiScenarios.class.getPackageName(),
                scenario, tags);
    }
}
