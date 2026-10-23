package atf.ui.autoex.api;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/** Typed form fields for the documented create/update account API contract. */
public record AutoExApiAccount(String name, String email, String password, String title,
                               String birthDate, String birthMonth, String birthYear,
                               String firstName, String lastName, String company,
                               String address1, String address2, String country,
                               String zipcode, String state, String city, String mobileNumber) {
    public AutoExApiAccount {
        name = required(name, "name");
        email = required(email, "email");
        password = required(password, "password");
        title = required(title, "title");
        birthDate = required(birthDate, "birth_date");
        birthMonth = required(birthMonth, "birth_month");
        birthYear = required(birthYear, "birth_year");
        firstName = required(firstName, "firstname");
        lastName = required(lastName, "lastname");
        company = required(company, "company");
        address1 = required(address1, "address1");
        address2 = Objects.requireNonNullElse(address2, "");
        country = required(country, "country");
        zipcode = required(zipcode, "zipcode");
        state = required(state, "state");
        city = required(city, "city");
        mobileNumber = required(mobileNumber, "mobile_number");
    }

    public Map<String, String> formFields() {
        Map<String, String> fields = new LinkedHashMap<>();
        fields.put("name", name);
        fields.put("email", email);
        fields.put("password", password);
        fields.put("title", title);
        fields.put("birth_date", birthDate);
        fields.put("birth_month", birthMonth);
        fields.put("birth_year", birthYear);
        fields.put("firstname", firstName);
        fields.put("lastname", lastName);
        fields.put("company", company);
        fields.put("address1", address1);
        fields.put("address2", address2);
        fields.put("country", country);
        fields.put("zipcode", zipcode);
        fields.put("state", state);
        fields.put("city", city);
        fields.put("mobile_number", mobileNumber);
        return Map.copyOf(fields);
    }

    @Override
    public String toString() {
        return "AutoExApiAccount[email=" + email + ", name=" + name + ", credentials=<redacted>]";
    }

    private static String required(String value, String field) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(field + " is required");
        return value.trim();
    }
}
