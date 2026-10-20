package atf.api.tests.governance.documentation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Selects one project-relative README or governed Markdown document for the
 * plug-and-play documentation governance smoke test.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface DocumentationTarget {
    String value();
}
