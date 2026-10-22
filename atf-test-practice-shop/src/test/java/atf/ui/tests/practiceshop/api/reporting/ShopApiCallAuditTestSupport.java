package atf.ui.tests.practiceshop.api.reporting;

import atf.api.reporting.contract.AtfReportPorts;
import atf.api.reporting.contract.AtfReportStatus;
import atf.api.reporting.extent.session.ExtentReportSession;
import atf.ui.practiceshop.config.ShopConfigurationLoader;
import atf.ui.practiceshop.support.ShopApiReportPublisher;
import atf.ui.tests.practiceshop.api.support.ShopApiCallAuditContextTestSupport;
import atf.ui.tests.practiceshop.api.support.ShopApiCallAuditContextTestSupport.CapturedExchange;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/** Captures transport exchanges and keeps JUnit's method verdict on the scenario. */
public final class ShopApiCallAuditTestSupport implements BeforeEachCallback, AfterEachCallback {
    private static final ExtensionContext.Namespace NAMESPACE =
            ExtensionContext.Namespace.create(ShopApiCallAuditTestSupport.class);
    private static final String RUN_KEY = "api-call-audit-run";
    private static final String CALLS_KEY = "api-call-audit-exchanges";

    private record TestCalls(List<CapturedExchange> exchanges) { }

    @Override
    public void beforeEach(ExtensionContext context) {
        List<CapturedExchange> calls = new ArrayList<>();
        context.getStore(NAMESPACE).put(CALLS_KEY, new TestCalls(calls));
        ShopApiCallAuditContextTestSupport.recordWith(calls::add);
    }

    @Override
    public void afterEach(ExtensionContext context) {
        List<String> associationGaps = ShopApiCallAuditContextTestSupport.unconsumedExpectations();
        ShopApiCallAuditContextTestSupport.clear();
        TestCalls captured = context.getStore(NAMESPACE).remove(CALLS_KEY, TestCalls.class);
        List<CapturedExchange> calls = captured == null ? List.of() : captured.exchanges();
        SuiteReport report = context.getRoot().getStore(NAMESPACE)
                .getOrComputeIfAbsent(RUN_KEY, ignored -> new SuiteReport(), SuiteReport.class);
        report.record(context, calls, associationGaps);
    }

    private static final class SuiteReport implements ExtensionContext.Store.CloseableResource {
        private final ExtentReportSession session = openSession();

        synchronized void record(ExtensionContext context, List<CapturedExchange> calls,
                                 List<String> associationGaps) {
            Class<?> testClass = context.getRequiredTestClass();
            String displayName = context.getDisplayName();
            var scenario = session.startScenario(testClass.getSimpleName(), testClass.getPackageName(),
                    displayName, List.of("api", "component", "api-call"));
            var phase = scenario.startPhase("TEST BODY");
            Throwable failure = context.getExecutionException().orElse(null);
            AtfReportStatus status = failure == null ? AtfReportStatus.PASS : AtfReportStatus.FAIL;
            try {
                long sequence = 0;
                for (CapturedExchange call : calls) {
                    ShopApiReportPublisher.publishObserved(phase, call.execution(), ++sequence,
                            call.contractValidation());
                }
                long contractedCount = calls.stream()
                        .filter(call -> call.contractValidation() != null).count();
                publishAuditDetails(phase, failure, calls.size(), contractedCount, associationGaps);
                phase.complete(status);
                scenario.complete(status);
            } finally {
                phase.close();
                scenario.close();
            }
        }

        private static ExtentReportSession openSession() {
            Path directory = ShopConfigurationLoader.load().apiReportDirectory();
            String timestamp = Instant.now().toString().replace(":", "-");
            return new ExtentReportSession(directory.resolve(
                    "api-atf-practice-shop-api-call-audit_" + timestamp + ".html"));
        }

        @Override
        public void close() {
            session.close();
        }
    }

    static void publishAuditDetails(AtfReportPorts.Phase phase, Throwable failure,
                                    int exchangeCount, long contractedCount,
                                    List<String> associationGaps) {
        String captured = exchangeCount + (exchangeCount == 1
                ? " actual exchange" : " actual exchanges") + " captured; "
                + contractedCount + " typed HTTP contracts attached";
        phase.detail("API audit completeness", captured);
        if (!associationGaps.isEmpty()) {
            phase.detail("API contract association", String.join("; ", associationGaps));
        }
        if (failure != null) {
            String message = failure.getMessage();
            String reason = failure.getClass().getSimpleName()
                    + (message == null || message.isBlank() ? "" : ": " + message);
            phase.detail("JUnit failure", reason);
        }
    }
}
