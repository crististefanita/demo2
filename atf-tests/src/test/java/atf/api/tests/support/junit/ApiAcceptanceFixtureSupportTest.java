package atf.api.tests.support.junit;

import atf.api.tests.support.ApiAcceptancePlanBuilder;
import atf.api.core.http.ApiRequest;
import java.net.http.HttpRequest;
import atf.api.execution.lifecycle.ApiScenarioPlan;
import atf.api.core.http.HttpHeader;
import atf.api.core.http.HttpMethod;
import atf.api.core.http.MediaTypes;
import atf.api.execution.routing.RoutePaths;
import atf.api.mock.AtfMockTransport;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ApiAcceptanceFixtureSupportTest {
    @Test
    void transportKeepsConcreteNegativeApiEvidence() {
        String body = "{\"name\":\"ATF user\",\"email\":\"atf@example.test\"}";
        var request = new ApiRequest(HttpRequest.BodyPublishers.ofString(body), HttpMethod.POST.value(),
                RoutePaths.USERS, java.util.Map.of(), java.util.Map.of(HttpHeader.CONTENT_TYPE.value(), MediaTypes.APPLICATION_JSON),
                ApiTestDefaults.NO_AUTH_PROFILE, null, body);
        var response = AtfMockTransport.defaults()
                .send(request,
                        ApiTestDefaults.BASE_URI + RoutePaths.USERS, ApiTestDefaults.NO_AUTH_PROFILE)
                .join();

        assertAll(
                () -> assertEquals(401, response.status()),
                () -> assertEquals(MediaTypes.APPLICATION_JSON, response.header(HttpHeader.CONTENT_TYPE.value())),
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
