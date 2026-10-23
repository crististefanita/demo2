package atf.api.reporting.markup;

import atf.api.testkit.AtfUnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@AtfUnitTest
class ExtentReportAssetCleanupTest {
    @Test
    void retriesTransientWindowsStyleLockAndRemovesOnlyGeneratedSparkAssets(@TempDir Path root)
            throws Exception {
        Path outside = Files.writeString(root.resolve("keep.txt"), "outside generated assets");
        Path spark = Files.createDirectory(root.resolve(ExtentReportAssets.SPARK_DIRECTORY));
        Path asset = Files.writeString(spark.resolve("spark.js"), "generated");
        AtomicInteger attempts = new AtomicInteger();

        ExtentReportAssets.removeGeneratedAssets(spark, path -> {
            if (path.equals(asset) && attempts.incrementAndGet() <= 2) {
                throw new AccessDeniedException(path.toString(), null, "simulated Windows file lock");
            }
            return Files.deleteIfExists(path);
        }, millis -> { });

        assertEquals(3, attempts.get(), "temporary access denial should retry with a bounded count");
        assertFalse(Files.exists(spark), "generated Spark assets should be removed after unlock");
        assertTrue(Files.exists(outside), "cleanup must not escape its generated Spark directory");
    }

    @Test
    void reportsPersistentLockAfterBoundedRetriesAndKeepsPublishedOutputOutsideTheTarget(@TempDir Path root)
            throws Exception {
        Path outside = Files.writeString(root.resolve("published-report.html"), "complete report");
        Path spark = Files.createDirectory(root.resolve(ExtentReportAssets.SPARK_DIRECTORY));
        Path asset = Files.writeString(spark.resolve("locked.js"), "generated");
        AtomicInteger attempts = new AtomicInteger();
        AtomicInteger pauses = new AtomicInteger();

        IOException failure = assertThrows(IOException.class, () ->
                ExtentReportAssets.removeGeneratedAssets(spark, path -> {
                    attempts.incrementAndGet();
                    throw new AccessDeniedException(path.toString(), null, "simulated persistent lock");
                }, millis -> pauses.incrementAndGet()));

        assertTrue(failure.getMessage().contains("after 4 attempts"));
        assertTrue(failure.getMessage().contains(asset.toString()));
        assertEquals(4, attempts.get(), "cleanup must stop after its configured attempt count");
        assertEquals(3, pauses.get(), "there is no delay after the last failed attempt");
        assertTrue(Files.exists(asset), "a locked generated file must not be claimed as deleted");
        assertTrue(Files.exists(outside), "cleanup failure must not alter the published report");
    }
}
