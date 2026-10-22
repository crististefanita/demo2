package atf.ui.practiceshop.api;

/** Auth-profile names exchanged with the shared ATF API executor. */
public enum ShopApiAuthProfile {
    NONE("none"),
    BEARER("bearer");

    private final String wireValue;

    ShopApiAuthProfile(String wireValue) {
        this.wireValue = wireValue;
    }

    public String wireValue() {
        return wireValue;
    }
}
