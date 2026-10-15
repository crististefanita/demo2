package atf.api.core.reporting;

import java.util.Objects;

/**
 * Renderer-neutral console event emitted by an ATF reporting lifecycle.
 *
 * <p>The event is deliberately smaller than an HTML/report model. It carries
 * only the information needed by a console sink and never exposes a vendor
 * reporter or a mutable execution context.</p>
 */
public record AtfConsoleEvent(Type type, Scope scope, String name,
                              AtfReportStatus status, long durationMillis,
                              String detailName, String detailValue) {
    public enum Type { START, DETAIL, CODE, COMPLETE }

    public enum Scope { SCENARIO, PHASE, REQUEST, VALIDATION }

    public AtfConsoleEvent {
        type = Objects.requireNonNull(type, "type");
        scope = Objects.requireNonNull(scope, "scope");
        name = name == null ? "" : name.trim();
        if (durationMillis < 0) {
            throw new IllegalArgumentException("durationMillis must not be negative");
        }
        if (type == Type.COMPLETE) {
            status = Objects.requireNonNull(status, "status");
        }
        detailName = detailName == null ? "" : detailName.trim();
        detailValue = detailValue == null ? "" : detailValue;
    }

    public static AtfConsoleEvent started(Scope scope, String name) {
        return new AtfConsoleEvent(Type.START, scope, name, null, 0, "", "");
    }

    public static AtfConsoleEvent detail(Scope scope, String name, String value) {
        return new AtfConsoleEvent(Type.DETAIL, scope, "", null, 0, name, value);
    }

    public static AtfConsoleEvent code(Scope scope, String name, String value) {
        return new AtfConsoleEvent(Type.CODE, scope, "", null, 0, name, value);
    }

    public static AtfConsoleEvent completed(Scope scope, String name,
                                            AtfReportStatus status, long durationMillis) {
        return new AtfConsoleEvent(Type.COMPLETE, scope, name, status, durationMillis, "", "");
    }
}
