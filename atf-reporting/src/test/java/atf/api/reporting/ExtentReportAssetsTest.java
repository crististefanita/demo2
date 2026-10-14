package atf.api.reporting;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ExtentReportAssetsTest {
    @Test
    void inlinesSupportedSparkAssetsWithoutLeavingExternalReferences() throws Exception {
        Path root = Files.createTempDirectory("atf-assets-");
        Path spark = Files.createDirectory(root.resolve("spark"));
        Files.writeString(spark.resolve("spark-style.css"), ".spark{color:red}");
        Files.writeString(spark.resolve("spark-script.js"), "window.sparkLoaded=true;");
        Files.write(spark.resolve("logo.png"), new byte[]{1, 2, 3});

        String source = "<link rel=\"stylesheet\" href=\"spark/spark-style.css\">"
                + "<script src=\"spark/spark-script.js\"></script>spark/logo.png";
        String result = ExtentReportAssets.inlineSparkAssets(source, spark);

        assertTrue(result.contains("data-atf-inline=\"spark-style.css\""));
        assertTrue(result.contains(".spark{color:red}"));
        assertTrue(result.contains("data-atf-inline=\"spark-script.js\""));
        assertTrue(result.contains("window.sparkLoaded=true;"));
        assertTrue(result.contains("data:image/png;base64,AQID"));
        assertFalse(result.contains("spark/spark-style.css"));
        assertFalse(result.contains("spark/spark-script.js"));

        ExtentReportAssets.removeGeneratedAssets(spark);
        assertFalse(Files.exists(spark));
        Files.deleteIfExists(root);
    }

    @Test
    void missingAssetsAreAStableNoOpAndNonSparkDirectoriesAreNeverRemoved() throws Exception {
        Path root = Files.createTempDirectory("atf-assets-");
        Path nonSpark = Files.createDirectory(root.resolve("not-spark"));
        Path marker = Files.writeString(nonSpark.resolve("marker.txt"), "keep");

        assertEquals("plain", ExtentReportAssets.inlineSparkAssets("plain", nonSpark));
        ExtentReportAssets.removeGeneratedAssets(nonSpark);

        assertTrue(Files.exists(marker));
        assertTrue(Files.exists(nonSpark));
        Files.deleteIfExists(marker);
        Files.deleteIfExists(nonSpark);
        Files.deleteIfExists(root);
    }
}
