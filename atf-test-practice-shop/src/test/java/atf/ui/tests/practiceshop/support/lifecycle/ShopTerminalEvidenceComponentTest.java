package atf.ui.tests.practiceshop.support.lifecycle;

import atf.api.reporting.contract.AtfAssertion;
import atf.api.testkit.AtfComponentTest;
import atf.api.ui.playwright.PlaywrightActionSequence;
import atf.api.ui.playwright.UiScreenValidation;
import com.microsoft.playwright.Page;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@AtfComponentTest
class ShopTerminalEvidenceComponentTest {
    private static final String CONFIRMATION = "order confirmation";
    private static final String CONFIRMATION_URL = "/confirmation";
    private static final List<AtfAssertion> ASSERTIONS = List.of(
            new AtfAssertion("Confirmation", "visible", "visible", true));

    @Test
    void captureDisabledStillPublishesOneExplicitAuditAfterEarlierEvidence() {
        var actions = new PlaywrightActionSequence();
        actions.observation("Prior evidence", "checkout", "ready", "ready");

        var evidence = ShopE2eContext.terminalContractEvidence(actions, page(), CONFIRMATION,
                ASSERTIONS, new UiScreenValidation.Evidence(CONFIRMATION, "", false));

        assertAll(
                () -> assertEquals(2, evidence.size()),
                () -> assertEquals("Prior evidence", evidence.getFirst().operation()),
                () -> assertEquals("Audit screen contract", evidence.getLast().operation()),
                () -> assertEquals("OBSERVE", evidence.getLast().source().name()),
                () -> assertTrue(evidence.getLast().observed().contains("screenshot=not captured")),
                () -> assertTrue(evidence.getLast().observed().contains("assertions=1")),
                () -> assertTrue(evidence.getLast().observed().contains("url=" + CONFIRMATION_URL)),
                () -> assertTrue(actions.drain().isEmpty()));
    }

    @Test
    void malformedCaptureIsReportedAsMissingRatherThanVisualEvidence() {
        var actions = new PlaywrightActionSequence();
        actions.observation("Prior evidence", "checkout", "ready", "ready");

        var evidence = ShopE2eContext.terminalContractEvidence(
                actions, page(), CONFIRMATION, ASSERTIONS,
                new UiScreenValidation.Evidence(CONFIRMATION, "not-a-png", true));
        assertEquals(2, evidence.size());
        assertEquals("Prior evidence", evidence.getFirst().operation());
        assertTrue(evidence.getLast().observed().contains("screenshot=not captured"));
        assertFalse(evidence.getLast().observed().contains("valid PNG"));
    }

    private static Page page() {
        return (Page) Proxy.newProxyInstance(Page.class.getClassLoader(), new Class<?>[]{Page.class},
                (proxy, method, arguments) -> {
                    if (method.getName().equals("url")) return CONFIRMATION_URL;
                    throw new UnsupportedOperationException(method.getName());
                });
    }
}
