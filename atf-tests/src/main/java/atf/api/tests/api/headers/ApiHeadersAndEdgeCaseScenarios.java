package atf.api.tests.api.headers;

import atf.api.core.reporting.AtfReportPorts;
import atf.api.execution.ApiModule;
import atf.api.execution.lifecycle.ApiScenarioLifecycleRunner;
import atf.api.tests.support.ApiAcceptancePlanBuilder;
import atf.api.tests.support.ApiAcceptancePublisher;

/** Extent projection for header, cache and rate-limit behavior. */
public final class ApiHeadersAndEdgeCaseScenarios {
    private ApiHeadersAndEdgeCaseScenarios() { }

    public static void publish(AtfReportPorts.Session session, ApiModule api,
                               ApiScenarioLifecycleRunner lifecycle) {
        publishOne(session, api, lifecycle, ApiHeaderRoutes.catalogPageTitle(),
                ApiHeaderRoutes.catalogPageTags(), ApiHeaderRoutes.catalogPageRequest(),
                ApiHeaderRoutes.catalogPageContract());
        publishOne(session, api, lifecycle, ApiHeaderRoutes.exportTitle(),
                ApiHeaderRoutes.exportTags(), ApiHeaderRoutes.exportRequest(),
                ApiHeaderRoutes.exportContract());
        publishOne(session, api, lifecycle, ApiHeaderRoutes.notModifiedTitle(),
                ApiHeaderRoutes.notModifiedTags(), ApiHeaderRoutes.notModifiedRequest(),
                ApiHeaderRoutes.notModifiedContract());
    }

    private static void publishOne(AtfReportPorts.Session session, ApiModule api,
                                   ApiScenarioLifecycleRunner lifecycle, String title,
                                   java.util.List<String> tags, atf.api.core.http.ApiRequest request,
                                   atf.api.validation.contract.ResponseContract contract) {
        var plan = ApiAcceptancePlanBuilder.scenario("ApiHeadersAndEdgeCasesTests",
                ApiHeadersAndEdgeCaseScenarios.class.getPackageName(), title, tags);
        lifecycle.run(session, plan.body(phase -> {
            var execution = api.executor().execute(request);
            ApiAcceptancePublisher.publish(phase, title, execution, contract, false, null, request);
        }).build());
    }
}
