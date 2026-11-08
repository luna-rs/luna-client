package luna;

import java.io.IOException;
import java.util.Map;

/**
 * Remembers optional login-screen preferences without accepting or persisting a password.
 *
 * @author lare96
 */
public final class LoginPreferences {
    private static final String USERNAME_CHARACTERS =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!\"\u00a3$%^&*()-_=+[{]};:'@#~,<.>/?\\| ";
    private final ClientConfiguration configuration;
    private boolean rememberUsername;
    private String username;
    private boolean loginMusicEnabled;

    public LoginPreferences() {
        this(ClientConfiguration.get());
    }

    public LoginPreferences(ClientConfiguration configuration) {
        this.configuration = configuration;
        rememberUsername = configuration.bool("rememberUsername");
        username = rememberUsername ? configuration.optionalString("lastUsername", "") : "";
        loginMusicEnabled = configuration.bool("loginMusicEnabled");
    }

    public boolean rememberUsername() {
        return rememberUsername;
    }

    public String username() {
        return username;
    }

    public boolean loginMusicEnabled() {
        return loginMusicEnabled;
    }

    /**
     * Enables remembering the current login name, or removes the previously saved name.
     */
    public void rememberUsername(boolean enabled, String currentUsername) {
        String next = enabled ? checkedUsername(currentUsername) : "";
        if (rememberUsername == enabled && username.equals(next)) return;
        rememberUsername = enabled;
        username = next;
        save(Map.of("rememberUsername", Boolean.toString(enabled), "lastUsername", next));
    }

    /**
     * Updates the saved name only while remembering is enabled and its value changes.
     */
    public void username(String currentUsername) {
        if (!rememberUsername) return;
        String next = checkedUsername(currentUsername);
        if (username.equals(next)) return;
        username = next;
        save(Map.of("lastUsername", next));
    }

    public void loginMusicEnabled(boolean enabled) {
        if (loginMusicEnabled == enabled) return;
        loginMusicEnabled = enabled;
        save(Map.of("loginMusicEnabled", Boolean.toString(enabled)));
    }

    static boolean validUsername(String value) {
        if (value == null || value.length() > 12) return false;
        for (int index = 0; index < value.length(); index++) {
            if (USERNAME_CHARACTERS.indexOf(value.charAt(index)) < 0) return false;
        }
        return true;
    }

    private static String checkedUsername(String value) {
        if (!validUsername(value)) throw new IllegalArgumentException("Invalid remembered login name.");
        return value;
    }

    private void save(Map<String, String> preferences) {
        try {
            configuration.savePreferences(preferences);
        } catch (IOException | IllegalArgumentException | SecurityException exception) {
            System.err.println("Unable to save login preferences.");
        }
    }
}
