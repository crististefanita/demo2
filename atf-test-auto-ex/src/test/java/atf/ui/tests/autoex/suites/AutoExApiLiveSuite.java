package atf.ui.tests.autoex.suites;

import atf.api.testkit.AtfIntegrationTest;
import atf.ui.tests.autoex.api.AutoExApiLiveContractTest;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/** Separate live API gate; the selected test remains disabled unless explicitly opted in. */
@Suite
@SelectClasses(AutoExApiLiveContractTest.class)
@AtfIntegrationTest
public class AutoExApiLiveSuite { }
