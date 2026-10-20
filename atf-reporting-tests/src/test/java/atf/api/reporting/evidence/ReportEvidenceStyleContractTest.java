package atf.api.reporting.evidence;
import atf.api.testkit.AtfUnitTest;

import atf.api.reporting.support.TestReportPaths;
import atf.api.reporting.evidence.ReportEvidenceStyleContract;
import atf.api.reporting.theme.AtfReportTheme;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

@AtfUnitTest
class ReportEvidenceStyleContractTest {
    @Test
    void sharedShellContractCoversBothSemanticFamilies() {
        String css = AtfReportTheme.css();
        String keyboardContract = "summary.setAttribute('aria-expanded','false');details.open?'true':'false'";
        String ui = "<html><style>" + css + "</style><script>" + keyboardContract + "</script>"
                + "<details class=\"atf-ui-action-sequence\">"
                + "<details class=\"atf-ui-observations\"><a class=\"atf-ui-action-screenshot-link\"></a>";
        String api = "<html><style>" + css + "</style><script>" + keyboardContract + "</script>"
                + "<details class=\"request-evidence\">"
                + "<details class=\"response-evidence\"><details class=\"execution-evidence\">"
                + "<table class=\"helm-evidence-table helm-validation-table\"></table>";

        Path directory = TestReportPaths.report("style-contract-ui.html").getParent();
        try {
            Files.createDirectories(directory);
            Path uiPath = directory.resolve("style-contract-ui.html");
            Path apiPath = directory.resolve("style-contract-api.html");
            Files.writeString(uiPath, ui);
            Files.writeString(apiPath, api);
            var comparison = ReportEvidenceStyleContract.compare(uiPath, apiPath);
            assertTrue(comparison.compliant(), () -> "UI=" + comparison.ui().checks()
                    + " API=" + comparison.api().checks()
                    + " cross=" + comparison.crossChecks());

            Files.writeString(apiPath, api.replace("--atf-evidence-status-column-width:52px",
                    "--atf-evidence-status-column-width:60px"));
            var drift = ReportEvidenceStyleContract.compare(uiPath, apiPath);
            assertFalse(drift.compliant());
            assertFalse(drift.crossChecks().get("shared token --atf-evidence-status-column-width"));
        } catch (java.io.IOException error) {
            throw new AssertionError(error);
        }
    }
}
