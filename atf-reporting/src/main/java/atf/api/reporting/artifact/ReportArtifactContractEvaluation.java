package atf.api.reporting.artifact;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Collections;

record ReportArtifactContractEvaluation(
        Map<String, Boolean> checks,
        Map<String, String> owners) {
    ReportArtifactContractEvaluation {
        checks = Collections.unmodifiableMap(new LinkedHashMap<>(checks));
        owners = Collections.unmodifiableMap(new LinkedHashMap<>(owners));
    }
}
