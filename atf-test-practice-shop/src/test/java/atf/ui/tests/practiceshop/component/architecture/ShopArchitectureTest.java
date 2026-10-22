package atf.ui.tests.practiceshop.component.architecture;

import atf.api.core.files.AtfFileTree;
import atf.api.core.files.AtfPathLocator;
import atf.api.testkit.AtfComponentTest;
import atf.ui.tests.practiceshop.api.reporting.ShopApiCallAuditTestSupport;
import atf.ui.tests.practiceshop.api.support.ShopApiCallAuditContextTestSupport;
import atf.ui.tests.practiceshop.api.support.ShopApiCallComponentTest;
import atf.ui.tests.practiceshop.suites.ShopApiCallAuditSuite;
import atf.ui.tests.practiceshop.suites.ShopComponentSuite;
import org.junit.platform.suite.api.IncludeTags;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Keeps the external test suite honest: tests/pages/flows cannot own CSS. */
@AtfComponentTest
class ShopArchitectureTest {
    @Test
    void everyClassInTheModuleUsesTheShopTypePrefix() throws IOException {
        Path moduleRoot = locateModuleRoot();
        String legacyPrefix = "Practice" + "Shop";
        String legacyTypeDeclaration = "(?s).*\\b(?:class|interface|enum|record|@interface)\\s+"
                + java.util.regex.Pattern.quote(legacyPrefix) + "\\w+.*";

        for (Path path : AtfFileTree.regularFiles(moduleRoot,
                List.of(Path.of("src/main/java"), Path.of("src/test/java")),
                candidate -> candidate.getFileName().toString().endsWith(".java"))) {
            String fileName = path.getFileName().toString();
            String source = Files.readString(path);
            assertFalse(fileName.startsWith(legacyPrefix),
                    "Java source file must use the Shop prefix: " + path);
            assertFalse(source.matches(legacyTypeDeclaration),
                    "declared Java type must use the Shop prefix: " + path);
        }
    }

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
        Path source = locateModuleRoot().resolve("src/main/java/atf/ui/practiceshop/locators/ShopLocators.java");
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
                            || fileName.equals("ShopLocators.java")
                            || fileName.equals("ShopE2eContext.java"),
                    "reusable support must live under src/main/java: " + path);
            assertTrue(fileName.endsWith("Test.java") || fileName.endsWith("TestSupport.java")
                            || fileName.endsWith("Fixture.java") || fileName.equals("ShopE2eSuite.java")
                            || fileName.equals("ShopComponentSuite.java")
                            || fileName.equals("ShopApiComponentSuite.java")
                            || fileName.equals("ShopApiCallAuditSuite.java")
                            || fileName.equals("ShopApiCallComponentTest.java"),
                    "test source must be a test or an explicitly test-scoped helper: " + path);
        }
    }

    @Test
    void shopComponentSuiteSelectsOnlyComponentAndSupportBoundaries() {
        SelectPackages packages = ShopComponentSuite.class.getAnnotation(SelectPackages.class);
        assertTrue(packages != null, "component suite must declare explicit package boundaries");
        assertTrue(List.of(packages.value()).containsAll(List.of(
                        "atf.ui.tests.practiceshop.component",
                        "atf.ui.tests.practiceshop.api.component",
                        "atf.ui.tests.practiceshop.api.network",
                        "atf.ui.tests.practiceshop.support")),
                "component suite must include shop UI/API components, network settlement, and support contracts");
        assertFalse(List.of(packages.value()).stream().anyMatch(name -> name.endsWith(".e2e")
                        || name.endsWith(".integration")),
                "component suite must not trigger live E2E or integration journeys");
    }

    @Test
    void shopAuditTypesFollowNamingAndSelectOnlyApiCallTests()
            throws ClassNotFoundException {
        List<Class<?>> auditTypes = List.of(ShopApiCallAuditTestSupport.class,
                ShopApiCallAuditContextTestSupport.class, ShopApiCallComponentTest.class,
                ShopApiCallAuditSuite.class,
                Class.forName("atf.ui.practiceshop.support.ShopE2eFailureBoundaryTest"));
        assertTrue(auditTypes.stream().allMatch(type -> type.getSimpleName().startsWith("Shop")),
                "new Practice Shop test types must use the Shop prefix from the feedback contract");

        IncludeTags includedTags = ShopApiCallAuditSuite.class.getAnnotation(IncludeTags.class);
        assertTrue(includedTags != null && List.of(includedTags.value()).equals(List.of("api-call")),
                "the API audit suite must select only explicitly API-call-audited component tests");
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
        Path root = locateModuleRoot().resolve("src/test/java/atf/ui/tests/practiceshop/e2e");
        for (String name : List.of("PurchaseE2eTest", "UiOnlyPurchaseE2eTest",
                "UiOnlySingleFlowPurchaseE2eTest", "AuthenticatedPurchaseE2eTest",
                "NewAccountPurchaseE2eTest")) {
            String text = Files.readString(root.resolve(name + ".java"));
            assertTrue(text.contains("@ShopE2eTestSupport.WithE2eHarness"),
                    name + " must use the per-method browser/report extension");
            assertTrue(text.contains("ShopE2eTestSupport.E2eHarness harness"),
                    name + " must receive its isolated harness as a method parameter");
            assertFalse(text.contains("@BeforeEach") || text.contains("@AfterEach"),
                    name + " must not duplicate the extension lifecycle");
            assertFalse(text.contains("@TestInstance") || text.contains("@BeforeAll"),
                    name + " must not share a suite browser");
        }
    }

    @Test
    void authenticatedPurchaseScenariosContinueFromSetupThroughOrderConfirmation() throws IOException {
        Path root = locateModuleRoot().resolve("src/test/java/atf/ui/tests/practiceshop/e2e");
        for (String name : List.of("NewAccountPurchaseE2eTest", "AuthenticatedPurchaseE2eTest")) {
            String source = Files.readString(root.resolve(name + ".java"));
            assertTrue(source.contains("ShopAuthenticatedAccountTestSupport::prepare"),
                    name + " must complete account setup and login before the business scenario");
            assertTrue(source.contains("purchaseConfiguredProductAuthenticated("),
                    name + " must execute the authenticated purchase journey");
            assertTrue(source.contains("journey.completePurchase(purchase);"),
                    name + " must assert the terminal purchase and publish its final screen capture");
        }
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
                        && !path.getFileName().toString().equals("ShopArchitectureTest.java")
                        && !path.toAbsolutePath().normalize().startsWith(locators));
    }

    private static Path locateModuleRoot() {
        return AtfPathLocator.locateModuleRoot(
                Path.of(System.getProperty("user.dir", ".")),
                Path.of("atf-test-practice-shop"),
                Path.of("src/main/java/atf/ui/practiceshop"));
    }
}


