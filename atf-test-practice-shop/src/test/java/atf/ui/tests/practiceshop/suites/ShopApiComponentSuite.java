package atf.ui.tests.practiceshop.suites;

import atf.api.testkit.AtfComponentTest;
import org.junit.platform.suite.api.IncludeTags;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

/** Runs the Practice Shop API component contracts and their Extent-producing report checks only. */
@Suite
@SelectPackages("atf.ui.tests.practiceshop.api.component")
@IncludeTags("component")
@AtfComponentTest
public class ShopApiComponentSuite { }
