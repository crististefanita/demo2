package atf.ui.practiceshop.support;

/** Pure decision seam for the configured screenshot policy at a checkpoint. */
public final class PracticeShopScreenshotPolicy {
    private PracticeShopScreenshotPolicy() { }

    /**
     * A failed checkpoint always needs evidence. A passed checkpoint needs
     * evidence only when the configured policy requests it.
     */
    public static boolean shouldCapture(boolean checkpointPassed, boolean screenshotOnFailure) {
        return !checkpointPassed || screenshotOnFailure;
    }
}
