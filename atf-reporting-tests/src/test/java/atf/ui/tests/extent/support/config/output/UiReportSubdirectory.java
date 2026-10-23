package atf.ui.tests.extent.support.config.output;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Selects a safe, relative subdirectory beneath the standard UI report output. */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface UiReportSubdirectory {
    String value();
}
