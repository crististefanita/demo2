package atf.api.tests.api.headers;

import atf.api.core.reporting.AtfReportPorts;
import atf.api.execution.ApiModule;
import atf.api.execution.lifecycle.ApiScenarioLifecycleRunner;
import atf.api.tests.support.ApiAcceptancePlanBuilder;
import atf.api.tests.support.ApiAcceptancePublisher;

import java.util.List;

/** Extent projection for header, cache and rate-limit behavior. */
public final class ApiHeadersAndEdgeCaseScenarios {
    public static final String REPORT_OWNER = "ApiHeadersAndEdgeCasesTests";
    public static final String EXPORT = "export";
    public static final String CATALOG_PAGE_TITLE = ApiHeaderRoutes.catalogPageTitle();
    public static final String EXPORT_TITLE = ApiHeaderRoutes.exportTitle();
    public static final String NOT_MODIFIED_TITLE = ApiHeaderRoutes.notModifiedTitle();
    public static final List<String> REPORT_SCENARIO_TITLES = List.of(
            CATALOG_PAGE_TITLE, EXPORT_TITLE, NOT_MODIFIED_TITLE);

    private ApiHeadersAndEdgeCaseScenarios() { }

    public static void publishSelected(AtfReportPorts.Session session, ApiModule api,
                                       ApiScenarioLifecycleRunner lifecycle, String method) {
        if (EXPORT.equals(method)) {
            publishOne(session, api, lifecycle, EXPORT_TITLE,
                    ApiHeaderRoutes.exportTags(), ApiHeaderRoutes.exportRequest(),
                    ApiHeaderRoutes.exportContract());
            return;
        }
        throw new IllegalArgumentException("Unknown header API scenario method: " + method);
    }

    public static String scenarioTitle(String method) {
        if (EXPORT.equals(method)) return EXPORT_TITLE;
        throw new IllegalArgumentException("Unknown header API scenario method: " + method);
    }

    public static void publish(AtfReportPorts.Session session, ApiModule api,
                               ApiScenarioLifecycleRunner lifecycle) {
        publishOne(session, api, lifecycle, CATALOG_PAGE_TITLE,
                ApiHeaderRoutes.catalogPageTags(), ApiHeaderRoutes.catalogPageRequest(),
                ApiHeaderRoutes.catalogPageContract());
        publishOne(session, api, lifecycle, EXPORT_TITLE,
                ApiHeaderRoutes.exportTags(), ApiHeaderRoutes.exportRequest(),
                ApiHeaderRoutes.exportContract());
        publishOne(session, api, lifecycle, NOT_MODIFIED_TITLE,
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
