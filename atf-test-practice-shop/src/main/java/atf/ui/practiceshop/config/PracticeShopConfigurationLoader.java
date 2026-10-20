package atf.ui.practiceshop.config;

import atf.api.ui.config.UiBrowserConfiguration;
import atf.api.core.config.ApiTransportMode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

/** YAML-first setup with explicit process overrides for local/CI account reuse. */
public final class PracticeShopConfigurationLoader {
    public static final String RESOURCE = "atf-practice-shop.yaml";
    private static final ObjectMapper YAML = new ObjectMapper(new YAMLFactory());

    private PracticeShopConfigurationLoader() { }

    public static PracticeShopConfiguration load() {
        Path account = Path.of(System.getProperty("user.dir", "."))
                .resolve("target/practice-software-testing-account.properties");
        try (var stream = openConfiguration()) {
            if (stream == null) return override(PracticeShopConfiguration.defaults(account));
            JsonNode root = YAML.readTree(stream).path("practiceShop");
            URI base = URI.create(text(root, "baseUrl", "https://practicesoftwaretesting.com"));
            Path configuredAccount = resolvePath(text(root, "accountFile", account.toString()));
            String product = text(root, "productName", "Pliers");
            String path = text(root, "productPath", PracticeShopConfiguration.DYNAMIC_PRODUCT_PATH);
            PracticeShopValidationProfile validationProfile = PracticeShopValidationProfile.from(
                    text(root.path("validation"), "profile", "ui-api"));
            JsonNode api = root.path("api");
            PracticeShopApiConfiguration apiConfiguration = new PracticeShopApiConfiguration(
                    URI.create(text(api, "endpoint", PracticeShopApiConfiguration.defaults().endpoint().toString())),
                    ApiTransportMode.parse(text(api, "mode", "acceptance")),
                    PracticeShopApiPrecondition.from(text(api, "precondition", "authenticated")),
                    duration(api, "timeout", Duration.ofSeconds(20)));
            JsonNode checkout = root.path("checkout");
            JsonNode guest = checkout.path("guest");
            JsonNode billing = checkout.path("billing");
            PracticeShopCheckoutConfiguration checkoutConfiguration =
                    new PracticeShopCheckoutConfiguration(
                            integer(checkout, "quantity", 2),
                            new PracticeShopGuest(text(guest, "email",
                                    "atf.practice.shop@example.test"),
                                    text(guest, "firstName", "ATF"),
                                    text(guest, "lastName", "PracticeShop")),
                            new PracticeShopBillingAddress(text(billing, "country", "Romania"),
                                    text(billing, "postalCode", "010101"),
                                    text(billing, "houseNumber", "10"),
                                    text(billing, "street", "Test Street"),
                                    text(billing, "city", "Bucharest"),
                                    text(billing, "state", "Bucharest")),
                            text(checkout, "paymentMethod", "Cash on Delivery"));
            return override(new PracticeShopConfiguration(base, configuredAccount, product, path,
                    checkoutConfiguration,
                    new UiBrowserConfiguration(base, text(root, "browser", "chromium"),
                            bool(root, "headless", true), duration(root, "timeout", Duration.ofSeconds(20)),
                            duration(root, "navigationTimeout", Duration.ofSeconds(45)),
                            integer(root.path("viewport"), "width", 1440),
                            integer(root.path("viewport"), "height", 1000),
                            integer(root, "zoomPercent", 100),
                            bool(root, "screenshotOnFailure", true)), validationProfile,
                    apiConfiguration));
        } catch (IOException | RuntimeException error) {
            throw new IllegalStateException("Cannot load " + RESOURCE, error);
        }
    }

    /**
     * The application fixture is owned by this module. The filesystem fallback
     * keeps local launches from an IDE and a staging checkout deterministic.
     */
    private static InputStream openConfiguration() throws IOException {
        InputStream classpath = PracticeShopConfigurationLoader.class.getClassLoader()
                .getResourceAsStream(RESOURCE);
        if (classpath != null) return classpath;

        Path current = Path.of(System.getProperty("user.dir", "."))
                .toAbsolutePath().normalize();
        while (current != null) {
            Path file = current.resolve("atf-test-practice-shop/src/test/resources").resolve(RESOURCE);
            if (Files.isRegularFile(file)) return Files.newInputStream(file);
            current = current.getParent();
        }
        return null;
    }

    private static PracticeShopConfiguration override(PracticeShopConfiguration base) {
        String baseUrl = System.getProperty("atf.practice.base-url", base.baseUrl().toString());
        String account = System.getProperty("atf.practice.account-file",
                System.getenv().getOrDefault("ATF_PRACTICE_ACCOUNT_FILE", "")).trim();
        Path accountFile = account.isEmpty() ? base.accountFile() : resolvePath(account);
        String product = System.getProperty("atf.practice.product-name", base.productName());
        String productPath = System.getProperty("atf.practice.product-path", base.productPath());
        String profile = System.getProperty("atf.practice.validation-profile", base.validationProfile().name());
        String apiEndpoint = System.getProperty("atf.practice.api-endpoint", base.api().endpoint().toString());
        String apiMode = System.getProperty("atf.practice.api-mode", base.api().mode().name());
        String apiPrecondition = System.getProperty("atf.practice.api-precondition",
                base.api().precondition().name());
        URI uri = URI.create(baseUrl);
        UiBrowserConfiguration browser = new UiBrowserConfiguration(uri, base.browser().browser(),
                base.browser().headless(), base.browser().timeout(), base.browser().navigationTimeout(),
                base.browser().viewportWidth(), base.browser().viewportHeight(), base.browser().zoomPercent(),
                base.browser().screenshotOnFailure());
        return new PracticeShopConfiguration(uri, accountFile, product, productPath,
                base.checkout(), browser, PracticeShopValidationProfile.from(profile),
                new PracticeShopApiConfiguration(URI.create(apiEndpoint), ApiTransportMode.parse(apiMode),
                        PracticeShopApiPrecondition.from(apiPrecondition), base.api().timeout()));
    }

    private static String text(JsonNode parent, String key, String fallback) {
        JsonNode value = parent.path(key);
        return value.isTextual() && !value.asText().isBlank() ? value.asText().trim() : fallback;
    }

    private static Path resolvePath(String configured) {
        Path path = Path.of(configured);
        if (path.isAbsolute()) return path.normalize();
        Path current = Path.of(System.getProperty("user.dir", ".")).toAbsolutePath().normalize();
        while (current != null) {
            Path candidate = current.resolve(path).normalize();
            if (Files.isRegularFile(candidate)) return candidate;
            current = current.getParent();
        }
        return Path.of(System.getProperty("user.dir", ".")).toAbsolutePath().normalize()
                .resolve(path).normalize();
    }

    private static boolean bool(JsonNode parent, String key, boolean fallback) {
        JsonNode value = parent.path(key);
        return value.isBoolean() ? value.booleanValue() : fallback;
    }

    private static int integer(JsonNode parent, String key, int fallback) {
        JsonNode value = parent.path(key);
        return value.canConvertToInt() && value.intValue() > 0 ? value.intValue() : fallback;
    }

    private static Duration duration(JsonNode parent, String key, Duration fallback) {
        try { return Duration.parse(text(parent, key, fallback.toString())); }
        catch (RuntimeException ignored) { return fallback; }
    }
}


