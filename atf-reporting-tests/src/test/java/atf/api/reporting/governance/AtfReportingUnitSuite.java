package atf.api.reporting.governance;

import atf.api.testkit.AtfUnitTest;
import org.junit.platform.suite.api.ExcludeClassNamePatterns;
import org.junit.platform.suite.api.IncludeTags;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

/** IntelliJ/Maven entry point for reporting-only unit contracts. */
@Suite
@SelectPackages("atf.api.reporting")
@IncludeTags("unit")
@ExcludeClassNamePatterns(".*Suite")
@AtfUnitTest
public class AtfReportingUnitSuite {
}
