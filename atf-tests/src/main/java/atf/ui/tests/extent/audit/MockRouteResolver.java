package atf.ui.tests.extent.audit;

import atf.api.core.http.ApiQueryParameters;
import atf.api.mock.AtfMockConfiguration;
import atf.api.mock.MockRoute;
import atf.ui.tests.extent.support.ValidationResult;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Resolves a rendered METHOD/path/query identity against the mock catalog. */
final class MockRouteResolver {
    private static final Pattern REQUEST_SIGNATURE = Pattern.compile(
            "^\\s*([A-Za-z]+)\\s+([^\\s]+)(?:\\s+—.*)?\\s*$");
    private final AtfMockConfiguration mock;

    MockRouteResolver(AtfMockConfiguration mock) {
        this.mock = Objects.requireNonNull(mock, "mock");
    }

    Resolution resolve(String requestTitle, String requestUrl, int actualStatus) {
        Signature signature = signature(requestTitle, requestUrl);
        if (signature == null) {
            String message = requestTitle == null || requestTitle.isBlank()
                    ? "API request title is blank; cannot resolve mock route"
                    : "API request title does not expose METHOD /path: " + requestTitle;
            return new Resolution(Optional.empty(), ValidationResult.ofViolations(List.of(message)));
        }
        ApiQueryParameters query = MockContractValueSupport.queryFromUrl(requestUrl);
        List<MockRoute> candidates = mock.routes().stream()
                .filter(route -> methodAndPathMatch(route, signature)).toList();
        List<MockRoute> queryMatches = candidates.stream()
                .filter(route -> query.containsAll(route.queryParameters())).toList();
        List<MockRoute> source = queryMatches.isEmpty() ? candidates : queryMatches;
        Optional<MockRoute> route = source.stream()
                .sorted((left, right) -> {
                    int status = Boolean.compare(right.status() == actualStatus, left.status() == actualStatus);
                    return status != 0 ? status
                            : Integer.compare(right.queryParameters().values().size(),
                            left.queryParameters().values().size());
                }).findFirst();
        if (route.isEmpty()) {
            return new Resolution(Optional.empty(), ValidationResult.ofViolations(List.of(
                    signature.method() + " " + signature.path()
                            + " has no response contract in the mock YAML catalog")));
        }
        MockRoute expected = route.get();
        if (actualStatus != expected.status()) {
            return new Resolution(route, ValidationResult.ofViolations(List.of(
                    expected.key() + " expected HTTP " + expected.status()
                            + " from mock but report rendered " + actualStatus)));
        }
        return new Resolution(route, ValidationResult.empty());
    }

    private static boolean methodAndPathMatch(MockRoute route, Signature signature) {
        return (route.method().equals("*") || route.method().equals(signature.method()))
                && (route.path().equals("/*") || route.path().equals(signature.path()));
    }

    private static Signature signature(String title, String requestUrl) {
        if (title == null || title.isBlank()) return null;
        Matcher matcher = REQUEST_SIGNATURE.matcher(title.replaceAll("\\s+", " ").trim());
        if (!matcher.matches()) return null;
        String path = matcher.group(2);
        if (requestUrl != null && !requestUrl.isBlank()) {
            try {
                String renderedPath = URI.create(requestUrl.trim()).getPath();
                if (renderedPath != null && !renderedPath.isBlank()) path = renderedPath;
            } catch (IllegalArgumentException ignored) { }
        }
        return new Signature(matcher.group(1), path);
    }

    record Resolution(Optional<MockRoute> route, ValidationResult validation) {
        Resolution {
            route = route == null ? Optional.empty() : route;
            validation = Objects.requireNonNull(validation, "validation");
        }
    }

    private record Signature(String method, String path) { }
}
