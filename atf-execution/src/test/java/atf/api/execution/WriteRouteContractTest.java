package atf.api.execution;

import atf.api.core.http.ApiExecutor;
import atf.api.core.http.ApiResponse;
import atf.api.core.http.ApiTransport;
import atf.api.execution.model.UserRequest;
import atf.api.execution.service.UsersService;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

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
        var route = atf.api.execution.routing.ApiRoutes.userCreate();
        assertEquals(atf.api.core.http.AuthRequirement.REQUIRED, route.authRequirement());
        assertEquals(atf.api.core.http.AuthType.BEARER, route.authType());
        assertTrue(route.tags().contains("write"));
    }
}
