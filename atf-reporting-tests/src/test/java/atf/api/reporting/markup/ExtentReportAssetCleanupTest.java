package atf.api.reporting.markup;

import atf.api.testkit.AtfUnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@AtfUnitTest
class ExtentReportAssetCleanupTest {
    @ParameterizedTest
    @EnumSource(LockKind.class)
    void retriesTransientWindowsStyleLockAndRemovesOnlyGeneratedSparkAssets(LockKind lock, @TempDir Path root)
            throws Exception {
        Path outside = Files.writeString(root.resolve("keep.txt"), "outside generated assets");
        Path spark = Files.createDirectory(root.resolve(ExtentReportAssets.SPARK_DIRECTORY));
        Path asset = Files.writeString(spark.resolve("spark.js"), "generated");
        AtomicInteger attempts = new AtomicInteger();

        ExtentReportAssets.removeGeneratedAssets(spark, path -> {
            if (path.equals(asset) && attempts.incrementAndGet() <= 2) {
                throw lock.failure(path);
            }
            return Files.deleteIfExists(path);
        }, millis -> { });

        assertEquals(3, attempts.get(), "temporary access denial should retry with a bounded count");
        assertFalse(Files.exists(spark), "generated Spark assets should be removed after unlock");
        assertTrue(Files.exists(outside), "cleanup must not escape its generated Spark directory");
    }

    @ParameterizedTest
    @EnumSource(LockKind.class)
    void reportsPersistentLockAfterBoundedRetriesAndKeepsPublishedOutputOutsideTheTarget(LockKind lock, @TempDir Path root)
            throws Exception {
        Path outside = Files.writeString(root.resolve("published-report.html"), "complete report");
        Path spark = Files.createDirectory(root.resolve(ExtentReportAssets.SPARK_DIRECTORY));
        Path asset = Files.writeString(spark.resolve("locked.js"), "generated");
        AtomicInteger attempts = new AtomicInteger();
        AtomicInteger pauses = new AtomicInteger();

        IOException failure = assertThrows(IOException.class, () ->
                ExtentReportAssets.removeGeneratedAssets(spark, path -> {
                    attempts.incrementAndGet();
                    throw lock.failure(path);
                }, millis -> pauses.incrementAndGet()));

        assertTrue(failure.getMessage().contains("after 4 attempts"));
        assertTrue(failure.getMessage().contains(asset.toString()));
        assertEquals(4, attempts.get(), "cleanup must stop after its configured attempt count");
        assertEquals(3, pauses.get(), "there is no delay after the last failed attempt");
        assertTrue(Files.exists(asset), "a locked generated file must not be claimed as deleted");
        assertTrue(Files.exists(outside), "cleanup failure must not alter the published report");
    }

    @Test
    void unrelatedIoFailureIsNotRetriedOrReplaced(@TempDir Path root) throws Exception {
        Path spark = Files.createDirectory(root.resolve(ExtentReportAssets.SPARK_DIRECTORY));
        AtomicInteger attempts = new AtomicInteger();
        var original = new IOException("unrelated I/O failure");

        IOException actual = assertThrows(IOException.class, () ->
                ExtentReportAssets.removeGeneratedAssets(spark, path -> {
                    attempts.incrementAndGet();
                    throw original;
                }, millis -> { throw new AssertionError("ordinary I/O must not be retried"); }));

        assertSame(original, actual);
        assertEquals(1, attempts.get());
    }

    @Test
    void interruptedRetryPreservesTheInterruptAndStopsCleanup(@TempDir Path root) throws Exception {
        Path spark = Files.createDirectory(root.resolve(ExtentReportAssets.SPARK_DIRECTORY));
        AtomicInteger attempts = new AtomicInteger();
        try {
            IOException actual = assertThrows(IOException.class, () ->
                    ExtentReportAssets.removeGeneratedAssets(spark, path -> {
                        attempts.incrementAndGet();
                        throw LockKind.SHARING_VIOLATION.failure(path);
                    }, millis -> { throw new InterruptedException("cleanup interrupted"); }));
            assertTrue(Thread.currentThread().isInterrupted());
            assertTrue(actual.getCause() instanceof InterruptedException);
            assertEquals(1, attempts.get());
        } finally {
            Thread.interrupted();
        }
    }

    private enum LockKind {
        ACCESS_DENIED, SHARING_VIOLATION;

        FileSystemException failure(Path path) {
            return this == ACCESS_DENIED
                    ? new AccessDeniedException(path.toString(), null, "simulated Windows file lock")
                    : new FileSystemException(path.toString(), null, "file is being used by another process");
        }
    }
}
