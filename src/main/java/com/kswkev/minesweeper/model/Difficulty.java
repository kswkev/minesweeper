package com.kswkev.minesweeper.model;

import java.util.Optional;

/** The selectable difficulty levels. Custom boards have no fixed preset. */
public enum Difficulty {
    BEGINNER("Beginner", new BoardConfig(9, 9, 10)),
    INTERMEDIATE("Intermediate", new BoardConfig(16, 16, 40)),
    EXPERT("Expert", new BoardConfig(16, 30, 99)),
    CUSTOM("Custom", null);

    private final String displayName;
    private final BoardConfig preset;

    Difficulty(String displayName, BoardConfig preset) {
        this.displayName = displayName;
        this.preset = preset;
    }

    public String getDisplayName() {
        return displayName;
    }

    /** The fixed board for this level, or empty for {@link #CUSTOM}. */
    public Optional<BoardConfig> getPreset() {
        return Optional.ofNullable(preset);
    }
}
