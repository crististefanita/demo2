package atf.ui.practiceshop.config;

import atf.api.ui.config.UiBrowserConfiguration;
import atf.api.ui.config.UiRetryOperation;
import atf.api.ui.config.UiRetryPolicy;
import atf.api.ui.config.UiNetworkInspectionConfiguration;
import atf.api.ui.config.UiNetworkSettlementPolicy;
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
import java.util.EnumSet;
import java.util.Set;
import java.util.Collection;
import java.util.stream.StreamSupport;
import java.util.LinkedHashSet;

/** YAML-first setup with explicit process overrides for local/CI account reuse. */
public final class PracticeShopConfigurationLoader {
    public static final String RESOURCE = "atf-practice-shop.yaml";
    private static final ObjectMapper YAML = new ObjectMapper(new YAMLFactory());

    private PracticeShopConfigurationLoader() { }

    public static PracticeShopConfiguration load() {
        try (var stream = openConfiguration()) {
            if (stream == null) {
                throw new IllegalStateException("Missing " + RESOURCE
                        + "; practiceShop.baseUrl must be supplied by the module YAML");
            }
            return load(stream);
        } catch (IOException | RuntimeException error) {
            throw new IllegalStateException("Cannot load " + RESOURCE, error);
        }
    }

    /** Loads an explicit YAML file for component tests and controlled launches. */
    public static PracticeShopConfiguration load(Path configurationFile) {
        try (InputStream stream = Files.newInputStream(configurationFile)) {
            return load(stream);
        } catch (IOException | RuntimeException error) {
            throw new IllegalStateException("Cannot load Practice Shop configuration: " + configurationFile,
                    error);
        }
    }

    private static PracticeShopConfiguration load(InputStream stream) throws IOException {
            Path account = Path.of(System.getProperty("user.dir", "."))
                    .resolve("target/practice-software-testing-account.properties");
            JsonNode root = YAML.readTree(stream).path("practiceShop");
            URI base = URI.create(requiredText(root, "baseUrl"));
            Path configuredAccount = resolvePath(text(root, "accountFile", account.toString()));
            String product = text(root, "productName", "Pliers");
            String path = text(root, "productPath", PracticeShopConfiguration.DYNAMIC_PRODUCT_PATH);
            PracticeShopValidationProfile validationProfile = PracticeShopValidationProfile.from(
                    text(root.path("validation"), "profile", "ui-api"));
            PracticeShopApiEvidenceBinding evidenceBinding = apiEvidenceBinding(
                    root.path("validation"), validationProfile);
            PracticeShopUiRetryConfiguration uiRetry = retry(root.path("retry"));
            UiNetworkInspectionConfiguration networkInspection = networkInspection(
                    root.path("networkInspector"));
            JsonNode api = root.path("api");
            PracticeShopApiConfiguration apiConfiguration = new PracticeShopApiConfiguration(
                    URI.create(text(api, "endpoint", PracticeShopApiConfiguration.defaults().endpoint().toString())),
                    ApiTransportMode.parse(text(api, "mode", "acceptance")),
                    PracticeShopApiPrecondition.from(text(api, "precondition", "authenticated")),
                    duration(api, "timeout", Duration.ofSeconds(20)), evidenceBinding);
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
            JsonNode reports = root.path("reports");
            Path uiReports = resolveDirectory(text(reports, "ui", "artifacts/reports/ui"));
            Path apiReports = resolveDirectory(text(reports, "api", "artifacts/reports/api"));
            boolean authenticatedE2e = bool(root, "authenticatedE2eEnabled", false);
            return override(new PracticeShopConfiguration(base, configuredAccount, product, path,
                    checkoutConfiguration,
                    new UiBrowserConfiguration(base, text(root, "browser", "chromium"),
                            bool(root, "headless", true), duration(root, "timeout", Duration.ofSeconds(20)),
                            duration(root, "navigationTimeout", Duration.ofSeconds(45)),
                            integer(root.path("viewport"), "width", 1440),
                            integer(root.path("viewport"), "height", 1000),
                            integer(root, "zoomPercent", 100),
                            bool(root, "screenshotOnFailure", true),
                            uiRetry.policy(), networkInspection), uiRetry, validationProfile,
                    apiConfiguration, uiReports, apiReports, authenticatedE2e));
    }

    private static PracticeShopUiRetryConfiguration retry(JsonNode retry) {
        int maxAttempts = positiveInteger(retry, "maxAttempts", UiRetryPolicy.DEFAULT_MAX_ATTEMPTS,
                "practiceShop.retry.maxAttempts");
        Set<UiRetryOperation> operations = retryOperations(retry);
        return new PracticeShopUiRetryConfiguration(maxAttempts,
                duration(retry, "backoff", UiRetryPolicy.DEFAULT_BACKOFF),
                operations, text(retry, "reason", UiRetryPolicy.DEFAULT_REASON));
    }

    private static Set<UiRetryOperation> retryOperations(JsonNode retry) {
        JsonNode configured = retry.path("eligibleOperations");
        if (configured.isMissingNode() || configured.isNull()) return Set.of();
        if (!configured.isArray()) {
            throw new IllegalArgumentException(
                    "Configuration key practiceShop.retry.eligibleOperations must be an array");
        }
        EnumSet<UiRetryOperation> operations = EnumSet.noneOf(UiRetryOperation.class);
        StreamSupport.stream(configured.spliterator(), false).forEach(node -> {
            if (!node.isTextual() || node.asText().isBlank()) {
                throw new IllegalArgumentException(
                        "Configuration key practiceShop.retry.eligibleOperations must contain text values");
            }
            operations.add(UiRetryOperation.from(node.asText()));
        });
        return operations;
    }

    private static UiNetworkInspectionConfiguration networkInspection(JsonNode network) {
        boolean enabled = bool(network, "enabled", false);
        Set<String> hosts = textSet(network.path("allowedHosts"),
                "practiceShop.networkInspector.allowedHosts");
        UiNetworkSettlementPolicy settlement = new UiNetworkSettlementPolicy(
                bool(network.path("settlement"), "enabled", false),
                duration(network.path("settlement"), "timeout", Duration.ofSeconds(10)),
                duration(network.path("settlement"), "pollInterval", Duration.ofMillis(100)));
        return new UiNetworkInspectionConfiguration(enabled, hosts,
                positiveInteger(network, "maxObservations", 256,
                        "practiceShop.networkInspector.maxObservations"),
                positiveInteger(network, "maxBodyPreviewCharacters", 12_000,
                        "practiceShop.networkInspector.maxBodyPreviewCharacters"),
                bool(network, "collapseConsecutiveSignatures", true), settlement);
    }

    private static Set<String> textSet(JsonNode configured, String path) {
        if (configured.isMissingNode() || configured.isNull()) return Set.of();
        if (!configured.isArray()) {
            throw new IllegalArgumentException("Configuration key " + path + " must be an array");
        }
        Set<String> values = new LinkedHashSet<>();
        for (JsonNode node : configured) {
            if (!node.isTextual() || node.asText().isBlank()) {
                throw new IllegalArgumentException("Configuration key " + path
                        + " must contain text values");
            }
            values.add(node.asText().trim());
        }
        return Set.copyOf(values);
    }

    private static PracticeShopApiEvidenceBinding apiEvidenceBinding(
            JsonNode validation, PracticeShopValidationProfile profile) {
        JsonNode configured = validation.path("apiEvidence");
        if (configured.isMissingNode() || configured.isNull()) {
            return PracticeShopApiEvidenceBinding.forProfile(profile, null);
        }
        if (!configured.isArray()) {
            throw new IllegalArgumentException(
                    "Configuration key practiceShop.validation.apiEvidence must be an array");
        }
        EnumSet<PracticeShopApiCheckpoint> checkpoints = EnumSet.noneOf(PracticeShopApiCheckpoint.class);
        for (JsonNode node : configured) {
            if (!node.isTextual() || node.asText().isBlank()) {
                throw new IllegalArgumentException(
                        "Configuration key practiceShop.validation.apiEvidence must contain text values");
            }
            checkpoints.add(PracticeShopApiCheckpoint.from(node.asText()));
        }
        return PracticeShopApiEvidenceBinding.forProfile(profile, checkpoints);
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
        PracticeShopValidationProfile validationProfile = PracticeShopValidationProfile.from(profile);
        String checkpoints = System.getProperty("atf.practice.api-evidence-checkpoints", "");
        PracticeShopApiEvidenceBinding evidenceBinding = checkpoints.isBlank()
                ? base.api().evidenceBinding()
                : PracticeShopApiEvidenceBinding.fromCsv(validationProfile, checkpoints);
        String apiEndpoint = System.getProperty("atf.practice.api-endpoint", base.api().endpoint().toString());
        String apiMode = System.getProperty("atf.practice.api-mode", base.api().mode().name());
        String apiPrecondition = System.getProperty("atf.practice.api-precondition",
                base.api().precondition().name());
        String uiReports = System.getProperty("atf.practice.ui-report-directory",
                base.uiReportDirectory().toString());
        String apiReports = System.getProperty("atf.practice.api-report-directory",
                base.apiReportDirectory().toString());
        boolean authenticatedE2e = Boolean.parseBoolean(System.getProperty(
                "atf.practice.authenticated-e2e", Boolean.toString(base.authenticatedE2eEnabled())));
        URI uri = URI.create(baseUrl);
        UiBrowserConfiguration browser = new UiBrowserConfiguration(uri, base.browser().browser(),
                base.browser().headless(), base.browser().timeout(), base.browser().navigationTimeout(),
                base.browser().viewportWidth(), base.browser().viewportHeight(), base.browser().zoomPercent(),
                base.browser().screenshotOnFailure(), base.browser().retryPolicy(),
                base.browser().networkInspection());
        return new PracticeShopConfiguration(uri, accountFile, product, productPath,
                base.checkout(), browser, base.uiRetry(), validationProfile,
                new PracticeShopApiConfiguration(URI.create(apiEndpoint), ApiTransportMode.parse(apiMode),
                        PracticeShopApiPrecondition.from(apiPrecondition), base.api().timeout(), evidenceBinding),
                resolveDirectory(uiReports), resolveDirectory(apiReports), authenticatedE2e);
    }

    private static String text(JsonNode parent, String key, String fallback) {
        JsonNode value = parent.path(key);
        return value.isTextual() && !value.asText().isBlank() ? value.asText().trim() : fallback;
    }

    private static String requiredText(JsonNode parent, String key) {
        JsonNode value = parent.path(key);
        if (!value.isTextual() || value.asText().isBlank()) {
            throw new IllegalArgumentException("Configuration key practiceShop." + key
                    + " must be a non-blank YAML value");
        }
        return value.asText().trim();
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

    private static Path resolveDirectory(String configured) {
        Path path = Path.of(configured);
        if (path.isAbsolute()) return path.normalize();
        Path current = Path.of(System.getProperty("user.dir", ".")).toAbsolutePath().normalize();
        while (current != null) {
            if (Files.isRegularFile(current.resolve("atf-reporting/pom.xml"))) {
                return current.resolve(path).normalize();
            }
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

    private static int positiveInteger(JsonNode parent, String key, int fallback, String path) {
        JsonNode value = parent.path(key);
        if (value.isMissingNode() || value.isNull()) return fallback;
        if (value.canConvertToInt() && value.intValue() > 0) return value.intValue();
        throw new IllegalArgumentException("Configuration key " + path
                + " must be a positive integer");
    }

    private static Duration duration(JsonNode parent, String key, Duration fallback) {
        JsonNode value = parent.path(key);
        if (value.isMissingNode() || value.isNull()) return fallback;
        if (!value.isTextual() || value.asText().isBlank()) {
            throw new IllegalArgumentException("Configuration key " + key
                    + " must be an ISO-8601 duration; value=" + value);
        }
        try {
            return Duration.parse(value.asText().trim());
        } catch (java.time.format.DateTimeParseException error) {
            throw new IllegalArgumentException("Configuration key " + key
                    + " has malformed duration: " + value.asText(), error);
        }
    }
}


