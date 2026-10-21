package atf.api.reporting.asset;

import atf.api.testkit.AtfUnitTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@AtfUnitTest
class ReportAssetSizeInventoryTest {
    // The incremental table-layout cache/observer contract is part of the
    // published runtime; keep a small measured headroom without masking growth.
    private static final long NATIVE_JAVASCRIPT_BASELINE_BYTES = 123_000L;
    private static final long PUBLICATION_JAVASCRIPT_BASELINE_BYTES = 100_000L;

    private static final ReportAssetSizeBudget CONSUMER_BASELINE = new ReportAssetSizeBudget(
            246_000L, 205_000L,
            java.util.Map.of(
                    ReportAsset.Format.CSS, 123_000L,
                    ReportAsset.Format.JAVASCRIPT, NATIVE_JAVASCRIPT_BASELINE_BYTES),
            java.util.Map.of(
                    ReportAsset.Format.CSS, 105_000L,
                    ReportAsset.Format.JAVASCRIPT, PUBLICATION_JAVASCRIPT_BASELINE_BYTES),
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
