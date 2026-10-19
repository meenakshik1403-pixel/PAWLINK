package com.pawlink.vet.view;

import com.pawlink.vet.api.ApiClient;
import com.pawlink.vet.api.ApiException;
import com.pawlink.vet.base.Dialogs;
import com.pawlink.vet.model.AuthResponseDTO;
import com.pawlink.vet.model.RegisterRequestDTO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Registration Dialog for creating new Pet Owner, Sitter, Vet, or Admin accounts.
 */
public class RegisterDialog extends JDialog {

    private JTextField nameField;
    private JTextField emailField;
    private JPasswordField passwordField;
    private JTextField phoneField;
    private JComboBox<String> roleComboBox;
    private JButton registerButton;
    private JButton cancelButton;

    private String registeredEmail;

    public RegisterDialog(Frame owner) {
        super(owner, "PawLink - Register Account", true);
        initUI();
    }

    private void initUI() {
        setSize(440, 420);
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
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(new EmptyBorder(15, 25, 15, 25));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 4, 6, 4);

        // Name
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.3;
        formPanel.add(new JLabel("Full Name:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        nameField = new JTextField(18);
        formPanel.add(nameField, gbc);

        // Email
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.3;
        formPanel.add(new JLabel("Email Address:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        emailField = new JTextField(18);
        formPanel.add(emailField, gbc);

        // Password
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.3;
        formPanel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        passwordField = new JPasswordField(18);
        formPanel.add(passwordField, gbc);

        // Phone
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0.3;
        formPanel.add(new JLabel("Phone:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        phoneField = new JTextField(18);
        formPanel.add(phoneField, gbc);

        // Role Dropdown
        gbc.gridx = 0; gbc.gridy = 4; gbc.weightx = 0.3;
        formPanel.add(new JLabel("Account Role:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        roleComboBox = new JComboBox<>(new String[]{"OWNER", "SITTER", "VET", "ADMIN"});
        formPanel.add(roleComboBox, gbc);

        add(formPanel, BorderLayout.CENTER);

        // Button Actions
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> dispose());

        registerButton = new JButton("Register");
        registerButton.setBackground(new Color(79, 70, 229));
        registerButton.setForeground(Color.WHITE);
        registerButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        registerButton.addActionListener(e -> handleRegister());

        buttonPanel.add(cancelButton);
        buttonPanel.add(registerButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void handleRegister() {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();
        String phone = phoneField.getText().trim();
        String role = (String) roleComboBox.getSelectedItem();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Dialogs.showError(this, "Please fill in all required fields (Name, Email, Password).");
            return;
        }

        registerButton.setEnabled(false);
        registerButton.setText("Registering...");

        SwingUtilities.invokeLater(() -> {
            try {
                RegisterRequestDTO req = new RegisterRequestDTO(name, email, password, phone, role);
                AuthResponseDTO res = ApiClient.getInstance().post("/api/auth/register", req, AuthResponseDTO.class);

                Dialogs.showSuccess(this, "Account created successfully for " + res.getName() + " (" + res.getRole() + ")!");
                this.registeredEmail = email;
                dispose();
            } catch (ApiException ex) {
                Dialogs.showError(this, ex.getMessage());
            } catch (Exception ex) {
                Dialogs.showError(this, "Registration failed: " + ex.getMessage());
            } finally {
                registerButton.setEnabled(true);
                registerButton.setText("Register");
            }
        });
    }

    public String getRegisteredEmail() {
        return registeredEmail;
    }
}
