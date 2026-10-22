package atf.ui.practiceshop.support;

import atf.api.core.http.request.HttpMethod;
import atf.ui.practiceshop.api.PracticeShopApiRequestFactory;
import atf.ui.practiceshop.api.PracticeShopApiRoutes;
import atf.ui.practiceshop.config.PracticeShopApiConfiguration;
import atf.ui.practiceshop.config.PracticeShopCredentials;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Typed boundary for the optional login-body recovery used when Playwright
 * releases the compressed response before its body can be read.
 */
public final class PracticeShopLoginBodyRecovery {
    private PracticeShopLoginBodyRecovery() { }

    @FunctionalInterface
    public interface BodySupplier {
        String get() throws Exception;
    }

    public record Result(boolean recovered, String body, String evidence) {
        public Result {
            body = body == null ? "" : body;
            evidence = Objects.requireNonNull(evidence, "evidence");
        }

        public static Result recovered(String body) {
            if (body == null || body.isBlank()) {
                return unavailable("recovery returned an empty response body");
            }
            return new Result(true, body, "login response body recovered");
        }

        public static Result unavailable(String detail) {
            return new Result(false, "", PracticeShopApiBodyEvidence.unavailable(
                    "login body recovery", detail));
        }

        public String effectiveBody() {
            return recovered ? body : evidence;
        }
    }

    public static Result attempt(BodySupplier supplier) {
        Objects.requireNonNull(supplier, "supplier");
        try {
            return Result.recovered(supplier.get());
        } catch (Exception error) {
            return new Result(false, "", PracticeShopApiBodyEvidence.unavailable(
                    "login body recovery", error));
        }
    }

    /** Performs the narrowly scoped live login read-back used when Playwright cannot expose its body. */
    public static Result recoverLive(PracticeShopApiConfiguration configuration,
                                     PracticeShopCredentials credentials) {
        Objects.requireNonNull(configuration, "configuration");
        Objects.requireNonNull(credentials, "credentials");
        if (configuration.mode() != atf.api.core.config.ApiTransportMode.LIVE) {
            return Result.unavailable("live recovery is disabled outside LIVE mode");
        }
        return attempt(() -> fetchLiveLoginBody(configuration, credentials));
    }

    private static String fetchLiveLoginBody(PracticeShopApiConfiguration configuration,
                                             PracticeShopCredentials credentials) throws IOException {
        var request = new PracticeShopApiRequestFactory().login(credentials);
        URI uri = configuration.endpoint().resolve(PracticeShopApiRoutes.LOGIN.substring(1));
        HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
        try {
            connection.setConnectTimeout((int) configuration.timeout().toMillis());
            connection.setReadTimeout((int) configuration.timeout().toMillis());
            connection.setRequestMethod(HttpMethod.POST.value());
            connection.setDoOutput(true);
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Content-Type", "application/json");
            byte[] body = request.bodyText().getBytes(StandardCharsets.UTF_8);
            try (var output = connection.getOutputStream()) {
                output.write(body);
            }
            int status = connection.getResponseCode();
            if (status < 200 || status >= 300) {
                throw new IOException("HTTP status " + status + " for " + PracticeShopApiRoutes.LOGIN);
            }
            try (InputStream input = connection.getInputStream()) {
                return new String(input.readAllBytes(), StandardCharsets.UTF_8);
            }
        } finally {
            connection.disconnect();
        }
    }
}
