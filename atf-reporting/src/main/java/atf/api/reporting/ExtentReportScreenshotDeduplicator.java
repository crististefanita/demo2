package atf.api.reporting;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Removes repeated embedded screenshot payloads while keeping the first link actionable. */
final class ExtentReportScreenshotDeduplicator {
    private static final Pattern SCREENSHOT_LINK = Pattern.compile(
            "<a\\b(?=[^>]*data-atf-screenshot=\\\"base64\\\")"
                    + "(?=[^>]*href=\\\"data:image/png;base64,([^\\\"]+)\\\")[^>]*>.*?</a>",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    private ExtentReportScreenshotDeduplicator() {
    }

    static String deduplicate(String html) {
        if (html == null || html.isEmpty()) return html;
        Set<String> seen = new HashSet<>();
        Matcher matcher = SCREENSHOT_LINK.matcher(html);
        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            String payload = matcher.group(1);
            String fingerprint = fingerprint(payload);
            if (seen.add(fingerprint)) {
                matcher.appendReplacement(result, Matcher.quoteReplacement(matcher.group()));
            } else {
                String replacement = "<span class=\"atf-ui-screenshot-duplicate\""
                        + " data-atf-screenshot-ref=\"" + fingerprint + "\">"
                        + "📷 same image — identical to an earlier captured state in this run;"
                        + " not re-embedded to avoid duplicating the same bytes.</span>";
                matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
            }
        }
        matcher.appendTail(result);
        return result.toString();
    }

    private static String fingerprint(String payload) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(payload.getBytes(StandardCharsets.US_ASCII));
            StringBuilder hex = new StringBuilder(digest.length * 2);
            for (byte value : digest) hex.append(String.format("%02x", value));
            return hex.toString();
        } catch (NoSuchAlgorithmException error) {
            throw new IllegalStateException("SHA-256 is required for screenshot deduplication", error);
        }
    }
}
