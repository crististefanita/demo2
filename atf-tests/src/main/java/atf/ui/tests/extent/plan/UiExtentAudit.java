package atf.ui.tests.extent.plan;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Programmatic contract for one reusable UI Extent audit. */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface UiExtentAudit {
    UiReportAuditRule[] rules() default {};
}
