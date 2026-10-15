package atf.api.tests.api.requests;

import atf.api.core.http.AuthType;
import atf.api.core.reporting.AtfReportStatus;
import atf.api.tests.support.ApiTestFixture;
import atf.api.console.junit.AtfConsoleTestExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Focused request-shape tests: query parameters, large JSON and auth profiles. */
@ExtendWith(AtfConsoleTestExtension.class)
class RequestVariantsApiTests {
    private ApiTestFixture fixture;

    @BeforeEach
    void setUp() {
        fixture = new ApiTestFixture();
    }

    @Test
    void sendsQueryParametersWithBearerProfileAndValidatesNestedResponse() {
        var request = RequestVariantRoutes.searchRequest();
        var execution = fixture.api().executor().execute(request);
        var decision = fixture.validate(execution, RequestVariantRoutes.searchContract());

        assertAll(
                () -> assertEquals(200, execution.response().status()),
                () -> assertTrue(execution.response().url().contains("page=2")),
                () -> assertTrue(execution.response().url().contains("include=details")),
                () -> assertEquals("bearer", request.authProfile()),
                () -> assertEquals(AuthType.BEARER, request.route().authType()),
                () -> assertTrue(execution.response().body().contains("history")),
                () -> assertEquals(AtfReportStatus.PASS, decision.status()));
    }

    @Test
    void sendsLargeNestedBodyWithApiKeyProfileAndValidatesAcceptedJob() {
        var request = RequestVariantRoutes.bulkImportRequest();
        var execution = fixture.api().executor().execute(request);
        var decision = fixture.validate(execution, RequestVariantRoutes.bulkImportContract());

        assertAll(
                () -> assertEquals(202, execution.response().status()),
                () -> assertEquals("api-key", request.authProfile()),
                () -> assertEquals(AuthType.API_KEY, request.route().authType()),
                () -> assertTrue(request.bodyText().length() > 700),
                () -> assertTrue(request.bodyText().contains("records")),
                () -> assertEquals(AtfReportStatus.PASS, decision.status()));
    }

    @Test
    void sendsNestedUpdateWithBasicProfileAndQueryParameter() {
        var request = RequestVariantRoutes.profileUpdateRequest();
        var execution = fixture.api().executor().execute(request);
        var decision = fixture.validate(execution, RequestVariantRoutes.profileUpdateContract());

        assertAll(
                () -> assertEquals(200, execution.response().status()),
                () -> assertTrue(execution.response().url().contains("expand=permissions")),
                () -> assertEquals("basic", request.authProfile()),
                () -> assertEquals(AuthType.BASIC, request.route().authType()),
                () -> assertTrue(execution.response().body().contains("inherited")),
                () -> assertEquals(AtfReportStatus.PASS, decision.status()));
    }

    @Test
    void sendsCustomAuthProfileWithParameterRichBody() {
        var request = RequestVariantRoutes.preferencesRequest();
        var execution = fixture.api().executor().execute(request);
        var decision = fixture.validate(execution, RequestVariantRoutes.preferencesContract());

        assertAll(
                () -> assertEquals(200, execution.response().status()),
                () -> assertTrue(execution.response().url().contains("locale=ro-RO")),
                () -> assertEquals("custom", request.authProfile()),
                () -> assertEquals(AuthType.CUSTOM, request.route().authType()),
                () -> assertTrue(request.bodyText().contains("notifications")),
                () -> assertEquals(AtfReportStatus.PASS, decision.status()));
    }
}
