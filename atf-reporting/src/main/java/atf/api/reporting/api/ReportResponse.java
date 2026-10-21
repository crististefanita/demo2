package atf.api.reporting.api;

import java.time.Duration;
import java.util.Map;

public record ReportResponse(int status, String body, Map<String,String> headers, Duration duration, String url) {
    public ReportResponse { body=body==null?"":body; headers=headers==null?Map.of():Map.copyOf(headers); duration=duration==null?Duration.ZERO:duration; url=url==null?"":url; }

    public String statusLabel() { return status < 0 ? "Unavailable" : Integer.toString(status); }
}
