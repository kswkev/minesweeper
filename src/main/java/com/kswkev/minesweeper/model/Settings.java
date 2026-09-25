package com.kswkev.minesweeper.model;

import java.util.prefs.Preferences;

/**
 * Player settings remembered between launches. Any problem reading or writing the
 * {@link Preferences} store falls back to the defaults so the game always starts.
 */
public final class Settings {

    private static final String DIFFICULTY = "difficulty";
    private static final String CUSTOM_ROWS = "custom.rows";
    private static final String CUSTOM_COLS = "custom.cols";
    private static final String CUSTOM_MINES = "custom.mines";
    private static final String QUESTION_MARKS = "questionMarks";
    private static final String LAST_NAME = "lastName";

    private static final BoardConfig DEFAULT_CUSTOM = Difficulty.BEGINNER.getPreset().orElseThrow();
    private static final String DEFAULT_NAME = "Anonymous";

    private final Preferences prefs;

    public Settings(Preferences prefs) {
        this.prefs = prefs;
    }

    public Difficulty getDifficulty() {
        try {
            return Difficulty.valueOf(prefs.get(DIFFICULTY, Difficulty.BEGINNER.name()));
        } catch (RuntimeException e) {
            return Difficulty.BEGINNER;
        }
    }

    public void setDifficulty(Difficulty difficulty) {
        put(() -> prefs.put(DIFFICULTY, difficulty.name()));
    }

    public BoardConfig getCustomConfig() {
        try {
            return new BoardConfig(
                    prefs.getInt(CUSTOM_ROWS, DEFAULT_CUSTOM.rows()),
                    prefs.getInt(CUSTOM_COLS, DEFAULT_CUSTOM.cols()),
                    prefs.getInt(CUSTOM_MINES, DEFAULT_CUSTOM.mines()));
        } catch (RuntimeException e) {
            return DEFAULT_CUSTOM;
        }
    }

    public void setCustomConfig(BoardConfig config) {
        put(() -> {
            prefs.putInt(CUSTOM_ROWS, config.rows());
            prefs.putInt(CUSTOM_COLS, config.cols());
            prefs.putInt(CUSTOM_MINES, config.mines());
        });
    }

    /** The board for the remembered difficulty. */
    public BoardConfig getBoardConfig() {
        return getDifficulty().getPreset().orElseGet(this::getCustomConfig);
    }

    public boolean isQuestionMarksEnabled() {
        try {
            return prefs.getBoolean(QUESTION_MARKS, true);
        } catch (RuntimeException e) {
            return true;
        }
    }

    public void setQuestionMarksEnabled(boolean enabled) {
        put(() -> prefs.putBoolean(QUESTION_MARKS, enabled));
    }

    /** The name last entered for a best time, used as the default next time. */
    public String getLastName() {
        try {
            return prefs.get(LAST_NAME, DEFAULT_NAME);
        } catch (RuntimeException e) {
            return DEFAULT_NAME;
        }
    }

    public void setLastName(String name) {
        put(() -> prefs.put(LAST_NAME, name));
    }

    private static void put(Runnable write) {
        try {
            write.run();
        } catch (RuntimeException ignored) {
            // Preferences unavailable: the setting just isn't remembered.
        }
    }
}
