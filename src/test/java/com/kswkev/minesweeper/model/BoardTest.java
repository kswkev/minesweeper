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
    void randomBoardHasExactMineCount() {
        Board board = new Board(16, 30, 99, new Random(42));

        int count = 0;
        for (int r = 0; r < board.getRows(); r++) {
            for (int c = 0; c < board.getCols(); c++) {
                if (board.getCell(r, c).isMine()) {
                    count++;
                }
            }
        }
        assertEquals(99, count);
    }

    @Test
    void rejectsInvalidMineCount() {
        assertThrows(IllegalArgumentException.class, () -> new Board(3, 3, 9));
        assertThrows(IllegalArgumentException.class, () -> new Board(3, 3, -1));
    }
}
