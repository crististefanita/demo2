package atf.ui.tests.autoex.component.browser;

import atf.api.testkit.AtfComponentTest;
import atf.api.ui.config.UiBrowserConfiguration;
import atf.api.ui.playwright.PlaywrightUiSession;
import atf.ui.autoex.support.AutoExVignetteHandler;
import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@AtfComponentTest
class AutoExVignetteHandlerTest {
    @Test
    void closesOnlyTheVisibleExactControlInAnAdFrame() {
        try (PlaywrightUiSession browser = PlaywrightUiSession.open(UiBrowserConfiguration.defaults())) {
            browser.page().navigate(URI.create("about:blank#google_vignette").toString());
            browser.page().setContent("""
                    <iframe title="advertisement" srcdoc="<button id='close' onclick='this.remove()'>Close</button>"></iframe>
                    """);

            boolean dismissed = new AutoExVignetteHandler()
                    .dismissIfPresent(browser.page(), browser.newActionSequence());

            assertTrue(dismissed);
            assertFalse(browser.page().frameLocator("iframe[title='advertisement']")
                    .locator("#close").isVisible());
        }
    }

    @Test
    void refusesToProceedWhenTheVignetteHasNoVisibleCloseControl() {
        try (PlaywrightUiSession browser = PlaywrightUiSession.open(UiBrowserConfiguration.defaults())) {
            browser.page().navigate(URI.create("about:blank#google_vignette").toString());
            browser.page().setContent("<main>checkout</main>");

            assertThrows(IllegalStateException.class, () -> new AutoExVignetteHandler()
                    .dismissIfPresent(browser.page(), browser.newActionSequence()));
        }
    }

    @Test
    void leavesPagesWithoutTheVignetteUntouched() {
        try (PlaywrightUiSession browser = PlaywrightUiSession.open(UiBrowserConfiguration.defaults())) {
            browser.page().setContent("<main>catalog</main>");

            assertFalse(new AutoExVignetteHandler()
                    .dismissIfPresent(browser.page(), browser.newActionSequence()));
        }
    }
}
