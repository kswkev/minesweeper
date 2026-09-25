package com.kswkev.minesweeper.ui;

import com.kswkev.minesweeper.model.Board;
import com.kswkev.minesweeper.model.GameState;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/** Grid of {@link CellButton}s bound to a {@link Board}. Redraws whenever the board changes. */
final class BoardPanel extends JPanel {

    /** Notified while a reveal or chord is held down, so the header can show the worried face. */
    interface PressListener {
        void cellPressed();

        void cellReleased();
    }

    private final Board board;
    private final CellButton[][] buttons;

    BoardPanel(Board board, PressListener pressListener) {
        super(new GridLayout(board.getRows(), board.getCols()));
        this.board = board;
        this.buttons = new CellButton[board.getRows()][board.getCols()];

        MouseAdapter mouseHandler = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (board.getState() == GameState.PLAYING && !SwingUtilities.isRightMouseButton(e)) {
                    pressListener.cellPressed();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                CellButton button = (CellButton) e.getSource();
                // Released outside the cell: treat as cancelled.
                if (button.contains(e.getPoint())) {
                    handleClick(button, e);
                }
                pressListener.cellReleased();
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
        board.addChangeListener(this::refresh);
        refresh();
    }

    private void handleClick(CellButton button, MouseEvent e) {
        int row = button.row;
        int col = button.col;
        if (SwingUtilities.isMiddleMouseButton(e)
                || (SwingUtilities.isLeftMouseButton(e) && board.getCell(row, col).isRevealed())) {
            board.chord(row, col);
        } else if (SwingUtilities.isLeftMouseButton(e)) {
            board.reveal(row, col);
        } else if (SwingUtilities.isRightMouseButton(e)) {
            board.cycleMark(row, col);
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
