package atf.ui.autoex.support;

import atf.api.console.junit.AtfConsoleTestContext;
import atf.api.reporting.api.ReportModelAdapters;
import atf.api.reporting.configuration.AtfReportConfigurationLoader;
import atf.api.reporting.contract.AtfAssertion;
import atf.api.reporting.contract.AtfGherkinKeyword;
import atf.api.reporting.contract.AtfReportStatus;
import atf.api.reporting.api.ReportRequest;
import atf.api.reporting.api.ReportResponse;
import atf.api.reporting.api.ReportTransportProvenance;
import atf.api.reporting.api.ReportTraceMetadata;
import atf.api.reporting.evidence.ReportScreenshotPolicy;
import atf.api.reporting.evidence.profile.ReportEvidenceProfile;
import atf.api.reporting.model.ReportExecutionEvidence;
import atf.api.reporting.ui.UiExtentReportSession;
import atf.api.reporting.ui.network.ReportNetworkInspector;
import atf.api.testkit.files.AtfTestResourceReader;
import atf.api.testkit.output.ReportOutputDirectory;
import atf.api.testkit.channel.AtfTestChannelResolver;
import atf.api.testkit.channel.TestChannel;
import atf.api.testkit.lifecycle.FailureAwareCleanup;
import atf.api.testkit.evidence.AtfUiEvidence;
import atf.api.testkit.evidence.AtfUiEvidenceFlow;
import atf.api.testkit.evidence.AtfUiScreenshotPolicy;
import atf.api.ui.config.UiScreenValidationConfiguration;
import atf.api.ui.config.UiBrowserConfiguration;
import atf.api.ui.config.UiNetworkInspectionConfiguration;
import atf.api.ui.playwright.AtfLocator;
import atf.api.ui.playwright.PlaywrightActionSequence;
import atf.api.ui.playwright.PlaywrightUiSession;
import atf.api.ui.playwright.UiScreenValidation;
import atf.ui.autoex.config.AutoExConfiguration;
import atf.ui.autoex.support.browser.AutoExPrivacyConsentHandler;
import atf.ui.autoex.support.browser.AutoExVignetteHandler;
import atf.ui.autoex.support.lifecycle.AutoExE2eInitialization;
import atf.ui.autoex.support.catalog.AutoExCatalogSnapshot;
import atf.ui.autoex.support.journey.AutoExPurchaseSession;
import atf.ui.autoex.support.journey.AutoExPurchaseTraceStep;
import atf.ui.autoex.config.AutoExConfigurationLoader;
import atf.ui.autoex.locator.AutoExRoutes;
import atf.ui.autoex.locator.AutoExSelectors;
import com.microsoft.playwright.Page;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/** Per-test composition root for one headless browser and one UI Extent artifact. */
public final class AutoExE2eRun implements AutoExPurchaseSession {
    private static final ExtensionContext.Namespace NAMESPACE =
            ExtensionContext.Namespace.create(AutoExE2eRun.class);
    private static final String SUITE_REPORT_KEY = "auto-ex-suite-report";
    private static final String NETWORK_SUMMARY_TITLE = "API network inspector — journey summary";
    private static final String REPORT_FILE_PREFIX = "ui-atf-auto-ex-purchase-suite_";
    private static final DateTimeFormatter REPORT_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss-SSS");

    private final AutoExConfiguration configuration;
    private final PlaywrightUiSession browser;
    private final UiExtentReportSession report;
    private final UiExtentReportSession.UiScenario scenario;
    private final UiExtentReportSession.UiPhase phase;
    private final ReportEvidenceProfile evidenceProfile;
    private final TestChannel channel;
    private final List<ReportNetworkInspector.Step> networkSteps = new ArrayList<>();
    private final PlaywrightActionSequence actions;
    private int networkCursor;
    private final AutoExApiTrafficWindow causalApiTraffic = new AutoExApiTrafficWindow();
    private final AutoExVignetteHandler vignetteHandler = new AutoExVignetteHandler();
    private final AutoExPrivacyConsentHandler privacyConsentHandler = new AutoExPrivacyConsentHandler();
    private AutoExAccountCleanup failureCleanup;
    private boolean finished;

    private AutoExE2eRun(AutoExConfiguration configuration, PlaywrightUiSession browser,
                         UiExtentReportSession report, UiExtentReportSession.UiScenario scenario,
                         UiExtentReportSession.UiPhase phase, ReportEvidenceProfile evidenceProfile,
                         TestChannel channel) {
        this.configuration = configuration;
        this.browser = browser;
        this.report = report;
        this.scenario = scenario;
        this.phase = phase;
        this.evidenceProfile = evidenceProfile;
        this.channel = channel;
        this.actions = browser.newActionSequence();
    }

    static AutoExE2eRun open(ExtensionContext context) {
        AutoExConfiguration configuration = AutoExConfigurationLoader.load();
        TestChannel channel = AtfTestChannelResolver.resolve(context.getRequiredTestClass())
                .orElseThrow(() -> new IllegalStateException("AutoEx E2E must declare @AtfTestChannel"))
                .channel();
        if (channel != TestChannel.UI && channel != TestChannel.UI_API) {
            throw new IllegalStateException("AutoEx purchase journey supports UI or UI_API channels only");
        }
        AtfUiEvidence evidenceAnnotation = context.getRequiredTestClass()
                .getAnnotation(AtfUiEvidence.class);
        AtfUiEvidenceFlow flow = evidenceAnnotation == null
                ? AtfUiEvidenceFlow.CONFIGURED : evidenceAnnotation.flow();
        AtfUiScreenshotPolicy screenshots = evidenceAnnotation == null
                ? AtfUiScreenshotPolicy.CONFIGURED : evidenceAnnotation.screenshots();
        var reportConfiguration = AtfReportConfigurationLoader.load();
        ReportEvidenceProfile evidence = AutoExEvidenceProfile.forRun(
                flow, screenshots, reportConfiguration.screenshotPolicy());
        ReportEvidenceProfile sessionEvidence = ReportEvidenceProfile.forScreenshotPolicy(
                reportConfiguration.screenshotPolicy());
        if (channel == TestChannel.UI_API) {
            configuration = withNetworkInspection(configuration);
        }
        var projectRoot = AutoExProjectRoot.discover();
        var uiBase = AtfTestResourceReader.resolveContained(
                projectRoot, configuration.uiReportBaseDirectory());
        var output = ReportOutputDirectory.resolve(projectRoot, uiBase,
                context.getRequiredTestClass());
        var console = AtfConsoleTestContext.configuredSink();
        var reportConsole = AutoExConsoleBridge.reportingSink(console);
        ReportResource reportResource = context.getRoot().getStore(NAMESPACE)
                .getOrComputeIfAbsent(SUITE_REPORT_KEY,
                        ignored -> new ReportResource(output, reportName(context), reportConsole, sessionEvidence),
                        ReportResource.class);
        UiExtentReportSession report = reportResource.report();
        AutoExConfiguration runConfiguration = configuration;
        return AutoExE2eInitialization.create(
                () -> PlaywrightUiSession.open(runConfiguration.browser(), console),
                browser -> report.startScenario(context.getRequiredTestClass().getSimpleName(),
                        context.getRequiredTestClass().getPackageName(), context.getDisplayName(),
                        List.of("e2e", "ui", "automation-exercise"), evidence),
                scenario -> scenario.startPhase("TEST BODY"),
                (browser, scenario, phase) -> new AutoExE2eRun(runConfiguration, browser, report,
                        scenario, phase, evidence, channel),
                phase -> phase.complete(AtfReportStatus.FAIL),
                scenario -> scenario.complete(AtfReportStatus.FAIL),
                PlaywrightUiSession::close);
    }

    private static AutoExConfiguration withNetworkInspection(AutoExConfiguration source) {
        UiBrowserConfiguration current = source.browser();
        var network = UiNetworkInspectionConfiguration.forHosts(Set.of(source.baseUrl().getHost()));
        var browser = new UiBrowserConfiguration(current.baseUrl(), current.browser(), current.headless(),
                current.timeout(), current.navigationTimeout(), current.viewportWidth(), current.viewportHeight(),
                current.zoomPercent(), current.screenshotOnFailure(), current.retryPolicy(), network);
        return new AutoExConfiguration(source.baseUrl(), source.e2eEnabled(), browser,
                source.uiReportBaseDirectory(), source.catalog(), source.registration(), source.payment());
    }

    private static final class ReportResource implements ExtensionContext.Store.CloseableResource {
        private final UiExtentReportSession report;

        private ReportResource(java.nio.file.Path output, String name,
                               atf.api.reporting.console.AtfConsoleSink console,
                               ReportEvidenceProfile evidence) {
            report = UiExtentReportSession.openConfigured(output, name, console, evidence);
        }

        private UiExtentReportSession report() { return report; }
        @Override public void close() { report.close(); }
    }

    private static String reportName(ExtensionContext context) {
        String method = context.getRequiredTestMethod().getName().replaceAll("[^A-Za-z0-9_-]", "-");
        String unique = UUID.randomUUID().toString().substring(0, 8);
        return REPORT_FILE_PREFIX + method + "_" + REPORT_TIME.format(LocalDateTime.now())
                + "_" + unique + ".html";
    }

    public AutoExConfiguration configuration() { return configuration; }
    public Page page() { return browser.page(); }
    public String currentUrl() { return page().url(); }

    /** Reads the configured product through the site's real, read-only catalog API. */
    @SuppressWarnings("unchecked")
    public AutoExCatalogSnapshot inspectCatalogApi(String productName) {
        java.net.URI endpoint = AutoExRoutes.PRODUCTS_API.resolve(configuration.baseUrl());
        Object result = page().evaluate("""
                async endpoint => {
                  const response = await fetch(endpoint, { headers: { Accept: 'application/json' } });
                  return { status: response.status, body: await response.json() };
                }
                """, endpoint.toString());
        if (!(result instanceof Map<?, ?> response)
                || !(response.get("status") instanceof Number status)
                || !(response.get("body") instanceof Map<?, ?> body)
                || !(body.get("products") instanceof List<?> products)) {
            throw new IllegalStateException("Catalog API returned an unexpected response shape");
        }
        String price = products.stream().filter(Map.class::isInstance)
                .map(Map.class::cast)
                .filter(product -> productName.equals(product.get("name")))
                .map(product -> Objects.toString(product.get("price"), ""))
                .findFirst().orElse("");
        actions.observation("API GET", AutoExRoutes.PRODUCTS_API.path(),
                "HTTP 200; configured product is listed", "HTTP " + status.intValue()
                        + "; products=" + products.size() + "; " + productName + " price="
                        + (price.isBlank() ? "not found" : price));
        return new AutoExCatalogSnapshot(status.intValue(), products.size(), price);
    }

    public void navigate(java.net.URI uri) {
        prepareUiInteraction();
        actions.navigate(page(), Objects.requireNonNull(uri, "uri"), "requested page is loaded");
    }

    public void click(String selector, String expected) {
        prepareUiInteraction();
        actions.click(locator(selector), expected);
    }

    public void clickFirst(String selector, String expected) {
        prepareUiInteraction();
        actions.click(new AtfLocator(page().locator(selector).first(), selector), expected);
    }

    public void clickLast(String selector, String expected) {
        prepareUiInteraction();
        actions.click(new AtfLocator(page().locator(selector).last(), selector), expected);
    }

    public void fill(String selector, String value, String expected) {
        prepareUiInteraction();
        actions.fill(locator(selector), value, expected);
    }

    public String text(String selector, String expected) {
        prepareUiInteraction();
        return actions.getText(locator(selector), expected);
    }

    public String textFirst(String selector, String expected) {
        prepareUiInteraction();
        return actions.getText(new AtfLocator(page().locator(selector).first(), selector + " (first match)"), expected);
    }

    public void select(String selector, String label, String expected) {
        prepareUiInteraction();
        actions.selectOption(locator(selector), label, expected);
    }

    public boolean visible(String selector) {
        prepareUiInteraction();
        return page().locator(selector).isVisible();
    }

    public int count(String selector) {
        prepareUiInteraction();
        return page().locator(selector).count();
    }

    public String firstAttribute(String selector, String attribute) {
        prepareUiInteraction();
        return page().locator(selector).first().getAttribute(attribute);
    }

    private void checkpoint(AtfGherkinKeyword keyword, String title, List<AtfAssertion> assertions) {
        prepareUiInteraction();
        boolean passed = assertions.stream().allMatch(AtfAssertion::passed);
        String screenshot = evidenceProfile.captures(ReportScreenshotPolicy.CapturePoint.CHECKPOINT)
                ? capture(title) : "";
        List<ReportExecutionEvidence> apiEvidence = channel == TestChannel.UI_API
                ? networkEvidenceSinceCursor() : List.of();
        var actionEvidence = ReportModelAdapters.uiActions(actions.drain());
        AtfReportStatus status = passed ? AtfReportStatus.PASS : AtfReportStatus.FAIL;
        phase.step(keyword, title, currentUrl(), screenshot, assertions, actionEvidence,
                apiEvidence, ReportScreenshotPolicy.CapturePoint.CHECKPOINT, status);
        if (channel == TestChannel.UI_API) {
            networkSteps.add(ReportNetworkInspector.Step.from(keyword.text(), title, assertions, apiEvidence));
        }
        if (!passed) throw new AssertionError("UI checkpoint failed: " + title);
    }

    public void checkpoint(AutoExPurchaseTraceStep step, List<AtfAssertion> assertions) {
        checkpoint(step.keyword(), step.title(), assertions);
    }

    /** Publishes the terminal state; FINAL_ONLY creates exactly this one screenshot. */
    public void finalScreenshot(String title, List<AtfAssertion> assertions) {
        prepareUiInteraction();
        boolean passed = assertions.stream().allMatch(AtfAssertion::passed);
        String screenshot = evidenceProfile.captures(ReportScreenshotPolicy.CapturePoint.FINAL)
                ? capture(title) : "";
        AutoExScreenAuditObservations.recordTerminalScreenContract(
                actions, title, currentUrl(), assertions, !screenshot.isBlank(), passed);
        phase.screenValidation(title, currentUrl(), screenshot, assertions,
                ReportModelAdapters.uiActions(actions.drain()), ReportScreenshotPolicy.CapturePoint.FINAL,
                passed ? AtfReportStatus.PASS : AtfReportStatus.FAIL);
        if (channel == TestChannel.UI_API) {
            List<ReportExecutionEvidence> apiEvidence = networkEvidenceSinceCursor();
            networkSteps.add(ReportNetworkInspector.Step.from(AtfGherkinKeyword.THEN.text(),
                    title, assertions, apiEvidence));
        }
        if (!passed) throw new AssertionError("Final screen validation failed: " + title
                + ". Failed assertions: " + failedAssertionSummary(assertions));
    }

    public void finalScreenshot(AutoExPurchaseTraceStep step, List<AtfAssertion> assertions) {
        finalScreenshot(step.title(), assertions);
    }

    private String capture(String title) {
        return UiScreenValidation.capture(page(), UiScreenValidationConfiguration.defaults(), title,
                List.of(page().locator(AutoExSelectors.SENSITIVE_FIELDS))).base64Png();
    }

    private List<ReportExecutionEvidence> networkEvidenceSinceCursor() {
        var observations = apiObservations();
        List<ReportExecutionEvidence> result = new ArrayList<>();
        for (int index = networkCursor; index < observations.size(); index++) {
            var observation = observations.get(index);
            result.add(toExecutionEvidence(observation));
        }
        networkCursor = observations.size();
        return List.copyOf(result);
    }

    public void onFailureCleanup(AutoExPurchaseSession.AutoExAccountCleanup cleanup) {
        failureCleanup = Objects.requireNonNull(cleanup, "cleanup");
    }

    public void clearFailureCleanup() { failureCleanup = null; }

    void finish(Throwable testFailure) throws Exception {
        if (finished) return;
        finished = true;
        Throwable terminalFailure = testFailure;
        boolean cleanupWasPending = failureCleanup != null;
        if (failureCleanup != null) {
            try { failureCleanup.run(); }
            catch (Exception | Error cleanupFailure) {
                terminalFailure = FailureAwareCleanup.combine(terminalFailure, cleanupFailure);
            }
            failureCleanup = null;
        }
        if (testFailure == null && cleanupWasPending && terminalFailure == null) {
            terminalFailure = new AssertionError(
                    "E2E completed without explicitly deleting its disposable account");
        }

        terminalFailure = FailureAwareCleanup.runAll(terminalFailure,
                () -> {
                    if (channel == TestChannel.UI_API) {
                        phase.networkInspector(NETWORK_SUMMARY_TITLE,
                                causalApiObservations().stream()
                                        .map(AutoExE2eRun::toExecutionEvidence).toList(), networkSteps);
                    }
                },
                browser::close);
        if (terminalFailure != null) publishFailure(terminalFailure);
        AtfReportStatus status = terminalFailure == null ? AtfReportStatus.PASS : AtfReportStatus.FAIL;
        terminalFailure = FailureAwareCleanup.runAll(terminalFailure, () -> phase.complete(status));
        AtfReportStatus scenarioStatus = terminalFailure == null ? status : AtfReportStatus.FAIL;
        terminalFailure = FailureAwareCleanup.runAll(terminalFailure, () -> scenario.complete(scenarioStatus));

        if (testFailure == null && terminalFailure != null) {
            if (terminalFailure instanceof Exception exception) throw exception;
            if (terminalFailure instanceof Error error) throw error;
            throw new IllegalStateException("Automation Exercise E2E teardown failed", terminalFailure);
        }
    }

    private void publishFailure(Throwable failure) {
        String screenshot = "";
        if (evidenceProfile.captures(ReportScreenshotPolicy.CapturePoint.FAILURE)) {
            try {
                screenshot = UiScreenValidation.capture(page(), UiScreenValidationConfiguration.defaults(),
                        "Failure state — sensitive fields masked",
                        List.of(page().locator(AutoExSelectors.SENSITIVE_FIELDS))).base64Png();
            } catch (RuntimeException | Error captureFailure) {
                failure.addSuppressed(captureFailure);
            }
        }
        try {
            phase.failureEvidence("Automation Exercise UI E2E failure", currentUrl(),
                    ReportModelAdapters.uiActions(actions.drain()), failure, screenshot);
        } catch (RuntimeException | Error reportFailure) {
            failure.addSuppressed(reportFailure);
        }
    }

    public boolean dismissGoogleVignetteIfPresent() {
        return vignetteHandler.dismissIfPresent(page(), actions);
    }

    public boolean dismissPrivacyPromptIfPresent() {
        return privacyConsentHandler.rejectOptionalProcessingIfPrompted(page(), actions);
    }

    public void markCausalApiTrafficBaseline() {
        causalApiTraffic.markBaseline(apiObservations());
    }
    public boolean observesApiTraffic() { return channel == TestChannel.UI_API; }

    public TestChannel channel() { return channel; }

    private static ReportExecutionEvidence toExecutionEvidence(
            atf.api.ui.network.UiNetworkObservation observation) {
        var request = new ReportRequest(observation.method(), observation.path(), Map.of(),
                observation.requestHeaders(), "browser-observed", observation.requestBody());
        var response = new ReportResponse(observation.status(), observation.responseBody(),
                observation.responseHeaders(), null, observation.url());
        var trace = new ReportTraceMetadata("", observation.sequence(), "TEST BODY", "api", "",
                "browser", "", observation.requestSignature(), "", "",
                observation.evidenceSource(), observation.service(), "", "");
        return new ReportExecutionEvidence(response, 1, null, List.of(), List.of(), request,
                ReportTransportProvenance.LIVE_HTTP, trace);
    }

    private List<atf.api.ui.network.UiNetworkObservation> apiObservations() {
        return browser.networkInspector().observations().stream()
                .filter(observation -> observation.path().startsWith("/api/"))
                .toList();
    }

    private List<atf.api.ui.network.UiNetworkObservation> causalApiObservations() {
        return causalApiTraffic.causalSince(apiObservations());
    }

    private void prepareUiInteraction() {
        dismissPrivacyPromptIfPresent();
        dismissGoogleVignetteIfPresent();
    }

    private static String failedAssertionSummary(List<AtfAssertion> assertions) {
        return assertions.stream().filter(assertion -> !assertion.passed())
                .map(assertion -> assertion.name() + " [expected=" + assertion.expected()
                        + "; actual=" + assertion.actual() + "]")
                .collect(java.util.stream.Collectors.joining("; "));
    }

    private AtfLocator locator(String selector) { return new AtfLocator(page().locator(selector), selector); }

}
