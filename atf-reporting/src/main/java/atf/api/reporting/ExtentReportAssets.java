package atf.api.reporting;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Comparator;
import java.util.stream.Stream;

/**
 * Owns only the Spark asset boundary of the Extent publication pipeline.
 * Keeping this concern separate makes asset inlining and cleanup testable
 * without constructing a report session or exercising the HTML runtime.
 */
final class ExtentReportAssets {
    static final String SPARK_DIRECTORY = "spark";

    private ExtentReportAssets() {
    }

    static String inlineSparkAssets(String html, Path assetDirectory) throws IOException {
        String result = inlineTextAsset(html, assetDirectory, "spark-style.css", "style");
        result = inlineTextAsset(result, assetDirectory, "font-awesome.min.css", "style");
        result = inlineFontAsset(result, assetDirectory, "fontawesome-webfont.eot", "application/vnd.ms-fontobject");
        result = inlineFontAsset(result, assetDirectory, "fontawesome-webfont.woff2", "font/woff2");
        result = inlineFontAsset(result, assetDirectory, "fontawesome-webfont.woff", "font/woff");
        result = inlineFontAsset(result, assetDirectory, "fontawesome-webfont.ttf", "font/ttf");
        result = inlineFontAsset(result, assetDirectory, "fontawesome-webfont.svg", "image/svg+xml");
        result = inlineTextAsset(result, assetDirectory, "jsontree.js", "script");
        result = inlineTextAsset(result, assetDirectory, "spark-script.js", "script");
        return inlineImageAsset(result, assetDirectory.resolve("logo.png"));
    }

    static void removeGeneratedAssets(Path assetDirectory) throws IOException {
        if (!Files.isDirectory(assetDirectory)
                || !SPARK_DIRECTORY.equals(assetDirectory.getFileName().toString())) {
            return;
        }
        try (Stream<Path> paths = Files.walk(assetDirectory)) {
            paths.sorted(Comparator.reverseOrder()).forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (IOException error) {
                    throw new AssetCleanupException(error);
                }
            });
        } catch (AssetCleanupException error) {
            throw error.cause;
        }
    }

    private static String inlineTextAsset(String html, Path assetDirectory, String fileName,
                                          String element) throws IOException {
        Path asset = assetDirectory.resolve(fileName);
        if (!Files.isRegularFile(asset)) {
            return html;
        }
        String content = Files.readString(asset, StandardCharsets.UTF_8);
        String replacement = "style".equals(element)
                ? "<style data-atf-inline=\"" + fileName + "\">" + content + "</style>"
                : "<script data-atf-inline=\"" + fileName + "\">" + content + "</script>";
        String escapedName = "spark/" + fileName;
        String source = "style".equals(element)
                ? "<link rel=\"stylesheet\" href=\"" + escapedName + "\">"
                : "<script src=\"" + escapedName + "\"></script>";
        return html.replace(source, replacement);
    }

    private static String inlineImageAsset(String html, Path logo) throws IOException {
        if (!Files.isRegularFile(logo)) {
            return html;
        }
        String dataUri = "data:image/png;base64,"
                + Base64.getEncoder().encodeToString(Files.readAllBytes(logo));
        return html.replace("spark/logo.png", dataUri);
    }

    private static String inlineFontAsset(String html, Path assetDirectory, String fileName, String mimeType)
            throws IOException {
        Path asset = findAsset(assetDirectory, fileName);
        if (asset == null) {
            return html;
        }
        String dataUri = "data:" + mimeType + ";base64,"
                + Base64.getEncoder().encodeToString(Files.readAllBytes(asset));
        return html.replace(fileName + "?v=4.7.0", dataUri)
                .replace(fileName + "?#iefix&v=4.7.0", dataUri)
                .replace(fileName + "?v=4.7.0#fontawesomeregular", dataUri);
    }

    private static Path findAsset(Path assetDirectory, String fileName) throws IOException {
        if (!Files.isDirectory(assetDirectory)) {
            return null;
        }
        Path direct = assetDirectory.resolve(fileName);
        if (Files.isRegularFile(direct)) {
            return direct;
        }
        try (Stream<Path> paths = Files.walk(assetDirectory)) {
            return paths.filter(path -> path.getFileName().toString().equals(fileName))
                    .filter(Files::isRegularFile)
                    .findFirst()
                    .orElse(null);
        }
    }

    private static final class AssetCleanupException extends RuntimeException {
        private final IOException cause;

        private AssetCleanupException(IOException cause) {
            this.cause = cause;
        }
    }
}
