package atf.api.tests.api.flows;

import atf.api.core.http.HttpHeader;
import atf.api.core.http.ApiOperationLabel;
import atf.api.core.http.HttpMethod;
import atf.api.core.http.MediaTypes;
import atf.api.execution.routing.RoutePaths;
import atf.api.core.http.ApiRequest;
import atf.api.execution.model.PostRequest;
import atf.api.execution.routing.ApiRoutes;
import atf.api.tests.api.data.ApiFixtureData;
import atf.api.validation.contract.ResponseContract;

import java.util.Map;

/** Immutable flow inputs and canonical contracts shared by direct and report flows. */
final class ApiRequestFlowFixtures {
    static final int CREATED_POST_ID = ApiFixtureData.CREATED_POST_ID;
    static final int PROFILE_ID = ApiFixtureData.PROFILE_ID;
    static final PostRequest CREATED_POST = ApiFixtureData.createdPostRequest();
    static final String POST_CREATE_TITLE = ApiOperationLabel.chain(
            HttpMethod.POST, RoutePaths.POSTS, HttpMethod.GET, RoutePaths.POSTS + "/" + CREATED_POST_ID, "read model");
    static final String PROFILE_UPDATE_TITLE = ApiOperationLabel.chain(
            HttpMethod.PUT, RoutePaths.PROFILES + "/" + PROFILE_ID, HttpMethod.GET,
            RoutePaths.PROFILES + "/" + PROFILE_ID, "read model");

    private ApiRequestFlowFixtures() { }

    static ApiRequest profileUpdateRequest() {
        return ApiRoutes.profileUpdate().requestBody(profileUpdateJson(), Map.of("id", PROFILE_ID),
                Map.of("expand", "permissions"),
                Map.of(HttpHeader.CONTENT_TYPE.value(), MediaTypes.APPLICATION_JSON));
    }

    static ApiRequest profileReadRequest() {
        return ApiRoutes.profileItem().request(Map.of("id", PROFILE_ID), Map.of(),
                Map.of(HttpHeader.ACCEPT.value(), MediaTypes.APPLICATION_JSON));
    }

    /** Mutation response: validates what the POST operation committed. */
    static ResponseContract createdPostMutationContract() {
        return new ResponseContract().status(201)
                .contentTypeContains(MediaTypes.APPLICATION_JSON)
                .jsonPathEquals("$.id", String.valueOf(ApiFixtureData.CREATED_POST_ID))
                .jsonPathEquals("$.created", "true")
                .jsonPathEquals("$.status", "published")
                .jsonPathEquals("$.title", ApiFixtureData.CREATED_POST_TITLE);
    }

    /** Read model response: validates projections, links and audit metadata. */
    static ResponseContract createdPostReadModelContract(String postId) {
        return new ResponseContract().status(200)
                .contentTypeContains(MediaTypes.APPLICATION_JSON)
                .jsonPathEquals("$.status", "published")
                .jsonPathEquals("$.links.self", RoutePaths.POSTS + "/" + postId)
                .jsonPathEquals("$.links.author", RoutePaths.USERS + "/" + ApiFixtureData.USER_ID)
                .jsonPathEquals("$.audit.source", "atf-mock")
                .jsonPathEquals("$.audit.version", "1")
                .jsonPathExists("$.timestamps.createdAt");
    }

    static ResponseContract createdPostCollectionContract(String postId) {
        return new ResponseContract().status(200)
                .contentTypeContains(MediaTypes.APPLICATION_JSON)
                .jsonArraySize("$", 2)
                .jsonPathEquals("$[1].id", postId)
                .jsonPathEquals("$[1].status", "published");
    }

    /** Mutation response: validates the applied change set, not the read model. */
    static ResponseContract updatedProfileMutationContract() {
        return new ResponseContract().status(200)
                .contentTypeContains(MediaTypes.APPLICATION_JSON)
                .jsonPathEquals("$.updated", "true")
                .jsonPathEquals("$.version", "7")
                .jsonPathEquals("$.changes[0].field", "locale")
                .jsonPathEquals("$.changes[1].to", ApiFixtureData.TIMEZONE)
                .jsonPathEquals("$.permissions.overrides.export", "false");
    }

    /** Read model response: validates the persisted projection and its links. */
    static ResponseContract profileReadModelContract() {
        return new ResponseContract().status(200)
                .contentTypeContains(MediaTypes.APPLICATION_JSON)
                .jsonPathEquals("$.readModel", "profile")
                .jsonPathEquals("$.profile.timezone", ApiFixtureData.TIMEZONE)
                .jsonPathEquals("$.permissions.inherited[0].from", "quality")
                .jsonPathEquals("$.links.self", RoutePaths.PROFILES + "/" + ApiFixtureData.PROFILE_ID)
                .jsonPathEquals("$.audit.lastMutation", "profile-update")
                .jsonPathEquals("$.audit.lastChangedFields[0]", "locale");
    }

    static String profileUpdateJson() {
        return """
                {
                  "profile": {"displayName": "%s", "locale": "%s", "timezone": "%s"},
                  "permissions": {"requested": ["read", "write"], "remove": ["export"], "inherit": true},
                  "reason": "multi-request flow contract"
                }
                """.formatted(ApiFixtureData.USER_NAME, ApiFixtureData.LOCALE, ApiFixtureData.TIMEZONE);
    }
}
