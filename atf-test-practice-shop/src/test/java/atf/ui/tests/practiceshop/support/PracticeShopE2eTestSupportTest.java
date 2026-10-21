package atf.ui.tests.practiceshop.support;

import atf.api.testkit.AtfUnitTest;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

@AtfUnitTest
class PracticeShopE2eTestSupportTest {
    @Test
    void closesThePerMethodResourceOnlyOnce() throws Exception {
        AtomicInteger closes = new AtomicInteger();
        var resource = new PracticeShopE2eTestSupport.E2eResource(closes::incrementAndGet);

        resource.close();
        resource.close();

        assertEquals(1, closes.get());
    }

    @Test
    void keepsTheBusinessFailurePrimaryWhenTeardownAlsoFails() {
        IOException teardown = new IOException("teardown failed");
        var resource = new PracticeShopE2eTestSupport.E2eResource(() -> { throw teardown; });
        AssertionError business = new AssertionError("observed business failure");

        assertDoesNotThrow(() -> PracticeShopE2eTestSupport.closeAfterTest(resource, business));

        assertEquals(1, business.getSuppressed().length);
        assertSame(teardown, business.getSuppressed()[0]);
    }

    @Test
    void reportsTeardownFailureWhenTheTestItselfPassed() {
        IOException teardown = new IOException("teardown failed");
        var resource = new PracticeShopE2eTestSupport.E2eResource(() -> { throw teardown; });

        assertSame(teardown, assertThrows(IOException.class,
                () -> PracticeShopE2eTestSupport.closeAfterTest(resource, null)));
    }
}
