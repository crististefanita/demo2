package atf.ui.autoex.support.journey;

import atf.ui.autoex.support.catalog.AutoExCatalogSnapshot;

import atf.api.reporting.contract.AtfAssertion;
import atf.ui.autoex.config.AutoExConfiguration;
import com.microsoft.playwright.Page;

import java.net.URI;
import java.util.List;

/** Application-facing browser/report contract; the JUnit lifecycle is supplied by the test harness. */
public interface AutoExPurchaseSession {
    AutoExConfiguration configuration();
    Page page();
    String currentUrl();
    void navigate(URI uri);
    void click(String selector, String expected);
    void clickFirst(String selector, String expected);
    void clickLast(String selector, String expected);
    void fill(String selector, String value, String expected);
    String text(String selector, String expected);
    String textFirst(String selector, String expected);
    void select(String selector, String label, String expected);
    boolean visible(String selector);
    void checkpoint(AutoExPurchaseTraceStep step, List<AtfAssertion> assertions);
    void finalScreenshot(AutoExPurchaseTraceStep step, List<AtfAssertion> assertions);
    AutoExCatalogSnapshot inspectCatalogApi(String productName);
    boolean observesApiTraffic();
    int apiExchangeCount();
    void markCausalApiTrafficBaseline();
    void onFailureCleanup(AutoExAccountCleanup cleanup);
    void clearFailureCleanup();
    boolean dismissGoogleVignetteIfPresent();
    boolean dismissPrivacyPromptIfPresent();

    @FunctionalInterface
    interface AutoExAccountCleanup { void run() throws Exception; }
}
