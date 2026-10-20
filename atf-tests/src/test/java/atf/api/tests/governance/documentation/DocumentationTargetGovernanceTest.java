package atf.api.tests.governance.documentation;

import atf.api.testkit.AtfComponentTest;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static atf.api.tests.governance.AtfGovernanceTestSupport.locateProjectRoot;

/** Runs every documentation validator against the document selected by the annotation. */
@AtfComponentTest
@DocumentationTarget("atf-test-practice-shop/README.md")
public class DocumentationTargetGovernanceTest {
    @Test
    void annotatedDocumentPassesAllDocumentationGovernanceModules() throws Exception {
        Path root = locateProjectRoot();
        String configuredPath = getClass().getAnnotation(DocumentationTarget.class).value();
        Path target = root.resolve(configuredPath).normalize();
        List<DocumentationGovernanceSupport.Violation> violations = new ArrayList<>();

        if (!target.startsWith(root) || !Files.isRegularFile(target)) {
            violations.add(DocumentationGovernanceSupport.violation(
                    "DOC-TARGET-PATH", root, target, 1,
                    "configured documentation target does not resolve to a file: " + configuredPath,
                    "DocumentationTarget values are project-relative and must point to a governed document",
                    "set @DocumentationTarget to an existing README.md or governed Markdown path"));
        } else if (!DocumentationGovernanceSupport.isGovernedMarkdown(root, target)) {
            violations.add(DocumentationGovernanceSupport.violation(
                    "DOC-TARGET-SCOPE", root, target, 1,
                    "configured target is outside the governed documentation scope",
                    "the target smoke test must exercise the same document population as the whole-ATF suite",
                    "target an owner README or a designated normative Markdown document"));
        } else {
            DocumentationDiagramGovernanceTest.validate(target, root, violations);
            DocumentationReadmeStructureGovernanceTest.validate(target, root, violations);
            DocumentationStyleGovernanceTest.validate(target, root, violations);
            DocumentationProseGovernanceTest.validate(target, root, violations);
            DynamicPathDocumentationArchitectureTest.validatePaths(target, root, violations);
            DynamicPathDocumentationArchitectureTest.validateGeneratedReportNames(
                    target, root, violations);
        }

        DocumentationGovernanceSupport.assertNoViolations(
                "targeted documentation governance [" + configuredPath + "]", violations);
    }
}
