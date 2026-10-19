package atf.api.reporting.runtime.evidence;

import atf.api.reporting.asset.ReportAssetComposer;

/** Java facade for the native evidence-disclosure asset. */
public final class ExtentReportRuntimeEvidenceDisclosure {
    private ExtentReportRuntimeEvidenceDisclosure() {
    }

    public static String script() {
        return ReportAssetComposer.extentEvidenceDisclosureScript();
    }
}
