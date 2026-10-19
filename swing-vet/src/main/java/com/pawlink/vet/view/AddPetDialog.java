package com.pawlink.vet.view;

import com.pawlink.vet.api.ApiClient;
import com.pawlink.vet.api.ApiException;
import com.pawlink.vet.api.Session;
import com.pawlink.vet.base.Dialogs;
import com.pawlink.vet.model.PetDTO;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

/**
 * Modal Dialog for registering a new pet in the Swing application.
 * Catches InvalidPetDataException from the backend and displays inline error messages.
 */
public class AddPetDialog extends JDialog {

    private final JTextField nameField;
    private final JComboBox<String> speciesCombo;
    private final JTextField dobField;
    private final JLabel errorLabel;
    private final JButton submitButton;
    private final JButton cancelButton;

    private boolean petCreated = false;

    public AddPetDialog(Frame parent) {
        super(parent, "Register New Pet", true);
        setSize(440, 380);
        setLocationRelativeTo(parent);
        setResizable(false);
        setLayout(new BorderLayout());

        JPanel root = new JPanel(new BorderLayout(0, 15));
        root.setBackground(Color.WHITE);
        root.setBorder(new EmptyBorder(20, 24, 20, 24));

        // Header
        JPanel headerPanel = new JPanel(new GridLayout(2, 1, 0, 4));
        headerPanel.setOpaque(false);

        JLabel title = new JLabel("🐾 Add a Pet");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(new Color(30, 41, 59));
        headerPanel.add(title);

        JLabel sub = new JLabel("Enter your pet's details to access species care guidelines.");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        sub.setForeground(new Color(100, 116, 139));
        headerPanel.add(sub);

        root.add(headerPanel, BorderLayout.NORTH);

        // Form Fields
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 4, 6, 4);

        // 1. Pet Name
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.3;
        JLabel nameLabel = new JLabel("Pet Name *:");
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        form.add(nameLabel, gbc);

        gbc.gridx = 1; gbc.weightx = 0.7;
        nameField = new JTextField(15);
        nameField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        form.add(nameField, gbc);

        // 2. Species Dropdown
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.3;
        JLabel speciesLabel = new JLabel("Species *:");
        speciesLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        form.add(speciesLabel, gbc);

        gbc.gridx = 1; gbc.weightx = 0.7;
        String[] speciesOptions = {"Dog", "Cat", "Bird", "Reptile"};
        speciesCombo = new JComboBox<>(speciesOptions);
        speciesCombo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        form.add(speciesCombo, gbc);

        // 3. Date of Birth
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.3;
        JLabel dobLabel = new JLabel("DOB (YYYY-MM-DD) *:");
        dobLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        form.add(dobLabel, gbc);

        gbc.gridx = 1; gbc.weightx = 0.7;
        dobField = new JTextField(LocalDate.now().minusYears(1).toString(), 15);
        dobField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        form.add(dobField, gbc);

        // 4. Inline Error Label
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        errorLabel = new JLabel(" ");
        errorLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        errorLabel.setForeground(new Color(220, 38, 38)); // Red
        form.add(errorLabel, gbc);

        root.add(form, BorderLayout.CENTER);

        // Bottom Actions
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false);

        cancelButton = new JButton("Cancel");
        cancelButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cancelButton.addActionListener(e -> dispose());
        buttonPanel.add(cancelButton);

        submitButton = new JButton("Register Pet");
        submitButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        submitButton.setBackground(new Color(37, 99, 235)); // Blue accent
        submitButton.setForeground(Color.WHITE);
        submitButton.setFocusPainted(false);
        submitButton.addActionListener(e -> handleSubmit());
        buttonPanel.add(submitButton);

        root.add(buttonPanel, BorderLayout.SOUTH);

        add(root);
    }

    private void handleSubmit() {
        errorLabel.setText(" ");
        String name = nameField.getText().trim();
        String species = (String) speciesCombo.getSelectedItem();
        String dobStr = dobField.getText().trim();

        if (name.isEmpty()) {
            errorLabel.setText("Pet name cannot be blank.");
            return;
        }

        LocalDate dob;
        try {
            dob = LocalDate.parse(dobStr);
        } catch (DateTimeParseException e) {
            errorLabel.setText("Invalid date format. Use YYYY-MM-DD (e.g., 2022-05-15).");
            return;
        }

        Long ownerId = Session.getInstance().getUserId();
        if (ownerId == null) {
            errorLabel.setText("Session expired: No active logged-in user.");
            return;
        }

        submitButton.setEnabled(false);
        submitButton.setText("Registering...");

        Map<String, Object> payload = new HashMap<>();
        payload.put("name", name);
        payload.put("species", species);
        payload.put("dob", dob.toString());
        payload.put("ownerId", ownerId);

        SwingWorker<PetDTO, Void> worker = new SwingWorker<>() {
            @Override
            protected PetDTO doInBackground() {
                return ApiClient.getInstance().post("/api/pets", payload, PetDTO.class);
            }

            @Override
            protected void done() {
                submitButton.setEnabled(true);
                submitButton.setText("Register Pet");
                try {
                    PetDTO created = get();
                    petCreated = true;
                    Dialogs.showSuccess(AddPetDialog.this, "Successfully registered " + created.getName() + " (" + created.getSpecies() + ")!");
                    dispose();
                } catch (Exception ex) {
                    String msg = ex.getMessage();
                    if (ex.getCause() instanceof ApiException apiEx) {
                        msg = apiEx.getMessage();
                    }
                    errorLabel.setText("Error: " + msg);
                }
            }
        };
        worker.execute();
    }

    public boolean isPetCreated() {
        return petCreated;
    }
}
