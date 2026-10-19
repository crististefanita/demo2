package atf.api.reporting.evidence;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Removes repeated embedded screenshot payloads while keeping every visual state
 * actionable through a visible inline reference to the first PNG anchor.
 */
public final class ExtentReportScreenshotDeduplicator {
    private static final Pattern SCREENSHOT_LINK = Pattern.compile(
            "<a\\b(?=[^>]*data-atf-screenshot=\\\"base64\\\")"
                    + "(?=[^>]*href=\\\"data:image/png;base64,([^\\\"]+)\\\")[^>]*>.*?</a>",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    private ExtentReportScreenshotDeduplicator() {
    }

    public static String deduplicate(String html) {
        if (html == null || html.isEmpty()) return html;
        Set<String> seen = new HashSet<>();
        Matcher matcher = SCREENSHOT_LINK.matcher(html);
        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            String payload = matcher.group(1);
            String fingerprint = fingerprint(payload);
            if (seen.add(fingerprint)) {
                matcher.appendReplacement(result,
                        Matcher.quoteReplacement(withCanonicalReference(matcher.group(), fingerprint)));
            } else {
                String replacement = "<a class=\"atf-ui-action-screenshot-link atf-ui-screenshot-duplicate\""
                        + " data-atf-screenshot-ref=\"" + fingerprint + "\""
                        + " href=\"#atf-screenshot-" + fingerprint + "\""
                        + " aria-label=\"Open duplicate screenshot\""
                        + " title=\"Open duplicate screenshot\">"
                        + "<span class=\"badge-gradient-primary\">📷 base64 img</span>"
                        + "<span class=\"atf-ui-screenshot-duplicate-text\">"
                        + "same image — identical to an earlier captured state in this run;"
                        + " not re-embedded to avoid duplicating the same bytes.</span></a>";
                matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
            }
        }
        matcher.appendTail(result);
        return result.toString();
    }

    private static String withCanonicalReference(String anchor, String fingerprint) {
        String openingTag = anchor.substring(0, anchor.indexOf('>') + 1);
        if (openingTag.contains("data-atf-screenshot-ref=\"")) return anchor;
        String referenceAttributes = " id=\"atf-screenshot-" + fingerprint
                + "\" data-atf-screenshot-ref=\"" + fingerprint + "\"";
        String enrichedOpeningTag = openingTag.substring(0, openingTag.length() - 1)
                + referenceAttributes + ">";
        return enrichedOpeningTag + anchor.substring(openingTag.length());
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
