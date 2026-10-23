package atf.ui.tests.practiceshop.support.lifecycle;


import atf.api.reporting.evidence.ReportScreenshotPolicy;
import atf.api.reporting.evidence.profile.ReportEvidenceFlowMode;
import atf.api.reporting.evidence.profile.ReportEvidenceProfile;
import atf.ui.practiceshop.config.ShopConfiguration;
import atf.ui.practiceshop.config.ShopConfigurationLoader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShopE2eContextEvidenceProfileTest {
    @TempDir
    Path temporaryReports;

    @Test
    void reportCompositionHonorsExplicitCapturePolicyOverYamlDefault() {
        ShopConfiguration yaml = ShopConfigurationLoader.load();
        ShopConfiguration isolated = new ShopConfiguration(
                yaml.baseUrl(), yaml.accountFile(), yaml.productName(), yaml.productPath(),
                yaml.checkout(), yaml.browser(), yaml.uiRetry(), yaml.validationProfile(), yaml.api(),
                temporaryReports, temporaryReports, yaml.authenticatedE2eEnabled(),
                yaml.reporting());
        ReportEvidenceProfile explicit = ReportEvidenceProfile.builder()
                .flowMode(ReportEvidenceFlowMode.UNIFIED_FLOW)
                .screenshotPolicy(ReportScreenshotPolicy.NONE)
                .build();

        try (var report = ShopE2eContext.openReport(isolated, explicit)) {
            assertEquals(ReportEvidenceFlowMode.UNIFIED_FLOW, report.evidenceProfile().flowMode());
            assertEquals(ReportScreenshotPolicy.NONE,
                    report.evidenceProfile().screenshotPolicy());
        }
    }

}
