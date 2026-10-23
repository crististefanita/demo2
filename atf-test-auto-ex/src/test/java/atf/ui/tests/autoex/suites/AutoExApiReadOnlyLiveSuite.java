package atf.ui.tests.autoex.suites;

import atf.api.testkit.AtfIntegrationTest;
import atf.ui.tests.autoex.api.AutoExApiReadOnlyLiveContractTest;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/** Safe live sample: GET and search-only POST requests; never creates or mutates accounts. */
@Suite
@SelectClasses(AutoExApiReadOnlyLiveContractTest.class)
@AtfIntegrationTest
public class AutoExApiReadOnlyLiveSuite { }
