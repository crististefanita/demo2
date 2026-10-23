package atf.ui.autoex.support.lifecycle;

import atf.ui.autoex.support.AutoExFailureCombiner;

/** Runs every teardown action while retaining the original failure as primary. */
public final class AutoExTeardown {
    private AutoExTeardown() { }

    public static Throwable attempt(Throwable primaryFailure, Action action) {
        try {
            action.run();
            return primaryFailure;
        } catch (Exception | Error teardownFailure) {
            return AutoExFailureCombiner.combine(primaryFailure, teardownFailure);
        }
    }

    @FunctionalInterface
    public interface Action {
        void run() throws Exception;
    }
}
