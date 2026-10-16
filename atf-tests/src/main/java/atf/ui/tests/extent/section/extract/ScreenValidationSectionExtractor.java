package atf.ui.tests.extent.section.extract;

import atf.ui.tests.extent.locators.ReportUiLocators;
import atf.ui.tests.extent.plan.ReportAuditSnapshot;
import atf.ui.tests.extent.support.UiAuditFlow;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Browser-only adapter for the Screen validation section. It observes the
 * rendered report and returns a renderer-neutral snapshot; it never asserts.
 */
public final class ScreenValidationSectionExtractor {
    private final UiAuditFlow audit;
    private final ReportUiLocators locators;

    public ScreenValidationSectionExtractor(UiAuditFlow audit, ReportUiLocators locators) {
        this.audit = Objects.requireNonNull(audit, "audit");
        this.locators = Objects.requireNonNull(locators, "locators");
    }

    public ReportAuditSnapshot.ScreenValidation inspect(Page page) {
        Objects.requireNonNull(page, "page");
        Locator allNodes = locators.screenValidationNodes(page);
        Locator node = locators.visibleScreenValidationNodeWithScreenshot(page);
        if (allNodes.count() == 0 || node.count() == 0 || !node.isVisible()) {
            return new ReportAuditSnapshot.ScreenValidation(0, 0, 0, 0, 0,
                    0, 0, false, false, false, false, false,
                    List.of("visible Screen validation node is missing"));
        }

        Locator card = locators.screenValidationCard(node);
        Locator body = card.locator(":scope > .collapse");
        if (!body.isVisible()) {
            audit.actions().click(locators.screenValidationNodeAction(page),
                    "Screen validation evidence zones expand");
        }
        Locator rows = locators.screenValidationZoneRows(card);
        int actionSequences = locators.screenValidationActionSequences(card).count();
        int observations = locators.screenValidationObservations(card).count();
        int assertions = locators.screenValidationAssertionTables(card).count();
        Locator screenshots = locators.screenValidationScreenshotLinks(card);
        // The data-atf-screenshot contract is the canonical machine-readable
        // marker; the label check below protects the user-facing emoji text.
        int canonicalScreenshots = screenshots.count();
        int labeledScreenshots = 0;
        for (Locator label : locators.screenValidationScreenshotLabels(card).all()) {
            String text = label.textContent();
            if (text != null && text.contains("base64 img")) labeledScreenshots++;
        }

        boolean directSiblings = rows.count() == 3
                && rows.nth(0).locator("details.atf-ui-action-sequence").count() == 1
                && rows.nth(1).locator("details.atf-ui-observations").count() == 1
                && rows.nth(2).locator("table.helm-validation-table").count() == 1;
        boolean noEmptyRows = rows.count() == 3;
        boolean stacked = true;
        boolean noSeparators = true;
        List<Double> tops = new ArrayList<>();
        for (int index = 0; index < rows.count(); index++) {
            Locator row = rows.nth(index);
            String text = row.innerText();
            noEmptyRows &= text != null && !text.isBlank();
            noSeparators &= row.locator(".atf-ui-section-separator, hr").count() == 0;
            var box = row.boundingBox();
            if (box == null) {
                stacked = false;
            } else {
                tops.add(box.y);
            }
        }
        for (int index = 1; index < tops.size(); index++) {
            stacked &= tops.get(index) >= tops.get(index - 1);
        }

        boolean inlineScreenshot = screenshots.count() > 0
                && canonicalScreenshots == screenshots.count()
                && labeledScreenshots == screenshots.count();
        List<String> violations = new ArrayList<>();
        if (allNodes.count() < 1) violations.add("Screen validation node is missing");
        if (!directSiblings) violations.add("action sequence, observations and assertions are not direct sibling zones");
        if (!stacked) violations.add("Screen validation zones are not vertically ordered");
        if (!noEmptyRows) violations.add("Screen validation contains an empty evidence row");
        if (!noSeparators) violations.add("Screen validation contains a decorative separator");
        if (!inlineScreenshot) violations.add("Screen validation does not expose the canonical inline screenshot link");
        return new ReportAuditSnapshot.ScreenValidation(1, actionSequences, observations,
                assertions, rows.count(), screenshots.count(), canonicalScreenshots,
                directSiblings, stacked, noEmptyRows, noSeparators, inlineScreenshot, violations);
    }
}
