package atf.api.reporting.extent.session;
import atf.api.reporting.evidence.ExtentReportScreenshotDeduplicator;
import atf.api.reporting.markup.ExtentReportAssets;
import atf.api.reporting.markup.ExtentReportCategoryMarkup;
import atf.api.reporting.markup.ExtentReportNavigationView;
import atf.api.reporting.markup.ExtentReportOverviewMarkup;
import atf.api.reporting.runtime.core.ExtentReportRuntimeBehaviour;
import atf.api.reporting.theme.AtfReportTheme;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

/**
 * Converts the vendor Spark output into the self-contained ATF report contract.
 * The vendor remains responsible for the report model; this class owns only
 * deterministic publication concerns such as assets, navigation and defaults.
 */
public final class ExtentReportPublicationPipeline {
    private ExtentReportPublicationPipeline() {
    }

    public static void publish(Path reportPath) throws IOException {
        Objects.requireNonNull(reportPath, "reportPath");
        if (!Files.isRegularFile(reportPath)) {
            throw new IOException("Extent report does not exist: " + reportPath);
        }

        Path assetDirectory = reportPath.toAbsolutePath().normalize().getParent()
                .resolve(ExtentReportAssets.SPARK_DIRECTORY);
        String html = Files.readString(reportPath, StandardCharsets.UTF_8);
        html = ExtentReportAssets.inlineSparkAssets(html, assetDirectory);
        html = injectAtfTheme(html);
        html = injectNavigationAndFailureView(html);
        html = ensureViewIdentifiersAndOverviewDefault(html);
        html = ExtentReportCategoryMarkup.ensureView(html);
        html = injectOverviewMetadata(html);
        html = injectRuntimeBehaviour(html);
        html = removeLegacyFailureTreeHook(html);
        html = ExtentReportScreenshotDeduplicator.deduplicate(html);
        writeAtomically(reportPath, html);
        ExtentReportAssets.removeGeneratedAssets(assetDirectory);
    }

    private static String injectAtfTheme(String html) {
        String css = AtfReportTheme.css();
        html = html.replace("</head>", "<style id=\"atf-report-theme\">" + css + "</style></head>");
        return html.replace("<body class=\"spa -report dark\">", "<body class=\"spa -report dark atf-overview-default\">");
    }

    /** Fails keeps only its diagnostic index; the full tree remains in Tests. */
    private static String removeLegacyFailureTreeHook(String html) {
        return html.replace("atf-failure-test-detail", "atf-failure-detail");
    }

    private static String injectNavigationAndFailureView(String html) {
        return ExtentReportNavigationView.inject(html);
    }

    private static String ensureViewIdentifiersAndOverviewDefault(String html) {
        html = html.replaceFirst("class=\"test-wrapper row view test-view\"",
                "id=\"test-view\" class=\"test-wrapper row view test-view hide\"");
        html = html.replaceFirst("class=\"test-wrapper row view category-view attributes-view\"",
                "id=\"category-view\" class=\"test-wrapper row view category-view attributes-view hide\"");
        return html;
    }

    private static String injectOverviewMetadata(String html) {
        return ExtentReportOverviewMarkup.inject(html);
    }

    private static String injectRuntimeBehaviour(String html) {
        return ExtentReportRuntimeBehaviour.inject(html);
    }

    private static void writeAtomically(Path reportPath, String html) throws IOException {
        Path parent = reportPath.toAbsolutePath().normalize().getParent();
        Path temp = Files.createTempFile(parent, reportPath.getFileName().toString(), ".tmp");
        try {
            Files.writeString(temp, html, StandardCharsets.UTF_8);
            try {
                Files.move(temp, reportPath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (IOException ignored) {
                Files.move(temp, reportPath, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temp);
        }
    }

}


