package atf.api.console.reporting;

import atf.api.core.reporting.console.AtfConsoleEvent;
import atf.api.core.reporting.console.AtfConsoleSink;

import java.nio.file.Path;
import java.util.Objects;

/** Adapts Reporting's console port to the renderer-neutral Core console port. */
public final class AtfReportingConsoleSinkAdapter {
    private AtfReportingConsoleSinkAdapter() { }

    public static atf.api.reporting.console.AtfConsoleSink toReportingSink(AtfConsoleSink console) {
        Objects.requireNonNull(console, "console");
        return event -> console.publish(toCoreEvent(Objects.requireNonNull(event, "event")));
    }

    private static AtfConsoleEvent toCoreEvent(atf.api.reporting.console.AtfConsoleEvent event) {
        return switch (event.type()) {
            case START -> AtfConsoleEvent.started(coreScope(event.scope()), event.name());
            case DETAIL -> AtfConsoleEvent.detail(coreScope(event.scope()), event.detailName(), event.detailValue());
            case CODE -> AtfConsoleEvent.code(coreScope(event.scope()), event.detailName(), event.detailValue());
            case COMPLETE -> AtfConsoleEvent.completed(coreScope(event.scope()), event.name(),
                    coreStatus(event.status()), event.durationMillis());
            case REPORT_PUBLISHED -> AtfConsoleEvent.reportPublished(Path.of(event.detailValue()));
        };
    }

    private static AtfConsoleEvent.Scope coreScope(
            atf.api.reporting.console.AtfConsoleEvent.Scope scope) {
        return AtfConsoleEvent.Scope.valueOf(scope.name());
    }

    private static atf.api.core.reporting.contract.AtfReportStatus coreStatus(
            atf.api.reporting.contract.AtfReportStatus status) {
        return status == null ? null
                : atf.api.core.reporting.contract.AtfReportStatus.valueOf(status.name());
    }
}
