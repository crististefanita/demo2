package atf.ui.tests.extent.support.configurable;

import java.util.List;

/** Data-only category cardinality cases used by the browser proof. */
public enum CategoryCardinalityFixture {
    ZERO(0, List.of()),
    ONE(1, List.of("category-one")),
    MANY(3, List.of("category-a", "category-b", "category-c"));

    private final int expectedCategoryCount;
    private final List<String> tags;

    CategoryCardinalityFixture(int expectedCategoryCount, List<String> tags) {
        this.expectedCategoryCount = expectedCategoryCount;
        this.tags = tags;
    }

    public int expectedCategoryCount() {
        return expectedCategoryCount;
    }

    public List<String> tags() {
        return tags;
    }

    public String gherkinProposition() {
        return switch (this) {
            case ZERO -> "GIVEN an Extent report has no category tags WHEN the Categories navigation is inspected "
                    + "THEN the category navigation is hidden and the empty Categories view remains inactive";
            case ONE -> "GIVEN an Extent report has one category tag WHEN its category filter is selected "
                    + "THEN the category count, linked scenario and filtered state reconcile";
            case MANY -> "GIVEN an Extent report has multiple category tags WHEN each category filter is selected "
                    + "THEN every category count, linked scenario and filtered state reconcile";
        };
    }
}
