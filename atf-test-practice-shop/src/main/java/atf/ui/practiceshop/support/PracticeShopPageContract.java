package atf.ui.practiceshop.support;

import atf.ui.practiceshop.config.PracticeShopScreen;
import atf.ui.practiceshop.locators.PracticeShopControl;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Declarative contract for one navigable Practice Shop page. */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface PracticeShopPageContract {
    PracticeShopScreen screen();
    /** Exact route or a route prefix ending in {@code /*}. */
    String route();
    PracticeShopControl[] requiredControls();
    String bodyMarker() default "";
}
