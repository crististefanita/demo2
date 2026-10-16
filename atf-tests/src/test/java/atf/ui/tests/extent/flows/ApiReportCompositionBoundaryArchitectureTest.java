package atf.ui.tests.extent.flows;

import atf.ui.tests.extent.composition.ApiReportAuditSetup;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Protects the API/report composition root from drifting back into UI flows. */
class ApiReportCompositionBoundaryArchitectureTest {
    @Test
    void compositionRootIsPrimaryAndHistoricalNameIsOnlyCompatibilityFacade() throws Exception {
        assertEquals(ApiReportAuditSetup.class, ApiReportSetupFlow.class.getSuperclass());
        Path source = sourceRoot().resolve(Path.of("composition", "ApiReportAuditSetup.java"));
        String text = Files.readString(source);
        assertTrue(text.contains("class ApiReportAuditSetup"));
        assertTrue(!text.contains("import com.microsoft.playwright"));
        assertTrue(!text.contains("page.locator("));
        String facade = Files.readString(sourceRoot().resolve(Path.of("flows", "ApiReportSetupFlow.java")));
        assertTrue(facade.contains("ApiReportAuditSetup"));
        assertTrue(!facade.contains("ExtentReportSession.openConfigured"));
    }

    private static Path sourceRoot() throws Exception {
        return Path.of(ApiReportCompositionBoundaryArchitectureTest.class
                .getProtectionDomain().getCodeSource().getLocation().toURI())
                .resolve("../../src/main/java/atf/ui/tests/extent").normalize();
    }
}
