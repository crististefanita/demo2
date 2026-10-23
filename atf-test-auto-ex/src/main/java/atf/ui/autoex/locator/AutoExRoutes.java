package atf.ui.autoex.locator;

import java.net.URI;

/** Application routes kept separate from page behavior and selectors. */
public enum AutoExRoutes {
    HOME("/"),
    PRODUCTS("/products"),
    PRODUCTS_API("/api/productsList"),
    CART("/view_cart"),
    LOGIN("/login"),
    LOGOUT("/logout"),
    PAYMENT("/payment"),
    DELETE_ACCOUNT("/delete_account"),
    INVOICE_DOWNLOAD("/download_invoice/");

    public static final String GOOGLE_VIGNETTE_FRAGMENT = "#google_vignette";

    private final String path;

    AutoExRoutes(String path) { this.path = path; }

    public String path() { return path; }

    public URI resolve(URI baseUrl) {
        String root = baseUrl.toString().endsWith("/")
                ? baseUrl.toString() : baseUrl + "/";
        return URI.create(root).resolve(path.startsWith("/") ? path.substring(1) : path);
    }
}
