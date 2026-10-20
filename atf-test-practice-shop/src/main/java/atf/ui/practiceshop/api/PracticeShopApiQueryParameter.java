package atf.ui.practiceshop.api;

/** Query-parameter vocabulary for the Practice Shop API. */
public enum PracticeShopApiQueryParameter {
    SEARCH("q"),
    COUNTRY("country"),
    POSTCODE("postcode"),
    HOUSE_NUMBER("house_number"),
    BRAND("by_brand"),
    CATEGORY("by_category"),
    RENTAL("is_rental"),
    PRICE_RANGE("between"),
    SORT("sort"),
    PAGE("page");

    private final String wireValue;

    PracticeShopApiQueryParameter(String wireValue) {
        this.wireValue = wireValue;
    }

    public String wireValue() {
        return wireValue;
    }
}
