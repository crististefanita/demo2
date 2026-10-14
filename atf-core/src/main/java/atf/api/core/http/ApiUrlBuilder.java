package atf.api.core.http;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public final class ApiUrlBuilder {
    private final URI baseUri;

    public ApiUrlBuilder(URI baseUri) {
        this.baseUri = baseUri;
    }

    public String build(ApiRequest request) {
        StringBuilder value = new StringBuilder(baseUri.toString());
        if (value.length() > 0 && value.charAt(value.length() - 1) == '/' && request.path().startsWith("/")) {
            value.deleteCharAt(value.length() - 1);
        }
        if (value.length() > 0 && value.charAt(value.length() - 1) != '/' && !request.path().startsWith("/")) {
            value.append('/');
        }
        value.append(request.path());
        if (!request.query().isEmpty()) {
            value.append('?');
            value.append(request.query().entrySet().stream()
                    .map(entry -> URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8) + "="
                            + URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8))
                    .collect(java.util.stream.Collectors.joining("&")));
        }
        return value.toString();
    }
}
