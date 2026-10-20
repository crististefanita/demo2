package atf.ui.tests.extent.support.audit.screen;
import atf.ui.tests.extent.support.lifecycle.UiAuditFlow;

import atf.ui.tests.extent.support.locators.ReportUiLocators;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

import java.util.Objects;

/** Reusable semantic validator for the optional Fails screen. */
public final class FailsScreenValidator {
    private final UiAuditFlow auditFlow;
    private final ReportUiLocators locators;

    public FailsScreenValidator(UiAuditFlow auditFlow) {
        this(auditFlow, new ReportUiLocators());
    }

    public FailsScreenValidator(UiAuditFlow auditFlow, ReportUiLocators locators) {
        this.auditFlow = Objects.requireNonNull(auditFlow, "auditFlow");
        this.locators = Objects.requireNonNull(locators, "locators");
    }

    /** Reads the Fails summary without assuming that the report contains failures. */
    public Observation inspect(Page page) {
        Locator fails = locators.failsView(page);
        int failureRows = locators.failureNames(fails).count();
        boolean hasFailures = failureRows > 0;
        boolean emptyState = locators.failureEmptyState(fails).count() == 1;
        int actionableRows = 0;
        if (hasFailures) {
            Locator items = locators.failureItems(fails);
            for (int index = 0; index < items.count(); index++) {
                auditFlow.actions().click(locators.failureItemAction(page, index),
                        "failure details opens");
                Locator links = locators.failureOpenLinks(fails);
                if (links.count() == 1 && links.isVisible()) actionableRows++;
            }
        }
        int openLinks = locators.failureOpenLinks(fails).count();
        int causes = locators.failureCauseValues(fails).count();
        boolean valid = hasFailures
                ? actionableRows == failureRows && openLinks == 1 && causes > 0
                : emptyState && openLinks == 0;
        return new Observation(failureRows, hasFailures, emptyState, actionableRows,
                openLinks, causes, valid, fails.count() > 0);
    }

    public record Observation(int failureRows, boolean hasFailures, boolean emptyState,
                              int actionableRows, int openLinkCount, int primaryCauseCount,
                              boolean valid, boolean viewPresent) { }
}
