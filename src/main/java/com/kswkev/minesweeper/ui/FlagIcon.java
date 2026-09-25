package com.kswkev.minesweeper.ui;

import javax.swing.Icon;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/** A red flag on a black pole, drawn with Java2D. */
final class FlagIcon implements Icon {

    static final FlagIcon INSTANCE = new FlagIcon();

    private static final int SIZE = 18;

    private FlagIcon() {
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

            g.setColor(Color.RED);
            g.fillPolygon(new int[] {10, 10, 3}, new int[] {2, 10, 6}, 3);

            g.setColor(Color.BLACK);
            g.fillRect(9, 2, 2, 12);
            g.fillRect(6, 13, 8, 2);
            g.fillRect(4, 15, 12, 2);
        } finally {
            g.dispose();
        }
    }
}
