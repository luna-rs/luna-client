package io.luna.build;

import groovy.json.JsonSlurper;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.LocalState;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.TaskAction;
import org.gradle.work.DisableCachingByDefault;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Resolves the selected public release on every online build and supplies verified cache inputs.
 *
 * @author lare96
 */
@DisableCachingByDefault(because = "Release asset contents can change without a source commit.")
public abstract class PrepareReleaseCache extends DefaultTask {
    private static final int REQUEST_ATTEMPTS = 3;

    public PrepareReleaseCache() {
        getRepository().convention("luna-rs/luna-client");
        getReleaseTag().convention("latest");
        getAssetName().convention("cache.zip");
        getOutputDirectory().convention(getProject().getLayout().getBuildDirectory().dir("prepared-cache"));
        getDownloadDirectory().convention(getProject().getLayout().getBuildDirectory().dir("release-cache-downloads"));
        getOutputs().upToDateWhen(task -> false);
    }

    @Input
    public abstract Property<String> getRepository();

    @Input
    public abstract Property<String> getReleaseTag();

    @Input
    public abstract Property<String> getAssetName();

    @Optional
    @InputFile
    @PathSensitive(PathSensitivity.NONE)
    public abstract RegularFileProperty getLocalCacheZip();

    @OutputDirectory
    public abstract DirectoryProperty getOutputDirectory();

    @LocalState
    public abstract DirectoryProperty getDownloadDirectory();

    @TaskAction
    public void prepareCache() {
        String repository = getRepository().get();
        String tag = getReleaseTag().get();
        String name = getAssetName().get();
        if (!repository.matches("[A-Za-z0-9_.-]+/[A-Za-z0-9_.-]+") || tag.isBlank() || tag.length() > 160
                || !name.matches("[A-Za-z0-9][A-Za-z0-9._-]*\\.zip")) throw new GradleException("Invalid release cache settings.");
        Path output = getOutputDirectory().get().getAsFile().toPath();
        Map<String, Object> source = new LinkedHashMap<>();
        source.put("repository", repository);
        source.put("requestedTag", tag);
        source.put("assetName", name);
        try {
            if (getLocalCacheZip().isPresent()) {
                Path archive = getLocalCacheZip().get().getAsFile().toPath().toAbsolutePath().normalize();
                if (!Files.isRegularFile(archive, LinkOption.NOFOLLOW_LINKS) || Files.size(archive) <= 0
                        || Files.size(archive) > ReleaseCache.MAX_ARCHIVE_BYTES) throw new IOException("Invalid local cache archive type or size.");
                source.put("mode", "local");
                ReleaseCache.prepare(archive, output, ReleaseCache.hash(archive), Files.size(archive), source);
                getLogger().lifecycle("Prepared six cache stores from the explicit local ZIP.");
            } else if (getProject().getGradle().getStartParameter().isOffline()) {
                ReleaseCache.verifyOffline(output, repository, tag, name);
                getLogger().lifecycle("Using the previously verified prepared cache for this offline build.");
            } else {
                HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(20))
                        .followRedirects(HttpClient.Redirect.NEVER).build();
                ReleaseCache.Asset asset = resolve(http, repository, tag, name);
                source.put("mode", "release");
                source.put("releaseTag", asset.tag());
                source.put("assetId", asset.id());
                source.put("downloadUrl", asset.downloadUrl().toString());
                Path archive = download(http, repository, asset);
                ReleaseCache.prepare(archive, output, asset.sha256(), asset.size(), source);
                getLogger().lifecycle("Prepared six verified cache stores from {}/{} (SHA-256 {}).", repository, asset.tag(), asset.sha256());
            }
        } catch (IOException | InterruptedException failure) {
            if (failure instanceof InterruptedException) Thread.currentThread().interrupt();
            throw new GradleException("Cache preparation failed: " + failure.getMessage());
        }
    }

    private ReleaseCache.Asset resolve(HttpClient http, String repository, String tag, String name)
            throws IOException, InterruptedException {
        String base = "https://api.github.com/repos/" + repository + "/releases";
        if (!tag.equals("latest")) {
            Object result = metadata(http, URI.create(base + "/tags/" + URLEncoder.encode(tag, StandardCharsets.UTF_8).replace("+", "%20")));
            if (!(result instanceof Map<?, ?> release)) throw new IOException("Invalid release response.");
            return ReleaseCache.selectTagged(release, name);
        }
        List<Object> releases = new ArrayList<>();
        for (int page = 1; page <= 10; page++) {
            Object result = metadata(http, URI.create(base + "?per_page=100&page=" + page));
            if (!(result instanceof List<?> values)) throw new IOException("Invalid release list response.");
            releases.addAll(values);
            if (values.size() < 100) return ReleaseCache.selectLatest(releases, name);
        }
        throw new IOException("Too many releases to select automatically; configure cacheReleaseTag explicitly.");
    }

    private Object metadata(HttpClient http, URI uri) throws IOException, InterruptedException {
        try (InputStream body = response(http, uri, true)) {
            byte[] bytes = body.readNBytes(4 * 1024 * 1024 + 1);
            if (bytes.length > 4 * 1024 * 1024) throw new IOException("Release metadata exceeds its size limit.");
            try {
                return new JsonSlurper().parseText(new String(bytes, StandardCharsets.UTF_8));
            } catch (RuntimeException invalid) {
                throw new IOException("GitHub returned invalid release metadata.");
            }
        }
    }

    private Path download(HttpClient http, String repository, ReleaseCache.Asset asset) throws IOException, InterruptedException {
        Path directory = getDownloadDirectory().get().getAsFile().toPath().toAbsolutePath().normalize();
        for (Path part = directory; part != null; part = part.getParent()) {
            if (Files.isSymbolicLink(part)) throw new IOException("Cache download directory must not contain links.");
        }
        Files.createDirectories(directory);
        Path archive = directory.resolve(asset.sha256() + ".zip");
        if (Files.exists(archive, LinkOption.NOFOLLOW_LINKS)) {
            if (Files.isSymbolicLink(archive) || !Files.isRegularFile(archive)) throw new IOException("Unexpected cached archive path.");
            try {
                ReleaseCache.verifyArchive(archive, asset.sha256(), asset.size());
                return archive;
            } catch (IOException corrupt) {
                Files.delete(archive);
            }
        }
        Path temporary = Files.createTempFile(directory, "cache-download-", ".tmp");
        try {
            URI binary = URI.create("https://api.github.com/repos/" + repository + "/releases/assets/" + asset.id());
            try (InputStream input = response(http, binary, false); var output = Files.newOutputStream(temporary)) {
                byte[] buffer = new byte[65536];
                long total = 0;
                for (int read; (read = input.read(buffer)) != -1;) {
                    total += read;
                    if (total > asset.size() || total > ReleaseCache.MAX_ARCHIVE_BYTES) throw new IOException("Cache download exceeds its advertised size.");
                    output.write(buffer, 0, read);
                }
            }
            ReleaseCache.verifyArchive(temporary, asset.sha256(), asset.size());
            Files.move(temporary, archive, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            return archive;
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    private static InputStream response(HttpClient http, URI initial, boolean metadata) throws IOException, InterruptedException {
        URI uri = initial;
        for (int redirects = 0; redirects <= 5; redirects++) {
            String host = uri.getHost();
            boolean apiHost = "api.github.com".equals(host);
            boolean allowed = metadata ? apiHost : apiHost || "github.com".equals(host)
                    || "objects.githubusercontent.com".equals(host) || "release-assets.githubusercontent.com".equals(host)
                    || "github-releases.githubusercontent.com".equals(host);
            if (!allowed || !"https".equals(uri.getScheme()) || uri.getUserInfo() != null) throw new IOException("Unexpected release download host.");
            HttpRequest.Builder request = HttpRequest.newBuilder(uri).timeout(Duration.ofMinutes(3))
                    .header("User-Agent", "luna-client-cache-build").header("Accept", metadata ? "application/vnd.github+json" : "application/octet-stream");
            if (apiHost) {
                request.header("X-GitHub-Api-Version", "2022-11-28");
                String token = System.getenv("GITHUB_TOKEN");
                if (token != null && !token.isBlank()) request.header("Authorization", "Bearer " + token);
            }
            HttpResponse<InputStream> result = sendWithRetries(http, request.GET().build());
            int status = result.statusCode();
            if (status == 200) return result.body();
            result.body().close();
            if (status == 301 || status == 302 || status == 303 || status == 307 || status == 308) {
                String location = result.headers().firstValue("location").orElseThrow(() -> new IOException("Release redirect lacks its destination."));
                uri = uri.resolve(location);
            } else {
                throw new IOException("GitHub release request returned HTTP " + status + "; check release settings and API access.");
            }
        }
        throw new IOException("Too many release download redirects.");
    }

    /**
     * Retries safe GET transport failures and temporary gateway responses without relaxing release validation.
     */
    private static HttpResponse<InputStream> sendWithRetries(HttpClient http, HttpRequest request)
            throws IOException, InterruptedException {
        for (int attempt = 1; attempt <= REQUEST_ATTEMPTS; attempt++) {
            HttpResponse<InputStream> result;
            try {
                result = http.send(request, HttpResponse.BodyHandlers.ofInputStream());
            } catch (IOException failure) {
                if (attempt == REQUEST_ATTEMPTS) {
                    throw new IOException("GitHub release request to " + request.uri().getHost()
                            + " failed after " + REQUEST_ATTEMPTS + " attempts; check network access.");
                }
                Thread.sleep(1000L * attempt);
                continue;
            }
            int status = result.statusCode();
            if (attempt == REQUEST_ATTEMPTS || status != 502 && status != 503 && status != 504) return result;
            try {
                result.body().close();
            } catch (IOException ignored) {
                // A failed temporary response must not prevent a fresh bounded request.
            }
            Thread.sleep(1000L * attempt);
        }
        throw new IOException("GitHub release request exceeded its retry limit.");
    }
}
