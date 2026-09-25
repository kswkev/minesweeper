package com.kswkev.minesweeper.model;

import java.util.Optional;
import java.util.prefs.Preferences;

/** Fastest winning time per preset difficulty, persisted in a {@link Preferences} node. */
public final class BestTimes {

    public record Entry(int seconds, String name) {
    }

    private static final int NONE = -1;

    private final Preferences prefs;

    public BestTimes(Preferences prefs) {
        this.prefs = prefs;
    }

    public Optional<Entry> get(Difficulty difficulty) {
        if (difficulty == Difficulty.CUSTOM) {
            return Optional.empty();
        }
        try {
            int seconds = prefs.getInt(timeKey(difficulty), NONE);
            if (seconds == NONE) {
                return Optional.empty();
            }
            return Optional.of(new Entry(seconds, prefs.get(nameKey(difficulty), "")));
        } catch (RuntimeException e) {
            return Optional.empty();
        }
    }

    /** True if a win in {@code seconds} beats the stored time. Custom boards never set records. */
    public boolean isRecord(Difficulty difficulty, int seconds) {
        if (difficulty == Difficulty.CUSTOM) {
            return false;
        }
        return get(difficulty).map(best -> seconds < best.seconds()).orElse(true);
    }

    public void record(Difficulty difficulty, int seconds, String name) {
        if (difficulty == Difficulty.CUSTOM) {
            throw new IllegalArgumentException("Custom boards have no best times");
        }
        try {
            prefs.putInt(timeKey(difficulty), seconds);
            prefs.put(nameKey(difficulty), name);
        } catch (RuntimeException ignored) {
            // Preferences unavailable: the record is simply not kept.
        }
    }

    public void reset() {
        for (Difficulty difficulty : Difficulty.values()) {
            if (difficulty != Difficulty.CUSTOM) {
                try {
                    prefs.remove(timeKey(difficulty));
                    prefs.remove(nameKey(difficulty));
                } catch (RuntimeException ignored) {
                    // Nothing stored, or preferences unavailable.
                }
            }
        }
    }

    private static String timeKey(Difficulty difficulty) {
        return "best." + difficulty.name() + ".time";
    }

    private static String nameKey(Difficulty difficulty) {
        return "best." + difficulty.name() + ".name";
    }
}
