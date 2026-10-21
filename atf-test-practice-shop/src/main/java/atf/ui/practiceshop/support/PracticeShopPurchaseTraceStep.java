package atf.ui.practiceshop.support;

import atf.api.reporting.contract.AtfGherkinKeyword;

/**
 * Stable vocabulary for the guest purchase trace.
 *
 * <p>The enum owns only static reporting semantics. Runtime values such as
 * product, quantity, route and API evidence remain in the flow assertions.</p>
 */
public enum PracticeShopPurchaseTraceStep {
    CATALOG_HOME(AtfGherkinKeyword.GIVEN,
            "catalog home structure displays navigation, branding and banner"),
    HOME_FILTERS(AtfGherkinKeyword.AND,
            "catalog home filters expose sorting, price, search and category controls"),
    HOME_PRODUCT_GRID(AtfGherkinKeyword.AND,
            "catalog home renders product cards, pagination and footer"),
    PRODUCT_PAGE(AtfGherkinKeyword.AND,
            "product page displays the configured product and ready-to-buy fields"),
    CART(AtfGherkinKeyword.WHEN,
            "cart displays the configured product with the requested quantity"),
    AUTHENTICATED_CHECKOUT(AtfGherkinKeyword.AND,
            "authenticated checkout exposes the signed-in proceed action"),
    GUEST_CHECKOUT(AtfGherkinKeyword.AND,
            "guest checkout form contains the configured identity"),
    BILLING(AtfGherkinKeyword.AND,
            "billing form is completed with the configured address"),
    PAYMENT_METHOD(AtfGherkinKeyword.AND,
            "payment form displays the configured demo method"),
    PAYMENT_CHECK(AtfGherkinKeyword.AND,
            "payment check accepts the configured demo method"),
    ORDER_CONFIRMATION(AtfGherkinKeyword.THEN,
            "order confirmation is displayed for the completed purchase");

    private final AtfGherkinKeyword keyword;
    private final String title;

    PracticeShopPurchaseTraceStep(AtfGherkinKeyword keyword, String title) {
        this.keyword = keyword;
        this.title = title;
    }

    public AtfGherkinKeyword keyword() {
        return keyword;
    }

    public String title() {
        return title;
    }
}
