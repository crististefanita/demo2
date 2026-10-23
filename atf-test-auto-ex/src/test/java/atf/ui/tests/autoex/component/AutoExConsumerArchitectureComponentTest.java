package atf.ui.tests.autoex.component;

import atf.api.testkit.AtfComponentTest;
import atf.ui.tests.autoex.e2e.AutoExPurchaseUiApiE2eTest;
import atf.ui.tests.autoex.e2e.AutoExPurchaseUiE2eTest;
import atf.ui.tests.autoex.e2e.AutoExPurchaseUiFinalScreenshotE2eTest;
import atf.ui.tests.autoex.suites.AutoExComponentSuite;
import atf.ui.tests.autoex.suites.AutoExPurchaseSuite;
import org.junit.jupiter.api.Test;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.SelectPackages;

import java.util.List;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
    void mainSourceIsConsumerRuntimeAndOwnsTheTypedGherkinBoundary() throws IOException {
        Path moduleRoot = locateModuleRoot();
        Path mainSource = moduleRoot.resolve("src/main/java/atf/ui/autoex");
        Path journey = mainSource.resolve("support/AutoExPurchaseJourney.java");
        String journeySource = Files.readString(journey);

        assertTrue(Files.isRegularFile(mainSource.resolve("config/AutoExConfigurationLoader.java")));
        assertTrue(Files.isRegularFile(mainSource.resolve("page/AccountPage.java")));
        assertTrue(Files.isRegularFile(mainSource.resolve("locator/AutoExSelectors.java")));
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
