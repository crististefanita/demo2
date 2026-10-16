package atf.ui.tests.extent.plan;

import atf.api.tests.support.ApiScenarioSelection;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Immutable, renderer-neutral expectation for one selected API report.
 *
 * <p>The browser produces observations; this model is built before browser
 * navigation from the selected API references and is therefore never learned
 * from the DOM being checked.</p>
 */
public record ExpectedReportProjection(List<Owner> owners,
                                       List<String> scenarioTitles,
                                       int scenarioCount,
                                       int phaseCount,
                                       int requestCount,
                                       List<String> requiredEvidence,
                                       List<String> tabs) {
    public ExpectedReportProjection {
        owners = List.copyOf(Objects.requireNonNull(owners, "owners"));
        scenarioTitles = List.copyOf(Objects.requireNonNull(scenarioTitles, "scenarioTitles"));
        requiredEvidence = List.copyOf(Objects.requireNonNull(requiredEvidence, "requiredEvidence"));
        tabs = List.copyOf(Objects.requireNonNull(tabs, "tabs"));
        if (scenarioCount < 0 || phaseCount < 0 || requestCount < 0) {
            throw new IllegalArgumentException("projection counts must be non-negative");
        }
        if (scenarioCount != scenarioTitles.size() || scenarioCount != phaseCount
                || scenarioCount != requestCount) {
            throw new IllegalArgumentException("selected scenario projection counts must agree");
        }
    }

    public static ExpectedReportProjection from(ApiScenarioManifest manifest) {
        Objects.requireNonNull(manifest, "manifest");
        List<ApiScenarioSelection> selected = manifest.selectedTests();
        List<Owner> owners = selected.stream()
                .map(selection -> new Owner(selection.reportOwner(), selection.family()))
                .distinct().toList();
        List<String> titles = selected.stream().map(ApiScenarioSelection::scenarioTitle).toList();
        return new ExpectedReportProjection(owners, titles, selected.size(), selected.size(), selected.size(),
                List.of("Request details", "Response details", "Execution evidence"),
                List.of("Tests", "Overview", "Categories", "Fails"));
    }

    public boolean matches(ReportAuditSnapshot.Tests observed) {
        Objects.requireNonNull(observed, "observed");
        List<String> expectedOwners = owners.stream().map(Owner::name).toList();
        return observed.classCount() == expectedOwners.size()
                && sameMultiset(observed.classNames(), expectedOwners)
                && observed.scenarioCount() == scenarioCount
                && observed.scenarioTitles().size() == scenarioCount
                && sameMultiset(observed.scenarioTitles(), scenarioTitles)
                && observed.phaseCount() >= phaseCount
                && observed.requestCount() >= requestCount
                && observed.validationCount() >= requestCount;
    }

    private static boolean sameMultiset(List<String> actual, List<String> expected) {
        return actual.size() == expected.size()
                && frequencies(actual).equals(frequencies(expected));
    }

    private static Map<String, Long> frequencies(List<String> values) {
        return values.stream().collect(Collectors.groupingBy(Function.identity(),
                java.util.LinkedHashMap::new, Collectors.counting()));
    }

    public record Owner(String name, String family) {
        public Owner {
            name = required(name, "name");
            family = required(family, "family");
        }

        private static String required(String value, String field) {
            if (value == null || value.isBlank()) throw new IllegalArgumentException(field + " must not be blank");
            return value.trim();
        }
    }
}
