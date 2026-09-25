package com.kswkev.minesweeper.model;

/** A single square on the board. Mutated only by {@link Board}. */
public final class Cell {

    private boolean mine;
    private boolean revealed;
    private boolean flagged;
    private int adjacentMines;

    public boolean isMine() {
        return mine;
    }

    public boolean isRevealed() {
        return revealed;
    }

    public boolean isFlagged() {
        return flagged;
    }

    public int getAdjacentMines() {
        return adjacentMines;
    }

    void setMine(boolean mine) {
        this.mine = mine;
    }

    void setRevealed(boolean revealed) {
        this.revealed = revealed;
    }

    void setFlagged(boolean flagged) {
        this.flagged = flagged;
    }

    void setAdjacentMines(int adjacentMines) {
        this.adjacentMines = adjacentMines;
    }
}