package atf.api.tests.support.console;

import atf.api.core.reporting.console.AtfConsoleEvent;
import atf.api.core.reporting.console.AtfConsoleSink;
import atf.api.core.reporting.contract.AtfReportStatus;

import java.util.Objects;

import static atf.api.reporting.console.AtfConsoleEvent.code;
import static atf.api.reporting.console.AtfConsoleEvent.completed;
import static atf.api.reporting.console.AtfConsoleEvent.detail;
import static atf.api.reporting.console.AtfConsoleEvent.started;

/**
 * Test-composition adapter for the two renderer-neutral console ports.
 *
 * <p>The adapter belongs to {@code atf-reporting-tests}, which is the
 * composition root that is allowed to consume both ports. Neither standalone
 * production module depends on the other, and Practice Shop does not own a
 * cross-module conversion.</p>
 */
final class AtfConsoleBridge {
    private AtfConsoleBridge() {
    }

    static atf.api.reporting.console.AtfConsoleSink toReportingSink(AtfConsoleSink coreSink) {
        Objects.requireNonNull(coreSink, "coreSink");
        return event -> coreSink.publish(toCoreEvent(event));
    }

    static AtfConsoleSink toCoreSink(atf.api.reporting.console.AtfConsoleSink reportingSink) {
        Objects.requireNonNull(reportingSink, "reportingSink");
        return event -> reportingSink.publish(toReportingEvent(event));
    }

    private static AtfConsoleEvent toCoreEvent(atf.api.reporting.console.AtfConsoleEvent event) {
        Objects.requireNonNull(event, "event");
        return switch (event.type()) {
            case START -> AtfConsoleEvent.started(coreScope(event.scope()), event.name());
            case DETAIL -> AtfConsoleEvent.detail(coreScope(event.scope()), event.detailName(), event.detailValue());
            case CODE -> AtfConsoleEvent.code(coreScope(event.scope()), event.detailName(), event.detailValue());
            case COMPLETE -> AtfConsoleEvent.completed(coreScope(event.scope()), event.name(),
                    coreStatus(event.status()), event.durationMillis());
        };
    }

    private static atf.api.reporting.console.AtfConsoleEvent toReportingEvent(AtfConsoleEvent event) {
        Objects.requireNonNull(event, "event");
        return switch (event.type()) {
            case START -> started(reportingScope(event.scope()), event.name());
            case DETAIL -> detail(reportingScope(event.scope()), event.detailName(), event.detailValue());
            case CODE -> code(reportingScope(event.scope()), event.detailName(), event.detailValue());
            case COMPLETE -> completed(reportingScope(event.scope()), event.name(),
                    reportingStatus(event.status()), event.durationMillis());
        };
    }

    private static AtfConsoleEvent.Scope coreScope(
            atf.api.reporting.console.AtfConsoleEvent.Scope scope) {
        return Enum.valueOf(AtfConsoleEvent.Scope.class, scope.name());
    }

    private static atf.api.reporting.console.AtfConsoleEvent.Scope reportingScope(AtfConsoleEvent.Scope scope) {
        return Enum.valueOf(atf.api.reporting.console.AtfConsoleEvent.Scope.class, scope.name());
    }

    private static AtfReportStatus coreStatus(atf.api.reporting.contract.AtfReportStatus status) {
        return status == null ? null : Enum.valueOf(AtfReportStatus.class, status.name());
    }

    private static atf.api.reporting.contract.AtfReportStatus reportingStatus(AtfReportStatus status) {
        return status == null ? null
                : Enum.valueOf(atf.api.reporting.contract.AtfReportStatus.class, status.name());
    }
}
