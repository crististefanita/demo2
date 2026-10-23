package atf.ui.tests.practiceshop.suites;

import atf.api.testkit.AtfE2eTest;
import atf.ui.tests.practiceshop.e2e.ShopAuthenticatedPurchaseE2eTest;
import atf.ui.tests.practiceshop.e2e.ShopNewAccountPurchaseE2eTest;
import atf.ui.tests.practiceshop.e2e.ShopPurchaseE2eTest;
import atf.ui.tests.practiceshop.e2e.ShopUiOnlyPurchaseE2eTest;
import atf.ui.tests.practiceshop.e2e.ShopUiOnlySingleFlowPurchaseE2eTest;
import org.junit.jupiter.api.ClassOrderer;
import org.junit.jupiter.api.TestClassOrder;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/** All Practice Shop end-to-end purchase variants, ordered after account setup/reuse. */
@Suite
@TestClassOrder(ClassOrderer.OrderAnnotation.class)
@SelectClasses({
        ShopNewAccountPurchaseE2eTest.class,
        ShopAuthenticatedPurchaseE2eTest.class,
        ShopPurchaseE2eTest.class,
        ShopUiOnlyPurchaseE2eTest.class,
        ShopUiOnlySingleFlowPurchaseE2eTest.class})
@AtfE2eTest
public class ShopE2eSuite { }
