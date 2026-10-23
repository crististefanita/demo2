package atf.ui.tests.extent.support.config.output;

import java.nio.file.Path;

/** Resolves a test owner's optional subdirectory without escaping the UI output boundary. */
public final class UiReportOutputDirectory {
    private UiReportOutputDirectory() { }

    public static Path resolve(Path uiReportDirectory, Class<?> testClass) {
        Path base = uiReportDirectory.toAbsolutePath().normalize();
        if (testClass == null) return base;
        UiReportSubdirectory annotation = testClass.getAnnotation(UiReportSubdirectory.class);
        if (annotation == null) return base;
        String value = annotation.value().trim();
        if (value.isEmpty()) throw new IllegalStateException("UI report subdirectory must not be blank");
        Path relative = Path.of(value);
        if (relative.isAbsolute()) {
            throw new IllegalStateException("UI report subdirectory must be relative: " + value);
        }
        Path resolved = base.resolve(relative).normalize();
        if (!resolved.startsWith(base)) {
            throw new IllegalStateException("UI report subdirectory escapes the standard output directory: " + value);
        }
        return resolved;
    }
}
