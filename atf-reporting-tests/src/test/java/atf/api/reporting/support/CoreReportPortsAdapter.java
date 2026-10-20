package atf.api.reporting.support;

import atf.api.reporting.api.ReportModelAdapters;
import atf.api.reporting.contract.AtfReportStatus;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;

/** Test-scope bridge: keeps ATF lifecycle tests on the legacy core port while the renderer is standalone. */
public final class CoreReportPortsAdapter implements atf.api.core.reporting.contract.AtfReportPorts.Session {
    private final atf.api.reporting.contract.AtfReportPorts.Session delegate;
    private CoreReportPortsAdapter(atf.api.reporting.contract.AtfReportPorts.Session delegate){this.delegate=delegate;}
    public static CoreReportPortsAdapter wrap(atf.api.reporting.contract.AtfReportPorts.Session delegate){return new CoreReportPortsAdapter(delegate);}
    public Path reportPath(){return delegate.reportPath();}
    public atf.api.core.reporting.contract.AtfReportPorts.Scenario startScenario(String testClass,String scenario,Collection<String> tags){return new Scenario(delegate.startScenario(testClass,scenario,tags));}
    public atf.api.core.reporting.contract.AtfReportPorts.Scenario startScenario(String testClass,String testPackage,String scenario,Collection<String> tags){return new Scenario(delegate.startScenario(testClass,testPackage,scenario,tags));}
    public void flush(){delegate.flush();} public void close(){delegate.close();}
    private static atf.api.reporting.contract.AtfReportStatus status(atf.api.core.reporting.contract.AtfReportStatus s){return atf.api.reporting.contract.AtfReportStatus.valueOf(s.name());}
    private static atf.api.reporting.contract.AtfAssertion assertion(atf.api.core.reporting.contract.AtfAssertion a){return new atf.api.reporting.contract.AtfAssertion(a.name(),a.expected(),a.actual(),a.passed(),status(a.resultStatus()));}
    private static atf.api.reporting.ui.AtfUiActionRecord action(atf.api.core.ui.AtfUiActionRecord a){return new atf.api.reporting.ui.AtfUiActionRecord(a.sequence(),a.operation(),a.target(),a.expected(),a.observed(),a.durationMillis(),status(a.status()),atf.api.reporting.ui.AtfUiEvidenceSource.valueOf(a.source().name()));}
    private static final class Scenario implements atf.api.core.reporting.contract.AtfReportPorts.Scenario {
        private final atf.api.reporting.contract.AtfReportPorts.Scenario delegate; Scenario(atf.api.reporting.contract.AtfReportPorts.Scenario d){delegate=d;}
        public String id(){return delegate.id();} public atf.api.core.reporting.contract.AtfReportPorts.Phase startPhase(String name){return new Phase(delegate.startPhase(name));}
        public void complete(atf.api.core.reporting.contract.AtfReportStatus s){delegate.complete(status(s));} public boolean hasWarnings(){return delegate.hasWarnings();} public void close(){delegate.close();}
    }
    private static final class Phase implements atf.api.core.reporting.contract.AtfReportPorts.Phase {
        private final atf.api.reporting.contract.AtfReportPorts.Phase delegate; Phase(atf.api.reporting.contract.AtfReportPorts.Phase d){delegate=d;}
        public atf.api.core.reporting.contract.AtfReportPorts.Request startRequest(String title,String method,String path){return new Request(delegate.startRequest(title,method,path));}
        public void detail(String name,String value){delegate.detail(name,value);} public void complete(atf.api.core.reporting.contract.AtfReportStatus s){delegate.complete(status(s));} public void close(){delegate.close();}
    }
    private static final class Request implements atf.api.core.reporting.contract.AtfReportPorts.Request {
        private final atf.api.reporting.contract.AtfReportPorts.Request delegate; Request(atf.api.reporting.contract.AtfReportPorts.Request d){delegate=d;}
        public void detail(String n,String v){delegate.detail(n,v);} public void json(String n,String v){delegate.json(n,v);} public void screenshot(String n,String v){delegate.screenshot(n,v);}
        public void uiActions(Collection<atf.api.core.ui.AtfUiActionRecord> a){delegate.uiActions(a==null?List.of():a.stream().map(CoreReportPortsAdapter::action).toList());}
        public void execution(atf.api.core.http.response.ApiExecutionResult e){delegate.execution(ReportModelAdapters.execution(e));}
        public atf.api.core.reporting.contract.AtfReportPorts.Validation startValidation(String title){return new Validation(delegate.startValidation(title));}
        public void complete(atf.api.core.reporting.contract.AtfReportStatus s){delegate.complete(status(s));} public void close(){delegate.close();}
    }
    private static final class Validation implements atf.api.core.reporting.contract.AtfReportPorts.Validation {
        private final atf.api.reporting.contract.AtfReportPorts.Validation delegate; Validation(atf.api.reporting.contract.AtfReportPorts.Validation d){delegate=d;}
        public void assertions(Collection<atf.api.core.reporting.contract.AtfAssertion> a){delegate.assertions(a==null?List.of():a.stream().map(CoreReportPortsAdapter::assertion).toList());}
        public void uiActions(Collection<atf.api.core.ui.AtfUiActionRecord> a){delegate.uiActions(a==null?List.of():a.stream().map(CoreReportPortsAdapter::action).toList());}
        public void screenshot(String n,String v){delegate.screenshot(n,v);} public void mediaScreenshot(String n,String v){delegate.mediaScreenshot(n,v);}
        public void complete(atf.api.core.reporting.contract.AtfReportStatus s){delegate.complete(status(s));} public void close(){delegate.close();}
    }
}
