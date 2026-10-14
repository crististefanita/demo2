package atf.api.core.http;

/** Token minim pentru anularea cooperantă a execuției și a retry delay-ului. */
@FunctionalInterface
public interface CancellationToken {
    boolean isCancelled();

    default void throwIfCancelled() {
        if (isCancelled()) {
            throw new java.util.concurrent.CancellationException("API execution was cancelled");
        }
    }

    static CancellationToken none() {
        return () -> false;
    }
}
