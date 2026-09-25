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
        board.cycleMark(1, 1);
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

        board.cycleMark(0, 0);
        board.reveal(0, 0);

        assertTrue(board.getCell(0, 0).isFlagged());
        assertFalse(board.getCell(0, 0).isRevealed());
        assertEquals(GameState.PLAYING, board.getState());

        board.cycleMark(0, 0);
        assertFalse(board.getCell(0, 0).isFlagged());
    }

    @Test
    void floodFillSkipsFlaggedCells() {
        Board board = Board.withMines(4, 4, new int[][] {{3, 3}});

        board.cycleMark(0, 3);
        board.reveal(0, 0);

        assertFalse(board.getCell(0, 3).isRevealed());
        assertTrue(board.getCell(0, 3).isFlagged());
        assertEquals(GameState.PLAYING, board.getState());
    }

    @Test
    void revealedCellCannotBeFlagged() {
        Board board = Board.withMines(3, 3, new int[][] {{0, 0}});

        board.reveal(0, 1);
        board.cycleMark(0, 1);

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

        board.cycleMark(0, 0);
        assertFalse(board.isStarted());

        board.cycleMark(0, 0);
        board.reveal(4, 4);
        assertTrue(board.isStarted());
    }

    // --- Mines-left counter ---

    @Test
    void remainingMinesTracksFlags() {
        Board board = Board.withMines(3, 3, new int[][] {{0, 0}});
        assertEquals(1, board.getRemainingMines());

        board.cycleMark(0, 0);
        assertEquals(0, board.getRemainingMines());

        board.cycleMark(2, 2);
        assertEquals(-1, board.getRemainingMines());

        board.cycleMark(2, 2);
        board.cycleMark(0, 0);
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
        board.cycleMark(0, 0);

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

        board.cycleMark(0, 0);
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

        board.cycleMark(1, 1);
        board.reveal(2, 1); // zero: floods around the flagged cell
        board.cycleMark(1, 1);
        board.chord(2, 1);
        assertFalse(board.getCell(1, 1).isRevealed());
        assertEquals(GameState.PLAYING, board.getState());
    }

    @Test
    void chordWithWrongFlagLoses() {
        Board board = Board.withMines(3, 3, new int[][] {{0, 0}});
        board.reveal(0, 1);
        board.cycleMark(0, 2); // wrong: the mine is at 0,0

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
        board.cycleMark(0, 0);

        board.chord(1, 1);

        assertEquals(GameState.WON, board.getState());
    }

    // --- Win flags all mines ---

    @Test
    void winningFlagsAllRemainingMines() {
        Board board = Board.withMines(3, 3, new int[][] {{0, 0}, {2, 2}});

        int[][] safeCells = {{0, 1}, {0, 2}, {1, 0}, {1, 1}, {1, 2}, {2, 0}, {2, 1}};
        for (int[] cell : safeCells) {
            board.reveal(cell[0], cell[1]);
        }

        assertEquals(GameState.WON, board.getState());
        assertTrue(board.getCell(0, 0).isFlagged());
        assertTrue(board.getCell(2, 2).isFlagged());
        assertEquals(0, board.getRemainingMines());
    }

    @Test
    void winningKeepsCounterAtZeroWithExistingFlags() {
        Board board = Board.withMines(2, 2, new int[][] {{0, 0}});
        board.cycleMark(0, 0);

        board.reveal(0, 1);
        board.reveal(1, 0);
        board.reveal(1, 1);

        assertEquals(GameState.WON, board.getState());
        assertEquals(0, board.getRemainingMines());
    }

    // --- ? marks ---

    @Test
    void cycleMarkSkipsQuestionWhenDisabled() {
        Board board = Board.withMines(3, 3, new int[][] {{0, 0}});

        board.cycleMark(1, 1);
        assertEquals(Cell.Mark.FLAG, board.getCell(1, 1).getMark());
        board.cycleMark(1, 1);
        assertEquals(Cell.Mark.NONE, board.getCell(1, 1).getMark());
    }

    @Test
    void cycleMarkIncludesQuestionWhenEnabled() {
        Board board = Board.withMines(3, 3, new int[][] {{0, 0}});
        board.setQuestionMarksEnabled(true);

        board.cycleMark(1, 1);
        assertEquals(Cell.Mark.FLAG, board.getCell(1, 1).getMark());
        assertEquals(0, board.getRemainingMines());

        board.cycleMark(1, 1);
        assertEquals(Cell.Mark.QUESTION, board.getCell(1, 1).getMark());
        assertEquals(1, board.getRemainingMines(), "? does not count as a flag");

        board.cycleMark(1, 1);
        assertEquals(Cell.Mark.NONE, board.getCell(1, 1).getMark());
        assertEquals(1, board.getRemainingMines());
    }

    @Test
    void questionCellCanBeRevealed() {
        Board board = Board.withMines(3, 3, new int[][] {{0, 0}});
        board.setQuestionMarksEnabled(true);
        board.cycleMark(0, 1);
        board.cycleMark(0, 1);

        board.reveal(0, 1);

        assertTrue(board.getCell(0, 1).isRevealed());
        assertEquals(Cell.Mark.NONE, board.getCell(0, 1).getMark());
    }

    @Test
    void floodFillOpensQuestionCells() {
        Board board = Board.withMines(4, 4, new int[][] {{3, 3}});
        board.setQuestionMarksEnabled(true);
        board.cycleMark(0, 3);
        board.cycleMark(0, 3);

        board.reveal(0, 0);

        assertTrue(board.getCell(0, 3).isRevealed());
        assertEquals(GameState.WON, board.getState());
    }

    @Test
    void chordIgnoresQuestionMarksAndRevealsThem() {
        // M . .
        // . . .
        // . . M
        Board board = Board.withMines(3, 3, new int[][] {{0, 0}, {2, 2}});
        board.setQuestionMarksEnabled(true);
        board.reveal(0, 1);
        board.cycleMark(0, 0);
        board.cycleMark(0, 0); // now ?, not a flag

        board.chord(0, 1);
        assertFalse(board.getCell(1, 1).isRevealed(), "? does not satisfy the number");

        board.cycleMark(0, 0); // none
        board.cycleMark(0, 0); // flag
        board.cycleMark(1, 1);
        board.cycleMark(1, 1); // ? on a safe neighbour
        board.chord(0, 1);
        assertTrue(board.getCell(1, 1).isRevealed());
    }

    // --- Change notifications ---

    @Test
    void changeListenerFiresOncePerAction() {
        Board board = Board.withMines(3, 3, new int[][] {{0, 0}, {2, 2}});
        int[] calls = {0};
        board.addChangeListener(() -> calls[0]++);

        board.reveal(0, 1);
        assertEquals(1, calls[0]);

        board.cycleMark(0, 0);
        assertEquals(2, calls[0]);

        board.chord(0, 1); // reveals several neighbours
        assertEquals(3, calls[0]);
    }

    @Test
    void changeListenerSilentWhenNothingChanges() {
        Board board = Board.withMines(3, 3, new int[][] {{0, 0}});
        int[] calls = {0};
        board.addChangeListener(() -> calls[0]++);

        board.reveal(0, 1);
        board.reveal(0, 1); // already revealed
        board.chord(0, 1); // no flags yet
        board.cycleMark(0, 1); // revealed cells can't be marked

        assertEquals(1, calls[0]);
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
