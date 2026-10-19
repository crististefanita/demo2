package atf.api.reporting.configuration;
import atf.api.core.http.execution.ApiExecutor;
import atf.api.core.http.transport.ApiTransport;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Collections;

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
}
