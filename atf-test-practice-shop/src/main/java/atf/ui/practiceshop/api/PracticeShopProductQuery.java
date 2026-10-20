package atf.ui.practiceshop.api;

import java.util.LinkedHashMap;
import java.util.Map;

/** Immutable product filter/sort query used by component and integration tests. */
public record PracticeShopProductQuery(
        String brandId,
        String categoryId,
        Boolean rental,
        String priceRange,
        PracticeShopProductSort sort,
        Integer page) {

    public PracticeShopProductQuery {
        brandId = optional(brandId);
        categoryId = optional(categoryId);
        priceRange = optional(priceRange);
        if (page != null && page < 1) throw new IllegalArgumentException("product page must be positive");
    }

    public static PracticeShopProductQuery defaults() {
        return new PracticeShopProductQuery(null, null, null, null, null, null);
    }

    public static PracticeShopProductQuery sorted(PracticeShopProductSort sort) {
        return new PracticeShopProductQuery(null, null, null, null,
                java.util.Objects.requireNonNull(sort, "sort"), null);
    }

    public static PracticeShopProductQuery category(String categoryId) {
        return new PracticeShopProductQuery(null, categoryId, null, null, null, null);
    }

    /** Wire representation matches the public Practice Shop API contract. */
    public Map<String, String> asQuery() {
        Map<String, String> values = new LinkedHashMap<>();
        if (brandId != null) values.put("by_brand", brandId);
        if (categoryId != null) values.put("by_category", categoryId);
        if (rental != null) values.put("is_rental", rental.toString());
        if (priceRange != null) values.put("between", priceRange);
        if (sort != null) values.put("sort", sort.wireValue());
        if (page != null) values.put("page", Integer.toString(page));
        return Map.copyOf(values);
    }

    private static String optional(String value) {
        if (value == null) return null;
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
