package atf.api.reporting.asset;

import atf.api.testkit.AtfUnitTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@AtfUnitTest
class ReportAssetSizeInventoryTest {
    private static final ReportAssetSizeBudget CONSUMER_BASELINE = new ReportAssetSizeBudget(
            235_000L, 195_000L,
            java.util.Map.of(
                    ReportAsset.Format.CSS, 123_000L,
                    ReportAsset.Format.JAVASCRIPT, 112_000L),
            java.util.Map.of(
                    ReportAsset.Format.CSS, 104_000L,
                    ReportAsset.Format.JAVASCRIPT, 89_000L),
            java.util.Map.of(), java.util.Map.of());

    @Test
    void measuresNativeAndPublicationCopiesWithoutLosingTheFormatBoundary() {
        ReportAssetSizeInventory inventory = ReportAssetSizeInventory.capture(
                new ReportAssetLoader(), ReportAssetManifest.production());

        assertAll(
                () -> assertEquals(ReportAsset.values().length, inventory.entries().size()),
                () -> assertTrue(inventory.nativeBytes() > 0),
                () -> assertTrue(inventory.publicationBytes() > 0),
                () -> assertTrue(inventory.nativeBytes() >= inventory.publicationBytes()),
                () -> assertEquals(2, inventory.bytesByFormat(false).size()),
                () -> assertEquals(ReportAsset.values().length, inventory.bytesByOwner(false).size()),
                () -> assertTrue(inventory.entries().stream()
                        .allMatch(ReportAssetSizeInventory.Entry::nativeSourceFormatted)),
                () -> assertTrue(inventory.entries().stream()
                        .allMatch(ReportAssetSizeInventory.Entry::publicationCompacted)),
                () -> assertFalse(inventory.entries().stream()
                        .anyMatch(entry -> entry.publicationBytes() > entry.nativeBytes())));
    }

    @Test
    void keepsTheConsumerAssetBundleInsideItsExplicitPublicationBaseline() {
        ReportAssetSizeInventory inventory = ReportAssetSizeInventory.capture(
                new ReportAssetLoader(), ReportAssetManifest.production());

        assertTrue(CONSUMER_BASELINE.accepts(inventory),
                () -> "Reporting asset baseline violations: "
                        + CONSUMER_BASELINE.violations(inventory));
    }
}
