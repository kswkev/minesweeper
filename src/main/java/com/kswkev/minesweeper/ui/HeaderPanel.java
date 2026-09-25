package com.kswkev.minesweeper.ui;

import com.kswkev.minesweeper.model.Board;
import com.kswkev.minesweeper.model.GameSession;
import com.kswkev.minesweeper.model.GameState;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;

/** Mines-left counter, smiley reset button and elapsed-time display. */
final class HeaderPanel extends JPanel {

    private static final int MAX_DISPLAY = GameSession.MAX_SECONDS;
    private static final int MIN_DISPLAY = -99;
    /** How often the time display is redrawn; the session owns the actual elapsed time. */
    private static final int REDRAW_MS = 250;

    private final GameSession session;
    private final JLabel minesLabel = createDisplay();
    private final JLabel timeLabel = createDisplay();
    private final JButton smileyButton = new JButton();
    private final Timer redrawTimer;

    HeaderPanel(GameSession session) {
        super(new BorderLayout());
        this.session = session;
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(4, 4, 4, 4),
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLoweredBevelBorder(),
                        BorderFactory.createEmptyBorder(4, 6, 4, 6))));

        smileyButton.setFocusable(false);
        smileyButton.setMargin(new Insets(2, 2, 2, 2));
        smileyButton.setToolTipText("New game (F2)");
        smileyButton.addActionListener(e -> session.newGame());

        JPanel centre = new JPanel();
        centre.setOpaque(false);
        centre.add(smileyButton);

        add(minesLabel, BorderLayout.WEST);
        add(centre, BorderLayout.CENTER);
        add(timeLabel, BorderLayout.EAST);

        redrawTimer = new Timer(REDRAW_MS, e -> timeLabel.setText(format(session.getElapsedSeconds())));
    }

    private static JLabel createDisplay() {
        JLabel label = new JLabel("000", SwingConstants.CENTER);
        label.setOpaque(true);
        label.setBackground(Color.BLACK);
        label.setForeground(Color.RED);
        label.setFont(new Font(Font.MONOSPACED, Font.BOLD, 24));
        label.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));
        return label;
    }

    /** Binds the header to a fresh board and resets the counter, face and timer. */
    void reset(Board board) {
        board.addChangeListener(this::update);
        update();
    }

    void showWorried() {
        smileyButton.setIcon(new SmileyIcon(SmileyIcon.Face.WORRIED));
    }

    /** Refreshes the counter, face and timer from the session's current board. */
    void update() {
        Board board = session.getBoard();
        minesLabel.setText(format(board.getRemainingMines()));
        timeLabel.setText(format(session.getElapsedSeconds()));

        GameState state = board.getState();
        if (state == GameState.PLAYING && board.isStarted()) {
            redrawTimer.start();
        } else {
            redrawTimer.stop();
        }

        SmileyIcon.Face face = switch (state) {
            case WON -> SmileyIcon.Face.WON;
            case LOST -> SmileyIcon.Face.LOST;
            case PLAYING -> SmileyIcon.Face.NORMAL;
        };
        smileyButton.setIcon(new SmileyIcon(face));
    }

    private static String format(int value) {
        int clamped = Math.max(MIN_DISPLAY, Math.min(MAX_DISPLAY, value));
        return clamped < 0 ? String.format("-%02d", -clamped) : String.format("%03d", clamped);
    }
}
