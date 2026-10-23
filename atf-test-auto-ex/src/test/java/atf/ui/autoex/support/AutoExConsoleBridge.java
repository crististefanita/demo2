package atf.ui.autoex.support;

import atf.api.core.reporting.console.AtfConsoleSink;

/** Compatibility seam; event conversion is owned by atf-console. */
final class AutoExConsoleBridge {
    private AutoExConsoleBridge() { }

    static atf.api.reporting.console.AtfConsoleSink reportingSink(AtfConsoleSink console) {
        return atf.api.console.reporting.AtfReportingConsoleSinkAdapter.toReportingSink(console);
    }
}
