package com.pawlink.vet.view;

import com.pawlink.vet.api.ApiClient;
import com.pawlink.vet.api.Session;
import com.pawlink.vet.base.BasePanel;
import com.pawlink.vet.base.Dialogs;
import com.pawlink.vet.model.UserDTO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

/**
 * Main application window and shell for PawLink.
 * Holds a centralized {@link JTabbedPane} shell ready to register modular tabs from teammates.
 * Dynamically configures initial tabs and features according to the logged-in user's role.
 *
 * NOTE: As per team architectural constraints, only Person 1 (P1 - Core Architecture Lead)
 * edits MainFrame.java. Teammates plug in their respective panels via registerTab(...).
 */
public class MainFrame extends JFrame {

    private static MainFrame currentInstance;

    private final Session session;
    private JTabbedPane tabbedPane;
    private JLabel userProfileLabel;
    private JLabel roleBadgeLabel;

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
        setSize(1180, 750);
        setMinimumSize(new Dimension(950, 600));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // 1. Top Branded Header & User Status Bar
        JPanel topBar = createTopHeaderBar();
        add(topBar, BorderLayout.NORTH);

        // 2. Central Shell: JTabbedPane ready for teammate modules
        tabbedPane = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tabbedPane.setBorder(new EmptyBorder(5, 5, 5, 5));

        // 3. Register initial role-adaptive tab layout
        setupRoleAdaptiveTabs();

        add(tabbedPane, BorderLayout.CENTER);

        // 4. Bottom System Status Bar
        JPanel statusBar = createBottomStatusBar();
        add(statusBar, BorderLayout.SOUTH);

        toFront();
        requestFocus();
    }

    private JPanel createTopHeaderBar() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(30, 41, 59)); // Slate 800
        header.setBorder(new EmptyBorder(12, 20, 12, 20));

        // Left Branding
        JPanel brandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        brandPanel.setOpaque(false);

        JLabel logo = new JLabel("🐾 PawLink");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        logo.setForeground(Color.WHITE);
        brandPanel.add(logo);

        JLabel roleTitle = new JLabel("| " + getRolePortalSubtitle());
        roleTitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        roleTitle.setForeground(new Color(148, 163, 184));
        brandPanel.add(roleTitle);

        header.add(brandPanel, BorderLayout.WEST);

        // Right User Information & Actions
        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        userPanel.setOpaque(false);

        userProfileLabel = new JLabel("👤 " + (session.getName() != null ? session.getName() : "Anonymous"));
        userProfileLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        userProfileLabel.setForeground(new Color(241, 245, 249));
        userPanel.add(userProfileLabel);

        roleBadgeLabel = new JLabel(" " + session.getRole() + " ");
        roleBadgeLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        roleBadgeLabel.setOpaque(true);
        roleBadgeLabel.setBackground(getRoleBadgeColor(session.getRole()));
        roleBadgeLabel.setForeground(Color.WHITE);
        roleBadgeLabel.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
        userPanel.add(roleBadgeLabel);

        JButton logoutButton = new JButton("Logout");
        logoutButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        logoutButton.setBackground(new Color(239, 68, 68)); // Red
        logoutButton.setForeground(Color.WHITE);
        logoutButton.setFocusable(false);
        logoutButton.addActionListener(e -> handleLogout());
        userPanel.add(logoutButton);

        header.add(userPanel, BorderLayout.EAST);
        return header;
    }

    private JPanel createBottomStatusBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(new Color(241, 245, 249));
        bar.setBorder(new EmptyBorder(4, 15, 4, 15));

        JLabel connLabel = new JLabel("● Backend Connected: http://localhost:8080 | Active User ID: " + session.getUserId());
        connLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        connLabel.setForeground(new Color(100, 116, 139));
        bar.add(connLabel, BorderLayout.WEST);

        JLabel tipLabel = new JLabel("PawLink Modular Architecture (P1-P8 Team Shell)");
        tipLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        tipLabel.setForeground(new Color(148, 163, 184));
        bar.add(tipLabel, BorderLayout.EAST);

        return bar;
    }

    private String getRolePortalSubtitle() {
        String role = session.getRole();
        if (role == null) return "Desktop Client";
        return switch (role.toUpperCase()) {
            case "VET" -> "Veterinary Clinical Dashboard";
            case "OWNER" -> "Pet Owner Portal";
            case "SITTER" -> "Pet Sitter Workstation";
            case "ADMIN" -> "System Administration Console";
            default -> "Pet Care System";
        };
    }

    private Color getRoleBadgeColor(String role) {
        if (role == null) return new Color(100, 116, 139);
        return switch (role.toUpperCase()) {
            case "VET" -> new Color(13, 148, 136);     // Teal
            case "OWNER" -> new Color(37, 99, 235);    // Blue
            case "SITTER" -> new Color(217, 119, 6);   // Amber
            case "ADMIN" -> new Color(147, 51, 234);   // Purple
            default -> new Color(100, 116, 139);
        };
    }

    /**
     * Configures the initial tab shell depending on the authenticated role.
     */
    private void setupRoleAdaptiveTabs() {
        String role = session.getRole() != null ? session.getRole().toUpperCase() : "VET";

        switch (role) {
            case "VET" -> setupVetTabs();
            case "OWNER" -> setupOwnerTabs();
            case "SITTER" -> setupSitterTabs();
            case "ADMIN" -> setupAdminTabs();
            default -> setupVetTabs();
        }
    }

    /**
     * VET TABS:
     * - Tab 1: Dashboard (P7)
     * - Tab 2: Pets & Patients (P2)
     * - Tab 3: Medical Records & Vaccines (P3)
     * - Tab 4: Appointments (P4)
     * - Tab 5: Reminders & Alerts (P5)
     * - Tab 6: Clinic Statistics (P8)
     */
    private void setupVetTabs() {
        registerTab("📊 Dashboard", new DashboardPanel());
        registerTab("📅 Appointments", new AppointmentsPanel());
        registerTab("📋 Medical & Vaccines", new RecordsPanel());
        registerTab("🐕 Patients / Pets", new PetsPanel());
        registerTab("🔔 Reminders", new RemindersPanel());
    }

    /**
     * OWNER TABS:
     * - My Pets (P2)
     * - Book Appointment (P4)
     * - Book Sitter (P6 & P7)
     * - Reminders (P5)
     * - Payments (P7)
     */
    private void setupOwnerTabs() {
        registerTab("🐶 My Pets", new OwnerPetsPanel());

        registerTab("🏥 Vet Appointments", new AppointmentsPanel());

        registerTab("🏡 Find Pet Sitter", new FindSitterPanel());

        registerTab("🐾 Booked Pet Sitters", new SitterBookingsPanel());

        registerTab("💳 Payment History", new OwnerPaymentsPanel());
    }

    /**
     * SITTER TABS:
     * - My Bookings (P6)
     * - Check-Ins (P6)
     * - Sitter Profile (P6)
     */
    private void setupSitterTabs() {
        registerTab("📋 Assigned Jobs", new SitterBookingsPanel());

        registerTab("📸 Pet Check-Ins", new SitterCheckinsPanel());

        registerTab("👤 Sitter Profile", new SitterProfilePanel());
    }

    /**
     * ADMIN TABS:
     * - Sitter Approvals
     * - Vet Approvals
     * - Platform Statistics
     */
    private void setupAdminTabs() {
        registerTab("✅ Sitter Approvals", new AdminSittersPanel());
        registerTab("🩺 Vet Approvals", new AdminVetsPanel());
        registerTab("📊 Platform Statistics", new StatsPanel());
    }

    /**
     * Creates an informative integration placeholder for teammate modules.
     */
    private JPanel createModulePlaceholder(String title, String description, String owner, String apiEndpoints) {
        BasePanel panel = new BasePanel(title, "Owner: " + owner) {};

        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                new EmptyBorder(25, 30, 25, 30)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(6, 0, 6, 0);

        JLabel descLbl = new JLabel("<html><b>Module Purpose:</b> " + description + "</html>");
        descLbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        card.add(descLbl, gbc);

        gbc.gridy++;
        JLabel ownerLbl = new JLabel("<html><b>Assigned Teammate:</b> <font color='#4f46e5'>" + owner + "</font></html>");
        ownerLbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        card.add(ownerLbl, gbc);

        gbc.gridy++;
        JLabel apiLbl = new JLabel("<html><b>Connected API Endpoints:</b> <code>" + apiEndpoints + "</code></html>");
        apiLbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        card.add(apiLbl, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(15, 0, 5, 0);
        JLabel hookLbl = new JLabel("<html><i>Plug-in Instruction: Replace this placeholder by invoking <code>mainFrame.registerTab(...)</code> from your module panel.</i></html>");
        hookLbl.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        hookLbl.setForeground(new Color(100, 116, 139));
        card.add(hookLbl, gbc);

        panel.setContent(card);
        return panel;
    }

    /**
     * Public method allowing teammates to register their panels into the shell.
     *
     * @param title     Title to display on the tab
     * @param component The JPanel or JComponent to display in the tab
     */
    public void registerTab(String title, JComponent component) {
        tabbedPane.addTab(title, component);
    }

    /**
     * Public method allowing teammates to register their panels with an icon.
     */
    public void registerTab(String title, Icon icon, JComponent component) {
        tabbedPane.addTab(title, icon, component);
    }

    public JTabbedPane getTabbedPane() {
        return tabbedPane;
    }

    private void handleLogout() {
        boolean confirmed = Dialogs.showConfirm(this, "Are you sure you want to log out of PawLink?", "Confirm Logout");
        if (confirmed) {
            session.logout();
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
            dispose();
        }
    }
}
