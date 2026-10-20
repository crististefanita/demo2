package atf.ui.practiceshop.api;

/** JSON field vocabulary shared by request construction and contract assertions. */
public enum PracticeShopApiJsonField {
    EMAIL("email"),
    PASSWORD("password"),
    PRODUCT_ID("product_id"),
    QUANTITY("quantity"),
    PAYMENT_METHOD("payment_method"),
    PAYMENT_DETAILS("payment_details"),
    DATA("data"),
    ID("id"),
    NAME("name"),
    CATEGORY("category"),
    IN_STOCK("in_stock"),
    MESSAGE("message"),
    RESULT("result"),
    POSTCODE("postcode"),
    CART_ITEMS("cart_items"),
    ACCESS_TOKEN("access_token"),
    ACCESS_TOKEN_CAMEL("accessToken"),
    TOKEN("token");

    private final String wireValue;

    PracticeShopApiJsonField(String wireValue) {
        this.wireValue = wireValue;
    }

    public String wireValue() {
        return wireValue;
    }
}
