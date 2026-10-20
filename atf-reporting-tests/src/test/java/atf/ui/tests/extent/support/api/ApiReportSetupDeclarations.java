package atf.ui.tests.extent.support.api;
import atf.api.core.testing.api.ApiScenarioSetup;


/** Annotation-only classes used to test API-family setup resolution. */
public final class ApiReportSetupDeclarations {
    private ApiReportSetupDeclarations() {
    }

    @ApiScenarioSetup(families = {"users", "posts"})
    public static final class UsersPosts {
        private UsersPosts() {
        }
    }

    @ApiScenarioSetup
    public static final class Empty {
        private Empty() {
        }
    }
}
