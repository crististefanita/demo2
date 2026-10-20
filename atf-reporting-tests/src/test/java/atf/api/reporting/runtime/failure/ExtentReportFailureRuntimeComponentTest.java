package atf.api.reporting.runtime.failure;

import atf.api.reporting.runtime.core.ExtentReportFailureRuntime;
import atf.api.testkit.AtfUnitTest;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@AtfUnitTest
class ExtentReportFailureRuntimeComponentTest {
    private static final Pattern FUNCTION = Pattern.compile("function\\s+([A-Za-z0-9_]+)\\(");

    @Test
    void facadePreservesFailureFragmentOrder() {
        String expected = String.join("",
                ExtentReportFailureDetection.script(),
                ExtentReportFailureContext.script(),
                ExtentReportFailureProjection.script());

        assertEquals(expected, ExtentReportFailureRuntime.script());
        assertTrue(expected.indexOf("function statusDisplay(")
                < expected.indexOf("function failureContext("));
        assertTrue(expected.indexOf("function failureContextFor(")
                < expected.indexOf("function failureIndexMarkup("));
        assertTrue(expected.indexOf("function failureIndexMarkup(")
                < expected.indexOf("function renderFails("));
    }

    @Test
    void eachFailureFunctionHasOneOwnerAndCauseHelpersStaySeparate() {
        String script = ExtentReportFailureRuntime.script();
        Matcher matcher = FUNCTION.matcher(script);
        Set<String> names = new HashSet<>();

        while (matcher.find()) {
            assertTrue(names.add(matcher.group(1)),
                    () -> "Duplicate failure runtime function: " + matcher.group(1));
        }

        assertEquals(20, names.size());
        assertTrue(ExtentReportFailureDetection.script().contains("function failedUiEvidence("));
        assertTrue(ExtentReportFailureContext.script().contains("function failureContextFromState("));
        assertTrue(ExtentReportFailureProjection.script().contains("function selectFailure("));
        assertFalse(ExtentReportFailureDetection.script().contains("function renderFails("));
        assertFalse(ExtentReportFailureContext.script().contains("function failureIndexMarkup("));
        assertFalse(ExtentReportFailureProjection.script().contains("function failureContext("));
        assertTrue(ExtentReportFailureRuntime.causeChainScript().contains("function failureCauseChain("));
    }
}
