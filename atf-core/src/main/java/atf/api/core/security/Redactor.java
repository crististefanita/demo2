package atf.api.core.security;

@FunctionalInterface
public interface Redactor {
    String redact(String value);
}
