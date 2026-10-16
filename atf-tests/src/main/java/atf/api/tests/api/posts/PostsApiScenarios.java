package atf.api.tests.api.posts;

import atf.api.core.http.ApiOperationLabel;
import atf.api.core.http.HttpMethod;
import atf.api.execution.routing.RoutePaths;
import atf.api.core.http.MediaTypes;

import atf.api.core.reporting.AtfReportPorts;
import atf.api.execution.ApiModule;
import atf.api.execution.data.JsonPayloads;
import atf.api.execution.lifecycle.ApiScenarioLifecycleRunner;
import atf.api.execution.model.PostRequest;
import atf.api.tests.api.data.ApiFixtureData;
import atf.api.tests.support.ApiAcceptancePlanBuilder;
import atf.api.tests.support.ApiAcceptancePublisher;
import atf.api.validation.contract.ContractValidators;
import atf.api.validation.contract.ResponseContract;

import java.util.List;

/** Post-resource and relationship scenarios in the public acceptance artifact. */
public final class PostsApiScenarios {
    public static final String REPORT_OWNER = "PostsApiTests";
    public static final String GET_COLLECTION = "getCollection";
    public static final String GET_BY_ID = "getById";
    public static final String GET_USER_POSTS = "getUserPosts";
    public static final String GET_UNKNOWN = "getUnknown";
    public static final String CREATE_POST = "createPost";
    public static final String GET_COLLECTION_TITLE = ApiOperationLabel.format(HttpMethod.GET, RoutePaths.POSTS,
            "collection contract");
    public static final String GET_BY_ID_TITLE = ApiOperationLabel.format(HttpMethod.GET, RoutePaths.POST_BY_ID,
            "item contract");
    public static final String GET_USER_POSTS_TITLE = ApiOperationLabel.format(HttpMethod.GET, RoutePaths.USER_POSTS,
            "relationship contract");
    public static final String GET_UNKNOWN_TITLE = ApiOperationLabel.format(HttpMethod.GET,
            RoutePaths.POST_BY_ID.replace("{id}", "{unknownId}"), "not found contract");
    public static final String CREATE_POST_TITLE = ApiOperationLabel.format(HttpMethod.POST, RoutePaths.POSTS,
            "create contract");
    public static final String CREATE_ROUTE_IDENTITY = HttpMethod.POST.value() + " " + RoutePaths.POSTS;
    public static final List<String> REPORT_SCENARIO_TITLES = List.of(
            GET_COLLECTION_TITLE, GET_BY_ID_TITLE, GET_USER_POSTS_TITLE, GET_UNKNOWN_TITLE, CREATE_POST_TITLE);

    private PostsApiScenarios() { }

    public static void publish(AtfReportPorts.Session session, ApiModule api,
                               ApiScenarioLifecycleRunner lifecycle) {
        publishCollection(session, api, lifecycle);
        publishItem(session, api, lifecycle);
        publishUserPosts(session, api, lifecycle);
        publishUnknown(session, api, lifecycle);
        publishCreate(session, api, lifecycle);
    }

    public static void publishSelected(AtfReportPorts.Session session, ApiModule api,
                                       ApiScenarioLifecycleRunner lifecycle, String method) {
        switch (method) {
            case GET_COLLECTION -> publishCollection(session, api, lifecycle);
            case GET_BY_ID -> publishItem(session, api, lifecycle);
            case GET_USER_POSTS -> publishUserPosts(session, api, lifecycle);
            case GET_UNKNOWN -> publishUnknown(session, api, lifecycle);
            case CREATE_POST -> publishCreate(session, api, lifecycle);
            default -> throw new IllegalArgumentException("Unknown posts API scenario method: " + method);
        }
    }

    public static String scenarioTitle(String method) {
        return switch (method) {
            case GET_COLLECTION -> GET_COLLECTION_TITLE;
            case GET_BY_ID -> GET_BY_ID_TITLE;
            case GET_USER_POSTS -> GET_USER_POSTS_TITLE;
            case GET_UNKNOWN -> GET_UNKNOWN_TITLE;
            case CREATE_POST -> CREATE_POST_TITLE;
            default -> throw new IllegalArgumentException("Unknown posts API scenario method: " + method);
        };
    }

    private static void publishCollection(AtfReportPorts.Session session, ApiModule api,
                                          ApiScenarioLifecycleRunner lifecycle) {
        run(lifecycle, session, plan(
                GET_COLLECTION_TITLE,
                List.of("api", "posts", "contract")),
                phase -> ApiAcceptancePublisher.publish(phase, ApiOperationLabel.format(HttpMethod.GET, RoutePaths.POSTS, null),
                        api.posts().getCollection(), ContractValidators.postCollection(), false, null));
    }

    private static void publishItem(AtfReportPorts.Session session, ApiModule api,
                                    ApiScenarioLifecycleRunner lifecycle) {
        run(lifecycle, session, plan(
                GET_BY_ID_TITLE,
                List.of("api", "posts", "contract")),
                phase -> ApiAcceptancePublisher.publish(phase, ApiOperationLabel.format(HttpMethod.GET, RoutePaths.POSTS + "/" + ApiFixtureData.EXISTING_POST_ID, null), api.posts().get(ApiFixtureData.EXISTING_POST_ID),
                        ContractValidators.postItem(), false, null));
    }

    private static void publishUserPosts(AtfReportPorts.Session session, ApiModule api,
                                         ApiScenarioLifecycleRunner lifecycle) {
        run(lifecycle, session, plan(
                GET_USER_POSTS_TITLE,
                List.of("api", "posts", "relationship", "contract")),
                phase -> ApiAcceptancePublisher.publish(phase, ApiOperationLabel.format(HttpMethod.GET, RoutePaths.USERS + "/" + ApiFixtureData.USER_ID + "/posts", null),
                        api.posts().forUser(ApiFixtureData.USER_ID), ContractValidators.relationshipCollection(String.valueOf(ApiFixtureData.USER_ID)), false, null));
    }

    private static void publishUnknown(AtfReportPorts.Session session, ApiModule api,
                                       ApiScenarioLifecycleRunner lifecycle) {
        run(lifecycle, session, plan(
                GET_UNKNOWN_TITLE,
                List.of("api", "posts", "negative", "contract")),
                phase -> ApiAcceptancePublisher.publish(phase, ApiOperationLabel.format(HttpMethod.GET, RoutePaths.POSTS + "/" + ApiFixtureData.UNKNOWN_POST_ID, null),
                        api.posts().get(ApiFixtureData.UNKNOWN_POST_ID),
                        new ResponseContract().status(404).contentTypeContains(MediaTypes.APPLICATION_JSON)
                                .bodyContains("Not found"), false, null));
    }

    private static void publishCreate(AtfReportPorts.Session session, ApiModule api,
                                      ApiScenarioLifecycleRunner lifecycle) {
        run(lifecycle, session, plan(
                CREATE_POST_TITLE,
                List.of("api", "posts", "write", "contract")),
                phase -> {
                    var payload = ApiFixtureData.createdPostRequest();
                    ApiAcceptancePublisher.publish(phase, ApiOperationLabel.format(HttpMethod.POST, RoutePaths.POSTS, null),
                            api.posts().create(payload),
                            new ResponseContract().status(201)
                                    .contentTypeContains(MediaTypes.APPLICATION_JSON)
                                    .jsonPathEquals("$.id", String.valueOf(ApiFixtureData.CREATED_POST_ID))
                                    .jsonPathEquals("$.user_id", String.valueOf(ApiFixtureData.USER_ID))
                                    .jsonPathEquals("$.title", ApiFixtureData.CREATED_POST_TITLE)
                                    .jsonPathEquals("$.body", ApiFixtureData.POST_BODY),
                            false, JsonPayloads.write(payload));
                });
    }

    private static void run(ApiScenarioLifecycleRunner lifecycle, AtfReportPorts.Session session,
                            ApiAcceptancePlanBuilder plan,
                            atf.api.execution.lifecycle.ApiScenarioPlan.PhaseAction body) {
        lifecycle.run(session, plan.body(body).build());
    }

    private static ApiAcceptancePlanBuilder plan(String scenario, List<String> tags) {
        return ApiAcceptancePlanBuilder.scenario("PostsApiTests", PostsApiScenarios.class.getPackageName(),
                scenario, tags);
    }
}
