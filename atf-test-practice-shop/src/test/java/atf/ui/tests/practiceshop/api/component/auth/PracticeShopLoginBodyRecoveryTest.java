package atf.ui.tests.practiceshop.api.component.auth;

import atf.api.testkit.AtfComponentTest;
import atf.ui.practiceshop.support.PracticeShopApiBodyEvidence;
import atf.ui.practiceshop.support.PracticeShopLoginBodyRecovery;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@AtfComponentTest
class PracticeShopLoginBodyRecoveryTest {
    @Test
    void recoveredBodyIsReturnedAsTheEffectiveEvidence() {
        var result = PracticeShopLoginBodyRecovery.attempt(
                () -> "{\"access_token\":\"<redacted>\"}");

        assertTrue(result.recovered());
        assertTrue(result.effectiveBody().contains("access_token"));
        assertFalse(PracticeShopApiBodyEvidence.isUnavailable(result.effectiveBody()));
    }

    @Test
    void emptyRecoveryIsExplicitAndNeverLooksLikeValidJson() {
        var result = PracticeShopLoginBodyRecovery.attempt(() -> " ");

        assertFalse(result.recovered());
        assertTrue(PracticeShopApiBodyEvidence.isUnavailable(result.effectiveBody()));
        assertTrue(result.effectiveBody().contains("empty response body"));
    }

    @Test
    void transportFailurePreservesTheCauseForTheReport() {
        var result = PracticeShopLoginBodyRecovery.attempt(
                () -> { throw new IOException("connection reset"); });

        assertFalse(result.recovered());
        assertTrue(result.effectiveBody().contains("IOException"));
        assertTrue(result.effectiveBody().contains("connection reset"));
    }

    @Test
    void nonSuccessHttpStatusRemainsVisibleInRecoveryEvidence() {
        var result = PracticeShopLoginBodyRecovery.attempt(
                () -> { throw new IOException("HTTP status 503 for /users/login"); });

        assertFalse(result.recovered());
        assertTrue(result.effectiveBody().contains("HTTP status 503"));
        assertTrue(result.effectiveBody().contains("/users/login"));
    }
}
