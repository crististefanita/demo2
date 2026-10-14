package atf.api.reporting;

import java.util.Map;

@FunctionalInterface
interface ReportArtifactContractGroup {
    void evaluate(String html, Map<String, Boolean> checks);
}
