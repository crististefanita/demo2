package atf.ui.practiceshop.support;

import atf.api.core.http.response.ApiExecutionResult;

import java.time.Instant;
import java.util.Objects;

/** Adapts direct API executions to the same exchange contract used by UI evidence. */
public final class PracticeShopApiExecutionExchange {
    private PracticeShopApiExecutionExchange() { }

    public static PracticeShopApiNetworkJournal.Exchange from(ApiExecutionResult execution,
                                                               long sequence,
                                                               String phase) {
        Objects.requireNonNull(execution, "execution");
        Objects.requireNonNull(execution.request(), "execution.request");
        Objects.requireNonNull(execution.response(), "execution.response");
        var request = execution.request();
        var response = execution.response();
        return new PracticeShopApiNetworkJournal.Exchange(
                request.method(), request.path(), response.url(), response.status(),
                request.bodyText(), response.body(), request.headers(), response.headers(),
                sequence, Instant.now(), phase, "", "",
                "BusinessFlow", phase,
                atf.ui.practiceshop.api.PracticeShopApiOperation.reportTitle(request.method(), request.path()),
                "HTTP_CLIENT", "", "status=" + response.status());
    }
}
