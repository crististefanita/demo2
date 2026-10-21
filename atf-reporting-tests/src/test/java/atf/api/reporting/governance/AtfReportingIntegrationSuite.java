package atf.api.reporting.governance;

import atf.api.testkit.AtfIntegrationTest;
import org.junit.platform.suite.api.ExcludeClassNamePatterns;
import org.junit.platform.suite.api.IncludeTags;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

/** IntelliJ/Maven entry point for reporting integration contracts. */
@Suite
@SelectPackages("atf.api.reporting")
@IncludeTags("integration")
@ExcludeClassNamePatterns(".*Suite")
@AtfIntegrationTest
public class AtfReportingIntegrationSuite {
}
