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
    private final Runnable onSuccess;

    public AddPetDialog(Frame parent) {
        this(parent, null);
    }

    public AddPetDialog(Frame parent, Runnable onSuccess) {
        super(parent, "Register New Pet", true);
        this.onSuccess = onSuccess;
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
        title.setForeground(new Color(15, 23, 42));
        headerPanel.add(title);

        JLabel sub = new JLabel("Register a pet to schedule checkups and vaccines");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        sub.setForeground(new Color(100, 116, 139));
        headerPanel.add(sub);
        root.add(headerPanel, BorderLayout.NORTH);

        // Form fields
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(6, 4, 6, 4);

        g.gridx = 0; g.gridy = 0; g.weightx = 0.35;
        JLabel nameLabel = new JLabel("Pet Name:");
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        form.add(nameLabel, g);

        g.gridx = 1; g.weightx = 0.65;
        nameField = new JTextField(15);
        form.add(nameField, g);

        g.gridx = 0; g.gridy = 1;
        JLabel speciesLabel = new JLabel("Species:");
        speciesLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        form.add(speciesLabel, g);

        g.gridx = 1;
        speciesCombo = new JComboBox<>(new String[]{"Dog", "Cat", "Bird", "Rabbit", "Other"});
        form.add(speciesCombo, g);

        g.gridx = 0; g.gridy = 2;
        JLabel dobLabel = new JLabel("Date of Birth:");
        dobLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        form.add(dobLabel, g);

        g.gridx = 1;
        dobField = new JTextField("2022-01-01", 15);
        dobField.setToolTipText("Format: YYYY-MM-DD");
        form.add(dobField, g);

        g.gridx = 0; g.gridy = 3; g.gridwidth = 2;
        errorLabel = new JLabel(" ");
        errorLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        errorLabel.setForeground(new Color(220, 38, 38));
        form.add(errorLabel, g);

        root.add(form, BorderLayout.CENTER);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setOpaque(false);

        cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> dispose());
        btnPanel.add(cancelButton);

        submitButton = new JButton("Register Pet");
        submitButton.setBackground(new Color(79, 70, 229));
        submitButton.setForeground(Color.WHITE);
        submitButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        submitButton.setFocusable(false);
        submitButton.addActionListener(e -> handleSubmit());
        btnPanel.add(submitButton);

        root.add(btnPanel, BorderLayout.SOUTH);
        add(root);
    }

    private void handleSubmit() {
        String name = nameField.getText().trim();
        String species = (String) speciesCombo.getSelectedItem();
        String dobStr = dobField.getText().trim();

        if (name.isEmpty()) {
            errorLabel.setText("Validation error: Pet name cannot be blank.");
            return;
        }

        LocalDate dob;
        try {
            dob = LocalDate.parse(dobStr);
            if (dob.isAfter(LocalDate.now())) {
                errorLabel.setText("Validation error: Date of birth cannot be in the future.");
                return;
            }
        } catch (DateTimeParseException ex) {
            errorLabel.setText("Format error: DOB must be in YYYY-MM-DD format.");
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
                    if (onSuccess != null) {
                        onSuccess.run();
                    }
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
