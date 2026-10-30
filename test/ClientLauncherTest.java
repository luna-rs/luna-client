import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.jar.JarFile;
import java.util.stream.Stream;

/**
 * Verifies launcher configuration before client initialization in fresh JVMs without opening graphics or audio.
 *
 * @author lare96
 */
public final class ClientLauncherTest {

    private static final String SUCCESS = "Client launcher fixture passed";
    private static final List<String> CLIENT_ARGS = List.of("alpha", "argument with spaces", "", "--fixture=true");

    public static void main(String[] args) throws Exception {
        check(args.length == 0 || args.length == 2,
                "supply both development and beta JAR paths, or neither");
        Path fixture = Files.createTempDirectory(Path.of("").toAbsolutePath(), "client-launcher-test-");
        try {
            compileClientFixture(fixture);
            String classpath = System.getProperty("java.class.path");
            launch(fixture, "development-classes", classpath, "ClientLauncher", false, false);
            launch(fixture, "manual-beta-classes", classpath, "ClientLauncher", true, true);
            launch(fixture, "beta-classes", classpath, "BetaClientLauncher", false, true);
            if (args.length == 2) {
                packagedLauncher(fixture, Path.of(args[0]), "ClientLauncher", false);
                packagedLauncher(fixture, Path.of(args[1]), "BetaClientLauncher", true);
            }
        } finally {
            try (Stream<Path> paths = Files.walk(fixture)) {
                for (Path path : paths.sorted(Comparator.reverseOrder()).toArray(Path[]::new)) Files.delete(path);
            }
        }
        System.out.println("ClientLauncherTest passed");
    }

    private static void compileClientFixture(Path fixture) throws IOException {
        Path source = fixture.resolve("client.java");
        Files.writeString(source, """
                import java.util.Arrays;

                /**
                 * Rejects graphics or profile configuration applied after the client starts initializing.
                 *
                 * @author lare96
                 */
                public final class client {
                    static {
                        require("false".equals(System.getProperty("sun.java2d.d3d")), "D3D is disabled before initialization");
                        require("false".equals(System.getProperty("sun.java2d.opengl")), "OpenGL is disabled before initialization");
                        require("true".equals(System.getProperty("sun.java2d.noddraw")), "DirectDraw is disabled before initialization");
                        require(Boolean.getBoolean("luna.beta") == Boolean.getBoolean("luna.launcher.fixture.expectedBeta"),
                                "the intended beta profile is selected before initialization");
                    }

                    public static void main(String[] args) {
                        require(Arrays.equals(args, new String[] {"alpha", "argument with spaces", "", "--fixture=true"}),
                                "all client arguments are forwarded unchanged");
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
        int exit = compiler.run(null, output, output, "-encoding", "UTF-8", "-d", fixture.toString(), source.toString());
        check(exit == 0, "the isolated client fixture compiles: " + output.toString(StandardCharsets.UTF_8));
    }

    private static void packagedLauncher(Path fixture, Path archive, String expectedMain, boolean expectedBeta)
            throws Exception {
        Path jarPath = archive.toAbsolutePath();
        String mainClass;
        try (JarFile jar = new JarFile(jarPath.toFile())) {
            check(jar.getManifest() != null, "the packaged client has a manifest");
            mainClass = jar.getManifest().getMainAttributes().getValue("Main-Class");
            check(expectedMain.equals(mainClass), "the packaged client selects its intended launcher");
            check(jar.getJarEntry(mainClass + ".class") != null, "the manifest launcher is packaged");
            check(jar.getJarEntry("ClientLauncher.class") != null, "the shared graphics launcher is packaged");
        }
        launch(fixture, expectedBeta ? "beta-jar" : "development-jar", jarPath.toString(), mainClass, false, expectedBeta);
    }

    private static void launch(Path fixture, String name, String classpath, String mainClass,
                               boolean incomingBeta, boolean expectedBeta) throws Exception {
        String executable = System.getProperty("os.name").startsWith("Windows") ? "java.exe" : "java";
        List<String> command = new ArrayList<>(List.of(
                Path.of(System.getProperty("java.home"), "bin", executable).toString(),
                "-Xmx64m", "-Djava.awt.headless=true",
                "-Dsun.java2d.d3d=true", "-Dsun.java2d.opengl=true", "-Dsun.java2d.noddraw=false",
                "-Dluna.beta=" + incomingBeta,
                "-Dluna.launcher.fixture.expectedBeta=" + expectedBeta,
                "-cp", fixture + File.pathSeparator + classpath, mainClass));
        command.addAll(CLIENT_ARGS);
        Path log = fixture.resolve(name + ".log");
        Process child = new ProcessBuilder(command).redirectErrorStream(true).redirectOutput(log.toFile()).start();
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
        check(child.exitValue() == 0 && output.contains(SUCCESS), name + " initializes safely and forwards arguments: " + output);
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
