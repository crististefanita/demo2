package atf.api.tests.support;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares that a UI exploration needs an API artifact prepared first.
 * Exact {@link #tests()} selection takes precedence over families and YAML.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiScenarioSetup {
    String[] families() default {};

    ApiTestRef[] tests() default {};
}
