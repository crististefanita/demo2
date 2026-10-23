package atf.ui.autoex.support;

/** Pure decision for disposing only the disposable account owned by this journey. */
public enum AutoExAccountCleanupAction {
    CONTINUE_THEN_DELETE,
    DELETE,
    NO_OP,
    CONFIRMED_BUT_UNAVAILABLE
}
