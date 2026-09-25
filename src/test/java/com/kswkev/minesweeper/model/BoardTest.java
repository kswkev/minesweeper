package com.kswkev.minesweeper.model;

import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BoardTest {

    @Test
    void adjacentCountsMatchKnownLayout() {
        // M . .
        // . . .
        // . . M
        Board board = Board.withMines(3, 3, new int[][] {{0, 0}, {2, 2}});

        assertEquals(1, board.getCell(0, 1).getAdjacentMines());
        assertEquals(2, board.getCell(1, 1).getAdjacentMines());
        assertEquals(0, board.getCell(0, 2).getAdjacentMines());
        assertEquals(0, board.getCell(2, 0).getAdjacentMines());
        assertEquals(1, board.getCell(2, 1).getAdjacentMines());
    }

    @Test
    void revealingZeroFloodFillsAndStopsAtNumbers() {
        // . . . .
        // . . . .
        // . . 1 1
        // . . 1 M
        Board board = Board.withMines(4, 4, new int[][] {{3, 3}});

        board.reveal(0, 0);

        for (int r = 0; r < 4; r++) {
            for (int c = 0; c < 4; c++) {
                boolean expected = !(r == 3 && c == 3);
                assertEquals(expected, board.getCell(r, c).isRevealed(), "cell " + r + "," + c);
            }
        }
        assertEquals(GameState.WON, board.getState());
    }

    @Test
    void revealingNumberRevealsOnlyThatCell() {
        Board board = Board.withMines(4, 4, new int[][] {{3, 3}});

        board.reveal(2, 2);

        assertTrue(board.getCell(2, 2).isRevealed());
        assertFalse(board.getCell(1, 1).isRevealed());
        assertEquals(GameState.PLAYING, board.getState());
    }

    @Test
    void revealingMineLosesAndFurtherActionsAreIgnored() {
        Board board = Board.withMines(3, 3, new int[][] {{0, 0}});

        board.reveal(0, 0);

        assertEquals(GameState.LOST, board.getState());
        assertTrue(board.isExploded(0, 0));

        board.reveal(2, 2);
        board.toggleFlag(1, 1);
        assertFalse(board.getCell(2, 2).isRevealed());
        assertFalse(board.getCell(1, 1).isFlagged());
        assertEquals(GameState.LOST, board.getState());
    }

    @Test
    void revealingAllSafeCellsWins() {
        // M .
        // . .
        Board board = Board.withMines(2, 2, new int[][] {{0, 0}});

        board.reveal(0, 1);
        board.reveal(1, 0);
        assertEquals(GameState.PLAYING, board.getState());

        board.reveal(1, 1);
        assertEquals(GameState.WON, board.getState());
    }

    @Test
    void flaggedCellCannotBeRevealed() {
        Board board = Board.withMines(3, 3, new int[][] {{0, 0}});

        board.toggleFlag(0, 0);
        board.reveal(0, 0);

        assertTrue(board.getCell(0, 0).isFlagged());
        assertFalse(board.getCell(0, 0).isRevealed());
        assertEquals(GameState.PLAYING, board.getState());

        board.toggleFlag(0, 0);
        assertFalse(board.getCell(0, 0).isFlagged());
    }

    @Test
    void floodFillSkipsFlaggedCells() {
        Board board = Board.withMines(4, 4, new int[][] {{3, 3}});

        board.toggleFlag(0, 3);
        board.reveal(0, 0);

        assertFalse(board.getCell(0, 3).isRevealed());
        assertTrue(board.getCell(0, 3).isFlagged());
        assertEquals(GameState.PLAYING, board.getState());
    }

    @Test
    void revealedCellCannotBeFlagged() {
        Board board = Board.withMines(3, 3, new int[][] {{0, 0}});

        board.reveal(0, 1);
        board.toggleFlag(0, 1);

        assertFalse(board.getCell(0, 1).isFlagged());
    }

    @Test
    void randomBoardHasExactMineCountAfterFirstReveal() {
        Board board = new Board(16, 30, 99, new Random(42));

        assertEquals(0, countMines(board));
        board.reveal(8, 15);
        assertEquals(99, countMines(board));
    }

    @Test
    void rejectsInvalidMineCount() {
        assertThrows(IllegalArgumentException.class, () -> new Board(3, 3, 9));
        assertThrows(IllegalArgumentException.class, () -> new Board(3, 3, -1));
    }

    // --- Safe first click ---

    @Test
    void firstRevealNeverLosesAndClearsSurroundingBlock() {
        for (long seed = 0; seed < 200; seed++) {
            Board board = new Board(16, 30, 99, new Random(seed));
            int row = (int) (seed % 16);
            int col = (int) (seed % 30);

            board.reveal(row, col);

            assertEquals(GameState.PLAYING, board.getState(), "seed " + seed);
            for (int r = row - 1; r <= row + 1; r++) {
                for (int c = col - 1; c <= col + 1; c++) {
                    if (r >= 0 && r < 16 && c >= 0 && c < 30) {
                        assertFalse(board.getCell(r, c).isMine(), "seed " + seed + " cell " + r + "," + c);
                    }
                }
            }
            assertEquals(99, countMines(board), "seed " + seed);
        }
    }

    @Test
    void denseBoardOnlyProtectsClickedCell() {
        for (long seed = 0; seed < 50; seed++) {
            Board board = new Board(5, 5, 20, new Random(seed));

            board.reveal(2, 2);

            assertFalse(board.getCell(2, 2).isMine(), "seed " + seed);
            assertTrue(board.getCell(2, 2).isRevealed(), "seed " + seed);
            assertEquals(20, countMines(board), "seed " + seed);
        }
    }

    @Test
    void isStartedOnlyAfterFirstReveal() {
        Board board = new Board(9, 9, 10, new Random(1));
        assertFalse(board.isStarted());

        board.toggleFlag(0, 0);
        assertFalse(board.isStarted());

        board.toggleFlag(0, 0);
        board.reveal(4, 4);
        assertTrue(board.isStarted());
    }

    // --- Mines-left counter ---

    @Test
    void remainingMinesTracksFlags() {
        Board board = Board.withMines(3, 3, new int[][] {{0, 0}});
        assertEquals(1, board.getRemainingMines());

        board.toggleFlag(0, 0);
        assertEquals(0, board.getRemainingMines());

        board.toggleFlag(2, 2);
        assertEquals(-1, board.getRemainingMines());

        board.toggleFlag(2, 2);
        board.toggleFlag(0, 0);
        assertEquals(1, board.getRemainingMines());
    }

    // --- Chording ---

    @Test
    void chordRevealsNeighboursWhenFlagsMatch() {
        // M . .
        // . . .
        // . . M
        Board board = Board.withMines(3, 3, new int[][] {{0, 0}, {2, 2}});
        board.reveal(0, 1);
        board.toggleFlag(0, 0);

        board.chord(0, 1);

        assertTrue(board.getCell(0, 2).isRevealed());
        assertTrue(board.getCell(1, 0).isRevealed());
        assertTrue(board.getCell(1, 1).isRevealed());
        assertTrue(board.getCell(1, 2).isRevealed());
        assertFalse(board.getCell(0, 0).isRevealed());
        assertEquals(GameState.PLAYING, board.getState());
    }

    @Test
    void chordDoesNothingWhenFlagsDoNotMatch() {
        Board board = Board.withMines(3, 3, new int[][] {{0, 0}, {2, 2}});
        board.reveal(1, 1);

        board.chord(1, 1); // needs 2 flags, has 0
        assertFalse(board.getCell(0, 1).isRevealed());

        board.toggleFlag(0, 0);
        board.chord(1, 1); // needs 2 flags, has 1
        assertFalse(board.getCell(0, 1).isRevealed());
        assertEquals(GameState.PLAYING, board.getState());
    }

    @Test
    void chordDoesNothingOnHiddenOrZeroCell() {
        // M . M
        // . . .
        // . . .
        Board board = Board.withMines(3, 3, new int[][] {{0, 0}, {0, 2}});

        board.chord(1, 1); // hidden
        assertFalse(board.getCell(1, 1).isRevealed());

        board.toggleFlag(1, 1);
        board.reveal(2, 1); // zero: floods around the flagged cell
        board.toggleFlag(1, 1);
        board.chord(2, 1);
        assertFalse(board.getCell(1, 1).isRevealed());
        assertEquals(GameState.PLAYING, board.getState());
    }

    @Test
    void chordWithWrongFlagLoses() {
        Board board = Board.withMines(3, 3, new int[][] {{0, 0}});
        board.reveal(0, 1);
        board.toggleFlag(0, 2); // wrong: the mine is at 0,0

        board.chord(0, 1);

        assertEquals(GameState.LOST, board.getState());
        assertTrue(board.isExploded(0, 0));
    }

    @Test
    void chordCanWinTheGame() {
        // M .
        // . .
        Board board = Board.withMines(2, 2, new int[][] {{0, 0}});
        board.reveal(1, 1);
        board.toggleFlag(0, 0);

        board.chord(1, 1);

        assertEquals(GameState.WON, board.getState());
    }

    private static int countMines(Board board) {
        int count = 0;
        for (int r = 0; r < board.getRows(); r++) {
            for (int c = 0; c < board.getCols(); c++) {
                if (board.getCell(r, c).isMine()) {
                    count++;
                }
            }
        }
        return count;
    }
}
