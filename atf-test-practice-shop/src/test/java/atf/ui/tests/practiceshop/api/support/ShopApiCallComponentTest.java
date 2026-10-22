package atf.ui.tests.practiceshop.api.support;

import atf.api.testkit.AtfComponentTest;
import atf.ui.tests.practiceshop.api.reporting.ShopApiCallAuditTestSupport;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Marks deterministic component tests whose actual exchanges belong in the API-call audit report. */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@AtfComponentTest
@Tag("api-call")
@ExtendWith(ShopApiCallAuditTestSupport.class)
public @interface ShopApiCallComponentTest { }
