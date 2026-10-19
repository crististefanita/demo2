package atf.api.reporting.api;

import atf.api.reporting.model.ReportExecutionEvidence;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.List;
import java.util.Map;

/** Optional compatibility seam for consumers migrating from the former core DTOs. */
public final class ReportModelAdapters {
    private ReportModelAdapters() { }
    public static ReportRequest request(Object source) {
        if (source == null) return null;
        if (source instanceof ReportRequest request) return request;
        return new ReportRequest(text(source,"method"), text(source,"path"), map(source,"query"), map(source,"headers"), text(source,"authProfile"), text(source,"bodyText"));
    }
    public static ReportTransportProvenance provenance(Object source) {
        if (source instanceof ReportTransportProvenance value) return value;
        String name = source == null ? "UNKNOWN" : source.toString();
        return name.contains("LIVE") ? ReportTransportProvenance.LIVE_HTTP : name.contains("MOCK") ? ReportTransportProvenance.ACCEPTANCE_MOCK : ReportTransportProvenance.UNKNOWN;
    }
    public static ReportExecutionEvidence execution(Object source) {
        if (source == null) return null;
        if (source instanceof ReportExecutionEvidence evidence) return evidence;
        Object response = invoke(source,"response");
        return new ReportExecutionEvidence(
                new ReportResponse(number(response,"status"), text(response,"body"), map(response,"headers"), duration(response,"duration"), text(response,"url")),
                number(source,"attempts"), duration(source,"totalDuration"),
                retryEvents(invoke(source, "retryEvents")), attemptHistory(invoke(source, "attemptHistory")),
                request(invoke(source,"request")), provenance(invoke(source,"provenance")));
    }
    private static List<ReportRetryEvent> retryEvents(Object value) {
        if (!(value instanceof Iterable<?> values)) return List.of();
        var result = new java.util.ArrayList<ReportRetryEvent>();
        for (Object event : values) result.add(new ReportRetryEvent(number(event, "attempt"),
                number(event, "statusCode"), text(event, "header"), duration(event, "delay"),
                text(event, "reason")));
        return List.copyOf(result);
    }
    private static List<ReportAttemptRecord> attemptHistory(Object value) {
        if (!(value instanceof Iterable<?> values)) return List.of();
        var result = new java.util.ArrayList<ReportAttemptRecord>();
        for (Object attempt : values) result.add(new ReportAttemptRecord(number(attempt, "number"),
                number(attempt, "statusCode"), duration(attempt, "duration"),
                text(attempt, "outcome"), text(attempt, "reason")));
        return List.copyOf(result);
    }
    private static Object invoke(Object source,String name){try{Method m=source.getClass().getMethod(name);return m.invoke(source);}catch(Exception e){return null;}}
    @SuppressWarnings("unchecked") private static Map<String,String> map(Object source,String name){Object value=invoke(source,name);return value instanceof Map<?,?> map ? (Map<String,String>)map : Map.of();}
    private static String text(Object source,String name){Object value=invoke(source,name);return value==null?"":String.valueOf(value);}
    private static int number(Object source,String name){try{return Integer.parseInt(text(source,name));}catch(Exception e){return 0;}}
    private static Duration duration(Object source,String name){Object value=invoke(source,name);return value instanceof Duration d?d:Duration.ZERO;}
}
