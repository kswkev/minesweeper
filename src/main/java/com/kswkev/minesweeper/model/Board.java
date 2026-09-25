package com.kswkev.minesweeper.model;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Random;

/** Minesweeper game logic. Contains no UI code. */
public final class Board {

    /** Cells kept mine-free around the first click need this many safe cells to fit. */
    private static final int SAFE_BLOCK_SIZE = 9;

    private final int rows;
    private final int cols;
    private final int mines;
    private final Cell[][] cells;
    private final Random rng;

    private GameState state = GameState.PLAYING;
    private boolean minesPlaced;
    private boolean started;
    private int revealedCount;
    private int flagCount;
    private int explodedRow = -1;
    private int explodedCol = -1;

    public Board(int rows, int cols, int mines) {
        this(rows, cols, mines, new Random());
    }

    /** Mines are placed on the first reveal so that the first click is always safe. */
    public Board(int rows, int cols, int mines, Random rng) {
        this(rows, cols, mines, rng, null);
    }

    public Board(BoardConfig config) {
        this(config.rows(), config.cols(), config.mines());
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
        this.rng = rng;
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
            computeAdjacentCounts();
            minesPlaced = true;
        }
    }

    /**
     * Places mines at random, avoiding the first-clicked cell and, when there is room, its neighbours.
     */
    private void placeMines(int safeRow, int safeCol) {
        boolean protectNeighbours = rows * cols - mines >= SAFE_BLOCK_SIZE;
        int placed = 0;
        while (placed < mines) {
            int r = rng.nextInt(rows);
            int c = rng.nextInt(cols);
            boolean excluded = protectNeighbours
                    ? Math.abs(r - safeRow) <= 1 && Math.abs(c - safeCol) <= 1
                    : r == safeRow && c == safeCol;
            if (!excluded && !cells[r][c].isMine()) {
                cells[r][c].setMine(true);
                placed++;
            }
        }
        computeAdjacentCounts();
        minesPlaced = true;
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
        if (!minesPlaced) {
            placeMines(row, col);
        }
        started = true;
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

    /**
     * Reveals all unflagged neighbours of a revealed number once the matching number of flags
     * surrounds it. A wrongly placed flag makes this lose the game.
     */
    public void chord(int row, int col) {
        if (state != GameState.PLAYING) {
            return;
        }
        Cell cell = cells[row][col];
        if (!cell.isRevealed() || cell.getAdjacentMines() == 0) {
            return;
        }
        int flagged = 0;
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                if (inBounds(row + dr, col + dc) && cells[row + dr][col + dc].isFlagged()) {
                    flagged++;
                }
            }
        }
        if (flagged != cell.getAdjacentMines()) {
            return;
        }
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                if (inBounds(row + dr, col + dc)) {
                    reveal(row + dr, col + dc);
                }
            }
        }
    }

    public void toggleFlag(int row, int col) {
        if (state != GameState.PLAYING) {
            return;
        }
        Cell cell = cells[row][col];
        if (!cell.isRevealed()) {
            cell.setFlagged(!cell.isFlagged());
            flagCount += cell.isFlagged() ? 1 : -1;
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

    /** True once the first cell has been revealed. */
    public boolean isStarted() {
        return started;
    }

    /** Mines minus flags placed; negative when more flags than mines have been placed. */
    public int getRemainingMines() {
        return mines - flagCount;
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
