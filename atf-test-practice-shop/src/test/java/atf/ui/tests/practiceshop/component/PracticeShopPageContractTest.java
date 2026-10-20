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
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@AtfComponentTest
class PracticeShopPageContractTest {
    @Test
    void purchasePagesDeclareStableRoutesAndSemanticControls() {
        assertContract(PracticeShopHomePage.class, PracticeShopScreen.HOME, "/", 16);
        assertContract(PracticeShopProductPage.class, PracticeShopScreen.PRODUCT, "/product/*", 5);
        assertContract(PracticeShopCartPage.class, PracticeShopScreen.CART, "/checkout", 3);
        assertContract(PracticeShopGuestCheckoutPage.class, PracticeShopScreen.GUEST_CHECKOUT, "/checkout", 4);
        assertContract(PracticeShopBillingPage.class, PracticeShopScreen.BILLING, "/checkout", 7);
        assertContract(PracticeShopPaymentPage.class, PracticeShopScreen.PAYMENT, "/checkout", 2);
        assertContract(PracticeShopConfirmationPage.class, PracticeShopScreen.CONFIRMATION, "/checkout", 1);
        assertContract(PracticeShopLoginPage.class, PracticeShopScreen.LOGIN, "/auth/login", 3);
        assertContract(PracticeShopAccountPage.class, PracticeShopScreen.ACCOUNT, "/account", 1);
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
                new atf.api.reporting.contract.AtfAssertion("route", "/product/*", "/cart", false),
                new atf.api.reporting.contract.AtfAssertion("name", "Pliers", "Pliers", true)));

        assertFalse(result.passed());
        assertEquals("1/2 validations passed for PRODUCT", result.summary());
        try {
            result.assertions().clear();
            throw new AssertionError("validation assertions must be immutable");
        } catch (UnsupportedOperationException expected) {
            // expected
        }
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
