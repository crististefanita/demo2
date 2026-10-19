package atf.api.reporting.runtime.evidence;

public final class ExtentReportRuntimeEvidenceLifecycle {
    private ExtentReportRuntimeEvidenceLifecycle() {}

    public static String script() {
        return ""
                + "var atfLegacyNormalizeEvidenceDisclosures=normalizeEvidenceDisclosures;normalizeEvidenceDisclosures=function(){normalizeSemanticEvidenceDisclosures();atfLegacyNormalizeEvidenceDisclosures();normalizePhaseFailureDisclosures();q('#test-view .test-content-detail [data-atf-node-role=\"request\"]').forEach(appendUiFailureEvidence);markLastSemanticEvidence();};";
    }
}

