package atf.ui.tests.autoex.component;

import atf.api.testkit.AtfComponentTest;
import atf.ui.tests.autoex.e2e.AutoExPurchaseUiApiE2eTest;
import atf.ui.tests.autoex.e2e.AutoExPurchaseUiE2eTest;
import atf.ui.tests.autoex.e2e.AutoExPurchaseUiFinalScreenshotE2eTest;
import atf.ui.tests.autoex.suites.AutoExComponentSuite;
import atf.ui.tests.autoex.suites.AutoExApiLiveSuite;
import atf.ui.tests.autoex.suites.AutoExPurchaseSuite;
import atf.ui.tests.autoex.api.AutoExApiLiveContractTest;
import org.junit.jupiter.api.Test;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.util.List;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Protects component/E2E entry-point boundaries and explicit suite composition. */
@AtfComponentTest
class AutoExConsumerArchitectureComponentTest {
    @Test
    void componentSuiteSelectsOnlyOfflineComponentPackage() {
        SelectPackages selection = AutoExComponentSuite.class.getAnnotation(SelectPackages.class);
        assertEquals(List.of("atf.ui.tests.autoex.component"), List.of(selection.value()));
    }

    @Test
    void purchaseSuiteSelectsExactlyTheThreeEvidenceVariants() {
        SelectClasses selection = AutoExPurchaseSuite.class.getAnnotation(SelectClasses.class);
        assertEquals(List.of(AutoExPurchaseUiE2eTest.class, AutoExPurchaseUiApiE2eTest.class,
                        AutoExPurchaseUiFinalScreenshotE2eTest.class), List.of(selection.value()));
        assertTrue(List.of(selection.value()).stream().allMatch(type -> type.getPackageName()
                .equals("atf.ui.tests.autoex.e2e")));
    }

    @Test
    void liveApiSuiteIsSeparateAndRequiresExplicitOptIn() {
        SelectClasses selection = AutoExApiLiveSuite.class.getAnnotation(SelectClasses.class);
        EnabledIfSystemProperty optIn = AutoExApiLiveContractTest.class
                .getAnnotation(EnabledIfSystemProperty.class);

        assertEquals(List.of(AutoExApiLiveContractTest.class), List.of(selection.value()));
        assertEquals("atf.autoex.api.live.enabled", optIn.named());
        assertEquals("true", optIn.matches());
        assertTrue(AutoExApiLiveContractTest.class.getPackageName().equals("atf.ui.tests.autoex.api"));
    }

    @Test
    void mainSourceIsConsumerRuntimeAndOwnsTheTypedGherkinBoundary() throws IOException {
        Path moduleRoot = locateModuleRoot();
        Path mainSource = moduleRoot.resolve("src/main/java/atf/ui/autoex");
        Path journey = mainSource.resolve("support/journey/AutoExPurchaseJourney.java");
        Path session = mainSource.resolve("support/journey/AutoExPurchaseSession.java");
        String journeySource = Files.readString(journey);
        String sessionSource = Files.readString(session);

        assertTrue(Files.isRegularFile(moduleRoot.resolve("src/test/resources/atf-auto-ex.yaml")),
                "the site-specific E2E profile belongs to test resources");
        assertFalse(Files.exists(moduleRoot.resolve("src/main/resources/atf-auto-ex.yaml")),
                "the E2E fixture must not be packaged as a production resource");
        assertTrue(Files.isRegularFile(mainSource.resolve("config/AutoExConfigurationLoader.java")));
        assertTrue(Files.isRegularFile(mainSource.resolve("page/AccountPage.java")));
        assertTrue(Files.isRegularFile(mainSource.resolve("locator/AutoExSelectors.java")));
        assertTrue(sessionSource.contains("com.microsoft.playwright.Page"),
                "the consumer runtime port may expose its browser capability to AutoEx page objects");
        assertTrue(sessionSource.contains("atf.api.reporting.contract.AtfAssertion"),
                "the consumer runtime port may expose reporting assertions to the AutoEx journey");
        assertFalse(sessionSource.contains("org.junit") || sessionSource.contains("atf.api.testkit"),
                "JUnit lifecycle and generic test-kit concerns belong in test scope, not in the runtime port");
        assertTrue(journeySource.contains("AutoExPurchaseTraceStep."));
        assertTrue(!journeySource.contains("checkpoint(AtfGherkinKeyword"),
                "business Gherkin titles must come from the typed phrase catalog");
        try (Stream<Path> sources = Files.walk(mainSource)) {
            assertTrue(sources.filter(path -> path.toString().endsWith(".java"))
                    .noneMatch(AutoExConsumerArchitectureComponentTest::importsTestFramework),
                    "JUnit and ATF test-kit dependencies stay outside main sources");
        }
    }

    private static boolean importsTestFramework(Path source) {
        try {
            String text = Files.readString(source);
            return text.contains("org.junit") || text.contains("atf.api.testkit");
        } catch (IOException error) {
            throw new IllegalStateException("Cannot inspect AutoEx main source: " + source, error);
        }
    }

    private static Path locateModuleRoot() {
        Path candidate = Path.of(System.getProperty("user.dir", ".")).toAbsolutePath().normalize();
        while (candidate != null) {
            if (Files.isDirectory(candidate.resolve("src/main/java/atf/ui/autoex"))) return candidate;
            candidate = candidate.getParent();
        }
        throw new IllegalStateException("Cannot locate atf-test-auto-ex module root");
    }
}
