package atf.ui.tests.extent.locators;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReportUiLocatorsTest {
    @Test
    void buildsStableIndexedCategorySelectorsAtTheLocatorBoundary() {
        ReportUiLocators locators = new ReportUiLocators();

        assertEquals("#category-view .test-list .test-item:nth-of-type(1)",
                locators.categoryItemSelector(0));
        assertEquals("#category-view .test-list .test-item:nth-of-type(8)",
                locators.categoryItemSelector(7));
    }

    @Test
    void rejectsNegativeCategoryIndexesBeforeConstructingASelector() {
        ReportUiLocators locators = new ReportUiLocators();

        assertThrows(IllegalArgumentException.class, () -> locators.categoryItemSelector(-1));
    }

    @Test
    void apiUiScenarioKeepsSelectorConstructionInsideTheLocatorFacade() throws IOException {
        Path source = Path.of("src/test/java/atf/ui/tests/extent/configurable/UsersPostsApiReportUiTest.java");
        String testSource = Files.readString(source);

        assertFalse(testSource.contains("page.locator("),
                "API UI scenarios must obtain page locators through ReportUiLocators");
        assertFalse(testSource.contains("getByRole("),
                "API UI scenarios must obtain role locators through ReportUiLocators");
        assertFalse(testSource.contains("getByText("),
                "API UI scenarios must obtain text locators through ReportUiLocators");
    }

    @Test
    void reportActionsMustExposeAUniqueNodeIdentityInsteadOfARepeatedHeaderSelector() throws IOException {
        Path source = Path.of("src/main/java/atf/ui/tests/extent/locators/ReportUiLocators.java");
        String locatorSource = Files.readString(source);

        assertTrue(locatorSource.contains(".node[id='"),
                "structural actions must be anchored to the rendered node id");
        assertFalse(locatorSource.contains("new AtfLocator(header, \"div.card-header[role='button']\")"),
                "a repeated generic header selector cannot be used as replay evidence");
        assertFalse(locatorSource.contains("new AtfLocator(requestNodeToggle(request), \" :scope > .card-header .node\")"),
                "request actions must expose the owning request identity");
    }
}
