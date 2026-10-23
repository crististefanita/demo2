package atf.api.testkit.output;

import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.Path;

/** Optional OS facility used only by the symlink boundary probe. */
final class ReportOutputSymlinkFixture {
    private ReportOutputSymlinkFixture() { }

    static boolean createIfSupported(Path link, Path target) {
        try {
            Files.createSymbolicLink(link, target);
            return true;
        } catch (FileSystemException | UnsupportedOperationException unavailable) {
            return false;
        } catch (java.io.IOException error) {
            throw new IllegalStateException("Cannot create symlink fixture: " + link, error);
        }
    }
}
