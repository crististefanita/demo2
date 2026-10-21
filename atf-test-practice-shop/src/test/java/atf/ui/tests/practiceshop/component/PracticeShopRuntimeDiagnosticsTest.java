package atf.ui.tests.practiceshop.component;

import atf.api.reporting.ui.network.ReportNetworkInspector;
import atf.api.testkit.AtfUnitTest;
import atf.ui.practiceshop.support.PracticeShopRuntimeDiagnostics;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@AtfUnitTest
class PracticeShopRuntimeDiagnosticsTest {
    @Test
    void convertsRuntimeFactsToTheGenericNetworkVocabularyWithoutUrlNoise() {
        var diagnostics = List.of(
                new PracticeShopRuntimeDiagnostics.Diagnostic(
                        "response", "GET", "https://shop.test/users/me", "/users/me", 401, "HTTP 401"),
                new PracticeShopRuntimeDiagnostics.Diagnostic(
                        "console", "GET", "https://shop.test/users/me", "/users/me", 401, "Unauthorized"));

        var converted = PracticeShopRuntimeDiagnostics.toNetworkDiagnostics(diagnostics);
        var aggregate = ReportNetworkInspector.aggregate(List.of(), List.of(), converted);

        assertEquals(1, aggregate.diagnostics().size());
        assertEquals(2, aggregate.diagnostics().getFirst().count());
        assertEquals(List.of("response", "console"), aggregate.diagnostics().getFirst().sources());
        assertTrue(converted.stream().allMatch(item -> !item.toString().contains("shop.test")));
    }
}
