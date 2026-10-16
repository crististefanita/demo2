package atf.ui.tests.extent.flows;

import atf.api.ui.config.AtfUiConfigurationLoader;
import atf.api.ui.config.UiConfiguration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

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
        configuration = AtfUiConfigurationLoader.load();
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
        new ApiReportUiContractFlow().execute(fixture, profile.ownerNames(),
                profile.auditTitle(), profile::screenTitle);
    }
}
