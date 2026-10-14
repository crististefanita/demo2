package atf.api.validation.contract;

import atf.api.core.http.ApiResponse;
import atf.api.validation.evaluation.ValidationTable;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Contract fluent pentru exact un response; nu poate fi evaluat de două ori. */
public final class ResponseContract {
    private final List<NamedRule> rules = new ArrayList<>();
    private final Set<String> identities = new HashSet<>();
    private final SchemaResourceResolver schemaResolver;
    private boolean evaluated;

    public ResponseContract() {
        this(SchemaResourceResolver.fromWorkingDirectory());
    }

    public ResponseContract(SchemaResourceResolver schemaResolver) {
        this.schemaResolver = java.util.Objects.requireNonNull(schemaResolver, "schemaResolver");
    }

    public ResponseContract status(int expected) {
        return add("status:" + expected, "HTTP status", (response, table) ->
                table.add("HTTP status", Integer.toString(expected), Integer.toString(response.status()), response.status() == expected));
    }

    public ResponseContract statusOneOf(int... expected) {
        if (expected == null || expected.length == 0) throw new IllegalArgumentException("expected statuses must not be empty");
        var allowed = java.util.Arrays.stream(expected).boxed().toList();
        return add("status-one-of:" + allowed, "HTTP status", (response, table) ->
                table.add("HTTP status", allowed.toString(), Integer.toString(response.status()), allowed.contains(response.status())));
    }

    public ResponseContract contentTypeContains(String expected) {
        requireText(expected, "expected");
        return add("content-type:" + expected, "Content-Type", (response, table) -> {
            var actual = response.header("Content-Type");
            table.add("Content-Type", expected, actual == null ? "<missing>" : actual,
                    actual != null && actual.toLowerCase().contains(expected.toLowerCase()));
        });
    }

    public ResponseContract headerPresent(String name) {
        requireText(name, "name");
        return add("header:" + name.toLowerCase(), "Header " + name, (response, table) -> {
            var actual = response.header(name);
            table.add("Header " + name, "present", actual == null ? "<missing>" : actual, actual != null);
        });
    }

    public ResponseContract bodyContains(String expected) {
        requireText(expected, "expected");
        return add("body:" + expected, "Response body", (response, table) ->
                table.add("Response body", expected, response.body(), response.body().contains(expected)));
    }

    public ResponseContract jsonPathExists(String path) {
        requireText(path, "path");
        return add("json-exists:" + path, "JSON path exists: " + path, (response, table) -> {
            var actual = JsonPathSupport.read(response.body(), path);
            table.add("JSON path exists: " + path, "present", actual == null ? "<missing>" : actual.toString(), actual != null);
        });
    }

    public ResponseContract jsonPathEquals(String path, String expected) {
        requireText(path, "path");
        if (expected == null) throw new IllegalArgumentException("expected must not be null");
        return add("json-equals:" + path + ":" + expected, "JSON path equals: " + path, (response, table) -> {
            var actual = JsonPathSupport.read(response.body(), path);
            var actualText = actual == null ? "<missing>" : actual.asText();
            table.add("JSON path equals: " + path, expected, actualText, expected.equals(actualText));
        });
    }

    public ResponseContract everyArrayItemEquals(String path, String property, String expected) {
        requireText(path, "path");
        requireText(property, "property");
        if (expected == null) throw new IllegalArgumentException("expected must not be null");
        return add("every-array:" + path + ":" + property + ":" + expected,
                "Every " + path + " item " + property, (response, table) -> {
                    var array = JsonPathSupport.read(response.body(), path);
                    boolean passed = array != null && array.isArray() && java.util.stream.StreamSupport.stream(array.spliterator(), false)
                            .allMatch(item -> item.has(property) && expected.equals(item.get(property).asText()));
                    table.add("Every " + path + " item " + property, expected,
                            array == null ? "<missing>" : array.toString(), passed);
                });
    }

    /** Validează semantic body-ul JSON față de schema contractului. */
    public ResponseContract jsonSchema(String schemaPath) {
        requireText(schemaPath, "schemaPath");
        return add("schema:" + schemaPath, "JSON schema", (response, table) -> {
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

    /** @deprecated use {@link #jsonSchema(String)}; kept for source compatibility. */
    @Deprecated(forRemoval = false)
    public ResponseContract jsonSchemaFileExists(String schemaPath) {
        return jsonSchema(schemaPath);
    }

    public void evaluate(ApiResponse response, ValidationTable table) {
        if (response == null || table == null) throw new NullPointerException("response/table");
        if (evaluated) throw new IllegalStateException("ResponseContract can be evaluated only once");
        if (rules.isEmpty()) throw new IllegalStateException("ResponseContract must contain at least one rule");
        evaluated = true;
        for (var rule : rules) {
            try {
                rule.rule().evaluate(response, table);
            } catch (RuntimeException error) {
                table.addFailure(rule.title(), "rule completed", error.getMessage(), error);
            }
        }
    }

    public List<String> ruleNames() {
        return rules.stream().map(NamedRule::title).toList();
    }

    private ResponseContract add(String identity, String title, ResponseValidationRule rule) {
        if (evaluated) throw new IllegalStateException("ResponseContract cannot be changed after evaluation");
        if (!identities.add(identity)) throw new IllegalArgumentException("duplicate response validation rule: " + identity);
        rules.add(new NamedRule(title, rule));
        return this;
    }

    private static void requireText(String value, String field) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(field + " must not be blank");
    }

    private record NamedRule(String title, ResponseValidationRule rule) { }
}
