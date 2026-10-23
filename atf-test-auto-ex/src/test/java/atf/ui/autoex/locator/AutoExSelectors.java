package atf.ui.autoex.locator;

/** CSS vocabulary for the public practice site; tests and page objects do not embed selectors. */
public final class AutoExSelectors {
    public static final String STORE_HEADING = "#slider-carousel h2";
    public static final String PRIVACY_DIALOG = ".fc-consent-root";
    public static final String PRIVACY_MANAGE_OPTIONS = ".fc-consent-root button:has-text('Manage options')";
    public static final String PRIVACY_SELECTED_OPTIONS = ".fc-consent-root input.fc-preference-slider-purpose:checked";
    public static final String PRIVACY_OPTION_LABEL = "label[for='%s']";
    public static final String PRIVACY_CONFIRM_CHOICES = ".fc-consent-root button:visible:has-text('Confirm choices')";
    public static final String PRODUCTS_LINK = "a[href='/products']";
    public static final String CART_LINK = "#header a[href='/view_cart']";
    public static final String SEARCH_INPUT = "#search_product";
    public static final String SEARCH_BUTTON = "#submit_search";
    public static final String PAGE_TITLE = "h2.title.text-center";
    public static final String SEARCH_RESULTS_HEADING = PAGE_TITLE;
    public static final String ACCOUNT_RESULT_HEADING = PAGE_TITLE;
    public static final String PRODUCT_DETAILS_LINK = "a[href^='/product_details/']";
    public static final String PRODUCT_INFORMATION = ".product-information";
    public static final String QUANTITY = "#quantity";
    public static final String ADD_TO_CART = "button.cart";
    public static final String VIEW_CART = "a[href='/view_cart']";
    public static final String CART_PRODUCT_ROW_PREFIX = "#product-";
    public static final String CART_QUANTITY = ".cart_quantity button";
    public static final String CART_PRICE = ".cart_price";
    public static final String CART_TOTAL = ".cart_total";
    public static final String CHECKOUT = "a.check_out";
    public static final String REGISTER_LOGIN = "a[href='/login']";
    public static final String SIGNUP_NAME = "[data-qa='signup-name']";
    public static final String SIGNUP_EMAIL = "[data-qa='signup-email']";
    public static final String SIGNUP_BUTTON = "[data-qa='signup-button']";
    public static final String ACCOUNT_PASSWORD = "[data-qa='password']";
    public static final String ACCOUNT_GENDER = "#id_gender1";
    public static final String ACCOUNT_DAYS = "[data-qa='days']";
    public static final String ACCOUNT_MONTHS = "[data-qa='months']";
    public static final String ACCOUNT_YEARS = "[data-qa='years']";
    public static final String ACCOUNT_FIRST_NAME = "[data-qa='first_name']";
    public static final String ACCOUNT_LAST_NAME = "[data-qa='last_name']";
    public static final String ACCOUNT_COMPANY = "[data-qa='company']";
    public static final String ACCOUNT_ADDRESS_1 = "[data-qa='address']";
    public static final String ACCOUNT_ADDRESS_2 = "[data-qa='address2']";
    public static final String ACCOUNT_COUNTRY = "[data-qa='country']";
    public static final String ACCOUNT_STATE = "[data-qa='state']";
    public static final String ACCOUNT_CITY = "[data-qa='city']";
    public static final String ACCOUNT_POSTAL_CODE = "[data-qa='zipcode']";
    public static final String ACCOUNT_MOBILE = "[data-qa='mobile_number']";
    public static final String ACCOUNT_NEWSLETTER = "#newsletter";
    public static final String ACCOUNT_OFFERS = "#optin";
    public static final String CREATE_ACCOUNT = "[data-qa='create-account']";
    public static final String CONTINUE = "[data-qa='continue-button']";
    public static final String DELETE_ACCOUNT = "#header a[href='/delete_account']";
    public static final String DELIVERY_ADDRESS = "#address_delivery";
    public static final String BILLING_ADDRESS = "#address_invoice";
    public static final String ORDER_REVIEW = "#cart_info";
    public static final String ORDER_COMMENT = "textarea[name='message']";
    public static final String PLACE_ORDER = "a[href='/payment']";
    public static final String PAYMENT_HEADING = PAGE_TITLE;
    public static final String CARD_NAME = "[data-qa='name-on-card']";
    public static final String CARD_NUMBER = "[data-qa='card-number']";
    public static final String CARD_CVC = "[data-qa='cvc']";
    public static final String CARD_EXPIRY_MONTH = "[data-qa='expiry-month']";
    public static final String CARD_EXPIRY_YEAR = "[data-qa='expiry-year']";
    public static final String PAY_AND_CONFIRM = "[data-qa='pay-button']";
    public static final String ORDER_SUCCESS = "#success_message";
    public static final String DOWNLOAD_INVOICE = "[data-qa='download-invoice']";
    public static final String SENSITIVE_FIELDS = "input, textarea, select, [contenteditable='true']";

    private AutoExSelectors() { }
}
