package atf.ui.practiceshop.api;

/** Stable sort vocabulary exposed by the Practice Shop product endpoint. */
public enum PracticeShopProductSort {
    NAME_ASC("name,asc"),
    NAME_DESC("name,desc"),
    PRICE_ASC("price,asc"),
    PRICE_DESC("price,desc");

    private final String wireValue;

    PracticeShopProductSort(String wireValue) { this.wireValue = wireValue; }

    public String wireValue() { return wireValue; }
}
