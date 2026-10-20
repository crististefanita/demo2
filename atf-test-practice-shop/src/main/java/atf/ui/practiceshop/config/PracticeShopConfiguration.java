package atf.ui.practiceshop.config;

import atf.api.ui.config.UiBrowserConfiguration;

import java.net.URI;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Objects;

/** Immutable external-system policy for the Practice Software Testing suite. */
public record PracticeShopConfiguration(
        URI baseUrl,
        Path accountFile,
        String productName,
        String productPath,
        PracticeShopCheckoutConfiguration checkout,
        UiBrowserConfiguration browser,
        PracticeShopValidationProfile validationProfile,
        PracticeShopApiConfiguration api) {
    public static final String DYNAMIC_PRODUCT_PATH = "auto";

    public PracticeShopConfiguration {
        baseUrl = Objects.requireNonNull(baseUrl, "baseUrl");
        accountFile = Objects.requireNonNull(accountFile, "accountFile").toAbsolutePath().normalize();
        productName = required(productName, "productName");
        productPath = required(productPath, "productPath");
        checkout = Objects.requireNonNull(checkout, "checkout");
        browser = Objects.requireNonNull(browser, "browser");
        validationProfile = Objects.requireNonNull(validationProfile, "validationProfile");
        api = Objects.requireNonNull(api, "api");
    }

    /** Compatibility constructor for UI-only callers. */
    public PracticeShopConfiguration(URI baseUrl, Path accountFile, String productName,
                                     String productPath, PracticeShopCheckoutConfiguration checkout,
                                     UiBrowserConfiguration browser,
                                     PracticeShopValidationProfile validationProfile) {
        this(baseUrl, accountFile, productName, productPath, checkout, browser,
                validationProfile, PracticeShopApiConfiguration.defaults());
    }

    public boolean accountConfigured() {
        return java.nio.file.Files.isRegularFile(accountFile);
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
        return usesDynamicProductPath() ? "/product/* (dynamic fixture)" : productPath;
    }

    public boolean matchesProductPath(String selectedPath) {
        if (selectedPath == null || !selectedPath.startsWith("/product/")) return false;
        return usesDynamicProductPath() || productPath.equals(selectedPath);
    }

    private static String required(String value, String name) {
        String normalized = Objects.requireNonNull(value, name).trim();
        if (normalized.isEmpty()) throw new IllegalArgumentException(name + " must not be blank");
        return normalized;
    }

    static PracticeShopConfiguration defaults(Path accountFile) {
        URI base = URI.create("https://practicesoftwaretesting.com");
        return new PracticeShopConfiguration(base, accountFile, "Pliers",
                DYNAMIC_PRODUCT_PATH, PracticeShopCheckoutConfiguration.defaults(),
                new UiBrowserConfiguration(base, "chromium", true,
                        Duration.ofSeconds(20), Duration.ofSeconds(45),
                        1440, 1000, 70, true), PracticeShopValidationProfile.UI_API,
                PracticeShopApiConfiguration.defaults());
    }
}


