package atf.api.tests.governance.changes;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/** Reads structured active requests and the separate baseline registry; prose cannot authorize a test edit. */
final class FeedbackTestChangeRequests {
    private static final Pattern BLOCK = Pattern.compile("(?ms)^```yaml\\R(.*?)^```[ \\t]*$");
    private static final ObjectMapper YAML = new ObjectMapper(
            new YAMLFactory().enable(JsonParser.Feature.STRICT_DUPLICATE_DETECTION))
            .enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS);
    private static final Set<String> FIELDS = Set.of(
            "id", "status", "base", "expires", "reason", "proof", "paths", "allowDelete");

    private FeedbackTestChangeRequests() { }

    static List<TestChangeRequest> read(Path feedback) throws IOException {
        if (!Files.isDirectory(feedback)) throw new IOException("TEST-CHANGE-DOC: missing " + feedback);
        var requests = new ArrayList<TestChangeRequest>();
        var documents = new ArrayList<Path>();
        try (var files = Files.walk(feedback)) {
            documents.addAll(files.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".md")).toList());
        }
        Path baseline = feedback.resolveSibling("ATF_Test_Changes_Baseline.md");
        if (Files.isRegularFile(baseline)) documents.add(baseline);
        documents.sort(Path::compareTo);
        for (Path file : documents) {
            requests.addAll(parse(Files.readString(file), sourceName(feedback, file)));
        }
        var ids = new HashSet<String>();
        for (var request : requests) {
            if (!ids.add(request.id())) throw new IOException("TEST-CHANGE-ID: duplicate " + request.id());
        }
        return List.copyOf(requests);
    }

    private static String sourceName(Path feedback, Path file) {
        Path project = feedback.getParent();
        return project == null ? file.toString() : project.relativize(file).toString().replace('\\', '/');
    }

    static List<TestChangeRequest> parse(String markdown, String source) throws IOException {
        var result = new ArrayList<TestChangeRequest>();
        var matcher = BLOCK.matcher(markdown);
        while (matcher.find()) {
            String block = matcher.group(1);
            if (!block.contains("testChanges:")) continue;
            try {
                JsonNode root = YAML.readTree(block);
                JsonNode rows = root.path("testChanges");
                if (!root.isObject() || root.size() != 1 || !rows.isArray() || rows.isEmpty()) {
                    throw new IllegalArgumentException("testChanges must be the sole key with a nonempty list");
                }
                for (JsonNode row : rows) result.add(request(row, source));
            } catch (IOException | IllegalArgumentException | java.time.DateTimeException invalid) {
                throw new IOException("TEST-CHANGE-SCHEMA " + source + ": " + invalid.getMessage(), invalid);
            }
        }
        if (BLOCK.matcher(markdown).replaceAll("").contains("testChanges:")) {
            throw new IOException("TEST-CHANGE-SCHEMA " + source + ": testChanges requires a closed yaml fence");
        }
        return List.copyOf(result);
    }

    private static TestChangeRequest request(JsonNode row, String source) {
        if (!row.isObject()) throw new IllegalArgumentException("request must be an object");
        row.fieldNames().forEachRemaining(field -> {
            if (!FIELDS.contains(field)) throw new IllegalArgumentException("unknown request field " + field);
        });
        String id = text(row, "id");
        if (!id.matches("[A-Z][A-Z0-9-]{3,79}")) throw new IllegalArgumentException("invalid id " + id);
        String status = text(row, "status");
        if (!Set.of("ACTIVE", "VERIFIED", "CLOSED", "DRAFT").contains(status)) {
            throw new IllegalArgumentException("invalid status for " + id);
        }
        String base = text(row, "base");
        if (!base.matches("[a-f0-9]{40}|[a-f0-9]{64}")) {
            throw new IllegalArgumentException("base must be an immutable commit hash for " + id);
        }
        String reason = text(row, "reason");
        if (reason.length() < 20) throw new IllegalArgumentException("reason must explain the change for " + id);
        String proof = text(row, "proof");
        if (!proof.startsWith("mvn ") || !(proof.contains("test") || proof.contains("verify"))) {
            throw new IllegalArgumentException("proof must name a Maven verification command for " + id);
        }
        JsonNode paths = row.path("paths");
        if (!paths.isArray() || paths.isEmpty()) throw new IllegalArgumentException("paths required for " + id);
        var exactPaths = new HashSet<String>();
        for (JsonNode value : paths) {
            String path = value.isTextual() ? value.textValue() : "";
            if (!TestChangePolicy.isExactPath(path) || !exactPaths.add(path)) {
                throw new IllegalArgumentException("invalid or duplicate exact path for " + id + ": " + path);
            }
        }
        JsonNode deletion = row.path("allowDelete");
        if (!deletion.isMissingNode() && !deletion.isBoolean()) {
            throw new IllegalArgumentException("allowDelete must be boolean for " + id);
        }
        return new TestChangeRequest(id, status, base, LocalDate.parse(text(row, "expires")),
                reason, proof, exactPaths, deletion.asBoolean(false), source);
    }

    private static String text(JsonNode row, String field) {
        JsonNode value = row.path(field);
        if (!value.isTextual() || value.textValue().isBlank()) {
            throw new IllegalArgumentException("nonempty text field required: " + field);
        }
        return value.textValue().trim();
    }
}
