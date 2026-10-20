package atf.api.core.http.transport;
import atf.api.core.http.request.ApiRequest;
import atf.api.core.http.response.ApiResponse;

import java.util.concurrent.CompletableFuture;

@FunctionalInterface
public interface ApiTransport {
    CompletableFuture<ApiResponse> send(ApiRequest request, String url, String authProfile);
}
