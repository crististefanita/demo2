package atf.api.tests.api.reporting;

import atf.api.core.config.ApiSettings;
import atf.api.core.http.ApiExecutionResult;
import atf.api.core.http.ApiAuthenticator;
import atf.api.core.http.AuthProfile;
import atf.api.core.http.ConfiguredApiAuthenticator;
import atf.api.core.http.HttpClientTransport;
import atf.api.core.reporting.AtfReportPorts;
import atf.api.core.reporting.AtfReportStatus;
import atf.api.execution.ApiModule;
import atf.api.execution.ApiModuleFactory;
import atf.api.validation.contract.ContractValidators;
import atf.api.validation.contract.ResponseContract;
import atf.api.validation.publishing.ResponseContractReporter;
import atf.api.reporting.ExtentReportSession;
import atf.api.tests.support.TestReportPaths;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.nio.file.Files;
import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Opt-in live HTTP acceptance smoke. This test proves the report contract
 * using the real transport and the configured public API, without performing
 * writes. It is intentionally opt-in so the default build stays deterministic
 * and does not depend on network availability.
 */
@Tag("live")
class ApiLiveReportTest {
    private static final ResponseContractReporter VALIDATOR = new ResponseContractReporter();

    @Test
    void rendersReadOnlyLiveApiResponsesAndValidations() throws Exception {
        Assumptions.assumeTrue(Boolean.getBoolean("atf.liveApi.enabled"),
                "Enable with -Datf.liveApi.enabled=true; live tests are opt-in.");

        URI baseUri = URI.create(System.getProperty("atf.liveApi.baseUri", "https://gorest.co.in"));
        Duration timeout = Duration.ofSeconds(Long.getLong("atf.liveApi.timeoutSeconds", 20L));
        var authenticator = new ConfiguredApiAuthenticator(Map.of("default", AuthProfile.none("default")));
        var transport = new HttpClientTransport(
                HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build(),
                authenticator,
                timeout);
        ApiModule api = ApiModuleFactory.fromTransport(
                transport,
                new ApiSettings(baseUri, "default", timeout, java.util.Set.of(429), 2));
        var report = TestReportPaths.timestampedReport("api-atf-live-report.html");

        try (var session = new ExtentReportSession(report)) {
            var users = session.startScenario("UsersApiTests", "GET /public/v2/users — live collection contract",
                    List.of("api", "users", "live", "contract"));
            var usersBody = users.startPhase("TEST BODY");
            ApiExecutionResult usersResult = api.users().getCollection();
            publish(usersBody, "GET /public/v2/users", usersResult, ContractValidators.userCollection());
            usersBody.complete(AtfReportStatus.PASS);
            users.complete(AtfReportStatus.PASS);
            users.close();

            int userId = firstId(usersResult.response().body());
            var user = session.startScenario("UsersApiTests", "GET /public/v2/users/{id} — live item contract",
                    List.of("api", "users", "live", "contract"));
            var userBody = user.startPhase("TEST BODY");
            ApiExecutionResult userResult = api.users().get(userId);
            publish(userBody, "GET /public/v2/users/" + userId, userResult, ContractValidators.userItem());
            userBody.complete(AtfReportStatus.PASS);
            user.complete(AtfReportStatus.PASS);
            user.close();

            var posts = session.startScenario("PostsApiTests", "GET /public/v2/posts — live collection contract",
                    List.of("api", "posts", "live", "contract"));
            var postsBody = posts.startPhase("TEST BODY");
            ApiExecutionResult postsResult = api.posts().getCollection();
            publish(postsBody, "GET /public/v2/posts", postsResult, ContractValidators.postCollection());
            postsBody.complete(AtfReportStatus.PASS);
            posts.complete(AtfReportStatus.PASS);
            posts.close();

            int postId = firstId(postsResult.response().body());
            var post = session.startScenario("PostsApiTests", "GET /public/v2/posts/{id} — live item contract",
                    List.of("api", "posts", "live", "contract"));
            var postBody = post.startPhase("TEST BODY");
            ApiExecutionResult postResult = api.posts().get(postId);
            publish(postBody, "GET /public/v2/posts/" + postId, postResult, ContractValidators.postItem());
            postBody.complete(AtfReportStatus.PASS);
            post.complete(AtfReportStatus.PASS);
            post.close();

            var relationship = session.startScenario("PostsApiTests",
                    "GET /public/v2/users/{userId}/posts — live relationship contract",
                    List.of("api", "posts", "live", "relationship", "contract"));
            var relationshipBody = relationship.startPhase("TEST BODY");
            ApiExecutionResult relationshipResult = api.posts().forUser(userId);
            publish(relationshipBody, "GET /public/v2/users/" + userId + "/posts", relationshipResult,
                    ContractValidators.relationshipCollection(Integer.toString(userId)));
            relationshipBody.complete(AtfReportStatus.PASS);
            relationship.complete(AtfReportStatus.PASS);
            relationship.close();
        }

        assertTrue(Files.isRegularFile(report), "Live API execution must publish an HTML report");
        assertTrue(Files.size(report) > 0, "Live API report must not be empty");
    }

    private static void publish(AtfReportPorts.Phase phase, String title,
                                ApiExecutionResult execution, ResponseContract contract) {
        var response = execution.response();
        var request = phase.startRequest(title, "GET", URI.create(response.url()).getPath());
        request.detail("Duration", response.duration().toMillis() + " ms");
        request.detail("URL", response.url());
        request.detail("Attempt", Integer.toString(execution.attempts()));
        request.json("RESPONSE JSON", response.body());
        var decision = VALIDATOR.validate(request, "Contract validation", response, contract, false);
        request.complete(decision.status());
        request.close();
    }

    private static int firstId(String body) throws Exception {
        var root = new com.fasterxml.jackson.databind.ObjectMapper().readTree(body);
        int id = root.path(0).path("id").asInt(0);
        if (id <= 0) throw new IllegalStateException("Live API returned no usable id");
        return id;
    }
}
