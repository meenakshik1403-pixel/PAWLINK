package com.pawlink.vet.base;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

/**
 * Modern Design System & Theme Engine for PawLink.
 * Establishes a warm, multi-coloured, friendly pet-care aesthetic with soft candy pastels,
 * crisp slate typography, smooth micro-interactions, and generous padding for high responsiveness.
 */
public final class PawTheme {

    private PawTheme() {}

    // --- Vibrant & Cute Multi-Coloured Palette ---
    public static final Color PRIMARY = new Color(13, 148, 136);          // Clinical Teal (#0D9488)
    public static final Color PRIMARY_HOVER = new Color(15, 118, 110);    // Deep Teal (#0F766E)
    public static final Color PRIMARY_LIGHT = new Color(204, 251, 241);    // Soft Mint Tint (#CCFBF1)
    public static final Color PRIMARY_DARK = new Color(19, 78, 74);       // Deep Spruce (#134E4A)

    // Playful Coral Rose (Love, pets, heart accents)
    public static final Color CORAL = new Color(255, 107, 139);            // Bubblegum Coral (#FF6B8B)
    public static final Color CORAL_HOVER = new Color(244, 63, 94);        // Rose Coral (#F43F5E)
    public static final Color CORAL_LIGHT = new Color(255, 238, 242);      // Coral Mist (#FFEEF2)
    public static final Color CORAL_TEXT = new Color(199, 31, 69);         // Deep Coral (#C71F45)

    // Sunny Honey Amber (Joy, playtime, energy)
    public static final Color SUNNY = new Color(245, 158, 11);             // Warm Honey Amber (#F59E0B)
    public static final Color SUNNY_HOVER = new Color(217, 119, 6);        // Deep Honey (#D97706)
    public static final Color SUNNY_LIGHT = new Color(254, 243, 199);      // Creamy Honey (#FEF3C7)
    public static final Color SUNNY_TEXT = new Color(180, 83, 9);          // Deep Amber (#B45309)

    // Fresh Mint Sage (Health, vaccines, verified)
    public static final Color MINT = new Color(16, 185, 129);              // Fresh Mint (#10B981)
    public static final Color MINT_HOVER = new Color(5, 150, 105);         // Emerald Mint (#059669)
    public static final Color MINT_LIGHT = new Color(209, 250, 229);       // Mint Mist (#D1FAE5)
    public static final Color MINT_TEXT = new Color(6, 95, 70);            // Forest Mint (#065F46)

    // Sky Cloud Blue (Appointments, calm care)
    public static final Color SKY = new Color(56, 189, 248);               // Sky Blue (#38BDF8)
    public static final Color SKY_HOVER = new Color(2, 132, 199);          // Deep Sky (#0284C7)
    public static final Color SKY_LIGHT = new Color(224, 242, 254);        // Sky Mist (#E0F2FE)
    public static final Color SKY_TEXT = new Color(3, 105, 161);           // Ocean Blue (#0369A1)

    // Sweet Lavender / Lilac (Records, check-ins, medicine)
    public static final Color LILAC = new Color(168, 85, 247);             // Sweet Lilac (#A855F7)
    public static final Color LILAC_HOVER = new Color(147, 51, 234);       // Deep Lilac (#9333EA)
    public static final Color LILAC_LIGHT = new Color(243, 232, 255);      // Lilac Mist (#F3E8FF)
    public static final Color LILAC_TEXT = new Color(126, 34, 206);        // Royal Lilac (#7E22CE)

    // Soft Apricot Peach (Feeding, warmth, treats)
    public static final Color PEACH = new Color(251, 146, 60);             // Apricot Peach (#FB923C)
    public static final Color PEACH_HOVER = new Color(234, 88, 12);        // Tangerine Peach (#EA580C)
    public static final Color PEACH_LIGHT = new Color(255, 237, 213);      // Peach Mist (#FFEDD5)
    public static final Color PEACH_TEXT = new Color(194, 65, 12);         // Rust Peach (#C2410C)

    // Royal Periwinkle Indigo (Escrow, finance, admin)
    public static final Color INDIGO = new Color(99, 102, 241);            // Royal Indigo (#6366F1)
    public static final Color INDIGO_HOVER = new Color(79, 70, 229);       // Deep Indigo (#4F46E5)
    public static final Color INDIGO_LIGHT = new Color(238, 242, 255);     // Periwinkle Mist (#EEF2FF)
    public static final Color INDIGO_TEXT = new Color(67, 56, 202);        // Deep Periwinkle (#4338CA)

    // Legacy Aliases
    public static final Color AMBER = SUNNY;
    public static final Color AMBER_LIGHT = SUNNY_LIGHT;
    public static final Color BLUE = new Color(37, 99, 235);
    public static final Color BLUE_LIGHT = new Color(239, 246, 255);
    public static final Color SUCCESS = MINT;
    public static final Color SUCCESS_LIGHT = MINT_LIGHT;
    public static final Color SUCCESS_TEXT = MINT_TEXT;
    public static final Color DANGER = new Color(225, 29, 72);
    public static final Color DANGER_LIGHT = new Color(255, 241, 242);
    public static final Color DANGER_TEXT = new Color(159, 18, 57);
    public static final Color WARNING = PEACH;
    public static final Color WARNING_LIGHT = PEACH_LIGHT;
    public static final Color WARNING_TEXT = PEACH_TEXT;
    public static final Color INFO = SKY;
    public static final Color INFO_LIGHT = SKY_LIGHT;
    public static final Color INFO_TEXT = SKY_TEXT;

    // Neutral Surfaces & Canvas
    public static final Color CANVAS = new Color(248, 249, 253);           // Warm friendly canvas (#F8F9FD)
    public static final Color SURFACE = Color.WHITE;                       // Pure Card Surface (#FFFFFF)
    public static final Color SURFACE_MUTED = new Color(243, 244, 249);    // Soft Slate 100 (#F3F4F9)
    public static final Color SIDEBAR_BG = new Color(24, 24, 43);          // Warm Midnight Indigo (#18182B)
    public static final Color SIDEBAR_HOVER = new Color(38, 38, 65);       // Indigo Hover (#262641)
    public static final Color SIDEBAR_ACTIVE = new Color(48, 48, 80);      // Indigo Active (#303050)
    public static final Color SIDEBAR_BORDER = new Color(42, 42, 68);      // Soft Dark Divider (#2A2A44)

    // Borders & Dividers
    public static final Color BORDER_LIGHT = new Color(226, 232, 240);     // Slate 200 (#E2E8F0)
    public static final Color BORDER_STRONG = new Color(203, 213, 225);    // Slate 300 (#CBD5E1)
    public static final Color BORDER_PASTEL = new Color(235, 238, 246);    // Gentle card border (#EBEEF6)

    // High-Contrast Slate Typography
    public static final Color TEXT_TITLE = new Color(15, 23, 42);          // Slate 900 (#0F172A)
    public static final Color TEXT_BODY = new Color(51, 65, 85);           // Slate 700 (#334155)
    public static final Color TEXT_MUTED = new Color(100, 116, 139);       // Slate 500 (#64748B)
    public static final Color TEXT_SUBTLE = new Color(148, 163, 184);      // Slate 400 (#94A3B8)

    // --- Typography Hierarchy ---
    public static Font font(float size, int style) {
        String[] preferredFonts = {"Segoe UI Variable Display", "Segoe UI", "Inter", "San Francisco", "Helvetica Neue", "Dialog"};
        for (String family : preferredFonts) {
            Font f = new Font(family, style, (int) size);
            if (f.getFamily().equalsIgnoreCase(family)) {
                return f.deriveFont(style, size);
            }
        }
        return new Font(Font.SANS_SERIF, style, (int) size);
    }

    public static final Font FONT_HERO = font(26f, Font.BOLD);
    public static final Font FONT_TITLE = font(20f, Font.BOLD);
    public static final Font FONT_SECTION = font(16f, Font.BOLD);
    public static final Font FONT_SUBTITLE = font(13f, Font.PLAIN);
    public static final Font FONT_BODY = font(13f, Font.PLAIN);
    public static final Font FONT_BODY_BOLD = font(13f, Font.BOLD);
    public static final Font FONT_SMALL = font(11f, Font.PLAIN);
    public static final Font FONT_SMALL_BOLD = font(11f, Font.BOLD);
    public static final Font FONT_BADGE = font(11f, Font.BOLD);

    /**
     * Initializes global Look & Feel settings with FlatLaf modern UI properties.
     */
    public static void install() {
        try {
            FlatLightLaf.setup();

            // Set global UI defaults for smooth modern curves and typography
            UIManager.put("Button.arc", 14);
            UIManager.put("Component.arc", 12);
            UIManager.put("ProgressBar.arc", 12);
            UIManager.put("TextComponent.arc", 10);
            UIManager.put("ScrollBar.thumbArc", 8);
            UIManager.put("ScrollBar.width", 10);

            UIManager.put("Component.focusWidth", 1);
            UIManager.put("Component.innerFocusWidth", 0);
            UIManager.put("Button.innerFocusWidth", 0);

            UIManager.put("Table.selectionBackground", PRIMARY_LIGHT);
            UIManager.put("Table.selectionForeground", TEXT_TITLE);
            UIManager.put("Table.gridColor", BORDER_LIGHT);
            UIManager.put("Table.rowHeight", 44);

            UIManager.put("TableHeader.background", SURFACE_MUTED);
            UIManager.put("TableHeader.foreground", TEXT_BODY);
            UIManager.put("TableHeader.font", FONT_SMALL_BOLD);
            UIManager.put("TableHeader.separatorColor", BORDER_LIGHT);

            UIManager.put("defaultFont", FONT_BODY);
        } catch (Exception ex) {
            // Fallback gracefully
        }
    }

    /**
     * Applies standard rounded card border with soft line and generous padding.
     */
    public static Border cardBorder() {
        return new CompoundBorder(
                new LineBorder(BORDER_PASTEL, 1, true),
                new EmptyBorder(18, 22, 18, 22)
        );
    }

    public static Border subtleBorder(int top, int left, int bottom, int right) {
        return new CompoundBorder(
                new LineBorder(BORDER_LIGHT, 1, true),
                new EmptyBorder(top, left, bottom, right)
        );
    }

    /**
     * Styles a modern JTable with generous row padding, height, and clean borders.
     */
    public static void styleTable(JTable table) {
        table.setRowHeight(44);
        table.setFont(FONT_BODY);
        table.setForeground(TEXT_BODY);
        table.setBackground(SURFACE);
        table.setSelectionBackground(PRIMARY_LIGHT);
        table.setSelectionForeground(TEXT_TITLE);
        table.setShowGrid(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(new Color(241, 245, 249));
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Header Styling
        table.getTableHeader().setFont(FONT_SMALL_BOLD);
        table.getTableHeader().setBackground(new Color(241, 245, 249));
        table.getTableHeader().setForeground(new Color(71, 85, 105));
        table.getTableHeader().setPreferredSize(new Dimension(0, 40));
        table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_LIGHT));
        table.getTableHeader().setReorderingAllowed(false);

        // Default cell renderer with left padding margin
        DefaultTableCellRenderer defaultRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val, boolean isSel, boolean hasFoc, int row, int col) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, val, isSel, hasFoc, row, col);
                l.setBorder(new EmptyBorder(0, 14, 0, 14));
                return l;
            }
        };
        table.setDefaultRenderer(Object.class, defaultRenderer);
    }

    /**
     * Styles a text field with clean padding and modern border.
     */
    public static void styleField(JTextField field) {
        field.setFont(FONT_BODY);
        field.setPreferredSize(new Dimension(field.getPreferredSize().width, 38));
        field.setBorder(new CompoundBorder(
                new LineBorder(BORDER_STRONG, 1, true),
                new EmptyBorder(4, 12, 4, 12)
        ));
        field.putClientProperty(FlatClientProperties.STYLE, "arc: 10; borderColor: #CBD5E1; focusedBorderColor: #0D9488");
    }
}
