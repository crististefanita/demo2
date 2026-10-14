package atf.api.core.reporting;

import java.util.Collection;

/**
 * Narrow reporting port exposed to validation code.
 *
 * <p>The validation module can publish assertion evidence without depending
 * on a request implementation, Extent, HTML, or any other renderer.</p>
 */
public interface ValidationSink {
    Scope startValidation(String title);

    interface Scope extends AutoCloseable {
        void assertions(Collection<AtfAssertion> assertions);
        void complete(AtfReportStatus status);

        @Override
        void close();
    }
}
