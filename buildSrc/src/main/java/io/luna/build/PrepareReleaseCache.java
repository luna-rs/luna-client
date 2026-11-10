package io.luna.build;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.TaskAction;
import org.gradle.work.DisableCachingByDefault;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.Map;

/**
 * Validates an explicitly supplied local cache before packaging a development client.
 *
 * @author lare96
 */
@DisableCachingByDefault(because = "The external cache is verified on every packaging build.")
public abstract class PrepareReleaseCache extends DefaultTask {
    public PrepareReleaseCache() {
        getOutputDirectory().convention(getProject().getLayout().getBuildDirectory().dir("prepared-cache"));
        getOutputs().upToDateWhen(task -> false);
    }

    @Optional
    @InputFile
    @PathSensitive(PathSensitivity.NONE)
    public abstract RegularFileProperty getLocalCacheZip();

    @OutputDirectory
    public abstract DirectoryProperty getOutputDirectory();

    @TaskAction
    public void prepareCache() {
        if (!getLocalCacheZip().isPresent()) {
            throw new GradleException("Supply your own cache ZIP with -PcacheZip=<path>. No game release is downloaded automatically.");
        }
        Path archive = getLocalCacheZip().get().getAsFile().toPath().toAbsolutePath().normalize();
        Path output = getOutputDirectory().get().getAsFile().toPath();
        try {
            if (!Files.isRegularFile(archive, LinkOption.NOFOLLOW_LINKS) || Files.size(archive) <= 0
                    || Files.size(archive) > ReleaseCache.MAX_ARCHIVE_BYTES) {
                throw new IOException("Invalid local cache archive type or size.");
            }
            ReleaseCache.prepare(archive, output, ReleaseCache.hash(archive), Files.size(archive), Map.of("mode", "local"));
            getLogger().lifecycle("Prepared six cache stores from the explicit local ZIP.");
        } catch (IOException failure) {
            throw new GradleException("Cache preparation failed: " + failure.getMessage());
        }
    }
}
