package atf.ui.tests.practiceshop.support;

import atf.api.core.reporting.console.AtfConsoleEvent;
import atf.api.core.reporting.console.AtfConsoleSink;

import java.nio.file.Path;
import java.util.Objects;

/** Test composition adapter between the console and Reporting event ports. */
final class ShopConsoleBridge {
    private ShopConsoleBridge() { }

    static atf.api.reporting.console.AtfConsoleSink reportingSink(AtfConsoleSink console) {
        Objects.requireNonNull(console, "console");
        return event -> console.publish(toCoreEvent(event));
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

    private static AtfConsoleEvent.Scope coreScope(atf.api.reporting.console.AtfConsoleEvent.Scope scope) {
        return Enum.valueOf(AtfConsoleEvent.Scope.class, scope.name());
    }

    private static atf.api.core.reporting.contract.AtfReportStatus coreStatus(
            atf.api.reporting.contract.AtfReportStatus status) {
        return status == null ? null : Enum.valueOf(
                atf.api.core.reporting.contract.AtfReportStatus.class, status.name());
    }
}
