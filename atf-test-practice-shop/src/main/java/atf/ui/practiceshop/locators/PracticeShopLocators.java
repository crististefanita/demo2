package atf.ui.practiceshop.locators;

import atf.api.ui.playwright.AtfLocator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

import java.util.Objects;

/** The only selector owner for the Practice Software Testing application. */
public final class PracticeShopLocators {
    private static final String LOGIN_EMAIL = "input[id='email']";
    private static final String LOGIN_PASSWORD = "input[id='password']";
    private static final String LOGIN_SUBMIT = "*[data-test='login-submit']";
    private static final String REGISTER_FIRST_NAME = "input[id='first_name']";
    private static final String REGISTER_LAST_NAME = "input[id='last_name']";
    private static final String REGISTER_DOB = "input[id='dob']";
    private static final String REGISTER_COUNTRY = "select[id='country']";
    private static final String REGISTER_POSTAL_CODE = "input[id='postal_code']";
    private static final String REGISTER_HOUSE_NUMBER = "input[id='house_number']";
    private static final String REGISTER_STREET = "input[id='street']";
    private static final String REGISTER_CITY = "input[id='city']";
    private static final String REGISTER_STATE = "input[id='state']";
    private static final String REGISTER_PHONE = "input[id='phone']";
    private static final String REGISTER_EMAIL = "input[id='email']";
    private static final String REGISTER_PASSWORD = "input[id='password']";
    private static final String REGISTER_SUBMIT = "button[data-test='register-submit']";
    private static final String SEARCH_QUERY = "input[id='search-query']";
    private static final String HOME_NAVIGATION = "nav.navbar";
    private static final String HOME_BRAND = "a.navbar-brand";
    private static final String HOME_BANNER = "img[alt='Banner']";
    private static final String HOME_SORT = "select[data-test='sort']";
    private static final String HOME_PRICE_RANGE = "span[role='slider']";
    private static final String HOME_SEARCH_RESET = "button[data-test='search-reset']";
    private static final String HOME_SEARCH_SUBMIT = "button[data-test='search-submit']";
    private static final String HOME_CATEGORY_FILTERS = "input[name='category_id']";
    private static final String HOME_BRAND_FILTERS = "input[name='brand_id']";
    private static final String HOME_ECO_FILTER = "input[data-test='eco-friendly-filter']";
    private static final String HOME_PRODUCT_GRID = "a[data-test^='product-']";
    private static final String HOME_PRODUCT_NAMES = "a[data-test^='product-'] [data-test='product-name']";
    private static final String HOME_PRODUCT_IMAGES = "a[data-test^='product-'] img.card-img-top";
    private static final String HOME_PAGINATION = "nav:has([data-test='pagination-next'])";
    private static final String HOME_FOOTER = "footer";
    private static final String PRODUCT_LINK = PracticeShopProductSelector.PRODUCT_CARD;
    // The external Practice Shop contract guarantees data-test, not a stable tag.
    private static final String PRODUCT_NAME = "*[data-test='product-name']";
    private static final String PRODUCT_PRICE = "*[data-test='unit-price']";
    private static final String PRODUCT_DESCRIPTION = "*[data-test='product-description']";
    private static final String PRODUCT_QUANTITY = "input[data-test='quantity']";
    private static final String ADD_TO_CART = "button[data-test='add-to-cart']";
    private static final String CART = "a[data-test='nav-cart']";
    private static final String CART_PRODUCT_TITLE = "[data-test='product-title']";
    private static final String CART_PRODUCT_QUANTITY = "input[data-test='product-quantity']";
    private static final String CART_PROCEED = "button[data-test='proceed-1']";
    private static final String GUEST_TAB = "a[href='#guest-tab']";
    private static final String GUEST_EMAIL = "input[data-test='guest-email']";
    private static final String GUEST_FIRST_NAME = "input[data-test='guest-first-name']";
    private static final String GUEST_LAST_NAME = "input[data-test='guest-last-name']";
    private static final String GUEST_SUBMIT = "input[data-test='guest-submit']";
    private static final String GUEST_PROCEED = "button[data-test='proceed-2-guest']";
    private static final String AUTHENTICATED_PROCEED = "button[data-test='proceed-2']";
    private static final String COUNTRY = "select[data-test='country']";
    private static final String COUNTRY_SELECTED_OPTION = COUNTRY + " option:checked";
    private static final String POSTAL_CODE = "input[data-test='postal_code']";
    private static final String HOUSE_NUMBER = "input[data-test='house_number']";
    private static final String STREET = "input[data-test='street']";
    private static final String CITY = "input[data-test='city']";
    private static final String STATE = "input[data-test='state']";
    private static final String BILLING_PROCEED = "button[data-test='proceed-3']";
    private static final String PAYMENT_METHOD = "select[data-test='payment-method']";
    private static final String PAYMENT_METHOD_SELECTED_OPTION =
            PAYMENT_METHOD + " option:checked";
    private static final String PAYMENT_CHECK = "button[data-test='finish']";
    private static final String ACCOUNT_HEADING = "h1";
    private static final String PAYMENT_SUCCESS =
            "xpath=//*[contains(normalize-space(text()), 'Payment was successful')]";

    public AtfLocator loginEmail(Page page) { return css(page, LOGIN_EMAIL); }
    public AtfLocator loginPassword(Page page) { return css(page, LOGIN_PASSWORD); }
    public AtfLocator loginSubmit(Page page) { return css(page, LOGIN_SUBMIT); }
    public AtfLocator registerFirstName(Page page) { return css(page, REGISTER_FIRST_NAME); }
    public AtfLocator registerLastName(Page page) { return css(page, REGISTER_LAST_NAME); }
    public AtfLocator registerDob(Page page) { return css(page, REGISTER_DOB); }
    public AtfLocator registerCountry(Page page) { return css(page, REGISTER_COUNTRY); }
    public AtfLocator registerPostalCode(Page page) { return css(page, REGISTER_POSTAL_CODE); }
    public AtfLocator registerHouseNumber(Page page) { return css(page, REGISTER_HOUSE_NUMBER); }
    public AtfLocator registerStreet(Page page) { return css(page, REGISTER_STREET); }
    public AtfLocator registerCity(Page page) { return css(page, REGISTER_CITY); }
    public AtfLocator registerState(Page page) { return css(page, REGISTER_STATE); }
    public AtfLocator registerPhone(Page page) { return css(page, REGISTER_PHONE); }
    public AtfLocator registerEmail(Page page) { return css(page, REGISTER_EMAIL); }
    public AtfLocator registerPassword(Page page) { return css(page, REGISTER_PASSWORD); }
    public AtfLocator registerSubmit(Page page) { return css(page, REGISTER_SUBMIT); }
    public AtfLocator searchQuery(Page page) { return css(page, SEARCH_QUERY); }
    public AtfLocator homeNavigation(Page page) { return css(page, HOME_NAVIGATION); }
    public AtfLocator homeBrand(Page page) { return css(page, HOME_BRAND); }
    public AtfLocator homeBanner(Page page) { return css(page, HOME_BANNER); }
    public AtfLocator homeSort(Page page) { return css(page, HOME_SORT); }
    public AtfLocator homePriceRange(Page page) { return css(page, HOME_PRICE_RANGE); }
    public AtfLocator homeSearchReset(Page page) { return css(page, HOME_SEARCH_RESET); }
    public AtfLocator homeSearchSubmit(Page page) { return css(page, HOME_SEARCH_SUBMIT); }
    public AtfLocator homeCategoryFilters(Page page) { return css(page, HOME_CATEGORY_FILTERS); }
    public AtfLocator homeBrandFilters(Page page) { return css(page, HOME_BRAND_FILTERS); }
    public AtfLocator homeEcoFilter(Page page) { return css(page, HOME_ECO_FILTER); }
    public AtfLocator homeProductGrid(Page page) { return css(page, HOME_PRODUCT_GRID); }
    /** First card is used only for strict synchronization; collection stays available for counts. */
    public AtfLocator homeProductGridFirst(Page page) {
        return new AtfLocator(page.locator(HOME_PRODUCT_GRID).first(), HOME_PRODUCT_GRID);
    }
    public AtfLocator homeProductNames(Page page) { return css(page, HOME_PRODUCT_NAMES); }
    public AtfLocator homeProductImages(Page page) { return css(page, HOME_PRODUCT_IMAGES); }
    public AtfLocator homePagination(Page page) { return css(page, HOME_PAGINATION); }
    public AtfLocator homeFooter(Page page) { return css(page, HOME_FOOTER); }
    public AtfLocator productLink(Page page, String name) {
        return css(page, PracticeShopProductSelector.exactProductCard(name));
    }
    /** First exact-name card is the deterministic interaction target; the collection remains available for diagnostics. */
    public AtfLocator productLinkFirst(Page page, String name) {
        AtfLocator matchingProducts = productLink(page, name);
        return new AtfLocator(matchingProducts.locator().first(), matchingProducts.selector());
    }
    public AtfLocator productLinkByPath(Page page, String productPath) {
        return css(page, "a[href='" + PracticeShopCssEscaper.quoted(productPath) + "']");
    }
    public AtfLocator accountBody(Page page) { return pageBody(page); }
    public AtfLocator accountHeading(Page page) { return css(page, ACCOUNT_HEADING); }
    public AtfLocator pageBody(Page page) { return css(page, "body"); }
    public AtfLocator productName(Page page) { return css(page, PRODUCT_NAME); }
    public AtfLocator productPrice(Page page) { return css(page, PRODUCT_PRICE); }
    public AtfLocator productDescription(Page page) { return css(page, PRODUCT_DESCRIPTION); }
    public AtfLocator productQuantity(Page page) { return css(page, PRODUCT_QUANTITY); }
    public AtfLocator addToCart(Page page) { return css(page, ADD_TO_CART); }
    public AtfLocator cart(Page page) { return css(page, CART); }
    public AtfLocator cartProductTitle(Page page) { return css(page, CART_PRODUCT_TITLE); }
    public AtfLocator cartProductQuantity(Page page) { return css(page, CART_PRODUCT_QUANTITY); }
    public AtfLocator cartProceed(Page page) { return css(page, CART_PROCEED); }
    public AtfLocator guestTab(Page page) { return css(page, GUEST_TAB); }
    public AtfLocator guestEmail(Page page) { return css(page, GUEST_EMAIL); }
    public AtfLocator guestFirstName(Page page) { return css(page, GUEST_FIRST_NAME); }
    public AtfLocator guestLastName(Page page) { return css(page, GUEST_LAST_NAME); }
    public AtfLocator guestSubmit(Page page) { return css(page, GUEST_SUBMIT); }
    public AtfLocator guestProceed(Page page) { return css(page, GUEST_PROCEED); }
    public AtfLocator authenticatedProceed(Page page) { return css(page, AUTHENTICATED_PROCEED); }
    public AtfLocator country(Page page) { return css(page, COUNTRY); }
    public AtfLocator selectedCountry(Page page) { return css(page, COUNTRY_SELECTED_OPTION); }
    public AtfLocator postalCode(Page page) { return css(page, POSTAL_CODE); }
    public AtfLocator houseNumber(Page page) { return css(page, HOUSE_NUMBER); }
    public AtfLocator street(Page page) { return css(page, STREET); }
    public AtfLocator city(Page page) { return css(page, CITY); }
    public AtfLocator state(Page page) { return css(page, STATE); }
    public AtfLocator billingProceed(Page page) { return css(page, BILLING_PROCEED); }
    public AtfLocator paymentMethod(Page page) { return css(page, PAYMENT_METHOD); }
    public AtfLocator selectedPaymentMethod(Page page) {
        return css(page, PAYMENT_METHOD_SELECTED_OPTION);
    }
    public AtfLocator paymentCheck(Page page) { return css(page, PAYMENT_CHECK); }
    public AtfLocator paymentSuccess(Page page) { return css(page, PAYMENT_SUCCESS); }
    public AtfLocator confirmOrder(Page page) {
        return new AtfLocator(page.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Confirm").setExact(true)),
                "role=button[name='Confirm']");
    }
    public AtfLocator orderConfirmation(Page page) {
        String selector = "xpath=//*[contains(normalize-space(text()), 'Thanks for your order!')]";
        return new AtfLocator(page.locator(selector), selector);
    }
    /** Resolves a semantic control for annotation-driven page validation. */
    public AtfLocator control(Page page, PracticeShopControl control) {
        Objects.requireNonNull(control, "control");
        return switch (control) {
            case PAGE_BODY -> pageBody(page);
            case HOME_NAVIGATION -> homeNavigation(page);
            case HOME_BRAND -> homeBrand(page);
            case HOME_BANNER -> homeBanner(page);
            case HOME_SORT -> homeSort(page);
            case HOME_PRICE_RANGE -> homePriceRange(page);
            case SEARCH_QUERY -> searchQuery(page);
            case HOME_SEARCH_RESET -> homeSearchReset(page);
            case HOME_SEARCH_SUBMIT -> homeSearchSubmit(page);
            case HOME_CATEGORY_FILTERS -> homeCategoryFilters(page);
            case HOME_BRAND_FILTERS -> homeBrandFilters(page);
            case HOME_ECO_FILTER -> homeEcoFilter(page);
            case HOME_PRODUCT_GRID -> homeProductGrid(page);
            case HOME_PRODUCT_NAMES -> homeProductNames(page);
            case HOME_PRODUCT_IMAGES -> homeProductImages(page);
            case HOME_PAGINATION -> homePagination(page);
            case HOME_FOOTER -> homeFooter(page);
            case LOGIN_EMAIL -> loginEmail(page);
            case LOGIN_PASSWORD -> loginPassword(page);
            case LOGIN_SUBMIT -> loginSubmit(page);
            case REGISTER_FIRST_NAME -> registerFirstName(page);
            case REGISTER_LAST_NAME -> registerLastName(page);
            case REGISTER_DOB -> registerDob(page);
            case REGISTER_COUNTRY -> registerCountry(page);
            case REGISTER_POSTAL_CODE -> registerPostalCode(page);
            case REGISTER_HOUSE_NUMBER -> registerHouseNumber(page);
            case REGISTER_STREET -> registerStreet(page);
            case REGISTER_CITY -> registerCity(page);
            case REGISTER_STATE -> registerState(page);
            case REGISTER_PHONE -> registerPhone(page);
            case REGISTER_EMAIL -> registerEmail(page);
            case REGISTER_PASSWORD -> registerPassword(page);
            case REGISTER_SUBMIT -> registerSubmit(page);
            case PRODUCT_NAME -> productName(page);
            case PRODUCT_PRICE -> productPrice(page);
            case PRODUCT_DESCRIPTION -> productDescription(page);
            case PRODUCT_QUANTITY -> productQuantity(page);
            case ADD_TO_CART -> addToCart(page);
            case CART_PRODUCT_TITLE -> cartProductTitle(page);
            case CART_PRODUCT_QUANTITY -> cartProductQuantity(page);
            case CART_PROCEED -> cartProceed(page);
            case GUEST_TAB -> guestTab(page);
            case GUEST_EMAIL -> guestEmail(page);
            case GUEST_FIRST_NAME -> guestFirstName(page);
            case GUEST_LAST_NAME -> guestLastName(page);
            case GUEST_SUBMIT -> guestSubmit(page);
            case GUEST_PROCEED -> guestProceed(page);
            case AUTHENTICATED_PROCEED -> authenticatedProceed(page);
            case COUNTRY -> country(page);
            case POSTAL_CODE -> postalCode(page);
            case HOUSE_NUMBER -> houseNumber(page);
            case STREET -> street(page);
            case CITY -> city(page);
            case STATE -> state(page);
            case BILLING_PROCEED -> billingProceed(page);
            case PAYMENT_METHOD -> paymentMethod(page);
            case PAYMENT_CHECK -> paymentCheck(page);
            case PAYMENT_SUCCESS -> paymentSuccess(page);
            case CONFIRM_ORDER -> confirmOrder(page);
            case ORDER_CONFIRMATION -> orderConfirmation(page);
            case ACCOUNT_MARKER -> accountHeading(page);
        };
    }
    public AtfLocator searchSubmit(Page page) {
        return css(page, "form:has(" + SEARCH_QUERY + ") button[type='submit']");
    }

    private static AtfLocator css(Page page, String selector) {
        return new AtfLocator(page.locator(selector), selector);
    }

}


