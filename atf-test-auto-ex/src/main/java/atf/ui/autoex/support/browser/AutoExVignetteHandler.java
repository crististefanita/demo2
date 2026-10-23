package atf.ui.autoex.support.browser;

import atf.api.ui.playwright.AtfLocator;
import atf.api.ui.playwright.PlaywrightActionSequence;
import atf.ui.autoex.locator.AutoExRoutes;
import atf.ui.autoex.locator.AutoExSelectors;
import com.microsoft.playwright.Frame;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

import java.util.Objects;

/** Opts into DOM activation only for the public site's visible Google vignette Close control. */
public final class AutoExVignetteHandler {
    private static final String CLOSE_EXPECTATION =
            "visible Google vignette is closed with its Close control";

    public boolean dismissIfPresent(Page page, PlaywrightActionSequence actions) {
        Objects.requireNonNull(page, "page");
        Objects.requireNonNull(actions, "actions");
        String currentUrl = page.url();
        if (!currentUrl.contains(AutoExRoutes.GOOGLE_VIGNETTE_FRAGMENT)) {
            return false;
        }

        for (Frame frame : page.frames()) {
            Locator candidate = frame.locator(AutoExSelectors.VIGNETTE_CLOSE_CONTROL);
            if (candidate.count() == 1 && candidate.isVisible()) {
                String zoom = Objects.toString(page.evaluate(
                        "() => getComputedStyle(document.documentElement).zoom"));
                actions.observation("Observed Google vignette", currentUrl,
                        "visible Close control in the site's displayed advert",
                        "Close visible; document CSS zoom=" + zoom);
                var closeControl = new AtfLocator(candidate, AutoExSelectors.VIGNETTE_CLOSE_CONTROL);
                actions.clickWithJavaScript(closeControl, CLOSE_EXPECTATION,
                        () -> page.waitForURL(url -> !url.contains(AutoExRoutes.GOOGLE_VIGNETTE_FRAGMENT)));
                return true;
            }
        }
        throw new IllegalStateException("Google vignette is present, but its visible Close control is unavailable; "
                + "the test will not bypass the overlay");
    }
}
