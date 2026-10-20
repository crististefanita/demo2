package atf.api.tests.governance;

import atf.api.testkit.AtfComponentTest;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static atf.api.tests.governance.AtfGovernanceTestSupport.locateProjectRoot;

/** Keeps project documentation relocatable and free of machine-specific paths. */
@AtfComponentTest
class DynamicPathDocumentationArchitectureTest {
    private static final Pattern ABSOLUTE_WINDOWS_PATH =
            Pattern.compile("(?i)(?<![a-z0-9])(?:file:///)?[a-z]:[\\\\/]");
    private static final Pattern CONCRETE_TIMESTAMPED_REPORT =
            Pattern.compile("(?i)(?:api|ui)-atf-extent-report[^\\s`)\\]]*20\\d{2}-\\d{2}-\\d{2}[^\\s`)\\]]*\\.html");
    private static final List<String> IGNORED_DIRECTORY_NAMES = List.of(
            ".git", "target", "artifacts", ".idea", "migration-copy-rejected",
            "migration-quarantine");

    @Test
    void markdownAndReadmesDoNotPublishMachineSpecificAbsolutePaths() throws Exception {
        Path root = locateProjectRoot();
        List<String> violations = new ArrayList<>();
        try (var files = Files.walk(root)) {
            files.filter(Files::isRegularFile)
                    .filter(this::isDocumentation)
                    .forEach(path -> inspect(path, root, violations));
        }
        assertTrue(violations.isEmpty(), () -> "Non-relocatable documentation: " + violations);
    }

    @Test
    void markdownAndReadmesDoNotPublishConcreteGeneratedReportNames() throws Exception {
        Path root = locateProjectRoot();
        List<String> violations = new ArrayList<>();
        try (var files = Files.walk(root)) {
            files.filter(Files::isRegularFile)
                    .filter(this::isDocumentation)
                    .forEach(path -> inspectGeneratedReportName(path, root, violations));
        }
        assertTrue(violations.isEmpty(), () ->
                "Documentation must not retain concrete generated reports: " + violations);
    }

    private boolean isDocumentation(Path path) {
        for (Path part : path) {
            if (IGNORED_DIRECTORY_NAMES.contains(part.toString())) return false;
        }
        String name = path.getFileName().toString();
        return name.equalsIgnoreCase("README.md")
                || (name.endsWith(".md") && path.toString().contains("docs"));
    }

    private static void inspect(Path path, Path root, List<String> violations) {
        try {
            int lineNumber = 0;
            for (String line : Files.readAllLines(path)) {
                lineNumber++;
                if (ABSOLUTE_WINDOWS_PATH.matcher(line).find()) {
                    violations.add(root.relativize(path) + ":" + lineNumber);
                }
            }
        } catch (Exception error) {
            violations.add("cannot read " + root.relativize(path) + ": " + error.getMessage());
        }
    }

    private static void inspectGeneratedReportName(Path path, Path root,
                                                   List<String> violations) {
        try {
            int lineNumber = 0;
            for (String line : Files.readAllLines(path)) {
                lineNumber++;
                if (CONCRETE_TIMESTAMPED_REPORT.matcher(line).find()) {
                    violations.add(root.relativize(path) + ":" + lineNumber);
                }
            }
        } catch (Exception error) {
            violations.add("cannot read " + root.relativize(path) + ": " + error.getMessage());
        }
    }

}
