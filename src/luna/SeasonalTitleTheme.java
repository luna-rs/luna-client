package luna;

import java.time.LocalDate;
import java.time.Month;

/**
 * Shares the computer-calendar selection used by title artwork and title music.
 *
 * @author lare96
 */
public enum SeasonalTitleTheme {
    DEFAULT(null, 0),
    EASTER("easter", 603),
    HALLOWEEN("halloween", 321),
    CHRISTMAS("christmas", 547);

    private final String directory;
    private final int song;

    SeasonalTitleTheme(String directory, int song) {
        this.directory = directory;
        this.song = song;
    }

    public String directory() {
        return directory;
    }

    public int song() {
        return song;
    }

    /**
     * Selects all of October, December, or the month containing Western Easter that year.
     */
    public static SeasonalTitleTheme forDate(LocalDate date) {
        if (date.getMonth() == Month.OCTOBER) return HALLOWEEN;
        if (date.getMonth() == Month.DECEMBER) return CHRISTMAS;
        if (date.getMonth() == easterSunday(date.getYear()).getMonth()) return EASTER;
        return DEFAULT;
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
