package luna;

import sign.MusicController;

import java.time.Clock;
import java.time.LocalDate;

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
        return SeasonalTitleTheme.forDate(date).song();
    }

    /**
     * Calculates Gregorian Easter Sunday using the Meeus/Jones/Butcher algorithm.
     */
    static LocalDate easterSunday(int year) {
        return SeasonalTitleTheme.easterSunday(year);
    }
}
