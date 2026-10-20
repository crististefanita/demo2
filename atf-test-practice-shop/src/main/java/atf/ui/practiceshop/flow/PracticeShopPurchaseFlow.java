package atf.ui.practiceshop.flow;

import atf.api.reporting.contract.AtfAssertion;
import atf.api.reporting.contract.AtfGherkinKeyword;
import atf.api.ui.playwright.AtfLocator;
import atf.api.ui.playwright.PlaywrightActionSequence;
import atf.ui.practiceshop.config.PracticeShopConfiguration;
import atf.ui.practiceshop.locators.PracticeShopControl;
import atf.ui.practiceshop.locators.PracticeShopLocators;
import atf.ui.practiceshop.pages.PracticeShopBillingPage;
import atf.ui.practiceshop.pages.PracticeShopCartPage;
import atf.ui.practiceshop.pages.PracticeShopConfirmationPage;
import atf.ui.practiceshop.pages.PracticeShopGuestCheckoutPage;
import atf.ui.practiceshop.pages.PracticeShopHomePage;
import atf.ui.practiceshop.pages.PracticeShopPaymentPage;
import atf.ui.practiceshop.pages.PracticeShopProductPage;
import atf.ui.practiceshop.support.PracticeShopPurchaseCheckpoint;
import atf.ui.practiceshop.support.PracticeShopApiNetworkJournal;
import atf.ui.practiceshop.support.PracticeShopPageValidationResult;
import atf.ui.practiceshop.support.PracticeShopPageValidator;
import atf.ui.practiceshop.support.PracticeShopHomeScreenValidator;
import atf.ui.practiceshop.support.PracticeShopPurchaseScreenValidator;
import com.microsoft.playwright.Page;

import java.util.Map;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import static atf.api.reporting.audit.ReportAuditChecks.check;

/** One complete, guest-safe purchase journey composed from page objects. */
public final class PracticeShopPurchaseFlow {
    private final PracticeShopLocators locators;
    private final PracticeShopPageValidator pageValidator;
    private final PracticeShopHomeScreenValidator homeScreenValidator;
    private final PracticeShopPurchaseScreenValidator purchaseScreenValidator;

    public PracticeShopPurchaseFlow(PracticeShopLocators locators) {
        this.locators = Objects.requireNonNull(locators, "locators");
        this.pageValidator = new PracticeShopPageValidator(locators);
        this.homeScreenValidator = new PracticeShopHomeScreenValidator(locators);
        this.purchaseScreenValidator = new PracticeShopPurchaseScreenValidator(locators);
    }

    public PurchaseResult purchaseConfiguredProduct(Page page, PlaywrightActionSequence actions,
                                                    PracticeShopConfiguration configuration) {
        return purchaseConfiguredProduct(page, actions, configuration, ignored -> { });
    }

    /**
     * Runs the same complete journey while exposing immutable page checkpoints.
     * The observer is the reporting concern; page objects and this flow remain
     * reusable for tests that need another evidence sink.
     */
    public PurchaseResult purchaseConfiguredProduct(Page page, PlaywrightActionSequence actions,
                                                    PracticeShopConfiguration configuration,
                                                    Consumer<PracticeShopPurchaseCheckpoint> checkpointObserver) {
        return execute(page, actions, configuration, checkpointObserver,
                new PracticeShopApiNetworkJournal(
                        atf.ui.practiceshop.config.PracticeShopValidationProfile.UI_ONLY), false);
    }

    /** Same journey after an authenticated setup; the sign-in checkout step is validated explicitly. */
    public PurchaseResult purchaseConfiguredProductAuthenticated(Page page, PlaywrightActionSequence actions,
                                                                  PracticeShopConfiguration configuration,
                                                                  Consumer<PracticeShopPurchaseCheckpoint> checkpointObserver,
                                                                  PracticeShopApiNetworkJournal apiNetwork) {
        return execute(page, actions, configuration, checkpointObserver, apiNetwork, true);
    }

    /** Backward-compatible API-aware guest journey used by the existing E2E proof. */
    public PurchaseResult purchaseConfiguredProduct(Page page, PlaywrightActionSequence actions,
                                                    PracticeShopConfiguration configuration,
                                                    Consumer<PracticeShopPurchaseCheckpoint> checkpointObserver,
                                                    PracticeShopApiNetworkJournal apiNetwork) {
        return execute(page, actions, configuration, checkpointObserver, apiNetwork, false);
    }

    private PurchaseResult execute(Page page, PlaywrightActionSequence actions,
                                   PracticeShopConfiguration configuration,
                                   Consumer<PracticeShopPurchaseCheckpoint> checkpointObserver,
                                   PracticeShopApiNetworkJournal apiNetwork,
                                   boolean authenticated) {
        Objects.requireNonNull(checkpointObserver, "checkpointObserver");
        Objects.requireNonNull(apiNetwork, "apiNetwork");

        PracticeShopHomePage home = new PracticeShopHomePage(page, locators, actions);
        home.open(configuration);
        assertVisible(actions, locators.homeNavigation(page), locators.homeBrand(page),
                locators.homeBanner(page));
        emit(checkpointObserver, checkpoint(AtfGherkinKeyword.GIVEN,
                "catalog home structure displays navigation, branding and banner",
                homeScreenValidator.structure(page), homeScreenValidator.structureStyle(page), List.of()));
        assertVisible(actions, locators.homeSort(page), locators.homePriceRange(page),
                locators.searchQuery(page), locators.homeSearchReset(page),
                locators.homeSearchSubmit(page), locators.homeCategoryFilters(page),
                locators.homeBrandFilters(page), locators.homeEcoFilter(page));
        emit(checkpointObserver, checkpoint(AtfGherkinKeyword.AND,
                "catalog home filters expose sorting, price, search and category controls",
                homeScreenValidator.filters(page), homeScreenValidator.filterIntegrity(page), List.of()));
        assertVisible(actions, locators.homeProductGrid(page), locators.homeProductNames(page),
                locators.homeProductImages(page), locators.homePagination(page), locators.homeFooter(page));
        emit(checkpointObserver, checkpoint(AtfGherkinKeyword.AND,
                "catalog home renders product cards, pagination and footer",
                homeScreenValidator.productGrid(page), homeScreenValidator.productGridIntegrity(page), List.of()));
        home.search(configuration.productName());
        var productLink = configuration.usesDynamicProductPath()
                ? locators.productLinkFirst(page, configuration.productName())
                : locators.productLinkByPath(page, configuration.productPath());
        boolean configuredProductPathFound = configuration.usesDynamicProductPath()
                || productLink.locator().count() > 0;
        if (!configuration.usesDynamicProductPath() && productLink.locator().count() == 0) {
            productLink = locators.productLinkFirst(page, configuration.productName());
        }
        String selectedProductPath = productLink.locator().getAttribute("href");
        actions.waitForVisible(productLink);
        actions.click(productLink, "matching product opens from search results");

        PracticeShopProductPage product = new PracticeShopProductPage(page, locators, actions);
        String productName = product.assertLoaded(configuration);
        var productApiAssertions = apiNetwork.productReady(configuration.productName(), selectedProductPath);
        emit(checkpointObserver, checkpoint(AtfGherkinKeyword.GIVEN,
                "product page displays the configured product and ready-to-buy fields",
                pageValidator.validate(page, PracticeShopProductPage.class, configuration.productName()),
                List.of(check("Product name", configuration.productName(), productName,
                                productName.contains(configuration.productName())),
                        check("Configured product route", configuration.productPathExpectation(), selectedProductPath,
                                configuredProductPathFound && configuration.matchesProductPath(selectedProductPath)),
                        check("Selected product route loaded", selectedProductPath, page.url(),
                                selectedProductPath != null && page.url().contains(selectedProductPath))),
                apiNetwork.productEvidence()).withAdditionalAssertions(productApiAssertions)
                .withAdditionalAssertions(purchaseScreenValidator.product(page)));

        product.addConfiguredQuantity(configuration);

        PracticeShopCartPage cart = new PracticeShopCartPage(page, locators, actions);
        cart.open(configuration);
        PracticeShopCartPage.CartLine line = cart.assertConfiguredLine(configuration);
        var cartApiAssertions = apiNetwork.cartReady(configuration.checkout().quantity());
        emit(checkpointObserver, checkpoint(AtfGherkinKeyword.WHEN,
                "cart displays the configured product with the requested quantity",
                pageValidator.validateValues(page, PracticeShopCartPage.class,
                        Map.of(PracticeShopControl.CART_PRODUCT_QUANTITY,
                                Integer.toString(configuration.checkout().quantity()))),
                List.of(
                        check("Cart product", configuration.productName(), line.productName(),
                                line.productName().contains(configuration.productName())),
                        check("Cart quantity", Integer.toString(configuration.checkout().quantity()), line.quantity(),
                                Integer.toString(configuration.checkout().quantity()).equals(line.quantity()))),
                apiNetwork.cartEvidence()).withAdditionalAssertions(cartApiAssertions)
                .withAdditionalAssertions(purchaseScreenValidator.cart(page)));
        cart.proceed();

        if (authenticated) {
            actions.waitForText(page, locators.pageBody(page), "already logged in",
                    "authenticated checkout setup is displayed");
            emit(checkpointObserver, checkpoint(AtfGherkinKeyword.AND,
                    "authenticated checkout recognizes the signed-in customer",
                    new PracticeShopPageValidationResult(atf.ui.practiceshop.config.PracticeShopScreen.GUEST_CHECKOUT,
                            List.of(new AtfAssertion("Checkout identity", "signed-in customer",
                                    "already logged in", true))),
                    List.of(new AtfAssertion("Guest checkout bypass", "guest identity is not requested",
                            "authenticated checkout continues directly to billing", true)), List.of()));
            actions.click(locators.authenticatedProceed(page), "authenticated customer proceeds to billing");
        } else {
            PracticeShopGuestCheckoutPage guest = new PracticeShopGuestCheckoutPage(page, locators, actions);
            var guestIdentity = configuration.checkout().guest();
            guest.fillIdentity(guestIdentity);
            emit(checkpointObserver, checkpoint(AtfGherkinKeyword.AND,
                    "guest checkout form contains the configured identity",
                    pageValidator.validateValues(page, PracticeShopGuestCheckoutPage.class, Map.of(
                            PracticeShopControl.GUEST_EMAIL, guestIdentity.email(),
                            PracticeShopControl.GUEST_FIRST_NAME, guestIdentity.firstName(),
                            PracticeShopControl.GUEST_LAST_NAME, guestIdentity.lastName())),
                    List.of(new AtfAssertion("Guest form", "ready for submission",
                            "guest identity fields are populated", true)), List.of())
                    .withAdditionalAssertions(purchaseScreenValidator.guestCheckout(page)));
            guest.submitIdentity();
            guest.proceedToBilling();
        }

        PracticeShopBillingPage billing = new PracticeShopBillingPage(page, locators, actions);
        billing.fill(configuration.checkout().billing());
        var address = configuration.checkout().billing();
        emit(checkpointObserver, checkpoint(AtfGherkinKeyword.AND,
                "billing form is completed with the configured address",
                pageValidator.validateValues(page, PracticeShopBillingPage.class, Map.of(
                        PracticeShopControl.POSTAL_CODE, address.postalCode(),
                        PracticeShopControl.HOUSE_NUMBER, address.houseNumber(),
                        PracticeShopControl.STREET, address.street(),
                        PracticeShopControl.CITY, address.city(),
                        PracticeShopControl.STATE, address.state())),
                List.of(new AtfAssertion("Billing form", "country and address fields completed",
                        "billing address is ready for payment", true)), List.of())
                .withAdditionalAssertions(purchaseScreenValidator.billing(page)));
        billing.proceedToPayment();

        PracticeShopPaymentPage payment = new PracticeShopPaymentPage(page, locators, actions);
        payment.chooseMethod(configuration.checkout().paymentMethod());
        emit(checkpointObserver, checkpoint(AtfGherkinKeyword.AND,
                "payment form displays the configured demo method",
                pageValidator.validate(page, PracticeShopPaymentPage.class),
                List.of(check("Payment method", configuration.checkout().paymentMethod(),
                        configuration.checkout().paymentMethod(), true)), List.of())
                .withAdditionalAssertions(purchaseScreenValidator.payment(page)));
        String confirmation = payment.confirm();
        var paymentApiAssertions = apiNetwork.paymentConfirmed(configuration.checkout().paymentMethod());
        emit(checkpointObserver, checkpoint(AtfGherkinKeyword.THEN,
                "payment confirmation is displayed for the completed purchase",
                pageValidator.validate(page, PracticeShopConfirmationPage.class),
                List.of(check("Payment confirmation", "Payment was successful", confirmation,
                        confirmation.contains("Payment was successful"))),
                apiNetwork.paymentEvidence()).withAdditionalAssertions(paymentApiAssertions)
                .withAdditionalAssertions(purchaseScreenValidator.confirmation(page)));
        return new PurchaseResult(line.productName(), line.quantity(),
                configuration.checkout().paymentMethod(), confirmation);
    }

    private PracticeShopPurchaseCheckpoint checkpoint(AtfGherkinKeyword keyword, String title,
                                                      PracticeShopPageValidationResult validation,
                                                      List<AtfAssertion> additional,
                                                      List<atf.api.reporting.model.ReportExecutionEvidence> api) {
        return new PracticeShopPurchaseCheckpoint(keyword, title, validation.assertions())
                .withAdditionalAssertions(additional)
                .withApiEvidence(api);
    }

    private static void emit(Consumer<PracticeShopPurchaseCheckpoint> observer,
                             PracticeShopPurchaseCheckpoint checkpoint) {
        observer.accept(checkpoint);
        if (!checkpoint.passed()) {
            throw new IllegalStateException("Page validation failed: " + checkpoint.title());
        }
    }

    private static void assertVisible(PlaywrightActionSequence actions, AtfLocator... controls) {
        for (AtfLocator control : controls) {
            actions.assertVisible(new AtfLocator(control.locator().first(), control.selector()));
        }
    }

    public record PurchaseResult(String productName, String quantity,
                                 String paymentMethod, String confirmation) {
    }
}
