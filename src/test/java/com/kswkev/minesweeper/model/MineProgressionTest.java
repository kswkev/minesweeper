package com.kswkev.minesweeper.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MineProgressionTest {

    private static final BoardConfig BEGINNER = Difficulty.BEGINNER.getPreset().orElseThrow();

    @Test
    void startsAtBaseMines() {
        MineProgression progression = new MineProgression(BEGINNER);

        assertEquals(BEGINNER, progression.current());
        assertEquals(0, progression.getExtraMines());
    }

    @Test
    void eachWinAddsOneMine() {
        MineProgression progression = new MineProgression(BEGINNER);

        progression.onWin();
        assertEquals(11, progression.current().mines());
        progression.onWin();
        progression.onWin();
        assertEquals(13, progression.current().mines());
        assertEquals(3, progression.getExtraMines());
        assertEquals(9, progression.current().rows());
        assertEquals(9, progression.current().cols());
    }

    @Test
    void lossResetsToBase() {
        MineProgression progression = new MineProgression(BEGINNER);
        progression.onWin();
        progression.onWin();

        progression.onLoss();

        assertEquals(BEGINNER, progression.current());
        assertEquals(0, progression.getExtraMines());
    }

    @Test
    void abandonResetsToBase() {
        MineProgression progression = new MineProgression(BEGINNER);
        progression.onWin();

        progression.onAbandon();

        assertEquals(BEGINNER, progression.current());
    }

    @Test
    void resetSwitchesBaseAndClearsExtraMines() {
        MineProgression progression = new MineProgression(BEGINNER);
        progression.onWin();
        BoardConfig expert = Difficulty.EXPERT.getPreset().orElseThrow();

        progression.reset(expert);

        assertEquals(expert, progression.current());
        assertEquals(0, progression.getExtraMines());
    }

    @Test
    void stopsAtBoardMaximum() {
        BoardConfig nearlyFull = new BoardConfig(5, 5, 15); // max is 16
        MineProgression progression = new MineProgression(nearlyFull);

        progression.onWin();
        progression.onWin();
        progression.onWin();

        assertEquals(16, progression.current().mines());
        assertEquals(1, progression.getExtraMines());
    }
}
