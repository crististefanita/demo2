package atf.ui.practiceshop.support;

import java.util.Objects;

/**
 * Typed boundary for the optional login-body recovery used when Playwright
 * releases the compressed response before its body can be read.
 */
public final class PracticeShopLoginBodyRecovery {
    private PracticeShopLoginBodyRecovery() { }

    @FunctionalInterface
    public interface BodySupplier {
        String get() throws Exception;
    }

    public record Result(boolean recovered, String body, String evidence) {
        public Result {
            body = body == null ? "" : body;
            evidence = Objects.requireNonNull(evidence, "evidence");
        }

        public static Result recovered(String body) {
            if (body == null || body.isBlank()) {
                return unavailable("recovery returned an empty response body");
            }
            return new Result(true, body, "login response body recovered");
        }

        public static Result unavailable(String detail) {
            return new Result(false, "", PracticeShopApiBodyEvidence.unavailable(
                    "login body recovery", detail));
        }

        public String effectiveBody() {
            return recovered ? body : evidence;
        }
    }

    public static Result attempt(BodySupplier supplier) {
        Objects.requireNonNull(supplier, "supplier");
        try {
            return Result.recovered(supplier.get());
        } catch (Exception error) {
            return new Result(false, "", PracticeShopApiBodyEvidence.unavailable(
                    "login body recovery", error));
        }
    }
}
