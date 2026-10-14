package atf.api.reporting;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Facade for the semantic contract checks of the published HTML report.
 *
 * <p>The facade deliberately owns only orchestration. Each contract group
 * owns one semantic surface and writes stable, human-readable check names to
 * the shared ordered result.</p>
 */
final class ReportArtifactContractChecks {
    private static final List<ReportArtifactContractGroup> GROUPS = List.of(
            ReportArtifactThemeContracts::evaluate,
            ReportArtifactNavigationContracts::evaluate,
            ReportArtifactEvidenceContracts::evaluate,
            ReportArtifactOverviewContracts::evaluate,
            ReportArtifactStructuralContracts::evaluate);

    private ReportArtifactContractChecks() { }

    static Map<String, Boolean> evaluate(String html) {
        var checks = new LinkedHashMap<String, Boolean>();
        checks.put("html exists", true);
        checks.put("doctype", html.toLowerCase().contains("<!doctype html>"));

        GROUPS.forEach(group -> group.evaluate(html, checks));
        return checks;
    }
}
