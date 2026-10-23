package atf.ui.autoex.support;

/** Keeps the original test failure primary while preserving teardown failures. */
public final class AutoExFailureCombiner {
    private AutoExFailureCombiner() { }

    public static Throwable combine(Throwable primary, Throwable secondary) {
        if (primary == null) return secondary;
        if (primary != secondary) primary.addSuppressed(secondary);
        return primary;
    }
}
