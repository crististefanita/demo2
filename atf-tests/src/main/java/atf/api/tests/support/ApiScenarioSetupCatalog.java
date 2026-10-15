package atf.api.tests.support;

import atf.api.core.reporting.AtfReportPorts;
import atf.api.execution.ApiModule;
import atf.api.execution.lifecycle.ApiScenarioLifecycleRunner;
import atf.api.tests.api.headers.ApiHeadersAndEdgeCaseScenarios;
import atf.api.tests.api.lifecycle.LifecycleApiScenarios;
import atf.api.tests.api.posts.PostsApiScenarios;
import atf.api.tests.api.requests.RequestVariantsApiScenarios;
import atf.api.tests.api.users.UsersApiScenarios;

import java.util.Collection;
import java.util.List;

/** Ordered composition root for the API artifact consumed by UI tests. */
public final class ApiScenarioSetupCatalog {
    public static final List<String> DEFAULT_ORDER = List.of("posts", "users", "requests", "headers", "lifecycle");

    private ApiScenarioSetupCatalog() { }

    public static void publish(AtfReportPorts.Session session, ApiModule api,
                               ApiScenarioLifecycleRunner lifecycle,
                               Collection<String> requestedFamilies) {
        Collection<String> families = requestedFamilies == null || requestedFamilies.isEmpty()
                ? DEFAULT_ORDER : requestedFamilies;
        for (String family : families) publishOne(session, api, lifecycle, family);
    }

    private static void publishOne(AtfReportPorts.Session session, ApiModule api,
                                   ApiScenarioLifecycleRunner lifecycle, String family) {
        String key = family == null ? "" : family.trim().toLowerCase();
        switch (key) {
            case "posts" -> PostsApiScenarios.publish(session, api, lifecycle);
            case "users" -> UsersApiScenarios.publish(session, api, lifecycle);
            case "requests" -> RequestVariantsApiScenarios.publish(session, api, lifecycle);
            case "headers" -> ApiHeadersAndEdgeCaseScenarios.publish(session, api, lifecycle);
            case "lifecycle" -> LifecycleApiScenarios.publish(session, api, lifecycle);
            default -> throw new IllegalArgumentException("Unknown API scenario family: " + family
                    + ". Supported families: " + DEFAULT_ORDER);
        }
    }
}
