package atf.ui.tests.autoex.suites;

import atf.api.testkit.AtfComponentTest;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

/** Offline consumer component contracts; never opens the public Automation Exercise site. */
@Suite
@SelectPackages("atf.ui.tests.autoex.component")
@AtfComponentTest
public class AutoExComponentSuite { }
