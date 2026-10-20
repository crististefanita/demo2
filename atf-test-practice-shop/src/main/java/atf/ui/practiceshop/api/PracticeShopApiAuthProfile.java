package atf.ui.practiceshop.api;

/** Auth-profile names exchanged with the shared ATF API executor. */
public enum PracticeShopApiAuthProfile {
    NONE("none"),
    BEARER("bearer");

    private final String wireValue;

    PracticeShopApiAuthProfile(String wireValue) {
        this.wireValue = wireValue;
    }

    public String wireValue() {
        return wireValue;
    }
}
