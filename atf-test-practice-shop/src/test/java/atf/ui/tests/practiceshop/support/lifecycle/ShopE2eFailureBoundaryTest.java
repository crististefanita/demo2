package atf.ui.tests.practiceshop.support.lifecycle;

import atf.api.testkit.AtfUnitTest;
import atf.api.testkit.lifecycle.FailureAwareCleanup;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

@AtfUnitTest
class ShopE2eFailureBoundaryTest {
    @Test
    void partialBrowserAcquisitionClosesInReverseAndKeepsAcquisitionFailurePrimary() {
        var acquisitionFailure = new IllegalStateException("network listener attach failed");
        var closeFailure = new IllegalArgumentException("browser close failed");
        var calls = new java.util.ArrayList<String>();

        Throwable result = FailureAwareCleanup.closeAfterFailure(acquisitionFailure,
                () -> calls.add("playwright"),
                () -> { calls.add("browser"); throw closeFailure; },
                () -> calls.add("context"));

        assertSame(acquisitionFailure, result);
        assertEquals(java.util.List.of("context", "browser", "playwright"), calls);
        assertEquals(1, result.getSuppressed().length);
        assertSame(closeFailure, result.getSuppressed()[0]);
    }

    @Test
    void publishesLinkageErrorsAndRethrowsTheOriginalFailure() {
        var failure = new NoClassDefFoundError("missing runtime class");
        var published = new AtomicReference<Throwable>();

        var thrown = assertThrows(NoClassDefFoundError.class,
                () -> ShopE2eContext.Scenario.executeWithFailureCapture(
                        () -> { throw failure; }, published::set));

        assertSame(failure, thrown);
        assertSame(failure, published.get());
    }
}
