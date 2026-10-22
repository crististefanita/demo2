package atf.ui.practiceshop.support;

import com.microsoft.playwright.Response;

import java.util.Optional;

/** Captures and sanitizes one Practice Shop HTTP response from the current UI session. */
final class PracticeShopApiTrafficCapture {
    private static final String API_HOST = "api.practicesoftwaretesting.com";

    Optional<PracticeShopApiNetworkJournal.Exchange> capture(Response response) {
        if (response == null) return Optional.empty();
        var request = response.request();
        if (request == null || !isPracticeShopApi(request.url())) {
            return Optional.empty();
        }
        String path = path(request.url());
        String responseBody;
        try {
            // Response callbacks can arrive before Chromium has released the
            // compressed CORS payload. Wait for the response lifecycle to settle
            // before reading the body; otherwise a valid JSON response can be
            // reported as transport-unavailable evidence.
            response.finished();
            responseBody = PracticeShopApiEvidenceMapper.redactJson(new String(response.body()));
        } catch (RuntimeException unavailable) {
            // Keep the limitation explicit when the browser still cannot expose
            // the payload after the lifecycle wait; assertions must not treat it
            // as an empty or successful JSON body.
            responseBody = PracticeShopApiBodyEvidence.unavailable(
                    "Chromium response body", unavailable);
        }
        return Optional.of(new PracticeShopApiNetworkJournal.Exchange(
                request.method(), path, request.url(), response.status(),
                PracticeShopApiEvidenceMapper.redactJson(request.postData()), responseBody,
                PracticeShopJsonEvidence.redactHeaders(request.headers()),
                PracticeShopJsonEvidence.redactHeaders(response.headers())));
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
