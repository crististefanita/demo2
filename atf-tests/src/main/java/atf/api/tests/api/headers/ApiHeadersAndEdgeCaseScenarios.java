package atf.api.tests.api.headers;

import atf.api.core.reporting.AtfReportPorts;
import atf.api.execution.ApiModule;
import atf.api.execution.lifecycle.ApiScenarioLifecycleRunner;
import atf.api.tests.support.ApiAcceptancePlanBuilder;
import atf.api.tests.support.ApiAcceptancePublisher;

/** Extent projection for header, cache and rate-limit behavior. */
public final class ApiHeadersAndEdgeCaseScenarios {
    public static final String REPORT_OWNER = "ApiHeadersAndEdgeCasesTests";
    public static final String EXPORT = "export";

    private ApiHeadersAndEdgeCaseScenarios() { }

    public static void publishSelected(AtfReportPorts.Session session, ApiModule api,
                                       ApiScenarioLifecycleRunner lifecycle, String method) {
        if (EXPORT.equals(method)) {
            publishOne(session, api, lifecycle, ApiHeaderRoutes.exportTitle(),
                    ApiHeaderRoutes.exportTags(), ApiHeaderRoutes.exportRequest(),
                    ApiHeaderRoutes.exportContract());
            return;
        }
        throw new IllegalArgumentException("Unknown header API scenario method: " + method);
    }

    public static String scenarioTitle(String method) {
        if (EXPORT.equals(method)) return ApiHeaderRoutes.exportTitle();
        throw new IllegalArgumentException("Unknown header API scenario method: " + method);
    }

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
        var plan = ApiAcceptancePlanBuilder.scenario(REPORT_OWNER,
                ApiHeadersAndEdgeCaseScenarios.class.getPackageName(), title, tags);
        lifecycle.run(session, plan.body(phase -> {
            var execution = api.executor().execute(request);
            ApiAcceptancePublisher.publish(phase, title, execution, contract, false, null, request);
        }).build());
    }
}
