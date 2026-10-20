package com.pawlink.vet.view;

import com.pawlink.vet.api.ApiClient;
import com.pawlink.vet.api.ApiException;
import com.pawlink.vet.base.Dialogs;
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

    private JButton registerButton;
    private JButton cancelButton;

    private String registeredEmail;

    public RegisterDialog(Frame owner) {
        super(owner, "PawLink - Register Account", true);
        initUI();
    }

    private void initUI() {
        setSize(480, 460);
        setLocationRelativeTo(getOwner());
        setResizable(false);
        setLayout(new BorderLayout());

        // Header
        JPanel headerPanel = new JPanel(new GridLayout(2, 1, 0, 4));
        headerPanel.setBackground(new Color(238, 242, 255));
        headerPanel.setBorder(new EmptyBorder(15, 20, 15, 20));

        JLabel titleLabel = new JLabel("Create a New PawLink Account");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 17));
        titleLabel.setForeground(new Color(67, 56, 202));
        headerPanel.add(titleLabel);

        JLabel subtitleLabel = new JLabel("Join as a Pet Owner, Pet Sitter, or Veterinarian");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitleLabel.setForeground(new Color(107, 114, 128));
        headerPanel.add(subtitleLabel);

        add(headerPanel, BorderLayout.NORTH);

        // Form Fields
        JPanel mainContentPanel = new JPanel();
        mainContentPanel.setLayout(new BoxLayout(mainContentPanel, BoxLayout.Y_AXIS));
        mainContentPanel.setBorder(new EmptyBorder(12, 20, 12, 20));

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 4, 6, 4);

        // Name
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.32;
        JLabel nameLbl = new JLabel("Full Name:");
        nameLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        formPanel.add(nameLbl, gbc);

        gbc.gridx = 1; gbc.weightx = 0.68;
        nameField = new JTextField(18);
        formPanel.add(nameField, gbc);

        // Email
        gbc.gridx = 0; gbc.gridy = 1;
        JLabel emailLbl = new JLabel("Email Address:");
        emailLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        formPanel.add(emailLbl, gbc);

        gbc.gridx = 1;
        emailField = new JTextField(18);
        formPanel.add(emailField, gbc);

        // Password
        gbc.gridx = 0; gbc.gridy = 2;
        JLabel passLbl = new JLabel("Password:");
        passLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        formPanel.add(passLbl, gbc);

        gbc.gridx = 1;
        passwordField = new JPasswordField(18);
        formPanel.add(passwordField, gbc);

        // Phone (Prefixed with +91 and strictly 10 digits)
        gbc.gridx = 0; gbc.gridy = 3;
        JLabel phoneLbl = new JLabel("Mobile Number:");
        phoneLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        formPanel.add(phoneLbl, gbc);

        gbc.gridx = 1;
        JPanel phonePanel = new JPanel(new BorderLayout(4, 0));
        phonePanel.setOpaque(false);

        JLabel prefixLabel = new JLabel("+91 ");
        prefixLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        prefixLabel.setForeground(new Color(67, 56, 202));
        phonePanel.add(prefixLabel, BorderLayout.WEST);

        phoneDigitsField = new JTextField(10);
        phoneDigitsField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
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
        roleLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        formPanel.add(roleLbl, gbc);

        gbc.gridx = 1;
        roleComboBox = new JComboBox<>(new String[]{"Pet Owner", "Pet Sitter", "Vet"});
        roleComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        roleComboBox.addActionListener(e -> onRoleChanged());
        formPanel.add(roleComboBox, gbc);

        mainContentPanel.add(formPanel);

        // Dynamic Document Upload Section for Pet Sitter and Vet
        documentsPanel = new JPanel();
        documentsPanel.setLayout(new BoxLayout(documentsPanel, BoxLayout.Y_AXIS));
        documentsPanel.setBackground(new Color(248, 250, 252));
        documentsPanel.setBorder(new CompoundBorder(
                new LineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(10, 14, 10, 14)
        ));

        JLabel docSectionTitle = new JLabel("📄 Professional Verification Documents (PDF, max 10MB)");
        docSectionTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        docSectionTitle.setForeground(new Color(30, 41, 59));
        documentsPanel.add(docSectionTitle);
        documentsPanel.add(Box.createVerticalStrut(8));

        // Row 1: ID Card
        JPanel idRow = new JPanel(new BorderLayout(8, 0));
        idRow.setOpaque(false);
        idCardLabel = new JLabel("ID Card (Govt / Photo):");
        idCardLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        idCardLabel.setPreferredSize(new Dimension(170, 26));
        idRow.add(idCardLabel, BorderLayout.WEST);

        JButton browseIdBtn = new JButton("📁 Choose PDF...");
        browseIdBtn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        browseIdBtn.addActionListener(e -> chooseDocumentFile(true));
        idRow.add(browseIdBtn, BorderLayout.CENTER);

        idCardFileLabel = new JLabel("No file selected");
        idCardFileLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        idCardFileLabel.setForeground(new Color(100, 116, 139));
        idCardFileLabel.setPreferredSize(new Dimension(130, 26));
        idRow.add(idCardFileLabel, BorderLayout.EAST);
        documentsPanel.add(idRow);

        documentsPanel.add(Box.createVerticalStrut(6));

        // Row 2: Qualification Proof
        JPanel qualRow = new JPanel(new BorderLayout(8, 0));
        qualRow.setOpaque(false);
        qualLabel = new JLabel("Qualifications Proof:");
        qualLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        qualLabel.setPreferredSize(new Dimension(170, 26));
        qualRow.add(qualLabel, BorderLayout.WEST);

        JButton browseQualBtn = new JButton("📁 Choose PDF...");
        browseQualBtn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        browseQualBtn.addActionListener(e -> chooseDocumentFile(false));
        qualRow.add(browseQualBtn, BorderLayout.CENTER);

        qualFileLabel = new JLabel("No file selected");
        qualFileLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        qualFileLabel.setForeground(new Color(100, 116, 139));
        qualFileLabel.setPreferredSize(new Dimension(130, 26));
        qualRow.add(qualFileLabel, BorderLayout.EAST);
        documentsPanel.add(qualRow);

        documentsPanel.setVisible(false);
        mainContentPanel.add(Box.createVerticalStrut(8));
        mainContentPanel.add(documentsPanel);

        add(mainContentPanel, BorderLayout.CENTER);

        // Button Actions
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> dispose());

        registerButton = new JButton("Register Account");
        registerButton.setBackground(new Color(79, 70, 229));
        registerButton.setForeground(Color.WHITE);
        registerButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
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
            setSize(490, 580);
        } else if ("Vet".equals(selected)) {
            idCardLabel.setText("ID Card Proof:");
            qualLabel.setText("Medical Qualifications:");
            documentsPanel.setVisible(true);
            setSize(490, 580);
        } else {
            documentsPanel.setVisible(false);
            setSize(480, 460);
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
                idCardFileLabel.setText("✓ " + file.getName());
                idCardFileLabel.setForeground(new Color(16, 185, 129));
            } else {
                selectedQualFile = file;
                qualFileLabel.setText("✓ " + file.getName());
                qualFileLabel.setForeground(new Color(16, 185, 129));
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
                            "📋 Application Submitted Successfully!\n\n" +
                            "Welcome, " + res.getName() + ".\n" +
                            "Your " + displayRole + " registration has been received with your documents.\n\n" +
                            "⏳ STATUS: PENDING APPROVAL\n" +
                            "Please wait until an administrator reviews your qualifications and approves your account before logging in."
                    );
                } else {
                    // Standard Pet Owner Registration
                    RegisterRequestDTO req = new RegisterRequestDTO(name, email, password, fullPhone, role);
                    AuthResponseDTO res = ApiClient.getInstance().post("/api/auth/register", req, AuthResponseDTO.class);

                    Dialogs.showSuccess(this,
                            "🎉 Account created successfully!\n\nWelcome to PawLink, " + res.getName() + " (Pet Owner).\nYou can now log in."
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
