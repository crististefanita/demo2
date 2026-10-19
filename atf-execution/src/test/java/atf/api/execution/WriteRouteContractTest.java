package atf.api.execution;

import atf.api.core.http.auth.AuthRequirement;
import atf.api.core.http.auth.AuthType;
import atf.api.core.http.execution.ApiExecutor;
import atf.api.core.http.response.ApiResponse;
import atf.api.core.http.transport.ApiTransport;
import atf.api.execution.model.UserRequest;
import atf.api.execution.routing.ApiRoutes;
import atf.api.execution.service.UsersService;
import atf.api.testkit.AtfComponentTest;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

@AtfComponentTest

class WriteRouteContractTest {
    @Test
    void semanticCreateUsesTypedRouteMetadataAndJsonBody() {
        var seen = new ApiResponse[1];
        ApiTransport transport = (request, url, auth) -> {
            seen[0] = new ApiResponse(201, "{\"id\":7}", Map.of(), Duration.ZERO, url);
            assertEquals("POST", request.method());
            assertEquals("default", auth);
            assertTrue(request.body().contentLength() > 0);
            return CompletableFuture.completedFuture(seen[0]);
        };

        var result = new UsersService(new ApiExecutor(transport, URI.create("https://example.test")))
                .create(new UserRequest("ATF", "atf@example.test", "male", "active"));

        assertEquals(201, result.response().status());
        var route = ApiRoutes.userCreate();
        assertEquals(AuthRequirement.REQUIRED, route.authRequirement());
        assertEquals(AuthType.BEARER, route.authType());
        assertTrue(route.tags().contains("write"));
    }
}
