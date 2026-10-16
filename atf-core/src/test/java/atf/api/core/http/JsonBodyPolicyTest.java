package atf.api.core.http;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JsonBodyPolicyTest {
    @Test
    void appliesOnlyToPostAndPutAndRejectsNullOrMalformedWriteDocuments() {
        assertTrue(JsonBodyPolicy.appliesTo("POST"));
        assertTrue(JsonBodyPolicy.appliesTo("put"));
        assertFalse(JsonBodyPolicy.appliesTo("GET"));
        assertTrue(JsonBodyPolicy.validWriteDocument("{\"ok\":true}"));
        assertFalse(JsonBodyPolicy.validWriteDocument("null"));
        assertFalse(JsonBodyPolicy.validWriteDocument("{malformed"));
    }

    @Test
    void validatesBothSidesOfTheWriteExchangeWithOnePolicy() {
        assertEquals(
                java.util.List.of("response JSON body is not valid JSON"),
                JsonBodyPolicy.validateWriteExchange("{\"request\":true}", "{malformed"));
        assertTrue(JsonBodyPolicy.validateWriteExchange("{\"request\":true}", "{\"ok\":true}").isEmpty());
    }
}
