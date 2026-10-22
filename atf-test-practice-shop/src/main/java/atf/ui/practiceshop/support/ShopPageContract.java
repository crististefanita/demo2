package atf.ui.practiceshop.support;

import atf.ui.practiceshop.config.ShopScreen;
import atf.ui.practiceshop.locators.ShopControl;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Declarative contract for one navigable Practice Shop page. */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ShopPageContract {
    ShopScreen screen();
    /** Exact route or a route prefix ending in {@code /*}. */
    String route();
    ShopControl[] requiredControls();
    String bodyMarker() default "";
}
