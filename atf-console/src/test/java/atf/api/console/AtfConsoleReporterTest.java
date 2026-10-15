package atf.api.console;

import atf.api.core.reporting.AtfConsoleEvent;
import atf.api.core.reporting.AtfReportStatus;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AtfConsoleReporterTest {
    @Test
    void rendersNestedLifecycleWithStatusAndMilliseconds() {
        var bytes = new ByteArrayOutputStream();
        var reporter = new AtfConsoleReporter(new PrintStream(bytes, true, StandardCharsets.UTF_8),
                new ConsoleSettings(true, false));

        reporter.publish(AtfConsoleEvent.started(AtfConsoleEvent.Scope.SCENARIO, "PostsApiTests"));
        reporter.publish(AtfConsoleEvent.started(AtfConsoleEvent.Scope.PHASE, "TEST BODY"));
        reporter.publish(AtfConsoleEvent.started(AtfConsoleEvent.Scope.REQUEST,
                "GET /public/v2/posts"));
        reporter.publish(AtfConsoleEvent.completed(AtfConsoleEvent.Scope.REQUEST,
                "GET /public/v2/posts", AtfReportStatus.PASS, 16));
        reporter.publish(AtfConsoleEvent.completed(AtfConsoleEvent.Scope.PHASE,
                "TEST BODY", AtfReportStatus.PASS, 18));
        reporter.publish(AtfConsoleEvent.completed(AtfConsoleEvent.Scope.SCENARIO,
                "PostsApiTests", AtfReportStatus.PASS, 20));

        String output = bytes.toString(StandardCharsets.UTF_8);
        assertAll(
                () -> assertTrue(output.contains("[ATF] ▶ PostsApiTests")),
                () -> assertTrue(output.contains("[ATF]     ▶ GET /public/v2/posts")),
                () -> assertTrue(output.contains("[ATF]   ✅ PASS: TEST BODY (18 ms)")),
                () -> assertTrue(output.contains("[ATF] ✅ PASS: PostsApiTests (20 ms)")),
                () -> assertFalse(output.contains("null")));
    }

    @Test
    void switchesToSecondsOnlyAboveTheThreeHundredMillisecondThreshold() {
        var bytes = new ByteArrayOutputStream();
        var reporter = new AtfConsoleReporter(new PrintStream(bytes, true, StandardCharsets.UTF_8),
                new ConsoleSettings(true, false));

        reporter.publish(AtfConsoleEvent.completed(AtfConsoleEvent.Scope.REQUEST,
                "short", AtfReportStatus.PASS, 300));
        reporter.publish(AtfConsoleEvent.completed(AtfConsoleEvent.Scope.REQUEST,
                "long", AtfReportStatus.PASS, 301));
        reporter.publish(AtfConsoleEvent.completed(AtfConsoleEvent.Scope.REQUEST,
                "second", AtfReportStatus.PASS, 1_000));

        String output = bytes.toString(StandardCharsets.UTF_8);
        assertAll(
                () -> assertTrue(output.contains("Call completed (300 ms)")),
                () -> assertTrue(output.contains("Call completed (0.301 s)")),
                () -> assertTrue(output.contains("Call completed (1 s)")));
    }

    @Test
    void keepsDetailsOptInAndCanBeDisabledEntirely() {
        var bytes = new ByteArrayOutputStream();
        var reporter = new AtfConsoleReporter(new PrintStream(bytes, true, StandardCharsets.UTF_8),
                new ConsoleSettings(true, false));
        reporter.publish(AtfConsoleEvent.detail(AtfConsoleEvent.Scope.REQUEST,
                "Authorization", "Bearer ***REDACTED***"));
        String compact = bytes.toString(StandardCharsets.UTF_8);
        assertFalse(compact.contains("Authorization"));

        var disabled = new ByteArrayOutputStream();
        var silent = new AtfConsoleReporter(new PrintStream(disabled, true, StandardCharsets.UTF_8),
                new ConsoleSettings(false, true));
        silent.publish(AtfConsoleEvent.started(AtfConsoleEvent.Scope.SCENARIO, "silent"));
        assertTrue(disabled.toString(StandardCharsets.UTF_8).isEmpty());
    }

    @Test
    void rendersRedactedDetailsOnlyWhenExplicitlyEnabled() {
        var bytes = new ByteArrayOutputStream();
        var reporter = new AtfConsoleReporter(new PrintStream(bytes, true, StandardCharsets.UTF_8),
                new ConsoleSettings(true, true));
        reporter.publish(AtfConsoleEvent.detail(AtfConsoleEvent.Scope.REQUEST,
                "Authorization", "Bearer ***REDACTED***"));

        assertTrue(bytes.toString(StandardCharsets.UTF_8)
                .contains("• Authorization=Bearer ***REDACTED***"));
    }

    @Test
    void rendersPrettyBodyAsAnIndentedDiagnosticBlockWhenDetailsAreEnabled() {
        var bytes = new ByteArrayOutputStream();
        var reporter = new AtfConsoleReporter(new PrintStream(bytes, true, StandardCharsets.UTF_8),
                new ConsoleSettings(true, true));

        reporter.publish(AtfConsoleEvent.code(AtfConsoleEvent.Scope.REQUEST,
                "response.body", "{\n  \"id\": 1\n}"));

        String output = bytes.toString(StandardCharsets.UTF_8);
        assertAll(
                () -> assertTrue(output.contains("• response.body")),
                () -> assertTrue(output.contains("  {")),
                () -> assertTrue(output.contains("    \"id\": 1")),
                () -> assertFalse(output.contains("null")));
    }

    @Test
    void normalizesReportTypographyForConsoleOutput() {
        var bytes = new ByteArrayOutputStream();
        var reporter = new AtfConsoleReporter(new PrintStream(bytes, true, StandardCharsets.UTF_8),
                new ConsoleSettings(true, false));

        reporter.publish(AtfConsoleEvent.started(AtfConsoleEvent.Scope.SCENARIO,
                "Users API \u2014 setup \u2192 test"));

        assertTrue(bytes.toString(StandardCharsets.UTF_8)
                .contains("[ATF] ▶ Users API - setup -> test"));
    }

    @Test
    void formatsSingleLineBodyAndValidationLikeReferenceConsoleFlow() {
        var bytes = new ByteArrayOutputStream();
        var reporter = new AtfConsoleReporter(new PrintStream(bytes, true, StandardCharsets.UTF_8),
                new ConsoleSettings(true, true));

        reporter.publish(AtfConsoleEvent.started(AtfConsoleEvent.Scope.REQUEST,
                "POST /public/v2/users"));
        reporter.publish(AtfConsoleEvent.detail(AtfConsoleEvent.Scope.REQUEST, "method", "POST"));
        reporter.publish(AtfConsoleEvent.code(AtfConsoleEvent.Scope.REQUEST,
                "request.body", "{\"name\":\"ATF user\"}"));
        reporter.publish(AtfConsoleEvent.completed(AtfConsoleEvent.Scope.REQUEST,
                "POST /public/v2/users", AtfReportStatus.PASS, 12));
        reporter.publish(AtfConsoleEvent.started(AtfConsoleEvent.Scope.VALIDATION,
                "Validate status: expect 201"));
        reporter.publish(AtfConsoleEvent.detail(AtfConsoleEvent.Scope.VALIDATION, "actual", "201"));
        reporter.publish(AtfConsoleEvent.completed(AtfConsoleEvent.Scope.VALIDATION,
                "Validate status: expect 201", AtfReportStatus.PASS, 1));

        String output = bytes.toString(StandardCharsets.UTF_8);
        assertTrue(output.contains("▶ POST /public/v2/users"));
        assertTrue(output.contains("• method=POST"));
        assertTrue(output.contains("• request.body={\"name\":\"ATF user\"}"));
        assertTrue(output.contains("✅ PASS: Call completed"));
        assertTrue(output.contains("▶ Validate status: expect 201"));
        assertTrue(output.contains("• actual=201"));
        assertTrue(output.contains("✅ PASS: Validated"));
    }
}
