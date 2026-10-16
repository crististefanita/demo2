package atf.ui.tests.extent.flows.inspection;

import atf.ui.tests.extent.flows.ApiReportUiFlow;
import atf.ui.tests.extent.locators.ReportUiLocators;
import atf.ui.tests.extent.support.UiAuditFlow;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Verifies the real failed-phase presentation in the Tests tab.
 *
 * <p>The inspector deliberately opens the rendered disclosures and closes
 * them again. It therefore proves both sides of the contract: the arrival
 * state is quiet, while the exact failure cause remains reachable and
 * readable on demand. It does not assert a renderer implementation detail
 * such as a particular HTML table nesting.</p>
 */
public final class FailureDetailsDisplayInspector {
    private static final String FAILURE_PHASE_ROLE = "[data-atf-node-role='phase']";
    private static final String OPEN = "true";

    private final UiAuditFlow auditFlow;
    private final ReportUiLocators locators;
    private final FailureTransportEvidenceInspector transportInspector;

    public FailureDetailsDisplayInspector(UiAuditFlow auditFlow, ReportUiLocators locators) {
        this.auditFlow = Objects.requireNonNull(auditFlow, "auditFlow");
        this.locators = Objects.requireNonNull(locators, "locators");
        this.transportInspector = new FailureTransportEvidenceInspector(auditFlow, locators);
    }

    public Observation inspect(Page page, String expectedOwner) {
        Objects.requireNonNull(page, "page");
        Objects.requireNonNull(expectedOwner, "expectedOwner");
        List<String> violations = new ArrayList<>();
        ApiReportUiFlow.Owner owner = new ApiReportUiFlow(auditFlow, locators)
                .openOwner(page, expectedOwner);
        Locator scenarios = locators.scenarios(owner.detail());
        int failedScenarios = 0;
        int failedPhases = 0;
        int failureDisclosures = 0;
        int readableCauses = 0;
        int semanticCauses = 0;
        int assertionEvidence = 0;
        int expectedActualPairs = 0;
        int visibleTraceTablesOnArrival = 0;
        int keyboardOpened = 0;
        int focusReturned = 0;
        int idempotentClosed = 0;
        FailureTransportEvidenceInspector.Observation transport =
                new FailureTransportEvidenceInspector.Observation(0, 0, 0, 0, 0, List.of());

        for (int scenarioIndex = 0; scenarioIndex < scenarios.count(); scenarioIndex++) {
            Locator scenario = scenarios.nth(scenarioIndex);
            if (!scenario.isVisible() || locators.failedScenarioBadge(scenario).count() == 0) {
                continue;
            }
            failedScenarios++;
            openScenario(page, scenario);
            Locator phases = locators.phases(scenario);
            for (int phaseIndex = 0; phaseIndex < phases.count(); phaseIndex++) {
                Locator phase = phases.nth(phaseIndex);
                if (!phase.isVisible() || locators.failedPhaseBadge(phase).count() == 0) {
                    continue;
                }
                failedPhases++;
                boolean phaseClosed = !OPEN.equals(
                        locators.boundPhaseHeader(phase).getAttribute("aria-expanded"));
                boolean phaseBodyHidden = !locators.phaseDirectBody(phase).isVisible();
                if (!phaseClosed) {
                    violations.add("failed phase arrives expanded: " + phaseTitle(phase));
                }
                if (!phaseBodyHidden) {
                    violations.add("failed phase leaks direct evidence while collapsed: "
                            + phaseTitle(phase));
                    visibleTraceTablesOnArrival += locators.phaseFailureTraceTables(phase).count();
                }

                openPhase(page, phase);
                Locator disclosures = locators.phaseFailureDisclosures(phase);
                failureDisclosures += disclosures.count();
                if (disclosures.count() != 1) {
                    violations.add(phaseTitle(phase) + " exposes " + disclosures.count()
                            + " failure disclosures; expected exactly one compact disclosure");
                }
                if (disclosures.count() == 1) {
                    Locator disclosure = disclosures.first();
                    String summary = locators.phaseFailureDisclosureSummary(disclosure)
                            .innerText().trim();
                    boolean closed = disclosure.getAttribute("open") == null;
                    boolean compact = !summary.contains("fields")
                            && !summary.contains("TECHNICAL TRACE")
                            && !summary.isBlank();
                    boolean traceHidden = !locators.phaseFailureTraceTables(phase).first().isVisible();
                    if (!closed) violations.add(phaseTitle(phase) + " failure details are open by default");
                    if (!compact) violations.add(phaseTitle(phase)
                            + " summary is noisy or not human-readable: " + summary);
                    if (!traceHidden) violations.add(phaseTitle(phase)
                            + " technical trace is visible before disclosure is opened");

                    var disclosureAction = locators.phaseFailureDisclosureAction(phase);
                    Locator summaryLocator = locators.phaseFailureDisclosureSummary(disclosure);
                    auditFlow.actions().focus(disclosureAction,
                            "failure disclosure summary receives focus");
                    auditFlow.actions().pressKey(page, "Enter", disclosureAction.selector(),
                            "Enter opens failure details for " + phaseTitle(phase));
                    auditFlow.actions().waitForDisclosureOpen(page,
                            disclosureAction,
                            "failure details are available for " + phaseTitle(phase));
                    keyboardOpened++;
                    if (isActiveElement(summaryLocator)) focusReturned++;
                    else violations.add(phaseTitle(phase)
                            + " loses focus on the disclosure summary after keyboard open");
                    Locator causeRows = locators.phaseFailureCauseValues(phase);
                    if (causeRows.count() > 0 && !causeRows.first().innerText().isBlank()) {
                        readableCauses++;
                        String cause = causeRows.first().innerText().trim();
                        boolean semantic = !cause.equalsIgnoreCase("Validation failed.")
                                && !cause.contains("FailureSignal");
                        if (semantic) {
                            semanticCauses++;
                        } else {
                            violations.add(phaseTitle(phase)
                                    + " exposes only a generic validation failure cause: " + cause);
                        }
                        if ("TEST BODY".equalsIgnoreCase(phaseTitle(phase))) {
                            boolean hasAssertion = locators.phaseFailureFieldValues(phase, "ASSERTION")
                                    .count() > 0;
                            boolean hasExpected = locators.phaseFailureFieldValues(phase, "EXPECTED")
                                    .count() > 0;
                            boolean hasActual = locators.phaseFailureFieldValues(phase, "ACTUAL")
                                    .count() > 0;
                            if (hasAssertion) assertionEvidence++;
                            if (hasExpected && hasActual) expectedActualPairs++;
                            if (!hasAssertion || !hasExpected || !hasActual) {
                                violations.add(phaseTitle(phase)
                                        + " does not expose ASSERTION / EXPECTED / ACTUAL evidence");
                            }
                        }
                    } else {
                        violations.add(phaseTitle(phase)
                                + " does not expose a readable FAILURE CAUSE when opened");
                    }
                    auditFlow.actions().pressKey(page, "Enter", disclosureAction.selector(),
                            "Enter closes failure details for " + phaseTitle(phase));
                    if (disclosure.getAttribute("open") == null) idempotentClosed++;
                    else violations.add(phaseTitle(phase)
                            + " keyboard close did not restore the collapsed disclosure state");
                    var transportObservation = transportInspector.inspect(page, phase);
                    transport = mergeTransport(transport, transportObservation);
                    violations.addAll(transportObservation.violations());
                }
                closePhase(page, phase);
            }
            closeScenario(page, scenario);
        }
        boolean finalStateCollapsed = locators.expandedScenarioHeaders(owner.detail()).count() == 0
                && locators.expandedPhaseHeaders(owner.detail()).count() == 0
                && locators.openEvidenceDisclosures(owner.detail()).count() == 0;
        if (!finalStateCollapsed) {
            violations.add("failed-phase audit leaves Tests details expanded");
        }
        boolean valid = failedScenarios > 0 && failedPhases > 0
                && failureDisclosures == failedPhases
                && readableCauses == failedPhases
                && semanticCauses == failedPhases
                && assertionEvidence > 0
                && expectedActualPairs > 0
                && visibleTraceTablesOnArrival == 0
                && keyboardOpened == failedPhases
                && focusReturned == failedPhases
                && idempotentClosed == failedPhases
                && transport.valid()
                && violations.isEmpty();
        return new Observation(failedScenarios, failedPhases, failureDisclosures,
                readableCauses, semanticCauses, assertionEvidence, expectedActualPairs,
                visibleTraceTablesOnArrival, finalStateCollapsed, keyboardOpened,
                focusReturned, idempotentClosed, transport.writeFailureCount(),
                transport.metadataCompleteCount(), transport.jsonCompleteCount(),
                transport.retryAuditedCount(), transport.retryValidCount(),
                valid, List.copyOf(violations));
    }

    private boolean isActiveElement(Locator element) {
        return Boolean.TRUE.equals(element.evaluate("node => node === document.activeElement"));
    }

    private void openScenario(Page page, Locator scenario) {
        if (OPEN.equals(scenario.getAttribute("data-atf-scenario-open"))) return;
        auditFlow.actions().click(locators.scenarioNodeAction(scenario),
                "open failed scenario " + phaseTitle(scenario));
        auditFlow.actions().waitForAttribute(page, locators.scenarioStateTarget(scenario),
                "data-atf-scenario-open", OPEN, "failed scenario is open");
    }

    private void closeScenario(Page page, Locator scenario) {
        if (!OPEN.equals(scenario.getAttribute("data-atf-scenario-open"))) return;
        auditFlow.actions().click(locators.scenarioNodeAction(scenario),
                "close failed scenario " + phaseTitle(scenario));
        auditFlow.actions().waitForAttribute(page, locators.scenarioStateTarget(scenario),
                "data-atf-scenario-open", "false", "failed scenario is collapsed");
    }

    private void openPhase(Page page, Locator phase) {
        if (OPEN.equals(phase.getAttribute("data-atf-phase-open"))) return;
        auditFlow.actions().click(locators.phaseNodeAction(phase),
                "open failed phase " + phaseTitle(phase));
        auditFlow.actions().waitForAttribute(page, locators.phaseStateTarget(phase),
                "data-atf-phase-open", OPEN, "failed phase is open");
    }

    private void closePhase(Page page, Locator phase) {
        if (!OPEN.equals(phase.getAttribute("data-atf-phase-open"))) return;
        auditFlow.actions().click(locators.phaseNodeAction(phase),
                "close failed phase " + phaseTitle(phase));
        auditFlow.actions().waitForAttribute(page, locators.phaseStateTarget(phase),
                "data-atf-phase-open", "false", "failed phase is collapsed");
    }

    private String phaseTitle(Locator card) {
        return locators.nodeTitle(card);
    }

    public record Observation(int failedScenarioCount, int failedPhaseCount,
                              int failureDisclosureCount, int readableCauseCount,
                              int semanticCauseCount, int assertionEvidenceCount,
                              int expectedActualPairCount,
                              int visibleTraceTablesOnArrival, boolean finalStateCollapsed,
                              int keyboardOpenedCount, int focusReturnedCount,
                              int idempotentClosedCount, int writeFailureCount,
                              int writeMetadataCompleteCount, int writeJsonCompleteCount,
                              int retryAuditedCount, int retryValidCount,
                              boolean valid, List<String> violations) {
        public Observation {
            violations = List.copyOf(Objects.requireNonNull(violations, "violations"));
        }

        public boolean transportValid() {
            return writeFailureCount > 0
                    && writeMetadataCompleteCount == writeFailureCount
                    && writeJsonCompleteCount == writeFailureCount
                    && retryAuditedCount == retryValidCount;
        }

    }

    private static FailureTransportEvidenceInspector.Observation mergeTransport(
            FailureTransportEvidenceInspector.Observation first,
            FailureTransportEvidenceInspector.Observation second) {
        List<String> combined = new ArrayList<>(first.violations());
        combined.addAll(second.violations());
        return new FailureTransportEvidenceInspector.Observation(
                first.writeFailureCount() + second.writeFailureCount(),
                first.metadataCompleteCount() + second.metadataCompleteCount(),
                first.jsonCompleteCount() + second.jsonCompleteCount(),
                first.retryAuditedCount() + second.retryAuditedCount(),
                first.retryValidCount() + second.retryValidCount(), combined);
    }
}
