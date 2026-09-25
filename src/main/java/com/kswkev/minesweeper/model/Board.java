package com.kswkev.minesweeper.model;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Random;

/** Minesweeper game logic. Contains no UI code. */
public final class Board {

    private final int rows;
    private final int cols;
    private final int mines;
    private final Cell[][] cells;

    private GameState state = GameState.PLAYING;
    private int revealedCount;
    private int explodedRow = -1;
    private int explodedCol = -1;

    public Board(int rows, int cols, int mines) {
        this(rows, cols, mines, new Random());
    }

    public Board(int rows, int cols, int mines, Random rng) {
        this(rows, cols, mines, rng, null);
    }

    /** Creates a board with mines at the given {row, col} positions. Intended for tests. */
    static Board withMines(int rows, int cols, int[][] minePositions) {
        return new Board(rows, cols, minePositions.length, null, minePositions);
    }

    private Board(int rows, int cols, int mines, Random rng, int[][] minePositions) {
        if (rows <= 0 || cols <= 0) {
            throw new IllegalArgumentException("Board dimensions must be positive");
        }
        if (mines < 0 || mines >= rows * cols) {
            throw new IllegalArgumentException("Mine count must be between 0 and rows*cols - 1");
        }
        this.rows = rows;
        this.cols = cols;
        this.mines = mines;
        this.cells = new Cell[rows][cols];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                cells[r][c] = new Cell();
            }
        }
        if (minePositions != null) {
            for (int[] pos : minePositions) {
                cells[pos[0]][pos[1]].setMine(true);
            }
        } else {
            placeRandomMines(rng);
        }
        computeAdjacentCounts();
    }

    private void placeRandomMines(Random rng) {
        int placed = 0;
        while (placed < mines) {
            Cell cell = cells[rng.nextInt(rows)][rng.nextInt(cols)];
            if (!cell.isMine()) {
                cell.setMine(true);
                placed++;
            }
        }
    }

    private void computeAdjacentCounts() {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                int count = 0;
                for (int dr = -1; dr <= 1; dr++) {
                    for (int dc = -1; dc <= 1; dc++) {
                        if ((dr != 0 || dc != 0) && inBounds(r + dr, c + dc) && cells[r + dr][c + dc].isMine()) {
                            count++;
                        }
                    }
                }
                cells[r][c].setAdjacentMines(count);
            }
        }
    }

    public void reveal(int row, int col) {
        if (state != GameState.PLAYING) {
            return;
        }
        Cell start = cells[row][col];
        if (start.isRevealed() || start.isFlagged()) {
            return;
        }
        if (start.isMine()) {
            start.setRevealed(true);
            explodedRow = row;
            explodedCol = col;
            state = GameState.LOST;
            return;
        }

        Deque<int[]> queue = new ArrayDeque<>();
        start.setRevealed(true);
        revealedCount++;
        queue.add(new int[] {row, col});
        while (!queue.isEmpty()) {
            int[] pos = queue.poll();
            if (cells[pos[0]][pos[1]].getAdjacentMines() != 0) {
                continue;
            }
            for (int dr = -1; dr <= 1; dr++) {
                for (int dc = -1; dc <= 1; dc++) {
                    int r = pos[0] + dr;
                    int c = pos[1] + dc;
                    if (!inBounds(r, c)) {
                        continue;
                    }
                    Cell neighbour = cells[r][c];
                    if (!neighbour.isRevealed() && !neighbour.isFlagged() && !neighbour.isMine()) {
                        neighbour.setRevealed(true);
                        revealedCount++;
                        queue.add(new int[] {r, c});
                    }
                }
            }
        }

        if (revealedCount == rows * cols - mines) {
            state = GameState.WON;
        }
    }

    public void toggleFlag(int row, int col) {
        if (state != GameState.PLAYING) {
            return;
        }
        Cell cell = cells[row][col];
        if (!cell.isRevealed()) {
            cell.setFlagged(!cell.isFlagged());
        }
    }

    public boolean isExploded(int row, int col) {
        return row == explodedRow && col == explodedCol;
    }

    private boolean inBounds(int r, int c) {
        return r >= 0 && r < rows && c >= 0 && c < cols;
    }

    public Cell getCell(int row, int col) {
        return cells[row][col];
    }

    public GameState getState() {
        return state;
    }

    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
    }

    public int getMines() {
        return mines;
    }
}