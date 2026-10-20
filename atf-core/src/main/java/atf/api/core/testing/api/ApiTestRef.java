package atf.api.core.testing.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** One stable API scenario method selected by a consumer-side audit. */
@Target({})
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiTestRef {
    Class<?> owner();

    String method();
}
