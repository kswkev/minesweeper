package com.kswkev.minesweeper.ui;

import com.kswkev.minesweeper.model.Board;
import com.kswkev.minesweeper.model.GameState;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/** Grid of {@link CellButton}s bound to a {@link Board}. */
final class BoardPanel extends JPanel {

    private final Board board;
    private final CellButton[][] buttons;

    BoardPanel(Board board) {
        super(new GridLayout(board.getRows(), board.getCols()));
        this.board = board;
        this.buttons = new CellButton[board.getRows()][board.getCols()];

        MouseAdapter mouseHandler = new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                CellButton button = (CellButton) e.getSource();
                if (!button.contains(e.getPoint())) {
                    return; // Released outside the cell: treat as cancelled.
                }
                handleClick(button, e);
            }
        };

        for (int r = 0; r < board.getRows(); r++) {
            for (int c = 0; c < board.getCols(); c++) {
                CellButton button = new CellButton(r, c);
                button.addMouseListener(mouseHandler);
                buttons[r][c] = button;
                add(button);
            }
        }
        refresh();
    }

    private void handleClick(CellButton button, MouseEvent e) {
        if (board.getState() != GameState.PLAYING) {
            return;
        }
        if (SwingUtilities.isLeftMouseButton(e)) {
            board.reveal(button.row, button.col);
        } else if (SwingUtilities.isRightMouseButton(e)) {
            board.toggleFlag(button.row, button.col);
        } else {
            return;
        }
        refresh();

        if (board.getState() == GameState.WON) {
            JOptionPane.showMessageDialog(this, "You win!", "Minesweeper", JOptionPane.INFORMATION_MESSAGE);
        } else if (board.getState() == GameState.LOST) {
            JOptionPane.showMessageDialog(this, "Boom! You hit a mine.", "Minesweeper", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refresh() {
        for (int r = 0; r < board.getRows(); r++) {
            for (int c = 0; c < board.getCols(); c++) {
                buttons[r][c].render(board.getCell(r, c), board.getState(), board.isExploded(r, c));
            }
        }
    }
}