package atf.api.reporting.contract;

import atf.api.reporting.inventory.ValidationSink;
import atf.api.reporting.model.ReportExecutionEvidence;
import atf.api.reporting.api.ReportModelAdapters;
import atf.api.reporting.ui.AtfUiActionRecord;
import atf.api.reporting.ui.network.ReportNetworkInspector;
import java.nio.file.Path;
import java.util.Collection;

/** Public renderer contract. Consumers provide data; reporting owns rendering. */
public final class AtfReportPorts {
    private AtfReportPorts() { }
    public interface Session extends AutoCloseable {
        Path reportPath();
        Scenario startScenario(String testClass, String scenario, Collection<String> tags);
        default Scenario startScenario(String testClass, String testPackage, String scenario,
                                       Collection<String> tags) { return startScenario(testClass, scenario, tags); }
        void flush();
        @Override void close();
    }
    public interface Scenario extends AutoCloseable {
        String id(); Phase startPhase(String name); void complete(AtfReportStatus status);
        boolean hasWarnings(); @Override void close();
    }
    public interface Phase extends AutoCloseable {
        Request startRequest(String title, String method, String path);
        default void detail(String name, String value) { }
        void complete(AtfReportStatus status); @Override void close();
    }
    public interface Request extends ValidationSink, AutoCloseable {
        void detail(String name, String value); void json(String label, String json);
        default void screenshot(String title, String base64Png) { }
        default void uiActions(Collection<AtfUiActionRecord> actions) { }
        default void execution(ReportExecutionEvidence result) { }
        /** Compatibility seam for legacy core execution snapshots during migration. */
        default void execution(Object result) { execution(ReportModelAdapters.execution(result)); }
        /** Adds one typed, renderer-neutral journey aggregate disclosure. */
        default void networkAggregate(ReportNetworkInspector.JourneyAggregate aggregate) { }
        @Override Validation startValidation(String title);
        void complete(AtfReportStatus status); @Override void close();
    }
    public interface Validation extends ValidationSink.Scope, AutoCloseable {
        void assertions(Collection<AtfAssertion> assertions);
        default void completeFailure(AtfReportStatus status) { complete(status); }
        default void uiActions(Collection<AtfUiActionRecord> actions) { }
        default void screenshot(String title, String base64Png) { }
        default void mediaScreenshot(String title, String base64Png) { }
        void complete(AtfReportStatus status); @Override void close();
    }
}
