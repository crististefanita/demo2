package atf.api.tests.support;

import atf.api.core.http.ApiRequest;
import atf.api.execution.lifecycle.ApiScenarioPlan;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ApiAcceptanceFixtureSupportTest {
    @Test
    void transportKeepsConcreteNegativeApiEvidence() {
        var response = new AcceptanceTransport()
                .send(ApiRequest.of("POST", "/public/v2/users"),
                        "https://atf.local/public/v2/users", "none")
                .join();

        assertAll(
                () -> assertEquals(401, response.status()),
                () -> assertEquals("application/json", response.header("Content-Type")),
                () -> assertEquals("{\"message\":\"Authentication required\"}", response.body()));
    }

    @Test
    void planBuilderOwnsScenarioMetadataAndOptionalLifecyclePhases() {
        ApiScenarioPlan plan = ApiAcceptancePlanBuilder.scenario(
                        "UsersApiTests", "users negative", List.of("api", "negative"))
                .setup(phase -> { })
                .body(phase -> { })
                .teardown(phase -> { })
                .build();

        assertAll(
                () -> assertEquals("UsersApiTests", plan.testClass()),
                () -> assertEquals(List.of("api", "negative"), plan.tags()),
                () -> assertNotNull(plan.setup()),
                () -> assertNotNull(plan.body()),
                () -> assertNotNull(plan.teardown()));
    }
}
