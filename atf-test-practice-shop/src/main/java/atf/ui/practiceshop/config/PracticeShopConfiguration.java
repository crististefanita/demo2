package atf.ui.practiceshop.config;

import atf.api.core.config.ApiTransportMode;
import atf.api.ui.config.UiBrowserConfiguration;
import atf.ui.practiceshop.routes.PracticeShopUiRoutes;

import java.net.URI;
import java.nio.file.Path;
import java.util.Objects;

/** Immutable external-system policy for the Practice Software Testing suite. */
public record PracticeShopConfiguration(
        URI baseUrl,
        Path accountFile,
        String productName,
        String productPath,
        PracticeShopCheckoutConfiguration checkout,
        UiBrowserConfiguration browser,
        PracticeShopUiRetryConfiguration uiRetry,
        PracticeShopValidationProfile validationProfile,
        PracticeShopApiConfiguration api,
        Path uiReportDirectory,
        Path apiReportDirectory,
        boolean authenticatedE2eEnabled,
        PracticeShopReportConfiguration reporting) {
    public static final String DYNAMIC_PRODUCT_PATH = "auto";

    public PracticeShopConfiguration {
        baseUrl = Objects.requireNonNull(baseUrl, "baseUrl");
        accountFile = Objects.requireNonNull(accountFile, "accountFile").toAbsolutePath().normalize();
        productName = required(productName, "productName");
        productPath = required(productPath, "productPath");
        checkout = Objects.requireNonNull(checkout, "checkout");
        browser = Objects.requireNonNull(browser, "browser");
        uiRetry = Objects.requireNonNull(uiRetry, "uiRetry");
        validationProfile = Objects.requireNonNull(validationProfile, "validationProfile");
        api = Objects.requireNonNull(api, "api");
        reporting = Objects.requireNonNull(reporting, "reporting");
        if (authenticatedE2eEnabled && !api.authenticatedPrecondition()) {
            throw new IllegalArgumentException(
                    "authenticatedE2eEnabled requires api.precondition=authenticated");
        }
        uiReportDirectory = Objects.requireNonNull(uiReportDirectory, "uiReportDirectory")
                .toAbsolutePath().normalize();
        apiReportDirectory = Objects.requireNonNull(apiReportDirectory, "apiReportDirectory")
                .toAbsolutePath().normalize();
    }

    /** Compatibility constructor for UI-only callers. */
    public PracticeShopConfiguration(URI baseUrl, Path accountFile, String productName,
                                     String productPath, PracticeShopCheckoutConfiguration checkout,
                                     UiBrowserConfiguration browser,
                                     PracticeShopValidationProfile validationProfile) {
        this(baseUrl, accountFile, productName, productPath, checkout, browser,
                PracticeShopUiRetryConfiguration.none(), validationProfile,
                PracticeShopApiConfiguration.defaults(),
                Path.of("artifacts", "reports", "ui"), Path.of("artifacts", "reports", "api"), false,
                PracticeShopReportConfiguration.defaults());
    }

    /** Compatibility constructor for callers that already provide the complete pre-retry shape. */
    public PracticeShopConfiguration(URI baseUrl, Path accountFile, String productName,
                                     String productPath, PracticeShopCheckoutConfiguration checkout,
                                     UiBrowserConfiguration browser,
                                     PracticeShopValidationProfile validationProfile,
                                     PracticeShopApiConfiguration api,
                                     Path uiReportDirectory, Path apiReportDirectory,
                                     boolean authenticatedE2eEnabled) {
        this(baseUrl, accountFile, productName, productPath, checkout, browser,
                PracticeShopUiRetryConfiguration.none(), validationProfile, api,
                uiReportDirectory, apiReportDirectory, authenticatedE2eEnabled,
                PracticeShopReportConfiguration.defaults());
    }

    /** Compatibility constructor for callers that already provide UI retry policy. */
    public PracticeShopConfiguration(URI baseUrl, Path accountFile, String productName,
                                     String productPath, PracticeShopCheckoutConfiguration checkout,
                                     UiBrowserConfiguration browser,
                                     PracticeShopUiRetryConfiguration uiRetry,
                                     PracticeShopValidationProfile validationProfile,
                                     PracticeShopApiConfiguration api,
                                     Path uiReportDirectory, Path apiReportDirectory,
                                     boolean authenticatedE2eEnabled) {
        this(baseUrl, accountFile, productName, productPath, checkout, browser, uiRetry,
                validationProfile, api, uiReportDirectory, apiReportDirectory,
                authenticatedE2eEnabled, PracticeShopReportConfiguration.defaults());
    }

    public boolean accountConfigured() {
        if (!accountPolicy().accepts(accountFile)) return false;
        if (!java.nio.file.Files.isRegularFile(accountFile)) return false;
        try {
            return PracticeShopCredentials.load(accountFile).hasLiveAccountCandidate();
        } catch (IllegalArgumentException | IllegalStateException invalidFixture) {
            // An unreadable, incomplete or reserved-domain fixture must fail closed.
            return false;
        }
    }

    public boolean authenticatedE2eAllowed() {
        return authenticatedE2eEnabled && accountConfigured();
    }

    /**
     * Creates the explicit live-auth launch profile used by the IntelliJ E2E
     * annotation. YAML remains the source for the business fixture; this
     * method only promotes the selected test into an authenticated live run
     * and applies the browser settings declared by that test.
     */
    public PracticeShopConfiguration forAuthenticatedE2e(Path fixture, boolean headless, int zoomPercent) {
        UiBrowserConfiguration selectedBrowser = new UiBrowserConfiguration(
                browser.baseUrl(), browser.browser(), headless, browser.timeout(),
                browser.navigationTimeout(), browser.viewportWidth(), browser.viewportHeight(),
                zoomPercent, browser.screenshotOnFailure(), browser.retryPolicy(), browser.networkInspection());
        PracticeShopApiConfiguration selectedApi = new PracticeShopApiConfiguration(
                api.endpoint(), ApiTransportMode.LIVE, PracticeShopApiPrecondition.AUTHENTICATED,
                api.timeout(), api.evidenceBinding());
        return new PracticeShopConfiguration(baseUrl, fixture, productName, productPath, checkout,
                selectedBrowser, uiRetry, validationProfile, selectedApi, uiReportDirectory,
                apiReportDirectory, true, reporting);
    }

    /** The module-owned synthetic fixture is the only supported live-auth source. */
    public PracticeShopAccountFixturePolicy accountPolicy() {
        return PracticeShopAccountFixturePolicy.SOURCE_TEST_RESOURCE;
    }

    public URI uri(String path) {
        String suffix = path == null ? "" : path.trim();
        if (suffix.isEmpty()) return baseUrl;
        return baseUrl.resolve(suffix.startsWith("/") ? suffix.substring(1) : suffix);
    }

    /** True when the external catalog owns the product id and may rotate it. */
    public boolean usesDynamicProductPath() {
        return DYNAMIC_PRODUCT_PATH.equalsIgnoreCase(productPath);
    }

    public String productPathExpectation() {
        return usesDynamicProductPath()
                ? PracticeShopUiRoutes.PRODUCT_PATTERN + " (dynamic fixture)"
                : productPath;
    }

    /** Returns the same runtime fixture with a profile-scoped evidence policy. */
    public PracticeShopConfiguration withValidationProfile(PracticeShopValidationProfile profile) {
        PracticeShopValidationProfile selected = Objects.requireNonNull(profile, "profile");
        PracticeShopApiEvidenceBinding binding = selected.apiEvidenceRequired()
                ? api.evidenceBinding() : PracticeShopApiEvidenceBinding.none();
        if (selected.apiEvidenceRequired() && binding.checkpoints().isEmpty()
                && !validationProfile.apiEvidenceRequired()) {
            binding = PracticeShopApiEvidenceBinding.all();
        }
        PracticeShopApiConfiguration selectedApi = new PracticeShopApiConfiguration(
                api.endpoint(), api.mode(), api.precondition(), api.timeout(), binding);
        return new PracticeShopConfiguration(baseUrl, accountFile, productName, productPath,
                checkout, browser, uiRetry, selected, selectedApi,
                uiReportDirectory, apiReportDirectory, authenticatedE2eEnabled, reporting);
    }

    public boolean matchesProductPath(String selectedPath) {
        if (selectedPath == null || !selectedPath.startsWith(PracticeShopUiRoutes.PRODUCT_PREFIX)) {
            return false;
        }
        return usesDynamicProductPath() || productPath.equals(selectedPath);
    }

    private static String required(String value, String name) {
        String normalized = Objects.requireNonNull(value, name).trim();
        if (normalized.isEmpty()) throw new IllegalArgumentException(name + " must not be blank");
        return normalized;
    }

}


