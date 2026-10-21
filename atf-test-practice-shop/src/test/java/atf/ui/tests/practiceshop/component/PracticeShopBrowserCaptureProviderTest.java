package atf.ui.tests.practiceshop.component;

import atf.api.reporting.evidence.profile.ReportEvidenceCaptureSource;
import atf.api.testkit.AtfComponentTest;
import atf.api.ui.config.UiScreenValidationConfiguration;
import atf.ui.practiceshop.support.evidence.PracticeShopBrowserCaptureProvider;
import com.microsoft.playwright.Page;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Proves the Practice Shop browser adapter returns renderer-neutral PNG evidence. */
@AtfComponentTest
class PracticeShopBrowserCaptureProviderTest {
    private static final byte[] ONE_PIXEL_PNG = java.util.Base64.getDecoder().decode(
            "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNk+A8AAQUBAScY42YAAAAASUVORK5CYII=");

    @Test
    void capturesThroughTheTypedBrowserProviderWithDeclaredProvenance() {
        Page page = (Page) Proxy.newProxyInstance(Page.class.getClassLoader(),
                new Class<?>[]{Page.class}, (proxy, method, args) ->
                        "screenshot".equals(method.getName()) ? ONE_PIXEL_PNG : null);
        var provider = new PracticeShopBrowserCaptureProvider(page,
                UiScreenValidationConfiguration.defaults());

        var screenshot = provider.capture("final state", "https://example.test/checkout");

        assertEquals(ReportEvidenceCaptureSource.BROWSER, provider.source());
        assertEquals("final state", screenshot.title());
        assertTrue(atf.api.ui.playwright.UiScreenValidation.isValidPngPayload(
                screenshot.base64Png()));
    }
}
