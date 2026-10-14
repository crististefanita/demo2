package atf.api.validation.contract;

import atf.api.core.http.ApiResponse;
import atf.api.validation.evaluation.ValidationTable;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.StreamSupport;

/**
 * Composable rule factory used by the fluent {@link ResponseContract} facade.
 * The factory owns rule mechanics and schema I/O; the facade owns lifecycle,
 * identity and public DSL compatibility.
 */
final class ResponseContractRuleSet {
    private final SchemaResourceResolver schemaResolver;

    ResponseContractRuleSet(SchemaResourceResolver schemaResolver) {
        this.schemaResolver = java.util.Objects.requireNonNull(schemaResolver, "schemaResolver");
    }

    ResponseContractRuleDefinition status(int expected) {
        return rule("status:" + expected, "HTTP status", (response, table) ->
                table.add("HTTP status", Integer.toString(expected), Integer.toString(response.status()), response.status() == expected));
    }

    ResponseContractRuleDefinition statusOneOf(int... expected) {
        if (expected == null || expected.length == 0) {
            throw new IllegalArgumentException("expected statuses must not be empty");
        }
        List<Integer> allowed = Arrays.stream(expected).boxed().toList();
        return rule("status-one-of:" + allowed, "HTTP status", (response, table) ->
                table.add("HTTP status", allowed.toString(), Integer.toString(response.status()), allowed.contains(response.status())));
    }

    ResponseContractRuleDefinition contentTypeContains(String expected) {
        return rule("content-type:" + expected, "Content-Type", (response, table) -> {
            var actual = response.header("Content-Type");
            table.add("Content-Type", expected, actual == null ? "<missing>" : actual,
                    actual != null && actual.toLowerCase().contains(expected.toLowerCase()));
        });
    }

    ResponseContractRuleDefinition headerPresent(String name) {
        return rule("header:" + name.toLowerCase(), "Header " + name, (response, table) -> {
            var actual = response.header(name);
            table.add("Header " + name, "present", actual == null ? "<missing>" : actual, actual != null);
        });
    }

    ResponseContractRuleDefinition bodyContains(String expected) {
        return rule("body:" + expected, "Response body", (response, table) ->
                table.add("Response body", expected, response.body(), response.body().contains(expected)));
    }

    ResponseContractRuleDefinition jsonPathExists(String path) {
        return rule("json-exists:" + path, "JSON path exists: " + path, (response, table) -> {
            var actual = JsonPathSupport.read(response.body(), path);
            table.add("JSON path exists: " + path, "present", actual == null ? "<missing>" : actual.toString(), actual != null);
        });
    }

    ResponseContractRuleDefinition jsonPathEquals(String path, String expected) {
        return rule("json-equals:" + path + ":" + expected, "JSON path equals: " + path, (response, table) -> {
            var actual = JsonPathSupport.read(response.body(), path);
            var actualText = actual == null ? "<missing>" : actual.asText();
            table.add("JSON path equals: " + path, expected, actualText, expected.equals(actualText));
        });
    }

    ResponseContractRuleDefinition everyArrayItemEquals(String path, String property, String expected) {
        return rule("every-array:" + path + ":" + property + ":" + expected,
                "Every " + path + " item " + property, (response, table) -> {
                    var array = JsonPathSupport.read(response.body(), path);
                    boolean passed = array != null && array.isArray()
                            && StreamSupport.stream(array.spliterator(), false)
                            .allMatch(item -> item.has(property) && expected.equals(item.get(property).asText()));
                    table.add("Every " + path + " item " + property, expected,
                            array == null ? "<missing>" : array.toString(), passed);
                });
    }

    ResponseContractRuleDefinition jsonSchema(String schemaPath) {
        return rule("schema:" + schemaPath, "JSON schema", (response, table) -> {
            Path path = schemaResolver.resolve(schemaPath);
            if (path == null || !Files.isRegularFile(path)) {
                table.add("JSON schema", schemaPath, "schema file missing", false);
                return;
            }
            try {
                var result = JsonSchemaValidator.validate(path, response.body());
                table.add("JSON schema", schemaPath,
                        result.valid() ? "schema matched" : String.join(" | ", result.violations()), result.valid());
            } catch (java.io.IOException error) {
                table.addFailure("JSON schema", schemaPath, error.getMessage(), error);
            }
        });
    }

    private static ResponseContractRuleDefinition rule(String identity, String title, ResponseValidationRule rule) {
        return new ResponseContractRuleDefinition(identity, title, rule);
    }
}

record ResponseContractRuleDefinition(String identity, String title, ResponseValidationRule rule) {
    ResponseContractRuleDefinition {
        java.util.Objects.requireNonNull(identity, "identity");
        java.util.Objects.requireNonNull(title, "title");
        java.util.Objects.requireNonNull(rule, "rule");
    }
}
