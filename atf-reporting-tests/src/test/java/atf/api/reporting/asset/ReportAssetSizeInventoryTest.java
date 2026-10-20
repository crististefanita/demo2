package atf.api.reporting.asset;

import atf.api.testkit.AtfUnitTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@AtfUnitTest
class ReportAssetSizeInventoryTest {
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
}
