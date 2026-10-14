package atf.api.tests.support;

import atf.api.core.http.ApiRequest;
import atf.api.core.http.ApiResponse;
import atf.api.core.http.ApiTransport;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/** Deterministic API fixture used by report-producing acceptance tests. */
public final class AcceptanceTransport implements ApiTransport {
    @Override
    public CompletableFuture<ApiResponse> send(ApiRequest request, String url, String authProfile) {
        String path = request.path();
        String body;
        int status;
        if ("GET".equals(request.method()) && "/public/v2/users".equals(path)) {
            status = 200;
            body = "[{\"id\":1,\"name\":\"ATF User\",\"email\":\"atf@example.test\"}]";
        } else if ("GET".equals(request.method()) && "/public/v2/users/1".equals(path)) {
            status = 200;
            body = "{\"id\":1,\"name\":\"ATF User\",\"email\":\"atf@example.test\"}";
        } else if ("POST".equals(request.method()) && "/public/v2/users".equals(path)) {
            status = 401;
            body = "{\"message\":\"Authentication required\"}";
        } else if ("GET".equals(request.method()) && "/public/v2/posts".equals(path)) {
            status = 200;
            body = "[{\"id\":101,\"user_id\":1,\"title\":\"ATF post\",\"body\":\"contract\"}]";
        } else if ("GET".equals(request.method()) && "/public/v2/posts/101".equals(path)) {
            status = 200;
            body = "{\"id\":101,\"user_id\":1,\"title\":\"ATF post\",\"body\":\"contract\"}";
        } else if ("GET".equals(request.method()) && "/public/v2/users/1/posts".equals(path)) {
            status = 200;
            body = "[{\"id\":101,\"user_id\":1,\"title\":\"ATF post\"}]";
        } else if ("GET".equals(request.method()) && "/public/v2/posts/999999".equals(path)) {
            status = 404;
            body = "{\"message\":\"Not found\"}";
        } else {
            status = 500;
            body = "{\"message\":\"Unexpected fake route\"}";
        }
        return CompletableFuture.completedFuture(new ApiResponse(status, body,
                Map.of("Content-Type", "application/json"), Duration.ofMillis(12), url));
    }
}
