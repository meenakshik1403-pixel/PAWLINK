package com.pawlink.vet.view;

import com.pawlink.vet.api.ApiClient;
import com.pawlink.vet.api.ApiException;
import com.pawlink.vet.api.Session;
import com.pawlink.vet.base.Dialogs;
import com.pawlink.vet.base.PawComponents;
import com.pawlink.vet.base.PawDatePicker;
import com.pawlink.vet.base.PawIcons;
import com.pawlink.vet.base.PawTheme;
import com.pawlink.vet.model.PetDTO;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

/**
 * Modernized Modal Dialog for registering a new pet in PawLink.
 * Pure Java2D vector styling, robust error handling, and tactile animations.
 */
public class AddPetDialog extends JDialog {

    private final JTextField nameField;
    private final JComboBox<String> speciesCombo;
    private final PawDatePicker dobField;
    private final JLabel errorLabel;
    private final PawComponents.PawButton submitButton;
    private final PawComponents.PawButton cancelButton;

    private boolean petCreated = false;
    private final Runnable onSuccess;

    public AddPetDialog(Frame parent) {
        this(parent, null);
    }

    public AddPetDialog(Frame parent, Runnable onSuccess) {
        super(parent, "Register New Pet", true);
        this.onSuccess = onSuccess;
        setSize(500, 470);
        setLocationRelativeTo(parent);
        setResizable(false);
        setLayout(new BorderLayout());

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout(14, 0));
        headerPanel.setBackground(PawTheme.SIDEBAR_BG);
        headerPanel.setBorder(new EmptyBorder(18, 24, 18, 24));

        JLabel iconHolder = new JLabel(PawIcons.paw(26, PawTheme.CORAL));
        headerPanel.add(iconHolder, BorderLayout.WEST);

        JPanel textHolder = new JPanel(new GridLayout(2, 1, 0, 4));
        textHolder.setOpaque(false);
        JLabel title = new JLabel("Register Pet");
        title.setFont(PawTheme.FONT_TITLE);
        title.setForeground(Color.WHITE);
        textHolder.add(title);

        JLabel sub = new JLabel("Add your companion to schedule appointments and vaccines");
        sub.setFont(PawTheme.FONT_SMALL);
        sub.setForeground(PawTheme.TEXT_SUBTLE);
        textHolder.add(sub);

        headerPanel.add(textHolder, BorderLayout.CENTER);
        add(headerPanel, BorderLayout.NORTH);

        // Form fields
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(PawTheme.CANVAS);
        form.setBorder(new EmptyBorder(22, 28, 22, 28));
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(10, 8, 10, 8);

        g.gridx = 0; g.gridy = 0; g.weightx = 0.35;
        JLabel nameLabel = new JLabel("Pet Name *:");
        nameLabel.setFont(PawTheme.FONT_BODY_BOLD);
        nameLabel.setForeground(PawTheme.TEXT_BODY);
        form.add(nameLabel, g);

        g.gridx = 1; g.weightx = 0.65;
        nameField = new JTextField(15);
        PawTheme.styleField(nameField);
        form.add(nameField, g);

        g.gridx = 0; g.gridy = 1;
        JLabel speciesLabel = new JLabel("Species *:");
        speciesLabel.setFont(PawTheme.FONT_BODY_BOLD);
        speciesLabel.setForeground(PawTheme.TEXT_BODY);
        form.add(speciesLabel, g);

        g.gridx = 1;
        speciesCombo = new JComboBox<>(new String[]{"Dog", "Cat", "Bird", "Rabbit", "Reptile", "Other"});
        speciesCombo.setFont(PawTheme.FONT_BODY);
        speciesCombo.setPreferredSize(new Dimension(speciesCombo.getPreferredSize().width, 38));
        form.add(speciesCombo, g);

        g.gridx = 0; g.gridy = 2;
        JLabel dobLabel = new JLabel("Date of Birth *:");
        dobLabel.setFont(PawTheme.FONT_BODY_BOLD);
        dobLabel.setForeground(PawTheme.TEXT_BODY);
        form.add(dobLabel, g);

        g.gridx = 1;
        dobField = new PawDatePicker(LocalDate.of(2022, 1, 1));
        dobField.setAccentColor(PawTheme.CORAL);
        dobField.setToolTipText("Format: YYYY-MM-DD");
        form.add(dobField, g);

        g.gridx = 0; g.gridy = 3; g.gridwidth = 2;
        errorLabel = new JLabel(" ");
        errorLabel.setFont(PawTheme.FONT_SMALL_BOLD);
        errorLabel.setForeground(PawTheme.DANGER);
        form.add(errorLabel, g);

        add(form, BorderLayout.CENTER);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 14));
        btnPanel.setBackground(PawTheme.SURFACE);
        btnPanel.setBorder(new CompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, PawTheme.BORDER_LIGHT),
                new EmptyBorder(8, 20, 10, 20)
        ));

        cancelButton = new PawComponents.PawButton("Cancel", PawComponents.ButtonStyle.SECONDARY);
        cancelButton.addActionListener(e -> dispose());
        btnPanel.add(cancelButton);

        submitButton = new PawComponents.PawButton("Register Pet", PawIcons.plus(13, Color.WHITE), PawComponents.ButtonStyle.CORAL);
        submitButton.addActionListener(e -> handleSubmit());
        btnPanel.add(submitButton);

        add(btnPanel, BorderLayout.SOUTH);
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
