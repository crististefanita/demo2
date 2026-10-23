package atf.ui.autoex.support.lifecycle;

import atf.api.reporting.contract.AtfReportStatus;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/** Acquires the browser/report nodes atomically and rolls back every acquired owner. */
public final class AutoExE2eInitialization {
    private AutoExE2eInitialization() { }

    public static <B, S, P, R> R create(Supplier<B> browserFactory,
                                       Function<B, S> scenarioFactory,
                                       Function<S, P> phaseFactory,
                                       RunFactory<B, S, P, R> runFactory,
                                       Consumer<P> failPhase,
                                       Consumer<S> failScenario,
                                       Consumer<B> closeBrowser) {
        Objects.requireNonNull(browserFactory, "browserFactory");
        Objects.requireNonNull(scenarioFactory, "scenarioFactory");
        Objects.requireNonNull(phaseFactory, "phaseFactory");
        Objects.requireNonNull(runFactory, "runFactory");
        Objects.requireNonNull(failPhase, "failPhase");
        Objects.requireNonNull(failScenario, "failScenario");
        Objects.requireNonNull(closeBrowser, "closeBrowser");

        B browser = null;
        S scenario = null;
        P phase = null;
        try {
            browser = Objects.requireNonNull(browserFactory.get(), "browserFactory returned null");
            scenario = Objects.requireNonNull(scenarioFactory.apply(browser), "scenarioFactory returned null");
            phase = Objects.requireNonNull(phaseFactory.apply(scenario), "phaseFactory returned null");
            return Objects.requireNonNull(runFactory.create(browser, scenario, phase), "runFactory returned null");
        } catch (RuntimeException | Error setupFailure) {
            P acquiredPhase = phase;
            if (acquiredPhase != null) {
                AutoExTeardown.attempt(setupFailure,
                        () -> failPhase.accept(acquiredPhase));
            }
            S acquiredScenario = scenario;
            if (acquiredScenario != null) {
                AutoExTeardown.attempt(setupFailure,
                        () -> failScenario.accept(acquiredScenario));
            }
            B acquiredBrowser = browser;
            if (acquiredBrowser != null) {
                AutoExTeardown.attempt(setupFailure,
                        () -> closeBrowser.accept(acquiredBrowser));
            }
            throw setupFailure;
        }
    }

    @FunctionalInterface
    public interface RunFactory<B, S, P, R> {
        R create(B browser, S scenario, P phase);
    }
}
