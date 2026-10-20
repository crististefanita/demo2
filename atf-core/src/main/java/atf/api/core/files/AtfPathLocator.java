package atf.api.core.files;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/** Reusable, root-independent file and source-root discovery utilities. */
public final class AtfPathLocator {
    private static final List<Path> ATF_TEST_SOURCE_ROOT_CANDIDATES = List.of(
            Path.of("src", "test", "java"),
            Path.of("atf-reporting", "src", "test", "java"));

    private AtfPathLocator() {
    }

    /** Finds the first configured regular file while walking from a directory to its ancestors. */
    public static Path firstRegularFileUpwards(Path start, Collection<String> fileNames) {
        Path current = normalizeStart(start);
        List<String> names = List.copyOf(Objects.requireNonNull(fileNames, "fileNames"));
        if (names.isEmpty() || names.stream().anyMatch(name -> name == null || name.isBlank())) {
            throw new IllegalArgumentException("fileNames must contain at least one non-blank name");
        }
        while (current != null) {
            for (String name : names) {
                Path candidate = current.resolve(name).normalize();
                if (candidate.getParent().equals(current) && Files.isRegularFile(candidate)) {
                    return candidate;
                }
            }
            current = current.getParent();
        }
        return null;
    }

    /**
     * Finds a source root candidate whose required package/path exists below it.
     * The returned path is the source root, not the package marker.
     */
    public static Path locateSourceRoot(Path start, Path requiredRelativePath,
                                        Collection<Path> sourceRootCandidates) {
        Path required = relativePath(requiredRelativePath, "requiredRelativePath");
        List<Path> candidates = List.copyOf(Objects.requireNonNull(
                sourceRootCandidates, "sourceRootCandidates"));
        if (candidates.isEmpty()) {
            throw new IllegalArgumentException("sourceRootCandidates must not be empty");
        }
        if (candidates.stream().anyMatch(path -> path == null || path.isAbsolute())) {
            throw new IllegalArgumentException("source root candidates must be relative");
        }

        Path current = normalizeStart(start);
        while (current != null) {
            for (Path candidate : candidates) {
                Path sourceRoot = current.resolve(candidate).normalize();
                if (sourceRoot.getParent() != null
                        && sourceRoot.getParent().startsWith(current)
                        && Files.isDirectory(sourceRoot)
                        && Files.isDirectory(sourceRoot.resolve(required))) {
                    return sourceRoot;
                }
            }
            current = current.getParent();
        }
        throw new IllegalStateException("Cannot locate source root containing " + required);
    }

    /**
     * Locates an ATF test source root using the repository's supported module
     * layouts. Test code should consume this seam instead of repeating
     * user.dir traversal and module-specific candidate lists.
     */
    public static Path locateAtfTestSourceRoot(Path start, Path requiredRelativePath) {
        return locateSourceRoot(start, requiredRelativePath, ATF_TEST_SOURCE_ROOT_CANDIDATES);
    }

    private static Path normalizeStart(Path start) {
        Path normalized = Objects.requireNonNull(start, "start").toAbsolutePath().normalize();
        return Files.isDirectory(normalized) ? normalized : normalized.getParent();
    }

    private static Path relativePath(Path path, String name) {
        Path normalized = Objects.requireNonNull(path, name).normalize();
        if (normalized.isAbsolute() || normalized.getNameCount() == 0) {
            throw new IllegalArgumentException(name + " must be a non-empty relative path");
        }
        return normalized;
    }
}
