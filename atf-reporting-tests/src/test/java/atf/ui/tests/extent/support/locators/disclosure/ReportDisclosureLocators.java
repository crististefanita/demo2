package atf.ui.tests.extent.support.locators.disclosure;

import atf.api.reporting.markup.vocabulary.AtfReportHtmlVocabulary.CssClass;
import atf.api.reporting.ui.contract.ReportUiContract;
import atf.ui.tests.extent.support.locators.ReportUiLocators;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

/** Selector boundary for disclosure probes; reuses the shared report locator facade. */
public final class ReportDisclosureLocators {
    private static final String CHILDREN = ":scope > [class~='accordion'] > ";
    private static final String OWN_EVIDENCE = ":scope > [class~='collapse'] ";
    private final ReportUiLocators common = new ReportUiLocators();

    public Locator firstOwner(Page page) { return common.testOwners(page).first(); }
    public Locator scenarios(Page page) { return common.scenarios(common.visibleTestDetail(page)); }
    public Locator firstPhase(Locator scenario) { return common.phases(scenario).first(); }
    public Locator header(Locator card) { return common.requestNodeHeader(card); }
    public Locator requests(Locator parent) {
        return parent.locator(CHILDREN + ReportUiContract.nodeRoleSelector(ReportUiContract.NodeRole.REQUEST));
    }
    public Locator validations(Locator parent) { return common.validationCards(parent); }
    public Locator directLogTables(Locator card) {
        return card.locator(OWN_EVIDENCE + "table[class~='table']");
    }
    public Locator directRequestEvidence(Locator card) {
        return card.locator(OWN_EVIDENCE + evidenceSelector(CssClass.REQUEST_EVIDENCE));
    }
    public Locator evidence(Locator card, CssClass kind) { return card.locator(evidenceSelector(kind)); }
    public Locator summary(Locator disclosure) { return common.disclosureSummary(disclosure); }
    public Locator assertionSummary(Locator validation) {
        return validation.locator("details[class~='assertion-evidence'] > summary");
    }
    public Locator assertionTables(Locator validation) { return common.validationTables(validation); }
    public Locator screenshots(Locator validation) { return common.screenValidationScreenshotLinks(validation); }
    public Locator viewport(Locator disclosure) {
        return disclosure.locator("[class~='" + CssClass.ATF_API_TABLE_WRAP.value() + "']");
    }
    public Locator finalAttempt(Locator viewport) {
        return viewport.locator("tbody tr").last().locator("td").first();
    }
    public Locator separators(Page page) { return page.locator("hr"); }

    private static String evidenceSelector(CssClass kind) {
        return "details[class~='" + kind.value() + "']";
    }
}
