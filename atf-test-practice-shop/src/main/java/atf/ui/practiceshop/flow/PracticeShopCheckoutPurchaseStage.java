package atf.ui.practiceshop.flow;

import atf.api.reporting.contract.AtfGherkinKeyword;
import atf.ui.practiceshop.config.PracticeShopScreen;
import atf.ui.practiceshop.locators.PracticeShopControl;
import atf.ui.practiceshop.pages.PracticeShopBillingPage;
import atf.ui.practiceshop.pages.PracticeShopCartPage;
import atf.ui.practiceshop.pages.PracticeShopConfirmationPage;
import atf.ui.practiceshop.pages.PracticeShopGuestCheckoutPage;
import atf.ui.practiceshop.pages.PracticeShopPaymentPage;
import atf.ui.practiceshop.support.PracticeShopPageValidationResult;
import atf.ui.practiceshop.config.PracticeShopApiCheckpoint;

import java.util.List;
import java.util.Map;

import static atf.api.reporting.audit.ReportAuditChecks.check;

/** Checkout-side business stage: cart, identity, billing, payment and confirmation. */
final class PracticeShopCheckoutPurchaseStage {
    PracticeShopPurchaseFlow.PurchaseResult complete(PracticeShopPurchaseJourneyContext context,
                                                      boolean authenticated) {
        var page = context.page();
        var actions = context.actions();
        var locators = context.locators();
        var configuration = context.configuration();

        PracticeShopCartPage cart = new PracticeShopCartPage(page, locators, actions);
        cart.open(configuration);
        PracticeShopCartPage.CartLine line = cart.assertConfiguredLine(configuration);
        var cartApiAssertions = context.apiNetwork().cartReady(configuration.checkout().quantity());
        context.publish(context.checkpoint(AtfGherkinKeyword.WHEN,
                "cart displays the configured product with the requested quantity",
                context.pageValidator().validateValues(page, PracticeShopCartPage.class,
                        Map.of(PracticeShopControl.CART_PRODUCT_QUANTITY,
                                Integer.toString(configuration.checkout().quantity()))),
                List.of(
                        check("Cart product", configuration.productName(), line.productName(),
                                line.productName().contains(configuration.productName())),
                        check("Cart quantity", Integer.toString(configuration.checkout().quantity()), line.quantity(),
                                Integer.toString(configuration.checkout().quantity()).equals(line.quantity()))),
                context.apiNetwork().evidence(PracticeShopApiCheckpoint.CART))
                .withAdditionalAssertions(cartApiAssertions)
                .withAdditionalAssertions(context.purchaseScreenValidator().cart(page)));
        cart.proceed();

        if (authenticated) {
            actions.waitForVisible(locators.authenticatedProceed(page));
            context.publish(context.checkpoint(AtfGherkinKeyword.AND,
                    "authenticated checkout exposes the signed-in proceed action",
                    new PracticeShopPageValidationResult(
                            PracticeShopScreen.AUTHENTICATED_CHECKOUT,
                            context.purchaseScreenValidator().authenticatedCheckout(page)),
                    List.of(), List.of()));
            actions.click(locators.authenticatedProceed(page), "authenticated customer proceeds to billing");
        } else {
            PracticeShopGuestCheckoutPage guest = new PracticeShopGuestCheckoutPage(page, locators, actions);
            var guestIdentity = configuration.checkout().guest();
            guest.fillIdentity(guestIdentity);
            context.publish(context.checkpoint(AtfGherkinKeyword.AND,
                    "guest checkout form contains the configured identity",
                    context.pageValidator().validateValues(page, PracticeShopGuestCheckoutPage.class, Map.of(
                            PracticeShopControl.GUEST_EMAIL, guestIdentity.email(),
                            PracticeShopControl.GUEST_FIRST_NAME, guestIdentity.firstName(),
                            PracticeShopControl.GUEST_LAST_NAME, guestIdentity.lastName())),
                    List.of(), List.of())
                    .withAdditionalAssertions(context.purchaseScreenValidator().guestCheckout(page)));
            guest.submitIdentity();
            guest.proceedToBilling();
        }

        PracticeShopBillingPage billing = new PracticeShopBillingPage(page, locators, actions);
        billing.fill(configuration.checkout().billing());
        var address = configuration.checkout().billing();
        context.publish(context.checkpoint(AtfGherkinKeyword.AND,
                "billing form is completed with the configured address",
                context.pageValidator().validateValues(page, PracticeShopBillingPage.class, Map.of(
                        PracticeShopControl.POSTAL_CODE, address.postalCode(),
                        PracticeShopControl.HOUSE_NUMBER, address.houseNumber(),
                        PracticeShopControl.STREET, address.street(),
                        PracticeShopControl.CITY, address.city(),
                        PracticeShopControl.STATE, address.state())),
                List.of(), List.of())
                .withAdditionalAssertions(context.purchaseScreenValidator().billing(page)));
        billing.proceedToPayment();

        PracticeShopPaymentPage payment = new PracticeShopPaymentPage(page, locators, actions);
        String observedPaymentMethod = payment.chooseMethod(configuration.checkout().paymentMethod());
        context.publish(context.checkpoint(AtfGherkinKeyword.AND,
                "payment form displays the configured demo method",
                context.pageValidator().validate(page, PracticeShopPaymentPage.class),
                List.of(check("Payment method", configuration.checkout().paymentMethod(),
                        observedPaymentMethod,
                        configuration.checkout().paymentMethod().equals(observedPaymentMethod))), List.of())
                .withAdditionalAssertions(context.purchaseScreenValidator().payment(page)));
        String confirmation = payment.confirm();
        var paymentApiAssertions = context.apiNetwork().paymentConfirmed(
                configuration.checkout().paymentMethod());
        context.publish(context.checkpoint(AtfGherkinKeyword.THEN,
                "payment confirmation is displayed for the completed purchase",
                context.pageValidator().validate(page, PracticeShopConfirmationPage.class),
                List.of(check("Payment confirmation", "Payment was successful", confirmation,
                        confirmation.contains("Payment was successful"))),
                context.apiNetwork().evidence(PracticeShopApiCheckpoint.PAYMENT))
                .withAdditionalAssertions(paymentApiAssertions)
                .withAdditionalAssertions(context.purchaseScreenValidator().confirmation(page)));
        return new PracticeShopPurchaseFlow.PurchaseResult(line.productName(), line.quantity(),
                configuration.checkout().paymentMethod(), confirmation);
    }
}
