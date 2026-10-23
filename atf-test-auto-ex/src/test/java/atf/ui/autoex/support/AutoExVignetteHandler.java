package atf.ui.autoex.support;

import atf.api.ui.playwright.AtfLocator;
import atf.api.ui.playwright.PlaywrightActionSequence;
import atf.ui.autoex.locator.AutoExRoutes;
import atf.ui.autoex.locator.AutoExSelectors;
import com.microsoft.playwright.Frame;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

import java.util.Objects;

/** Dismisses only the public site's visible Google vignette close control. */
public final class AutoExVignetteHandler {
    private static final String CLOSE_EXPECTATION =
            "visible Google vignette is closed with its Close control";

    private String dismissedUrl;

    public boolean dismissIfPresent(Page page, PlaywrightActionSequence actions) {
        Objects.requireNonNull(page, "page");
        Objects.requireNonNull(actions, "actions");
        String currentUrl = page.url();
        if (!currentUrl.contains(AutoExRoutes.GOOGLE_VIGNETTE_FRAGMENT)) {
            dismissedUrl = null;
            return false;
        }

        for (Frame frame : page.frames()) {
            Locator candidate = frame.locator(AutoExSelectors.VIGNETTE_CLOSE_CONTROL);
            if (candidate.count() == 1 && candidate.isVisible()) {
                actions.click(new AtfLocator(candidate, AutoExSelectors.VIGNETTE_CLOSE_CONTROL), CLOSE_EXPECTATION);
                dismissedUrl = currentUrl;
                return true;
            }
        }
        if (currentUrl.equals(dismissedUrl)) return false;
        throw new IllegalStateException("Google vignette is present, but its visible Close control is unavailable; "
                + "the test will not bypass the overlay");
    }
}
