package atf.api.reporting.semantic;
import atf.api.testkit.AtfUnitTest;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static atf.api.reporting.contract.AtfGherkinKeyword.AND;
import static atf.api.reporting.contract.AtfGherkinKeyword.BUT;
import static atf.api.reporting.contract.AtfGherkinKeyword.GIVEN;
import static atf.api.reporting.contract.AtfGherkinKeyword.THEN;
import static atf.api.reporting.contract.AtfGherkinKeyword.WHEN;

/** Keeps the executable report vocabulary stable and centralized. */
@AtfUnitTest
class AtfGherkinKeywordTest {
    @Test
    void exposesStableDisplayTokens() {
        assertEquals("GIVEN", GIVEN.text());
        assertEquals("WHEN", WHEN.text());
        assertEquals("THEN", THEN.text());
        assertEquals("AND", AND.text());
        assertEquals("BUT", BUT.text());
    }
}
