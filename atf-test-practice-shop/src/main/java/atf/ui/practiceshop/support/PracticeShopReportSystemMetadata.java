package atf.ui.practiceshop.support;

import atf.ui.practiceshop.config.PracticeShopConfiguration;
import atf.api.core.config.ApiTransportMode;
import atf.api.reporting.configuration.ReportingSettings;
import atf.api.reporting.configuration.ReportEnvironmentField;
import atf.api.reporting.configuration.ReportEnvironmentSnapshot;

import java.net.URI;
import java.time.Duration;
import java.util.Map;

/** Builds the safe, semantic configuration snapshot shown in Overview/System. */
public final class PracticeShopReportSystemMetadata {
    private static final String ENVIRONMENT_PROPERTY = "atf.environment";

    private PracticeShopReportSystemMetadata() {
    }

    public static Map<String, String> from(PracticeShopConfiguration configuration) {
        return snapshot(configuration).systemDisplayValues();
    }

    /** Builds the allowlisted form consumed by the reporting composition root. */
    public static ReportEnvironmentSnapshot snapshot(PracticeShopConfiguration configuration) {
        if (configuration == null) return ReportEnvironmentSnapshot.empty();
        return snapshotValues(configuration).build();
    }

    /**
     * Compatibility overload for composition roots that also own generic
     * reporting settings. The System preview is deliberately read only from
     * the consumer policy, never from the Request/Response JSON limit.
     */
    public static ReportEnvironmentSnapshot snapshot(PracticeShopConfiguration configuration,
                                                     ReportingSettings reportingSettings) {
        if (reportingSettings == null) throw new NullPointerException("reportingSettings");
        return snapshot(configuration);
    }

    private static ReportEnvironmentSnapshot.Builder snapshotValues(
            PracticeShopConfiguration configuration) {
        return ReportEnvironmentSnapshot.builder()
                .put(ReportEnvironmentField.ENVIRONMENT,
                        System.getProperty(ENVIRONMENT_PROPERTY, "LOCAL"))
                .put(ReportEnvironmentField.UI_HOST, origin(configuration.baseUrl()))
                .put(ReportEnvironmentField.API_ENDPOINT, origin(configuration.api().endpoint()))
                .put(ReportEnvironmentField.BROWSER, browser(configuration))
                .put(ReportEnvironmentField.PROFILE,
                        profile(configuration))
                .put(ReportEnvironmentField.TRANSPORT_MODE,
                        configuration.api().mode() == ApiTransportMode.LIVE ? "LIVE HTTP" : "FIXTURE")
                .put(ReportEnvironmentField.PRECONDITION,
                        "API authentication: "
                                + configuration.api().precondition().name().toLowerCase())
                .put(ReportEnvironmentField.UI_JOURNEY_PRECONDITION,
                        configuration.authenticatedE2eAllowed()
                                ? "authenticated checkout account fixture loaded"
                                : "guest checkout configuration ready")
                .put(ReportEnvironmentField.API_EVIDENCE,
                        configuration.validationProfile().apiEvidenceRequired()
                                ? "enabled at checkpoints: "
                                + configuration.api().evidenceBinding().displayValue()
                                : "disabled by profile")
                .put(ReportEnvironmentField.NETWORK_INSPECTOR,
                        configuration.validationProfile().apiEvidenceRequired()
                                ? "Enabled · collapsed summary · checkpoints: "
                                + configuration.api().evidenceBinding().displayValue()
                                + " · JSON in checkpoint evidence"
                                : "disabled by validation profile")
                .put(ReportEnvironmentField.RETRY_POLICY,
                        configuration.uiRetry().summary())
                .put(ReportEnvironmentField.BODY_PREVIEW,
                        String.format("%,d characters · full JSON remains in checkpoint evidence",
                                configuration.reporting().systemBodyPreviewCharacters()))
                .put(ReportEnvironmentField.SCREENSHOT_POLICY,
                        PracticeShopScreenshotPolicy.profile(
                                configuration.browser().screenshotOnFailure())
                                .screenshotPolicy().configValue())
                .put(ReportEnvironmentField.UI_TIMEOUT,
                        humanDuration(configuration.browser().timeout()))
                .put(ReportEnvironmentField.NAVIGATION_TIMEOUT,
                        humanDuration(configuration.browser().navigationTimeout()))
                .put(ReportEnvironmentField.API_TIMEOUT,
                        humanDuration(configuration.api().timeout()))
                .put(ReportEnvironmentField.SOURCE_BUILD,
                        configuration.api().mode() == ApiTransportMode.LIVE
                                ? "Live HTTP evidence when observed"
                                : "Fixture transport boundary")
                .put(ReportEnvironmentField.REPORTING,
                        "Extent offline HTML · UI/API output roles separated");
    }

    /**
     * Builds the consumer snapshot with the System preview policy bound.
     * The overload keeps the pure component contract convenient while generic
     * Request/Response JSON settings remain owned by {@code atf-reporting}.
     */
    public static Map<String, String> from(PracticeShopConfiguration configuration,
                                           ReportingSettings reportingSettings) {
        return snapshot(configuration, reportingSettings).systemDisplayValues();
    }

    private static String browser(PracticeShopConfiguration configuration) {
        var browser = configuration.browser();
        return browser.browser() + " · " + (browser.headless() ? "headless" : "headed")
                + " · viewport " + browser.viewportWidth() + " × " + browser.viewportHeight()
                + " · zoom " + browser.zoomPercent() + "%";
    }

    private static String profile(PracticeShopConfiguration configuration) {
        return configuration.validationProfile().apiEvidenceRequired()
                ? "UI + API evidence" : "UI evidence";
    }

    private static String humanDuration(Duration duration) {
        long millis = duration.toMillis();
        if (millis % 1_000 == 0) return (millis / 1_000) + "s";
        return millis + "ms";
    }

    private static String origin(URI uri) {
        if (uri == null || uri.getScheme() == null || uri.getHost() == null) return "configured";
        String authority = uri.getRawAuthority();
        return uri.getScheme() + "://" + (authority == null ? uri.getHost() : authority);
    }
}
