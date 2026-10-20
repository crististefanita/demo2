package atf.api.reporting.html.script;
import atf.api.testkit.AtfUnitTest;

import atf.api.reporting.support.TestReportPaths;
import atf.api.reporting.html.HtmlReportSession;
import atf.api.reporting.configuration.ReportingSettings;
import atf.api.reporting.console.AtfConsoleSink;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@AtfUnitTest
class HtmlReportNavigationScriptTest {
    @Test
    void standardScriptOwnsOnlyFallbackReportNavigation() {
        String script = HtmlReportNavigationScript.standard().source();

        assertAll(
                () -> assertNotNull(script),
                () -> assertTrue(script.contains("document.querySelectorAll('.tab')")),
                () -> assertTrue(script.contains("history.replaceState")),
                () -> assertTrue(script.contains("activate('overview')")),
                () -> assertTrue(script.contains("activate('tests')")));
    }

    @Test
    void sessionAcceptsAReplaceableScriptThroughItsAssetPort() {
        var session = new HtmlReportSession(
                TestReportPaths.report("navigation-port.html"),
                ReportingSettings.defaults(),
                AtfConsoleSink.noOp(),
                () -> "window.__atfNavigationPort = true;");

        assertTrue(session.render().contains("window.__atfNavigationPort = true;"));
    }
}
