package atf.ui.tests.autoex.api;

/** Arms deletion before account creation so partial server-side creation is also cleaned up. */
public final class AutoExApiAccountCleanup implements AutoCloseable {
    private final Runnable cleanup;
    private boolean armed;

    public AutoExApiAccountCleanup(Runnable cleanup) {
        this.cleanup = java.util.Objects.requireNonNull(cleanup, "cleanup");
    }

    public void armBeforeCreateAttempt() {
        armed = true;
    }

    @Override
    public void close() {
        if (!armed) return;
        cleanup.run();
        armed = false;
    }
}
