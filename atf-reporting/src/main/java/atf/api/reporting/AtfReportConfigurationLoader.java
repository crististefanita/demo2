package atf.api.reporting;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Resolves the report output without relying on IntelliJ's current working directory.
 * An explicit system property or environment variable wins; otherwise the loader
 * searches from the working directory upwards for {@code atf.yaml}/{@code atf.yml}.
 */
public final class AtfReportConfigurationLoader {
    public static final String CONFIG_PROPERTY = "atf.report.config";
    public static final String CONFIG_ENVIRONMENT_VARIABLE = "ATF_REPORT_CONFIG";
    public static final List<String> DEFAULT_CONFIG_NAMES = List.of("atf.yaml", "atf.yml");

    private static final ObjectMapper YAML = new ObjectMapper(new YAMLFactory());

    private AtfReportConfigurationLoader() {
    }

    public static ExtentReportConfiguration load() {
        return load(AtfReportConfigurationContext.fromProcess());
    }

    /**
     * Loads from an explicit immutable context. No process-global state is
     * sampled after this method receives the context.
     */
    public static ExtentReportConfiguration load(AtfReportConfigurationContext context) {
        Objects.requireNonNull(context, "context");
        Path explicit = context.explicitConfigPath();
        if (explicit != null) {
            Path resolved = explicit.isAbsolute()
                    ? explicit
                    : context.startDirectory().resolve(explicit);
            return load(resolved);
        }

        Path discovered = discover(context.startDirectory());
        return discovered == null
                ? ExtentReportConfiguration.defaults(context.startDirectory())
                : load(discovered);
    }

    public static ExtentReportConfiguration load(Path configPath) {
        Objects.requireNonNull(configPath, "configPath");
        Path absoluteConfig = configPath.toAbsolutePath().normalize();
        if (!Files.isRegularFile(absoluteConfig)) {
            throw new IllegalStateException("ATF report configuration does not exist: " + absoluteConfig);
        }

        try {
            JsonNode root = YAML.readTree(absoluteConfig.toFile());
            JsonNode extent = root.path("reporting").path("extent");
            String outputDirectory = text(extent, "outputDirectory", "target");
            String fileName = text(extent, "fileName", ExtentReportConfiguration.DEFAULT_FILE_NAME);
            boolean timestampEnabled = bool(extent, "timestampEnabled", true);
            String timestampPattern = text(extent, "timestampPattern",
                    ExtentReportConfiguration.DEFAULT_TIMESTAMP_PATTERN);
            String timestampZone = text(extent, "timestampZone",
                    ExtentReportConfiguration.DEFAULT_TIMESTAMP_ZONE);
            Path baseDirectory = absoluteConfig.getParent() == null
                    ? Path.of(".").toAbsolutePath().normalize()
                    : absoluteConfig.getParent();
            Path directory = Path.of(outputDirectory);
            if (!directory.isAbsolute()) {
                directory = baseDirectory.resolve(directory);
            }
            directory = directory.toAbsolutePath().normalize();
            if (!directory.startsWith(baseDirectory)) {
                throw new IllegalStateException(
                        "ATF report outputDirectory escapes the configuration directory: " + outputDirectory);
            }
            return new ExtentReportConfiguration(directory, fileName, timestampEnabled,
                    timestampPattern, timestampZone);
        } catch (IOException error) {
            throw new IllegalStateException("Cannot read ATF report configuration: " + absoluteConfig, error);
        }
    }

    public static Path discover(Path startDirectory) {
        Path current = Objects.requireNonNull(startDirectory, "startDirectory")
                .toAbsolutePath().normalize();
        if (!Files.isDirectory(current)) {
            current = current.getParent();
        }
        while (current != null) {
            for (String name : DEFAULT_CONFIG_NAMES) {
                Path candidate = current.resolve(name);
                if (Files.isRegularFile(candidate)) {
                    return candidate;
                }
            }
            current = current.getParent();
        }
        return null;
    }

    private static String text(JsonNode parent, String name, String defaultValue) {
        JsonNode value = parent.path(name);
        if (value.isMissingNode() || value.isNull()) {
            return defaultValue;
        }
        if (!value.isTextual() || value.asText().isBlank()) {
            throw new IllegalStateException("ATF report configuration value must be text: reporting.extent." + name);
        }
        return value.asText().trim();
    }

    private static boolean bool(JsonNode parent, String name, boolean defaultValue) {
        JsonNode value = parent.path(name);
        if (value.isMissingNode() || value.isNull()) {
            return defaultValue;
        }
        if (value.isBoolean()) {
            return value.booleanValue();
        }
        throw new IllegalStateException("ATF report configuration value must be boolean: reporting.extent." + name);
    }

}
