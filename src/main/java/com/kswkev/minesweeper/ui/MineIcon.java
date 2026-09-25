package com.kswkev.minesweeper.ui;

import javax.swing.Icon;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/** A black mine with spikes and a highlight, drawn with Java2D. */
final class MineIcon implements Icon {

    static final MineIcon INSTANCE = new MineIcon();

    private static final int SIZE = 18;

    private MineIcon() {
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
            int mid = SIZE / 2;

            g.setColor(Color.BLACK);
            g.setStroke(new BasicStroke(2f));
            g.drawLine(mid, 1, mid, SIZE - 1);
            g.drawLine(1, mid, SIZE - 1, mid);
            g.setStroke(new BasicStroke(1.5f));
            g.drawLine(3, 3, SIZE - 3, SIZE - 3);
            g.drawLine(3, SIZE - 3, SIZE - 3, 3);
            g.fillOval(3, 3, SIZE - 6, SIZE - 6);

            g.setColor(Color.WHITE);
            g.fillOval(mid - 4, mid - 4, 3, 3);
        } finally {
            g.dispose();
        }
    }
}
