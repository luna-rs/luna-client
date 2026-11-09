package luna;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * Loads bundled defaults and the corresponding root settings before client services initialize.
 *
 * @author lare96
 */
public final class ClientConfiguration {
    private static final Set<String> PREFERENCES = Set.of("clientSize", "width", "height", "coloredGameMessages",
            "targetFps", "renderThreads", "drawDistance", "showFps", "rememberUsername", "lastUsername", "loginMusicEnabled");
    private static String selectedFile;
    private static ClientConfiguration current;

    private final Path path;
    private final Properties values;
    private final List<String> template;

    private ClientConfiguration(Path path, Properties values, String contents) {
        this.path = path;
        this.values = values;
        this.template = contents.lines().toList();
        values.putIfAbsent("rememberUsername", "false");
        values.putIfAbsent("lastUsername", "");
        values.putIfAbsent("loginMusicEnabled", "true");
        values.putIfAbsent("customPacketsEnabled", "false");
        String name = values.getProperty("clientName", "").trim();
        if ("luna-rs #377 (beta)".equalsIgnoreCase(name) || "Luna Beta".equalsIgnoreCase(name)) {
            values.setProperty("clientName", "luna-rs #377");
        }
        validate();
    }

    /**
     * Chooses a complete profile before the first configuration consumer initializes.
     */
    public static synchronized void selectFile(String filename) {
        if (current != null) throw new IllegalStateException("Client configuration is already loaded.");
        selectedFile = filename;
    }

    /**
     * Returns the selected root file independently of startup arguments and system-property overrides.
     */
    public static synchronized ClientConfiguration get() {
        if (current == null) {
            if (selectedFile != null) {
                Path selected = Path.of(selectedFile);
                if (!selected.isAbsolute()) selected = rootDirectory().resolve(selected);
                current = load(selected);
            } else {
                try (InputStream bundled = ClientConfiguration.class.getClassLoader().getResourceAsStream("client.properties")) {
                    if (bundled == null) {
                        current = load(rootDirectory().resolve("client.properties"));
                    } else {
                        String contents = new String(bundled.readAllBytes(), StandardCharsets.UTF_8);
                        Properties defaults = parse(contents);
                        String filename = defaults.getProperty("settingsFile");
                        validateFilename(filename);
                        Path external = rootDirectory().resolve(filename).toAbsolutePath().normalize();
                        current = Files.exists(external) ? load(external)
                                : new ClientConfiguration(external, defaults, contents);
                    }
                } catch (IOException exception) {
                    throw new IllegalArgumentException("Unable to read the bundled client.properties.", exception);
                }
            }
        }
        return current;
    }

    /**
     * Reads a complete configuration without changing the running client's selected profile.
     */
    public static ClientConfiguration load(Path file) {
        Path path = file.toAbsolutePath().normalize();
        String contents;
        Properties values;
        try {
            contents = Files.readString(path, StandardCharsets.UTF_8);
            values = parse(contents);
        } catch (IOException | IllegalArgumentException exception) {
            throw new IllegalArgumentException("Unable to read client configuration " + path
                    + ". Restore the properties file in the Luna client root folder.", exception);
        }
        return new ClientConfiguration(path, values, contents);
    }

    private static Properties parse(String contents) throws IOException {
        Properties values = new Properties();
        if (contents.startsWith("\uFEFF")) contents = contents.substring(1);
        try (Reader reader = new StringReader(contents)) {
            values.load(reader);
        }
        return values;
    }

    private static void validateFilename(String filename) {
        if (filename == null || !filename.matches("[A-Za-z0-9][A-Za-z0-9 ._()-]*\\.properties"))
            throw new IllegalArgumentException("Client setting 'settingsFile' must be a properties filename in the client root.");
    }

    /**
     * Finds source, build/libs, or distribution roots from the loaded executable rather than the working directory.
     */
    public static Path rootDirectory() {
        try {
            Path location = Path.of(ClientConfiguration.class.getProtectionDomain().getCodeSource().getLocation().toURI())
                    .toAbsolutePath().normalize();
            if (Files.isDirectory(location)) {
                for (Path directory = location; directory != null; directory = directory.getParent()) {
                    if (Files.isRegularFile(directory.resolve("build.gradle"))) return directory;
                }
                for (Path directory = location; directory != null; directory = directory.getParent()) {
                    if (Files.isRegularFile(directory.resolve("client.properties"))) return directory;
                }
                return location;
            }
            Path start = location.getParent();
            Path parent = start.getParent();
            if (start.getFileName() != null && start.getFileName().toString().equals("libs")
                    && parent != null && parent.getFileName() != null && parent.getFileName().toString().equals("build")
                    && parent.getParent() != null && Files.isRegularFile(parent.getParent().resolve("build.gradle")))
                return parent.getParent();
            if (start.getFileName() != null && start.getFileName().toString().equals("lib")
                    && parent != null && Files.isDirectory(parent.resolve("bin"))) return parent;
            return start;
        } catch (URISyntaxException | SecurityException exception) {
            throw new IllegalStateException("Unable to locate the Luna client root directory.", exception);
        }
    }

    public Path path() {
        return path;
    }

    public String string(String key) {
        String value = values.getProperty(key);
        if (value == null || value.isBlank()) throw invalid(key, "is required");
        return value.trim();
    }

    /**
     * Reads optional text without discarding spaces or rejecting an empty saved preference.
     */
    public String optionalString(String key, String fallback) {
        return values.getProperty(key, fallback);
    }

    public boolean bool(String key) {
        String value = string(key);
        if (value.equalsIgnoreCase("true")) return true;
        if (value.equalsIgnoreCase("false")) return false;
        throw invalid(key, "must be true or false");
    }

    public int integer(String key, int minimum, int maximum) {
        try {
            int value = Integer.parseInt(string(key));
            if (value >= minimum && value <= maximum) return value;
        } catch (NumberFormatException exception) {
            // The error names the setting without echoing its contents.
        }
        throw invalid(key, "must be between " + minimum + " and " + maximum);
    }

    public BigInteger rsaModulus() {
        return publicInteger("rsaModulus");
    }

    public BigInteger rsaExponent() {
        return publicInteger("rsaExponent");
    }

    /**
     * Expands the operating-system home directory while keeping settings outside the game cache.
     */
    public Path cacheDirectory() {
        String configured = string("cacheDirectory").replace("${user.home}", System.getProperty("user.home"));
        try {
            Path cache = Path.of(configured);
            if (!cache.isAbsolute()) cache = path.getParent().resolve(cache);
            cache = cache.toAbsolutePath().normalize();
            if (cache.getParent() == null || !cache.endsWith(Path.of("luna", "cache")))
                throw invalid("cacheDirectory", "must end in luna/cache for the verified cache installer");
            return cache;
        } catch (InvalidPathException exception) {
            throw invalid("cacheDirectory", "must contain a valid filesystem path ending in luna/cache");
        }
    }

    /**
     * Updates only mutable preferences, preserving connection keys, RSA values, comments and unrelated entries.
     */
    public synchronized void savePreferences(Map<String, String> preferences) throws IOException {
        if (!PREFERENCES.containsAll(preferences.keySet()))
            throw new IllegalArgumentException("Only client preferences may be saved automatically.");
        List<String> lines = Files.exists(path) ? Files.readAllLines(path, StandardCharsets.UTF_8) : template;
        List<String> updated = new ArrayList<>();
        Set<String> remaining = new HashSet<>(preferences.keySet());
        for (int index = 0; index < lines.size(); index++) {
            String line = lines.get(index);
            int first = index;
            boolean comment = line.stripLeading().startsWith("#") || line.stripLeading().startsWith("!");
            while (!comment && continued(line) && index + 1 < lines.size()) line = lines.get(++index);
            List<String> block = lines.subList(first, index + 1);
            Properties property = parse(String.join("\n", block));
            String key = property.size() == 1 ? property.stringPropertyNames().iterator().next() : null;
            if (key != null && preferences.containsKey(key)) {
                if (remaining.remove(key)) updated.add(preferenceLine(key, preferences.get(key)));
            } else {
                updated.addAll(block);
            }
        }
        remaining.stream().sorted().forEach(key -> updated.add(preferenceLine(key, preferences.get(key))));
        Path temporary = Files.createTempFile(path.getParent(), ".client-settings-", ".properties");
        try {
            Files.write(temporary, updated, StandardCharsets.UTF_8);
            load(temporary);
            try {
                Files.move(temporary, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException exception) {
                Files.move(temporary, path, StandardCopyOption.REPLACE_EXISTING);
            }
            values.putAll(preferences);
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    private static String preferenceLine(String key, String value) {
        StringBuilder output = new StringBuilder(key).append('=');
        for (int index = 0; index < value.length(); index++) {
            char character = value.charAt(index);
            switch (character) {
                case '\\' -> output.append("\\\\");
                case '\n' -> output.append("\\n");
                case '\r' -> output.append("\\r");
                case '\t' -> output.append("\\t");
                case '\f' -> output.append("\\f");
                case '=', ':', '#', '!' -> output.append('\\').append(character);
                case ' ' -> output.append(index == 0 ? "\\ " : " ");
                default -> output.append(character);
            }
        }
        return output.toString();
    }

    private static boolean continued(String line) {
        int backslashes = 0;
        for (int index = line.length() - 1; index >= 0 && line.charAt(index) == '\\'; index--) backslashes++;
        return backslashes % 2 != 0;
    }

    private BigInteger publicInteger(String key) {
        try {
            BigInteger value = new BigInteger(string(key));
            if (value.compareTo(BigInteger.ONE) > 0) return value;
        } catch (NumberFormatException exception) {
            // RSA contents are never included in diagnostic messages.
        }
        throw invalid(key, "must contain the server's positive public RSA value");
    }

    private IllegalArgumentException invalid(String key, String requirement) {
        return new IllegalArgumentException("Invalid client setting '" + key + "' in " + path + ": " + requirement);
    }

    private void validate() {
        try {
            validateFilename(string("settingsFile"));
        } catch (IllegalArgumentException exception) {
            throw invalid("settingsFile", "must be a properties filename in the client root");
        }
        string("clientName");
        string("connectHost");
        bool("rememberUsername");
        bool("loginMusicEnabled");
        bool("customPacketsEnabled");
        if (!LoginPreferences.validUsername(optionalString("lastUsername", "")))
            throw invalid("lastUsername", "must contain at most twelve valid login characters");
        for (String key : List.of("connectPort", "jaggrabPort", "httpPort")) integer(key, 1, 65535);
        int offset = integer("portOffset", 0, 65534);
        if ((long) integer("connectPort", 1, 65535) + offset > 65535
                || (long) integer("httpPort", 1, 65535) + offset > 65535)
            throw invalid("portOffset", "must keep effective ports at or below 65535");
        integer("world", 0, 65535);
        integer("cacheStoreId", 0, 255);
        for (String key : List.of("members", "lowMemory", "debug", "rsaEnabled", "coloredGameMessages", "showFps",
                "graphicsD3d", "graphicsOpenGl", "graphicsNoDraw")) bool(key);
        if (bool("rsaEnabled")) {
            BigInteger modulus = rsaModulus();
            BigInteger exponent = rsaExponent();
            if (!modulus.testBit(0) || !exponent.testBit(0) || exponent.compareTo(modulus) >= 0)
                throw invalid("rsaModulus/rsaExponent", "must be an odd public modulus and an odd exponent below it");
        }
        cacheDirectory();
        integer("clientSize", 0, 1);
        for (String key : List.of("width", "defaultWidth")) integer(key, 765, 32768);
        for (String key : List.of("height", "defaultHeight")) integer(key, 503, 32768);
        integer("targetFps", 1, 1000);
        integer("renderThreads", 0, 4);
        integer("drawDistance", 0, 27);
        integer("brightness", 1, 4);
        integer("musicVolume", 0, 4);
        integer("soundVolume", 0, 4);
        for (String key : List.of("mouseButtons", "chatEffects", "splitPrivateChat", "bankInsertMode")) integer(key, 0, 1);
        integer("publicChatMode", 0, 3);
        integer("privateChatMode", 0, 2);
        integer("tradeChatMode", 0, 2);
    }
}
