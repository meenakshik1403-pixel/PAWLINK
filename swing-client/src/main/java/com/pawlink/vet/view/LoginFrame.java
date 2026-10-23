package com.pawlink.vet.view;

import com.pawlink.vet.api.ApiClient;
import com.pawlink.vet.api.ApiException;
import com.pawlink.vet.api.Session;
import com.pawlink.vet.base.PawComponents;
import com.pawlink.vet.base.PawIcons;
import com.pawlink.vet.base.PawTheme;
import com.pawlink.vet.model.AuthResponseDTO;
import com.pawlink.vet.model.LoginRequestDTO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * Modernized, aesthetic Login Window for PawLink.
 * Features a dual-pane layout: Deep slate visual brand panel with vector illustration
 * on the left, and a clean tactile form card with instant quick-fill demo buttons on the right.
 */
public class LoginFrame extends JFrame {

    private JTextField emailField;
    private JPasswordField passwordField;
    private PawComponents.PawButton loginButton;
    private PawComponents.PawButton registerButton;
    private JPanel alertBox;
    private JLabel alertLabel;

    public LoginFrame() {
        super("PawLink - Secure Portal Login");
        initUI();
    }

    private void initUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(840, 560);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(new BorderLayout());
        getContentPane().setBackground(PawTheme.CANVAS);

        // 1. Left Brand Feature Panel (Deep Slate)
        JPanel leftBrandPanel = createBrandPanel();
        add(leftBrandPanel, BorderLayout.WEST);

        // 2. Right Form Container
        JPanel rightFormPanel = createFormPanel();
        add(rightFormPanel, BorderLayout.CENTER);
    }

    private JPanel createBrandPanel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                try {
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    // Midnight Indigo gradient with subtle warm tint
                    GradientPaint gp = new GradientPaint(
                            0, 0, PawTheme.SIDEBAR_BG,
                            0, getHeight(), new Color(15, 15, 26)
                    );
                    g2.setPaint(gp);
                    g2.fillRect(0, 0, getWidth(), getHeight());

                    // Decorative subtle vector circles
                    g2.setColor(new Color(255, 107, 107, 16));
                    g2.fillOval(-40, getHeight() - 140, 220, 220);
                    g2.setColor(new Color(138, 92, 246, 18));
                    g2.fillOval(getWidth() - 90, -30, 180, 180);
                } finally {
                    g2.dispose();
                }
            }
        };

        panel.setPreferredSize(new Dimension(320, 560));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(44, 30, 36, 30));

        // Brand Icon & Title
        JLabel brandIcon = new JLabel(PawIcons.paw(48, PawTheme.CORAL));
        brandIcon.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(brandIcon);
        panel.add(Box.createVerticalStrut(14));

        JLabel brandTitle = new JLabel("PawLink");
        brandTitle.setFont(PawTheme.FONT_HERO);
        brandTitle.setForeground(Color.WHITE);
        brandTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(brandTitle);

        JLabel brandSub = new JLabel("Connected Pet Care Platform");
        brandSub.setFont(PawTheme.FONT_SUBTITLE);
        brandSub.setForeground(PawTheme.TEXT_SUBTLE);
        brandSub.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(brandSub);

        panel.add(Box.createVerticalStrut(34));

        // Value Proposition Bullet Items with crisp vector icons & candy pastel accents
        panel.add(createFeatureItem("Multi-Role Portals", "Owner, Vet, Sitter, Admin", PawIcons.user(16, PawTheme.CORAL)));
        panel.add(Box.createVerticalStrut(16));
        panel.add(createFeatureItem("Clinical Records", "Medical & vaccine timelines", PawIcons.stethoscope(16, PawTheme.SKY)));
        panel.add(Box.createVerticalStrut(16));
        panel.add(createFeatureItem("Verified Network", "Document reviewed sitters & vets", PawIcons.shield(16, PawTheme.SUNNY)));
        panel.add(Box.createVerticalStrut(16));
        panel.add(createFeatureItem("Direct Settlement", "End-session automatic release (₹)", PawIcons.wallet(16, PawTheme.MINT)));

        panel.add(Box.createVerticalGlue());

        // Platform runtime footer tag
        JLabel footerTag = new JLabel("JDK 25 LTS · Spring Boot 3 · REST :8080");
        footerTag.setFont(PawTheme.FONT_SMALL);
        footerTag.setForeground(new Color(100, 116, 139));
        footerTag.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(footerTag);

        return panel;
    }

    private JPanel createFeatureItem(String title, String desc, Icon icon) {
        JPanel item = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        item.setOpaque(false);
        item.setAlignmentX(Component.LEFT_ALIGNMENT);

        item.add(new JLabel(icon));

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);

        JLabel t = new JLabel(title);
        t.setFont(PawTheme.FONT_BODY_BOLD);
        t.setForeground(new Color(241, 245, 249));
        textPanel.add(t);

        JLabel d = new JLabel(desc);
        d.setFont(PawTheme.FONT_SMALL);
        d.setForeground(new Color(148, 163, 184));
        textPanel.add(d);

        item.add(textPanel);
        return item;
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(PawTheme.SURFACE);
        panel.setBorder(new EmptyBorder(36, 40, 24, 40));

        // Form Header
        JLabel welcomeTitle = new JLabel("Welcome Back");
        welcomeTitle.setFont(PawTheme.FONT_TITLE);
        welcomeTitle.setForeground(PawTheme.TEXT_TITLE);
        welcomeTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(welcomeTitle);

        JLabel welcomeSub = new JLabel("Sign in with your registered account credentials.");
        welcomeSub.setFont(PawTheme.FONT_SUBTITLE);
        welcomeSub.setForeground(PawTheme.TEXT_MUTED);
        welcomeSub.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(welcomeSub);

        panel.add(Box.createVerticalStrut(20));

        // Inline Alert Box (replaces jarring plain error text)
        alertBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 8, 8));
                g2.dispose();
            }
        };
        alertBox.setOpaque(false);
        alertBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        alertBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        alertBox.setVisible(false);

        alertLabel = new JLabel();
        alertLabel.setFont(PawTheme.FONT_SMALL_BOLD);
        alertBox.add(alertLabel);
        panel.add(alertBox);

        panel.add(Box.createVerticalStrut(12));

        // Email Address Field
        JLabel emailLbl = new JLabel("Email Address");
        emailLbl.setFont(PawTheme.FONT_SMALL_BOLD);
        emailLbl.setForeground(PawTheme.TEXT_BODY);
        emailLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(emailLbl);
        panel.add(Box.createVerticalStrut(4));

        emailField = new JTextField("vet.sreekumar@pawlink.com");
        PawTheme.styleField(emailField);
        emailField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        emailField.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(emailField);

        panel.add(Box.createVerticalStrut(14));

        // Password Field
        JLabel passLbl = new JLabel("Password");
        passLbl.setFont(PawTheme.FONT_SMALL_BOLD);
        passLbl.setForeground(PawTheme.TEXT_BODY);
        passLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(passLbl);
        panel.add(Box.createVerticalStrut(4));

        passwordField = new JPasswordField("password123");
        passwordField.setFont(PawTheme.FONT_BODY);
        passwordField.setPreferredSize(new Dimension(passwordField.getPreferredSize().width, 38));
        passwordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        passwordField.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(passwordField);

        KeyAdapter enterSubmit = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    handleLogin();
                }
            }
        };
        emailField.addKeyListener(enterSubmit);
        passwordField.addKeyListener(enterSubmit);

        panel.add(Box.createVerticalStrut(18));

        // Action Buttons: Sign In (Coral) & Register (Secondary)
        JPanel actionRow = new JPanel(new GridLayout(1, 2, 14, 0));
        actionRow.setOpaque(false);
        actionRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        actionRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        registerButton = new PawComponents.PawButton("Create Account", PawIcons.user(16, PawTheme.TEXT_BODY), PawComponents.ButtonStyle.SECONDARY);
        registerButton.addActionListener(e -> handleOpenRegister());

        loginButton = new PawComponents.PawButton("Sign In", PawIcons.check(16, Color.WHITE), PawComponents.ButtonStyle.CORAL);
        loginButton.addActionListener(e -> handleLogin());

        actionRow.add(registerButton);
        actionRow.add(loginButton);
        panel.add(actionRow);

        panel.add(Box.createVerticalStrut(26));

        // Quick Demo Fill Toolstrip
        JLabel demoHead = new JLabel("Quick Demo Access:");
        demoHead.setFont(PawTheme.FONT_SMALL_BOLD);
        demoHead.setForeground(PawTheme.TEXT_MUTED);
        demoHead.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(demoHead);
        panel.add(Box.createVerticalStrut(10));

        JPanel demoGroup = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        demoGroup.setOpaque(false);
        demoGroup.setAlignmentX(Component.LEFT_ALIGNMENT);

        demoGroup.add(createDemoPill("Dr. Sreekumar (Vet)", "vet.sreekumar@pawlink.com", "password123", PawTheme.SKY_LIGHT, PawTheme.SKY_TEXT, PawTheme.SKY, PawIcons.stethoscope(13, PawTheme.SKY_TEXT)));
        demoGroup.add(createDemoPill("John (Owner)", "owner.john@pawlink.com", "password123", PawTheme.CORAL_LIGHT, PawTheme.CORAL_TEXT, PawTheme.CORAL, PawIcons.paw(13, PawTheme.CORAL_TEXT)));
        demoGroup.add(createDemoPill("Emily (Sitter)", "sitter.emily@pawlink.com", "password123", PawTheme.SUNNY_LIGHT, PawTheme.SUNNY_TEXT, PawTheme.SUNNY, PawIcons.sitter(13, PawTheme.SUNNY_TEXT)));
        demoGroup.add(createDemoPill("Admin", "admin@pawlink.com", "AdminSecurePass2026!", PawTheme.LILAC_LIGHT, PawTheme.LILAC_TEXT, PawTheme.LILAC, PawIcons.shield(13, PawTheme.LILAC_TEXT)));

        panel.add(demoGroup);

        return panel;
    }

    private JButton createDemoPill(String label, String email, String pass, Color bg, Color fg, Color border, Icon icon) {
        JButton btn = new JButton(label, icon);
        btn.setFont(PawTheme.FONT_SMALL_BOLD);
        btn.setForeground(fg);
        btn.setBackground(bg);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(true);
        btn.setIconTextGap(6);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(border.getRed(), border.getGreen(), border.getBlue(), 120), 1, true),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> {
            emailField.setText(email);
            passwordField.setText(pass);
            showAlert("Filled credentials for " + label, false);
        });
        return btn;
    }

    private void showAlert(String msg, boolean isError) {
        alertBox.setVisible(true);
        alertBox.setBackground(isError ? PawTheme.DANGER_LIGHT : PawTheme.SUCCESS_LIGHT);
        alertLabel.setForeground(isError ? PawTheme.DANGER_TEXT : PawTheme.SUCCESS_TEXT);
        alertLabel.setText(msg);
        alertLabel.setIcon(isError ? PawIcons.cross(14, PawTheme.DANGER) : PawIcons.check(14, PawTheme.SUCCESS));
        alertBox.revalidate();
        alertBox.repaint();
    }

    private void handleOpenRegister() {
        RegisterDialog dialog = new RegisterDialog(this);
        dialog.setVisible(true);
        if (dialog.getRegisteredEmail() != null) {
            emailField.setText(dialog.getRegisteredEmail());
            passwordField.setText("");
            passwordField.requestFocusInWindow();
            showAlert("Registration submitted! If approved, enter your password.", false);
        }
    }

    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (email.isEmpty() || password.isEmpty()) {
            showAlert("Please enter both email address and password.", true);
            return;
        }

        loginButton.setEnabled(false);
        loginButton.setText("Signing In...");

        SwingWorker<AuthResponseDTO, Void> worker = new SwingWorker<>() {
            @Override
            protected AuthResponseDTO doInBackground() throws Exception {
                LoginRequestDTO req = new LoginRequestDTO(email, password);
                return ApiClient.getInstance().post("/api/auth/login", req, AuthResponseDTO.class);
            }

            @Override
            protected void done() {
                loginButton.setEnabled(true);
                loginButton.setText("Sign In");

                try {
                    AuthResponseDTO response = get();
                    Session.getInstance().startSession(
                            response.getId(),
                            response.getName(),
                            response.getEmail(),
                            response.getRole()
                    );

                    MainFrame mainFrame = new MainFrame();
                    mainFrame.setVisible(true);
                    dispose();
                } catch (Exception ex) {
                    String msg = ex.getMessage();
                    if (ex.getCause() instanceof ApiException apiEx) {
                        msg = apiEx.getMessage();
                    } else if (ex.getCause() != null) {
                        msg = ex.getCause().getMessage();
                    }
                    showAlert("Authentication failed: " + msg, true);
                }
            }
        };
        worker.execute();
    }
}
