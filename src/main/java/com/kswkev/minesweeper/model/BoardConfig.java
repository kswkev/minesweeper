package com.kswkev.minesweeper.model;

/** Board dimensions and mine count for a game. */
public record BoardConfig(int rows, int cols, int mines) {

    public static final int MIN_ROWS = 5;
    public static final int MAX_ROWS = 24;
    public static final int MIN_COLS = 5;
    public static final int MAX_COLS = 30;
    public static final int MIN_MINES = 1;

    public static final BoardConfig BEGINNER = new BoardConfig(9, 9, 10);
    public static final BoardConfig INTERMEDIATE = new BoardConfig(16, 16, 40);
    public static final BoardConfig EXPERT = new BoardConfig(16, 30, 99);

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

    /** Leaves room for a mine-free 3x3 block around the first click. */
    public static int maxMines(int rows, int cols) {
        return rows * cols - 9;
    }
}
