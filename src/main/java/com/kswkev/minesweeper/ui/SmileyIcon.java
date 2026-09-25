package com.kswkev.minesweeper.ui;

import javax.swing.Icon;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Arc2D;

/** The reset-button face, drawn with Java2D so it doesn't depend on emoji fonts. */
final class SmileyIcon implements Icon {

    enum Face {
        NORMAL,
        WORRIED,
        WON,
        LOST
    }

    private static final int SIZE = 26;
    private static final Color FACE_FILL = new Color(0xFFD700);

    private final Face face;

    SmileyIcon(Face face) {
        this.face = face;
    }

    @Override
    public int getIconWidth() {
        return SIZE;
    }

    @Override
    public int getIconHeight() {
        return SIZE;
    }

    @Override
    public void paintIcon(Component c, Graphics graphics, int x, int y) {
        Graphics2D g = (Graphics2D) graphics.create();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.translate(x, y);
            g.setStroke(new BasicStroke(1.5f));

            g.setColor(FACE_FILL);
            g.fillOval(1, 1, SIZE - 2, SIZE - 2);
            g.setColor(Color.BLACK);
            g.drawOval(1, 1, SIZE - 2, SIZE - 2);

            paintEyes(g);
            paintMouth(g);
        } finally {
            g.dispose();
        }
    }

    private void paintEyes(Graphics2D g) {
        switch (face) {
            case WON -> {
                // Sunglasses: two dark lenses joined by a bridge.
                g.fillRoundRect(5, 8, 7, 5, 3, 3);
                g.fillRoundRect(14, 8, 7, 5, 3, 3);
                g.drawLine(11, 9, 15, 9);
                g.drawLine(2, 9, 5, 9);
                g.drawLine(21, 9, 24, 9);
            }
            case LOST -> {
                drawCross(g, 9, 10);
                drawCross(g, 17, 10);
            }
            default -> {
                g.fillOval(8, 8, 3, 4);
                g.fillOval(15, 8, 3, 4);
            }
        }
    }

    private void paintMouth(Graphics2D g) {
        switch (face) {
            case WORRIED -> g.drawOval(11, 16, 4, 5);
            case LOST -> g.draw(new Arc2D.Double(8, 16, 10, 7, 20, 140, Arc2D.OPEN));
            default -> g.draw(new Arc2D.Double(7, 10, 12, 10, 200, 140, Arc2D.OPEN));
        }
    }

    private static void drawCross(Graphics2D g, int cx, int cy) {
        g.drawLine(cx - 2, cy - 2, cx + 2, cy + 2);
        g.drawLine(cx - 2, cy + 2, cx + 2, cy - 2);
    }
}
