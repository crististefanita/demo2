package atf.api.console.reporting;

import atf.api.core.reporting.console.AtfConsoleEvent;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AtfReportingConsoleSinkAdapterTest {
    @Test
    void mapsAllScopesForStartDetailAndCode() {
        List<AtfConsoleEvent> actual = new ArrayList<>();
        var sink = AtfReportingConsoleSinkAdapter.toReportingSink(actual::add);

        for (var scope : atf.api.reporting.console.AtfConsoleEvent.Scope.values()) {
            sink.publish(atf.api.reporting.console.AtfConsoleEvent.started(scope, "test"));
            sink.publish(atf.api.reporting.console.AtfConsoleEvent.detail(scope, "field", "value"));
            sink.publish(atf.api.reporting.console.AtfConsoleEvent.code(scope, "body", "{}"));
        }

        assertEquals(18, actual.size());
        int index = 0;
        for (var scope : AtfConsoleEvent.Scope.values()) {
            assertEquals(AtfConsoleEvent.started(scope, "test"), actual.get(index++));
            assertEquals(AtfConsoleEvent.detail(scope, "field", "value"), actual.get(index++));
            assertEquals(AtfConsoleEvent.code(scope, "body", "{}"), actual.get(index++));
        }
    }

    @Test
    void mapsEveryCompletionStatusAndDuration() {
        List<AtfConsoleEvent> actual = new ArrayList<>();
        var sink = AtfReportingConsoleSinkAdapter.toReportingSink(actual::add);

        for (var status : atf.api.reporting.contract.AtfReportStatus.values()) {
            sink.publish(atf.api.reporting.console.AtfConsoleEvent.completed(
                    atf.api.reporting.console.AtfConsoleEvent.Scope.VALIDATION,
                    "validation", status, 27));
        }

        assertEquals(atf.api.reporting.contract.AtfReportStatus.values().length, actual.size());
        for (int index = 0; index < actual.size(); index++) {
            assertEquals(atf.api.core.reporting.contract.AtfReportStatus.values()[index],
                    actual.get(index).status());
            assertEquals(AtfConsoleEvent.Scope.VALIDATION, actual.get(index).scope());
            assertEquals(27, actual.get(index).durationMillis());
        }
    }

    @Test
    void mapsPublishedReportAndRejectsNullInputs() {
        List<AtfConsoleEvent> actual = new ArrayList<>();
        var sink = AtfReportingConsoleSinkAdapter.toReportingSink(actual::add);
        Path report = Path.of("build", "reports", "extent.html").toAbsolutePath().normalize();

        sink.publish(atf.api.reporting.console.AtfConsoleEvent.reportPublished(report));

        assertEquals(AtfConsoleEvent.reportPublished(report), actual.getFirst());
        assertThrows(NullPointerException.class,
                () -> AtfReportingConsoleSinkAdapter.toReportingSink(null));
        assertThrows(NullPointerException.class, () -> sink.publish(null));
    }
}
