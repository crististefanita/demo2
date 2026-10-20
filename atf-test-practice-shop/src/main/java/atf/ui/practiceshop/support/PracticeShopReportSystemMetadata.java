package atf.ui.practiceshop.support;

import atf.ui.practiceshop.config.PracticeShopConfiguration;
import atf.api.core.config.ApiTransportMode;

import java.net.URI;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** Builds the safe, semantic configuration snapshot shown in Overview/System. */
public final class PracticeShopReportSystemMetadata {
    private static final String ENVIRONMENT_PROPERTY = "atf.environment";

    private PracticeShopReportSystemMetadata() {
    }

    public static Map<String, String> from(PracticeShopConfiguration configuration) {
        if (configuration == null) return Map.of();
        var metadata = new LinkedHashMap<String, String>();
        metadata.put("Environment", System.getProperty(ENVIRONMENT_PROPERTY, "LOCAL"));
        metadata.put("UI host", origin(configuration.baseUrl()));
        metadata.put("API endpoint", origin(configuration.api().endpoint()));
        metadata.put("Browser", browser(configuration));
        metadata.put("Validation profile", configuration.validationProfile().name().toLowerCase());
        metadata.put("API mode", configuration.api().mode().name());
        metadata.put("API precondition", configuration.api().precondition().name().toLowerCase());
        metadata.put("API evidence", configuration.validationProfile().apiEvidenceRequired()
                ? "enabled per UI checkpoint" : "disabled by profile");
        metadata.put("Screenshot policy", configuration.browser().screenshotOnFailure()
                ? "failure checkpoints" : "disabled");
        metadata.put("UI timeout", configuration.browser().timeout().toString());
        metadata.put("Navigation timeout", configuration.browser().navigationTimeout().toString());
        metadata.put("API timeout", configuration.api().timeout().toString());
        metadata.put("UI report destination", "configured UI report directory");
        metadata.put("API report destination", "configured API report directory");
        metadata.put("Product fixture", configuration.productName() + "; route="
                + configuration.productPathExpectation());
        metadata.put("Checkout", "quantity=" + configuration.checkout().quantity()
                + "; payment=" + configuration.checkout().paymentMethod());
        metadata.put("Authenticated E2E", "configured=" + configuration.authenticatedE2eEnabled()
                + "; allowed=" + configuration.authenticatedE2eAllowed());
        metadata.put("Suite", "Practice Shop purchase E2E");
        metadata.put("Provenance", configuration.api().mode() == ApiTransportMode.LIVE
                ? "LIVE_HTTP when network evidence is present" : "acceptance/fixture boundary");
        return Collections.unmodifiableMap(metadata);
    }

    private static String browser(PracticeShopConfiguration configuration) {
        var browser = configuration.browser();
        return browser.browser() + "; headless=" + browser.headless()
                + "; viewport=" + browser.viewportWidth() + "x" + browser.viewportHeight()
                + "; zoom=" + browser.zoomPercent() + "%";
    }

    private static String origin(URI uri) {
        if (uri == null || uri.getScheme() == null || uri.getHost() == null) return "configured";
        String authority = uri.getRawAuthority();
        return uri.getScheme() + "://" + (authority == null ? uri.getHost() : authority);
    }
}
