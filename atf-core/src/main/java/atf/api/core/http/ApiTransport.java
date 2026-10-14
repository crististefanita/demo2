package atf.api.core.http;

import java.util.concurrent.CompletableFuture;

@FunctionalInterface
public interface ApiTransport {
    CompletableFuture<ApiResponse> send(ApiRequest request, String url, String authProfile);
}
