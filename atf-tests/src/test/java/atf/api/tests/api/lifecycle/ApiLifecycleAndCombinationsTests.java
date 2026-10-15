package atf.api.tests.api.lifecycle;

import atf.api.core.reporting.AtfReportStatus;
import atf.api.execution.ApiModule;
import atf.api.execution.lifecycle.ApiScenarioLifecycleRunner;
import atf.api.execution.lifecycle.ApiScenarioOutcome;
import atf.api.reporting.ExtentReportSession;
import atf.api.tests.support.ApiAcceptancePlanBuilder;
import atf.api.tests.support.ApiAcceptancePublisher;
import atf.api.tests.support.ApiTestFixture;
import atf.api.console.junit.AtfConsoleTestExtension;
import atf.api.tests.support.TestReportPaths;
import atf.api.validation.contract.ContractValidators;
import atf.api.validation.contract.ResponseContract;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Explicit lifecycle test: setup, body, teardown and final report verdict. */
@ExtendWith(AtfConsoleTestExtension.class)
class ApiLifecycleAndCombinationsTests {
    @Test
    void executesSetupBodyAndTeardownThroughTheSameApiAndReportLayers() throws Exception {
        ApiModule api = new ApiTestFixture().api();
        var report = TestReportPaths.privateReport("api-lifecycle-test.html");
        ApiScenarioOutcome outcome;

        try (var session = new ExtentReportSession(report)) {
            outcome = new ApiScenarioLifecycleRunner().run(session,
                    ApiAcceptancePlanBuilder.scenario(
                                    "ApiLifecycleAndCombinationsTests",
                                    "Users API — setup → test → teardown",
                                    java.util.List.of("api", "users", "lifecycle"))
                            .setup(phase -> ApiAcceptancePublisher.publish(phase, "GET /public/v2/users",
                                    api.users().getCollection(), ContractValidators.userCollection(), false, null))
                            .body(phase -> ApiAcceptancePublisher.publish(phase, "GET /public/v2/users/1",
                                    api.users().get(1),
                                    new ResponseContract().status(201).contentTypeContains("application/json"),
                                    false, null))
                            .teardown(phase -> ApiAcceptancePublisher.publish(phase, "GET /public/v2/posts",
                                    api.posts().getCollection(), ContractValidators.postCollection(), false, null))
                            .build());
        }

        String html = Files.readString(report);
        assertAll(
                () -> assertEquals(AtfReportStatus.FAIL, outcome.status()),
                () -> assertTrue(html.contains("SETUP")),
                () -> assertTrue(html.contains("TEST BODY")),
                () -> assertTrue(html.contains("TEARDOWN")),
                () -> assertTrue(html.contains("ApiLifecycleAndCombinationsTests")));
    }
}
