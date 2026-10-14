package atf.api.core.security;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RedactionStrategiesTest {

    @Test
    void specializedStrategiesOwnTheirBoundaries() {
        var context = new RedactionContext(RedactionSettings.defaults());

        assertAll(
                () -> assertFalse(new SecretPatternRedactor(context)
                        .redact("Authorization: Bearer secret").contains("secret")),
                () -> assertFalse(new PiiRedactor(new RedactionContext(
                        new RedactionSettings(true, "[hidden]", false, true, Set.of())))
                        .redact("person@example.test").contains("@")),
                () -> assertFalse(new LocalPathRedactor(context)
                        .redact("C:\\Users\\person\\secret.json").contains("person")));
    }

    @Test
    void structuredStrategyDoesNotMutateSourceAndFallsBackForMalformedJson() {
        var context = new RedactionContext(RedactionSettings.defaults());
        var strategy = new StructuredJsonRedactor(context, new SecretPatternRedactor(context));
        String source = "{\"token\":\"secret\",\"items\":[{\"api_key\":\"abc\"}]}";

        String safe = strategy.redact(source);
        String malformed = strategy.redact("token=secret");

        assertAll(
                () -> assertEquals("{\"token\":\"secret\",\"items\":[{\"api_key\":\"abc\"}]}", source),
                () -> assertFalse(safe.contains("secret")),
                () -> assertFalse(safe.contains("abc")),
                () -> assertEquals("token=***REDACTED***", malformed),
                () -> assertFalse(malformed.contains("secret")));
    }
}
