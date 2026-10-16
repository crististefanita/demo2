package atf.api.reporting;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Base64;
import java.util.List;
import java.util.Objects;

/**
 * Publishes temporary Playwright captures as self-contained, collapsible
 * evidence in the final Extent HTML artifact.
 */
public final class ExtentReportUiEvidence {
    private static final String SECTION_ID = "atf-ui-exploratory-evidence";

    private ExtentReportUiEvidence() {
    }

    public record Capture(Path png, String title, List<String> steps) {
        public Capture {
            Objects.requireNonNull(png, "png");
            Objects.requireNonNull(title, "title");
            steps = List.copyOf(Objects.requireNonNull(steps, "steps"));
        }
    }

    /**
     * Embeds PNG captures and their audit steps into the report Overview.
     * The source files are not copied beside the report and are never
     * referenced by a relative URL.
     */
    public static void embedInlinePngs(Path report, List<Capture> captures) throws IOException {
        Objects.requireNonNull(report, "report");
        Objects.requireNonNull(captures, "captures");
        if (captures.isEmpty()) {
            return;
        }
        String html = Files.readString(report, StandardCharsets.UTF_8);
        String evidence = render(captures);
        html = html.replaceAll("(?s)<section id=\"" + SECTION_ID + "\".*?</section>", "");
        int dashboard = html.indexOf("<div id=\"dashboard-view\"");
        int openingEnd = dashboard < 0 ? -1 : html.indexOf('>', dashboard);
        if (openingEnd < 0) {
            throw new IOException("Extent report does not expose dashboard-view: " + report);
        }
        html = html.substring(0, openingEnd + 1) + evidence + html.substring(openingEnd + 1);
        writeAtomically(report, html);
    }

    private static String render(List<Capture> captures) throws IOException {
        StringBuilder html = new StringBuilder();
        html.append("<style id=\"atf-ui-evidence-style\">")
                .append("#").append(SECTION_ID).append("{margin-top:24px;padding:18px;"
                        + "border:1px solid #2b3d5a;border-radius:5px;background:#111a29;color:#dbe7ff}")
                .append("#").append(SECTION_ID).append(" h2{margin:0 0 12px;font-size:16px;font-weight:400}")
                .append("#").append(SECTION_ID).append(" details{margin:10px 0;border:1px solid #2b3d5a;"
                        + "border-radius:4px;padding:10px;background:#151f31}")
                .append("#").append(SECTION_ID).append(" summary{cursor:pointer;color:#80aaff}")
                .append("#").append(SECTION_ID).append(" ol{margin:12px 0;color:#b8c5dc;font-size:12px}")
                .append("#").append(SECTION_ID).append(" .atf-ui-evidence-screenshot-link{display:inline-flex;"
                        + "align-items:flex-end;align-self:flex-end;justify-content:flex-start;vertical-align:bottom;margin:14px 0 0;"
                        + "transform:none;text-decoration:none}")
                .append("#").append(SECTION_ID).append(" .atf-ui-evidence-screenshot-link:hover{"
                        + "background:#1a2940;text-decoration:underline}")
                .append("</style>")
                .append("<section id=\"").append(SECTION_ID).append("\">")
                .append("<h2>Playwright exploratory evidence · ").append(captures.size())
                .append(" captures</h2>");
        for (Capture capture : captures) {
            Path png = capture.png().toAbsolutePath().normalize();
            if (!Files.isRegularFile(png)) {
                throw new IOException("Playwright capture does not exist: " + png);
            }
            String base64 = Base64.getEncoder().encodeToString(Files.readAllBytes(png));
            html.append("<details><summary>").append(escape(capture.title())).append("</summary>")
                    .append("<ol>");
            for (String step : capture.steps()) {
                html.append("<li>").append(escape(step)).append("</li>");
            }
            html.append("</ol><a class=\"atf-ui-evidence-screenshot-link\" data-atf-screenshot=\"base64\"")
                    .append(" href=\"data:image/png;base64,").append(base64)
                    .append("\" target=\"_blank\" rel=\"noopener\" aria-label=\"Open ")
                    .append(escape(capture.title()))
                    .append("\"><span class=\"badge badge-gradient-primary\">📷 base64 img</span></a></details>");
        }
        return html.append("</section>").toString();
    }

    private static String escape(String value) {
        return value.replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;");
    }

    private static void writeAtomically(Path report, String html) throws IOException {
        Path absolute = report.toAbsolutePath().normalize();
        Path temporary = Files.createTempFile(absolute.getParent(), absolute.getFileName().toString(), ".tmp");
        try {
            Files.writeString(temporary, html, StandardCharsets.UTF_8);
            try {
                Files.move(temporary, absolute, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (IOException ignored) {
                Files.move(temporary, absolute, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporary);
        }
    }
}
