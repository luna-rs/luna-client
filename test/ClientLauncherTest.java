import luna.ClientConfigurationFixture;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.jar.JarFile;
import java.util.stream.Stream;

/**
 * Verifies root and packaged configuration before a fake client initializes in isolated fresh JVMs.
 *
 * @author lare96
 */
public final class ClientLauncherTest {

    private static final String SUCCESS = "Client launcher fixture passed";

    public static void main(String[] args) throws Exception {
        check(args.length == 0 || args.length == 2,
                "supply both development and beta JAR paths, or neither");
        Path fixture = Files.createTempDirectory("luna-client-launcher-test-").toAbsolutePath();
        try {
            Path fakeClient = fixture.resolve("fake-client");
            Files.createDirectories(fakeClient);
            compileClientFixture(fakeClient);
            Path working = Files.createDirectory(fixture.resolve("unrelated working directory"));
            Path classesRoot = fixture.resolve("classes application");
            Path classes = classesRoot.resolve("build/classes/java/main");
            copyProductionClasses(classes);
            writeSettings(classesRoot);
            String classpath = fakeClient + File.pathSeparator + classes;
            launch(fixture, working, "development-classes", classpath, "ClientLauncher",
                    classesRoot.resolve("client.properties"), null, false);
            launch(fixture, working, "rejected-arguments", classpath, "ClientLauncher", null,
                    "startup arguments are not supported", true);

            Files.delete(classesRoot.resolve("client.properties"));
            launch(fixture, working, "missing-settings", classpath, "ClientLauncher", null,
                    "client.properties", false);
            Properties invalid = profile("Invalid configuration", "invalid.invalid", 4444, false, true, false);
            invalid.setProperty("graphicsD3d", "not-a-boolean");
            ClientConfigurationFixture.write(classesRoot.resolve("client.properties"), invalid);
            launch(fixture, working, "invalid-settings", classpath, "ClientLauncher", null,
                    "graphicsD3d", false);

            if (args.length == 2) {
                Files.writeString(fixture.resolve("build.gradle"), "// Unrelated ancestor build must not own relocated executables.\n");
                packagedLauncher(fixture, working, fakeClient, Path.of(args[0]), "client.properties", false);
                packagedLauncher(fixture, working, fakeClient, Path.of(args[1]), "client (2).properties", false);
                packagedLauncher(fixture, working, fakeClient, Path.of(args[0]), "client.properties", true);
                packagedLauncher(fixture, working, fakeClient, Path.of(args[1]), "client (2).properties", true);
            }
        } finally {
            try (Stream<Path> paths = Files.walk(fixture)) {
                for (Path path : paths.sorted(Comparator.reverseOrder()).toArray(Path[]::new)) Files.delete(path);
            }
        }
        System.out.println("ClientLauncherTest passed");
    }

    private static Properties profile(String name, String host, int port, boolean d3d, boolean openGl, boolean noDraw) {
        Properties properties = ClientConfigurationFixture.defaults();
        properties.setProperty("clientName", name);
        properties.setProperty("connectHost", host);
        properties.setProperty("connectPort", Integer.toString(port));
        properties.setProperty("graphicsD3d", Boolean.toString(d3d));
        properties.setProperty("graphicsOpenGl", Boolean.toString(openGl));
        properties.setProperty("graphicsNoDraw", Boolean.toString(noDraw));
        return properties;
    }

    private static void writeSettings(Path root) throws IOException {
        Files.createDirectories(root);
        ClientConfigurationFixture.write(root.resolve("client.properties"),
                profile("Development fixture", "development.invalid", 43594, false, false, true));
    }

    private static void copyProductionClasses(Path destination) throws Exception {
        Path source = Path.of(ClientLauncher.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        check(Files.isDirectory(source), "class-directory verification uses compiled production classes");
        try (Stream<Path> paths = Files.walk(source)) {
            for (Path entry : paths.filter(path -> Files.isRegularFile(path) && path.toString().endsWith(".class"))
                    .toArray(Path[]::new)) {
                Path output = destination.resolve(source.relativize(entry));
                Files.createDirectories(output.getParent());
                Files.copy(entry, output);
            }
        }
    }

    private static void compileClientFixture(Path fixture) throws IOException {
        Path source = fixture.resolve("client.java");
        Files.writeString(source, """
                import java.nio.file.Path;
                import java.nio.file.Files;
                import luna.ClientConfiguration;

                /**
                 * Rejects file selection or graphics configuration applied after client initialization starts.
                 *
                 * @author lare96
                 */
                public final class client {
                    static {
                        ClientConfiguration configuration = ClientConfiguration.get();
                        require(configuration.path().equals(Path.of(System.getProperty("fixture.config"))),
                                "the intended external file is selected before initialization");
                        require(configuration.string("clientName").equals(System.getProperty("fixture.name")),
                                "branding comes from the selected file");
                        require(configuration.string("connectHost").equals(System.getProperty("fixture.host")),
                                "the endpoint comes from the selected file");
                        require(configuration.integer("connectPort", 1, 65535) == Integer.parseInt(System.getProperty("fixture.port")),
                                "the port comes from the selected file");
                        require(Boolean.toString(configuration.bool("graphicsD3d")).equals(System.getProperty("sun.java2d.d3d")),
                                "D3D configuration precedes initialization");
                        require(Boolean.toString(configuration.bool("graphicsOpenGl")).equals(System.getProperty("sun.java2d.opengl")),
                                "OpenGL configuration precedes initialization");
                        require(Boolean.toString(configuration.bool("graphicsNoDraw")).equals(System.getProperty("sun.java2d.noddraw")),
                                "DirectDraw configuration precedes initialization");
                    }

                    public static void main(String[] args) throws Exception {
                        require(args.length == 0, "the client receives no startup arguments");
                        ClientConfiguration configuration = ClientConfiguration.get();
                        String host = configuration.string("connectHost");
                        String settingsFile = configuration.string("settingsFile");
                        boolean rsaEnabled = configuration.bool("rsaEnabled");
                        var modulus = rsaEnabled ? configuration.rsaModulus() : null;
                        var exponent = rsaEnabled ? configuration.rsaExponent() : null;
                        ClientSettings settings = ClientSettings.load();
                        boolean originalColors = settings.coloredGameMessages;
                        settings.coloredGameMessages = !originalColors;
                        settings.save();
                        require(Files.isRegularFile(configuration.path()), "saving materializes the selected root file");
                        ClientConfiguration saved = ClientConfiguration.load(configuration.path());
                        require(saved.string("connectHost").equals(host) && saved.string("settingsFile").equals(settingsFile),
                                "saving preferences preserves the endpoint and selected filename");
                        require(saved.bool("rsaEnabled") == rsaEnabled && (!rsaEnabled ||
                                saved.rsaModulus().equals(modulus) && saved.rsaExponent().equals(exponent)),
                                "saving preferences preserves the public login key");
                        require(ClientSettings.load().coloredGameMessages != originalColors,
                                "preferences reload from the selected root file");
                        System.out.println("Client launcher fixture passed");
                    }

                    private static void require(boolean condition, String message) {
                        if (!condition) throw new AssertionError(message);
                    }
                }
                """, StandardCharsets.UTF_8);
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        check(compiler != null, "launcher verification runs with a JDK");
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        int exit = compiler.run(null, output, output, "-encoding", "UTF-8", "-classpath",
                System.getProperty("java.class.path"), "-d", fixture.toString(), source.toString());
        check(exit == 0, "the isolated client fixture compiles: " + output.toString(StandardCharsets.UTF_8));
    }

    private static void packagedLauncher(Path fixture, Path working, Path fakeClient, Path archive,
                                         String expectedSettingsFile, boolean distribution) throws Exception {
        String name = archive.getFileName() + (distribution ? "-distribution" : "-standalone");
        Path root = Files.createDirectories(fixture.resolve(name));
        Path jarPath = (distribution ? Files.createDirectories(root.resolve("lib")) : root).resolve(archive.getFileName());
        Files.copy(archive.toAbsolutePath(), jarPath, StandardCopyOption.REPLACE_EXISTING);
        String mainClass;
        Properties bundled = new Properties();
        try (JarFile jar = new JarFile(jarPath.toFile())) {
            check(jar.getManifest() != null, "the packaged client has a manifest");
            mainClass = jar.getManifest().getMainAttributes().getValue("Main-Class");
            check("ClientLauncher".equals(mainClass), "both executables use the same launcher");
            check(jar.getJarEntry(mainClass + ".class") != null, "the manifest launcher is packaged");
            check(jar.getJarEntry("luna/ClientConfiguration.class") != null, "the file loader is packaged");
            check(jar.getJarEntry("BetaClientLauncher.class") == null, "no alternate runtime launcher is packaged");
            check(jar.getJarEntry("client.properties") != null && jar.getJarEntry("client (2).properties") == null,
                    "each executable has exactly the common settings resource");
            try (var input = new java.io.InputStreamReader(jar.getInputStream(jar.getJarEntry("client.properties")),
                    StandardCharsets.UTF_8)) {
                bundled.load(input);
            }
        }
        check(expectedSettingsFile.equals(bundled.getProperty("settingsFile")),
                "the packaged settings choose the intended root preferences file");
        Path external = root.resolve(expectedSettingsFile);
        String classpath = fakeClient + File.pathSeparator + jarPath;
        Properties initial = bundled;
        if (distribution) {
            Files.createDirectory(root.resolve("bin"));
            writeSettings(root);
            initial = profile("Distribution root fixture", "distribution.invalid", 4445, false, true, false);
            initial.setProperty("settingsFile", expectedSettingsFile);
            ClientConfigurationFixture.write(external, initial);
        }
        launch(fixture, working, name + "-initial", classpath, mainClass,
                external, initial, null, false);
        check(Files.isRegularFile(external), "preferences are saved to the root file selected by packaged defaults");
        Properties override = profile("External override fixture", "override.invalid", 4444, true, false, false);
        override.setProperty("settingsFile", expectedSettingsFile);
        ClientConfigurationFixture.write(external, override);
        launch(fixture, working, name + "-external", classpath, mainClass,
                external, override, null, false);
    }

    private static void launch(Path fixture, Path working, String name, String classpath, String mainClass,
                               Path expectedFile, String expectedError, boolean supplyArguments) throws Exception {
        Properties expected = expectedFile == null ? null : ClientConfigurationFixture.read(expectedFile);
        launch(fixture, working, name, classpath, mainClass, expectedFile, expected, expectedError, supplyArguments);
    }

    private static void launch(Path fixture, Path working, String name, String classpath, String mainClass,
                               Path expectedFile, Properties expected, String expectedError,
                               boolean supplyArguments) throws Exception {
        String executable = System.getProperty("os.name").startsWith("Windows") ? "java.exe" : "java";
        List<String> command = new ArrayList<>(List.of(
                Path.of(System.getProperty("java.home"), "bin", executable).toString(),
                "-Xmx64m", "-Djava.awt.headless=true",
                "-Dsun.java2d.d3d=unconfigured", "-Dsun.java2d.opengl=unconfigured", "-Dsun.java2d.noddraw=unconfigured"));
        if (expectedFile != null) {
            command.add("-Dfixture.config=" + expectedFile);
            command.add("-Dfixture.name=" + expected.getProperty("clientName"));
            command.add("-Dfixture.host=" + expected.getProperty("connectHost"));
            command.add("-Dfixture.port=" + expected.getProperty("connectPort"));
        }
        command.addAll(List.of("-cp", classpath, mainClass));
        if (supplyArguments) command.add("--unsupported-setting=true");
        Path log = fixture.resolve(name + ".log");
        Process child = new ProcessBuilder(command).directory(working.toFile())
                .redirectErrorStream(true).redirectOutput(log.toFile()).start();
        boolean completed;
        try {
            completed = child.waitFor(15, TimeUnit.SECONDS);
        } finally {
            if (child.isAlive()) {
                child.destroyForcibly();
                check(child.waitFor(5, TimeUnit.SECONDS), "the launcher fixture process stops after its deadline");
            }
        }
        String output;
        try (InputStream input = Files.newInputStream(log)) {
            output = new String(input.readNBytes(8192), StandardCharsets.UTF_8);
        }
        check(completed, name + " completes within its deadline: " + output);
        if (expectedError == null) {
            check(child.exitValue() == 0 && output.contains(SUCCESS), name + " initializes safely: " + output);
        } else {
            check(child.exitValue() != 0 && !output.contains(SUCCESS) && output.contains(expectedError),
                    name + " rejects invalid external settings before client initialization: " + output);
        }
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
