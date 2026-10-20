package atf.api.core.http.transport;
import atf.api.core.http.auth.ApiAuthenticator;
import atf.api.core.http.request.ApiRequest;
import atf.api.core.http.request.HttpHeader;
import atf.api.core.http.request.MediaTypes;
import atf.api.core.http.response.ApiResponse;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

public final class HttpClientTransport implements ApiTransport {
    private final HttpClient client;
    private final ApiAuthenticator authenticator;
    private final Duration timeout;

    public HttpClientTransport(HttpClient client, ApiAuthenticator authenticator, Duration timeout) {
        this.client = client;
        this.authenticator = authenticator;
        this.timeout = timeout;
    }

    @Override
    public CompletableFuture<ApiResponse> send(ApiRequest request, String url, String authProfile) {
        var builder = HttpRequest.newBuilder(URI.create(url))
                .timeout(timeout)
                .header(HttpHeader.ACCEPT.value(), MediaTypes.APPLICATION_JSON)
                .header(HttpHeader.CONTENT_TYPE.value(), MediaTypes.APPLICATION_JSON)
                .method(request.method(), request.body());
        request.headers().forEach(builder::header);
        authenticator.apply(builder, authProfile);
        long started = System.nanoTime();
        return client.sendAsync(builder.build(), HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> new ApiResponse(
                        response.statusCode(), response.body(), ApiResponse.flatten(response.headers()),
                        Duration.ofNanos(System.nanoTime() - started), url));
    }
}
