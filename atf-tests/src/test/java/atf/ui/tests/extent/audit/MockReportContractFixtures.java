package atf.ui.tests.extent.audit;

import atf.api.core.http.ApiOperationLabel;
import atf.api.core.http.HttpHeader;
import atf.api.core.http.HttpMethod;
import atf.api.core.http.JsonBodyPolicy;
import atf.api.core.http.MediaTypes;
import atf.api.mock.AtfMockConfiguration;
import atf.api.mock.MockRequestBodyMode;
import atf.api.mock.MockRoute;
import atf.api.tests.support.ApiEnvironment;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/** Canonical fixtures for the mock-to-report contract auditor tests. */
final class MockReportContractFixtures {
    static final String DEMO_PATH = "/demo";
    static final String FALLBACK_BODY = "{\"message\":\"fallback\"}";
    static final String MOCK_SCENARIO = "demo";
    static final String API_KEY_PROFILE = "api-key";
    static final String BEARER_PROFILE = "bearer";
    static final String CUSTOM_PROFILE = "custom";
    static final String MODE_PARAMETER = "mode";
    static final String VALIDATE_MODE = "validate";
    static final String PAGE_PARAMETER = "page";
    static final String FIRST_PAGE = "1";
    static final String SECOND_PAGE = "2";
    static final String REQUEST_BODY = "{\"name\":\"atf\"}";
    static final String OK_BODY = "{\"ok\":true}";
    static final String EMPTY_OBJECT = "{}";
    static final String ACCEPTED_BODY = "{\"job\":{\"state\":\"accepted\"},\"items\":[1,2]}";
    static final String REQUEST_TREE_BODY = "{\"request\":true}";
    static final String BODY_MISMATCH = "{\"ok\":false}";
    static final String JSON = MediaTypes.APPLICATION_JSON;
    static final String PLAIN_TEXT = MediaTypes.TEXT_PLAIN;
    static final String BASE_URL = ApiEnvironment.MOCK_BASE_URI.toString();
    static final Duration NO_DELAY = Duration.ZERO;
    static final int OK_STATUS = 200;
    static final int ACCEPTED_STATUS = 202;
    static final int FALLBACK_STATUS = 500;

    private MockReportContractFixtures() { }

    static AtfMockConfiguration configuration(MockRoute... routes) {
        return new AtfMockConfiguration(List.of(routes), fallbackRoute());
    }

    static MockRoute acceptedPostRoute() {
        return new MockRoute(HttpMethod.POST.value(), DEMO_PATH, ACCEPTED_STATUS,
                OK_BODY,
                responseJsonHeaders(), NO_DELAY, API_KEY_PROFILE,
                Map.of(MODE_PARAMETER, VALIDATE_MODE),
                Map.of(HttpHeader.CONTENT_TYPE.value(), JSON,
                        HttpHeader.X_CLIENT.value(), "atf-java"),
                MockRequestBodyMode.JSON.value(), REQUEST_BODY);
    }

    static MockRoute transportGetRoute() {
        return new MockRoute(HttpMethod.GET.value(), DEMO_PATH, OK_STATUS, OK_BODY,
                responseJsonHeaders(), NO_DELAY, BEARER_PROFILE,
                Map.of(PAGE_PARAMETER, SECOND_PAGE),
                Map.of(HttpHeader.ACCEPT.value(), JSON));
    }

    static MockRoute responseTreePostRoute() {
        return new MockRoute(HttpMethod.POST.value(), DEMO_PATH, ACCEPTED_STATUS,
                ACCEPTED_BODY, jsonHeaders(), NO_DELAY);
    }

    static MockRoute bodyDriftGetRoute() {
        return new MockRoute(HttpMethod.GET.value(), DEMO_PATH, OK_STATUS, OK_BODY,
                Map.of(), NO_DELAY);
    }

    static MockRoute putRoute() {
        return new MockRoute(HttpMethod.PUT.value(), DEMO_PATH, OK_STATUS, OK_BODY,
                Map.of(), NO_DELAY);
    }

    static String operation(HttpMethod method) {
        return ApiOperationLabel.format(method, DEMO_PATH, null);
    }

    static String url(String path) {
        return BASE_URL + path;
    }

    static String acceptedUrl() {
        return url(DEMO_PATH) + "?" + MODE_PARAMETER + "=" + VALIDATE_MODE;
    }

    static String pagedUrl() {
        return url(DEMO_PATH) + "?" + PAGE_PARAMETER + "=" + FIRST_PAGE;
    }

    static ApiJsonEvidenceAuditor.Result acceptedEvidence() {
        return new ApiJsonEvidenceAuditor.Result(REQUEST_BODY, OK_BODY, List.of());
    }

    static ApiJsonEvidenceAuditor.Result responseTreeEvidence() {
        return new ApiJsonEvidenceAuditor.Result(REQUEST_TREE_BODY,
                "{ \"items\" : [1, 2], \"job\" : { \"state\" : \"accepted\" } }", List.of());
    }

    static MockReportContractAuditor.RenderedEvidence acceptedRenderedEvidence() {
        return new MockReportContractAuditor.RenderedEvidence(
                Map.of(MockReportEvidenceField.AUTH_PROFILE.key(), API_KEY_PROFILE,
                        MockReportEvidenceField.REQUEST_HEADERS.key(),
                        "{Content-Type=" + JSON + ", X-Client=atf-java}"),
                Map.of(MockReportEvidenceField.HTTP_STATUS.key(), String.valueOf(ACCEPTED_STATUS),
                        MockReportEvidenceField.CONTENT_TYPE.key(), JSON,
                        MockReportEvidenceField.RESPONSE_HEADERS.key(),
                        "[Content-Type=" + JSON + ", " + HttpHeader.X_MOCK_SCENARIO.value()
                                + "=" + MOCK_SCENARIO + "]"),
                Map.of(MockReportEvidenceField.RESPONSE_STATUS.key(), String.valueOf(ACCEPTED_STATUS),
                        MockReportEvidenceField.RESPONSE_URL.key(), acceptedUrl()));
    }

    static MockReportContractAuditor.RenderedEvidence transportDriftEvidence() {
        return new MockReportContractAuditor.RenderedEvidence(
                Map.of(MockReportEvidenceField.AUTH_PROFILE.key(), CUSTOM_PROFILE,
                        MockReportEvidenceField.REQUEST_HEADERS.key(),
                        "{Accept=" + PLAIN_TEXT + "}"),
                Map.of(MockReportEvidenceField.HTTP_STATUS.key(), String.valueOf(OK_STATUS),
                        MockReportEvidenceField.CONTENT_TYPE.key(), PLAIN_TEXT,
                        MockReportEvidenceField.RESPONSE_HEADERS.key(),
                        "[Content-Type=" + PLAIN_TEXT + "]"),
                Map.of(MockReportEvidenceField.RESPONSE_STATUS.key(), String.valueOf(FALLBACK_STATUS),
                        MockReportEvidenceField.RESPONSE_URL.key(), url("/other")));
    }

    static MockReportContractAuditor.RenderedEvidence emptyRenderedEvidence() {
        return MockReportContractAuditor.RenderedEvidence.empty();
    }

    static MockRoute fallbackRoute() {
        return new MockRoute(MockRoute.ANY_METHOD, MockRoute.ANY_PATH, FALLBACK_STATUS,
                FALLBACK_BODY, Map.of(), NO_DELAY);
    }

    private static Map<String, String> jsonHeaders() {
        return Map.of(HttpHeader.CONTENT_TYPE.value(), JSON);
    }

    private static Map<String, String> responseJsonHeaders() {
        return Map.of(HttpHeader.CONTENT_TYPE.value(), JSON,
                HttpHeader.X_MOCK_SCENARIO.value(), MOCK_SCENARIO);
    }

    enum ViolationSignal {
        AUTH_PROFILE("AUTH PROFILE"),
        QUERY("query page"),
        REQUEST_HEADER("request header"),
        RESPONSE_HEADER("response header"),
        EXECUTION_STATUS("execution evidence response.status"),
        NO_RESPONSE_CONTRACT("no response contract"),
        EXPECTED_STATUS("expected HTTP 200"),
        BODY_DRIFT("differs from mock YAML body"),
        REQUEST_JSON_BODY("non-empty JSON request body"),
        RESPONSE_JSON_BODY("non-empty JSON response body");

        private final String text;

        ViolationSignal(String text) {
            this.text = text;
        }

        boolean occursIn(List<String> violations) {
            return violations.stream().anyMatch(violation -> violation.contains(text));
        }
    }
}
