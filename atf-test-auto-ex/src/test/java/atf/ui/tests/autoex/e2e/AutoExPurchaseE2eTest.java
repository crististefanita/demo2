package atf.ui.tests.autoex.e2e;

import atf.api.console.junit.AtfConsoleTestExtension;
import atf.api.reporting.contract.AtfAssertion;
import atf.api.reporting.contract.AtfGherkinKeyword;
import atf.api.testkit.AtfConsoleMessages;
import atf.api.testkit.AtfE2eTest;
import atf.api.testkit.channel.AtfTestChannel;
import atf.api.testkit.channel.TestChannel;
import atf.api.testkit.output.UiReportSubdirectory;
import atf.ui.autoex.config.AutoExConfiguration;
import atf.ui.autoex.page.AccountPage;
import atf.ui.autoex.page.CartPage;
import atf.ui.autoex.page.CheckoutPage;
import atf.ui.autoex.page.HomePage;
import atf.ui.autoex.page.ProductPage;
import atf.ui.autoex.page.ProductsPage;
import atf.ui.autoex.support.AutoExE2eExtension;
import atf.ui.autoex.support.AutoExE2eRun;
import atf.ui.autoex.support.AutoExTestAccount;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

/** Plug-and-play UI scenarios; public-site execution is explicitly opt-in. */
@AtfE2eTest
@AtfTestChannel(TestChannel.UI)
@UiReportSubdirectory("auto-ex")
@AtfConsoleMessages(AtfConsoleMessages.Policy.DETAILS)
@ExtendWith({AtfConsoleTestExtension.class, AutoExE2eExtension.class})
@DisplayName("Automation Exercise customer journey")
class AutoExPurchaseE2eTest {
    @Test
    @DisplayName("search returns the configured product and current price")
    void searchReturnsConfiguredProduct(AutoExE2eRun run) {
        AutoExConfiguration configuration = run.configuration();
        String homeHeading = new HomePage(run).open();
        run.checkpoint(AtfGherkinKeyword.GIVEN, "the Automation Exercise store is available",
                List.of(new AtfAssertion("Store heading", "Full-Fledged practice website",
                        homeHeading, homeHeading.contains("Full-Fledged practice website"))));

        new HomePage(run).openProducts();
        ProductsPage products = new ProductsPage(run);
        products.search(configuration.catalog().searchTerm());
        String heading = products.searchHeading();
        String name = products.firstProductName();
        String price = products.firstProductPrice();
        String searchUrl = run.currentUrl();
        run.checkpoint(AtfGherkinKeyword.THEN, "search results show the configured product and price",
                List.of(
                        new AtfAssertion("Search results", "SEARCHED PRODUCTS", heading,
                                heading.contains("SEARCHED PRODUCTS")),
                        new AtfAssertion("Product", configuration.catalog().productName(), name,
                                configuration.catalog().productName().equals(name)),
                        new AtfAssertion("Unit price", configuration.catalog().unitPrice(), price,
                                configuration.catalog().unitPrice().equals(price)),
                        new AtfAssertion("Search route", "/products?search=", searchUrl,
                                searchUrl.contains("/products?search="))));
    }

    @Test
    @DisplayName("customer registers during checkout, completes a demo order and removes the account")
    void completesRegisterAtCheckoutPurchaseAndCleanup(AutoExE2eRun run) {
        AutoExConfiguration configuration = run.configuration();
        AutoExConfiguration.Catalog product = configuration.catalog();
        String homeHeading = new HomePage(run).open();
        run.checkpoint(AtfGherkinKeyword.GIVEN, "a new browser session opens the public store",
                List.of(new AtfAssertion("Store availability", "Automation Exercise is visible",
                        homeHeading, homeHeading.contains("Full-Fledged practice website"))));

        new HomePage(run).openProducts();
        ProductsPage products = new ProductsPage(run);
        products.search(product.searchTerm());
        String resultName = products.firstProductName();
        run.checkpoint(AtfGherkinKeyword.WHEN, "the customer searches the catalog for the configured product",
                List.of(new AtfAssertion("Search product", product.productName(), resultName,
                        product.productName().equals(resultName))));

        ProductPage details = products.openFirstProduct();
        String detailText = details.details();
        run.checkpoint(AtfGherkinKeyword.THEN, "product details show its price and availability",
                List.of(
                        new AtfAssertion("Product name", product.productName(), detailText,
                                detailText.contains(product.productName())),
                        new AtfAssertion("Product price", product.unitPrice(), detailText,
                                detailText.contains(product.unitPrice())),
                        new AtfAssertion("Availability", "In Stock", detailText,
                                detailText.contains("In Stock"))));

        details.addQuantityToCart(product.quantity());
        String added = details.addConfirmation();
        run.checkpoint(AtfGherkinKeyword.WHEN, "the selected quantity is added to the cart",
                List.of(new AtfAssertion("Add-to-cart confirmation", "Added!", added,
                        added.contains("Added!") && added.contains("added to cart"))));

        CartPage cart = details.viewCartFromConfirmation();
        String cartName = cart.productName(product);
        String cartPrice = cart.productPrice(product);
        String cartQuantity = cart.quantity(product);
        String cartTotal = cart.total(product);
        run.checkpoint(AtfGherkinKeyword.THEN, "the cart preserves the product, quantity and calculated total",
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
        run.checkpoint(AtfGherkinKeyword.THEN,
                "checkout shows the registered addresses and the selected order",
                List.of(
                        new AtfAssertion("Delivery address", configuration.registration().displayName(),
                                deliveryAddress, deliveryAddress.contains(configuration.registration().displayName())),
                        new AtfAssertion("Billing address", configuration.registration().displayName(),
                                billingAddress, billingAddress.contains(configuration.registration().displayName())),
                        new AtfAssertion("Order review", product.productName(), orderReview,
                                orderReview.contains(product.productName()))));

        CheckoutPage checkout = new CheckoutPage(run);
        String paymentHeading = checkout.placeOrder("Synthetic ATF E2E order; demo environment only.");
        run.checkpoint(AtfGherkinKeyword.THEN, "the demo payment form is displayed",
                List.of(new AtfAssertion("Payment page", "Payment", paymentHeading,
                        "Payment".equals(paymentHeading))));
        String confirmation = checkout.payWithDemoCard(configuration.payment());
        run.checkpoint(AtfGherkinKeyword.THEN, "the demo checkout confirms the order",
                List.of(new AtfAssertion("Order confirmation", "Your order has been placed successfully!",
                        confirmation, confirmation.contains("Your order has been placed successfully!"))));

        checkout.downloadInvoice();
        checkout.continueAfterInvoice();
        accountPage.accountDeletionResult();
    }
}
