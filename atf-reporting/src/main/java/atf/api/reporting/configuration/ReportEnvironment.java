package atf.api.reporting.configuration;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Collections;
import atf.api.reporting.security.SensitiveDataRedactor;

/** Supplies stable execution metadata without coupling lifecycle code to the host runtime. */
public final class ReportEnvironment {
    private ReportEnvironment() {
    }

    public static Map<String, String> defaults() {
        var metadata = new LinkedHashMap<String, String>();
        metadata.put("Environment", "LOCAL");
        metadata.put("API base URL", "configured per ApiExecutor");
        metadata.put("Runtime", "Java " + System.getProperty("java.version", "unknown"));
        metadata.put("OS", System.getProperty("os.name", "unknown") + " "
                + System.getProperty("os.version", "unknown"));
        metadata.put("Client", "ATF ApiTransport (configured per execution)");
        metadata.put("Authentication", "configured per request");
        metadata.put("Test package", "atf-tests");
        metadata.put("Reporting", "Extent · embedded offline HTML");
        return Collections.unmodifiableMap(metadata);
    }

    /**
     * Merges consumer-owned, already semantic metadata into the generic
     * environment without allowing secrets to cross the report boundary.
     */
    public static Map<String, String> with(Map<String, String> consumerMetadata,
                                           SensitiveDataRedactor redactor) {
        var metadata = new LinkedHashMap<>(defaults());
        if (consumerMetadata != null) {
            var safeRedactor = redactor == null ? new SensitiveDataRedactor() : redactor;
            consumerMetadata.forEach((name, value) -> {
                if (name == null || name.isBlank()) return;
                String key = name.trim();
                metadata.put(key, safeRedactor.redactField(key, value));
            });
        }
        return Collections.unmodifiableMap(metadata);
    }
}
