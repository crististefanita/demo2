package atf.ui.autoex.config;

import atf.api.testkit.files.AtfTestResourceReader;
import atf.api.ui.config.UiBrowserConfiguration;
import atf.api.ui.config.UiNetworkInspectionConfiguration;
import atf.api.ui.config.UiRetryPolicy;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

/** Reads module-owned YAML once into validated browser and demo data. */
public final class AutoExConfigurationLoader {
    public static final String CONFIG_PROPERTY = "atf.autoex.config";
    public static final String ENABLE_PROPERTY = "atf.autoex.e2e.enabled";
    public static final String DEFAULT_RESOURCE = "atf-auto-ex.yaml";

    private static final ObjectMapper YAML = new ObjectMapper(new YAMLFactory());

    private AutoExConfigurationLoader() { }

    public static AutoExConfiguration load() {
        String selected = System.getProperty(CONFIG_PROPERTY);
        return selected == null || selected.isBlank()
                ? parse(AtfTestResourceReader.readResourceUtf8(
                        AutoExConfigurationLoader.class, DEFAULT_RESOURCE))
                : load(Path.of(selected));
    }

    public static AutoExConfiguration load(Path path) {
        Path source = path.toAbsolutePath().normalize();
        if (!Files.isRegularFile(source)) {
            throw new IllegalStateException("Automation Exercise configuration is missing: " + source);
        }
        try {
            return parse(Files.readString(source));
        } catch (IOException error) {
            throw new IllegalStateException("Cannot read Automation Exercise configuration: " + source, error);
        }
    }

    public static boolean e2eEnabled(AutoExConfiguration configuration) {
        String override = System.getProperty(ENABLE_PROPERTY);
        return override == null ? configuration.e2eEnabled() : Boolean.parseBoolean(override);
    }

    private static AutoExConfiguration parse(String source) {
        try {
            JsonNode root = YAML.readTree(source);
            JsonNode site = requiredNode(root, "automationExercise");
            URI baseUrl = URI.create(requiredText(site, "baseUrl"));
            if (baseUrl.getHost() == null || !("https".equalsIgnoreCase(baseUrl.getScheme())
                    || "http".equalsIgnoreCase(baseUrl.getScheme()))) {
                throw new IllegalArgumentException("baseUrl must be an absolute HTTP(S) URI");
            }
            JsonNode browser = requiredNode(site, "browser");
            JsonNode viewport = requiredNode(browser, "viewport");
            UiBrowserConfiguration browserConfiguration = new UiBrowserConfiguration(baseUrl,
                    text(browser, "engine", "chromium"), bool(browser, "headless", true),
                    Duration.parse(text(browser, "timeout", "PT20S")),
                    Duration.parse(text(browser, "navigationTimeout", "PT45S")),
                    integer(viewport, "width", 1440), integer(viewport, "height", 1000),
                    integer(browser, "zoomPercent", UiBrowserConfiguration.DEFAULT_ZOOM_PERCENT),
                    true, UiRetryPolicy.none(), UiNetworkInspectionConfiguration.disabled());

            JsonNode reports = requiredNode(site, "reports");
            JsonNode catalog = requiredNode(site, "catalog");
            JsonNode registration = requiredNode(site, "registration");
            JsonNode payment = requiredNode(site, "payment");
            return new AutoExConfiguration(baseUrl, bool(site, "e2eEnabled", false), browserConfiguration,
                    Path.of(requiredText(reports, "uiBaseDirectory")),
                    new AutoExConfiguration.Catalog(requiredText(catalog, "searchTerm"),
                            requiredText(catalog, "productId"), requiredText(catalog, "productName"),
                            requiredText(catalog, "unitPrice"), integer(catalog, "quantity", 1)),
                    new AutoExConfiguration.Registration(requiredText(registration, "title"),
                            requiredText(registration, "birthDay"), requiredText(registration, "birthMonth"),
                            requiredText(registration, "birthYear"), requiredText(registration, "firstName"),
                            requiredText(registration, "lastName"), requiredText(registration, "company"),
                            requiredText(registration, "address1"), requiredText(registration, "address2"),
                            requiredText(registration, "country"), requiredText(registration, "state"),
                            requiredText(registration, "city"), requiredText(registration, "postalCode"),
                            requiredText(registration, "mobileNumber")),
                    new AutoExConfiguration.Payment(requiredText(payment, "nameOnCard"),
                            requiredText(payment, "cardNumber"), requiredText(payment, "cvc"),
                            requiredText(payment, "expiryMonth"), requiredText(payment, "expiryYear")));
        } catch (IOException | RuntimeException error) {
            if (error instanceof IllegalArgumentException argument) throw argument;
            throw new IllegalStateException("Invalid Automation Exercise configuration", error);
        }
    }

    private static JsonNode requiredNode(JsonNode parent, String name) {
        JsonNode node = parent.path(name);
        if (!node.isObject()) throw new IllegalArgumentException("Missing configuration object: " + name);
        return node;
    }

    private static String requiredText(JsonNode parent, String name) {
        JsonNode value = parent.path(name);
        if (!value.isTextual() || value.asText().isBlank()) {
            throw new IllegalArgumentException("Missing configuration value: " + name);
        }
        return value.asText().trim();
    }

    private static String text(JsonNode parent, String name, String fallback) {
        JsonNode value = parent.path(name);
        return value.isMissingNode() || value.isNull() ? fallback : requiredText(parent, name);
    }

    private static int integer(JsonNode parent, String name, int fallback) {
        JsonNode value = parent.path(name);
        if (value.isMissingNode() || value.isNull()) return fallback;
        if (!value.canConvertToInt()) throw new IllegalArgumentException("Invalid integer configuration: " + name);
        return value.intValue();
    }

    private static boolean bool(JsonNode parent, String name, boolean fallback) {
        JsonNode value = parent.path(name);
        if (value.isMissingNode() || value.isNull()) return fallback;
        if (!value.isBoolean()) throw new IllegalArgumentException("Invalid boolean configuration: " + name);
        return value.booleanValue();
    }
}
