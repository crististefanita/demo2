package atf.ui.tests.extent.support;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReportUiContractTest {
    @Test
    void keepsClosedReportVocabularyCentralized() {
        assertEquals("atf-input-source-", ReportUiContract.INPUT_SOURCE_TEMP_PREFIX);
        assertEquals("#test-view", ReportUiContract.Tab.TESTS.hash());
        assertEquals("TEST BODY", ReportUiContract.Phase.TEST_BODY.displayName());
        assertEquals("request", ReportUiContract.NodeRole.REQUEST.attributeValue());
        assertEquals(UiTestData.USERS_OWNER, UiTestData.ApiFamily.USERS.ownerName());
        assertEquals("#atf-execution-metadata-grid", ReportUiContract.OVERVIEW_EXECUTION_METADATA_GRID);
        assertTrue(ReportUiContract.overviewSelectors(true).contains("#parent-analysis"));
        assertTrue(ReportUiContract.overviewSelectors(false).contains(".category-container"));
    }
}
