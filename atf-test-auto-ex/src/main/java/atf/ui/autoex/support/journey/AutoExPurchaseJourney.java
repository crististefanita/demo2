package atf.ui.autoex.support.journey;

import atf.api.reporting.contract.AtfAssertion;
import atf.ui.autoex.config.AutoExConfiguration;
import atf.ui.autoex.support.account.AutoExTestAccount;
import atf.ui.autoex.support.catalog.AutoExCatalogAssertions;
import atf.ui.autoex.page.AccountPage;
import atf.ui.autoex.page.CartPage;
import atf.ui.autoex.page.CheckoutPage;
import atf.ui.autoex.page.HomePage;
import atf.ui.autoex.page.ProductPage;
import atf.ui.autoex.page.ProductsPage;

import java.util.List;

/** One end-to-end business journey on the actual public store. */
public final class AutoExPurchaseJourney {
    private AutoExPurchaseJourney() { }

    public static void execute(AutoExPurchaseSession run) {
        AutoExConfiguration configuration = run.configuration();
        AutoExConfiguration.Catalog product = configuration.catalog();
        ProductsPage products = searchConfiguredProduct(run);

        ProductPage details = products.openFirstProduct();
        String detailText = details.details();
        run.checkpoint(AutoExPurchaseTraceStep.PRODUCT_DETAILS,
                List.of(
                        new AtfAssertion("Product name", product.productName(), detailText,
                                detailText.contains(product.productName())),
                        new AtfAssertion("Product price", product.unitPrice(), detailText,
                                AutoExCatalogAssertions.containsExactLine(
                                        detailText, product.unitPrice())),
                        new AtfAssertion("Availability", "In Stock", detailText,
                                detailText.contains("In Stock"))));

        details.addQuantityToCart(product.quantity());
        String added = details.addConfirmation();
        run.checkpoint(AutoExPurchaseTraceStep.CART_ADD,
                List.of(new AtfAssertion("Add-to-cart confirmation", "Added!", added,
                        added.contains("Added!") && added.contains("added to cart"))));

        CartPage cart = details.viewCartFromConfirmation();
        String cartName = cart.productName(product);
        String cartPrice = cart.productPrice(product);
        String cartQuantity = cart.quantity(product);
        String cartTotal = cart.total(product);
        run.checkpoint(AutoExPurchaseTraceStep.CART_VERIFIED,
                List.of(
                        new AtfAssertion("Cart product", product.productName(), cartName,
                                product.productName().equals(cartName)),
                        new AtfAssertion("Cart unit price", product.unitPrice(), cartPrice,
                                product.unitPrice().equals(cartPrice)),
                        new AtfAssertion("Cart quantity", Integer.toString(product.quantity()), cartQuantity,
                                Integer.toString(product.quantity()).equals(cartQuantity)),
                        new AtfAssertion("Cart line total", configuration.expectedCartTotal(), cartTotal,
                                configuration.expectedCartTotal().equals(cartTotal))));

        cart.proceedToCheckout();
        AutoExTestAccount account = AutoExTestAccount.create(configuration.registration());
        new AccountPage(run).registerFromCheckout(account);

        cart = new CartPage(run);
        cart.openFromHeader();
        cart.proceedToCheckout();
        AccountPage accountPage = new AccountPage(run);
        String deliveryAddress = accountPage.deliveryAddress();
        String billingAddress = accountPage.billingAddress();
        String orderReview = new CheckoutPage(run).orderReview();
        run.checkpoint(AutoExPurchaseTraceStep.CHECKOUT_REVIEW,
                List.of(
                        new AtfAssertion("Delivery address", configuration.registration().displayName(),
                                deliveryAddress, deliveryAddress.contains(configuration.registration().displayName())),
                        new AtfAssertion("Billing address", configuration.registration().displayName(),
                                billingAddress, billingAddress.contains(configuration.registration().displayName())),
                        new AtfAssertion("Order review", product.productName(), orderReview,
                                orderReview.contains(product.productName()))));

        CheckoutPage checkout = new CheckoutPage(run);
        String paymentHeading = checkout.placeOrder("Synthetic ATF E2E order; demo environment only.");
        run.checkpoint(AutoExPurchaseTraceStep.PAYMENT_FORM,
                List.of(new AtfAssertion("Payment page", "Payment", paymentHeading,
                        "Payment".equals(paymentHeading))));
        String confirmation = checkout.payWithDemoCard(configuration.payment());
        String confirmationMessage = checkout.orderConfirmationMessage();
        run.checkpoint(AutoExPurchaseTraceStep.ORDER_CONFIRMED,
                List.of(
                        new AtfAssertion("Order status", "Order Placed! (case-insensitive)", confirmation,
                                "Order Placed!".equalsIgnoreCase(confirmation)),
                        new AtfAssertion("Order confirmation", "Congratulations! Your order has been confirmed!",
                                confirmationMessage,
                                confirmationMessage.contains("Your order has been confirmed!"))));

        checkout.downloadInvoice();
        checkout.continueAfterInvoice();
        String deletion = accountPage.accountDeletionResult();
        String landingUrl = run.currentUrl();
        String expectedLandingUrl = configuration.baseUrl().toString().replaceAll("/$", "") + "/";
        List<AtfAssertion> finalAssertions = new java.util.ArrayList<>(List.of(
                new AtfAssertion("Post-cleanup landing page", expectedLandingUrl,
                        landingUrl, expectedLandingUrl.equals(landingUrl)),
                new AtfAssertion("Account cleanup remains confirmed", "ACCOUNT DELETED!",
                        deletion, deletion.contains("ACCOUNT DELETED!"))));
        if (run.observesApiTraffic()) {
            int exchanges = run.apiExchangeCount();
            finalAssertions.add(new AtfAssertion("Observed first-party API exchanges",
                    "> 0 API requests caused by this browser journey", Integer.toString(exchanges),
                    exchanges > 0));
        }
        run.finalScreenshot(AutoExPurchaseTraceStep.TERMINAL_STATE, finalAssertions);
    }

    /** Opens the store, submits the configured search and validates the public UI result and route. */
    public static ProductsPage searchConfiguredProduct(AutoExPurchaseSession run) {
        AutoExConfiguration.Catalog product = run.configuration().catalog();
        String homeHeading = new HomePage(run).open();
        run.checkpoint(AutoExPurchaseTraceStep.STORE_OPEN,
                List.of(new AtfAssertion("Store availability", "Automation Exercise is visible",
                        homeHeading, homeHeading.contains("Full-Fledged practice website"))));

        if (run.observesApiTraffic()) {
            AutoExCatalogApiPrecondition.verifyAndMarkBaseline(product.productName(), product.unitPrice(),
                    run::inspectCatalogApi,
                    assertions -> run.checkpoint(AutoExPurchaseTraceStep.CATALOG_API_PRECONDITION, assertions),
                    run::markCausalApiTrafficBaseline);
        }

        new HomePage(run).openProducts();
        ProductsPage products = new ProductsPage(run);
        products.search(product.searchTerm());
        String resultName = products.firstProductName();
        String searchUrl = run.currentUrl();
        run.checkpoint(AutoExPurchaseTraceStep.PRODUCT_SEARCH,
                List.of(
                new AtfAssertion("Search product", product.productName(), resultName,
                        product.productName().equals(resultName)),
                new AtfAssertion("Search route", "/products?search=", searchUrl,
                        searchUrl.contains("/products?search="))));
        return products;
    }
}
