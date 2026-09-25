package com.kswkev.minesweeper.ui;

import com.kswkev.minesweeper.model.Cell;
import com.kswkev.minesweeper.model.GameState;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.border.Border;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;

/** Renders one {@link Cell}. Holds no game state of its own. */
final class CellButton extends JButton {

    private static final int SIZE = 30;

    private static final Color HIDDEN_BG = new Color(0xC0C0C0);
    private static final Color REVEALED_BG = new Color(0xC0C0C0);
    private static final Color EXPLODED_BG = new Color(0xFF4040);
    private static final Border HIDDEN_BORDER = BorderFactory.createRaisedBevelBorder();
    // Classic sunken look: a thin dark line on the top and left edges only.
    private static final Border REVEALED_BORDER = BorderFactory.createMatteBorder(1, 1, 0, 0, new Color(0x808080));

    private static final Color[] NUMBER_COLOURS = {
            null,
            new Color(0x0000FF), // 1
            new Color(0x008000), // 2
            new Color(0xFF0000), // 3
            new Color(0x000080), // 4
            new Color(0x800000), // 5
            new Color(0x008080), // 6
            new Color(0x000000), // 7
            new Color(0x808080), // 8
    };

    final int row;
    final int col;

    CellButton(int row, int col) {
        this.row = row;
        this.col = col;
        setPreferredSize(new Dimension(SIZE, SIZE));
        setMargin(new Insets(0, 0, 0, 0));
        setFocusable(false);
        setFocusPainted(false);
        setContentAreaFilled(true);
        setOpaque(true);
        setFont(new Font(Font.DIALOG, Font.BOLD, 16));
    }

    void render(Cell cell, GameState state, boolean exploded) {
        boolean lost = state == GameState.LOST;
        String text = "";
        Icon icon = null;
        Color fg = Color.BLACK;
        Color bg = HIDDEN_BG;
        Border border = HIDDEN_BORDER;

        if (cell.isRevealed() || (lost && cell.isMine() && !cell.isFlagged())) {
            bg = exploded ? EXPLODED_BG : REVEALED_BG;
            border = REVEALED_BORDER;
            if (cell.isMine()) {
                icon = MineIcon.INSTANCE;
            } else if (cell.getAdjacentMines() > 0) {
                text = Integer.toString(cell.getAdjacentMines());
                fg = NUMBER_COLOURS[cell.getAdjacentMines()];
            }
        } else if (cell.isFlagged()) {
            if (lost && !cell.isMine()) {
                // Wrong flag: mark it once the game is lost.
                text = "X";
                fg = Color.RED;
            } else {
                icon = FlagIcon.INSTANCE;
            }
        } else if (cell.isQuestion()) {
            text = "?";
        }

        setText(text);
        setIcon(icon);
        setForeground(fg);
        setBackground(bg);
        setBorder(border);
    }
}
