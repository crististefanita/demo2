package atf.ui.practiceshop.api;

import atf.api.core.http.request.HttpMethod;
import atf.api.core.http.response.ApiOperationLabel;

/**
 * Stable Practice Shop operation vocabulary used by request creation,
 * network selection and report titles. Dynamic IDs and query values remain in
 * the request/path, never in this enum.
 */
public enum PracticeShopApiOperation {
    LOGIN(HttpMethod.POST, PracticeShopApiRoutes.LOGIN, "establish API session", "/users/login"),
    SEARCH(HttpMethod.GET, PracticeShopApiRoutes.PRODUCT_SEARCH,
            "find configured product", "/products/search"),
    PRODUCT_DETAIL(HttpMethod.GET, PracticeShopApiRoutes.PRODUCTS,
            "read selected product", "/products/{productId}"),
    CART_CREATE(HttpMethod.POST, PracticeShopApiRoutes.CARTS,
            "create cart", PracticeShopApiRoutes.CARTS),
    CART_ADD(HttpMethod.POST, PracticeShopApiRoutes.CART_RESOURCE_PREFIX,
            "add product to cart", "/carts/{cartId}"),
    CART_READ(HttpMethod.GET, PracticeShopApiRoutes.CART_RESOURCE_PREFIX,
            "verify cart contents", "/carts/{cartId}"),
    POSTCODE_LOOKUP(HttpMethod.GET, PracticeShopApiRoutes.POSTCODE_LOOKUP,
            "resolve checkout address", PracticeShopApiRoutes.POSTCODE_LOOKUP),
    PAYMENT_CHECK(HttpMethod.POST, PracticeShopApiRoutes.PAYMENT_CHECK,
            "validate payment selection", PracticeShopApiRoutes.PAYMENT_CHECK),
    UNKNOWN(null, "", "API request", "");

    private static final String OBSERVED_QUERY_METHOD = "QUERY";

    private final HttpMethod method;
    private final String routePrefix;
    private final String description;
    private final String routeTemplate;

    PracticeShopApiOperation(HttpMethod method, String routePrefix,
                             String description, String routeTemplate) {
        this.method = method;
        this.routePrefix = routePrefix;
        this.description = description;
        this.routeTemplate = routeTemplate;
    }

    /** The actual HTTP verb sent over the wire. */
    public String wireValue() {
        return method == null ? "" : method.value();
    }

    /** Canonical method used by the report when a legacy UI alias was observed. */
    public String reportMethod() {
        return wireValue();
    }

    public String description() {
        return description;
    }

    public String routeTemplate() {
        return routeTemplate;
    }

    public boolean matches(String actualMethod, String actualPath) {
        if (method == null || actualMethod == null || actualPath == null) return false;
        if (!method.value().equalsIgnoreCase(actualMethod)
                && !(this == SEARCH && OBSERVED_QUERY_METHOD.equalsIgnoreCase(actualMethod))) {
            return false;
        }
        return switch (this) {
            case LOGIN, SEARCH, CART_CREATE, POSTCODE_LOOKUP, PAYMENT_CHECK ->
                    actualPath.equals(routePrefix) || actualPath.startsWith(routePrefix + "?");
            case PRODUCT_DETAIL -> actualPath.startsWith(routePrefix)
                    && !actualPath.equals(PracticeShopApiRoutes.PRODUCT_COLLECTION)
                    && !actualPath.startsWith(PracticeShopApiRoutes.PRODUCT_SEARCH);
            case CART_ADD, CART_READ -> actualPath.startsWith(routePrefix)
                    && !actualPath.equals(PracticeShopApiRoutes.CARTS);
            case UNKNOWN -> false;
        };
    }

    public static PracticeShopApiOperation resolve(String method, String path) {
        for (PracticeShopApiOperation operation : values()) {
            if (operation.matches(method, path)) return operation;
        }
        return UNKNOWN;
    }

    public static String reportTitle(String method, String path) {
        PracticeShopApiOperation operation = resolve(method, path);
        String reportMethod = operation == UNKNOWN ? method : operation.reportMethod();
        return ApiOperationLabel.format(reportMethod, path, operation.description());
    }
}
