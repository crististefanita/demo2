package atf.api.tests.api.users;

import atf.api.core.http.ApiOperationLabel;
import atf.api.core.http.HttpMethod;
import atf.api.execution.routing.RoutePaths;
import atf.api.core.http.MediaTypes;

import atf.api.core.reporting.AtfReportPorts;
import atf.api.execution.ApiModule;
import atf.api.execution.data.JsonPayloads;
import atf.api.execution.lifecycle.ApiScenarioLifecycleRunner;
import atf.api.execution.model.UserRequest;
import atf.api.tests.api.data.ApiFixtureData;
import atf.api.tests.support.ApiAcceptancePlanBuilder;
import atf.api.tests.support.ApiAcceptancePublisher;
import atf.api.validation.contract.ContractValidators;
import atf.api.validation.contract.ResponseContract;

import java.util.List;

/** User-resource scenarios that appear in the public acceptance artifact. */
public final class UsersApiScenarios {
    public static final String REPORT_OWNER = "UsersApiTests";
    public static final String GET_COLLECTION = "getCollection";
    public static final String GET_BY_ID = "getById";
    public static final String CREATE_UNAUTHENTICATED = "createUnauthenticated";
    public static final String GET_COLLECTION_WARNING = "getCollectionWarning";
    public static final String GET_COLLECTION_TITLE = ApiOperationLabel.format(HttpMethod.GET, RoutePaths.USERS,
            "collection contract");
    public static final String GET_BY_ID_TITLE = ApiOperationLabel.format(HttpMethod.GET, RoutePaths.USER_BY_ID,
            "item contract");
    public static final String CREATE_UNAUTHENTICATED_TITLE = ApiOperationLabel.format(HttpMethod.POST, RoutePaths.USERS,
            "missing authentication");
    public static final String GET_COLLECTION_WARNING_TITLE = ApiOperationLabel.format(HttpMethod.GET, RoutePaths.USERS,
            "soft contract warning");
    public static final List<String> REPORT_SCENARIO_TITLES = List.of(
            GET_COLLECTION_TITLE, GET_BY_ID_TITLE, CREATE_UNAUTHENTICATED_TITLE, GET_COLLECTION_WARNING_TITLE);

    private UsersApiScenarios() { }

    public static void publish(AtfReportPorts.Session session, ApiModule api,
                               ApiScenarioLifecycleRunner lifecycle) {
        publishCollection(session, api, lifecycle);
        publishItem(session, api, lifecycle);
        publishUnauthenticatedCreate(session, api, lifecycle);
        publishCollectionWarning(session, api, lifecycle);
    }

    public static void publishSelected(AtfReportPorts.Session session, ApiModule api,
                                       ApiScenarioLifecycleRunner lifecycle, String method) {
        switch (method) {
            case GET_COLLECTION -> publishCollection(session, api, lifecycle);
            case GET_BY_ID -> publishItem(session, api, lifecycle);
            case CREATE_UNAUTHENTICATED -> publishUnauthenticatedCreate(session, api, lifecycle);
            case GET_COLLECTION_WARNING -> publishCollectionWarning(session, api, lifecycle);
            default -> throw new IllegalArgumentException("Unknown users API scenario method: " + method);
        }
    }

    public static String scenarioTitle(String method) {
        return switch (method) {
            case GET_COLLECTION -> GET_COLLECTION_TITLE;
            case GET_BY_ID -> GET_BY_ID_TITLE;
            case CREATE_UNAUTHENTICATED -> CREATE_UNAUTHENTICATED_TITLE;
            case GET_COLLECTION_WARNING -> GET_COLLECTION_WARNING_TITLE;
            default -> throw new IllegalArgumentException("Unknown users API scenario method: " + method);
        };
    }

    private static void publishCollection(AtfReportPorts.Session session, ApiModule api,
                                          ApiScenarioLifecycleRunner lifecycle) {
        run(lifecycle, session, plan(
                GET_COLLECTION_TITLE,
                List.of("api", "users", "contract", "smoke")),
                phase -> ApiAcceptancePublisher.publish(phase, ApiOperationLabel.format(HttpMethod.GET, RoutePaths.USERS, null),
                        api.users().getCollection(), ContractValidators.userCollection(), false, null));
    }

    private static void publishItem(AtfReportPorts.Session session, ApiModule api,
                                    ApiScenarioLifecycleRunner lifecycle) {
        run(lifecycle, session, plan(
                GET_BY_ID_TITLE,
                List.of("api", "users", "contract")),
                phase -> ApiAcceptancePublisher.publish(phase, ApiOperationLabel.format(HttpMethod.GET, RoutePaths.USERS + "/1", null), api.users().get(1),
                        ContractValidators.userItem(), false, null));
    }

    private static void publishUnauthenticatedCreate(AtfReportPorts.Session session, ApiModule api,
                                                      ApiScenarioLifecycleRunner lifecycle) {
        run(lifecycle, session, plan(
                CREATE_UNAUTHENTICATED_TITLE,
                List.of("api", "users", "negative", "auth")),
                phase -> {
                    UserRequest payload = ApiFixtureData.unauthenticatedUserRequest();
                    ApiAcceptancePublisher.publish(phase, ApiOperationLabel.format(HttpMethod.POST, RoutePaths.USERS, null), api.users().create(payload),
                            new ResponseContract().status(401).contentTypeContains(MediaTypes.APPLICATION_JSON)
                                    .bodyContains("Authentication required"), false,
                            JsonPayloads.write(payload));
                });
    }

    private static void publishCollectionWarning(AtfReportPorts.Session session, ApiModule api,
                                                 ApiScenarioLifecycleRunner lifecycle) {
        run(lifecycle, session, plan(
                GET_COLLECTION_WARNING_TITLE,
                List.of("api", "users", "reporting", "soft-assert")),
                phase -> ApiAcceptancePublisher.publish(phase, ApiOperationLabel.format(HttpMethod.GET, RoutePaths.USERS, null),
                        api.users().getCollection(),
                        new ResponseContract().status(201).contentTypeContains(MediaTypes.APPLICATION_JSON), true, null));
    }

    private static void run(ApiScenarioLifecycleRunner lifecycle, AtfReportPorts.Session session,
                            ApiAcceptancePlanBuilder plan,
                            atf.api.execution.lifecycle.ApiScenarioPlan.PhaseAction body) {
        lifecycle.run(session, plan.body(body).build());
    }

    private static ApiAcceptancePlanBuilder plan(String scenario, List<String> tags) {
        return ApiAcceptancePlanBuilder.scenario("UsersApiTests", UsersApiScenarios.class.getPackageName(),
                scenario, tags);
    }
}
