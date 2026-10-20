package atf.ui.practiceshop.config;

import java.util.Objects;

/** Typed registration input used by the reusable registration page object. */
public record PracticeShopRegistrationData(String firstName, String lastName, String dateOfBirth,
                                           String country, String postalCode, String houseNumber,
                                           String street, String city, String state, String phone,
                                           String email, String password) {
    public PracticeShopRegistrationData {
        firstName = required(firstName, "firstName");
        lastName = required(lastName, "lastName");
        dateOfBirth = required(dateOfBirth, "dateOfBirth");
        country = required(country, "country");
        postalCode = required(postalCode, "postalCode");
        houseNumber = required(houseNumber, "houseNumber");
        street = required(street, "street");
        city = required(city, "city");
        state = required(state, "state");
        phone = required(phone, "phone");
        email = required(email, "email");
        password = required(password, "password");
    }

    private static String required(String value, String field) {
        String normalized = Objects.requireNonNull(value, field).trim();
        if (normalized.isEmpty()) throw new IllegalArgumentException(field + " must not be blank");
        return normalized;
    }
}
