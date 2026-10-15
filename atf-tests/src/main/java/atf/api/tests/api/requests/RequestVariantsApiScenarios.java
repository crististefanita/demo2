package atf.api.tests.api.requests;

import atf.api.core.reporting.AtfReportPorts;
import atf.api.execution.ApiModule;
import atf.api.execution.lifecycle.ApiScenarioLifecycleRunner;
import atf.api.tests.support.ApiAcceptancePlanBuilder;
import atf.api.tests.support.ApiAcceptancePublisher;

/** Public Extent scenarios for request parameters, bodies and auth profiles. */
public final class RequestVariantsApiScenarios {
    private RequestVariantsApiScenarios() { }

    public static void publish(AtfReportPorts.Session session, ApiModule api,
                               ApiScenarioLifecycleRunner lifecycle) {
        run(lifecycle, session, plan(
                RequestVariantRoutes.searchTitle(), RequestVariantRoutes.searchTags()),
                phase -> {
                    var request = RequestVariantRoutes.searchRequest();
                    var execution = api.executor().execute(request);
                    ApiAcceptancePublisher.publish(phase, RequestVariantRoutes.searchTitle(), execution,
                            RequestVariantRoutes.searchContract(), false, null, request);
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

    private static void run(ApiScenarioLifecycleRunner lifecycle, AtfReportPorts.Session session,
                            ApiAcceptancePlanBuilder plan,
                            atf.api.execution.lifecycle.ApiScenarioPlan.PhaseAction body) {
        lifecycle.run(session, plan.body(body).build());
    }

    private static ApiAcceptancePlanBuilder plan(String scenario, java.util.List<String> tags) {
        return ApiAcceptancePlanBuilder.scenario("RequestVariantsApiTests", RequestVariantsApiScenarios.class.getPackageName(),
                scenario, tags);
    }
}
