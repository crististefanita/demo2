package atf.ui.autoex.support;

/** Keeps the original test failure primary while preserving teardown failures. */
final class AutoExFailureCombiner {
    private AutoExFailureCombiner() { }

    static Throwable combine(Throwable primary, Throwable secondary) {
        if (primary == null) return secondary;
        if (primary != secondary) primary.addSuppressed(secondary);
        return primary;
    }
}
