package atf.api.reporting.markup;
import atf.api.testkit.AtfUnitTest;

import atf.api.reporting.markup.vocabulary.AtfReportAsset;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@AtfUnitTest
class AtfReportAssetTest {
    @Test
    void manifestKeepsStableAutonomousAssetOrderAndTypes() {
        var manifest = ExtentReportAssets.sparkAssetManifest();

        assertEquals("spark-style.css", manifest.get(0).fileName());
        assertEquals(AtfReportAsset.Kind.TEXT, manifest.get(0).kind());
        assertEquals("logo.png", manifest.get(manifest.size() - 1).fileName());
        assertTrue(AtfReportAsset.fontAssets().stream()
                .allMatch(asset -> asset.mimeType().startsWith("font/")
                        || asset.mimeType().contains("ms-fontobject")
                        || asset.mimeType().equals("image/svg+xml")));
    }
}
