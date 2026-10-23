package atf.ui.autoex.support.lifecycle;

/** Separates the effective opt-in decision from JUnit extension wiring. */
public final class AutoExE2eActivationPolicy {
    private AutoExE2eActivationPolicy() { }

    public static Decision evaluate(String propertyOverride, boolean yamlEnabled, boolean ideRunnerDetected) {
        if (propertyOverride != null) {
            boolean enabled = Boolean.parseBoolean(propertyOverride);
            return new Decision(enabled, enabled
                    ? "enabled by JVM property atf.autoex.e2e.enabled=true"
                    : "disabled by explicit JVM property atf.autoex.e2e.enabled=false");
        }
        if (yamlEnabled) return new Decision(true, "enabled by AutoEx YAML configuration");
        if (ideRunnerDetected) return new Decision(true, "enabled by IntelliJ runner detection heuristic");
        return new Decision(false, "set -Datf.autoex.e2e.enabled=true to run public-site E2E");
    }

    public record Decision(boolean enabled, String reason) { }
}
