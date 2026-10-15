package atf.api.tests.api.headers;

import atf.api.console.junit.AtfConsoleTestExtension;
import atf.api.core.reporting.AtfReportStatus;
import atf.api.tests.support.ApiTestFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Deep offline API cases for sent headers, response headers and edge statuses. */
@ExtendWith(AtfConsoleTestExtension.class)
class ApiHeadersAndEdgeCasesTests {
    private ApiTestFixture fixture;

    @BeforeEach
    void setUp() {
        fixture = new ApiTestFixture();
    }

    @Test
    void preservesSentCorrelationAndAcceptHeadersAndValidatesPaginationHeaders() {
        var request = ApiHeaderRoutes.catalogPageRequest();
        var execution = fixture.api().executor().execute(request);
        var decision = fixture.validate(execution, ApiHeaderRoutes.catalogPageContract());

        assertAll(
                () -> assertEquals(200, execution.response().status()),
                () -> assertEquals("application/json", request.headers().get("Accept")),
                () -> assertEquals("corr-headers-001", request.headers().get("X-Correlation-Id")),
                () -> assertEquals("req-catalog-003", execution.response().header("X-Request-Id")),
                () -> assertEquals("97", execution.response().header("X-RateLimit-Remaining")),
                () -> assertEquals("W/catalog-3", execution.response().header("ETag")),
                () -> assertEquals(AtfReportStatus.PASS, decision.status()));
    }

    @Test
    void preservesRateLimitResponseAsAnExplicitContractOutcome() {
        var request = ApiHeaderRoutes.exportRequest();
        var execution = fixture.api().executor().execute(request);
        var decision = fixture.validate(execution, ApiHeaderRoutes.exportContract());

        assertAll(
                () -> assertEquals(429, execution.response().status()),
                () -> assertEquals("text/csv", request.headers().get("Accept")),
                () -> assertEquals("2.4", request.headers().get("X-Client-Version")),
                () -> assertEquals("2", execution.response().header("Retry-After")),
                () -> assertEquals("0", execution.response().header("X-RateLimit-Remaining")),
                () -> assertTrue(execution.response().body().contains("RATE_LIMITED")),
                () -> assertEquals(AtfReportStatus.PASS, decision.status()));
    }

    @Test
    void preservesConditionalRequestHeadersAndNotModifiedResponse() {
        var request = ApiHeaderRoutes.notModifiedRequest();
        var execution = fixture.api().executor().execute(request);
        var decision = fixture.validate(execution, ApiHeaderRoutes.notModifiedContract());

        assertAll(
                () -> assertEquals(304, execution.response().status()),
                () -> assertEquals("W/catalog-42", request.headers().get("If-None-Match")),
                () -> assertEquals("W/catalog-42", execution.response().header("ETag")),
                () -> assertEquals("max-age=60", execution.response().header("Cache-Control")),
                () -> assertTrue(execution.response().body().isBlank()),
                () -> assertEquals(AtfReportStatus.PASS, decision.status()));
    }
}
