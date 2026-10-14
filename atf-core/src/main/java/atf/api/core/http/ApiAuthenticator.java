package atf.api.core.http;

import java.net.http.HttpRequest;

public interface ApiAuthenticator {
    void apply(HttpRequest.Builder request, String profileName);
    String describeMode(String profileName);
    String describeTokenState(String profileName);
}
