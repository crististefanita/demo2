package atf.ui.tests.practiceshop.support.lifecycle;

/** Keeps public-site E2E execution explicit outside the IntelliJ run workflow. */
public final class ShopE2eActivationPolicy {
    private static final String ENABLE_PROPERTY = "atf.practice.shop.e2e.enabled";

    private ShopE2eActivationPolicy() { }

    public static Decision evaluate(String propertyOverride, boolean ideRunnerDetected) {
        if (propertyOverride != null) {
            if ("true".equalsIgnoreCase(propertyOverride)) {
                return new Decision(true, "enabled by JVM property " + ENABLE_PROPERTY + "=true");
            }
            if ("false".equalsIgnoreCase(propertyOverride)) {
                return new Decision(false, "disabled by explicit JVM property " + ENABLE_PROPERTY + "=false");
            }
            return new Decision(false, "disabled because JVM property " + ENABLE_PROPERTY
                    + " must be exactly true or false");
        }
        if (ideRunnerDetected) {
            return new Decision(true, "enabled by IntelliJ runner detection heuristic");
        }
        return new Decision(false, "set -D" + ENABLE_PROPERTY + "=true to run Practice Shop E2E");
    }

    public record Decision(boolean enabled, String reason) { }
}
