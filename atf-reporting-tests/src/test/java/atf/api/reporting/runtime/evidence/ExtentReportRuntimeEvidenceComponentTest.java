package atf.api.reporting.runtime.evidence;
import atf.api.reporting.runtime.section.ExtentReportRuntimeEvidence;

import atf.api.testkit.AtfUnitTest;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@AtfUnitTest
class ExtentReportRuntimeEvidenceComponentTest {
    private static final Pattern FUNCTION = Pattern.compile("function\\s+([A-Za-z0-9_]+)\\(");

    @Test
    void facadePreservesTypedEvidenceFragmentOrder() {
        String expected = String.join("",
                ExtentReportRuntimeEvidenceRoles.script(),
                ExtentReportRuntimeEvidenceDisclosure.script(),
                ExtentReportRuntimeEvidenceFailures.script(),
                ExtentReportRuntimeEvidenceTableLayout.script(),
                ExtentReportRuntimeEvidenceLifecycle.script());

        assertEquals(expected, ExtentReportRuntimeEvidence.script());
        assertTrue(expected.contains("function assignNodeRoles("));
        assertTrue(expected.contains("function normalizeEvidenceDisclosures("));
        assertTrue(expected.contains("function appendUiFailureEvidence("));
        assertTrue(expected.contains("normalizeSemanticEvidenceDisclosures();"));
    }

    @Test
    void evidenceFunctionDeclarationsHaveOneOwner() {
        String script = ExtentReportRuntimeEvidence.script();
        Matcher matcher = FUNCTION.matcher(script);
        Set<String> names = new HashSet<>();

        while (matcher.find()) {
            String name = matcher.group(1);
            assertTrue(names.add(name), () -> "Duplicate evidence function: " + name);
        }

        assertEquals(17, names.size());
        assertNotEquals(-1, script.indexOf("var atfLegacyNormalizeEvidenceDisclosures="));
        assertFalse(ExtentReportRuntimeEvidenceRoles.script().contains("appendUiFailureEvidence"));
        assertFalse(ExtentReportRuntimeEvidenceDisclosure.script().contains("uiFailureSource"));
        assertFalse(ExtentReportRuntimeEvidenceFailures.script().contains("normalizeEvidenceDisclosures"));
        assertFalse(ExtentReportRuntimeEvidenceLifecycle.script().contains("function "));
    }
}

