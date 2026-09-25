package com.kswkev.minesweeper.model;

/** Board dimensions and mine count for a game. Presets live on {@link Difficulty}. */
public record BoardConfig(int rows, int cols, int mines) {

    public static final int MIN_ROWS = 5;
    public static final int MAX_ROWS = 24;
    public static final int MIN_COLS = 5;
    public static final int MAX_COLS = 30;
    public static final int MIN_MINES = 1;

    public BoardConfig {
        if (rows < MIN_ROWS || rows > MAX_ROWS) {
            throw new IllegalArgumentException("Rows must be between " + MIN_ROWS + " and " + MAX_ROWS);
        }
        if (cols < MIN_COLS || cols > MAX_COLS) {
            throw new IllegalArgumentException("Columns must be between " + MIN_COLS + " and " + MAX_COLS);
        }
        if (mines < MIN_MINES || mines > maxMines(rows, cols)) {
            throw new IllegalArgumentException(
                    "Mines must be between " + MIN_MINES + " and " + maxMines(rows, cols));
        }
    }

    /** Returns a copy of this config with a different mine count. */
    public BoardConfig withMines(int newMines) {
        return new BoardConfig(rows, cols, newMines);
    }

    /** The most mines this board can hold. */
    public int maxMines() {
        return maxMines(rows, cols);
    }

    /** Leaves room for a mine-free 3x3 block around the first click. */
    public static int maxMines(int rows, int cols) {
        return rows * cols - 9;
    }
}
