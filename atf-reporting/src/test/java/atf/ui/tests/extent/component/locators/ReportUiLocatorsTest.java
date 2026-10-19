package atf.ui.tests.extent.component.locators;
import atf.api.core.files.AtfPathLocator;
import atf.api.testkit.AtfComponentTest;
import atf.ui.tests.extent.support.locators.ReportUiLocators;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@AtfComponentTest
class ReportUiLocatorsTest {
    private static final Path TEST_SOURCE_ROOT = AtfPathLocator.locateAtfTestSourceRoot(
            Path.of(System.getProperty("user.dir", ".")), Path.of("atf", "ui", "tests"));

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
        Path source = TEST_SOURCE_ROOT.resolve(
                "atf/ui/tests/extent/e2e/section/SelectedApiTestsReportUiTest.java");
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
        Path source = TEST_SOURCE_ROOT.resolve(
                "atf/ui/tests/extent/support/locators/ReportLocatorSupport.java");
        String locatorSource = Files.readString(source);

        assertTrue(locatorSource.contains(".node[id='"),
                "structural actions must be anchored to the rendered node id in the shared support seam");
        assertFalse(Files.readString(TEST_SOURCE_ROOT.resolve(
                "atf/ui/tests/extent/support/locators/ReportUiLocators.java")).contains(
                "new AtfLocator(header, \"div.card-header[role='button']\")"),
                "a repeated generic header selector cannot be used as replay evidence");
    }

}
