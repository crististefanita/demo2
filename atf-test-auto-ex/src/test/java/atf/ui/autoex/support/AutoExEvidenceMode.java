package atf.ui.autoex.support;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Selects a report-evidence variation without changing the purchase journey. */
@Inherited
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface AutoExEvidenceMode {
    Mode value() default Mode.CONFIGURED;

    enum Mode {
        CONFIGURED,
        CHECKPOINT_SCREENSHOTS,
        FINAL_SCREENSHOT_ONLY;

        public boolean capturesCheckpointScreenshots() {
            return this != FINAL_SCREENSHOT_ONLY;
        }
    }
}
