package atf.api.reporting.ui;
import atf.api.testkit.AtfUnitTest;
import atf.api.reporting.ui.UiEvidencePublisher;
import atf.api.reporting.ui.UiExtentReportSession;
import atf.api.reporting.ui.UiPhaseLifecycle;
import atf.api.reporting.ui.UiScenarioLifecycle;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Guards the lifecycle composition boundary without changing the public facade. */
@AtfUnitTest
class UiExtentReportSessionCompositionTest {
    @Test
    void publicFacadeDelegatesLifecycleStateToFocusedCollaborators() throws Exception {
        String source = Files.readString(sourceFile("atf/api/reporting/ui/UiExtentReportSession.java"));

        assertAll(
                () -> assertTrue(source.contains("UiScenarioLifecycle")),
                () -> assertTrue(source.contains("UiPhaseLifecycle")),
                () -> assertFalse(source.contains("new UiEvidencePublisher")),
                () -> assertFalse(source.contains("delegateScenario.startPhase")),
                () -> assertTrue(source.contains("lifecycle.screenValidationAsMedia")));
    }

    @Test
    void lifecycleCollaboratorsOwnCompletionGuardsAndEvidencePublication() throws Exception {
        String scenario = Files.readString(sourceFile("atf/api/reporting/ui/UiScenarioLifecycle.java"));
        String phase = Files.readString(sourceFile("atf/api/reporting/ui/UiPhaseLifecycle.java"));

        assertAll(
                () -> assertTrue(scenario.contains("UI scenario is already complete")),
                () -> assertTrue(scenario.contains("delegate.complete")),
                () -> assertTrue(phase.contains("UI phase is already complete")),
                () -> assertTrue(phase.contains("publishAction")),
                () -> assertTrue(phase.contains("publishFailure")),
                () -> assertFalse(phase.contains("Page")));
    }

    private static Path sourceFile(String relativePath) {
        Path current = Path.of(System.getProperty("user.dir", "."))
                .toAbsolutePath().normalize();
        while (current != null) {
            Path moduleSource = current.resolve("src/main/java").resolve(relativePath);
            if (Files.isRegularFile(moduleSource)) return moduleSource;
            Path reactorSource = current.resolve("atf-reporting/src/main/java").resolve(relativePath);
            if (Files.isRegularFile(reactorSource)) return reactorSource;
            current = current.getParent();
        }
        throw new IllegalStateException("Cannot locate reporting source: " + relativePath);
    }
}
