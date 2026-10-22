package atf.ui.practiceshop.flow;

import atf.ui.practiceshop.config.PracticeShopScreen;
import atf.ui.practiceshop.locators.PracticeShopControl;
import atf.ui.practiceshop.pages.PracticeShopBillingPage;
import atf.ui.practiceshop.pages.PracticeShopCartPage;
import atf.ui.practiceshop.pages.PracticeShopConfirmationPage;
import atf.ui.practiceshop.pages.PracticeShopGuestCheckoutPage;
import atf.ui.practiceshop.pages.PracticeShopPaymentPage;
import atf.ui.practiceshop.support.PracticeShopPageValidationResult;
import atf.ui.practiceshop.config.PracticeShopApiCheckpoint;
import atf.ui.practiceshop.support.PracticeShopPurchaseTraceStep;

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
        context.publish(context.checkpoint(PracticeShopPurchaseTraceStep.CART,
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
            context.publish(context.checkpoint(PracticeShopPurchaseTraceStep.AUTHENTICATED_CHECKOUT,
                    new PracticeShopPageValidationResult(
                            PracticeShopScreen.AUTHENTICATED_CHECKOUT,
                            context.purchaseScreenValidator().authenticatedCheckout(page)),
                    List.of(), List.of()));
            actions.click(locators.authenticatedProceed(page), "authenticated customer proceeds to billing");
        } else {
            PracticeShopGuestCheckoutPage guest = new PracticeShopGuestCheckoutPage(page, locators, actions);
            var guestIdentity = configuration.checkout().guest();
            guest.fillIdentity(guestIdentity);
            context.publish(context.checkpoint(PracticeShopPurchaseTraceStep.GUEST_CHECKOUT,
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
        context.publish(context.checkpoint(PracticeShopPurchaseTraceStep.BILLING,
                context.pageValidator().validateValues(page, PracticeShopBillingPage.class, Map.of(
                        PracticeShopControl.COUNTRY, address.country(),
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
        context.publish(context.checkpoint(PracticeShopPurchaseTraceStep.PAYMENT_METHOD,
                context.pageValidator().validate(page, PracticeShopPaymentPage.class),
                List.of(check("Payment method", configuration.checkout().paymentMethod(),
                        observedPaymentMethod,
                        configuration.checkout().paymentMethod().equals(observedPaymentMethod))), List.of())
                .withAdditionalAssertions(context.purchaseScreenValidator().payment(page)));
        String paymentConfirmation = payment.checkPayment();
        var paymentApiAssertions = context.apiNetwork().paymentConfirmed(
                configuration.checkout().paymentMethod());
        context.publish(context.checkpoint(PracticeShopPurchaseTraceStep.PAYMENT_CHECK,
                new PracticeShopPageValidationResult(PracticeShopScreen.PAYMENT,
                        List.of(context.pageValidator().validateVisible(page, PracticeShopControl.PAYMENT_SUCCESS))),
                List.of(check("Payment check", "Payment was successful", paymentConfirmation,
                        paymentConfirmation.contains("Payment was successful"))),
                context.apiNetwork().evidence(PracticeShopApiCheckpoint.PAYMENT))
                .withAdditionalAssertions(paymentApiAssertions)
                .withAdditionalAssertions(context.purchaseScreenValidator().paymentChecked(page)));

        PracticeShopConfirmationPage orderConfirmationPage =
                new PracticeShopConfirmationPage(page, locators, actions);
        long orderSequenceFence = context.apiNetwork().sequenceMarker();
        String confirmation = orderConfirmationPage.confirmOrder();
        var orderApiAssertions = context.apiNetwork().orderCreated(orderSequenceFence, confirmation);
        context.publish(context.checkpoint(PracticeShopPurchaseTraceStep.ORDER_CONFIRMATION,
                context.pageValidator().validate(page, PracticeShopConfirmationPage.class),
                List.of(check("Order confirmation", "Thanks for your order! and invoice number",
                        confirmation, confirmation.contains("Thanks for your order!")
                                && confirmation.contains("INV-"))),
                context.apiNetwork().evidence(PracticeShopApiCheckpoint.ORDER))
                .withAdditionalAssertions(orderApiAssertions)
                .withAdditionalAssertions(context.purchaseScreenValidator().orderConfirmation(page)));
        return new PracticeShopPurchaseFlow.PurchaseResult(line.productName(), line.quantity(),
                configuration.checkout().paymentMethod(), confirmation);
    }
}
