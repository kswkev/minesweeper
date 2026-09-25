package com.kswkev.minesweeper.model;

/**
 * Win-streak mine increase: each win adds one mine to the next game; a loss, an abandoned game
 * or a change of difficulty drops back to the base mine count.
 */
public final class MineProgression {

    private BoardConfig base;
    private int extraMines;

    public MineProgression(BoardConfig base) {
        this.base = base;
    }

    /** The config for the next game: the base board plus any extra mines, capped at the board's maximum. */
    public BoardConfig current() {
        return base.withMines(Math.min(base.mines() + extraMines, base.maxMines()));
    }

    public void onWin() {
        if (base.mines() + extraMines < base.maxMines()) {
            extraMines++;
        }
    }

    public void onLoss() {
        extraMines = 0;
    }

    public void onAbandon() {
        extraMines = 0;
    }

    /** Starts again from a new base board, e.g. after a difficulty change. */
    public void reset(BoardConfig newBase) {
        base = newBase;
        extraMines = 0;
    }

    public BoardConfig getBase() {
        return base;
    }

    public int getExtraMines() {
        return extraMines;
    }
}
