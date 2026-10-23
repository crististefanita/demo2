package atf.ui.tests.autoex.suites;

import atf.api.testkit.AtfE2eTest;
import atf.ui.tests.autoex.e2e.AutoExCartUiE2eTest;
import atf.ui.tests.autoex.e2e.AutoExPurchaseUiE2eTest;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.platform.suite.api.IncludeTags;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/** Three distinct UI business stories; evidence-format variants remain in the explicit purchase suite. */
@Suite
@SelectClasses({AutoExPurchaseUiE2eTest.class, AutoExCartUiE2eTest.class})
@IncludeTags("e2e")
@AtfE2eTest
@Execution(ExecutionMode.SAME_THREAD)
public class AutoExBusinessSuite { }
