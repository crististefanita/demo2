package atf.ui.practiceshop.support;

import atf.api.reporting.contract.AtfAssertion;
import atf.api.reporting.api.ReportRequest;
import atf.api.reporting.api.ReportRequestSignature;
import atf.api.reporting.model.ReportExecutionEvidence;
import atf.api.core.http.request.HttpMethod;
import atf.ui.practiceshop.api.PracticeShopApiOperation;
import atf.ui.practiceshop.api.PracticeShopApiHttpStatus;
import atf.ui.practiceshop.api.PracticeShopApiAuthType;
import atf.ui.practiceshop.api.PracticeShopApiRequestFactory;
import atf.ui.practiceshop.api.PracticeShopApiRoutes;
import atf.ui.practiceshop.config.PracticeShopApiConfiguration;
import atf.ui.practiceshop.config.PracticeShopCredentials;
import atf.ui.practiceshop.config.PracticeShopValidationProfile;
import atf.ui.practiceshop.config.PracticeShopApiCheckpoint;
import atf.ui.practiceshop.config.PracticeShopApiEvidenceBinding;
import atf.api.ui.network.UiNetworkInspector;
import atf.api.ui.network.UiNetworkObservation;
import com.microsoft.playwright.Page;

import java.util.ArrayList;
import java.time.Instant;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.EnumMap;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Captures only the Practice Shop API traffic produced by the same UI session.
 * It is deliberately a consumer-side adapter: production modules do not depend on
 * the external API and no second browser or independent cart is created.
 */
public final class PracticeShopApiNetworkJournal {
    private final PracticeShopValidationProfile profile;
    private final PracticeShopApiEvidenceBinding evidenceBinding;
    private final Function<String, String> serviceAlias;
    private final PracticeShopApiTrafficCapture trafficCapture = new PracticeShopApiTrafficCapture();
    private final List<Exchange> exchanges = new ArrayList<>();
    private final AtomicLong sequence = new AtomicLong();
    private final EnumMap<PracticeShopApiCheckpoint, List<ReportExecutionEvidence>> evidenceByCheckpoint =
            new EnumMap<>(PracticeShopApiCheckpoint.class);
    private String productId;

    public PracticeShopApiNetworkJournal(PracticeShopValidationProfile profile) {
        this(profile, PracticeShopApiEvidenceBinding.forProfile(profile, null));
    }

    public PracticeShopApiNetworkJournal(PracticeShopValidationProfile profile,
                                         PracticeShopApiEvidenceBinding evidenceBinding) {
        this(profile, evidenceBinding, Function.identity());
    }

    /** Consumer adapter seam for an honest service alias; the generic inspector remains host-based. */
    public PracticeShopApiNetworkJournal(PracticeShopValidationProfile profile,
                                         PracticeShopApiEvidenceBinding evidenceBinding,
                                         Function<String, String> serviceAlias) {
        this.profile = Objects.requireNonNull(profile, "profile");
        this.evidenceBinding = Objects.requireNonNull(evidenceBinding, "evidenceBinding");
        this.serviceAlias = Objects.requireNonNull(serviceAlias, "serviceAlias");
    }

    public void attach(Page page) {
        Objects.requireNonNull(page, "page");
        if (!hasEnabledCheckpoint()) return;
        page.onResponse(response -> trafficCapture.capture(response).ifPresent(this::recordCapturedExchange));
    }

    /**
     * Reuses the generic Playwright network boundary when the UI session owns
     * one. This is the plug-and-play path; the page-specific overload above is
     * retained as a compatibility seam for focused consumer tests.
     */
    public void attach(UiNetworkInspector inspector) {
        Objects.requireNonNull(inspector, "inspector");
        if (!hasEnabledCheckpoint()) return;
        inspector.subscribe(this::recordCapturedObservation);
    }

    /** Internal composition seam for deterministic component tests; browser capture remains the live path. */
    void recordCapturedExchange(Exchange exchange) {
        Objects.requireNonNull(exchange, "exchange");
        if (!PracticeShopApiTrafficCapture.isPublishable(exchange.method(), exchange.path())) return;
        exchanges.add(exchange.withTrace(sequence.incrementAndGet(), Instant.now(), phase(exchange.path())));
    }

    private void recordCapturedObservation(UiNetworkObservation observation) {
        Exchange exchange = new Exchange(
                observation.method(), observation.path(), observation.url(), observation.status(),
                observation.requestBody(), observation.responseBody(), observation.requestHeaders(),
                observation.responseHeaders()).withObservedAuthMethod(observation.authMethod());
        recordCapturedExchange(observation.requestFailed()
                ? exchange.withFailureReason(observation.failureReason()) : exchange);
    }

    /** API precondition for the product-page purchase action. */
    public List<AtfAssertion> productReady(String productName) {
        return productReady(productName, null);
    }

    /** API confirmation for the login setup that precedes an authenticated UI flow. */
    /**
     * Confirms the browser login and keeps deterministic JSON in the report when
     * Chromium releases the compressed CORS login payload before it can be read.
     * Recovery is limited to the idempotent login exchange; the browser exchange
     * remains the canonical trace and no cart/payment request is replayed.
     */
    public List<AtfAssertion> loginConfirmed(PracticeShopApiConfiguration apiConfiguration,
                                             PracticeShopCredentials credentials) {
        if (!enabled(PracticeShopApiCheckpoint.LOGIN)) return List.of();
        Exchange login = PracticeShopApiExchangeSelector.latest(
                exchanges, HttpMethod.POST.value(), PracticeShopApiRoutes.LOGIN,
                exchange -> exchange.status() == 200);
        login = recoverLoginBody(login, apiConfiguration, credentials);
        store(PracticeShopApiCheckpoint.LOGIN, List.of(toEvidence(login)));
        return PracticeShopApiAssertions.login(login);
    }

    /** Sequence fence used to associate only the browser submission that followed the registration form. */
    public long sequenceMarker() {
        return sequence.get();
    }

    /** Validates and owns the observed form POST; the route is discovered from this browser run, not guessed. */
    public List<AtfAssertion> registrationConfirmed(long afterSequence) {
        if (!enabled(PracticeShopApiCheckpoint.REGISTRATION)) return List.of();
        List<Exchange> submissions = exchanges.stream()
                .filter(exchange -> exchange.sequence() > afterSequence)
                .filter(exchange -> HttpMethod.POST.value().equalsIgnoreCase(exchange.method()))
                .toList();
        List<Exchange> safeSubmissions = submissions.stream()
                .map(exchange -> exchange.withPhase("account-setup").withRegistrationRedaction())
                .toList();
        if (!safeSubmissions.isEmpty()) {
            store(PracticeShopApiCheckpoint.REGISTRATION,
                    safeSubmissions.stream().map(this::toEvidence).toList());
        }
        boolean singleSubmission = safeSubmissions.size() == 1;
        boolean successfulResponse = singleSubmission
                && safeSubmissions.getFirst().status() >= 200
                && safeSubmissions.getFirst().status() < 300;
        return List.of(
                new AtfAssertion("Registration browser exchange", "exactly one observed form POST",
                        safeSubmissions.size() + " observed form POST(s)", singleSubmission),
                new AtfAssertion("Registration response", "HTTP 2xx",
                        singleSubmission ? "HTTP " + safeSubmissions.getFirst().status() : "not uniquely observed",
                        successfulResponse));
    }

    private static Exchange recoverLoginBody(Exchange login,
                                             PracticeShopApiConfiguration apiConfiguration,
                                             PracticeShopCredentials credentials) {
        if (login == null || !bodyUnavailable(login.responseBody())
                || apiConfiguration == null || credentials == null
                || apiConfiguration.mode() != atf.api.core.config.ApiTransportMode.LIVE) {
            return login;
        }
        PracticeShopLoginBodyRecovery.Result recovery = PracticeShopLoginBodyRecovery.attempt(
                () -> recoverLoginBody(apiConfiguration, credentials));
        // Browser evidence remains authoritative. A failed optional recovery is
        // still published as explicit evidence instead of being swallowed.
        return login.withResponseBody(recovery.effectiveBody());
    }

    private static String recoverLoginBody(PracticeShopApiConfiguration configuration,
                                           PracticeShopCredentials credentials) throws IOException {
        var request = new PracticeShopApiRequestFactory().login(credentials);
        URI uri = configuration.endpoint().resolve(PracticeShopApiRoutes.LOGIN.substring(1));
        HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
        try {
            connection.setConnectTimeout((int) configuration.timeout().toMillis());
            connection.setReadTimeout((int) configuration.timeout().toMillis());
            connection.setRequestMethod(HttpMethod.POST.value());
            connection.setDoOutput(true);
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Content-Type", "application/json");
            byte[] body = request.bodyText().getBytes(StandardCharsets.UTF_8);
            try (var output = connection.getOutputStream()) {
                output.write(body);
            }
            int status = connection.getResponseCode();
            if (status < 200 || status >= 300) {
                throw new IOException("HTTP status " + status + " for " + PracticeShopApiRoutes.LOGIN);
            }
            try (InputStream input = connection.getInputStream()) {
                return new String(input.readAllBytes(), StandardCharsets.UTF_8);
            }
        } finally {
            connection.disconnect();
        }
    }

    private static boolean bodyUnavailable(String body) {
        return PracticeShopApiBodyEvidence.isUnavailable(body);
    }

    /**
     * API precondition for the product selected by the UI. The route is the
     * source of truth when the external catalog rotates IDs or contains
     * several cards with the same display name.
     */
    public List<AtfAssertion> productReady(String productName, String selectedProductPath) {
        if (!enabled(PracticeShopApiCheckpoint.PRODUCT)) return List.of();
        Exchange search = PracticeShopApiExchangeSelector.latest(
                exchanges, PracticeShopApiOperation.SEARCH,
                exchange -> exchange.status() == 200);
        productId = PracticeShopApiExchangeSelector.productIdFromPath(selectedProductPath);
        if (productId == null) {
            productId = PracticeShopApiAssertions.productId(search.responseBody(), productName);
        }
        String productPath = PracticeShopApiRoutes.product(productId);
        Exchange detail = PracticeShopApiExchangeSelector.latest(exchanges, HttpMethod.GET.value(), productPath,
                exchange -> exchange.status() == 200
                        && exchange.path().equals(productPath));
        store(PracticeShopApiCheckpoint.PRODUCT, List.of(toEvidence(search), toEvidence(detail)));
        return PracticeShopApiAssertions.product(search, detail, productName, productId);
    }

    /** API confirmation after the UI adds the configured quantity to the cart. */
    public List<AtfAssertion> cartReady(int quantity) {
        if (!enabled(PracticeShopApiCheckpoint.CART)) return List.of();
        if (productId == null || productId.isBlank()) {
            throw new IllegalStateException("Product API precondition was not established before cart validation");
        }
        Exchange add = PracticeShopApiExchangeSelector.latest(exchanges, HttpMethod.POST.value(),
                PracticeShopApiRoutes.CART_RESOURCE_PREFIX, exchange -> exchange.status() == 200
                && exchange.requestBody().contains(productId));
        Exchange read = PracticeShopApiExchangeSelector.latest(
                exchanges, HttpMethod.GET.value(), add.path(), exchange -> exchange.status() == 200);
        store(PracticeShopApiCheckpoint.CART, List.of(toEvidence(add), toEvidence(read)));
        return PracticeShopApiAssertions.cart(add, read, productId, quantity);
    }

    /** API confirmation after the UI submits the configured payment method. */
    public List<AtfAssertion> paymentConfirmed(String paymentMethod) {
        if (!enabled(PracticeShopApiCheckpoint.PAYMENT)) return List.of();
        Exchange payment = PracticeShopApiExchangeSelector.latest(
                exchanges, HttpMethod.POST.value(), PracticeShopApiRoutes.PAYMENT_CHECK,
                exchange -> exchange.status() == 200);
        store(PracticeShopApiCheckpoint.PAYMENT, List.of(toEvidence(payment)));
        return PracticeShopApiAssertions.payment(payment, paymentMethod);
    }

    /** API confirmation for the order created by the final UI confirmation action. */
    public List<AtfAssertion> orderCreated(String uiConfirmation) {
        if (!enabled(PracticeShopApiCheckpoint.ORDER)) return List.of();
        Exchange order = PracticeShopApiExchangeSelector.latest(
                exchanges, PracticeShopApiOperation.ORDER_CREATE,
                exchange -> PracticeShopApiHttpStatus.isSuccess(exchange.status()));
        store(PracticeShopApiCheckpoint.ORDER, List.of(toEvidence(order)));
        return PracticeShopApiAssertions.order(order, uiConfirmation);
    }

    public List<Exchange> exchanges() { return List.copyOf(exchanges); }
    /** Returns the complete ordered browser network trace for the journey-level inspector. */
    public List<ReportExecutionEvidence> allEvidence() {
        return exchanges.stream()
                .map(this::toEvidence)
                .toList();
    }

    /**
     * Returns only exchanges owned by the configured, successfully evaluated
     * API checkpoints. The selection belongs to this consumer composition
     * boundary; the reporting renderer must remain unaware of Practice Shop
     * business concepts and must never receive unbound browser traffic.
     */
    public List<ReportExecutionEvidence> selectedEvidence() {
        if (!profile.apiEvidenceRequired() || evidenceBinding.checkpoints().isEmpty()) {
            return List.of();
        }
        List<ReportExecutionEvidence> selected = evidenceByCheckpoint.values().stream()
                .flatMap(List::stream)
                .toList();
        if (selected.isEmpty()) return List.of();

        Set<String> selectedEventIds = selected.stream()
                .map(ReportExecutionEvidence::trace)
                .filter(Objects::nonNull)
                .map(trace -> trace.eventId())
                .filter(eventId -> eventId != null && !eventId.isBlank())
                .collect(Collectors.toSet());
        return allEvidence().stream()
                .filter(evidence -> selectedEventIds.contains(evidence.trace().eventId())
                        || selected.contains(evidence))
                .toList();
    }

    /** Keeps failed allowed-host requests visible in the journey even without a successful checkpoint. */
    public List<ReportExecutionEvidence> inspectorEvidence() {
        if (!hasEnabledCheckpoint()) return List.of();
        Set<String> selectedEventIds = selectedEvidence().stream()
                .map(evidence -> evidence.trace().eventId())
                .collect(Collectors.toSet());
        return allEvidence().stream()
                .filter(evidence -> evidence.response().status() < 0
                        || selectedEventIds.contains(evidence.trace().eventId()))
                .toList();
    }

    public List<ReportExecutionEvidence> evidence(PracticeShopApiCheckpoint checkpoint) {
        return evidenceByCheckpoint.getOrDefault(Objects.requireNonNull(checkpoint, "checkpoint"), List.of());
    }
    public String productId() { return productId; }

    private boolean enabled(PracticeShopApiCheckpoint checkpoint) {
        return profile.apiEvidenceRequired() && evidenceBinding.includes(checkpoint);
    }

    private boolean hasEnabledCheckpoint() {
        return profile.apiEvidenceRequired() && !evidenceBinding.checkpoints().isEmpty();
    }

    private void store(PracticeShopApiCheckpoint checkpoint, List<ReportExecutionEvidence> evidence) {
        if (enabled(checkpoint)) evidenceByCheckpoint.put(checkpoint, List.copyOf(evidence));
    }

    private ReportExecutionEvidence toEvidence(Exchange exchange) {
        String host = URI.create(exchange.url()).getHost();
        String alias = serviceAlias.apply(host == null ? "" : host);
        return PracticeShopApiEvidenceMapper.toExecution(exchange, alias);
    }

    private static String phase(String path) {
        if (path == null) return "unknown";
        if (path.startsWith(PracticeShopApiRoutes.LOGIN)) return "authentication";
        if (path.startsWith(PracticeShopApiRoutes.PRODUCT_SEARCH)
                || path.startsWith(PracticeShopApiRoutes.PRODUCT_COLLECTION)
                || path.startsWith(PracticeShopApiRoutes.PRODUCTS)) return "catalog";
        if (path.startsWith(PracticeShopApiRoutes.CARTS)) return "cart";
        if (path.startsWith(PracticeShopApiRoutes.PAYMENT_CHECK)
                || path.startsWith(PracticeShopApiRoutes.POSTCODE_LOOKUP)
                || path.equals(PracticeShopApiRoutes.INVOICE_CREATE)
                || path.equals(PracticeShopApiRoutes.GUEST_INVOICE_CREATE)) return "checkout";
        return "unknown";
    }

    public record Exchange(String method, String path, String url, int status,
                           String requestBody, String responseBody,
                           java.util.Map<String, String> requestHeaders,
                           java.util.Map<String, String> responseHeaders,
                           long sequence, Instant capturedAt, String phase,
                           String signature, String digest,
                           String role, String stepId, String initiator,
                           String evidenceSource, String expected, String observed,
                           String observedAuthMethod, String failureReason) {
        public Exchange(String method, String path, String url, int status,
                        String requestBody, String responseBody,
                        java.util.Map<String, String> requestHeaders,
                        java.util.Map<String, String> responseHeaders) {
            this(method, path, url, status, requestBody, responseBody, requestHeaders, responseHeaders,
                    0, Instant.EPOCH, "unknown", digest(method, path, status, requestBody, responseBody), "",
                    "", "", "", "", "", "", "", "");
        }

        /** Compatibility constructor for direct API fixture and test callers. */
        public Exchange(String method, String path, String url, int status,
                        String requestBody, String responseBody,
                        java.util.Map<String, String> requestHeaders,
                        java.util.Map<String, String> responseHeaders,
                        long sequence, Instant capturedAt, String phase,
                        String signature, String digest, String role, String stepId,
                        String initiator, String evidenceSource, String expected,
                        String observed) {
            this(method, path, url, status, requestBody, responseBody, requestHeaders,
                    responseHeaders, sequence, capturedAt, phase, signature, digest,
                    role, stepId, initiator, evidenceSource, expected, observed, "");
        }

        /** Compatibility constructor for response exchanges created before failure evidence. */
        public Exchange(String method, String path, String url, int status,
                        String requestBody, String responseBody,
                        java.util.Map<String, String> requestHeaders,
                        java.util.Map<String, String> responseHeaders,
                        long sequence, Instant capturedAt, String phase,
                        String signature, String digest, String role, String stepId,
                        String initiator, String evidenceSource, String expected,
                        String observed, String observedAuthMethod) {
            this(method, path, url, status, requestBody, responseBody, requestHeaders,
                    responseHeaders, sequence, capturedAt, phase, signature, digest,
                    role, stepId, initiator, evidenceSource, expected, observed,
                    observedAuthMethod, "");
        }

        public Exchange {
            requestHeaders = PracticeShopJsonEvidence.redactHeaders(requestHeaders);
            responseHeaders = PracticeShopJsonEvidence.redactHeaders(responseHeaders);
            requestBody = PracticeShopApiEvidenceMapper.redactJson(requestBody);
            responseBody = PracticeShopApiEvidenceMapper.redactJson(responseBody);
            capturedAt = capturedAt == null ? Instant.EPOCH : capturedAt;
            phase = phase == null || phase.isBlank() ? "unknown" : phase;
            signature = signature == null || signature.isBlank()
                    ? requestSignature(method, path, requestBody) : signature;
            digest = digest == null ? "" : digest;
            role = role == null ? "" : role.trim();
            stepId = stepId == null ? "" : stepId.trim();
            initiator = initiator == null ? "" : initiator.trim();
            evidenceSource = evidenceSource == null ? "" : evidenceSource.trim();
            expected = expected == null ? "" : expected.trim();
            observed = observed == null ? "" : observed.trim();
            observedAuthMethod = observedAuthMethod == null ? "" : observedAuthMethod.trim();
            failureReason = failureReason == null ? "" : failureReason.trim();
        }

        public Exchange withTrace(long sequence, Instant capturedAt, String phase) {
            return withTrace(sequence, capturedAt, phase, "ActionCaused", phase,
                    reportSummary(), failureReason.isBlank() ? "UI_BROWSER" : "UI_BROWSER_REQUEST_FAILED", "", "");
        }

        public Exchange withTrace(long sequence, Instant capturedAt, String phase,
                                  String role, String stepId, String initiator,
                                  String evidenceSource, String expected, String observed) {
            String digest = digest(method, path, status, requestBody, responseBody);
            String signature = requestSignature(method, path, requestBody);
            return new Exchange(method, path, url, status, requestBody, responseBody,
                    requestHeaders, responseHeaders, sequence, capturedAt, phase, signature, digest,
                    role, stepId, initiator, evidenceSource, expected, observed,
                    observedAuthMethod, failureReason);
        }

        public Exchange withResponseBody(String body) {
            return new Exchange(method, path, url, status, requestBody, body,
                    requestHeaders, responseHeaders, sequence, capturedAt, phase, signature,
                    digest(method, path, status, requestBody, body), role, stepId, initiator,
                    evidenceSource, expected, observed, observedAuthMethod, failureReason);
        }

        public Exchange withPhase(String selectedPhase) {
            return new Exchange(method, path, url, status, requestBody, responseBody,
                    requestHeaders, responseHeaders, sequence, capturedAt, selectedPhase,
                    signature, digest, role, stepId, initiator, evidenceSource,
                    expected, observed, observedAuthMethod, failureReason);
        }

        public Exchange withRegistrationRedaction() {
            return new Exchange(method, path, url, status,
                    PracticeShopJsonEvidence.redactRegistration(requestBody),
                    PracticeShopJsonEvidence.redactRegistration(responseBody),
                    requestHeaders, responseHeaders, sequence, capturedAt, phase,
                    signature, digest(method, path, status, requestBody, responseBody),
                    role, stepId, initiator, evidenceSource, expected, observed,
                    observedAuthMethod, failureReason);
        }

        public Exchange withObservedAuthMethod(String authMethod) {
            return new Exchange(method, path, url, status, requestBody, responseBody,
                    requestHeaders, responseHeaders, sequence, capturedAt, phase, signature,
                    digest, role, stepId, initiator, evidenceSource, expected, observed,
                    authMethod, failureReason);
        }

        public Exchange withFailureReason(String reason) {
            return new Exchange(method, path, url, status, requestBody, responseBody,
                    requestHeaders, responseHeaders, sequence, capturedAt, phase, signature,
                    digest, role, stepId, initiator, evidenceSource, expected, observed,
                    observedAuthMethod, reason);
        }

        public String contentType() {
            return responseHeaders.entrySet().stream()
                    .filter(entry -> entry.getKey().equalsIgnoreCase("content-type"))
                    .map(java.util.Map.Entry::getValue).findFirst().orElse("");
        }

        public boolean isJsonResponse() { return contentType().toLowerCase().contains(
                atf.api.core.http.request.MediaTypes.APPLICATION_JSON); }
        public String authType() {
            if (!observedAuthMethod.isBlank()) return observedAuthMethod;
            if (requestHeaders.keySet().stream().anyMatch(key -> key.equalsIgnoreCase("authorization"))) {
                String value = requestHeaders.entrySet().stream()
                        .filter(entry -> entry.getKey().equalsIgnoreCase("authorization"))
                        .map(Map.Entry::getValue).findFirst().orElse("").toLowerCase();
                if (value.startsWith(PracticeShopApiAuthType.BEARER.wireValue())) {
                    return PracticeShopApiAuthType.BEARER.wireValue();
                }
                if (value.startsWith(PracticeShopApiAuthType.BASIC.wireValue())) {
                    return PracticeShopApiAuthType.BASIC.wireValue();
                }
                return PracticeShopApiAuthType.AUTHORIZATION_HEADER.wireValue();
            }
            if (requestHeaders.keySet().stream().anyMatch(key -> key.equalsIgnoreCase("x-api-key"))) {
                return PracticeShopApiAuthType.API_KEY.wireValue();
            }
            if (requestHeaders.keySet().stream().anyMatch(key -> key.equalsIgnoreCase("cookie"))) {
                return PracticeShopApiAuthType.COOKIE.wireValue();
            }
            return PracticeShopApiAuthType.NONE.wireValue();
        }
        public String summary() { return method + " " + path + " → " + statusLabel() + " " + contentType(); }

        public String statusLabel() { return status < 0 ? "Unavailable" : Integer.toString(status); }

        public String eventId() { return sequence > 0 ? "E" + sequence : ""; }

        public String correlationKey() {
            return (method == null ? "" : method.toUpperCase()) + " "
                    + PracticeShopApiExchangeSelector.normalize(path);
        }

        public String evidenceSource() { return evidenceSource; }

        /**
         * Human-facing summary for assertions and report evidence. A browser
         * capture alias is never allowed to obscure the real HTTP operation;
         * when one is observed it remains explicit metadata in the suffix.
         */
        public String reportSummary() {
            PracticeShopApiOperation operation = PracticeShopApiOperation.resolve(method, path);
            String reportMethod = operation == PracticeShopApiOperation.UNKNOWN
                    ? method : operation.reportMethod();
            String summary = reportMethod + " " + path + " → " + statusLabel() + " " + contentType();
            return operation != PracticeShopApiOperation.UNKNOWN
                    && !reportMethod.equalsIgnoreCase(method)
                    ? summary + " (observed method=" + method + ")" : summary;
        }

        private static String digest(String method, String path, int status,
                                     String requestBody, String responseBody) {
            try {
                byte[] bytes = (method + "\n" + path + "\n" + status + "\n"
                        + requestBody + "\n" + responseBody).getBytes(StandardCharsets.UTF_8);
                byte[] hash = MessageDigest.getInstance("SHA-256").digest(bytes);
                StringBuilder result = new StringBuilder(hash.length * 2);
                for (byte value : hash) result.append(String.format("%02x", value));
                return result.toString();
            } catch (java.security.NoSuchAlgorithmException error) {
                throw new IllegalStateException("SHA-256 is unavailable", error);
            }
        }

        /** Stable request identity: retries share it, but different JSON bodies do not. */
        private static String requestSignature(String method, String path, String requestBody) {
            String target = path == null ? "/" : path;
            URI uri = URI.create("https://trace.invalid"
                    + (target.startsWith("/") ? target : "/" + target));
            Map<String, String> query = new java.util.LinkedHashMap<>();
            if (uri.getRawQuery() != null) {
                for (String pair : uri.getRawQuery().split("&")) {
                    String[] parts = pair.split("=", 2);
                    query.put(java.net.URLDecoder.decode(parts[0], StandardCharsets.UTF_8),
                            parts.length == 1 ? "" : java.net.URLDecoder.decode(parts[1], StandardCharsets.UTF_8));
                }
            }
            return ReportRequestSignature.forRequest(new ReportRequest(
                    method == null ? "GET" : method, uri.getRawPath(), query,
                    Map.of(), "none", requestBody));
        }
    }
}
