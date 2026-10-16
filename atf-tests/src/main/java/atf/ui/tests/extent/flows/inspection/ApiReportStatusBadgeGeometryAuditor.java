package atf.ui.tests.extent.flows.inspection;

import atf.ui.tests.extent.locators.ReportUiLocators;
import atf.ui.tests.extent.plan.ReportAuditSnapshot;
import atf.ui.tests.extent.support.ReportUiContract;
import atf.ui.tests.extent.support.UiAuditFlow;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

import java.util.Map;

/** Owns only viewport replay and status-badge geometry observation. */
public final class ApiReportStatusBadgeGeometryAuditor {
    private final UiAuditFlow auditFlow;
    private final ReportUiLocators locators;

    public ApiReportStatusBadgeGeometryAuditor(UiAuditFlow auditFlow, ReportUiLocators locators) {
        this.auditFlow = auditFlow;
        this.locators = locators;
    }

    public ReportAuditSnapshot.BadgeGeometry inspect(Page page,
                                                       int normalWidth,
                                                       int normalHeight,
                                                       int narrowWidth,
                                                       int narrowHeight) {
        page.setViewportSize(normalWidth, normalHeight);
        page.reload();
        page.waitForLoadState();
        restoreTestsProjection(page, "normal viewport report state settles");
        ReportAuditSnapshot.Badge normal = statusBadgeGeometry(page);

        page.setViewportSize(narrowWidth, narrowHeight);
        page.reload();
        page.waitForLoadState();
        restoreTestsProjection(page, "narrow viewport report state settles");
        ReportAuditSnapshot.Badge narrow = statusBadgeGeometry(page);

        try {
            return new ReportAuditSnapshot.BadgeGeometry(normal, narrow);
        } finally {
            page.setViewportSize(normalWidth, normalHeight);
            page.reload();
            page.waitForLoadState();
            restoreTestsProjection(page, "configured viewport report state is restored");
        }
    }

    private void restoreTestsProjection(Page page, String description) {
        Locator tests = locators.testsView(page);
        if (!tests.isVisible()) {
            auditFlow.actions().click(locators.navTabAction(page, ReportUiContract.Tab.TESTS),
                    "restore Tests projection after viewport reload");
        }
        auditFlow.actions().waitForVisible(tests, ReportUiContract.Tab.TESTS.hash());
        auditFlow.actions().waitForAttribute(page,
                new atf.api.ui.playwright.AtfLocator(locators.runtimeRoot(page), "html"),
                "data-atf-runtime-ready", "true", description);
    }

    @SuppressWarnings("unchecked")
    private static ReportAuditSnapshot.Badge statusBadgeGeometry(Page page) {
        Map<String, Object> result = (Map<String, Object>) page.evaluate("""
                () => {
                  const labels = ['Pass', 'Fail', 'Skip', 'Warning'];
                  const visible = element => {
                    let current = element;
                    while (current) {
                      const style = getComputedStyle(current);
                      if (style.display === 'none' || style.visibility === 'hidden') return false;
                      current = current.parentElement;
                    }
                    return true;
                  };
                  const badges = [...document.querySelectorAll('.badge')]
                    .filter(element => labels.includes((element.textContent || '').trim()) && visible(element));
                  const styles = badges.map(element => {
                    const style = getComputedStyle(element);
                    return [style.display, style.boxSizing, style.height, style.minHeight,
                      style.paddingTop, style.paddingRight, style.paddingBottom, style.paddingLeft,
                      style.lineHeight, style.borderRadius].join('|');
                  });
                  return {
                    count: badges.length,
                    labels: badges.map(element => (element.textContent || '').trim()).sort().join('|'),
                    signature: [...new Set(styles)].sort().join('||')
                  };
                }
                """);
        return new ReportAuditSnapshot.Badge(
                atf.ui.tests.extent.support.ReportAuditChecks.count(result.get("count")),
                String.valueOf(result.getOrDefault("labels", "")),
                String.valueOf(result.getOrDefault("signature", "")));
    }
}
