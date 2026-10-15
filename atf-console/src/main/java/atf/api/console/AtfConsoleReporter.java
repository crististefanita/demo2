package atf.api.console;

import atf.api.core.reporting.AtfConsoleEvent;
import atf.api.core.reporting.AtfConsoleSink;

import java.io.PrintStream;
import java.util.Objects;

/**
 * Stream adapter for the ATF console projection.
 *
 * <p>Configuration, stream capability detection, lifecycle indentation, and
 * event formatting are deliberately delegated to independent collaborators.
 * Console diagnostics remain best-effort and can never change a test verdict.</p>
 */
public final class AtfConsoleReporter implements AtfConsoleSink {
    private static final String PREFIX = "[ATF] ";

    private final PrintStream output;
    private final ConsoleSettings settings;
    private final AtfConsoleFormatter formatter;
    private final ConsoleLifecycleState lifecycle = new ConsoleLifecycleState();

    public AtfConsoleReporter(PrintStream output, ConsoleSettings settings) {
        this.output = Objects.requireNonNull(output, "output");
        this.settings = Objects.requireNonNull(settings, "settings");
        this.formatter = new AtfConsoleFormatter(ConsoleOutputStyle.forStream(output));
    }

    @Override
    public synchronized void publish(AtfConsoleEvent event) {
        Objects.requireNonNull(event, "event");
        if (!settings.enabled()) return;
        try {
            int eventDepth = lifecycle.depthFor(event.type());
            for (String line : formatter.format(event, settings.detailsEnabled())) {
                output.println(PREFIX + "  ".repeat(Math.max(0, eventDepth)) + line);
            }
        } catch (RuntimeException ignored) {
            // Console output is diagnostic; it must never change a test verdict.
        }
    }
}
