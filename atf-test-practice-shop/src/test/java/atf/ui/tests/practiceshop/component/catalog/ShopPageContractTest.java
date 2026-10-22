package atf.ui.tests.practiceshop.component.catalog;

import atf.api.testkit.AtfComponentTest;
import atf.ui.practiceshop.config.ShopScreen;
import atf.ui.practiceshop.locators.ShopControl;
import atf.ui.practiceshop.pages.ShopBillingPage;
import atf.ui.practiceshop.pages.ShopCartPage;
import atf.ui.practiceshop.pages.ShopConfirmationPage;
import atf.ui.practiceshop.pages.ShopGuestCheckoutPage;
import atf.ui.practiceshop.pages.ShopHomePage;
import atf.ui.practiceshop.pages.ShopLoginPage;
import atf.ui.practiceshop.pages.ShopAccountPage;
import atf.ui.practiceshop.pages.ShopPaymentPage;
import atf.ui.practiceshop.pages.ShopProductPage;
import atf.ui.practiceshop.support.ShopPageContract;
import atf.ui.practiceshop.support.ShopPageValidationResult;
import atf.ui.practiceshop.routes.ShopUiRoutes;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@AtfComponentTest
class ShopPageContractTest {
    @Test
    void purchasePagesDeclareStableRoutesAndSemanticControls() {
        assertContract(ShopHomePage.class, ShopScreen.HOME, ShopUiRoutes.HOME, 16);
        assertContract(ShopProductPage.class, ShopScreen.PRODUCT,
                ShopUiRoutes.PRODUCT_PATTERN, 5);
        assertContract(ShopCartPage.class, ShopScreen.CART, ShopUiRoutes.CHECKOUT, 3);
        assertContract(ShopGuestCheckoutPage.class, ShopScreen.GUEST_CHECKOUT,
                ShopUiRoutes.CHECKOUT, 4);
        assertContract(ShopBillingPage.class, ShopScreen.BILLING,
                ShopUiRoutes.CHECKOUT, 7);
        assertContract(ShopPaymentPage.class, ShopScreen.PAYMENT,
                ShopUiRoutes.CHECKOUT, 2);
        assertContract(ShopConfirmationPage.class, ShopScreen.CONFIRMATION,
                ShopUiRoutes.CHECKOUT, 1);
        assertContract(ShopLoginPage.class, ShopScreen.LOGIN,
                ShopUiRoutes.AUTH_LOGIN, 3);
        assertContract(ShopAccountPage.class, ShopScreen.ACCOUNT,
                ShopUiRoutes.ACCOUNT, 1);
    }

    @Test
    void homeContractExposesDistinctControlsForStructureFiltersAndProductGrid() {
        var contract = ShopHomePage.class.getAnnotation(ShopPageContract.class);
        List<ShopControl> controls = List.of(contract.requiredControls());

        assertEquals(16, controls.stream().distinct().count());
        assertTrue(controls.containsAll(List.of(
                ShopControl.HOME_NAVIGATION,
                ShopControl.HOME_BRAND,
                ShopControl.HOME_BANNER,
                ShopControl.HOME_SORT,
                ShopControl.HOME_PRICE_RANGE,
                ShopControl.SEARCH_QUERY,
                ShopControl.HOME_PRODUCT_GRID,
                ShopControl.HOME_PRODUCT_NAMES,
                ShopControl.HOME_PRODUCT_IMAGES,
                ShopControl.HOME_PAGINATION,
                ShopControl.HOME_FOOTER)));
    }

    @Test
    void immutableValidationResultAggregatesAllFailures() {
        var result = new ShopPageValidationResult(ShopScreen.PRODUCT, List.of(
                new atf.api.reporting.contract.AtfAssertion("route", ShopUiRoutes.PRODUCT_PATTERN,
                        "/cart", false),
                new atf.api.reporting.contract.AtfAssertion("name", "Pliers", "Pliers", true)));

        assertFalse(result.passed());
        assertEquals("1/2 validations passed for PRODUCT", result.summary());
        assertThrows(UnsupportedOperationException.class, () -> result.assertions().clear());
    }

    private static void assertContract(Class<?> pageType, ShopScreen screen,
                                       String route, int controlCount) {
        ShopPageContract contract = pageType.getAnnotation(ShopPageContract.class);
        assertTrue(contract != null, pageType.getSimpleName() + " must be contract annotated");
        assertEquals(screen, contract.screen());
        assertEquals(route, contract.route());
        assertEquals(controlCount, contract.requiredControls().length);
        for (ShopControl control : contract.requiredControls()) {
            assertTrue(control.label().length() > 3);
        }
    }
}
