package atf.api.core.reporting;

import atf.api.core.http.ApiExecutionResult;

import java.nio.file.Path;
import java.util.Collection;

public final class AtfReportPorts {
    private AtfReportPorts() { }

    public interface Session extends AutoCloseable {
        Path reportPath();
        Scenario startScenario(String testClass, String scenario, Collection<String> tags);
        void flush();
        @Override void close();
    }

    public interface Scenario extends AutoCloseable {
        String id();
        Phase startPhase(String name);
        void complete(AtfReportStatus status);
        boolean hasWarnings();
        @Override void close();
    }

    public interface Phase extends AutoCloseable {
        Request startRequest(String title, String method, String path);
        default void detail(String name, String value) { }
        void complete(AtfReportStatus status);
        @Override void close();
    }

    public interface Request extends ValidationSink, AutoCloseable {
        void detail(String name, String value);
        void json(String label, String json);

        /**
         * Publishes renderer-neutral transport evidence for the completed
         * exchange. Implementations may render attempts and retry history, but
         * must keep the API response/body redaction policy at the reporting
         * boundary. The default preserves compatibility for external ports.
         */
        default void execution(ApiExecutionResult result) { }

        @Override
        Validation startValidation(String title);
        void complete(AtfReportStatus status);
        @Override void close();
    }

    public interface Validation extends ValidationSink.Scope, AutoCloseable {
        void assertions(Collection<AtfAssertion> assertions);
        void complete(AtfReportStatus status);
        @Override void close();
    }
}
