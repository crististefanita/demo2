package atf.ui.tests.extent.support.inspection.layout;

import com.microsoft.playwright.Locator;

import java.util.Objects;

/** Browser adapter that audits the runtime-generated assertion column layout. */
public final class ValidationTableLayoutInspector {
    public Result inspect(Locator table) {
        Objects.requireNonNull(table, "table");
        String evidence = table.evaluate("""
                element => {
                  const rows = Array.from(element.rows || []);
                  const count = rows.length ? rows[0].cells.length : 0;
                  const longest = [];
                  for (const row of rows) {
                    Array.from(row.cells || []).forEach((cell, index) => {
                      const value = String(cell.textContent || '').replace(/\\s+/g, ' ').trim();
                      longest[index] = Math.max(longest[index] || 0, value.length);
                    });
                  }
                  const widths = String(element.getAttribute('data-atf-validation-column-widths') || '')
                    .split('|').filter(Boolean).map(value => Number.parseFloat(value));
                  const flexible = longest.slice(0, Math.max(0, count - 1));
                  const longestIndex = flexible.length
                    ? flexible.indexOf(Math.max(...flexible)) : -1;
                  const widestIndex = widths.length > 1
                    ? widths.slice(0, -1).indexOf(Math.max(...widths.slice(0, -1))) : -1;
                  return [
                    element.getAttribute('data-atf-validation-layout') || '',
                    count,
                    widths.join(','),
                    widths.reduce((sum, value) => sum + value, 0),
                    longestIndex,
                    widestIndex,
                    element.scrollWidth,
                    element.clientWidth
                  ].join('|');
                }
                """).toString();
        String[] parts = evidence.split("\\|", -1);
        if (parts.length != 8) return new Result(false, evidence);
        try {
            int columns = Integer.parseInt(parts[1]);
            double widthSum = Double.parseDouble(parts[3]);
            int longestIndex = Integer.parseInt(parts[4]);
            int widestIndex = Integer.parseInt(parts[5]);
            double scrollWidth = Double.parseDouble(parts[6]);
            double clientWidth = Double.parseDouble(parts[7]);
            boolean valid = "dynamic-longest-value".equals(parts[0])
                    && columns >= 2
                    && parts[2].split(",", -1).length == columns
                    && widthSum >= 99.0 && widthSum <= 101.0
                    && longestIndex >= 0 && longestIndex == widestIndex
                    && scrollWidth <= clientWidth + 1.0;
            return new Result(valid, evidence);
        } catch (NumberFormatException ignored) {
            return new Result(false, evidence);
        }
    }

    public record Result(boolean valid, String evidence) {
        public Result {
            Objects.requireNonNull(evidence, "evidence");
        }
    }
}
