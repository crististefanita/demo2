package atf.api.testkit.reporting;

import atf.api.testkit.AtfUnitTest;
import org.junit.jupiter.api.Test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@AtfUnitTest
class AtfReportPathPolicyResolverTest {
    @Test
    void absenceDisablesAndDirectDeclarationOptsIn() {
        assertEquals(AtfReportPath.Policy.DISABLED,
                AtfReportPathPolicyResolver.resolve(Unconfigured.class));
        assertEquals(AtfReportPath.Policy.ENABLED,
                AtfReportPathPolicyResolver.resolve(DirectOptIn.class));
    }

    @Test
    void policySupportsMetaAnnotationsAndNearestSuperclassOverride() {
        assertEquals(AtfReportPath.Policy.ENABLED,
                AtfReportPathPolicyResolver.resolve(MetaOptIn.class));
        assertEquals(AtfReportPath.Policy.DISABLED,
                AtfReportPathPolicyResolver.resolve(ExplicitOptOut.class));
    }

    @Test
    void conflictingComposedPoliciesFailBeforeReportSetup() {
        assertThrows(IllegalStateException.class,
                () -> AtfReportPathPolicyResolver.resolve(Conflicting.class));
    }

    static class Unconfigured { }

    @AtfReportPath
    static class DirectOptIn { }

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @AtfReportPath
    @interface ReportOwner { }

    @ReportOwner
    static class MetaOptIn { }

    @AtfReportPath
    static class InheritedOwner { }

    @AtfReportPath(AtfReportPath.Policy.DISABLED)
    static class ExplicitOptOut extends InheritedOwner { }

    @AtfReportPath
    @DisabledReportPath
    static class Conflicting { }

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @AtfReportPath(AtfReportPath.Policy.DISABLED)
    @interface DisabledReportPath { }
}
