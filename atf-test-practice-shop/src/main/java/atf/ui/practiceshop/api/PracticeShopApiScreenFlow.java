package atf.ui.practiceshop.api;

import atf.ui.practiceshop.api.screen.PracticeShopCartApi;
import atf.ui.practiceshop.api.screen.PracticeShopCatalogApi;
import atf.ui.practiceshop.api.screen.PracticeShopCheckoutApi;
import atf.ui.practiceshop.api.screen.PracticeShopLoginApi;

/** Composition root for the screen-by-screen API pyramid. */
public final class PracticeShopApiScreenFlow {
    private final PracticeShopApiClient client;

    public PracticeShopApiScreenFlow(PracticeShopApiClient client) { this.client = client; }

    public PracticeShopLoginApi login() { return new PracticeShopLoginApi(client); }
    public PracticeShopCatalogApi catalog() { return new PracticeShopCatalogApi(client); }
    public PracticeShopCartApi cart() { return new PracticeShopCartApi(client); }
    public PracticeShopCheckoutApi checkout() { return new PracticeShopCheckoutApi(client); }
}
