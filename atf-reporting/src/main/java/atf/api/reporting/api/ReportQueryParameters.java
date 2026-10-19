package atf.api.reporting.api;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public record ReportQueryParameters(List<ReportQueryParameter> values) {
    public ReportQueryParameters { values=List.copyOf(values==null?List.of():values); }
    public static ReportQueryParameters empty(){return new ReportQueryParameters(List.of());}
    public static ReportQueryParameters from(Object source) {
        if (source == null) return empty();
        if (source instanceof ReportQueryParameters value) return value;
        try {
            Object map = source.getClass().getMethod("asMap").invoke(source);
            if (map instanceof Map<?,?> values) {
                return new ReportQueryParameters(values.entrySet().stream()
                        .map(entry -> new ReportQueryParameter(String.valueOf(entry.getKey()), String.valueOf(entry.getValue())))
                        .toList());
            }
        } catch (ReflectiveOperationException ignored) { }
        return empty();
    }
    public static ReportQueryParameters fromUri(URI uri){
        if(uri==null||uri.getRawQuery()==null||uri.getRawQuery().isBlank()) return empty();
        return new ReportQueryParameters(Arrays.stream(uri.getRawQuery().split("&"))
            .map(item->item.split("=",2)).map(parts->new ReportQueryParameter(URLDecoder.decode(parts[0],StandardCharsets.UTF_8), parts.length>1?URLDecoder.decode(parts[1],StandardCharsets.UTF_8):"" )).toList());
    }
    public Map<String,String> asMap(){return values.stream().collect(Collectors.toMap(ReportQueryParameter::name,ReportQueryParameter::value,(a,b)->b,LinkedHashMap::new));}
    public boolean isEmpty() { return values.isEmpty(); }
    public boolean containsAll(ReportQueryParameters expected) { return asMap().entrySet().containsAll(expected.asMap().entrySet()); }
    public String display() { return asMap().entrySet().stream().map(e -> e.getKey()+"="+e.getValue()).collect(Collectors.joining("&")); }
}
