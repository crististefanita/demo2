package atf.ui.practiceshop.support;

import com.microsoft.playwright.Response;

import java.util.Optional;

/** Captures and sanitizes one Practice Shop HTTP response from the current UI session. */
final class PracticeShopApiTrafficCapture {
    private static final String API_HOST = "api.practicesoftwaretesting.com";

    Optional<PracticeShopApiNetworkJournal.Exchange> capture(Response response) {
        if (response == null || !isPracticeShopApi(response.request().url())) {
            return Optional.empty();
        }
        var request = response.request();
        String responseBody;
        try {
            responseBody = PracticeShopApiEvidenceMapper.redactJson(new String(response.body()));
        } catch (RuntimeException unavailable) {
            // Chromium may release a compressed CORS payload before CDP can read it.
            responseBody = "<body unavailable: Chromium released response payload>";
        }
        return Optional.of(new PracticeShopApiNetworkJournal.Exchange(
                request.method(), path(request.url()), request.url(), response.status(),
                PracticeShopApiEvidenceMapper.redactJson(request.postData()), responseBody,
                request.headers(), response.headers()));
    }

    private static boolean isPracticeShopApi(String url) {
        try {
            return API_HOST.equals(java.net.URI.create(url).getHost());
        } catch (RuntimeException ignored) {
            return false;
        }
    }

    private static String path(String url) {
        java.net.URI uri = java.net.URI.create(url);
        return uri.getRawPath() + (uri.getRawQuery() == null ? "" : "?" + uri.getRawQuery());
    }
}
