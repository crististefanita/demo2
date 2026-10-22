package atf.ui.practiceshop.api;

/** Stable evidence vocabulary for the authentication mechanism observed on a request. */
public enum ShopApiAuthType {
    NONE("none"),
    BEARER("bearer"),
    BASIC("basic"),
    API_KEY("api-key"),
    COOKIE("cookie"),
    AUTHORIZATION_HEADER("authorization-header");

    private final String wireValue;

    ShopApiAuthType(String wireValue) {
        this.wireValue = wireValue;
    }

    public String wireValue() {
        return wireValue;
    }
}
