package atf.api.core.http;

/** Declară dacă o rută acceptă, recomandă sau cere autentificare. */
public enum AuthRequirement {
    NONE,
    OPTIONAL,
    REQUIRED
}
