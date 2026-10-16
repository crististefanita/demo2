package atf.ui.tests.extent.audit;

import atf.api.mock.AtfMockConfiguration;
import atf.api.mock.MockRoute;
import atf.api.core.http.ApiQueryParameters;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import atf.ui.tests.extent.support.ValidationResult;

/**
 * Reconciles the response JSON documents rendered in an artifact with the
 * response bodies declared by the mock route catalog. It deliberately checks
 * parsed JSON trees instead of whitespace or pretty-printing, so a renderer
 * may format the same payload without weakening the contract.
 */
public final class ApiMockEvidenceAuditor {
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final Pattern TEXTAREA = Pattern.compile(
            "(?is)<textarea\\b[^>]*class\\s*=\\s*['\"]([^'\"]*\\bcode-block\\b[^'\"]*)['\"][^>]*>(.*?)</textarea>");
    private static final Pattern PRE = Pattern.compile(
            "(?is)<pre\\b[^>]*class\\s*=\\s*['\"]([^'\"]*\\bcode-block\\b[^'\"]*)['\"][^>]*>(.*?)</pre>");

    public Result inspect(Path report, AtfMockConfiguration configuration) {
        if (report == null || configuration == null) {
            throw new NullPointerException("report/configuration");
        }
        try {
            return inspectHtml(Files.readString(report), configuration);
        } catch (IOException error) {
            throw new IllegalStateException("Cannot read API report for mock evidence audit: " + report, error);
        }
    }

    Result inspectHtml(String html, AtfMockConfiguration configuration) {
        List<JsonNode> rendered = renderedJsonDocuments(html == null ? "" : html);
        List<String> violations = new ArrayList<>();
        int checked = 0;
        for (MockRoute route : configuration.routes()) {
            checked++;
            JsonNode expected;
            try {
                expected = parse(route.body());
            } catch (RuntimeException error) {
                violations.add(route.key() + " -> mock body is not valid JSON: " + error.getMessage());
                continue;
            }
            if (rendered.stream().noneMatch(expected::equals)) {
                violations.add(route.key() + " -> configured mock response JSON was not rendered");
            }
        }
        return new Result(checked, rendered.size(), violations);
    }

    /** Compares one rendered request node with the exact YAML route selected by its URL. */
    public List<String> compareResponse(String method, String url, String responseBody,
                                        AtfMockConfiguration configuration) {
        List<String> violations = new ArrayList<>();
        URI actual = URI.create(url);
        MockRoute route = configuration.routes().stream()
                .filter(candidate -> candidate.method().equalsIgnoreCase(method))
                .filter(candidate -> candidate.path().equals(actual.getPath()))
                .filter(candidate -> queryMatches(candidate, actual.getRawQuery()))
                .sorted(java.util.Comparator.comparingInt(
                        (MockRoute candidate) -> candidate.queryParameters().values().size()).reversed())
                .findFirst().orElse(null);
        if (route == null) {
            violations.add(method + " " + actual.getPath() + " -> no matching YAML mock route");
            return violations;
        }
        try {
            JsonNode expected = parse(route.body());
            JsonNode actualBody = parse(responseBody);
            if (!expected.equals(actualBody)) {
                violations.add(route.key() + " -> rendered response JSON does not match atf-mock.yaml");
            }
        } catch (RuntimeException error) {
            violations.add(route.key() + " -> rendered response JSON cannot be compared: " + error.getMessage());
        }
        return violations;
    }

    private static boolean queryMatches(MockRoute route, String rawQuery) {
        if (route.query().isEmpty()) return true;
        return ApiQueryParameters.fromRawQuery(rawQuery).containsAll(route.queryParameters());
    }

    private static List<JsonNode> renderedJsonDocuments(String html) {
        List<JsonNode> result = new ArrayList<>();
        collect(html, TEXTAREA, result);
        collect(html, PRE, result);
        return result;
    }

    private static void collect(String html, Pattern pattern, List<JsonNode> result) {
        Matcher matcher = pattern.matcher(html);
        while (matcher.find()) {
            String source = unescapeHtml(matcher.group(2)).trim();
            if (source.isBlank()) continue;
            try {
                result.add(parse(source));
            } catch (RuntimeException ignored) {
                // Non-JSON code blocks belong to another evidence channel.
            }
        }
    }

    private static JsonNode parse(String source) {
        try {
            return JSON.readTree(source == null || source.isBlank() ? "null" : source);
        } catch (IOException error) {
            throw new IllegalArgumentException(error.getMessage(), error);
        }
    }

    private static String unescapeHtml(String value) {
        return value.replace("&quot;", "\"")
                .replace("&#39;", "'")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&amp;", "&");
    }

    public record Result(int configuredRoutes, int renderedJsonDocuments, List<String> violations) {
        public Result {
            violations = violations == null ? List.of() : List.copyOf(violations);
        }

        public boolean compliant() {
            return configuredRoutes > 0 && renderedJsonDocuments > 0 && violations.isEmpty();
        }

        public ValidationResult validation() {
            return ValidationResult.fromViolations("Mock evidence", violations);
        }
    }
}
