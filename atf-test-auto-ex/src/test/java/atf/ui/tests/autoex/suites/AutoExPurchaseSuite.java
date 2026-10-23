package atf.ui.tests.autoex.suites;

import atf.api.testkit.AtfE2eTest;
import atf.ui.tests.autoex.e2e.AutoExPurchaseUiApiE2eTest;
import atf.ui.tests.autoex.e2e.AutoExPurchaseUiE2eTest;
import atf.ui.tests.autoex.e2e.AutoExPurchaseUiFinalScreenshotE2eTest;
import org.junit.jupiter.api.ClassOrderer;
import org.junit.jupiter.api.TestClassOrder;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.platform.suite.api.IncludeTags;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/** Three evidence configurations of the same business purchase; one shared Extent report. */
@Suite
@TestClassOrder(ClassOrderer.OrderAnnotation.class)
@SelectClasses({
        AutoExPurchaseUiE2eTest.class,
        AutoExPurchaseUiApiE2eTest.class,
        AutoExPurchaseUiFinalScreenshotE2eTest.class})
@IncludeTags("e2e")
@AtfE2eTest
@Execution(ExecutionMode.SAME_THREAD)
public class AutoExPurchaseSuite { }
