package atf.ui.practiceshop.config;

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


