package atf.api.console.junit;

/** Thread-bound bridge from a directly running JUnit test to its console lifecycle. */
public final class AtfConsoleTestContext {
    private static final ThreadLocal<AtfConsoleTestLifecycle> CURRENT = new ThreadLocal<>();

    private AtfConsoleTestContext() { }

    static void install(AtfConsoleTestLifecycle lifecycle) { CURRENT.set(lifecycle); }
    static void clear() { CURRENT.remove(); }
    public static AtfConsoleTestLifecycle current() { return CURRENT.get(); }
}
