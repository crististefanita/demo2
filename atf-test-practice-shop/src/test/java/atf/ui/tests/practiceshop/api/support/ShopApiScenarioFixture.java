package atf.ui.tests.practiceshop.api.support;

import atf.ui.practiceshop.config.ShopApiConfiguration;
import atf.ui.practiceshop.config.ShopCredentials;

import java.net.URI;

/** Named scenario vocabulary shared by Practice Shop API component tests. */
public final class ShopApiScenarioFixture {
    public static final URI API_ENDPOINT = ShopApiConfiguration.defaults().endpoint();
    public static final String VALID_EMAIL = "atf@example.test";
    public static final String VALID_PASSWORD = "secret";
    public static final String VALID_FIRST_NAME = "ATF";
    public static final String VALID_LAST_NAME = "Practice";
    public static final String INVALID_EMAIL = "invalid@example.test";
    public static final String INVALID_PASSWORD = "invalid";
    public static final String INVALID_LAST_NAME = "Invalid";
    public static final String PRODUCT_NAME = "Combination Pliers";
    public static final String PRODUCT_ID = "01product";
    public static final String CART_ID = "01cart";
    public static final String CATEGORY_ID = "category-1";
    public static final String OUT_OF_STOCK_PRODUCT_ID = "out-of-stock";
    public static final String MUTATION_CART_ID = "cart-1";
    public static final String MUTATION_PRODUCT_ID = "product-1";
    public static final String FILTER_BRAND_ID = "brand-1";
    public static final String FILTER_PRICE_RANGE = "price,10,30";
    public static final int FIRST_PAGE = 1;
    public static final int FILTER_PAGE = 2;
    public static final boolean FILTER_RENTAL = false;
    public static final String MISSING_PRODUCT_ID = "missing-product";
    public static final String COUNTRY = "RO";
    public static final String POSTCODE = "010101";
    public static final String HOUSE_NUMBER = "10";
    public static final String UNAUTHORIZED_BODY = "{\"message\":\"Unauthorized\"}";
    public static final String NOT_FOUND_BODY = "{\"message\":\"Not found\"}";
    public static final String PAYMENT_SUCCESS_BODY = "{\"message\":\"Payment was successful\"}";
    public static final String LOGIN_TOKEN_BODY = "{\"access_token\":\"token-1\"}";
    public static final String PRODUCT_SEARCH_BODY =
            "{\"data\":[{\"id\":\"01product\",\"name\":\"Combination Pliers\"}]}";
    public static final String PRODUCT_COLLECTION_BODY =
            "{\"data\":[{\"id\":\"oos\",\"name\":\"Out of stock\",\"in_stock\":false}],"
                    + "\"category\":\"category-1\"}";
    public static final String CATEGORIES_BODY = "[{\"id\":\"category-1\",\"name\":\"Hand Tools\"}]";
    public static final String OUT_OF_STOCK_BODY =
            "{\"id\":\"out-of-stock\",\"name\":\"Out of stock\",\"in_stock\":false}";
    public static final String PRODUCT_BODY = "{\"id\":\"01product\",\"name\":\"Combination Pliers\"}";
    public static final String CART_BODY = "{\"id\":\"01cart\"}";
    public static final String CART_ITEM_BODY =
            "{\"cart_items\":[{\"product_id\":\"01product\",\"quantity\":2}]}";
    public static final String CART_MUTATION_BODY = "{\"result\":\"item added or updated\"}";
    public static final String CART_UPDATE_BODY = "{\"result\":\"item updated\"}";
    public static final String CART_UPDATE_RESULT = "item updated";
    public static final String ADDRESS_BODY = "{\"country\":\"RO\",\"postcode\":\"010101\"}";
    public static final String UNEXPECTED_ROUTE_BODY = "{\"message\":\"unexpected route\"}";
    public static final int QUANTITY = 2;
    public static final int UPDATED_QUANTITY = 3;

    private ShopApiScenarioFixture() { }

    public static ShopCredentials validCredentials() {
        return new ShopCredentials(VALID_EMAIL, VALID_PASSWORD, VALID_FIRST_NAME, VALID_LAST_NAME);
    }

    public static ShopCredentials invalidCredentials() {
        return new ShopCredentials(INVALID_EMAIL, INVALID_PASSWORD, VALID_FIRST_NAME, INVALID_LAST_NAME);
    }
}
