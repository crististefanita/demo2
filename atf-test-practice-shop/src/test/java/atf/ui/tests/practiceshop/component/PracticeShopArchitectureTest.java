package atf.ui.tests.practiceshop.component;

import atf.api.core.files.AtfFileTree;
import atf.api.core.files.AtfPathLocator;
import atf.api.testkit.AtfComponentTest;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Keeps the external test suite honest: tests/pages/flows cannot own CSS. */
@AtfComponentTest
class PracticeShopArchitectureTest {
    @Test
    void pageObjectsFlowsAndTestsDoNotBypassTheLocatorBoundary() throws IOException {
        Path root = locateModuleRoot();
        for (Path path : sourceFiles(root)) {
            String source = Files.readString(path);
            assertFalse(source.contains("page.locator("),
                    "raw page locator escaped boundary: " + path);
            assertFalse(source.contains("getByText("),
                    "text selector escaped boundary: " + path);
            assertFalse(source.contains("getByRole("),
                    "role selector escaped boundary: " + path);
            assertFalse(source.matches("(?s).*pressKey\\(page,\\s*\"[^\"]+\",\\s*\"[^\"]+\".*"),
                    "keyboard action must receive its target from the locator facade: " + path);
        }
    }

    @Test
    void knownApplicationLocatorsUseExplicitElementAttributeSelectors() throws IOException {
        Path source = locateModuleRoot().resolve("src/main/java/atf/ui/practiceshop/locators/PracticeShopLocators.java");
        String locators = Files.readString(source);

        assertTrue(locators.contains("input[id='email']"));
        assertTrue(locators.contains("input[id='search-query']"));
        assertTrue(locators.contains("*[data-test='login-submit']"));
        assertFalse(locators.contains("input#"), "ID shorthand must not be the default selector form");
        assertFalse(locators.contains("= \"[data-test='login-submit']\""),
                "known button tag must not be hidden behind a generic attribute selector");
        assertTrue(locators.contains("external Practice Shop contract guarantees data-test"),
                "wildcard selectors require a local external-contract justification");
    }

    @Test
    void reusableApplicationSupportDoesNotRemainUnderTheTestSourcePackage() throws IOException {
        Path moduleRoot = locateModuleRoot();
        for (Path path : AtfFileTree.regularFiles(
                moduleRoot.resolve("src/test/java/atf/ui/tests/practiceshop"),
                List.of(Path.of(".")), path -> path.getFileName().toString().endsWith(".java"))) {
            String fileName = path.getFileName().toString();
            assertFalse(fileName.endsWith("Page.java") || fileName.endsWith("Flow.java")
                            || fileName.equals("PracticeShopLocators.java")
                            || fileName.equals("PracticeShopE2eContext.java"),
                    "reusable support must live under src/main/java: " + path);
            assertTrue(fileName.endsWith("Test.java") || fileName.endsWith("TestSupport.java")
                            || fileName.endsWith("Fixture.java"),
                    "test source must be a test or an explicitly test-scoped helper: " + path);
        }
    }

    @Test
    void reusableSupportDependenciesAreAvailableToMainSources() throws IOException {
        String pom = Files.readString(locateModuleRoot().resolve("pom.xml"));
        for (String artifact : new String[]{"atf-core", "atf-console", "atf-test-kit", "atf-reporting", "atf-ui"}) {
            int dependencyStart = pom.indexOf("<artifactId>" + artifact + "</artifactId>");
            assertTrue(dependencyStart >= 0, "missing module dependency: " + artifact);
            int dependencyEnd = pom.indexOf("</dependency>", dependencyStart);
            assertTrue(dependencyEnd > dependencyStart, "malformed dependency: " + artifact);
            String dependency = pom.substring(dependencyStart, dependencyEnd);
            assertFalse(dependency.contains("<scope>test</scope>"),
                    "main reusable support cannot depend on test-scoped " + artifact);
        }
    }

    @Test
    void endToEndAssertionsMustUseObservedValues() throws IOException {
        Path e2eRoot = locateModuleRoot().resolve("src/test/java/atf/ui/tests/practiceshop/e2e");
        for (Path path : AtfFileTree.regularFiles(e2eRoot, List.of(Path.of(".")),
                candidate -> candidate.getFileName().toString().endsWith(".java"))) {
            assertObservedVerdicts(path, "E2E");
        }
    }

    @Test
    void reusableFlowAndEvidenceAssertionsMustUseObservedValues() throws IOException {
        Path moduleRoot = locateModuleRoot();
        for (Path path : AtfFileTree.regularFiles(moduleRoot,
                List.of(Path.of("src/main/java/atf/ui/practiceshop/flow"),
                        Path.of("src/main/java/atf/ui/practiceshop/support")),
                candidate -> candidate.getFileName().toString().endsWith(".java"))) {
            assertObservedVerdicts(path, "reusable Practice Shop support");
        }
    }

    @Test
    void multiJourneyE2eClassCreatesAnIsolatedBrowserContextPerTest() throws IOException {
        Path source = locateModuleRoot().resolve(
                "src/test/java/atf/ui/tests/practiceshop/e2e/PracticeShopAccountAndCatalogE2eTest.java");
        String text = Files.readString(source);
        assertTrue(text.contains("@BeforeEach"), "multi-journey E2E must open context per test");
        assertTrue(text.contains("@AfterEach"), "multi-journey E2E must close context per test");
        assertFalse(text.contains("@TestInstance"), "multi-journey E2E must not share PER_CLASS state");
        assertFalse(text.contains("@BeforeAll"), "multi-journey E2E must not open a suite browser");
    }

    private void assertObservedVerdicts(Path path, String layer) throws IOException {
        String source = Files.readString(path);
        assertFalse(source.matches("(?s).*\\bcheck\\([^;]*,\\s*true\\s*\\).*"),
                layer + " check must derive its verdict from an observed value: " + path);
        assertFalse(source.matches("(?s).*new\\s+AtfAssertion\\([^;]*,\\s*true\\s*\\).*"),
                layer + " assertion must derive its verdict from an observed value: " + path);
        if (layer.equals("E2E")) {
            assertFalse(source.contains("configuration.productName(), configuration.productName()"),
                    "E2E product assertion must not compare a value with itself: " + path);
            assertFalse(source.contains("\"My account\", \"My account\""),
                    "E2E account assertion must not compare a value with itself: " + path);
        }
    }

    private List<Path> sourceFiles(Path moduleRoot) throws IOException {
        Path locators = moduleRoot.resolve("src/main/java/atf/ui/practiceshop/locators")
                .toAbsolutePath().normalize();
        return AtfFileTree.regularFiles(moduleRoot,
                List.of(Path.of("src/main/java/atf/ui/practiceshop"),
                        Path.of("src/test/java/atf/ui/tests/practiceshop")),
                path -> path.getFileName().toString().endsWith(".java")
                        && !path.getFileName().toString().equals("PracticeShopArchitectureTest.java")
                        && !path.toAbsolutePath().normalize().startsWith(locators));
    }

    private static Path locateModuleRoot() {
        return AtfPathLocator.locateModuleRoot(
                Path.of(System.getProperty("user.dir", ".")),
                Path.of("atf-test-practice-shop"),
                Path.of("src/main/java/atf/ui/practiceshop"));
    }
}


