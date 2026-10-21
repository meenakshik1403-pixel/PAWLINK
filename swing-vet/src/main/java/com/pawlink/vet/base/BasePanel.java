package com.pawlink.vet.base;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Modern BasePanel for all screens in PawLink.
 * Automatically cleans any legacy emoji characters from title strings to prevent font tofu boxes.
 * Applies high-contrast modern typography, generous layout padding, vector icon support,
 * and a distinctly colored theme header strip across all views.
 */
public abstract class BasePanel extends JPanel implements Refreshable {

    protected JPanel headerPanel;
    protected JLabel titleLabel;
    protected JLabel subtitleLabel;
    protected JLabel iconLabel;
    protected JPanel iconBadge;
    protected JPanel accessoryHolder;
    protected JPanel contentPanel;

    private Color headerAccentColor = PawTheme.PRIMARY;
    private Color headerBgColor = PawTheme.PRIMARY_LIGHT;
    private Color headerTextColor = PawTheme.PRIMARY;
    private Color headerBorderColor = new Color(13, 148, 136, 80);

    public BasePanel(String title, String subtitle) {
        super(new BorderLayout(0, 18));
        setBorder(new EmptyBorder(20, 28, 24, 28));
        setBackground(PawTheme.CANVAS);

        initHeader(cleanEmoji(title), subtitle);
        initContent();
    }

    public BasePanel(String title) {
        this(title, null);
    }

    /**
     * Default implementation for auto-refreshing panels on tab display.
     * Subclasses override to invoke their respective load/fetch routines.
     */
    @Override
    public void refresh() {
        // Subclasses override
    }

    /**
     * Cleans legacy unicode emoji characters to prevent missing glyph tofu boxes.
     */
    public static String cleanEmoji(String text) {
        if (text == null) return "";
        // Removes unicode symbols / emojis that might cause box artifacts
        return text.replaceAll("[\\p{So}\\p{Cn}\\uFE0F\\u200D\\u2600-\\u27BF]", "").trim();
    }

    private void initHeader(String title, String subtitle) {
        headerPanel = new JPanel(new BorderLayout(16, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                try {
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    int w = getWidth();
                    int h = getHeight();
                    int arc = 18;

                    // 1. Fill rounded header strip background
                    if (headerBgColor != null) {
                        g2.setColor(headerBgColor);
                        g2.fillRoundRect(0, 0, w, h, arc, arc);
                    }

                    // 2. Left vertical accent indicator pill (gives clean structural anchor)
                    if (headerAccentColor != null) {
                        g2.setColor(headerAccentColor);
                        g2.fillRoundRect(0, 0, 6, h, 6, 6);
                    }

                    // 3. Subtle tinted outline border
                    if (headerBorderColor != null) {
                        g2.setColor(headerBorderColor);
                        g2.drawRoundRect(0, 0, w - 1, h - 1, arc, arc);
                    }
                } finally {
                    g2.dispose();
                }
                super.paintComponent(g);
            }
        };
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(14, 20, 14, 20));

        // Icon Badge container
        iconBadge = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                try {
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    int w = getWidth();
                    int h = getHeight();
                    g2.setColor(Color.WHITE);
                    g2.fillRoundRect(0, 0, w, h, 14, 14);

                    if (headerBorderColor != null) {
                        g2.setColor(headerBorderColor);
                        g2.drawRoundRect(0, 0, w - 1, h - 1, 14, 14);
                    }
                } finally {
                    g2.dispose();
                }
            }
        };
        iconBadge.setOpaque(false);
        iconBadge.setPreferredSize(new Dimension(44, 44));
        iconBadge.setMinimumSize(new Dimension(44, 44));

        iconLabel = new JLabel();
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        iconBadge.add(iconLabel);
        iconBadge.setVisible(false);
        headerPanel.add(iconBadge, BorderLayout.WEST);

        // Center: Title & Subtitle in vertical stack, centered vertically
        JPanel titleContainer = new JPanel();
        titleContainer.setLayout(new BoxLayout(titleContainer, BoxLayout.Y_AXIS));
        titleContainer.setOpaque(false);

        titleLabel = new JLabel(title);
        titleLabel.setFont(PawTheme.FONT_TITLE);
        titleLabel.setForeground(headerTextColor);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        titleContainer.add(titleLabel);

        if (subtitle != null && !subtitle.trim().isEmpty()) {
            titleContainer.add(Box.createVerticalStrut(3));
            subtitleLabel = new JLabel(subtitle);
            subtitleLabel.setFont(PawTheme.FONT_SUBTITLE);
            subtitleLabel.setForeground(new Color(71, 85, 105)); // Slate 600
            subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            titleContainer.add(subtitleLabel);
        }

        JPanel titleWrapper = new JPanel(new GridBagLayout());
        titleWrapper.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.weightx = 1.0;
        titleWrapper.add(titleContainer, gbc);

        headerPanel.add(titleWrapper, BorderLayout.CENTER);

        // Right: Header Accessory Holder
        accessoryHolder = new JPanel(new GridBagLayout());
        accessoryHolder.setOpaque(false);
        headerPanel.add(accessoryHolder, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);
    }

    public void setHeaderIcon(Icon icon) {
        if (icon != null) {
            iconLabel.setIcon(icon);
            iconBadge.setVisible(true);
        } else {
            iconLabel.setIcon(null);
            iconBadge.setVisible(false);
        }
        headerPanel.revalidate();
        headerPanel.repaint();
    }

    public void setHeaderColor(Color color) {
        if (color == null) return;
        applyHeaderPalette(color);
        if (titleLabel != null) {
            titleLabel.setForeground(headerTextColor);
        }
        if (headerPanel != null) {
            headerPanel.revalidate();
            headerPanel.repaint();
        }
    }

    public void setHeaderIcon(Icon icon, Color headerColor) {
        setHeaderColor(headerColor);
        setHeaderIcon(icon);
    }

    public void setHeaderColors(Color accentColor, Color bgColor, Color textColor) {
        this.headerAccentColor = accentColor;
        this.headerBgColor = bgColor;
        this.headerTextColor = textColor;
        this.headerBorderColor = new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), 90);
        if (titleLabel != null) {
            titleLabel.setForeground(textColor);
        }
        if (headerPanel != null) {
            headerPanel.revalidate();
            headerPanel.repaint();
        }
    }

    protected void applyHeaderPalette(Color color) {
        if (matchesColor(color, PawTheme.CORAL, PawTheme.CORAL_TEXT)) {
            headerAccentColor = PawTheme.CORAL;
            headerBgColor = new Color(255, 235, 238); // Soft Rose Blush (#FFEbee)
            headerTextColor = PawTheme.CORAL_TEXT; // Deep Berry Rose (#9F1239)
            headerBorderColor = new Color(251, 113, 133, 90);
        } else if (matchesColor(color, PawTheme.SKY, PawTheme.SKY_TEXT)) {
            headerAccentColor = PawTheme.SKY;
            headerBgColor = new Color(224, 242, 254); // Sky Mist (#E0F2FE)
            headerTextColor = PawTheme.SKY_TEXT; // Ocean Blue (#0369A1)
            headerBorderColor = new Color(56, 189, 248, 90);
        } else if (matchesColor(color, PawTheme.LILAC, PawTheme.LILAC_TEXT)) {
            headerAccentColor = PawTheme.LILAC;
            headerBgColor = new Color(243, 232, 255); // Lilac Mist (#F3E8FF)
            headerTextColor = PawTheme.LILAC_TEXT; // Royal Lilac (#7E22CE)
            headerBorderColor = new Color(168, 85, 247, 90);
        } else if (matchesColor(color, PawTheme.SUNNY, PawTheme.SUNNY_TEXT)) {
            headerAccentColor = PawTheme.SUNNY;
            headerBgColor = new Color(254, 243, 199); // Amber Gold Mist (#FEF3C7)
            headerTextColor = PawTheme.SUNNY_TEXT; // Amber Bark (#A16207)
            headerBorderColor = new Color(245, 158, 11, 90);
        } else if (matchesColor(color, PawTheme.MINT, PawTheme.MINT_TEXT)) {
            headerAccentColor = PawTheme.MINT;
            headerBgColor = new Color(209, 250, 229); // Mint Mist (#D1FAE5)
            headerTextColor = PawTheme.MINT_TEXT; // Forest Mint (#065F46)
            headerBorderColor = new Color(16, 185, 129, 90);
        } else if (matchesColor(color, PawTheme.INDIGO, PawTheme.INDIGO_TEXT)) {
            headerAccentColor = PawTheme.INDIGO;
            headerBgColor = new Color(238, 242, 255); // Periwinkle Mist (#EEF2FF)
            headerTextColor = PawTheme.INDIGO_TEXT; // Deep Periwinkle (#4338CA)
            headerBorderColor = new Color(99, 102, 241, 90);
        } else if (matchesColor(color, PawTheme.PEACH, PawTheme.PEACH_TEXT)) {
            headerAccentColor = PawTheme.PEACH;
            headerBgColor = new Color(255, 237, 213); // Peach Mist (#FFEDD5)
            headerTextColor = PawTheme.PEACH_TEXT; // Rust Peach (#C2410C)
            headerBorderColor = new Color(251, 146, 60, 90);
        } else {
            headerAccentColor = PawTheme.PRIMARY;
            headerBgColor = new Color(204, 251, 241); // Teal Mist (#CCFBF1)
            headerTextColor = new Color(15, 118, 110); // Deep Forest Teal (#0F766E)
            headerBorderColor = new Color(13, 148, 136, 90);
        }
    }

    private boolean matchesColor(Color input, Color... candidates) {
        if (input == null) return false;
        for (Color c : candidates) {
            if (c != null) {
                if (input.equals(c)) return true;
                int dr = Math.abs(input.getRed() - c.getRed());
                int dg = Math.abs(input.getGreen() - c.getGreen());
                int db = Math.abs(input.getBlue() - c.getBlue());
                if (dr < 25 && dg < 25 && db < 25) return true;
            }
        }
        return false;
    }

    private void initContent() {
        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setOpaque(false);
        add(contentPanel, BorderLayout.CENTER);
    }

    public void setHeaderAccessory(JComponent accessory) {
        if (accessoryHolder != null) {
            accessoryHolder.removeAll();
            if (accessory != null) {
                accessoryHolder.add(accessory);
            }
            headerPanel.revalidate();
            headerPanel.repaint();
        }
    }

    public void setContent(JComponent component) {
        contentPanel.removeAll();
        contentPanel.add(component, BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    public void showError(String msg) {
        Dialogs.showError(this, msg);
    }

    public void showInfo(String msg) {
        Dialogs.showInfo(this, msg);
    }

    public void showSuccess(String msg) {
        Dialogs.showSuccess(this, msg);
    }
}
