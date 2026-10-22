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

    @Test
    void registrationEvidenceRedactsIdentityAddressAndContactFieldsRecursively() {
        String json = "{\"data\":[{\"first_name\":\"Ada\",\"last_name\":\"Lovelace\","
                + "\"dob\":\"1990-01-01\",\"email\":\"ada@owned.test\","
                + "\"address\":{\"street\":\"Secret Street\",\"city\":\"Hidden City\","
                + "\"postal_code\":\"12345\",\"phone\":\"0700000000\"}}]}";

        String redacted = PracticeShopJsonEvidence.redactRegistration(json);

        assertTrue(redacted.contains("<redacted>"));
        for (String personalValue : java.util.List.of("Ada", "Lovelace", "1990-01-01",
                "ada@owned.test", "Secret Street", "Hidden City", "12345", "0700000000")) {
            assertFalse(redacted.contains(personalValue), "leaked value: " + personalValue);
        }
    }
}
