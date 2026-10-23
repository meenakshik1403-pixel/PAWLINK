package com.pawlink.vet.view;

import com.pawlink.vet.api.Session;
import com.pawlink.vet.base.BasePanel;
import com.pawlink.vet.base.Dialogs;
import com.pawlink.vet.base.PawComponents;
import com.pawlink.vet.base.PawIcons;
import com.pawlink.vet.base.PawTheme;
import com.pawlink.vet.base.Refreshable;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Modernized Desktop Shell for PawLink.
 * Features an artisanal, friendly, multi-coloured Midnight Indigo sidebar with scalable vector icons,
 * role-adaptive navigation items with custom cute color accents, active pill indicators,
 * and generous responsive padding across all desktop screen sizes.
 */
public class MainFrame extends JFrame {

    private static MainFrame currentInstance;

    private final Session session;
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentCards = new JPanel(cardLayout);
    private final JPanel navItemsContainer = new JPanel();
    private final List<NavButton> navButtons = new ArrayList<>();
    private final List<JComponent> tabComponents = new ArrayList<>();
    private JLabel currentViewIconLabel;
    private JLabel currentViewTitleLabel;
    private JPanel topContextBar;
    private int activeTabIndex = 0;

    public MainFrame() {
        super("PawLink - Pet Care Management System");
        this.session = Session.getInstance();
        currentInstance = this;
        initUI();
    }

    public static MainFrame getCurrentInstance() {
        return currentInstance;
    }

    private void initUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1260, 800);
        setMinimumSize(new Dimension(980, 620));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(PawTheme.CANVAS);

        // 1. Left Sleek Navigation Sidebar
        JPanel sidebar = createSidebar();
        add(sidebar, BorderLayout.WEST);

        // 2. Right Canvas Area (Top Context Bar + Card Content)
        JPanel rightCanvas = new JPanel(new BorderLayout());
        rightCanvas.setOpaque(false);

        JPanel topBar = createTopContextBar();
        rightCanvas.add(topBar, BorderLayout.NORTH);

        contentCards.setOpaque(false);
        rightCanvas.add(contentCards, BorderLayout.CENTER);

        add(rightCanvas, BorderLayout.CENTER);

        // 3. Register Role-Adaptive Navigation Panels
        setupRoleAdaptiveTabs();

        // Select initial tab
        if (!navButtons.isEmpty()) {
            selectTab(0);
        }

        toFront();
        requestFocus();
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                try {
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(PawTheme.SIDEBAR_BG);
                    g2.fillRect(0, 0, getWidth(), getHeight());

                    // Subtle playful top-right glow
                    g2.setColor(new Color(255, 107, 139, 12));
                    g2.fillOval(getWidth() - 80, -40, 160, 160);
                } finally {
                    g2.dispose();
                }
            }
        };
        sidebar.setPreferredSize(new Dimension(256, 800));
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, PawTheme.SIDEBAR_BORDER));

        // Top Sidebar Branding & Role Pill
        JPanel brandBox = new JPanel();
        brandBox.setLayout(new BoxLayout(brandBox, BoxLayout.Y_AXIS));
        brandBox.setOpaque(false);
        brandBox.setBorder(new EmptyBorder(26, 22, 20, 22));

        JPanel logoRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        logoRow.setOpaque(false);
        logoRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel logoIcon = new JLabel(PawIcons.paw(28, PawTheme.CORAL));
        logoRow.add(logoIcon);

        JLabel logoText = new JLabel("PawLink");
        logoText.setFont(PawTheme.FONT_HERO);
        logoText.setForeground(Color.WHITE);
        logoRow.add(logoText);
        brandBox.add(logoRow);

        brandBox.add(Box.createVerticalStrut(10));

        // Role Capsule Badge
        String roleStr = session.getRole() != null ? session.getRole().toUpperCase() : "GUEST";
        Color roleColor = getRoleBadgeColor(roleStr);
        JPanel rolePill = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 3)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                try {
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(roleColor.getRed(), roleColor.getGreen(), roleColor.getBlue(), 40));
                    g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), getHeight(), getHeight()));
                } finally {
                    g2.dispose();
                }
            }
        };
        rolePill.setOpaque(false);
        rolePill.setAlignmentX(Component.LEFT_ALIGNMENT);
        rolePill.setBorder(new EmptyBorder(2, 10, 2, 12));

        JLabel dot = new JLabel(PawIcons.dot(8, roleColor));
        rolePill.add(dot);

        JLabel roleLabel = new JLabel(getRoleDisplayName(roleStr));
        roleLabel.setFont(PawTheme.FONT_BADGE);
        roleLabel.setForeground(roleColor.brighter());
        rolePill.add(roleLabel);

        brandBox.add(rolePill);
        sidebar.add(brandBox, BorderLayout.NORTH);

        // Center: Navigation Items List
        navItemsContainer.setLayout(new BoxLayout(navItemsContainer, BoxLayout.Y_AXIS));
        navItemsContainer.setOpaque(false);
        navItemsContainer.setBorder(new EmptyBorder(8, 14, 14, 14));

        JScrollPane navScroll = new JScrollPane(navItemsContainer);
        navScroll.setOpaque(false);
        navScroll.getViewport().setOpaque(false);
        navScroll.setBorder(null);
        sidebar.add(navScroll, BorderLayout.CENTER);

        // Bottom User Profile & Sign Out Footer
        JPanel userFooter = createUserFooter();
        sidebar.add(userFooter, BorderLayout.SOUTH);

        return sidebar;
    }

    private JPanel createUserFooter() {
        JPanel footer = new JPanel();
        footer.setLayout(new BoxLayout(footer, BoxLayout.Y_AXIS));
        footer.setOpaque(false);
        footer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, PawTheme.SIDEBAR_BORDER),
                new EmptyBorder(16, 20, 16, 20)
        ));

        // User Avatar + Name Row
        JPanel userRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        userRow.setOpaque(false);
        userRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Circle Avatar with initials
        String userName = session.getName() != null ? session.getName() : "Anonymous";
        String initials = getInitials(userName);
        Color roleColor = getRoleBadgeColor(session.getRole());
        JPanel avatar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                try {
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(roleColor);
                    g2.fillOval(0, 0, getWidth(), getHeight());

                    g2.setColor(Color.WHITE);
                    g2.setFont(PawTheme.FONT_SMALL_BOLD);
                    FontMetrics fm = g2.getFontMetrics();
                    int tx = (getWidth() - fm.stringWidth(initials)) / 2;
                    int ty = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                    g2.drawString(initials, tx, ty);
                } finally {
                    g2.dispose();
                }
            }
        };
        avatar.setPreferredSize(new Dimension(36, 36));
        avatar.setOpaque(false);
        userRow.add(avatar);

        JPanel nameCol = new JPanel();
        nameCol.setLayout(new BoxLayout(nameCol, BoxLayout.Y_AXIS));
        nameCol.setOpaque(false);

        JLabel nameLbl = new JLabel(userName);
        nameLbl.setFont(PawTheme.FONT_BODY_BOLD);
        nameLbl.setForeground(new Color(241, 245, 249));
        nameCol.add(nameLbl);

        JLabel idLbl = new JLabel("User #" + session.getUserId());
        idLbl.setFont(PawTheme.FONT_SMALL);
        idLbl.setForeground(PawTheme.TEXT_SUBTLE);
        nameCol.add(idLbl);

        userRow.add(nameCol);
        footer.add(userRow);

        footer.add(Box.createVerticalStrut(14));

        // Sign Out Button
        PawComponents.PawButton logoutBtn = new PawComponents.PawButton("Sign Out", PawIcons.logout(14, Color.WHITE), PawComponents.ButtonStyle.DANGER);
        logoutBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        logoutBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        logoutBtn.addActionListener(e -> handleLogout());
        footer.add(logoutBtn);

        return footer;
    }

    private JPanel createTopContextBar() {
        topContextBar = new JPanel(new BorderLayout(16, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                try {
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    Color acc = (activeTabIndex >= 0 && activeTabIndex < navButtons.size())
                            ? navButtons.get(activeTabIndex).getAccentColor()
                            : PawTheme.PRIMARY;
                    Color bg = resolveLightColor(acc);

                    // 1. Fill entire strip with theme light background
                    g2.setColor(bg);
                    g2.fillRect(0, 0, getWidth(), getHeight());

                    // 2. Bottom accent stripe in vibrant theme color
                    g2.setColor(acc != null ? acc : PawTheme.PRIMARY);
                    g2.fillRect(0, getHeight() - 2, getWidth(), 2);
                } finally {
                    g2.dispose();
                }
                super.paintChildren(g);
            }
        };
        topContextBar.setOpaque(false);
        topContextBar.setBorder(new EmptyBorder(14, 28, 14, 28));

        JPanel leftTitle = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftTitle.setOpaque(false);

        currentViewIconLabel = new JLabel();
        leftTitle.add(currentViewIconLabel);

        currentViewTitleLabel = new JLabel("Portal Dashboard");
        currentViewTitleLabel.setFont(PawTheme.FONT_SECTION);
        currentViewTitleLabel.setForeground(PawTheme.TEXT_TITLE);
        leftTitle.add(currentViewTitleLabel);
        topContextBar.add(leftTitle, BorderLayout.WEST);

        return topContextBar;
    }

    private Color resolveLightColor(Color acc) {
        if (acc == null) return new Color(204, 251, 241);
        if (isColorMatch(acc, PawTheme.CORAL, PawTheme.CORAL_TEXT)) return new Color(255, 235, 238);
        if (isColorMatch(acc, PawTheme.SKY, PawTheme.SKY_TEXT)) return new Color(224, 242, 254);
        if (isColorMatch(acc, PawTheme.LILAC, PawTheme.LILAC_TEXT)) return new Color(243, 232, 255);
        if (isColorMatch(acc, PawTheme.SUNNY, PawTheme.SUNNY_TEXT)) return new Color(254, 243, 199);
        if (isColorMatch(acc, PawTheme.MINT, PawTheme.MINT_TEXT)) return new Color(209, 250, 229);
        if (isColorMatch(acc, PawTheme.INDIGO, PawTheme.INDIGO_TEXT)) return new Color(238, 242, 255);
        if (isColorMatch(acc, PawTheme.PEACH, PawTheme.PEACH_TEXT)) return new Color(255, 237, 213);
        return new Color(204, 251, 241); // Default Teal Mist
    }

    private Color resolveTextColor(Color acc) {
        if (acc == null) return new Color(15, 118, 110);
        if (isColorMatch(acc, PawTheme.CORAL, PawTheme.CORAL_TEXT)) return PawTheme.CORAL_TEXT;
        if (isColorMatch(acc, PawTheme.SKY, PawTheme.SKY_TEXT)) return PawTheme.SKY_TEXT;
        if (isColorMatch(acc, PawTheme.LILAC, PawTheme.LILAC_TEXT)) return PawTheme.LILAC_TEXT;
        if (isColorMatch(acc, PawTheme.SUNNY, PawTheme.SUNNY_TEXT)) return PawTheme.SUNNY_TEXT;
        if (isColorMatch(acc, PawTheme.MINT, PawTheme.MINT_TEXT)) return PawTheme.MINT_TEXT;
        if (isColorMatch(acc, PawTheme.INDIGO, PawTheme.INDIGO_TEXT)) return PawTheme.INDIGO_TEXT;
        if (isColorMatch(acc, PawTheme.PEACH, PawTheme.PEACH_TEXT)) return PawTheme.PEACH_TEXT;
        return new Color(15, 118, 110);
    }

    private boolean isColorMatch(Color input, Color... candidates) {
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

    private void setupRoleAdaptiveTabs() {
        String role = session.getRole() != null ? session.getRole().toUpperCase() : "VET";

        switch (role) {
            case "VET" -> {
                registerTab("Dashboard", PawIcons.stats(18, PawTheme.PRIMARY), PawTheme.PRIMARY, new DashboardPanel());
                registerTab("Appointments", PawIcons.calendar(18, PawTheme.SKY), PawTheme.SKY, new AppointmentsPanel());
                registerTab("Medical & Vaccines", PawIcons.medicalRecord(18, PawTheme.LILAC), PawTheme.LILAC, new RecordsPanel());
                registerTab("Patients & Pets", PawIcons.paw(18, PawTheme.CORAL), PawTheme.CORAL, new PetsPanel());
                registerTab("Reminders", PawIcons.bell(18, PawTheme.SUNNY), PawTheme.SUNNY, new RemindersPanel());
            }
            case "OWNER" -> {
                registerTab("My Pets", PawIcons.paw(18, PawTheme.CORAL), PawTheme.CORAL, new OwnerPetsPanel());
                registerTab("Vet Appointments", PawIcons.calendar(18, PawTheme.SKY), PawTheme.SKY, new AppointmentsPanel());
                registerTab("Find Pet Sitter", PawIcons.search(18, PawTheme.SUNNY), PawTheme.SUNNY, new FindSitterPanel());
                registerTab("Booked Pet Sitters", PawIcons.sitter(18, PawTheme.PEACH), PawTheme.PEACH, new SitterBookingsPanel());
                registerTab("Payment History", PawIcons.wallet(18, PawTheme.INDIGO), PawTheme.INDIGO, new OwnerPaymentsPanel());
            }
            case "SITTER" -> {
                registerTab("Assigned Jobs", PawIcons.sitter(18, PawTheme.SUNNY), PawTheme.SUNNY, new SitterBookingsPanel());
                registerTab("Pet Check-Ins", PawIcons.clock(18, PawTheme.CORAL), PawTheme.CORAL, new SitterCheckinsPanel());
                registerTab("Sitter Profile", PawIcons.user(18, PawTheme.LILAC), PawTheme.LILAC, new SitterProfilePanel());
            }
            case "ADMIN" -> {
                registerTab("Sitter Approvals", PawIcons.shield(18, PawTheme.MINT), PawTheme.MINT, new AdminSittersPanel());
                registerTab("Vet Approvals", PawIcons.stethoscope(18, PawTheme.SKY), PawTheme.SKY, new AdminVetsPanel());
                registerTab("Platform Statistics", PawIcons.stats(18, PawTheme.LILAC), PawTheme.LILAC, new StatsPanel());
            }
        }
    }

    /**
     * Registers a tab panel with a custom vector icon and themed accent color.
     */
    public void registerTab(String title, Icon icon, Color accentColor, JComponent component) {
        String cleanTitle = BasePanel.cleanEmoji(title);
        int index = navButtons.size();
        String cardName = "card_" + index;

        if (component instanceof BasePanel bp) {
            bp.setHeaderColor(accentColor);
        }

        contentCards.add(component, cardName);
        tabComponents.add(component);

        NavButton btn = new NavButton(cleanTitle, icon, accentColor, index);
        navButtons.add(btn);
        navItemsContainer.add(btn);
        navItemsContainer.add(Box.createVerticalStrut(6));
        navItemsContainer.revalidate();
        navItemsContainer.repaint();
    }

    public void registerTab(String title, Icon icon, JComponent component) {
        registerTab(title, icon, PawTheme.CORAL, component);
    }

    public void registerTab(String title, JComponent component) {
        Icon defaultIcon = resolveIconForTitle(title);
        Color accentColor = resolveColorForTitle(title);
        registerTab(title, defaultIcon, accentColor, component);
    }

    public void selectTab(int index) {
        if (index < 0 || index >= navButtons.size()) return;
        this.activeTabIndex = index;

        for (int i = 0; i < navButtons.size(); i++) {
            navButtons.get(i).setActive(i == index);
        }

        cardLayout.show(contentCards, "card_" + index);

        if (currentViewTitleLabel != null) {
            currentViewTitleLabel.setText(navButtons.get(index).getTitle());
            Color acc = navButtons.get(index).getAccentColor();
            currentViewTitleLabel.setForeground(resolveTextColor(acc));
        }

        if (currentViewIconLabel != null && index < navButtons.size()) {
            currentViewIconLabel.setIcon(navButtons.get(index).getIcon());
        }

        if (topContextBar != null) {
            topContextBar.repaint();
        }

        // Auto trigger refresh on opening that page
        if (index < tabComponents.size()) {
            JComponent comp = tabComponents.get(index);
            if (comp instanceof Refreshable r) {
                r.refresh();
            }
        }
    }

    private Icon resolveIconForTitle(String title) {
        String lower = title.toLowerCase();
        if (lower.contains("pet") || lower.contains("patient")) return PawIcons.paw(18, PawTheme.CORAL);
        if (lower.contains("appoint")) return PawIcons.calendar(18, PawTheme.SKY);
        if (lower.contains("record") || lower.contains("vaccin") || lower.contains("med")) return PawIcons.medicalRecord(18, PawTheme.LILAC);
        if (lower.contains("sitter") && lower.contains("find")) return PawIcons.search(18, PawTheme.SUNNY);
        if (lower.contains("sitter") || lower.contains("job")) return PawIcons.sitter(18, PawTheme.PEACH);
        if (lower.contains("pay") || lower.contains("bill") || lower.contains("wallet")) return PawIcons.wallet(18, PawTheme.INDIGO);
        if (lower.contains("remind") || lower.contains("alert")) return PawIcons.bell(18, PawTheme.MINT);
        if (lower.contains("check")) return PawIcons.clock(18, PawTheme.CORAL);
        if (lower.contains("stat") || lower.contains("dash")) return PawIcons.stats(18, PawTheme.SUNNY);
        if (lower.contains("approv")) return PawIcons.shield(18, PawTheme.MINT);
        return PawIcons.document(18, PawTheme.SKY);
    }

    private Color resolveColorForTitle(String title) {
        String lower = title.toLowerCase();
        if (lower.contains("pet") || lower.contains("patient")) return PawTheme.CORAL;
        if (lower.contains("appoint")) return PawTheme.SKY;
        if (lower.contains("record") || lower.contains("vaccin") || lower.contains("med")) return PawTheme.LILAC;
        if (lower.contains("sitter") && lower.contains("find")) return PawTheme.SUNNY;
        if (lower.contains("sitter") || lower.contains("job")) return PawTheme.PEACH;
        if (lower.contains("pay") || lower.contains("bill") || lower.contains("wallet")) return PawTheme.INDIGO;
        if (lower.contains("remind") || lower.contains("alert")) return PawTheme.MINT;
        if (lower.contains("check")) return PawTheme.CORAL;
        if (lower.contains("stat") || lower.contains("dash")) return PawTheme.SUNNY;
        if (lower.contains("approv")) return PawTheme.MINT;
        return PawTheme.CORAL;
    }

    private Color getRoleBadgeColor(String role) {
        if (role == null) return PawTheme.TEXT_MUTED;
        return switch (role.toUpperCase()) {
            case "VET" -> PawTheme.SKY;
            case "OWNER" -> PawTheme.CORAL;
            case "SITTER" -> PawTheme.SUNNY;
            case "ADMIN" -> PawTheme.LILAC;
            default -> PawTheme.MINT;
        };
    }

    private String getRoleDisplayName(String role) {
        if (role == null) return "User";
        return switch (role.toUpperCase()) {
            case "VET" -> "Veterinarian";
            case "OWNER" -> "Pet Owner";
            case "SITTER" -> "Pet Sitter";
            case "ADMIN" -> "Administrator";
            default -> role;
        };
    }

    private String getInitials(String name) {
        if (name == null || name.trim().isEmpty()) return "PL";
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        return ("" + parts[0].charAt(0) + parts[parts.length - 1].charAt(0)).toUpperCase();
    }

    private void handleLogout() {
        boolean confirmed = Dialogs.showConfirm(this, "Are you sure you want to sign out of PawLink?", "Confirm Sign Out");
        if (confirmed) {
            session.logout();
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
            dispose();
        }
    }

    /**
     * Interactive, multi-coloured modern navigation item for the sidebar.
     */
    private class NavButton extends JPanel {
        private final String title;
        private final Icon icon;
        private final Color accentColor;
        private boolean isActive = false;
        private boolean isHovered = false;

        public NavButton(String title, Icon icon, Color accentColor, int index) {
            this.title = title;
            this.icon = icon;
            this.accentColor = accentColor;

            setLayout(new BorderLayout(14, 0));
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
            setPreferredSize(new Dimension(228, 46));
            setBorder(new EmptyBorder(0, 16, 0, 16));

            JLabel iconLbl = new JLabel(icon);
            add(iconLbl, BorderLayout.WEST);

            JLabel textLbl = new JLabel(title);
            textLbl.setFont(PawTheme.FONT_BODY_BOLD);
            textLbl.setForeground(PawTheme.TEXT_SUBTLE);
            add(textLbl, BorderLayout.CENTER);

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    isHovered = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    isHovered = false;
                    repaint();
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    selectTab(index);
                }
            });
        }

        public String getTitle() {
            return title;
        }

        public Icon getIcon() {
            return icon;
        }

        public Color getAccentColor() {
            return accentColor;
        }

        public void setActive(boolean active) {
            this.isActive = active;
            for (Component c : getComponents()) {
                if (c instanceof JLabel l && l.getText() != null && !l.getText().isEmpty()) {
                    l.setForeground(active ? Color.WHITE : PawTheme.TEXT_SUBTLE);
                }
            }
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth();
                int h = getHeight();

                if (isActive) {
                    // Soft coloured tint background
                    g2.setColor(new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), 45));
                    g2.fill(new RoundRectangle2D.Double(0, 2, w, h - 4, 12, 12));

                    // Left Vertical Accent Pill (4px colorful highlight)
                    g2.setColor(accentColor);
                    g2.fill(new RoundRectangle2D.Double(0, 8, 4, h - 16, 4, 4));
                } else if (isHovered) {
                    g2.setColor(new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), 25));
                    g2.fill(new RoundRectangle2D.Double(0, 2, w, h - 4, 12, 12));
                }
            } finally {
                g2.dispose();
            }
            super.paintComponent(g);
        }
    }
}
