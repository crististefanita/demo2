package atf.api.reporting.ui.network;

import atf.api.reporting.contract.AtfAssertion;
import atf.api.reporting.api.ReportAuthObservation;
import atf.api.reporting.api.ReportRequestSignature;
import atf.api.reporting.model.ReportExecutionEvidence;
import atf.api.reporting.model.ReportField;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;

/**
 * Builds the compact, renderer-neutral network summary for a UI journey.
 * Detailed request/response bodies remain owned by their UI checkpoints; this
 * projection only answers which exchange happened, when, and why.
 */
public final class ReportNetworkInspector {
    private ReportNetworkInspector() {
    }

    public static List<ReportField> fields(Collection<ReportExecutionEvidence> evidence) {
        return fields(evidence, List.of());
    }

    /**
     * Projects the full browser trace and its causal UI checkpoints. The
     * checkpoint rows stay compact; request/response JSON remains attached to
     * the corresponding UI checkpoint and is never copied into this summary.
     */
    public static List<ReportField> fields(Collection<ReportExecutionEvidence> evidence,
                                           Collection<Step> steps) {
        return fields(evidence, steps, List.of());
    }

    /**
     * Projects the ordered trace and one renderer-neutral aggregate.  The
     * aggregate deliberately contains facts only; bodies remain attached to
     * their canonical request/response disclosures.
     */
    public static List<ReportField> fields(Collection<ReportExecutionEvidence> evidence,
                                           Collection<Step> steps,
                                           Collection<Diagnostic> diagnostics) {
        List<ReportExecutionEvidence> exchanges = evidence == null
                ? List.of()
                : evidence.stream().filter(Objects::nonNull).toList();
        Set<String> signatures = new LinkedHashSet<>();
        List<ReportField> fields = new java.util.ArrayList<>();
        fields.add(ReportField.of("network.observations", Integer.toString(exchanges.size())));
        List<Step> journey = steps == null ? List.of() : steps.stream()
                .filter(Objects::nonNull).toList();
        fields.add(ReportField.of("network.ui.steps", Integer.toString(journey.size())));
        for (int index = 0; index < journey.size(); index++) {
            fields.add(ReportField.of("network.ui.step." + (index + 1), journey.get(index).summary()));
        }
        for (ReportExecutionEvidence exchange : exchanges) {
            if (exchange.request() != null) {
                signatures.add(signature(exchange));
            }
        }
        fields.add(ReportField.of("network.unique.signatures", Integer.toString(signatures.size())));
        for (int index = 0; index < exchanges.size(); index++) {
            fields.add(ReportField.of("network.exchange." + (index + 1), summary(exchanges.get(index))));
        }
        appendAggregate(fields, aggregate(exchanges, journey, diagnostics));
        return List.copyOf(fields);
    }

    /** Builds the typed journey projection consumed by all report renderers. */
    public static JourneyAggregate aggregate(Collection<ReportExecutionEvidence> evidence,
                                             Collection<Step> steps,
                                             Collection<Diagnostic> diagnostics) {
        List<ReportExecutionEvidence> exchanges = evidence == null
                ? List.of() : evidence.stream().filter(Objects::nonNull).toList();
        List<Step> journey = steps == null ? List.of() : steps.stream()
                .filter(Objects::nonNull).toList();
        List<Diagnostic> runtimeDiagnostics = diagnostics == null ? List.of() : diagnostics.stream()
                .filter(Objects::nonNull).toList();

        Map<AuthKey, Integer> authCounts = new LinkedHashMap<>();
        Map<SignatureKey, SignatureAccumulator> signatures = new LinkedHashMap<>();
        int successful = 0;
        int failed = 0;
        int attempts = 0;
        int retries = 0;
        int live = 0;
        int mock = 0;
        int unknown = 0;
        for (ReportExecutionEvidence exchange : exchanges) {
            if (exchange.request() != null) {
                ReportAuthObservation auth = exchange.request().authObservation();
                int status = exchange.response() == null ? -1 : exchange.response().status();
                AuthKey key = new AuthKey(auth.method().wireValue(), auth.source().wireValue(), status);
                authCounts.merge(key, 1, Integer::sum);
                String requestSignature = signature(exchange);
                SignatureKey signatureKey = new SignatureKey(requestSignature);
                signatures.computeIfAbsent(signatureKey, ignored -> new SignatureAccumulator())
                        .add(status);
            }
            if (exchange.response() != null) {
                if (exchange.response().status() >= 200 && exchange.response().status() < 400) successful++;
                else failed++;
            }
            attempts += Math.max(0, exchange.attempts());
            retries += exchange.retryEvents().size();
            switch (exchange.provenance()) {
                case LIVE_HTTP -> live++;
                case ACCEPTANCE_MOCK -> mock++;
                case UNKNOWN -> unknown++;
            }
        }
        List<AuthenticationObservation> authentication = authCounts.entrySet().stream()
                .map(entry -> new AuthenticationObservation(entry.getKey().method(),
                        entry.getKey().source(), entry.getKey().status(), entry.getValue()))
                .toList();
        List<SignatureObservation> signatureObservations = signatures.entrySet().stream()
                .map(entry -> entry.getValue().toObservation(entry.getKey().signature()))
                .toList();
        List<ScenarioCorrelation> correlation = journey.stream()
                .map(step -> new ScenarioCorrelation(step.keyword(), step.title(), step.exchanges().size(),
                        step.assertionCount(), step.passed()))
                .toList();
        List<DiagnosticObservation> diagnosticObservations = deduplicateDiagnostics(runtimeDiagnostics);
        return new JourneyAggregate(authentication, correlation,
                new TransportCounters(exchanges.size(), successful, failed, attempts, retries,
                        live, mock, unknown), signatureObservations, diagnosticObservations);
    }

    private static void appendAggregate(List<ReportField> fields, JourneyAggregate aggregate) {
        if (aggregate.observations() == 0 && aggregate.correlation().isEmpty()
                && aggregate.diagnostics().isEmpty()) return;
        fields.add(ReportField.of("network.aggregate.authentication",
                Integer.toString(aggregate.authentication().size())));
        for (int index = 0; index < aggregate.authentication().size(); index++) {
            fields.add(ReportField.of("network.aggregate.authentication." + (index + 1),
                    aggregate.authentication().get(index).summary()));
        }
        fields.add(ReportField.of("network.aggregate.correlation",
                Integer.toString(aggregate.correlation().size())));
        for (int index = 0; index < aggregate.correlation().size(); index++) {
            fields.add(ReportField.of("network.aggregate.correlation." + (index + 1),
                    aggregate.correlation().get(index).summary()));
        }
        fields.add(ReportField.of("network.aggregate.transport", aggregate.transport().summary()));
        fields.add(ReportField.of("network.aggregate.signatures",
                Integer.toString(aggregate.signatures().size())));
        for (int index = 0; index < aggregate.signatures().size(); index++) {
            fields.add(ReportField.of("network.aggregate.signature." + (index + 1),
                    aggregate.signatures().get(index).summary()));
        }
        fields.add(ReportField.of("network.aggregate.diagnostics",
                Integer.toString(aggregate.diagnostics().size())));
        for (int index = 0; index < aggregate.diagnostics().size(); index++) {
            fields.add(ReportField.of("network.aggregate.diagnostic." + (index + 1),
                    aggregate.diagnostics().get(index).summary()));
        }
    }

    private static List<DiagnosticObservation> deduplicateDiagnostics(Collection<Diagnostic> diagnostics) {
        Map<DiagnosticKey, DiagnosticAccumulator> grouped = new LinkedHashMap<>();
        for (Diagnostic diagnostic : diagnostics) {
            DiagnosticKey key = DiagnosticKey.from(diagnostic);
            grouped.computeIfAbsent(key, ignored -> new DiagnosticAccumulator()).add(diagnostic.source());
        }
        int unboundIndex = 0;
        List<DiagnosticObservation> observations = new ArrayList<>();
        for (var entry : grouped.entrySet()) {
            var key = entry.getKey();
            String signal = key.unbound() ? "UNBOUND-" + (++unboundIndex) : "";
            observations.add(new DiagnosticObservation(key.method(), key.route(), key.status(), signal,
                    entry.getValue().count, entry.getValue().sources()));
        }
        return List.copyOf(observations);
    }

    /** Causal, renderer-neutral summary for one UI checkpoint. */
    public record Step(String keyword, String title, int assertionCount,
                       boolean passed, Collection<ReportExecutionEvidence> exchanges) {
        public Step {
            keyword = keyword == null || keyword.isBlank() ? "STEP" : keyword.trim().toUpperCase();
            title = title == null || title.isBlank() ? "UI checkpoint" : title.trim();
            if (assertionCount < 0) throw new IllegalArgumentException("assertionCount must not be negative");
            exchanges = exchanges == null ? List.of() : exchanges.stream()
                    .filter(Objects::nonNull).toList();
        }

        public static Step from(String keyword, String title, Collection<AtfAssertion> assertions,
                                Collection<ReportExecutionEvidence> exchanges) {
            List<AtfAssertion> safeAssertions = assertions == null ? List.of() : assertions.stream()
                    .filter(Objects::nonNull).toList();
            boolean passed = safeAssertions.stream().allMatch(AtfAssertion::passed);
            return new Step(keyword, title, safeAssertions.size(), passed, exchanges);
        }

        String summary() {
            String responses = exchanges.stream()
                    .filter(exchange -> exchange.response() != null)
                    .map(exchange -> Integer.toString(exchange.response().status()))
                    .toList().toString();
            return keyword + " " + title + " | exchanges=" + exchanges.size()
                    + " | responses=" + responses + " | assertions=" + assertionCount
                    + " " + (passed ? "PASS" : "FAIL");
        }
    }

    /** Complete renderer-neutral journey aggregate; no JSON or credentials. */
    public record JourneyAggregate(List<AuthenticationObservation> authentication,
                                   List<ScenarioCorrelation> correlation,
                                   TransportCounters transport,
                                   List<SignatureObservation> signatures,
                                   List<DiagnosticObservation> diagnostics) {
        public JourneyAggregate {
            authentication = List.copyOf(authentication == null ? List.of() : authentication);
            correlation = List.copyOf(correlation == null ? List.of() : correlation);
            transport = transport == null ? TransportCounters.empty() : transport;
            signatures = List.copyOf(signatures == null ? List.of() : signatures);
            diagnostics = List.copyOf(diagnostics == null ? List.of() : diagnostics);
        }

        public int observations() { return transport.observations(); }
    }

    public record AuthenticationObservation(String method, String source, int status, int count) {
        public String summary() {
            return "method=" + method + "; source=" + source + "; status=" + status + "; count=" + count;
        }
    }

    public record ScenarioCorrelation(String keyword, String title, int exchangeCount,
                                      int assertionCount, boolean passed) {
        public String summary() {
            return keyword + " " + title + " | exchanges=" + exchangeCount
                    + " | assertions=" + assertionCount + " | " + (passed ? "PASS" : "FAIL");
        }
    }

    public record TransportCounters(int observations, int successful, int failed, int attempts,
                                    int retries, int live, int mock, int unknown) {
        public TransportCounters {
            if (observations < 0 || successful < 0 || failed < 0 || attempts < 0 || retries < 0
                    || live < 0 || mock < 0 || unknown < 0) {
                throw new IllegalArgumentException("network counters must not be negative");
            }
        }

        static TransportCounters empty() { return new TransportCounters(0, 0, 0, 0, 0, 0, 0, 0); }

        public String summary() {
            return "observations=" + observations + "; successful=" + successful
                    + "; failed=" + failed + "; attempts=" + attempts + "; retries=" + retries
                    + "; live=" + live + "; mock=" + mock + "; unknown=" + unknown;
        }
    }

    public record SignatureObservation(String signature, int count, List<Integer> statuses) {
        public SignatureObservation {
            signature = signature == null ? "" : signature;
            statuses = List.copyOf(statuses == null ? List.of() : statuses);
        }

        public String summary() {
            return "count=" + count + "; signature="
                    + ReportRequestSignature.displaySignature(signature)
                    + "; statuses=" + statuses;
        }
    }

    public record Diagnostic(String source, String method, String route, int status, String message) {
        public Diagnostic {
            source = normalize(source);
            method = normalize(method).toUpperCase(Locale.ROOT);
            route = normalize(route);
            message = normalize(message);
        }
    }

    public record DiagnosticObservation(String method, String route, int status, String signal,
                                        int count, List<String> sources) {
        public DiagnosticObservation {
            method = normalize(method).toUpperCase(Locale.ROOT);
            route = normalize(route);
            signal = normalize(signal);
            sources = List.copyOf(sources == null ? List.of() : sources);
        }

        public String summary() {
            return "method=" + method + "; route=" + route + "; status=" + status
                    + "; count=" + count + "; sources=" + sources
                    + (signal.isBlank() ? "" : "; signal=" + signal);
        }
    }

    private record AuthKey(String method, String source, int status) { }
    private record SignatureKey(String signature) { }
    private record DiagnosticKey(String method, String route, int status,
                                 String message, boolean unbound) {
        private static DiagnosticKey from(Diagnostic diagnostic) {
            boolean unbound = diagnostic.method().isBlank()
                    && diagnostic.route().isBlank()
                    && diagnostic.status() < 0;
            return new DiagnosticKey(diagnostic.method(), diagnostic.route(), diagnostic.status(),
                    unbound ? diagnostic.message() : "", unbound);
        }
    }

    private static final class SignatureAccumulator {
        private int count;
        private final List<Integer> statuses = new ArrayList<>();

        void add(int status) { count++; if (!statuses.contains(status)) statuses.add(status); }

        SignatureObservation toObservation(String signature) {
            return new SignatureObservation(signature, count, statuses);
        }
    }

    private static final class DiagnosticAccumulator {
        private int count;
        private final LinkedHashSet<String> sources = new LinkedHashSet<>();

        void add(String source) { count++; sources.add(normalize(source)); }

        List<String> sources() { return List.copyOf(sources); }
    }

    private static String normalize(String value) { return value == null ? "" : value.trim(); }

    public static String summary(ReportExecutionEvidence evidence) {
        if (evidence == null || evidence.request() == null || evidence.response() == null) {
            return "unavailable exchange";
        }
        var request = evidence.request();
        var response = evidence.response();
        var trace = evidence.trace();
        String event = trace != null && !trace.eventId().isBlank()
                ? trace.eventId() : "exchange";
        String phase = trace == null || trace.phase().isBlank() ? "unknown" : trace.phase();
        String contentType = header(response.headers(), "content-type");
        String transport = evidence.provenance().reportLabel();
        String operation = request.method() + " " + request.path() + " → "
                + response.status() + (contentType.isBlank() ? "" : " " + contentType);
        String signature = signature(evidence);
        String source = trace == null || trace.evidenceSource().isBlank()
                ? "unknown" : trace.evidenceSource();
        return event + " | " + traceFacts(trace) + "phase=" + phase + " | " + operation
                + " | auth=" + request.authObservation().summary()
                + " | attempts=" + evidence.attempts()
                + " | retries=" + evidence.retryEvents().size()
                + " | source=" + source + " | transport=" + transport
                + " | signature=" + ReportRequestSignature.displaySignature(signature);
    }

    private static String traceFacts(atf.api.reporting.api.ReportTraceMetadata trace) {
        if (trace == null) return "";
        var facts = new StringJoiner(" | ");
        if (trace.sequence() > 0) facts.add("sequence=" + trace.sequence());
        if (!trace.stepId().isBlank()) facts.add("step=" + trace.stepId());
        if (!trace.role().isBlank()) facts.add("role=" + trace.role());
        if (!trace.initiator().isBlank()) facts.add("initiator=" + trace.initiator());
        if (!trace.correlationKey().isBlank()) facts.add("correlation=" + trace.correlationKey());
        String value = facts.toString();
        return value.isBlank() ? "" : value + " | ";
    }

    private static String signature(ReportExecutionEvidence evidence) {
        if (evidence == null || evidence.request() == null) return "";
        if (evidence.trace() != null && !evidence.trace().requestSignature().isBlank()) {
            return evidence.trace().requestSignature();
        }
        return atf.api.reporting.api.ReportRequestSignature.forRequest(evidence.request());
    }

    private static String header(java.util.Map<String, String> headers, String name) {
        if (headers == null) return "";
        return headers.entrySet().stream()
                .filter(entry -> entry.getKey().equalsIgnoreCase(name))
                .map(java.util.Map.Entry::getValue)
                .findFirst().orElse("");
    }
}
