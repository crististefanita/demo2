package atf.api.ui.playwright;

import atf.api.ui.config.UiBrowserConfiguration;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.ViewportSize;

import java.net.URI;
import java.util.Objects;

/** Scoped Playwright adapter. One instance owns exactly one browser context. */
public final class PlaywrightUiSession implements AutoCloseable {
    private final Playwright playwright;
    private final Browser browser;
    private final BrowserContext context;
    private final Page page;
    private boolean closed;

    private PlaywrightUiSession(Playwright playwright, Browser browser,
                                BrowserContext context, Page page) {
        this.playwright = playwright;
        this.browser = browser;
        this.context = context;
        this.page = page;
    }

    public static PlaywrightUiSession open(UiBrowserConfiguration configuration) {
        Objects.requireNonNull(configuration, "configuration");
        Playwright playwright = Playwright.create();
        try {
            BrowserType type = switch (configuration.browser()) {
                case "chromium" -> playwright.chromium();
                case "firefox" -> playwright.firefox();
                case "webkit" -> playwright.webkit();
                default -> throw new IllegalArgumentException("Unsupported browser: " + configuration.browser());
            };
            Browser browser = type.launch(new BrowserType.LaunchOptions().setHeadless(configuration.headless()));
            BrowserContext context = browser.newContext(new Browser.NewContextOptions()
                    .setViewportSize(new ViewportSize(configuration.viewportWidth(), configuration.viewportHeight())));
            context.setDefaultTimeout(configuration.timeout().toMillis());
            context.setDefaultNavigationTimeout(configuration.navigationTimeout().toMillis());
            return new PlaywrightUiSession(playwright, browser, context, context.newPage());
        } catch (RuntimeException error) {
            playwright.close();
            throw error;
        }
    }

    public Page page() { return page; }

    /** Starts an isolated consecutive action log for one visual validation state. */
    public PlaywrightActionSequence newActionSequence() {
        if (closed) throw new IllegalStateException("Playwright UI session is closed");
        return new PlaywrightActionSequence();
    }

    public void navigate(URI uri) {
        page.navigate(Objects.requireNonNull(uri, "uri").toString());
    }

    @Override
    public void close() {
        if (closed) return;
        closed = true;
        try { context.close(); }
        finally {
            try { browser.close(); }
            finally { playwright.close(); }
        }
    }
}
