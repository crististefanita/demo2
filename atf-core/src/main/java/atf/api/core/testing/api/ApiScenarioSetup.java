package atf.api.core.testing.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares the API scenarios required by a consumer-side UI audit.
 * Exact {@link #tests()} selection takes precedence over families and YAML.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiScenarioSetup {
    String[] families() default {};

    ApiTestRef[] tests() default {};
}
