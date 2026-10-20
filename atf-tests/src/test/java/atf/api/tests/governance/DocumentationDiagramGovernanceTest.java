package atf.api.tests.governance;

import atf.api.testkit.AtfComponentTest;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static atf.api.tests.governance.AtfGovernanceTestSupport.locateProjectRoot;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Checks the stable visual contract of Mermaid diagrams embedded in README files. */
@AtfComponentTest
class DocumentationDiagramGovernanceTest {
    @Test
    void nonRootReadmeDiagramsKeepTheDocumentedVisualContract() throws Exception {
        Path root = locateProjectRoot();
        List<String> violations = new ArrayList<>();
        try (var files = Files.walk(root)) {
            files.filter(Files::isRegularFile)
                    .filter(this::isReadme)
                    .forEach(path -> inspect(path, root, violations));
        }

        assertTrue(violations.isEmpty(), () ->
                "README diagram governance violations: " + violations);
    }

    private boolean isReadme(Path path) {
        for (Path part : path) {
            if (List.of(".git", "target", "artifacts", ".idea").contains(part.toString())) return false;
        }
        return path.getFileName().toString().equalsIgnoreCase("README.md");
    }

    private static void inspect(Path path, Path root, List<String> violations) {
        try {
            List<String> lines = Files.readAllLines(path);
            for (int index = 0; index < lines.size(); index++) {
                if (!lines.get(index).trim().equals("```mermaid")) continue;

                int closing = findClosingFence(lines, index + 1);
                if (closing < 0) {
                    violations.add(location(root, path, index + 1) + " has an unclosed Mermaid fence");
                    break;
                }

                inspectDiagram(lines, index, closing, root, path, violations);
                inspectImmediateLegend(lines, closing, root, path, violations);
                index = closing;
            }
        } catch (Exception error) {
            violations.add("cannot read " + root.relativize(path) + ": " + error.getMessage());
        }
    }

    private static int findClosingFence(List<String> lines, int start) {
        for (int index = start; index < lines.size(); index++) {
            if (lines.get(index).trim().equals("```")) return index;
        }
        return -1;
    }

    private static void inspectDiagram(List<String> lines, int opening, int closing,
                                       Path root, Path path, List<String> violations) {
        String firstStatement = firstNonBlank(lines, opening + 1, closing);
        if (firstStatement == null || !firstStatement.matches("(?:flowchart\\s+LR|sequenceDiagram|stateDiagram-v2|classDiagram).*")) {
            violations.add(location(root, path, opening + 1) + " has an unsupported or missing Mermaid orientation");
            return;
        }
        if (!firstStatement.startsWith("flowchart")) return;
        if (!firstStatement.matches("flowchart\\s+LR.*")) {
            violations.add(location(root, path, opening + 2) + " flowchart must use LR outside the approved root baseline");
        }

        int edges = 0;
        boolean hasLabeledEdge = false;
        for (int index = opening + 2; index < closing; index++) {
            String line = lines.get(index).trim();
            if (line.contains("-->") || line.matches(".*-\\.\\s+.+\\s+\\.->.*")) {
                edges++;
                hasLabeledEdge |= line.contains("-->|")
                        || line.matches(".*-\\.\\s+.+\\s+\\.->.*");
            }
        }
        if (edges >= 2 && !hasLabeledEdge && !path.equals(root.resolve("README.md"))) {
            violations.add(location(root, path, opening + 1)
                    + " flowchart has multiple unlabeled relationships; add a short edge verb");
        }
    }

    private static void inspectImmediateLegend(List<String> lines, int closing,
                                               Path root, Path path, List<String> violations) {
        int next = closing + 1;
        while (next < lines.size() && lines.get(next).isBlank()) next++;
        if (next >= lines.size() || !lines.get(next).trim().equals("<details>")) {
            violations.add(location(root, path, closing + 1)
                    + " diagram must be followed immediately by one collapsed legend");
            return;
        }
        int summaryEnd = Math.min(lines.size(), next + 4);
        boolean hasLegendSummary = false;
        for (int index = next; index < summaryEnd; index++) {
            if (lines.get(index).toLowerCase().contains("legend")) {
                hasLegendSummary = true;
                break;
            }
        }
        if (!hasLegendSummary) {
            violations.add(location(root, path, next + 1)
                    + " collapsed details must identify the diagram legend");
        }
    }

    private static String firstNonBlank(List<String> lines, int start, int end) {
        for (int index = start; index < end; index++) {
            if (!lines.get(index).isBlank()) return lines.get(index).trim();
        }
        return null;
    }

    private static String location(Path root, Path path, int line) {
        return root.relativize(path) + ":" + line;
    }
}
