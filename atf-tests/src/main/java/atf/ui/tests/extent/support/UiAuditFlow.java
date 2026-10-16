package atf.ui.tests.extent.support;

import atf.api.core.reporting.AtfAssertion;
import atf.api.core.reporting.AtfReportStatus;
import atf.api.core.ui.AtfUiActionRecord;
import atf.api.reporting.UiExtentReportSession;
import atf.api.ui.config.UiConfiguration;
import atf.api.ui.playwright.PlaywrightActionSequence;
import atf.api.ui.playwright.UiScreenValidation;
import com.microsoft.playwright.Page;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.nio.file.Files;

/**
 * Typed orchestration boundary shared by UI report audits.
 *
 * <p>It owns lifecycle and evidence publication. DOM selectors and semantic
 * snapshots are delegated to flow/auditor classes; the calling test keeps only
 * Gherkin intent and assertions.</p>
 */
public final class UiAuditFlow {
    private final ReportAuditContext context;
    private final UiConfiguration configuration;
    private final String testClass;
    private final String testPackage;
    private final List<String> tags;
    private final String setupDescription;
    private final String failureTitle;
    private final String failureTarget;
    private PlaywrightActionSequence actions;

    public UiAuditFlow(ReportAuditContext context, UiConfiguration configuration,
                       String testClass, String testPackage, List<String> tags,
                       String setupDescription, String failureTitle, String failureTarget) {
        this.context = Objects.requireNonNull(context, "context");
        this.configuration = Objects.requireNonNull(configuration, "configuration");
        this.testClass = Objects.requireNonNull(testClass, "testClass");
        this.testPackage = Objects.requireNonNull(testPackage, "testPackage");
        this.tags = List.copyOf(Objects.requireNonNull(tags, "tags"));
        this.setupDescription = Objects.requireNonNull(setupDescription, "setupDescription");
        this.failureTitle = Objects.requireNonNull(failureTitle, "failureTitle");
        this.failureTarget = Objects.requireNonNull(failureTarget, "failureTarget");
    }

    public Page openTab(String hash) {
        actions = context.newActionSequence();
        return context.openTab(hash, actions);
    }

    public PlaywrightActionSequence actions() {
        if (actions == null) {
            actions = context.newActionSequence();
        }
        return actions;
    }

    public UiScreenValidation.Evidence capture(Page page, String title) {
        Objects.requireNonNull(page, "page");
        UiScreenValidation.Evidence evidence = UiScreenValidation.capture(
                page, configuration.screenValidation(), title);
        actions().observation("Screenshot", "current page", "visual state captured",
                "Base64 screenshot link");
        return evidence;
    }

    public void audit(String title, AuditAction body) {
        auditResult(title, audit -> {
            body.run(audit);
            return null;
        });
    }

    /**
     * Renderer adapter variant used by section modules. The callback returns
     * its pure result after publishing the same Extent evidence.
     */
    public <T> T auditResult(String title, AuditActionWithResult<T> body) {
        Objects.requireNonNull(title, "title");
        Objects.requireNonNull(body, "body");
        actions = context.newActionSequence();
        UiExtentReportSession.UiScenario scenario = context.startScenario(
                testClass, testPackage, title, tags);
        boolean inputReportExists = Files.isRegularFile(context.inputReport());
        var setup = scenario.startPhase(ReportUiContract.Phase.SETUP.displayName());
        setup.step("GIVEN", setupDescription, context.inputArtifactIdentity().display(), null,
                List.of(ReportAuditChecks.check("Input report", "published HTML",
                        context.inputReport().toString(), inputReportExists)),
                inputReportExists ? AtfReportStatus.PASS : AtfReportStatus.FAIL);
        setup.complete(inputReportExists ? AtfReportStatus.PASS : AtfReportStatus.FAIL);
        if (!inputReportExists) {
            throw new IllegalStateException("UI audit input report does not exist: " + context.inputReport());
        }
        AuditSession session = new AuditSession(scenario,
                scenario.startPhase(ReportUiContract.Phase.TEST_BODY.displayName()));
        try {
            T result = body.run(session);
            session.pass();
            return result;
        } catch (AssertionError | RuntimeException failure) {
            session.fail(failure);
            throw failure;
        }
    }

    @FunctionalInterface
    public interface AuditAction {
        void run(AuditSession audit);
    }

    @FunctionalInterface
    public interface AuditActionWithResult<T> {
        T run(AuditSession audit);
    }

    public final class AuditSession {
        private final UiAuditLifecycle lifecycle;

        private AuditSession(UiExtentReportSession.UiScenario scenario,
                             UiExtentReportSession.UiPhase body) {
            this.lifecycle = new UiAuditLifecycle(scenario, body);
        }

        public void step(String keyword, String title, String target, String screenshot,
                         Collection<AtfAssertion> assertions, AtfReportStatus status) {
            lifecycle.screenValidation(title, target, screenshot, assertions,
                    actions == null ? List.of() : actions.drain(), status);
        }

        public void screen(String title, String target, UiScreenValidation.Evidence evidence,
                           Collection<AtfAssertion> assertions, AtfReportStatus status) {
            Objects.requireNonNull(evidence, "evidence");
            step("THEN", title, target, evidence.base64Png(), assertions, status);
        }

        private void pass() {
            lifecycle.pass();
        }

        private void fail(Throwable failure) {
            UiScreenValidation.Evidence failureEvidence = context.captureFailure(
                    configuration, failureTitle);
            lifecycle.fail(failureTitle, failureTarget,
                    actions == null ? List.of() : actions.drain(), failure,
                    failureEvidence.hasScreenshotPayload() ? failureEvidence.base64Png() : null);
        }
    }
}
