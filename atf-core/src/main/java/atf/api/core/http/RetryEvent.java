package atf.api.core.http;

import java.time.Duration;

public record RetryEvent(int attempt, int statusCode, String header, Duration delay, String reason) { }
