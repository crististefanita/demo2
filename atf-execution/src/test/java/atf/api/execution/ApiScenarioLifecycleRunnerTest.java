package atf.api.execution;

import atf.api.core.reporting.AtfReportStatus;
import atf.api.core.reporting.AtfReportPorts;
import atf.api.execution.lifecycle.ApiScenarioLifecycleRunner;
import atf.api.execution.lifecycle.ApiScenarioPlan;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.Collection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

class ApiScenarioLifecycleRunnerTest {
    @Test
    void testOnlyScenarioHasOnlyTestBody() {
        var result = new ApiScenarioLifecycleRunner().run(null, () -> { }, null);

        assertEquals(AtfReportStatus.PASS, result.status());
        assertEquals(1, result.phases().size());
        assertTrue(result.phases().containsKey(ApiScenarioLifecycleRunner.Phase.TEST_BODY));
    }

    @Test
    void setupSkipSkipsBodyButStillRunsTeardown() throws Exception {
        AtomicBoolean teardown = new AtomicBoolean();
        var result = new ApiScenarioLifecycleRunner().run(
                () -> { throw new ApiScenarioLifecycleRunner.SkipSignal("missing prerequisite"); },
                () -> { throw new IllegalStateException("body must not run after setup skip"); },
                ignored -> teardown.set(true));

        assertEquals(AtfReportStatus.SKIP, result.status());
        assertEquals(AtfReportStatus.SKIP, result.phases().get(ApiScenarioLifecycleRunner.Phase.SETUP));
        assertEquals(AtfReportStatus.SKIP, result.phases().get(ApiScenarioLifecycleRunner.Phase.TEST_BODY));
        assertEquals(AtfReportStatus.PASS, result.phases().get(ApiScenarioLifecycleRunner.Phase.TEARDOWN));
        assertTrue(teardown.get());
    }

    @Test
    void primaryBodyFailureWinsOverTeardownFailure() {
        var result = new ApiScenarioLifecycleRunner().run(
                null,
                () -> { throw new IllegalStateException("body"); },
                (java.util.function.Consumer<Boolean>) ignored -> { throw new IllegalStateException("teardown"); });

        assertEquals(AtfReportStatus.FAIL, result.status());
        assertEquals("body", result.primaryFailure().getMessage());
    }

    @Test
    void compatibilityRunnerPreservesSetupFailureWhenTeardownAlsoFails() {
        var result = new ApiScenarioLifecycleRunner().run(
                () -> { throw new IllegalStateException("setup"); },
                () -> { throw new IllegalStateException("body must be skipped"); },
                (java.util.function.Consumer<Boolean>) ignored -> { throw new IllegalStateException("teardown"); });

        assertEquals(AtfReportStatus.FAIL, result.status());
        assertEquals("setup", result.primaryFailure().getMessage());
        assertEquals(AtfReportStatus.FAIL,
                result.phases().get(ApiScenarioLifecycleRunner.Phase.SETUP));
        assertEquals(AtfReportStatus.SKIP,
                result.phases().get(ApiScenarioLifecycleRunner.Phase.TEST_BODY));
        assertEquals(AtfReportStatus.FAIL,
                result.phases().get(ApiScenarioLifecycleRunner.Phase.TEARDOWN));
    }

    @Test
    void warningIsVisibleButDoesNotBecomeFailure() throws Exception {
        var result = new ApiScenarioLifecycleRunner().run(
                () -> { throw new ApiScenarioLifecycleRunner.WarningSignal("known drift"); },
                () -> { }, null);

        assertEquals(AtfReportStatus.WARNING, result.status());
        assertEquals(AtfReportStatus.WARNING, result.phases().get(ApiScenarioLifecycleRunner.Phase.SETUP));
        assertTrue(result.phases().containsKey(ApiScenarioLifecycleRunner.Phase.TEST_BODY));
    }

    @Test
    void reportedWarningDoesNotBecomePrimaryFailure() {
        var plan = ApiScenarioPlan.scenario("LifecycleTests", "warning boundary")
                .setup(phase -> { throw new ApiScenarioLifecycleRunner.WarningSignal("known drift"); })
                .body(phase -> { })
                .build();

        var result = new ApiScenarioLifecycleRunner().run(new NoOpSession(), plan);

        assertEquals(AtfReportStatus.WARNING, result.status());
        assertEquals(AtfReportStatus.WARNING,
                result.phases().get(ApiScenarioLifecycleRunner.Phase.SETUP));
        assertNull(result.primaryFailure(), "warning is evidence, not the primary failure");
    }

    @Test
    void lifecycleMatrixPreservesStatusPrimaryFailureAndPhaseOrder() {
        var cases = List.of(
                new LifecycleCase("all phases pass", AtfReportStatus.PASS, null,
                        null, null, null,
                        Map.of(ApiScenarioLifecycleRunner.Phase.SETUP, AtfReportStatus.PASS,
                                ApiScenarioLifecycleRunner.Phase.TEST_BODY, AtfReportStatus.PASS,
                                ApiScenarioLifecycleRunner.Phase.TEARDOWN, AtfReportStatus.PASS)),
                new LifecycleCase("setup skip", AtfReportStatus.SKIP, null,
                        new ApiScenarioLifecycleRunner.SkipSignal("setup skip"), null, null,
                        Map.of(ApiScenarioLifecycleRunner.Phase.SETUP, AtfReportStatus.SKIP,
                                ApiScenarioLifecycleRunner.Phase.TEST_BODY, AtfReportStatus.SKIP,
                                ApiScenarioLifecycleRunner.Phase.TEARDOWN, AtfReportStatus.PASS)),
                new LifecycleCase("setup fail", AtfReportStatus.FAIL, "setup failure",
                        new IllegalStateException("setup failure"), null, null,
                        Map.of(ApiScenarioLifecycleRunner.Phase.SETUP, AtfReportStatus.FAIL,
                                ApiScenarioLifecycleRunner.Phase.TEST_BODY, AtfReportStatus.SKIP,
                                ApiScenarioLifecycleRunner.Phase.TEARDOWN, AtfReportStatus.PASS)),
                new LifecycleCase("body warning", AtfReportStatus.WARNING, null,
                        null, new ApiScenarioLifecycleRunner.WarningSignal("body warning"), null,
                        Map.of(ApiScenarioLifecycleRunner.Phase.SETUP, AtfReportStatus.PASS,
                                ApiScenarioLifecycleRunner.Phase.TEST_BODY, AtfReportStatus.WARNING,
                                ApiScenarioLifecycleRunner.Phase.TEARDOWN, AtfReportStatus.PASS)),
                new LifecycleCase("body fail plus teardown warning", AtfReportStatus.FAIL, "body failure",
                        null, new IllegalStateException("body failure"),
                        new ApiScenarioLifecycleRunner.WarningSignal("teardown warning"),
                        Map.of(ApiScenarioLifecycleRunner.Phase.SETUP, AtfReportStatus.PASS,
                                ApiScenarioLifecycleRunner.Phase.TEST_BODY, AtfReportStatus.FAIL,
                                ApiScenarioLifecycleRunner.Phase.TEARDOWN, AtfReportStatus.WARNING)),
                new LifecycleCase("setup warning then body fail", AtfReportStatus.FAIL, "body failure",
                        new ApiScenarioLifecycleRunner.WarningSignal("setup warning"),
                        new IllegalStateException("body failure"), null,
                        Map.of(ApiScenarioLifecycleRunner.Phase.SETUP, AtfReportStatus.WARNING,
                                ApiScenarioLifecycleRunner.Phase.TEST_BODY, AtfReportStatus.FAIL,
                                ApiScenarioLifecycleRunner.Phase.TEARDOWN, AtfReportStatus.PASS)),
                new LifecycleCase("setup warning then teardown fail", AtfReportStatus.FAIL, "teardown failure",
                        new ApiScenarioLifecycleRunner.WarningSignal("setup warning"), null,
                        new IllegalStateException("teardown failure"),
                        Map.of(ApiScenarioLifecycleRunner.Phase.SETUP, AtfReportStatus.WARNING,
                                ApiScenarioLifecycleRunner.Phase.TEST_BODY, AtfReportStatus.PASS,
                                ApiScenarioLifecycleRunner.Phase.TEARDOWN, AtfReportStatus.FAIL)),
                new LifecycleCase("teardown warning", AtfReportStatus.WARNING, null,
                        null, null, new ApiScenarioLifecycleRunner.WarningSignal("teardown warning"),
                        Map.of(ApiScenarioLifecycleRunner.Phase.SETUP, AtfReportStatus.PASS,
                                ApiScenarioLifecycleRunner.Phase.TEST_BODY, AtfReportStatus.PASS,
                                ApiScenarioLifecycleRunner.Phase.TEARDOWN, AtfReportStatus.WARNING)),
                new LifecycleCase("teardown skip", AtfReportStatus.SKIP, null,
                        null, null, new ApiScenarioLifecycleRunner.SkipSignal("teardown skip"),
                        Map.of(ApiScenarioLifecycleRunner.Phase.SETUP, AtfReportStatus.PASS,
                                ApiScenarioLifecycleRunner.Phase.TEST_BODY, AtfReportStatus.PASS,
                                ApiScenarioLifecycleRunner.Phase.TEARDOWN, AtfReportStatus.SKIP)),
                new LifecycleCase("teardown fail", AtfReportStatus.FAIL, "teardown failure",
                        null, null, new IllegalStateException("teardown failure"),
                        Map.of(ApiScenarioLifecycleRunner.Phase.SETUP, AtfReportStatus.PASS,
                                ApiScenarioLifecycleRunner.Phase.TEST_BODY, AtfReportStatus.PASS,
                                ApiScenarioLifecycleRunner.Phase.TEARDOWN, AtfReportStatus.FAIL)));

        for (var testCase : cases) {
            var events = new ArrayList<String>();
            var result = new ApiScenarioLifecycleRunner().run(
                    new RecordingSession(events), testCase.plan());

            assertEquals(testCase.expectedStatus(), result.status(), testCase.name());
            assertEquals(testCase.expectedPrimaryFailure(),
                    result.primaryFailure() == null ? null : result.primaryFailure().getMessage(),
                    testCase.name());
            assertEquals(testCase.expectedPhases(), result.phases(), testCase.name());
            testCase.expectedPhases().forEach((phase, expectedStatus) -> {
                var phaseOutcome = result.phaseOutcomes().get(phase);
                assertNotNull(phaseOutcome, testCase.name() + " missing " + phase);
                assertEquals(expectedStatus, phaseOutcome.status(), testCase.name() + " " + phase);
            });
            if (testCase.expectedPrimaryFailure() != null) {
                assertTrue(result.phaseOutcomes().values().stream()
                                .anyMatch(outcome -> outcome.failure() != null
                                        && testCase.expectedPrimaryFailure().equals(outcome.failure().getMessage())),
                        testCase.name() + " must retain phase failure evidence");
            }
            var expectedEvents = new ArrayList<String>(List.of(
                    "start:SETUP", "complete:SETUP:"
                            + testCase.expectedPhases().get(ApiScenarioLifecycleRunner.Phase.SETUP)));
            var setupStatus = testCase.expectedPhases().get(ApiScenarioLifecycleRunner.Phase.SETUP);
            if (setupStatus != AtfReportStatus.SKIP && setupStatus != AtfReportStatus.FAIL) {
                expectedEvents.add("start:TEST BODY");
                expectedEvents.add("complete:TEST BODY:"
                        + testCase.expectedPhases().get(ApiScenarioLifecycleRunner.Phase.TEST_BODY));
            }
            expectedEvents.add("start:TEARDOWN");
            expectedEvents.add("complete:TEARDOWN:"
                    + testCase.expectedPhases().get(ApiScenarioLifecycleRunner.Phase.TEARDOWN));
            assertEquals(expectedEvents, events, testCase.name());
        }
    }

    private static ApiScenarioPlan planFor(LifecycleCase testCase) {
        var builder = ApiScenarioPlan.scenario("LifecycleTests", testCase.name())
                .setup(phase -> throwIfPresent(testCase.setupFailure()))
                .body(phase -> throwIfPresent(testCase.bodyFailure()))
                .teardown(phase -> throwIfPresent(testCase.teardownFailure()));
        return builder.build();
    }

    private static void throwIfPresent(Throwable failure) throws Exception {
        if (failure == null) return;
        if (failure instanceof Exception exception) throw exception;
        if (failure instanceof Error error) throw error;
        throw new IllegalStateException(failure);
    }

    private record LifecycleCase(String name, AtfReportStatus expectedStatus,
                                 String expectedPrimaryFailure, Throwable setupFailure,
                                 Throwable bodyFailure, Throwable teardownFailure,
                                 Map<ApiScenarioLifecycleRunner.Phase, AtfReportStatus> expectedPhases) {
        private ApiScenarioPlan plan() { return planFor(this); }
    }

    private static final class RecordingSession implements AtfReportPorts.Session {
        private final List<String> events;

        private RecordingSession(List<String> events) { this.events = events; }

        @Override public Path reportPath() { return Path.of("target", "lifecycle-matrix.html"); }
        @Override public AtfReportPorts.Scenario startScenario(String testClass, String scenario,
                                                                Collection<String> tags) {
            return new RecordingScenario(events);
        }
        @Override public void flush() { }
        @Override public void close() { }
    }

    private static final class RecordingScenario implements AtfReportPorts.Scenario {
        private final List<String> events;

        private RecordingScenario(List<String> events) { this.events = events; }
        @Override public String id() { return "lifecycle-matrix"; }
        @Override public AtfReportPorts.Phase startPhase(String name) {
            events.add("start:" + name);
            return new RecordingPhase(events, name);
        }
        @Override public void complete(AtfReportStatus status) { }
        @Override public boolean hasWarnings() { return false; }
        @Override public void close() { }
    }

    private static final class RecordingPhase implements AtfReportPorts.Phase {
        private final List<String> events;
        private final String name;

        private RecordingPhase(List<String> events, String name) {
            this.events = events;
            this.name = name;
        }
        @Override public AtfReportPorts.Request startRequest(String title, String method, String path) {
            return new NoOpRequest();
        }
        @Override public void complete(AtfReportStatus status) {
            events.add("complete:" + name + ":" + status);
        }
        @Override public void close() { }
    }

    private static final class NoOpSession implements AtfReportPorts.Session {
        @Override public Path reportPath() { return Path.of("target", "lifecycle-test.html"); }
        @Override public AtfReportPorts.Scenario startScenario(String testClass, String scenario,
                                                                Collection<String> tags) {
            return new NoOpScenario();
        }
        @Override public void flush() { }
        @Override public void close() { }
    }

    private static final class NoOpScenario implements AtfReportPorts.Scenario {
        @Override public String id() { return "scenario"; }
        @Override public AtfReportPorts.Phase startPhase(String name) { return new NoOpPhase(); }
        @Override public void complete(AtfReportStatus status) { }
        @Override public boolean hasWarnings() { return false; }
        @Override public void close() { }
    }

    private static final class NoOpPhase implements AtfReportPorts.Phase {
        @Override public AtfReportPorts.Request startRequest(String title, String method, String path) {
            return new NoOpRequest();
        }
        @Override public void complete(AtfReportStatus status) { }
        @Override public void close() { }
    }

    private static final class NoOpRequest implements AtfReportPorts.Request {
        @Override public void detail(String name, String value) { }
        @Override public void json(String label, String json) { }
        @Override public AtfReportPorts.Validation startValidation(String title) {
            return new NoOpValidation();
        }
        @Override public void complete(AtfReportStatus status) { }
        @Override public void close() { }
    }

    private static final class NoOpValidation implements AtfReportPorts.Validation {
        @Override public void assertions(Collection<atf.api.core.reporting.AtfAssertion> assertions) { }
        @Override public void complete(AtfReportStatus status) { }
        @Override public void close() { }
    }
}
