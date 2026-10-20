package atf.api.reporting.runtime.section;

/** Runtime controller for Tests navigation data, filters and Categories projection. */
public final class ExtentReportRuntimeTests {
    private ExtentReportRuntimeTests() { }

    public static String script() {
        return String.join("",
                atf.api.reporting.runtime.tests.ExtentReportRuntimeTestFilters.script(),
                atf.api.reporting.runtime.tests.ExtentReportRuntimeCategories.script(),
                atf.api.reporting.runtime.tests.ExtentReportRuntimeScenarios.script());
    }
}
