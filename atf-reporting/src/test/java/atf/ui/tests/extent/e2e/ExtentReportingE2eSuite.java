package atf.ui.tests.extent.e2e;
import atf.api.testkit.AtfE2eTest;

import org.junit.platform.suite.api.ExcludeClassNamePatterns;
import org.junit.platform.suite.api.IncludeTags;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

/** Single IntelliJ entry point for the browser-backed Extent reporting E2E contract. */
@Suite
@SelectPackages("atf.ui.tests.extent.e2e")
@IncludeTags("e2e")
@ExcludeClassNamePatterns(".*Suite")
@AtfE2eTest
public class ExtentReportingE2eSuite { }
