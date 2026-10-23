package atf.ui.autoex.support.lifecycle;

import atf.api.testkit.lifecycle.FailureAwareCleanup;

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
        B browser = null;
        S scenario = null;
        P phase = null;
        try {
            browser = browserFactory.get();
            scenario = scenarioFactory.apply(browser);
            phase = phaseFactory.apply(scenario);
            return runFactory.create(browser, scenario, phase);
        } catch (RuntimeException | Error setupFailure) {
            P acquiredPhase = phase;
            S acquiredScenario = scenario;
            B acquiredBrowser = browser;
            FailureAwareCleanup.runAll(setupFailure,
                    () -> { if (acquiredPhase != null) failPhase.accept(acquiredPhase); },
                    () -> { if (acquiredScenario != null) failScenario.accept(acquiredScenario); },
                    () -> { if (acquiredBrowser != null) closeBrowser.accept(acquiredBrowser); });
            throw setupFailure;
        }
    }

    @FunctionalInterface
    public interface RunFactory<B, S, P, R> {
        R create(B browser, S scenario, P phase);
    }
}
