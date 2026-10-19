package atf.api.reporting.asset;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;

/** Loads and expands browser assets from the reporting library classpath. */
public final class ReportAssetLoader {
    private static final String TEMPLATE_PREFIX = "${";
    private static final String TEMPLATE_SUFFIX = "}";

    public String load(ReportAsset asset) {
        ReportAsset.require(asset);
        try (InputStream stream = ReportAssetLoader.class.getResourceAsStream(asset.resourcePath())) {
            if (stream == null) {
                throw new IllegalStateException("Missing reporting asset: " + asset.resourcePath());
            }
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException error) {
            throw new IllegalStateException("Cannot read reporting asset: " + asset.resourcePath(), error);
        }
    }

    public String render(ReportAsset asset, Map<String, String> variables) {
        String source = load(asset);
        for (var entry : Objects.requireNonNull(variables, "variables").entrySet()) {
            String key = Objects.requireNonNull(entry.getKey(), "template variable");
            String value = Objects.requireNonNull(entry.getValue(), "template value for " + key);
            source = source.replace(TEMPLATE_PREFIX + key + TEMPLATE_SUFFIX, value);
        }
        if (source.contains(TEMPLATE_PREFIX)) {
            throw new IllegalStateException("Unresolved reporting asset template in " + asset.resourcePath());
        }
        return source;
    }
}







