package atf.api.tests.support.junit;

import atf.api.core.http.response.ApiExecutionResult;
import atf.api.core.http.response.ApiOperationLabel;
import atf.api.core.http.request.HttpMethod;
import atf.api.console.junit.AtfConsoleTestExtension;
import atf.api.testkit.AtfConsoleMessages;
import atf.api.validation.contract.ResponseContract;
import atf.api.validation.evaluation.ValidationDecision;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;

/** Shared direct-run composition for resource-oriented API tests. */
@ExtendWith({AtfConsoleTestExtension.class, AtfExtentReportTestExtension.class})
@AtfConsoleMessages(AtfConsoleMessages.Policy.DETAILS)
public abstract class ApiResourceTestBase {
    private ApiTestFixture fixture;

    @BeforeEach
    void composeResourceFixture() {
        fixture = new ApiTestFixture();
    }

    protected final ApiTestFixture fixture() {
        return fixture;
    }

    protected final String operation(HttpMethod method, String path, String description) {
        return ApiOperationLabel.format(method, path, description);
    }

    protected final ValidationDecision validate(String reportTitle,
                                                ApiExecutionResult execution,
                                                ResponseContract contract) {
        return fixture.validate(reportTitle, execution, contract, false);
    }

    protected final ValidationDecision validate(String reportTitle,
                                                ApiExecutionResult execution,
                                                ResponseContract contract,
                                                boolean warningMode) {
        return fixture.validate(reportTitle, execution, contract, warningMode);
    }

    protected final ValidationDecision validateNegative(String reportTitle,
                                                        ApiExecutionResult execution,
                                                        ResponseContract contract) {
        return fixture.validateNegative(reportTitle, execution, contract);
    }
}
