package atf.api.tests.support.junit;
import atf.api.testkit.AtfComponentTest;
import atf.api.reporting.api.acceptance.ApiAcceptancePlanBuilder;

import atf.api.core.http.request.ApiRequest;
import atf.api.core.config.source.ApiEnvironment;
import java.net.http.HttpRequest;
import atf.api.core.lifecycle.ApiScenarioPlan;
import atf.api.core.http.request.HttpHeader;
import atf.api.core.http.request.HttpMethod;
import atf.api.core.http.request.MediaTypes;
import atf.api.mock.MockRoutePaths;
import atf.api.mock.AtfMockTransport;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@AtfComponentTest
class ApiAcceptanceFixtureSupportTest {
    @Test
    void transportKeepsConcreteNegativeApiEvidence() {
        String body = "{\"name\":\"ATF user\",\"email\":\"atf@example.test\"}";
        var request = new ApiRequest(HttpRequest.BodyPublishers.ofString(body), HttpMethod.POST.value(),
                MockRoutePaths.USERS, java.util.Map.of(), java.util.Map.of(HttpHeader.CONTENT_TYPE.value(), MediaTypes.APPLICATION_JSON),
                ApiEnvironment.NO_AUTH_PROFILE, null, body);
        var response = AtfMockTransport.defaults()
                .send(request,
                        ApiEnvironment.MOCK_BASE_URI + MockRoutePaths.USERS, ApiEnvironment.NO_AUTH_PROFILE)
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
