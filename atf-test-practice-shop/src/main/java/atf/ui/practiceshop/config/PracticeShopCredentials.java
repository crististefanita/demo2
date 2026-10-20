package atf.ui.practiceshop.config;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Properties;

/** External account material. Password is intentionally never exposed by toString. */
public record PracticeShopCredentials(String email, String password,
                                      String firstName, String lastName) {
    public PracticeShopCredentials {
        email = required(email, "email");
        password = required(password, "password");
        firstName = required(firstName, "firstName");
        lastName = required(lastName, "lastName");
    }

    public static PracticeShopCredentials load(Path file) {
        Objects.requireNonNull(file, "file");
        Properties properties = new Properties();
        try (Reader reader = Files.newBufferedReader(file)) {
            properties.load(reader);
        } catch (IOException error) {
            throw new IllegalStateException("Cannot read Practice Shop account file: " + file, error);
        }
        return new PracticeShopCredentials(properties.getProperty("email"),
                properties.getProperty("password"), properties.getProperty("firstName", "ATF"),
                properties.getProperty("lastName", "E2E"));
    }

    public String displayName() { return firstName + " " + lastName; }

    @Override
    public String toString() {
        return "PracticeShopCredentials[email=" + email + ", displayName=" + displayName() + "]";
    }

    private static String required(String value, String name) {
        String normalized = value == null ? "" : value.trim();
        if (normalized.isEmpty()) throw new IllegalArgumentException(name + " must not be blank");
        return normalized;
    }
}


