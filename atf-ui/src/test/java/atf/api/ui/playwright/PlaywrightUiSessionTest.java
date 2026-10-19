package atf.api.ui.playwright;

import atf.api.testkit.AtfComponentTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@AtfComponentTest
class PlaywrightUiSessionTest {
    @Test
    void zoomInitScriptIsSafeBeforeDocumentElementExists() {
        String script = PlaywrightUiSession.zoomInitScript(70);

        assertTrue(script.contains("if (document.documentElement)"));
        assertTrue(script.contains("DOMContentLoaded"));
        assertTrue(script.contains("document.documentElement.style.zoom = '70%'") );
        assertFalse(script.startsWith("document.documentElement.style.zoom"));
    }

    @Test
    void executableOverrideIsOptInAndDoesNotBecomeBusinessConfiguration() {
        assertTrue(PlaywrightUiSession.configuredExecutablePath(" ").isEmpty());
        assertThrows(IllegalArgumentException.class,
                () -> PlaywrightUiSession.configuredExecutablePath("missing-playwright-browser.exe"));
    }
}
