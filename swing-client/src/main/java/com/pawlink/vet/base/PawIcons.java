package com.pawlink.vet.base;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;

/**
 * Pure Java2D Vector Icon Engine for PawLink.
 * Eliminates all emoji font fallback issues (which caused icons to render as blank boxes/tofu '[]').
 * Renders scalable, high-DPI, pixel-perfect vector icons with antialiased geometry.
 */
public final class PawIcons {

    private PawIcons() {}

    @FunctionalInterface
    public interface IconPainter {
        void paint(Graphics2D g2, int size, Color color);
    }

    private static class VectorIcon implements Icon {
        private final int size;
        private final Color color;
        private final IconPainter painter;

        public VectorIcon(int size, Color color, IconPainter painter) {
            this.size = size;
            this.color = color;
            this.painter = painter;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.translate(x, y);
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
                painter.paint(g2, size, color);
            } finally {
                g2.dispose();
            }
        }

        @Override
        public int getIconWidth() {
            return size;
        }

        @Override
        public int getIconHeight() {
            return size;
        }
    }

    public static Icon create(int size, Color color, IconPainter painter) {
        return new VectorIcon(size, color, painter);
    }

    // --- 1. Paw Icon (Pet Care Brand Mark) ---
    public static Icon paw(int size, Color color) {
        return create(size, color, (g2, s, c) -> {
            double scale = s / 24.0;
            g2.scale(scale, scale);
            g2.setColor(c);

            // Main Pad (rounded heart-like pad)
            Path2D.Double pad = new Path2D.Double();
            pad.moveTo(12, 11);
            pad.curveTo(8, 11, 6, 14, 6, 17);
            pad.curveTo(6, 20, 8.5, 22, 12, 22);
            pad.curveTo(15.5, 22, 18, 20, 18, 17);
            pad.curveTo(18, 14, 16, 11, 12, 11);
            pad.closePath();
            g2.fill(pad);

            // 4 Toes
            g2.fill(new Ellipse2D.Double(5.5, 6.5, 3.2, 4.4));
            g2.fill(new Ellipse2D.Double(9.5, 4.0, 3.2, 4.8));
            g2.fill(new Ellipse2D.Double(14.5, 4.0, 3.2, 4.8));
            g2.fill(new Ellipse2D.Double(18.5, 6.5, 3.2, 4.4));
        });
    }

    // --- 2. Stethoscope / Clinical Doctor Icon ---
    public static Icon stethoscope(int size, Color color) {
        return create(size, color, (g2, s, c) -> {
            double scale = s / 24.0;
            g2.scale(scale, scale);
            g2.setColor(c);
            g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            // Earpieces & U-tube
            Path2D.Double tube = new Path2D.Double();
            tube.moveTo(6, 4);
            tube.lineTo(6, 8);
            tube.curveTo(6, 13, 16, 13, 16, 8);
            tube.lineTo(16, 4);
            g2.draw(tube);

            // Ear tips
            g2.fill(new Ellipse2D.Double(4.5, 3, 3, 3));
            g2.fill(new Ellipse2D.Double(14.5, 3, 3, 3));

            // Stem & Chest Piece
            Path2D.Double stem = new Path2D.Double();
            stem.moveTo(11, 12.8);
            stem.lineTo(11, 16);
            stem.curveTo(11, 19, 18, 19, 18, 16);
            g2.draw(stem);

            g2.fill(new Ellipse2D.Double(16.5, 14.5, 4.5, 4.5));
        });
    }

    // --- 3. Calendar / Appointment Schedule Icon ---
    public static Icon calendar(int size, Color color) {
        return create(size, color, (g2, s, c) -> {
            double scale = s / 24.0;
            g2.scale(scale, scale);
            g2.setColor(c);

            // Outline body
            g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.draw(new RoundRectangle2D.Double(3.5, 5.5, 17, 15, 3.5, 3.5));

            // Top Header Bar
            g2.drawLine(4, 10, 20, 10);

            // Rings / Bindings
            g2.draw(new Line2D.Double(7.5, 3.5, 7.5, 6.5));
            g2.draw(new Line2D.Double(16.5, 3.5, 16.5, 6.5));

            // Grid dots (dates)
            g2.fill(new Ellipse2D.Double(7, 13, 2, 2));
            g2.fill(new Ellipse2D.Double(11, 13, 2, 2));
            g2.fill(new Ellipse2D.Double(15, 13, 2, 2));
            g2.fill(new Ellipse2D.Double(7, 16.5, 2, 2));
            g2.fill(new Ellipse2D.Double(11, 16.5, 2, 2));
        });
    }

    // --- 4. Sitter / Companion Walk Icon ---
    public static Icon sitter(int size, Color color) {
        return create(size, color, (g2, s, c) -> {
            double scale = s / 24.0;
            g2.scale(scale, scale);
            g2.setColor(c);
            g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            // Sitter Silhouette
            g2.fill(new Ellipse2D.Double(6.5, 4, 4.5, 4.5));
            Path2D.Double body = new Path2D.Double();
            body.moveTo(4, 17);
            body.curveTo(4, 12, 13.5, 12, 13.5, 17);
            g2.draw(body);

            // Little companion pet next to them
            g2.fill(new Ellipse2D.Double(16.5, 13, 3.5, 3.5)); // Pet head
            g2.draw(new RoundRectangle2D.Double(15, 15.5, 6, 4.5, 2, 2)); // Pet body
            // Leash
            g2.drawLine(11, 14, 16, 15);
        });
    }

    // --- 5. Shield / Admin Security Icon ---
    public static Icon shield(int size, Color color) {
        return create(size, color, (g2, s, c) -> {
            double scale = s / 24.0;
            g2.scale(scale, scale);
            g2.setColor(c);
            g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            Path2D.Double p = new Path2D.Double();
            p.moveTo(12, 3.5);
            p.lineTo(19, 6.5);
            p.curveTo(19, 13, 16, 18, 12, 20.5);
            p.curveTo(8, 18, 5, 13, 5, 6.5);
            p.closePath();
            g2.draw(p);

            // Center Checkmark
            Path2D.Double check = new Path2D.Double();
            check.moveTo(9, 11.5);
            check.lineTo(11, 14);
            check.lineTo(15.5, 9);
            g2.draw(check);
        });
    }

    // --- 6. Vaccine / Syringe Icon ---
    public static Icon vaccine(int size, Color color) {
        return create(size, color, (g2, s, c) -> {
            double scale = s / 24.0;
            g2.scale(scale, scale);
            g2.setColor(c);
            g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            // Angled barrel
            Path2D.Double b = new Path2D.Double();
            b.moveTo(13, 5);
            b.lineTo(19, 11);
            b.lineTo(16, 14);
            b.lineTo(10, 8);
            b.closePath();
            g2.draw(b);

            // Needle
            g2.drawLine(10, 14, 5, 19);
            // Plunger top
            g2.drawLine(15, 3, 21, 9);
        });
    }

    // --- 7. Medical Record / Clipboard Icon ---
    public static Icon medicalRecord(int size, Color color) {
        return create(size, color, (g2, s, c) -> {
            double scale = s / 24.0;
            g2.scale(scale, scale);
            g2.setColor(c);
            g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            // Clipboard board
            g2.draw(new RoundRectangle2D.Double(4, 5, 16, 16, 3, 3));
            // Clip
            g2.fill(new RoundRectangle2D.Double(8.5, 3, 7, 3.5, 1.5, 1.5));

            // Medical Cross
            g2.drawLine(12, 10, 12, 16);
            g2.drawLine(9, 13, 15, 13);
        });
    }

    // --- 8. Notification Bell Icon ---
    public static Icon bell(int size, Color color) {
        return create(size, color, (g2, s, c) -> {
            double scale = s / 24.0;
            g2.scale(scale, scale);
            g2.setColor(c);
            g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            Path2D.Double b = new Path2D.Double();
            b.moveTo(12, 4);
            b.curveTo(8, 4, 6, 8, 6, 13);
            b.lineTo(4.5, 16.5);
            b.lineTo(19.5, 16.5);
            b.lineTo(18, 13);
            b.curveTo(18, 8, 16, 4, 12, 4);
            b.closePath();
            g2.draw(b);

            // Clapper
            g2.fill(new Arc2D.Double(10, 16.5, 4, 4, 180, 180, Arc2D.CHORD));
        });
    }

    // --- 9. Wallet / Payment Icon ---
    public static Icon wallet(int size, Color color) {
        return create(size, color, (g2, s, c) -> {
            double scale = s / 24.0;
            g2.scale(scale, scale);
            g2.setColor(c);
            g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            // Wallet Body
            g2.draw(new RoundRectangle2D.Double(3, 6, 18, 13, 3, 3));
            // Card flap
            Path2D.Double flap = new Path2D.Double();
            flap.moveTo(14, 9.5);
            flap.lineTo(19.5, 9.5);
            flap.curveTo(21, 9.5, 21, 15.5, 19.5, 15.5);
            flap.lineTo(14, 15.5);
            flap.closePath();
            g2.draw(flap);
            g2.fill(new Ellipse2D.Double(17, 11.5, 2, 2));
        });
    }

    // --- 10. Document / PDF Paper Icon ---
    public static Icon document(int size, Color color) {
        return create(size, color, (g2, s, c) -> {
            double scale = s / 24.0;
            g2.scale(scale, scale);
            g2.setColor(c);
            g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            Path2D.Double p = new Path2D.Double();
            p.moveTo(5, 4);
            p.lineTo(14, 4);
            p.lineTo(19, 9);
            p.lineTo(19, 20);
            p.lineTo(5, 20);
            p.closePath();
            g2.draw(p);

            // Fold
            g2.drawLine(14, 4, 14, 9);
            g2.drawLine(14, 9, 19, 9);

            // Text lines
            g2.drawLine(8, 13, 16, 13);
            g2.drawLine(8, 16, 14, 16);
        });
    }

    // --- 11. ID Card / Badge Icon ---
    public static Icon idCard(int size, Color color) {
        return create(size, color, (g2, s, c) -> {
            double scale = s / 24.0;
            g2.scale(scale, scale);
            g2.setColor(c);
            g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            g2.draw(new RoundRectangle2D.Double(3, 5, 18, 14, 3, 3));
            // Photo box
            g2.draw(new Rectangle2D.Double(5.5, 8, 4.5, 5.5));
            // Lines
            g2.drawLine(12, 9, 18, 9);
            g2.drawLine(12, 12, 17, 12);
        });
    }

    // --- 12. Checkmark Icon ---
    public static Icon check(int size, Color color) {
        return create(size, color, (g2, s, c) -> {
            double scale = s / 24.0;
            g2.scale(scale, scale);
            g2.setColor(c);
            g2.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            Path2D.Double p = new Path2D.Double();
            p.moveTo(4.5, 12);
            p.lineTo(9.5, 17);
            p.lineTo(19.5, 6.5);
            g2.draw(p);
        });
    }

    // --- 13. Cross / Cancel Icon ---
    public static Icon cross(int size, Color color) {
        return create(size, color, (g2, s, c) -> {
            double scale = s / 24.0;
            g2.scale(scale, scale);
            g2.setColor(c);
            g2.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            g2.drawLine(6, 6, 18, 18);
            g2.drawLine(18, 6, 6, 18);
        });
    }

    // --- 14. Plus / Add Icon ---
    public static Icon plus(int size, Color color) {
        return create(size, color, (g2, s, c) -> {
            double scale = s / 24.0;
            g2.scale(scale, scale);
            g2.setColor(c);
            g2.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            g2.drawLine(12, 5, 12, 19);
            g2.drawLine(5, 12, 19, 12);
        });
    }

    // --- 15. User / Profile Icon ---
    public static Icon user(int size, Color color) {
        return create(size, color, (g2, s, c) -> {
            double scale = s / 24.0;
            g2.scale(scale, scale);
            g2.setColor(c);
            g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            // Head
            g2.fill(new Ellipse2D.Double(8, 4.5, 8, 8));
            // Shoulders
            Path2D.Double sld = new Path2D.Double();
            sld.moveTo(4, 20);
            sld.curveTo(4, 15, 20, 15, 20, 20);
            g2.draw(sld);
        });
    }

    // --- 16. Logout / Exit Icon ---
    public static Icon logout(int size, Color color) {
        return create(size, color, (g2, s, c) -> {
            double scale = s / 24.0;
            g2.scale(scale, scale);
            g2.setColor(c);
            g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            // Door frame
            Path2D.Double d = new Path2D.Double();
            d.moveTo(9, 4);
            d.lineTo(4, 4);
            d.lineTo(4, 20);
            d.lineTo(9, 20);
            g2.draw(d);

            // Arrow pointing right
            g2.drawLine(9, 12, 20, 12);
            g2.drawLine(16, 8, 20, 12);
            g2.drawLine(16, 16, 20, 12);
        });
    }

    // --- 17. Refresh / Sync Icon ---
    public static Icon refresh(int size, Color color) {
        return create(size, color, (g2, s, c) -> {
            double scale = s / 24.0;
            g2.scale(scale, scale);
            g2.setColor(c);
            g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            // Arc
            g2.draw(new Arc2D.Double(4, 4, 16, 16, 45, 270, Arc2D.OPEN));
            // Arrowhead
            Path2D.Double arrow = new Path2D.Double();
            arrow.moveTo(17, 4);
            arrow.lineTo(21, 7.5);
            arrow.lineTo(16, 9.5);
            g2.fill(arrow);
        });
    }

    // --- 18. Analytics / Stats Icon ---
    public static Icon stats(int size, Color color) {
        return create(size, color, (g2, s, c) -> {
            double scale = s / 24.0;
            g2.scale(scale, scale);
            g2.setColor(c);

            // 3 Vertical Bars
            g2.fill(new RoundRectangle2D.Double(4.5, 12, 3.5, 8, 1.5, 1.5));
            g2.fill(new RoundRectangle2D.Double(10.2, 7, 3.5, 13, 1.5, 1.5));
            g2.fill(new RoundRectangle2D.Double(16.0, 4, 3.5, 16, 1.5, 1.5));
        });
    }

    // --- 19. Search / Lens Icon ---
    public static Icon search(int size, Color color) {
        return create(size, color, (g2, s, c) -> {
            double scale = s / 24.0;
            g2.scale(scale, scale);
            g2.setColor(c);
            g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            g2.draw(new Ellipse2D.Double(4, 4, 11, 11));
            g2.drawLine(13, 13, 20, 20);
        });
    }

    // --- 20. Circle Dot Icon (for status pills) ---
    public static Icon dot(int size, Color color) {
        return create(size, color, (g2, s, c) -> {
            g2.setColor(c);
            int d = Math.max(4, s - 4);
            int offset = (s - d) / 2;
            g2.fill(new Ellipse2D.Double(offset, offset, d, d));
        });
    }

    // --- 21. Eye / Preview Icon ---
    public static Icon eye(int size, Color color) {
        return create(size, color, (g2, s, c) -> {
            double scale = s / 24.0;
            g2.scale(scale, scale);
            g2.setColor(c);
            g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            Path2D.Double p = new Path2D.Double();
            p.moveTo(3, 12);
            p.curveTo(7, 6, 17, 6, 21, 12);
            p.curveTo(17, 18, 7, 18, 3, 12);
            p.closePath();
            g2.draw(p);

            g2.fill(new Ellipse2D.Double(9.5, 9.5, 5, 5));
        });
    }

    // --- 22. Clock / Time Icon ---
    public static Icon clock(int size, Color color) {
        return create(size, color, (g2, s, c) -> {
            double scale = s / 24.0;
            g2.scale(scale, scale);
            g2.setColor(c);
            g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            g2.draw(new Ellipse2D.Double(3, 3, 18, 18));
            // Hour hand (pointing to 12)
            g2.drawLine(12, 12, 12, 7);
            // Minute hand (pointing to 3)
            g2.drawLine(12, 12, 16, 12);
        });
    }

    // --- 23. Heart / Care Love Icon ---
    public static Icon heart(int size, Color color) {
        return create(size, color, (g2, s, c) -> {
            double scale = s / 24.0;
            g2.scale(scale, scale);
            g2.setColor(c);

            Path2D.Double p = new Path2D.Double();
            p.moveTo(12, 20);
            p.curveTo(4, 13.5, 3, 7.5, 7.5, 4.8);
            p.curveTo(10, 3.2, 11.5, 4.5, 12, 6);
            p.curveTo(12.5, 4.5, 14, 3.2, 16.5, 4.8);
            p.curveTo(21, 7.5, 20, 13.5, 12, 20);
            p.closePath();
            g2.fill(p);
        });
    }

    // --- 24. Star / Sparkle Icon ---
    public static Icon star(int size, Color color) {
        return create(size, color, (g2, s, c) -> {
            double scale = s / 24.0;
            g2.scale(scale, scale);
            g2.setColor(c);

            Path2D.Double p = new Path2D.Double();
            p.moveTo(12, 3);
            p.lineTo(14.8, 8.6);
            p.lineTo(21, 9.5);
            p.lineTo(16.5, 13.9);
            p.lineTo(17.6, 20.1);
            p.lineTo(12, 17.2);
            p.lineTo(6.4, 20.1);
            p.lineTo(7.5, 13.9);
            p.lineTo(3, 9.5);
            p.lineTo(9.2, 8.6);
            p.closePath();
            g2.fill(p);
        });
    }

    // --- 25. Bone / Treat Icon ---
    public static Icon bone(int size, Color color) {
        return create(size, color, (g2, s, c) -> {
            double scale = s / 24.0;
            g2.scale(scale, scale);
            g2.setColor(c);

            g2.setStroke(new BasicStroke(3.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawLine(8, 12, 16, 12);
            g2.fill(new Ellipse2D.Double(5, 8.5, 4.5, 4.5));
            g2.fill(new Ellipse2D.Double(5, 11.5, 4.5, 4.5));
            g2.fill(new Ellipse2D.Double(14.5, 8.5, 4.5, 4.5));
            g2.fill(new Ellipse2D.Double(14.5, 11.5, 4.5, 4.5));
        });
    }
}
