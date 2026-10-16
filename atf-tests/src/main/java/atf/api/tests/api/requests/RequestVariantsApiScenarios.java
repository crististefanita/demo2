package atf.api.tests.api.requests;

import atf.api.core.reporting.AtfReportPorts;
import atf.api.execution.ApiModule;
import atf.api.execution.lifecycle.ApiScenarioLifecycleRunner;
import atf.api.tests.support.ApiAcceptancePlanBuilder;
import atf.api.tests.support.ApiAcceptancePublisher;

/** Public Extent scenarios for request parameters, bodies and auth profiles. */
public final class RequestVariantsApiScenarios {
    public static final String REPORT_OWNER = "RequestVariantsApiTests";
    public static final String SEARCH = "search";

    private RequestVariantsApiScenarios() { }

    public static void publishSelected(AtfReportPorts.Session session, ApiModule api,
                                       ApiScenarioLifecycleRunner lifecycle, String method) {
        switch (method) {
            case SEARCH -> publishSearch(session, api, lifecycle);
            default -> throw new IllegalArgumentException("Unknown request-variant API scenario method: " + method);
        }
    }

    public static String scenarioTitle(String method) {
        return switch (method) {
            case SEARCH -> RequestVariantRoutes.searchTitle();
            default -> throw new IllegalArgumentException("Unknown request-variant API scenario method: " + method);
        };
    }

    public static void publish(AtfReportPorts.Session session, ApiModule api,
                               ApiScenarioLifecycleRunner lifecycle) {
        publishSearch(session, api, lifecycle);

        run(lifecycle, session, plan(
                RequestVariantRoutes.invalidSearchQueryTitle(), RequestVariantRoutes.invalidSearchQueryTags()),
                phase -> {
                    var request = RequestVariantRoutes.invalidSearchQueryRequest();
                    var execution = api.executor().execute(request);
                    ApiAcceptancePublisher.publish(phase, RequestVariantRoutes.invalidSearchQueryTitle(), execution,
                            RequestVariantRoutes.invalidSearchQueryContract(), false, null, request);
                });

        run(lifecycle, session, plan(
                RequestVariantRoutes.missingPageSearchTitle(), RequestVariantRoutes.missingPageSearchTags()),
                phase -> {
                    var request = RequestVariantRoutes.missingPageSearchRequest();
                    var execution = api.executor().execute(request);
                    ApiAcceptancePublisher.publish(phase, RequestVariantRoutes.missingPageSearchTitle(), execution,
                            RequestVariantRoutes.missingPageSearchContract(), false, null, request);
                });

        run(lifecycle, session, plan(
                RequestVariantRoutes.emptyPageSearchTitle(), RequestVariantRoutes.emptyPageSearchTags()),
                phase -> {
                    var request = RequestVariantRoutes.emptyPageSearchRequest();
                    var execution = api.executor().execute(request);
                    ApiAcceptancePublisher.publish(phase, RequestVariantRoutes.emptyPageSearchTitle(), execution,
                            RequestVariantRoutes.emptyPageSearchContract(), false, null, request);
                });

        run(lifecycle, session, plan(
                RequestVariantRoutes.duplicateTagSearchTitle(), RequestVariantRoutes.duplicateTagSearchTags()),
                phase -> {
                    var request = RequestVariantRoutes.duplicateTagSearchRequest();
                    var execution = api.executor().execute(request);
                    ApiAcceptancePublisher.publish(phase, RequestVariantRoutes.duplicateTagSearchTitle(), execution,
                            RequestVariantRoutes.duplicateTagSearchContract(), false, null, request);
                });

        run(lifecycle, session, plan(
                RequestVariantRoutes.bulkImportTitle(), RequestVariantRoutes.bulkImportTags()),
                phase -> {
                    var request = RequestVariantRoutes.bulkImportRequest();
                    var execution = api.executor().execute(request);
                    ApiAcceptancePublisher.publish(phase, RequestVariantRoutes.bulkImportTitle(), execution,
                            RequestVariantRoutes.bulkImportContract(), false, null, request);
                });

        run(lifecycle, session, plan(
                RequestVariantRoutes.profileUpdateTitle(), RequestVariantRoutes.profileUpdateTags()),
                phase -> {
                    var request = RequestVariantRoutes.profileUpdateRequest();
                    var execution = api.executor().execute(request);
                    ApiAcceptancePublisher.publish(phase, RequestVariantRoutes.profileUpdateTitle(), execution,
                            RequestVariantRoutes.profileUpdateContract(), false, null, request);
                });

        run(lifecycle, session, plan(
                RequestVariantRoutes.preferencesTitle(), RequestVariantRoutes.preferencesTags()),
                phase -> {
                    var request = RequestVariantRoutes.preferencesRequest();
                    var execution = api.executor().execute(request);
                    ApiAcceptancePublisher.publish(phase, RequestVariantRoutes.preferencesTitle(), execution,
                            RequestVariantRoutes.preferencesContract(), false, null, request);
                });
    }

    private static void publishSearch(AtfReportPorts.Session session, ApiModule api,
                                      ApiScenarioLifecycleRunner lifecycle) {
        run(lifecycle, session, plan(
                RequestVariantRoutes.searchTitle(), RequestVariantRoutes.searchTags()),
                phase -> {
                    var request = RequestVariantRoutes.searchRequest();
                    var execution = api.executor().execute(request);
                    ApiAcceptancePublisher.publish(phase, RequestVariantRoutes.searchTitle(), execution,
                            RequestVariantRoutes.searchContract(), false, null, request);
                });
    }

    private static void run(ApiScenarioLifecycleRunner lifecycle, AtfReportPorts.Session session,
                            ApiAcceptancePlanBuilder plan,
                            atf.api.execution.lifecycle.ApiScenarioPlan.PhaseAction body) {
        lifecycle.run(session, plan.body(body).build());
    }

    private static ApiAcceptancePlanBuilder plan(String scenario, java.util.List<String> tags) {
        return ApiAcceptancePlanBuilder.scenario(REPORT_OWNER, RequestVariantsApiScenarios.class.getPackageName(),
                scenario, tags);
    }
}
