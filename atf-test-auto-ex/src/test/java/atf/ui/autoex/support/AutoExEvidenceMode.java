package atf.ui.autoex.support;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Selects the AutoEx report-flow layout and screenshot policy without changing the purchase journey. */
@Inherited
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface AutoExEvidenceMode {
    Mode value() default Mode.CONFIGURED;

    enum Mode {
        /** Uses configured screenshot behavior and independent checkpoint nodes. */
        CONFIGURED,
        /** Keeps independent checkpoint nodes and captures each checkpoint. */
        CHECKPOINT_SCREENSHOTS,
        /** Consolidates browser actions into one Playwright table and captures only the terminal screen. */
        FINAL_SCREENSHOT_ONLY;

        public boolean capturesCheckpointScreenshots() {
            return this != FINAL_SCREENSHOT_ONLY;
        }
    }
}
