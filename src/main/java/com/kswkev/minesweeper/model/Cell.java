package com.kswkev.minesweeper.model;

/** A single square on the board. Mutated only by {@link Board}. */
public final class Cell {

    /** A player-placed marker on a hidden cell. */
    public enum Mark {
        NONE,
        FLAG,
        QUESTION
    }

    private boolean mine;
    private boolean revealed;
    private Mark mark = Mark.NONE;
    private int adjacentMines;

    public boolean isMine() {
        return mine;
    }

    public boolean isRevealed() {
        return revealed;
    }

    public Mark getMark() {
        return mark;
    }

    public boolean isFlagged() {
        return mark == Mark.FLAG;
    }

    public boolean isQuestion() {
        return mark == Mark.QUESTION;
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

    void setMark(Mark mark) {
        this.mark = mark;
    }

    void setAdjacentMines(int adjacentMines) {
        this.adjacentMines = adjacentMines;
    }
}
