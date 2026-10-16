package atf.ui.tests.extent.flows.inspection;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Guards the separation between duration contract, projection observation and
 * responsive badge geometry; the facade must remain orchestration-only.
 */
class ApiReportDurationArchitectureTest {
    @Test
    void facadeDelegatesDomAndResponsiveResponsibilitiesToDedicatedSeams() throws Exception {
        Path root = Path.of(ApiReportDurationArchitectureTest.class
                .getProtectionDomain().getCodeSource().getLocation().toURI())
                .resolve("../../src/main/java/atf/ui/tests/extent/flows/inspection").normalize();
        String facade = Files.readString(root.resolve("ApiReportDurationInspector.java"));
        assertTrue(facade.contains("ApiReportDurationDomInspector"));
        assertTrue(facade.contains("ApiReportDurationResponsiveInspector"));
        assertTrue(facade.lines().count() < 100,
                "duration facade should remain orchestration-only");
        assertTrue(Files.isRegularFile(root.resolve("ApiReportDurationDomInspector.java")));
        assertTrue(Files.isRegularFile(root.resolve("ApiReportDurationResponsiveInspector.java")));
    }
}
