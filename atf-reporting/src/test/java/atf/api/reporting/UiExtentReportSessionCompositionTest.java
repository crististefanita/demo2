package atf.api.reporting;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Guards the lifecycle composition boundary without changing the public facade. */
class UiExtentReportSessionCompositionTest {
    @Test
    void publicFacadeDelegatesLifecycleStateToFocusedCollaborators() throws Exception {
        String source = Files.readString(Path.of(
                "src/main/java/atf/api/reporting/UiExtentReportSession.java"));

        assertAll(
                () -> assertTrue(source.contains("UiScenarioLifecycle")),
                () -> assertTrue(source.contains("UiPhaseLifecycle")),
                () -> assertFalse(source.contains("new UiEvidencePublisher")),
                () -> assertFalse(source.contains("delegateScenario.startPhase")),
                () -> assertTrue(source.contains("lifecycle.screenValidationAsMedia")));
    }

    @Test
    void lifecycleCollaboratorsOwnCompletionGuardsAndEvidencePublication() throws Exception {
        String scenario = Files.readString(Path.of(
                "src/main/java/atf/api/reporting/UiScenarioLifecycle.java"));
        String phase = Files.readString(Path.of(
                "src/main/java/atf/api/reporting/UiPhaseLifecycle.java"));

        assertAll(
                () -> assertTrue(scenario.contains("UI scenario is already complete")),
                () -> assertTrue(scenario.contains("delegate.complete")),
                () -> assertTrue(phase.contains("UI phase is already complete")),
                () -> assertTrue(phase.contains("publishAction")),
                () -> assertTrue(phase.contains("publishFailure")),
                () -> assertFalse(phase.contains("Page")));
    }
}
