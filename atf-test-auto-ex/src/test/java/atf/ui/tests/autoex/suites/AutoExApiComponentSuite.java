package atf.ui.tests.autoex.suites;

import atf.api.testkit.AtfComponentTest;
import org.junit.platform.suite.api.IncludeTags;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

/** Offline API consumer contracts and adapters; no browser or external service is opened. */
@Suite
@SelectPackages("atf.ui.tests.autoex.component.api")
@IncludeTags("component")
@AtfComponentTest
public class AutoExApiComponentSuite { }
