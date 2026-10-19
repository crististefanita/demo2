package atf.api.reporting.runtime.core;
import atf.api.reporting.html.script.HtmlReportScriptBuilder;
import atf.api.reporting.html.script.HtmlReportScriptModule;

/**
 * Composes the semantic browser-runtime controllers injected into the vendor document.
 *
 * Publication remains responsible for the file; each controller owns one runtime
 * boundary so navigation, Tests, evidence and Overview can be reviewed independently.
 */
public final class ExtentReportRuntimeBehaviour {
    private static final String HEAD_CLOSE = "</head>";
    private static final String SCRIPT_OPEN_PREFIX = "<script id=\"";
    private static final String SCRIPT_OPEN_SUFFIX = "\" type=\"text/javascript\">";
    private static final String SCRIPT_CLOSE = "</script>";
    private static final String RUNTIME_ELEMENT_ID = "atf-report-behaviour";
    private static final String RUNTIME_BOOT_FLAG = "__atfRuntimeBoot";
    private static final String RUNTIME_READY_FLAG = "__atfRuntimeReady";
    private static final String RUNTIME_ERROR_FLAG = "__atfRuntimeError";
    private static final String RUNTIME_READY_ATTRIBUTE = "data-atf-runtime-ready";

    private ExtentReportRuntimeBehaviour() { }

    public static String inject(String html) {
        String javascript = runtimeScript();
        return html.replace(HEAD_CLOSE, SCRIPT_OPEN_PREFIX + RUNTIME_ELEMENT_ID
                + SCRIPT_OPEN_SUFFIX + javascript + SCRIPT_CLOSE + HEAD_CLOSE);
    }

    private static String runtimeScript() {
        var script = new HtmlReportScriptBuilder()
                .append(HtmlReportScriptModule.RUNTIME_BOOT,
                        "(function(){try{window." + RUNTIME_BOOT_FLAG + "='starting';");
        for (ExtentReportRuntimeModule module : ExtentReportRuntimeModule.values()) {
            module.appendTo(script);
        }
        script
                .append(HtmlReportScriptModule.RUNTIME_READY, "window." + RUNTIME_READY_FLAG + "=true;document.documentElement.setAttribute('"
                        + RUNTIME_READY_ATTRIBUTE + "','true');}catch(error){window." + RUNTIME_ERROR_FLAG
                        + "=String(error&&error.stack||error);}})();");
        return script.build();
    }
}
