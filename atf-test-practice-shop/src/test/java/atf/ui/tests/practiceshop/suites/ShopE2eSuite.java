package atf.ui.tests.practiceshop.suites;

import atf.api.testkit.AtfE2eTest;
import atf.ui.tests.practiceshop.e2e.AuthenticatedPurchaseE2eTest;
import atf.ui.tests.practiceshop.e2e.NewAccountPurchaseE2eTest;
import atf.ui.tests.practiceshop.e2e.PurchaseE2eTest;
import atf.ui.tests.practiceshop.e2e.UiOnlyPurchaseE2eTest;
import atf.ui.tests.practiceshop.e2e.UiOnlySingleFlowPurchaseE2eTest;
import org.junit.jupiter.api.ClassOrderer;
import org.junit.jupiter.api.TestClassOrder;
import org.junit.platform.suite.api.IncludeTags;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/** All Practice Shop end-to-end purchase variants, ordered after account setup/reuse. */
@Suite
@TestClassOrder(ClassOrderer.OrderAnnotation.class)
@SelectClasses({
        NewAccountPurchaseE2eTest.class,
        AuthenticatedPurchaseE2eTest.class,
        PurchaseE2eTest.class,
        UiOnlyPurchaseE2eTest.class,
        UiOnlySingleFlowPurchaseE2eTest.class})
@IncludeTags("e2e")
@AtfE2eTest
public class ShopE2eSuite { }
