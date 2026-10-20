package atf.ui.practiceshop.api.screen;

import atf.api.core.http.response.ApiExecutionResult;
import atf.ui.practiceshop.api.PracticeShopApiClient;
import atf.ui.practiceshop.api.PracticeShopProductQuery;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/** Catalog-screen API boundary: search is the source of truth for the product id. */
public final class PracticeShopCatalogApi {
    private static final ObjectMapper JSON = new ObjectMapper();
    private final PracticeShopApiClient client;

    public PracticeShopCatalogApi(PracticeShopApiClient client) { this.client = client; }

    public CatalogScreen openProduct(String productName) {
        ApiExecutionResult search = client.execute(client.requests().search(productName));
        String productId = productId(search.response().body(), productName);
        ApiExecutionResult detail = client.execute(client.requests().product(productId, client.bearerToken()));
        return new CatalogScreen(productName, productId, search, detail);
    }

    public ApiExecutionResult listProducts(PracticeShopProductQuery filters) {
        return client.execute(client.requests().products(filters));
    }

    public ApiExecutionResult listCategories() {
        return client.execute(client.requests().categories());
    }

    private static String productId(String body, String productName) {
        try {
            JsonNode products = JSON.readTree(body).path("data");
            for (JsonNode product : products) {
                if (productName.equalsIgnoreCase(product.path("name").asText())) {
                    String id = product.path("id").asText();
                    if (!id.isBlank()) return id;
                }
            }
        } catch (Exception error) {
            throw new IllegalStateException("Catalog search response is not usable JSON", error);
        }
        throw new IllegalStateException("Catalog search did not return product: " + productName);
    }

    public record CatalogScreen(String productName, String productId,
                                ApiExecutionResult search, ApiExecutionResult detail) { }
}
