package atf.ui.autoex.support;

import atf.api.core.reporting.console.AtfConsoleEvent;
import atf.api.core.reporting.console.AtfConsoleSink;

import java.util.Objects;

/** Test-only composition of the independent Reporting and Core console ports. */
final class AutoExConsoleBridge {
    private AutoExConsoleBridge() { }

    static atf.api.reporting.console.AtfConsoleSink reportingSink(AtfConsoleSink console) {
        Objects.requireNonNull(console, "console");
        return event -> console.publish(AtfConsoleEvent.fromExternalEnums(
                event.type(), event.scope(), event.name(), event.status(),
                event.durationMillis(), event.detailName(), event.detailValue()));
    }
}
