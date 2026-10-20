package atf.ui.tests.practiceshop.component;

import atf.api.testkit.AtfComponentTest;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Keeps the external test suite honest: tests/pages/flows cannot own CSS. */
@AtfComponentTest
class PracticeShopArchitectureTest {
    @Test
    void pageObjectsFlowsAndTestsDoNotBypassTheLocatorBoundary() throws IOException {
        Path root = locateModuleRoot();
        try (Stream<Path> files = sourceRoots(root).flatMap(this::walkSafely)) {
            files.filter(Files::isRegularFile)
                    .filter(path -> !path.toString().contains("component"))
                    .filter(path -> !path.toString().contains("locators"))
                    .filter(path -> path.toString().endsWith(".java"))
                    .forEach(path -> {
                        try {
                            String source = Files.readString(path);
                            assertFalse(source.contains("page.locator("),
                                    "raw page locator escaped boundary: " + path);
                            assertFalse(source.contains("getByText("),
                                    "text selector escaped boundary: " + path);
                            assertFalse(source.contains("getByRole("),
                                    "role selector escaped boundary: " + path);
                            assertFalse(source.matches("(?s).*pressKey\\(page,\\s*\"[^\"]+\",\\s*\"[^\"]+\".*"),
                                    "keyboard action must receive its target from the locator facade: " + path);
                        } catch (IOException error) {
                            throw new IllegalStateException(error);
                        }
                    });
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
        Path testRoot = locateModuleRoot().resolve("src/test/java/atf/ui/tests/practiceshop");
        try (Stream<Path> files = Files.walk(testRoot)) {
            files.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java"))
                    .forEach(path -> {
                        String fileName = path.getFileName().toString();
                        assertFalse(fileName.endsWith("Page.java") || fileName.endsWith("Flow.java")
                                        || fileName.equals("PracticeShopLocators.java")
                                        || fileName.equals("PracticeShopE2eContext.java"),
                                "reusable support must live under src/main/java: " + path);
                    assertTrue(fileName.endsWith("Test.java") || fileName.endsWith("TestSupport.java")
                                    || fileName.endsWith("Fixture.java"),
                            "test source must be a test or an explicitly test-scoped helper: " + path);
                    });
        }
    }

    @Test
    void reusableSupportDependenciesAreAvailableToMainSources() throws IOException {
        String pom = Files.readString(locateModuleRoot().resolve("pom.xml"));
        for (String artifact : new String[]{"atf-core", "atf-test-kit", "atf-reporting", "atf-ui"}) {
            int dependencyStart = pom.indexOf("<artifactId>" + artifact + "</artifactId>");
            assertTrue(dependencyStart >= 0, "missing module dependency: " + artifact);
            int dependencyEnd = pom.indexOf("</dependency>", dependencyStart);
            assertTrue(dependencyEnd > dependencyStart, "malformed dependency: " + artifact);
            String dependency = pom.substring(dependencyStart, dependencyEnd);
            assertFalse(dependency.contains("<scope>test</scope>"),
                    "main reusable support cannot depend on test-scoped " + artifact);
        }
    }

    private Stream<Path> sourceRoots(Path moduleRoot) {
        return Stream.of(moduleRoot.resolve("src/main/java/atf/ui/practiceshop"),
                moduleRoot.resolve("src/test/java/atf/ui/tests/practiceshop"));
    }

    private Stream<Path> walkSafely(Path root) {
        try {
            return Files.walk(root);
        } catch (IOException error) {
            throw new IllegalStateException("Cannot scan source root: " + root, error);
        }
    }

    private static Path locateModuleRoot() {
        Path current = Path.of(System.getProperty("user.dir", "."))
                .toAbsolutePath().normalize();
        while (current != null) {
            Path candidate = current.resolve("atf-test-practice-shop");
            if (Files.isDirectory(candidate.resolve("src/main/java/atf/ui/practiceshop"))) return candidate;
            current = current.getParent();
        }
        throw new IllegalStateException("Cannot locate Practice Shop module root");
    }
}


