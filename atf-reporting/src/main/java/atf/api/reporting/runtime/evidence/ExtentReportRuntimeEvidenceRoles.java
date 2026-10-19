package atf.api.reporting.runtime.evidence;

import atf.api.reporting.asset.ReportAssetComposer;

public final class ExtentReportRuntimeEvidenceRoles {
    private ExtentReportRuntimeEvidenceRoles() {
    }

    public static String script() {
        return ReportAssetComposer.extentEvidenceRolesScript();
    }
}

