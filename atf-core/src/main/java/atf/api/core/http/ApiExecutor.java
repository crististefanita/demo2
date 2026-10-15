package atf.api.core.http;

import java.net.URI;
import atf.api.core.config.ApiSettings;
import atf.api.core.config.ApiSettingsSnapshot;
import atf.api.core.reporting.AtfConsoleEvent;
import atf.api.core.reporting.AtfConsoleSink;
import atf.api.core.reporting.AtfReportStatus;
import atf.api.core.security.SensitiveDataRedactor;
import java.time.Duration;
import java.util.Objects;
import java.util.ArrayList;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.CompletableFuture;

public final class ApiExecutor {
    private final ApiTransport transport;
    private final ApiUrlBuilder urls;
    private final RetryPolicy retryPolicy;
    private final ApiExecutionOptions defaultOptions;
    private final ApiExecutionClock clock;
    private final AtfConsoleSink console;
    private final SensitiveDataRedactor redactor = new SensitiveDataRedactor();

    public ApiExecutor(ApiTransport transport, URI baseUri) {
        this(transport, baseUri, RetryPolicy.none(), ApiExecutionClock.system(), AtfConsoleSink.noOp());
    }

    public ApiExecutor(ApiTransport transport, URI baseUri, RetryPolicy retryPolicy) {
        this(transport, baseUri, retryPolicy, ApiExecutionClock.system(), AtfConsoleSink.noOp());
    }

    public ApiExecutor(ApiTransport transport, URI baseUri, AtfConsoleSink console) {
        this(transport, baseUri, RetryPolicy.none(), ApiExecutionClock.system(), console);
    }

    ApiExecutor(ApiTransport transport, URI baseUri, RetryPolicy retryPolicy, ApiExecutionClock clock) {
        this(transport, baseUri, retryPolicy, clock, AtfConsoleSink.noOp());
    }

    private ApiExecutor(ApiTransport transport, URI baseUri, RetryPolicy retryPolicy,
                        ApiExecutionClock clock, AtfConsoleSink console) {
        this.transport = Objects.requireNonNull(transport, "transport");
        this.urls = new ApiUrlBuilder(Objects.requireNonNull(baseUri, "baseUri"));
        this.retryPolicy = Objects.requireNonNull(retryPolicy, "retryPolicy");
        this.defaultOptions = ApiExecutionOptions.defaults();
        this.clock = Objects.requireNonNull(clock, "clock");
        this.console = console == null ? AtfConsoleSink.noOp() : console;
    }

    public ApiExecutor(ApiTransport transport, ApiSettings settings) {
        this(transport, Objects.requireNonNull(settings, "settings").snapshot());
    }

    /**
     * Builds an executor from the immutable settings owned by one execution
     * context. The package composition root uses this overload so the
     * executor and the exposed module snapshot cannot diverge.
     */
    public ApiExecutor(ApiTransport transport, ApiSettingsSnapshot settings) {
        this(transport, settings, ApiExecutionClock.system(), AtfConsoleSink.noOp());
    }

    public ApiExecutor(ApiTransport transport, ApiSettingsSnapshot settings, AtfConsoleSink console) {
        this(transport, settings, ApiExecutionClock.system(), console);
    }

    ApiExecutor(ApiTransport transport, ApiSettingsSnapshot settings, ApiExecutionClock clock) {
        this(transport, settings, clock, AtfConsoleSink.noOp());
    }

    private ApiExecutor(ApiTransport transport, ApiSettingsSnapshot settings,
                        ApiExecutionClock clock, AtfConsoleSink console) {
        this.transport = Objects.requireNonNull(transport, "transport");
        var snapshot = Objects.requireNonNull(settings, "settings");
        this.urls = new ApiUrlBuilder(snapshot.baseUri());
        this.retryPolicy = RetryPolicy.onStatuses(snapshot.retryStatuses(), snapshot.maxAttempts(), Duration.ZERO);
        this.defaultOptions = new ApiExecutionOptions(snapshot.timeout());
        this.clock = Objects.requireNonNull(clock, "clock");
        this.console = console == null ? AtfConsoleSink.noOp() : console;
    }

    public ApiExecutionResult execute(ApiRequest request, ApiSettings settings, CancellationToken cancellationToken) {
        Objects.requireNonNull(settings, "settings");
        return execute(request, new ApiExecutionOptions(settings.timeout()), cancellationToken);
    }

    public ApiExecutionResult execute(ApiRequest request) {
        return execute(request, defaultOptions, CancellationToken.none());
    }

    public ApiExecutionResult execute(ApiRequest request, Duration timeout, CancellationToken cancellationToken) {
        return execute(request, new ApiExecutionOptions(timeout), cancellationToken);
    }

    public ApiExecutionResult execute(ApiRequest request, ApiExecutionOptions options, CancellationToken cancellationToken) {
        Objects.requireNonNull(request, "request");
        Objects.requireNonNull(options, "options");
        Objects.requireNonNull(cancellationToken, "cancellationToken");
        String consoleName = request.method() + " " + request.path();
        long consoleStarted = clock.nanoTime();
        console.publish(AtfConsoleEvent.started(AtfConsoleEvent.Scope.REQUEST, consoleName));
        console.publish(AtfConsoleEvent.detail(AtfConsoleEvent.Scope.REQUEST,
                "method", request.method()));
        var deadline = new ApiExecutionDeadline(clock, options.timeout());
        var retryWaiter = new ApiRetryWaiter(clock, deadline);
        String url = urls.build(request);
        console.publish(AtfConsoleEvent.detail(AtfConsoleEvent.Scope.REQUEST,
                "url", redactor.redactText(url)));
        if (!request.bodyText().isBlank()) {
            console.publish(AtfConsoleEvent.code(AtfConsoleEvent.Scope.REQUEST,
                    "request.body", redactor.redactJson(request.bodyText())));
        }
        var retryEvents = new ArrayList<RetryEvent>();
        var attemptHistory = new ArrayList<ApiAttemptRecord>();
        int attempt = 1;
        ApiResponse response;
        try {
            while (true) {
            cancellationToken.throwIfCancelled();
            long attemptStarted = clock.nanoTime();
            long remainingNanos = deadline.remainingNanos();
            if (remainingNanos <= 0) {
                throw new ApiExecutionException("API execution timed out after " + options.timeout(),
                        new TimeoutException("execution deadline exceeded"));
            }
            CompletableFuture<ApiResponse> pending = transport.send(request, url, request.authProfile());
            try {
                while (true) {
                    cancellationToken.throwIfCancelled();
                    remainingNanos = deadline.remainingNanos();
                    if (remainingNanos <= 0) {
                        pending.cancel(true);
                        throw new ApiExecutionException("API execution timed out after " + options.timeout(),
                                new TimeoutException("execution deadline exceeded"));
                    }
                    try {
                        response = pending.get(Math.min(remainingNanos, TimeUnit.MILLISECONDS.toNanos(50)),
                                TimeUnit.NANOSECONDS);
                        break;
                    } catch (TimeoutException pollTimeout) {
                        // Poll in short slices so cancellation is observed while transport is pending.
                    }
                }
            } catch (CancellationException error) {
                pending.cancel(true);
                throw error;
            } catch (InterruptedException error) {
                pending.cancel(true);
                Thread.currentThread().interrupt();
                throw new ApiExecutionException("API execution interrupted", error);
            } catch (ExecutionException error) {
                Throwable cause = error.getCause() == null ? error : error.getCause();
                var retry = retryPolicy.nextFailure(cause, attempt);
                if (retry.isEmpty()) {
                    throw new ApiExecutionException("API transport failed", cause);
                }
                attemptHistory.add(new ApiAttemptRecord(attempt, 0,
                        Duration.ofNanos(Math.max(0, clock.nanoTime() - attemptStarted)),
                        ApiAttemptOutcome.RETRYABLE_FAILURE, retry.get().reason()));
                retryEvents.add(retry.get());
                retryWaiter.await(retry.get().delay(), cancellationToken);
                attempt++;
                continue;
            }
            var retry = retryPolicy.next(response, attempt);
            if (retry.isEmpty()) {
                attemptHistory.add(new ApiAttemptRecord(attempt, response.status(),
                        Duration.ofNanos(Math.max(0, clock.nanoTime() - attemptStarted)),
                        ApiAttemptOutcome.SUCCESS, "final response"));
                break;
            }
            attemptHistory.add(new ApiAttemptRecord(attempt, response.status(),
                    Duration.ofNanos(Math.max(0, clock.nanoTime() - attemptStarted)),
                    ApiAttemptOutcome.RETRYABLE_RESPONSE, retry.get().reason()));
            retryEvents.add(retry.get());
            retryWaiter.await(retry.get().delay(), cancellationToken);
            attempt++;
            }
            var result = new ApiExecutionResult(response, attempt, Duration.ofNanos(deadline.elapsedNanos()), retryEvents, attemptHistory);
            console.publish(AtfConsoleEvent.detail(AtfConsoleEvent.Scope.REQUEST,
                    "response.status", Integer.toString(response.status())));
            console.publish(AtfConsoleEvent.detail(AtfConsoleEvent.Scope.REQUEST,
                    "response.duration.ms", Long.toString(response.duration().toMillis())));
            console.publish(AtfConsoleEvent.code(AtfConsoleEvent.Scope.REQUEST,
                    "response.body", redactor.redactJson(response.body())));
            console.publish(AtfConsoleEvent.completed(AtfConsoleEvent.Scope.REQUEST,
                    consoleName, AtfReportStatus.PASS, elapsedMillis(consoleStarted)));
            return result;
        } catch (RuntimeException error) {
            console.publish(AtfConsoleEvent.completed(AtfConsoleEvent.Scope.REQUEST,
                    consoleName, AtfReportStatus.FAIL, elapsedMillis(consoleStarted)));
            throw error;
        }
    }

    private long elapsedMillis(long startedAt) {
        return Math.max(0, (clock.nanoTime() - startedAt) / 1_000_000);
    }
}
