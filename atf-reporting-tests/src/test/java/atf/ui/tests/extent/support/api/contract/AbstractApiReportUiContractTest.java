package atf.ui.tests.extent.support.api.contract;
import atf.ui.tests.extent.support.api.ApiReportUiContractFlow;
import atf.ui.tests.extent.support.api.ApiReportUiFixture;
import atf.ui.tests.extent.support.api.ApiReportUiTestProfile;

import atf.ui.tests.extent.support.config.ReportingTestResources;
import atf.ui.tests.extent.support.config.UiConfiguration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * OOP test harness for declarative API-report UI contract tests.
 *
 * <p>Concrete tests provide only an annotation and a profile. Setup, report
 * navigation, request expansion, mock validation and audit evidence stay in
 * one reusable lifecycle so every API family is checked consistently.</p>
 */
public abstract class AbstractApiReportUiContractTest {
    private UiConfiguration configuration;
    private ApiReportUiFixture fixture;

    protected abstract ApiReportUiTestProfile profile();

    @BeforeAll
    final void setupApiReportAndUiAudit() {
        ApiReportUiTestProfile profile = profile();
        configuration = ReportingTestResources.uiConfiguration();
        fixture = ApiReportUiFixture.open(getClass(), configuration, profile.reportKey(),
                profile.tags(), profile.setupDescription(), profile.failureMessage(),
                profile.reportLabel());
    }

    @AfterAll
    final void closeUiAudit() {
        if (fixture != null) fixture.close();
    }

    @Test
    final void configuredApiOwnersAndRequestsMatchMockJsonContracts() {
        ApiReportUiTestProfile profile = profile();
        List<String> selectedOwners = fixture.setup().manifest().expectedOwnerNames();
        assertFalse(selectedOwners.isEmpty(),
                "The E2E smoke report must be based on at least one explicit API method");
        new ApiReportUiContractFlow().execute(fixture, selectedOwners,
                profile.auditTitle(), profile::screenTitle);
    }
}
