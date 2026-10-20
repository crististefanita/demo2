package atf.ui.tests.practiceshop.component;

import atf.api.testkit.AtfComponentTest;
import atf.ui.practiceshop.support.PracticeShopJsonEvidence;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@AtfComponentTest
class PracticeShopJsonEvidenceTest {
    @Test
    void redactsNestedArrayFieldsAndSensitiveHeaderVariantsBeforeEvidenceIsPublished() {
        String json = "{\"items\":[{\"password\":\"secret\",\"apiKey\":\"key-1\"}],"
                + "\"note\":\"escaped \\\"value\\\"\"}";

        String redacted = PracticeShopJsonEvidence.redact(json);
        Map<String, String> headers = PracticeShopJsonEvidence.redactHeaders(Map.of(
                "X-API-Key", "key-1", "apiKey", "key-2", "Authorization", "Bearer secret"));

        assertFalse(redacted.contains("secret"));
        assertFalse(redacted.contains("key-1"));
        assertTrue(redacted.contains("escaped"));
        assertTrue(headers.values().stream().allMatch("<redacted>"::equals));
    }
}
