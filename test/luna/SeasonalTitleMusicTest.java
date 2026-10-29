package luna;

import sign.MusicController;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Exercises seasonal dates, title sessions and music cancellation without audio hardware.
 *
 * @author lare96
 */
public final class SeasonalTitleMusicTest {

    public static void main(String[] args) {
        easterDates();
        completeMonths();
        dateChanges();
        titleSessions();
        mutedTitle();
        localCalendar();
        System.out.println("SeasonalTitleMusicTest passed");
    }

    private static void easterDates() {
        String[] expected = {"1818-03-22", "1900-04-15", "1943-04-25", "2000-04-23",
                "2008-03-23", "2024-03-31", "2025-04-20", "2026-04-05", "2027-03-28",
                "2028-04-16", "2038-04-25", "2100-03-28"};
        for (String value : expected) {
            LocalDate date = LocalDate.parse(value);
            check(SeasonalTitleMusic.easterSunday(date.getYear()).equals(date), "Easter date " + value);
        }
        for (int year = 1900; year <= 2200; year++) {
            LocalDate date = SeasonalTitleMusic.easterSunday(year);
            check(date.getDayOfWeek() == DayOfWeek.SUNDAY, "Easter must be a Sunday in " + year);
            check(!date.isBefore(LocalDate.of(year, 3, 22)) && !date.isAfter(LocalDate.of(year, 4, 25)),
                    "Easter must remain in its Gregorian date range in " + year);
        }
    }

    private static void completeMonths() {
        for (int year : new int[]{2024, 2025, 2026, 2027}) {
            for (LocalDate date = LocalDate.of(year, 1, 1); date.getYear() == year; date = date.plusDays(1)) {
                int expected = 0;
                if (date.getMonthValue() == 10) expected = 321;
                if (date.getMonthValue() == 12) expected = 547;
                int easterMonth = year == 2024 || year == 2027 ? 3 : 4;
                if (date.getMonthValue() == easterMonth) expected = 603;
                check(SeasonalTitleMusic.songFor(date) == expected, "seasonal selection on " + date);
            }
        }
    }

    private static void dateChanges() {
        Fixture fixture = new Fixture("2026-09-30T23:59:59Z", ZoneOffset.UTC);
        fixture.title.update();
        check(fixture.requests.equals(List.of(0)), "ordinary title starts with track zero");
        fixture.clock.set("2026-10-01T00:00:00Z");
        fixture.title.update();
        check(fixture.requests.equals(List.of(0, 321)), "October starts Halloween title music");
        fixture.title.update();
        fixture.clock.set("2026-10-02T00:00:00Z");
        fixture.title.update();
        check(fixture.requests.size() == 2, "title must not restart every frame or day");
        fixture.clock.set("2026-11-01T00:00:00Z");
        fixture.title.update();
        fixture.clock.set("2026-12-01T00:00:00Z");
        fixture.title.update();
        fixture.clock.set("2027-01-01T00:00:00Z");
        fixture.title.update();
        fixture.clock.set("2027-03-01T00:00:00Z");
        fixture.title.update();
        fixture.clock.set("2027-04-01T00:00:00Z");
        fixture.title.update();
        check(fixture.requests.equals(List.of(0, 321, 0, 547, 0, 603, 0)),
                "title follows month boundaries and recalculates Easter for the new year");
        fixture.clock.set("2027-03-31T23:59:59Z");
        fixture.title.update();
        check(fixture.lastRequest() == 603, "moving the clock backwards updates the title too");
    }

    private static void titleSessions() {
        Fixture fixture = new Fixture("2026-10-29T12:00:00Z", ZoneOffset.UTC);
        fixture.title.update();
        fixture.title.reset();
        fixture.music.reset();
        fixture.music.setSong(99);
        fixture.music.accept(321, new byte[1]);
        check(fixture.played.isEmpty(), "late title download cannot replace game music");
        fixture.music.accept(99, new byte[1]);
        check(fixture.played.equals(List.of(true)), "game background plays normally");
        fixture.title.reset();
        fixture.music.reset();
        fixture.title.update();
        check(fixture.requests.equals(List.of(321, 99, 321)), "logout resumes the title on the same date");
        fixture.music.accept(99, new byte[1]);
        check(fixture.played.size() == 1, "late world download cannot replace the title");
        fixture.music.accept(321, new byte[1]);
        check(fixture.played.equals(List.of(true, true)), "seasonal title music loops");
    }

    private static void mutedTitle() {
        Fixture fixture = new Fixture("2026-12-25T12:00:00Z", ZoneOffset.UTC);
        fixture.music.setEnabled(false);
        fixture.title.update();
        check(fixture.requests.isEmpty(), "muted or low-memory title must not request audio");
        fixture.music.setEnabled(true);
        check(fixture.requests.equals(List.of(547)), "unmuting resumes the selected title song");
        fixture.music.setEnabled(false);
        fixture.music.accept(547, new byte[1]);
        check(fixture.played.isEmpty(), "muting cancels a pending seasonal download");
    }

    private static void localCalendar() {
        Fixture fixture = new Fixture("2026-10-01T01:00:00Z", ZoneId.of("America/Toronto"));
        fixture.title.update();
        check(fixture.lastRequest() == 0, "selection uses the local September date, not UTC October");
        fixture.clock.set("2026-10-01T04:00:00Z");
        fixture.title.update();
        check(fixture.lastRequest() == 321, "local midnight starts seasonal title music");
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    /**
     * Records requests and playback while allowing the calendar to cross event boundaries.
     *
     * @author lare96
     */
    private static final class Fixture {
        final List<Integer> requests = new ArrayList<>();
        final List<Boolean> played = new ArrayList<>();
        final MutableClock clock;
        final MusicController music = new MusicController(id -> {
            requests.add(id);
            return true;
        }, (data, loop, started) -> {
            played.add(loop);
            started.accept(1000);
        }, () -> {}, () -> {});
        final SeasonalTitleMusic title;

        Fixture(String instant, ZoneId zone) {
            clock = new MutableClock(new AtomicReference<>(Instant.parse(instant)), zone);
            title = new SeasonalTitleMusic(music, clock);
        }

        int lastRequest() {
            return requests.get(requests.size() - 1);
        }
    }

    /**
     * Supplies deterministic calendar changes without waiting or changing the computer's clock.
     *
     * @author lare96
     */
    private static final class MutableClock extends Clock {
        private final AtomicReference<Instant> time;
        private final ZoneId zone;

        MutableClock(AtomicReference<Instant> time, ZoneId zone) {
            this.time = time;
            this.zone = zone;
        }

        void set(String instant) {
            time.set(Instant.parse(instant));
        }

        @Override
        public ZoneId getZone() {
            return zone;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return new MutableClock(time, zone);
        }

        @Override
        public Instant instant() {
            return time.get();
        }
    }
}
