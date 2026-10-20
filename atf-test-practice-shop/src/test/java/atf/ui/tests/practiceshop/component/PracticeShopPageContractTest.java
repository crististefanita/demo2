package atf.ui.tests.practiceshop.component;

import atf.api.testkit.AtfComponentTest;
import atf.ui.practiceshop.config.PracticeShopScreen;
import atf.ui.practiceshop.locators.PracticeShopControl;
import atf.ui.practiceshop.pages.PracticeShopBillingPage;
import atf.ui.practiceshop.pages.PracticeShopCartPage;
import atf.ui.practiceshop.pages.PracticeShopConfirmationPage;
import atf.ui.practiceshop.pages.PracticeShopGuestCheckoutPage;
import atf.ui.practiceshop.pages.PracticeShopHomePage;
import atf.ui.practiceshop.pages.PracticeShopLoginPage;
import atf.ui.practiceshop.pages.PracticeShopAccountPage;
import atf.ui.practiceshop.pages.PracticeShopPaymentPage;
import atf.ui.practiceshop.pages.PracticeShopProductPage;
import atf.ui.practiceshop.support.PracticeShopPageContract;
import atf.ui.practiceshop.support.PracticeShopPageValidationResult;
import atf.ui.practiceshop.routes.PracticeShopUiRoutes;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@AtfComponentTest
class PracticeShopPageContractTest {
    @Test
    void purchasePagesDeclareStableRoutesAndSemanticControls() {
        assertContract(PracticeShopHomePage.class, PracticeShopScreen.HOME, PracticeShopUiRoutes.HOME, 16);
        assertContract(PracticeShopProductPage.class, PracticeShopScreen.PRODUCT,
                PracticeShopUiRoutes.PRODUCT_PATTERN, 5);
        assertContract(PracticeShopCartPage.class, PracticeShopScreen.CART, PracticeShopUiRoutes.CHECKOUT, 3);
        assertContract(PracticeShopGuestCheckoutPage.class, PracticeShopScreen.GUEST_CHECKOUT,
                PracticeShopUiRoutes.CHECKOUT, 4);
        assertContract(PracticeShopBillingPage.class, PracticeShopScreen.BILLING,
                PracticeShopUiRoutes.CHECKOUT, 7);
        assertContract(PracticeShopPaymentPage.class, PracticeShopScreen.PAYMENT,
                PracticeShopUiRoutes.CHECKOUT, 2);
        assertContract(PracticeShopConfirmationPage.class, PracticeShopScreen.CONFIRMATION,
                PracticeShopUiRoutes.CHECKOUT, 1);
        assertContract(PracticeShopLoginPage.class, PracticeShopScreen.LOGIN,
                PracticeShopUiRoutes.AUTH_LOGIN, 3);
        assertContract(PracticeShopAccountPage.class, PracticeShopScreen.ACCOUNT,
                PracticeShopUiRoutes.ACCOUNT, 1);
    }

    @Test
    void homeContractExposesDistinctControlsForStructureFiltersAndProductGrid() {
        var contract = PracticeShopHomePage.class.getAnnotation(PracticeShopPageContract.class);
        List<PracticeShopControl> controls = List.of(contract.requiredControls());

        assertEquals(16, controls.stream().distinct().count());
        assertTrue(controls.containsAll(List.of(
                PracticeShopControl.HOME_NAVIGATION,
                PracticeShopControl.HOME_BRAND,
                PracticeShopControl.HOME_BANNER,
                PracticeShopControl.HOME_SORT,
                PracticeShopControl.HOME_PRICE_RANGE,
                PracticeShopControl.SEARCH_QUERY,
                PracticeShopControl.HOME_PRODUCT_GRID,
                PracticeShopControl.HOME_PRODUCT_NAMES,
                PracticeShopControl.HOME_PRODUCT_IMAGES,
                PracticeShopControl.HOME_PAGINATION,
                PracticeShopControl.HOME_FOOTER)));
    }

    @Test
    void immutableValidationResultAggregatesAllFailures() {
        var result = new PracticeShopPageValidationResult(PracticeShopScreen.PRODUCT, List.of(
                new atf.api.reporting.contract.AtfAssertion("route", PracticeShopUiRoutes.PRODUCT_PATTERN,
                        "/cart", false),
                new atf.api.reporting.contract.AtfAssertion("name", "Pliers", "Pliers", true)));

        assertFalse(result.passed());
        assertEquals("1/2 validations passed for PRODUCT", result.summary());
        assertThrows(UnsupportedOperationException.class, () -> result.assertions().clear());
    }

    private static void assertContract(Class<?> pageType, PracticeShopScreen screen,
                                       String route, int controlCount) {
        PracticeShopPageContract contract = pageType.getAnnotation(PracticeShopPageContract.class);
        assertTrue(contract != null, pageType.getSimpleName() + " must be contract annotated");
        assertEquals(screen, contract.screen());
        assertEquals(route, contract.route());
        assertEquals(controlCount, contract.requiredControls().length);
        for (PracticeShopControl control : contract.requiredControls()) {
            assertTrue(control.label().length() > 3);
        }
    }
}
