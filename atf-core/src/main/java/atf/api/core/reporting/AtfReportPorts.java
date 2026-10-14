package atf.api.core.reporting;

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
