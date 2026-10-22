package atf.api.reporting.support.publication;

import atf.api.reporting.extent.session.ExtentReportFinalizer;
import atf.api.reporting.support.ReportTestScreenshots;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryType;
import java.nio.file.Files;
import java.nio.file.Path;

/** Dedicated JVM entry point; the parent component test supplies the explicit heap limit. */
public final class LargeReportPublicationProbe {
    private static final int SCREENSHOTS = 1_500;
    private static final int UNICODE_REPETITIONS = 350_000;

    private LargeReportPublicationProbe() { }

    public static void main(String[] args) throws Exception {
        if (args.length != 1) throw new IllegalArgumentException("Expected output report path");
        Path report = Path.of(args[0]);
        Files.createDirectories(report.toAbsolutePath().getParent());
        var html = new StringBuilder(14_000_000);
        html.append("<!doctype html><html><head></head><body class=\"spa -report dark\">")
                .append("<div class=\"test-wrapper row view test-view\">");
        for (int index = 0; index < SCREENSHOTS; index++) {
            String png = index % 2 == 0
                    ? ReportTestScreenshots.ONE_PIXEL_PNG : ReportTestScreenshots.SECOND_PIXEL_PNG;
            html.append("<a data-atf-screenshot=\"base64\" href=\"data:image/png;base64,")
                    .append(png)
                    .append("\">📷 image ")
                    .append(index)
                    .append("</a>");
        }
        html.append("<p>").append("Audit acțiune ✓ {JSON} ".repeat(UNICODE_REPETITIONS))
                .append("evidence-end-marker</p></div>")
                .append("<div class=\"test-wrapper row view category-view attributes-view\"></div>")
                .append("<div class=\"container-fluid p-4 view dashboard-view\"></div>")
                .append("</body></html>");
        Files.writeString(report, html);
        html = null;
        System.gc();
        ExtentReportFinalizer.finalizeReport(report);
        long peak = ManagementFactory.getMemoryPoolMXBeans().stream()
                .filter(pool -> pool.getType() == MemoryType.HEAP)
                .mapToLong(pool -> pool.getPeakUsage().getUsed())
                .sum();
        System.out.println("PEAK_HEAP_BYTES=" + peak);
    }
}
