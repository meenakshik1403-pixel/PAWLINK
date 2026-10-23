package com.pawlink.vet.view;

import com.pawlink.vet.api.ApiClient;
import com.pawlink.vet.api.ApiException;
import com.pawlink.vet.base.Dialogs;
import com.pawlink.vet.base.PawComponents;
import com.pawlink.vet.base.PawIcons;
import com.pawlink.vet.base.PawTheme;
import com.pawlink.vet.model.AuthResponseDTO;
import com.pawlink.vet.model.RegisterRequestDTO;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Registration Dialog for creating new Pet Owner, Pet Sitter, or Veterinarian accounts.
 * Displays user-friendly role names: "Pet Owner", "Pet Sitter", "Vet".
 * Requires strict 10-digit phone with +91 prefix.
 * Requires verification documents (ID Card, Qualifications) for Sitters and Vets.
 * Modernized with pure Java2D vector icons and tactile micro-animations.
 */
public class RegisterDialog extends JDialog {

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    private JTextField nameField;
    private JTextField emailField;
    private JPasswordField passwordField;
    private JTextField phoneDigitsField;
    private JComboBox<String> roleComboBox;

    // Document Upload components for Sitter & Vet
    private JPanel documentsPanel;
    private JLabel idCardLabel;
    private JLabel idCardFileLabel;
    private File selectedIdCardFile;

    private JLabel qualLabel;
    private JLabel qualFileLabel;
    private File selectedQualFile;

    private PawComponents.PawButton registerButton;
    private PawComponents.PawButton cancelButton;

    private String registeredEmail;

    public RegisterDialog(Frame owner) {
        super(owner, "PawLink - Register Account", true);
        initUI();
    }

    private void initUI() {
        setSize(580, 540);
        setLocationRelativeTo(getOwner());
        setResizable(false);
        setLayout(new BorderLayout());

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout(14, 0));
        headerPanel.setBackground(PawTheme.SIDEBAR_BG);
        headerPanel.setBorder(new EmptyBorder(18, 24, 18, 24));

        JLabel iconLabel = new JLabel(PawIcons.user(26, PawTheme.CORAL));
        headerPanel.add(iconLabel, BorderLayout.WEST);

        JPanel titleBlock = new JPanel(new GridLayout(2, 1, 0, 4));
        titleBlock.setOpaque(false);

        JLabel titleLabel = new JLabel("Create a New PawLink Account");
        titleLabel.setFont(PawTheme.FONT_TITLE);
        titleLabel.setForeground(Color.WHITE);
        titleBlock.add(titleLabel);

        JLabel subtitleLabel = new JLabel("Join as a Pet Owner, Pet Sitter, or Veterinarian");
        subtitleLabel.setFont(PawTheme.FONT_SMALL);
        subtitleLabel.setForeground(PawTheme.TEXT_SUBTLE);
        titleBlock.add(subtitleLabel);

        headerPanel.add(titleBlock, BorderLayout.CENTER);
        add(headerPanel, BorderLayout.NORTH);

        // Form Fields
        JPanel mainContentPanel = new JPanel();
        mainContentPanel.setLayout(new BoxLayout(mainContentPanel, BoxLayout.Y_AXIS));
        mainContentPanel.setBorder(new EmptyBorder(18, 26, 18, 26));
        mainContentPanel.setBackground(PawTheme.CANVAS);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 6, 8, 6);

        // Name
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.32;
        JLabel nameLbl = new JLabel("Full Name:");
        nameLbl.setFont(PawTheme.FONT_BODY_BOLD);
        nameLbl.setForeground(PawTheme.TEXT_BODY);
        formPanel.add(nameLbl, gbc);

        gbc.gridx = 1; gbc.weightx = 0.68;
        nameField = new JTextField(18);
        PawTheme.styleField(nameField);
        formPanel.add(nameField, gbc);

        // Email
        gbc.gridx = 0; gbc.gridy = 1;
        JLabel emailLbl = new JLabel("Email Address:");
        emailLbl.setFont(PawTheme.FONT_BODY_BOLD);
        emailLbl.setForeground(PawTheme.TEXT_BODY);
        formPanel.add(emailLbl, gbc);

        gbc.gridx = 1;
        emailField = new JTextField(18);
        PawTheme.styleField(emailField);
        formPanel.add(emailField, gbc);

        // Password
        gbc.gridx = 0; gbc.gridy = 2;
        JLabel passLbl = new JLabel("Password:");
        passLbl.setFont(PawTheme.FONT_BODY_BOLD);
        passLbl.setForeground(PawTheme.TEXT_BODY);
        formPanel.add(passLbl, gbc);

        gbc.gridx = 1;
        passwordField = new JPasswordField(18);
        PawTheme.styleField(passwordField);
        formPanel.add(passwordField, gbc);

        // Phone (Prefixed with +91 and strictly 10 digits)
        gbc.gridx = 0; gbc.gridy = 3;
        JLabel phoneLbl = new JLabel("Mobile Number:");
        phoneLbl.setFont(PawTheme.FONT_BODY_BOLD);
        phoneLbl.setForeground(PawTheme.TEXT_BODY);
        formPanel.add(phoneLbl, gbc);

        gbc.gridx = 1;
        JPanel phonePanel = new JPanel(new BorderLayout(8, 0));
        phonePanel.setOpaque(false);

        JLabel prefixLabel = new JLabel("+91 ");
        prefixLabel.setFont(PawTheme.FONT_BODY_BOLD);
        prefixLabel.setForeground(PawTheme.CORAL);
        phonePanel.add(prefixLabel, BorderLayout.WEST);

        phoneDigitsField = new JTextField(10);
        PawTheme.styleField(phoneDigitsField);
        phoneDigitsField.setToolTipText("Enter strictly 10 numeric digits");

        // Restrict phone field strictly to 10 numeric digits
        ((AbstractDocument) phoneDigitsField.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
                if (string == null) return;
                if (isNumeric(string) && (fb.getDocument().getLength() + string.length() <= 10)) {
                    super.insertString(fb, offset, string, attr);
                }
            }

            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                if (text == null) return;
                if (isNumeric(text) && (fb.getDocument().getLength() - length + text.length() <= 10)) {
                    super.replace(fb, offset, length, text, attrs);
                }
            }

            private boolean isNumeric(String str) {
                return str.matches("\\d*");
            }
        });

        phonePanel.add(phoneDigitsField, BorderLayout.CENTER);
        formPanel.add(phonePanel, gbc);

        // Role Dropdown ("Pet Owner", "Pet Sitter", "Vet")
        gbc.gridx = 0; gbc.gridy = 4;
        JLabel roleLbl = new JLabel("Account Role:");
        roleLbl.setFont(PawTheme.FONT_BODY_BOLD);
        roleLbl.setForeground(PawTheme.TEXT_BODY);
        formPanel.add(roleLbl, gbc);

        gbc.gridx = 1;
        roleComboBox = new JComboBox<>(new String[]{"Pet Owner", "Pet Sitter", "Vet"});
        roleComboBox.setFont(PawTheme.FONT_BODY);
        roleComboBox.setPreferredSize(new Dimension(roleComboBox.getPreferredSize().width, 38));
        roleComboBox.addActionListener(e -> onRoleChanged());
        formPanel.add(roleComboBox, gbc);

        mainContentPanel.add(formPanel);

        // Dynamic Document Upload Section for Pet Sitter and Vet
        documentsPanel = new JPanel();
        documentsPanel.setLayout(new BoxLayout(documentsPanel, BoxLayout.Y_AXIS));
        documentsPanel.setBackground(PawTheme.PEACH_LIGHT);
        documentsPanel.setBorder(new CompoundBorder(
                new LineBorder(PawTheme.PEACH, 1, true),
                new EmptyBorder(14, 18, 14, 18)
        ));

        JPanel docTitleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        docTitleRow.setOpaque(false);
        docTitleRow.add(new JLabel(PawIcons.document(18, PawTheme.SUNNY_TEXT)));

        JLabel docSectionTitle = new JLabel("Professional Verification Documents (PDF, max 10MB)");
        docSectionTitle.setFont(PawTheme.FONT_BODY_BOLD);
        docSectionTitle.setForeground(PawTheme.SUNNY_TEXT);
        docTitleRow.add(docSectionTitle);
        documentsPanel.add(docTitleRow);
        documentsPanel.add(Box.createVerticalStrut(12));

        // Row 1: ID Card
        JPanel idRow = new JPanel(new BorderLayout(10, 0));
        idRow.setOpaque(false);
        idCardLabel = new JLabel("ID Card (Govt / Photo):");
        idCardLabel.setFont(PawTheme.FONT_BODY_BOLD);
        idCardLabel.setForeground(PawTheme.TEXT_BODY);
        idCardLabel.setPreferredSize(new Dimension(175, 32));
        idRow.add(idCardLabel, BorderLayout.WEST);

        PawComponents.PawButton browseIdBtn = new PawComponents.PawButton("Choose PDF...", PawIcons.document(12, PawTheme.SUNNY_TEXT), PawComponents.ButtonStyle.SUNNY);
        browseIdBtn.addActionListener(e -> chooseDocumentFile(true));
        idRow.add(browseIdBtn, BorderLayout.CENTER);

        idCardFileLabel = new JLabel("No file selected");
        idCardFileLabel.setFont(PawTheme.FONT_SMALL);
        idCardFileLabel.setForeground(PawTheme.TEXT_MUTED);
        idCardFileLabel.setPreferredSize(new Dimension(150, 32));
        idRow.add(idCardFileLabel, BorderLayout.EAST);
        documentsPanel.add(idRow);

        documentsPanel.add(Box.createVerticalStrut(10));

        // Row 2: Qualification Proof
        JPanel qualRow = new JPanel(new BorderLayout(10, 0));
        qualRow.setOpaque(false);
        qualLabel = new JLabel("Qualifications Proof:");
        qualLabel.setFont(PawTheme.FONT_BODY_BOLD);
        qualLabel.setForeground(PawTheme.TEXT_BODY);
        qualLabel.setPreferredSize(new Dimension(175, 32));
        qualRow.add(qualLabel, BorderLayout.WEST);

        PawComponents.PawButton browseQualBtn = new PawComponents.PawButton("Choose PDF...", PawIcons.document(12, PawTheme.SUNNY_TEXT), PawComponents.ButtonStyle.SUNNY);
        browseQualBtn.addActionListener(e -> chooseDocumentFile(false));
        qualRow.add(browseQualBtn, BorderLayout.CENTER);

        qualFileLabel = new JLabel("No file selected");
        qualFileLabel.setFont(PawTheme.FONT_SMALL);
        qualFileLabel.setForeground(PawTheme.TEXT_MUTED);
        qualFileLabel.setPreferredSize(new Dimension(150, 32));
        qualRow.add(qualFileLabel, BorderLayout.EAST);
        documentsPanel.add(qualRow);

        documentsPanel.setVisible(false);
        mainContentPanel.add(Box.createVerticalStrut(10));
        mainContentPanel.add(documentsPanel);

        add(mainContentPanel, BorderLayout.CENTER);

        // Button Actions
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 14));
        buttonPanel.setBackground(PawTheme.SURFACE);
        buttonPanel.setBorder(new CompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, PawTheme.BORDER_LIGHT),
                new EmptyBorder(8, 20, 10, 20)
        ));

        cancelButton = new PawComponents.PawButton("Cancel", PawComponents.ButtonStyle.SECONDARY);
        cancelButton.addActionListener(e -> dispose());

        registerButton = new PawComponents.PawButton("Register Account", PawIcons.check(14, Color.WHITE), PawComponents.ButtonStyle.CORAL);
        registerButton.addActionListener(e -> handleRegister());

        buttonPanel.add(cancelButton);
        buttonPanel.add(registerButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void onRoleChanged() {
        String selected = (String) roleComboBox.getSelectedItem();
        if ("Pet Sitter".equals(selected)) {
            idCardLabel.setText("ID Card Proof:");
            qualLabel.setText("Experience / Proof:");
            documentsPanel.setVisible(true);
            setSize(580, 680);
        } else if ("Vet".equals(selected)) {
            idCardLabel.setText("ID Card Proof:");
            qualLabel.setText("Medical Qualifications:");
            documentsPanel.setVisible(true);
            setSize(580, 680);
        } else {
            documentsPanel.setVisible(false);
            setSize(580, 540);
        }
        revalidate();
        repaint();
    }

    private void chooseDocumentFile(boolean isIdCard) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select PDF Document (Max 10MB)");
        chooser.setFileFilter(new FileNameExtensionFilter("PDF Documents (*.pdf)", "pdf"));
        chooser.setAcceptAllFileFilterUsed(false);

        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            if (file == null || !file.exists()) return;

            if (!file.getName().toLowerCase().endsWith(".pdf")) {
                Dialogs.showError(this, "Only PDF (.pdf) documents are permitted.");
                return;
            }

            if (file.length() > MAX_FILE_SIZE) {
                Dialogs.showError(this, "The selected file exceeds the maximum permitted size of 10MB (" +
                        String.format("%.2f", file.length() / (1024.0 * 1024.0)) + " MB).");
                return;
            }

            if (isIdCard) {
                selectedIdCardFile = file;
                idCardFileLabel.setText(file.getName());
                idCardFileLabel.setForeground(PawTheme.SUCCESS_TEXT);
            } else {
                selectedQualFile = file;
                qualFileLabel.setText(file.getName());
                qualFileLabel.setForeground(PawTheme.SUCCESS_TEXT);
            }
        }
    }

    private String getBackendRole(String displayRole) {
        return switch (displayRole) {
            case "Pet Owner" -> "OWNER";
            case "Pet Sitter" -> "SITTER";
            case "Vet" -> "VET";
            default -> "OWNER";
        };
    }

    private void handleRegister() {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();
        String phoneDigits = phoneDigitsField.getText().trim();
        String displayRole = (String) roleComboBox.getSelectedItem();
        String role = getBackendRole(displayRole);

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Dialogs.showError(this, "Please fill in all required fields (Name, Email, Password).");
            return;
        }

        // Strict 10-digit mobile number validation
        if (phoneDigits.length() != 10) {
            Dialogs.showError(this, "Mobile number must be exactly 10 digits after +91.");
            phoneDigitsField.requestFocus();
            return;
        }

        String fullPhone = "+91" + phoneDigits;

        // Document validations for Sitters and Vets
        if ("SITTER".equals(role) || "VET".equals(role)) {
            if (selectedIdCardFile == null) {
                Dialogs.showError(this, "Please upload your ID Card PDF document.");
                return;
            }
            if (selectedQualFile == null) {
                Dialogs.showError(this, "Please upload your Qualifications / Experience Certificate PDF.");
                return;
            }
        }

        registerButton.setEnabled(false);
        registerButton.setText("Submitting Application...");

        SwingUtilities.invokeLater(() -> {
            try {
                if ("SITTER".equals(role) || "VET".equals(role)) {
                    Map<String, String> fields = new HashMap<>();
                    fields.put("name", name);
                    fields.put("email", email);
                    fields.put("password", password);
                    fields.put("phone", fullPhone);
                    fields.put("role", role);

                    Map<String, File> files = new HashMap<>();
                    files.put("idCard", selectedIdCardFile);
                    files.put("qualificationProof", selectedQualFile);

                    AuthResponseDTO res = ApiClient.getInstance().postMultipart(
                            "/api/auth/register-with-documents",
                            fields,
                            files,
                            AuthResponseDTO.class
                    );

                    Dialogs.showSuccess(this,
                            "Application Submitted Successfully!\n\n" +
                            "Welcome, " + res.getName() + ".\n" +
                            "Your " + displayRole + " registration has been received with your documents.\n\n" +
                            "STATUS: PENDING APPROVAL\n" +
                            "Please wait until an administrator reviews your qualifications and approves your account before logging in."
                    );
                } else {
                    // Standard Pet Owner Registration
                    RegisterRequestDTO req = new RegisterRequestDTO(name, email, password, fullPhone, role);
                    AuthResponseDTO res = ApiClient.getInstance().post("/api/auth/register", req, AuthResponseDTO.class);

                    Dialogs.showSuccess(this,
                            "Account created successfully!\n\nWelcome to PawLink, " + res.getName() + " (Pet Owner).\nYou can now log in."
                    );
                }

                this.registeredEmail = email;
                dispose();
            } catch (ApiException ex) {
                Dialogs.showError(this, ex.getMessage());
            } catch (Exception ex) {
                Dialogs.showError(this, "Registration failed: " + ex.getMessage());
            } finally {
                registerButton.setEnabled(true);
                registerButton.setText("Register Account");
            }
        });
    }

    public String getRegisteredEmail() {
        return registeredEmail;
    }
}
