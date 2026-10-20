package atf.ui.practiceshop.api;

/** HTTP status vocabulary used by the Practice Shop API contract tests. */
public enum PracticeShopApiHttpStatus {
    OK(200),
    CREATED(201),
    NO_CONTENT(204),
    UNAUTHORIZED(401),
    NOT_FOUND(404),
    INTERNAL_SERVER_ERROR(500);

    private final int code;

    PracticeShopApiHttpStatus(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }

    public String wireValue() {
        return Integer.toString(code);
    }

    public boolean matches(int actual) {
        return code == actual;
    }

    public static boolean isSuccess(int actual) {
        return actual >= 200 && actual < 300;
    }
}
