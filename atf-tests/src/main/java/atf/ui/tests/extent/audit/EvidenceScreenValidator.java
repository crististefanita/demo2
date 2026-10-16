package atf.ui.tests.extent.audit;

import atf.api.ui.playwright.AtfLocator;
import atf.ui.tests.extent.locators.ReportUiLocators;
import atf.ui.tests.extent.support.ReportUiContract;
import atf.ui.tests.extent.support.UiAuditFlow;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

import java.util.Base64;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/** Reusable validator for action tables, screenshot links and visual evidence. */
public final class EvidenceScreenValidator {
    private final UiAuditFlow auditFlow;
    private final ExistingReportStructureAuditor structureAuditor;
    private final ReportUiLocators locators;

    public EvidenceScreenValidator(UiAuditFlow auditFlow,
                                   ExistingReportStructureAuditor structureAuditor,
                                   ReportUiLocators locators) {
        this.auditFlow = Objects.requireNonNull(auditFlow, "auditFlow");
        this.structureAuditor = Objects.requireNonNull(structureAuditor, "structureAuditor");
        this.locators = Objects.requireNonNull(locators, "locators");
    }

    public VisualObservation inspectVisual(Page page) {
        auditFlow.actions().click(locators.firstTestOwnerAction(page), "class detail opens");
        Locator detail = locators.visibleTestDetail(page);
        Locator validations = locators.validationTables(detail);
        Locator disclosures = locators.evidenceDisclosures(detail);
        Locator actionSequences = locators.actionSequences(detail);
        int screenshotLinks = locators.base64ScreenshotLinks(actionSequences).count();
        int duplicateScreenshotMarkers = locators.duplicateScreenshotMarkers(actionSequences).count();
        int nestedScreenshotImages = locators.nestedScreenshotImages(actionSequences).count();
        int directScreenshotLinks = locators.dataUriScreenshotLinks(actionSequences).count();
        int featherlightScreenshotLinks = locators.featherlightScreenshotLinks(actionSequences).count();
        Locator screenshotAnchors = locators.base64ScreenshotLinks(actionSequences);
        Locator actionRows = locators.actionRows(actionSequences);
        String observedActionText = actionRows.count() > 0
                ? actionRows.first().innerText().replace("\n", " | ") : "no action rows";
        boolean observedTextIsStructured = actionRows.count() > 0
                && locators.actionRowCells(actionRows.first()).count() == 7
                && !observedActionText.contains("ASSERTIONEXPECTEDACTUALRESULT");
        int canonicalScreenshotLinks = 0;
        for (int index = 0; index < screenshotAnchors.count(); index++) {
            if (screenshotAnchors.nth(index).innerText().trim().contains("📷 base64 img")) {
                canonicalScreenshotLinks++;
            }
        }
        AtfLocator validationTable = new AtfLocator(validations.first(),
                ReportUiContract.VALIDATION_TABLE_SELECTOR);
        auditFlow.actions().findElement(validationTable);
        auditFlow.actions().expectText(validationTable, "ASSERTION");
        Locator visibleDisclosure = locators.visibleDisclosure(detail);
        if (visibleDisclosure.count() > 0 && !"true".equals(visibleDisclosure.getAttribute("open"))) {
            auditFlow.actions().click(new AtfLocator(locators.disclosureSummary(visibleDisclosure),
                    "details:visible > summary"), "screen evidence is expanded");
        }
        return new VisualObservation(validations.count(), disclosures.count(), actionSequences.count(),
                screenshotLinks, duplicateScreenshotMarkers, nestedScreenshotImages,
                directScreenshotLinks, featherlightScreenshotLinks, screenshotAnchors.count(),
                canonicalScreenshotLinks, observedActionText, observedTextIsStructured,
                structureAuditor.hasFixedActionTableCss());
    }

    public ScreenshotObservation inspectActionableScreenshot(Page page) {
        auditFlow.actions().click(locators.firstTestOwnerAction(page), "class detail opens");
        Locator detail = locators.visibleTestDetail(page);
        Locator sequence = null;
        Locator scenarios = locators.scenarios(detail);
        // The first scenario is often SETUP and intentionally has no screenshot.
        // Replay each scenario boundary until the action sequence that owns the
        // canonical visual evidence is found; otherwise a static audit can bind
        // to an unrelated media-less table and report a false missing screenshot.
        for (int scenarioIndex = 0; scenarioIndex < scenarios.count() && sequence == null;
             scenarioIndex++) {
            Locator scenario = scenarios.nth(scenarioIndex);
            if (!"true".equals(scenario.getAttribute("data-atf-scenario-open"))) {
                auditFlow.actions().click(locators.scenarioNodeAction(scenario),
                        "scenario evidence expands");
            }
            Locator actionSequences = locators.actionSequences(scenario);
            for (Locator candidate : actionSequences.all()) {
                for (Locator ancestor : locators.nodeAncestors(candidate).all()) {
                    Locator nodeHeader = locators.nodeHeader(ancestor);
                    Locator node = locators.nodeControl(nodeHeader);
                    if (node.count() == 0 || !node.isVisible()) continue;
                    String role = ancestor.getAttribute("data-atf-node-role");
                    String openAttribute = "scenario".equals(role)
                            ? "data-atf-scenario-open"
                            : "phase".equals(role) ? "data-atf-phase-open" : "aria-expanded";
                    if (!"true".equals(ancestor.getAttribute(openAttribute))) {
                        auditFlow.actions().click(locators.nodeHeaderAction(nodeHeader),
                                role + " evidence expands");
                    }
                }
                // Audit-only action tables may intentionally omit media. The
                // Screen validation contract must bind to the sequence that
                // actually owns the canonical inline Base64 link.
                if (candidate.isVisible() && locators.base64ScreenshotLinks(candidate).count() > 0) {
                    sequence = candidate;
                    break;
                }
            }
        }
        if (sequence == null || !sequence.isVisible()) return ScreenshotObservation.notAvailable();
        if (!"true".equals(sequence.getAttribute("open"))) {
            auditFlow.actions().click(new AtfLocator(locators.sequenceSummary(sequence),
                    ReportUiContract.ACTION_SEQUENCE_SELECTOR + " > summary"),
                    "action sequence expands");
        }
        Locator screenshotLink = locators.base64ScreenshotLinks(sequence).first();
        String href = screenshotLink.getAttribute("href");
        String target = screenshotLink.getAttribute("target");
        String rel = screenshotLink.getAttribute("rel");
        String featherlight = screenshotLink.getAttribute("data-featherlight");
        boolean visible = screenshotLink.isVisible();
        String markerText = screenshotLink.innerText();
        int inlineImageCount = locators.inlineScreenshotImages(screenshotLink).count();
        Set<String> distinctPayloads = new HashSet<>();
        for (Locator candidate : locators.base64ScreenshotLinks(sequence).all()) {
            String candidateHref = candidate.getAttribute("href");
            if (candidateHref != null && !candidateHref.isBlank()) distinctPayloads.add(candidateHref);
        }
        String geometry = screenshotLink.evaluate("""
                element => {
                    const row = element.closest('.atf-ui-action-screenshot');
                    if (!row) return 'missing-row';
                    const linkRect = element.getBoundingClientRect();
                    const rowRect = row.getBoundingClientRect();
                    const linkStyle = getComputedStyle(element);
                    const rowStyle = getComputedStyle(row);
                    return [linkRect.left - rowRect.left,
                        (linkRect.top + linkRect.height / 2) - (rowRect.top + rowRect.height / 2),
                        linkStyle.alignSelf, linkStyle.verticalAlign, linkStyle.transform,
                        rowStyle.alignItems, rowStyle.verticalAlign, rowStyle.overflow,
                        rowStyle.overflowY].join('|');
                }
                """).toString();
        String[] geometryParts = geometry.split("\\|", -1);
        boolean centered = geometryParts.length == 9 && closeToZero(geometryParts[0], 1.5)
                && closeToZero(geometryParts[1], 1.5) && "center".equals(geometryParts[2])
                && "middle".equals(geometryParts[3]) && "none".equals(geometryParts[4])
                && "center".equals(geometryParts[5]) && "middle".equals(geometryParts[6])
                && "visible".equals(geometryParts[7]) && "visible".equals(geometryParts[8]);
        auditFlow.actions().click(new AtfLocator(screenshotLink,
                ReportUiContract.SCREENSHOT_LINK_SELECTOR), "Base64 image overlay opens");
        Locator lightbox = locators.lightbox(page);
        Locator lightboxImage = locators.lightboxImage(lightbox);
        String lightboxSource = lightboxImage.getAttribute("src");
        boolean opened = lightbox.count() == 1 && lightboxImage.count() == 1
                && isPngDataUri(lightboxSource);
        boolean actionable = visible && isPngDataUri(href) && "_blank".equals(target)
                && rel != null && rel.contains("noopener") && "image".equals(featherlight)
                && distinctPayloads.size() == 1 && markerText.trim().contains("📷 base64 img")
                && centered && opened;
        if (lightbox.count() == 1) {
            auditFlow.actions().click(new AtfLocator(locators.lightboxClose(lightbox),
                    ".featherlight-close-icon"), "Base64 image overlay closes");
        }
        return new ScreenshotObservation(actionable, isPngDataUri(href), target, rel, featherlight,
                markerText, geometry, centered, opened, distinctPayloads.size(), inlineImageCount);
    }

    private static boolean closeToZero(String value, double tolerance) {
        try { return Math.abs(Double.parseDouble(value)) <= tolerance; }
        catch (NumberFormatException ignored) { return false; }
    }

    private static boolean isPngDataUri(String href) {
        String prefix = "data:image/png;base64,";
        if (href == null || !href.startsWith(prefix) || href.length() == prefix.length()) return false;
        try {
            byte[] bytes = Base64.getDecoder().decode(href.substring(prefix.length()));
            return bytes.length >= 8 && (bytes[0] & 0xFF) == 0x89 && bytes[1] == 0x50
                    && bytes[2] == 0x4E && bytes[3] == 0x47 && bytes[4] == 0x0D
                    && bytes[5] == 0x0A && bytes[6] == 0x1A && bytes[7] == 0x0A;
        } catch (IllegalArgumentException ignored) { return false; }
    }

    public record VisualObservation(int validationCount, int disclosureCount,
                                    int actionSequenceCount, int screenshotLinkCount,
                                    int duplicateScreenshotMarkers, int nestedScreenshotImages,
                                    int directScreenshotLinks, int featherlightScreenshotLinks,
                                    int screenshotAnchorCount, int canonicalScreenshotLinks,
                                    String observedActionText, boolean observedTextIsStructured,
                                    boolean fixedActionTable) { }

    public record ScreenshotObservation(boolean actionable, boolean validPngPayload,
                                        String target, String rel, String featherlight,
                                        String markerText, String geometry,
                                        boolean centeredVertically, boolean openedBase64Image,
                                        int distinctPayloadCount, int inlineImageCount) {
        static ScreenshotObservation notAvailable() {
            return new ScreenshotObservation(false, false, "", "", "", "", "missing",
                    false, false, 0, 0);
        }
    }
}
