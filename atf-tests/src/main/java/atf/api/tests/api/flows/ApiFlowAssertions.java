package atf.api.tests.api.flows;

import atf.api.execution.routing.RoutePaths;

import atf.api.core.http.ApiExecutionResult;
import atf.api.core.reporting.AtfReportPorts;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Cross-request assertions for stateful API flows.
 *
 * <p>Response contracts validate each HTTP response independently. These
 * assertions validate the relationship between the mutation response and the
 * subsequent read model, which is the part a duplicated POST/GET contract
 * cannot prove.</p>
 */
final class ApiFlowAssertions {
    private static final ObjectMapper JSON = new ObjectMapper();

    private ApiFlowAssertions() { }

    static void postReadBack(ApiExecutionResult created, ApiExecutionResult readBack,
                             AtfReportPorts.Request request) {
        postReadBack(created, null, readBack, request);
    }

    static void postReadBack(ApiExecutionResult created, ApiExecutionResult collection,
                             ApiExecutionResult readBack, AtfReportPorts.Request request) {
        JsonNode mutation = body(created, "POST");
        if (collection != null) {
            JsonNode collectionModel = body(collection, "GET");
            String createdId = text(mutation, "id", "POST resource id");
            boolean present = collectionModel.isArray() && java.util.stream.StreamSupport
                    .stream(collectionModel.spliterator(), false)
                    .anyMatch(item -> createdId.equals(textOrNull(item, "id")));
            if (!present) {
                throw new AssertionError("GET collection does not contain POST id " + createdId);
            }
        }
        JsonNode readModel = body(readBack, "GET");
        String createdId = text(mutation, "id", "POST resource id");
        String selfLink = textAt(readModel, "links.self", "GET self link");
        String expectedSelf = RoutePaths.POSTS + "/" + createdId;
        requireEqual(expectedSelf, selfLink, "POST id is linked to the GET read model");
        requireEqual("published", textAt(readModel, "status", "GET publication status"),
                "GET exposes the persisted publication state");
        if (request != null) {
            request.detail("FLOW ASSERTIONS", "PASS — POST $.id=" + createdId
                    + " → GET collection contains id → GET $.links.self=" + selfLink);
            request.detail("Read-model state", "PASS — GET $.status=published");
        }
    }

    static String resourceId(ApiExecutionResult mutation, String method) {
        return text(body(mutation, method), "id", method + " resource id");
    }

    static void profileReadBack(ApiExecutionResult updated, ApiExecutionResult readBack,
                                AtfReportPorts.Request request) {
        JsonNode mutation = body(updated, "PUT");
        JsonNode readModel = body(readBack, "GET");
        String mutationId = text(mutation, "id", "PUT profile id");
        String readId = text(readModel, "id", "GET profile id");
        requireEqual(mutationId, readId, "PUT id is preserved by the GET read model");
        requireEqual(text(mutation, "version", "PUT profile version"),
                text(readModel, "version", "GET profile version"),
                "PUT version is preserved by the GET read model");
        requireEqual("profile-update", textAt(readModel, "audit.lastMutation", "GET mutation audit"),
                "GET exposes the mutation that produced the read model");
        requireEqual("locale", textAt(readModel, "audit.lastChangedFields[0]", "GET changed fields"),
                "GET exposes the first persisted changed field");
        if (request != null) {
            request.detail("FLOW ASSERTIONS", "PASS — PUT $.id=" + mutationId
                    + " == GET $.id=" + readId);
            request.detail("Mutation lineage", "PASS — GET $.audit.lastMutation=profile-update");
        }
    }

    private static String textOrNull(JsonNode root, String field) {
        JsonNode value = root == null ? null : root.get(field);
        return value == null || value.isNull() ? null : value.asText();
    }

    private static JsonNode body(ApiExecutionResult execution, String method) {
        try {
            return JSON.readTree(execution.response().body());
        } catch (Exception error) {
            throw new AssertionError(method + " response is not valid JSON", error);
        }
    }

    private static String text(JsonNode root, String field, String label) {
        return textAt(root, field, label);
    }

    private static String textAt(JsonNode root, String path, String label) {
        JsonNode current = root;
        for (String segment : path.split("\\.")) {
            if (segment.matches(".+\\[\\d+]")) {
                int bracket = segment.indexOf('[');
                current = current == null ? null : current.get(segment.substring(0, bracket));
                int index = Integer.parseInt(segment.substring(bracket + 1, segment.length() - 1));
                current = current == null ? null : current.get(index);
            } else {
                current = current == null ? null : current.get(segment);
            }
        }
        if (current == null || current.isMissingNode() || current.isNull()) {
            throw new AssertionError(label + " is missing at $." + path);
        }
        return current.asText();
    }

    private static void requireEqual(String expected, String actual, String label) {
        if (!expected.equals(actual)) {
            throw new AssertionError(label + ": expected " + expected + " but was " + actual);
        }
    }
}
