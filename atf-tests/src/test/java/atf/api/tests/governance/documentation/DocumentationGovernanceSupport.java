package atf.api.tests.governance.documentation;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

/** Shared discovery and diagnostic vocabulary for documentation governance tests. */
final class DocumentationGovernanceSupport {
    private static final List<String> IGNORED_DIRECTORY_NAMES = List.of(
            ".git", "target", "artifacts", ".idea", "migration-copy-rejected",
            "migration-quarantine");

    private DocumentationGovernanceSupport() {
    }

    record Violation(String code, Path file, int line, String finding,
                     String rule, String fix) {
        @Override
        public String toString() {
            return "\n[" + code + "] " + file + ":" + line
                    + "\n  found: " + finding
                    + "\n  rule: " + rule
                    + "\n  fix: " + fix;
        }
    }

    static Violation violation(String code, Path root, Path file, int line,
                               String finding, String rule, String fix) {
        Path relative = root.toAbsolutePath().normalize()
                .relativize(file.toAbsolutePath().normalize());
        return new Violation(code, relative, line, finding, rule, fix);
    }

    static void assertNoViolations(String area, List<Violation> violations) {
        assertTrue(violations.isEmpty(), () -> {
            String details = violations.stream().map(Violation::toString).reduce("", String::concat);
            return area + " failed with " + violations.size()
                    + " actionable violation(s)." + details;
        });
    }

    static boolean isReadme(Path path) {
        if (!Files.isRegularFile(path)
                || !path.getFileName().toString().equalsIgnoreCase("README.md")) {
            return false;
        }
        for (Path part : path) {
            if (IGNORED_DIRECTORY_NAMES.contains(part.toString())) return false;
        }
        return true;
    }

    static boolean isGovernedMarkdown(Path root, Path path) {
        return isReadme(path)
                || path.toAbsolutePath().normalize().equals(
                root.resolve("docs/ATF_Architecture.md").toAbsolutePath().normalize());
    }
}
