package atf.api.execution.data;

import com.fasterxml.jackson.databind.ObjectMapper;

/** Serializare unică pentru body-urile semanticelor API. */
public final class JsonPayloads {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private JsonPayloads() { }

    public static String write(Object value) {
        try {
            return MAPPER.writeValueAsString(value);
        } catch (Exception error) {
            throw new IllegalArgumentException("Cannot serialize API request body", error);
        }
    }
}
