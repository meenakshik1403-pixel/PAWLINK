package com.pawlink.vet.base;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * Modern, tactile, animated Swing components for PawLink.
 * Features smooth micro-hover transitions, generous responsive padding,
 * and multi-coloured candy pill accents.
 */
public final class PawComponents {

    private PawComponents() {}

    public enum ButtonStyle {
        PRIMARY,
        SECONDARY,
        SUCCESS,
        DANGER,
        ACTION_BLUE,
        GHOST,
        CORAL,
        SUNNY,
        MINT,
        SKY,
        LILAC,
        INDIGO
    }

    /**
     * Tactile, animated modern button with smooth hover fade, generous padding, and rounded corners.
     */
    public static class PawButton extends JButton {
        private final ButtonStyle style;
        private float hoverAlpha = 0f;
        private Timer hoverTimer;
        private boolean isHovered = false;
        private boolean isPressed = false;
        private int arc = 14;

        public PawButton(String text, Icon icon, ButtonStyle style) {
            super(text, icon);
            this.style = style;
            init();
        }

        public PawButton(String text, ButtonStyle style) {
            this(text, null, style);
        }

        public PawButton(String text, Icon icon) {
            this(text, icon, ButtonStyle.PRIMARY);
        }

        private void init() {
            setFont(PawTheme.FONT_BODY_BOLD);
            setFocusPainted(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setIconTextGap(10);
            setBorder(new EmptyBorder(10, 20, 10, 20));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    if (isEnabled()) {
                        isHovered = true;
                        startFade(true);
                    }
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    isHovered = false;
                    isPressed = false;
                    startFade(false);
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    if (isEnabled()) {
                        isPressed = true;
                        repaint();
                    }
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    isPressed = false;
                    repaint();
                }
            });
        }

        @Override
        public Dimension getPreferredSize() {
            Dimension d = super.getPreferredSize();
            int h = Math.max(d.height, 38);
            int w = Math.max(d.width, 80);
            return new Dimension(w, h);
        }

        public void setArc(int arc) {
            this.arc = arc;
            repaint();
        }

        private void startFade(boolean fadeIn) {
            if (hoverTimer != null && hoverTimer.isRunning()) {
                hoverTimer.stop();
            }
            hoverTimer = new Timer(15, e -> {
                if (fadeIn) {
                    hoverAlpha += 0.2f;
                    if (hoverAlpha >= 1f) {
                        hoverAlpha = 1f;
                        hoverTimer.stop();
                    }
                } else {
                    hoverAlpha -= 0.2f;
                    if (hoverAlpha <= 0f) {
                        hoverAlpha = 0f;
                        hoverTimer.stop();
                    }
                }
                repaint();
            });
            hoverTimer.start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth();
                int h = getHeight();

                Color bgBase;
                Color bgHover;
                Color fgColor;
                Color borderCol = null;

                switch (style) {
                    case PRIMARY -> {
                        bgBase = PawTheme.PRIMARY;
                        bgHover = PawTheme.PRIMARY_HOVER;
                        fgColor = Color.WHITE;
                    }
                    case CORAL -> {
                        bgBase = PawTheme.CORAL;
                        bgHover = PawTheme.CORAL_HOVER;
                        fgColor = Color.WHITE;
                    }
                    case SUNNY -> {
                        bgBase = PawTheme.SUNNY;
                        bgHover = PawTheme.SUNNY_HOVER;
                        fgColor = Color.WHITE;
                    }
                    case MINT, SUCCESS -> {
                        bgBase = PawTheme.MINT;
                        bgHover = PawTheme.MINT_HOVER;
                        fgColor = Color.WHITE;
                    }
                    case SKY, ACTION_BLUE -> {
                        bgBase = PawTheme.BLUE;
                        bgHover = new Color(29, 78, 216);
                        fgColor = Color.WHITE;
                    }
                    case LILAC -> {
                        bgBase = PawTheme.LILAC;
                        bgHover = PawTheme.LILAC_HOVER;
                        fgColor = Color.WHITE;
                    }
                    case INDIGO -> {
                        bgBase = PawTheme.INDIGO;
                        bgHover = PawTheme.INDIGO_HOVER;
                        fgColor = Color.WHITE;
                    }
                    case DANGER -> {
                        bgBase = PawTheme.DANGER;
                        bgHover = new Color(190, 18, 60);
                        fgColor = Color.WHITE;
                    }
                    case GHOST -> {
                        bgBase = new Color(0, 0, 0, 0);
                        bgHover = PawTheme.SURFACE_MUTED;
                        fgColor = PawTheme.TEXT_BODY;
                    }
                    default -> {
                        bgBase = PawTheme.SURFACE;
                        bgHover = PawTheme.SURFACE_MUTED;
                        fgColor = PawTheme.TEXT_BODY;
                        borderCol = isHovered ? PawTheme.BORDER_STRONG : PawTheme.BORDER_LIGHT;
                    }
                }

                if (!isEnabled()) {
                    bgBase = new Color(241, 245, 249);
                    fgColor = new Color(148, 163, 184);
                    borderCol = PawTheme.BORDER_LIGHT;
                }

                // Interpolate hover color
                Color currentBg = interpolateColor(bgBase, bgHover, hoverAlpha);
                if (isPressed) {
                    currentBg = bgHover.darker();
                }

                // Fill Background with soft rounded pill corners
                g2.setColor(currentBg);
                g2.fill(new RoundRectangle2D.Double(0, 0, w, h, arc, arc));

                // Optional Stroke Border
                if (borderCol != null) {
                    g2.setColor(borderCol);
                    g2.setStroke(new BasicStroke(1.2f));
                    g2.draw(new RoundRectangle2D.Double(0.6, 0.6, w - 1.2, h - 1.2, arc, arc));
                }

                setForeground(fgColor);
                super.paintComponent(g);
            } finally {
                g2.dispose();
            }
        }

        private static Color interpolateColor(Color c1, Color c2, float t) {
            float f = Math.max(0f, Math.min(1f, t));
            int r = (int) (c1.getRed() + f * (c2.getRed() - c1.getRed()));
            int g = (int) (c1.getGreen() + f * (c2.getGreen() - c1.getGreen()));
            int b = (int) (c1.getBlue() + f * (c2.getBlue() - c1.getBlue()));
            int a = (int) (c1.getAlpha() + f * (c2.getAlpha() - c1.getAlpha()));
            return new Color(r, g, b, a);
        }
    }

    /**
     * Modern Pill Status Badge for displaying state cleanly without ugly text boxes.
     */
    public static class StatusPill extends JPanel {
        private final Color bg;

        public StatusPill(String text, Color bg, Color fg, Color dotColor) {
            this.bg = bg;

            setOpaque(false);
            setLayout(new FlowLayout(FlowLayout.CENTER, 6, 3));
            setBorder(new EmptyBorder(3, 12, 3, 12));

            if (dotColor != null) {
                JLabel dot = new JLabel(PawIcons.dot(10, dotColor));
                add(dot);
            }

            JLabel lbl = new JLabel(text);
            lbl.setFont(PawTheme.FONT_BADGE);
            lbl.setForeground(fg);
            add(lbl);
        }

        public static StatusPill forSpecies(String species) {
            return fromStatus(species);
        }

        public static StatusPill fromStatus(String status) {
            if (status == null) status = "";
            String s = status.trim().toUpperCase();

            return switch (s) {
                case "CONFIRMED", "VERIFIED", "APPROVED", "ACTIVE" ->
                        new StatusPill(s, PawTheme.MINT_LIGHT, PawTheme.MINT_TEXT, PawTheme.MINT);
                case "COMPLETED", "SETTLED", "PAID" ->
                        new StatusPill(s, PawTheme.SKY_LIGHT, PawTheme.SKY_TEXT, PawTheme.SKY);
                case "PENDING", "UNVERIFIED", "WAITING" ->
                        new StatusPill(s, PawTheme.SUNNY_LIGHT, PawTheme.SUNNY_TEXT, PawTheme.SUNNY);
                case "CANCELLED", "REJECTED", "FAILED" ->
                        new StatusPill(s, PawTheme.DANGER_LIGHT, PawTheme.DANGER_TEXT, PawTheme.DANGER);
                case "ESCROW_HELD", "HELD" ->
                        new StatusPill(s, PawTheme.INDIGO_LIGHT, PawTheme.INDIGO_TEXT, PawTheme.INDIGO);
                case "DOG" ->
                        new StatusPill(s, PawTheme.SKY_LIGHT, PawTheme.SKY_TEXT, PawTheme.SKY);
                case "CAT" ->
                        new StatusPill(s, PawTheme.LILAC_LIGHT, PawTheme.LILAC_TEXT, PawTheme.LILAC);
                case "BIRD" ->
                        new StatusPill(s, PawTheme.MINT_LIGHT, PawTheme.MINT_TEXT, PawTheme.MINT);
                case "RABBIT" ->
                        new StatusPill(s, PawTheme.CORAL_LIGHT, PawTheme.CORAL_TEXT, PawTheme.CORAL);
                case "REPTILE" ->
                        new StatusPill(s, PawTheme.SUNNY_LIGHT, PawTheme.SUNNY_TEXT, PawTheme.SUNNY);
                default ->
                        new StatusPill(s.isEmpty() ? "UNKNOWN" : s, PawTheme.SURFACE_MUTED, PawTheme.TEXT_MUTED, PawTheme.TEXT_MUTED);
            };
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), getHeight(), getHeight()));
            } finally {
                g2.dispose();
            }
            super.paintComponent(g);
        }
    }

    /**
     * Clean, modern TableCellRenderer returning a modern StatusPill.
     */
    public static class StatusTableCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            String valStr = value != null ? value.toString() : "";
            StatusPill pill = StatusPill.fromStatus(valStr);

            JPanel holder = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 5));
            holder.setOpaque(isSelected);
            if (isSelected) {
                holder.setBackground(table.getSelectionBackground());
            } else {
                holder.setBackground(row % 2 == 0 ? PawTheme.SURFACE : new Color(250, 251, 254));
            }
            holder.add(pill);
            return holder;
        }
    }

    /**
     * Modern Card Surface panel with clean border, generous internal padding, and background.
     */
    public static class PawCard extends JPanel {
        private final int arc;
        private Color cardBg = PawTheme.SURFACE;
        private Color borderCol = PawTheme.BORDER_PASTEL;

        public PawCard(int arc) {
            this.arc = arc;
            setOpaque(false);
            setBorder(new EmptyBorder(18, 22, 18, 22));
        }

        public PawCard() {
            this(16);
        }

        public void setCardBackground(Color bg) {
            this.cardBg = bg;
            repaint();
        }

        public void setBackgroundTint(Color bg) {
            setCardBackground(bg);
        }

        public void setCardBorderColor(Color border) {
            this.borderCol = border;
            repaint();
        }

        public void setBorderTint(Color border) {
            setCardBorderColor(border);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth();
                int h = getHeight();

                g2.setColor(cardBg);
                g2.fill(new RoundRectangle2D.Double(0, 0, w, h, arc, arc));

                if (borderCol != null) {
                    g2.setColor(borderCol);
                    g2.setStroke(new BasicStroke(1.0f));
                    g2.draw(new RoundRectangle2D.Double(0.5, 0.5, w - 1, h - 1, arc, arc));
                }
            } finally {
                g2.dispose();
            }
            super.paintComponent(g);
        }
    }
}
