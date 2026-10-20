package atf.api.core.files;

import atf.api.testkit.AtfUnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@AtfUnitTest
class AtfPathLocatorTest {
    @TempDir
    Path temp;

    @Test
    void findsConfiguredFileFromNestedWorkingDirectory() throws Exception {
        Path project = Files.createDirectories(temp.resolve("project"));
        Path nested = Files.createDirectories(project.resolve("module/target"));
        Path config = Files.createFile(project.resolve("atf.yaml"));

        assertEquals(config, AtfPathLocator.firstRegularFileUpwards(
                nested, List.of("atf.yaml", "atf.yml")));
    }

    @Test
    void resolvesTheSourceRootRatherThanTheRequiredPackageDirectory() throws Exception {
        Path project = Files.createDirectories(temp.resolve("project"));
        Path sourceRoot = Files.createDirectories(project.resolve("atf-reporting/src/test/java"));
        Files.createDirectories(sourceRoot.resolve("atf/ui/tests"));

        assertEquals(sourceRoot, AtfPathLocator.locateSourceRoot(
                project.resolve("atf-reporting"), Path.of("atf", "ui", "tests"),
                List.of(Path.of("src", "test", "java"),
                        Path.of("atf-reporting", "src", "test", "java"))));
    }

    @Test
    void rejectsAbsoluteSourceRootCandidates() {
        assertThrows(IllegalArgumentException.class, () -> AtfPathLocator.locateSourceRoot(
                temp, Path.of("atf"), List.of(temp.resolve("src/test/java"))));
    }

    @Test
    void locatesTheAtfTestRootWithoutDuplicatingModuleCandidates() throws Exception {
        Path project = Files.createDirectories(temp.resolve("project"));
        Path sourceRoot = Files.createDirectories(project.resolve("atf-reporting/src/test/java"));
        Files.createDirectories(sourceRoot.resolve("atf/ui/tests"));

        assertEquals(sourceRoot, AtfPathLocator.locateAtfTestSourceRoot(
                project.resolve("atf-reporting"), Path.of("atf", "ui", "tests")));
    }
}
