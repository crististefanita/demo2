package atf.api.reporting.api;

import java.time.Duration;

public record ReportAttemptRecord(int number, int statusCode, Duration duration, String outcome, String reason) {
    public ReportAttemptRecord { if(number<1 || statusCode<0) throw new IllegalArgumentException("invalid attempt"); duration=duration==null?Duration.ZERO:duration; reason=reason==null?"":reason; }
}
