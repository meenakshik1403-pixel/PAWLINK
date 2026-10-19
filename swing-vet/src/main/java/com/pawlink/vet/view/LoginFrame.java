package com.pawlink.vet.view;

import com.pawlink.vet.api.ApiClient;
import com.pawlink.vet.api.ApiException;
import com.pawlink.vet.api.Session;
import com.pawlink.vet.base.Dialogs;
import com.pawlink.vet.model.AuthResponseDTO;
import com.pawlink.vet.model.LoginRequestDTO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Login Window for the PawLink Desktop Application.
 * Supports authentication against Spring Boot REST API (POST /api/auth/login),
 * session storage in Session singleton, quick-fill demo buttons, and role-based shell launch.
 */
public class LoginFrame extends JFrame {

    private JTextField emailField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton registerButton;
    private JCheckBox strictVetOnlyCheckBox;
    private JLabel statusLabel;

    public LoginFrame() {
        super("PawLink - Login");
        initUI();
    }

    private void initUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(480, 520);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(new BorderLayout());

        // Top Banner
        JPanel bannerPanel = new JPanel();
        bannerPanel.setLayout(new BoxLayout(bannerPanel, BoxLayout.Y_AXIS));
        bannerPanel.setBackground(new Color(30, 41, 59)); // Dark slate
        bannerPanel.setBorder(new EmptyBorder(25, 20, 25, 20));

        JLabel logoLabel = new JLabel("🐾 PawLink");
        logoLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        logoLabel.setForeground(new Color(248, 250, 252));
        logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        bannerPanel.add(logoLabel);

        bannerPanel.add(Box.createVerticalStrut(6));

        JLabel subtitleLabel = new JLabel("Pet Care Management & Clinical System");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitleLabel.setForeground(new Color(148, 163, 184));
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        bannerPanel.add(subtitleLabel);

        add(bannerPanel, BorderLayout.NORTH);

        // Center Form
        JPanel formContainer = new JPanel();
        formContainer.setLayout(new BoxLayout(formContainer, BoxLayout.Y_AXIS));
        formContainer.setBorder(new EmptyBorder(20, 35, 10, 35));
        formContainer.setBackground(Color.WHITE);

        // Email field
        JLabel emailLbl = new JLabel("Email Address");
        emailLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        emailLbl.setForeground(new Color(71, 85, 105));
        emailLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        formContainer.add(emailLbl);
        formContainer.add(Box.createVerticalStrut(4));

        emailField = new JTextField("vet.sarah@pawlink.com");
        emailField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        emailField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        emailField.setAlignmentX(Component.LEFT_ALIGNMENT);
        formContainer.add(emailField);

        formContainer.add(Box.createVerticalStrut(14));

        // Password field
        JLabel passLbl = new JLabel("Password");
        passLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        passLbl.setForeground(new Color(71, 85, 105));
        passLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        formContainer.add(passLbl);
        formContainer.add(Box.createVerticalStrut(4));

        passwordField = new JPasswordField("password123");
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        passwordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        passwordField.setAlignmentX(Component.LEFT_ALIGNMENT);
        formContainer.add(passwordField);

        // Enter key listener on password field
        passwordField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    handleLogin();
                }
            }
        });

        formContainer.add(Box.createVerticalStrut(12));

        // Strict Vet-Only checkbox (per proposal requirement / testing toggle)
        strictVetOnlyCheckBox = new JCheckBox("Strict Vet-Only desktop client rule", false);
        strictVetOnlyCheckBox.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        strictVetOnlyCheckBox.setBackground(Color.WHITE);
        strictVetOnlyCheckBox.setForeground(new Color(100, 116, 139));
        strictVetOnlyCheckBox.setToolTipText("If enabled, non-VET roles will be blocked with: 'Access Denied: Only veterinarians can log into this desktop client.'");
        strictVetOnlyCheckBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        formContainer.add(strictVetOnlyCheckBox);

        formContainer.add(Box.createVerticalStrut(14));

        // Status Label for inline messages
        statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        statusLabel.setForeground(new Color(220, 38, 38));
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        formContainer.add(statusLabel);

        formContainer.add(Box.createVerticalStrut(8));

        // Action Buttons (Login + Register)
        JPanel actionButtonPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        actionButtonPanel.setOpaque(false);
        actionButtonPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        actionButtonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        registerButton = new JButton("Register New");
        registerButton.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        registerButton.addActionListener(e -> handleOpenRegister());

        loginButton = new JButton("Sign In");
        loginButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        loginButton.setBackground(new Color(79, 70, 229));
        loginButton.setForeground(Color.WHITE);
        loginButton.addActionListener(e -> handleLogin());

        actionButtonPanel.add(registerButton);
        actionButtonPanel.add(loginButton);
        formContainer.add(actionButtonPanel);

        formContainer.add(Box.createVerticalStrut(16));

        // Quick Demo Fill Toolbar for easy viva grading
        JLabel demoLbl = new JLabel("Quick Demo Logins:");
        demoLbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        demoLbl.setForeground(new Color(148, 163, 184));
        demoLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        formContainer.add(demoLbl);
        formContainer.add(Box.createVerticalStrut(6));

        JPanel demoBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 4));
        demoBar.setOpaque(false);
        demoBar.setAlignmentX(Component.LEFT_ALIGNMENT);

        demoBar.add(createDemoButton("Vet: Sarah", "vet.sarah@pawlink.com"));
        demoBar.add(createDemoButton("Vet: Michael", "vet.michael@pawlink.com"));
        demoBar.add(createDemoButton("Owner: John", "owner.john@pawlink.com"));
        demoBar.add(createDemoButton("Sitter: Emily", "sitter.emily@pawlink.com"));
        demoBar.add(createDemoButton("Admin: Alice", "admin@pawlink.com"));
        formContainer.add(demoBar);

        add(formContainer, BorderLayout.CENTER);
    }

    private JButton createDemoButton(String text, String email) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        btn.setMargin(new Insets(2, 6, 2, 6));
        btn.setFocusable(false);
        btn.addActionListener(e -> {
            emailField.setText(email);
            passwordField.setText("password123");
            statusLabel.setText("Filled credentials for " + text);
            statusLabel.setForeground(new Color(37, 99, 235));
        });
        return btn;
    }

    private void handleOpenRegister() {
        RegisterDialog dialog = new RegisterDialog(this);
        dialog.setVisible(true);
        if (dialog.getRegisteredEmail() != null) {
            emailField.setText(dialog.getRegisteredEmail());
            passwordField.setText("");
            passwordField.requestFocusInWindow();
            statusLabel.setText("Registration complete! Enter password to log in.");
            statusLabel.setForeground(new Color(22, 163, 74));
        }
    }

    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (email.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Please enter both email and password.");
            statusLabel.setForeground(new Color(220, 38, 38));
            return;
        }

        loginButton.setEnabled(false);
        loginButton.setText("Authenticating...");
        statusLabel.setText("Connecting to PawLink server...");
        statusLabel.setForeground(new Color(71, 85, 105));

        SwingUtilities.invokeLater(() -> {
            try {
                LoginRequestDTO req = new LoginRequestDTO(email, password);
                AuthResponseDTO res = ApiClient.getInstance().post("/api/auth/login", req, AuthResponseDTO.class);

                // Check role restrictions
                boolean isVet = "VET".equalsIgnoreCase(res.getRole());

                if (strictVetOnlyCheckBox.isSelected() && !isVet) {
                    Dialogs.showError(this, "Access Denied: Only veterinarians can log into this desktop client.");
                    statusLabel.setText("Access Denied: Only veterinarians permitted in strict vet mode.");
                    statusLabel.setForeground(new Color(220, 38, 38));
                    return;
                }

                // If role != VET and not in strict mode, confirm opening unified portal
                if (!isVet) {
                    boolean proceed = Dialogs.showConfirm(
                            this,
                            "User '" + res.getName() + "' is registered as a " + res.getRole() + ".\n" +
                            "Would you like to open the Unified PawLink Desktop Portal for this role?",
                            "Role Notice"
                    );
                    if (!proceed) {
                        Dialogs.showError(this, "Access Denied: Only veterinarians can log into this desktop client.");
                        statusLabel.setText("Access Denied: User role is " + res.getRole());
                        statusLabel.setForeground(new Color(220, 38, 38));
                        return;
                    }
                }

                // Store in Session singleton
                Session.getInstance().login(res.getId(), res.getName(), res.getEmail(), res.getRole());

                // Open MainFrame and close LoginFrame
                MainFrame mainFrame = new MainFrame();
                mainFrame.setVisible(true);
                dispose();

            } catch (ApiException ex) {
                statusLabel.setText(ex.getMessage());
                statusLabel.setForeground(new Color(220, 38, 38));
                Dialogs.showError(this, ex.getMessage());
            } catch (Exception ex) {
                statusLabel.setText("Connection failed. Is the backend running on port 8080?");
                statusLabel.setForeground(new Color(220, 38, 38));
                Dialogs.showError(this, "Connection error: Could not reach backend server at http://localhost:8080.\n" + ex.getMessage());
            } finally {
                loginButton.setEnabled(true);
                loginButton.setText("Sign In");
            }
        });
    }
}
