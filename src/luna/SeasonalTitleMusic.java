package luna;

import sign.MusicController;

import java.time.Clock;
import java.time.LocalDate;
import java.time.Month;

/**
 * Selects cached title music from the computer's calendar, including Easter's changing month.
 *
 * @author lare96
 */
public final class SeasonalTitleMusic {

    private final MusicController music;
    private final Clock clock;
    private LocalDate checkedDate;
    private int selectedSong = -1;

    public SeasonalTitleMusic(MusicController music) {
        this(music, Clock.systemDefaultZone());
    }

    SeasonalTitleMusic(MusicController music, Clock clock) {
        this.music = music;
        this.clock = clock;
    }

    /**
     * Updates title music when the date changes without restarting the same seasonal track.
     */
    public void update() {
        LocalDate today = LocalDate.now(clock);
        if (today.equals(checkedDate)) {
            return;
        }
        int song = songFor(today);
        if (song != selectedSong) {
            music.setSong(song);
            selectedSong = song;
        }
        checkedDate = today;
    }

    /**
     * Clears title selection so returning from the game requests the current title track again.
     */
    public void reset() {
        checkedDate = null;
        selectedSong = -1;
    }

    /**
     * Resolves the original 377 MIDI IDs, with track zero as the ordinary title theme.
     */
    static int songFor(LocalDate date) {
        if (date.getMonth() == Month.OCTOBER) {
            return 321; // Scape Scared.
        }
        if (date.getMonth() == Month.DECEMBER) {
            return 547; // Scape Santa.
        }
        if (date.getMonth() == easterSunday(date.getYear()).getMonth()) {
            return 603; // Funny Bunnies.
        }
        return 0;
    }

    /**
     * Calculates Gregorian Easter Sunday using the Meeus/Jones/Butcher algorithm.
     */
    static LocalDate easterSunday(int year) {
        int a = year % 19;
        int b = year / 100;
        int c = year % 100;
        int d = b / 4;
        int e = b % 4;
        int f = (b + 8) / 25;
        int g = (b - f + 1) / 3;
        int h = (19 * a + b - d - g + 15) % 30;
        int i = c / 4;
        int k = c % 4;
        int l = (32 + 2 * e + 2 * i - h - k) % 7;
        int m = (a + 11 * h + 22 * l) / 451;
        int value = h + l - 7 * m + 114;
        return LocalDate.of(year, value / 31, value % 31 + 1);
    }
}
