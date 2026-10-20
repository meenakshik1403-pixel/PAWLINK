package com.pawlink.vet.view;

import com.pawlink.vet.api.ApiClient;
import com.pawlink.vet.api.Session;
import com.pawlink.vet.model.PetDTO;
import com.pawlink.vet.model.ReminderDTO;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Modal dialog to schedule a new pet care reminder (Medication, Vaccination, Checkup).
 */
public class CreateReminderDialog extends JDialog {

    private final JComboBox<String> petComboBox;
    private final Map<Integer, Long> petIndexToId = new HashMap<>();
    private final JComboBox<String> typeComboBox;
    private final JTextField dueDateField;

    private final JButton submitButton;
    private final JButton cancelButton;
    private final Runnable onSuccess;
    private final Long preselectedPetId;

    public CreateReminderDialog(Frame parent, Runnable onSuccess) {
        this(parent, null, onSuccess);
    }

    public CreateReminderDialog(Frame parent, Long preselectedPetId, Runnable onSuccess) {
        super(parent, "➕ Schedule Pet Care Reminder", true);
        this.onSuccess = onSuccess;
        this.preselectedPetId = preselectedPetId;

        setSize(480, 380);
        setLocationRelativeTo(parent);
        setResizable(false);
        setLayout(new BorderLayout());

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(30, 41, 59));
        header.setBorder(new EmptyBorder(16, 20, 16, 20));

        JLabel titleLabel = new JLabel("Schedule Pet Care Reminder");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);

        JLabel subLabel = new JLabel("Configure reminders for upcoming medications, vaccinations, or wellness checkups");
        subLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subLabel.setForeground(new Color(148, 163, 184));

        header.add(titleLabel, BorderLayout.NORTH);
        header.add(subLabel, BorderLayout.SOUTH);
        add(header, BorderLayout.NORTH);

        // Form Fields
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(20, 24, 20, 24));
        form.setBackground(new Color(248, 250, 252));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 6, 8, 6);

        // 1. Pet Selection
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.3;
        JLabel petLabel = new JLabel("Select Pet *:");
        petLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        form.add(petLabel, gbc);

        gbc.gridx = 1; gbc.weightx = 0.7;
        petComboBox = new JComboBox<>();
        petComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        form.add(petComboBox, gbc);

        // 2. Reminder Type
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.3;
        JLabel typeLabel = new JLabel("Reminder Type *:");
        typeLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        form.add(typeLabel, gbc);

        gbc.gridx = 1; gbc.weightx = 0.7;
        typeComboBox = new JComboBox<>(new String[]{"MEDICATION", "VACCINATION", "CHECKUP"});
        typeComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        form.add(typeComboBox, gbc);

        // 3. Due Date
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.3;
        JLabel dateLabel = new JLabel("Due Date *:");
        dateLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        form.add(dateLabel, gbc);

        gbc.gridx = 1; gbc.weightx = 0.7;
        dueDateField = new JTextField(LocalDate.now().plusWeeks(2).toString());
        dueDateField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        form.add(dueDateField, gbc);

        add(form, BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(new CompoundBorder(
                new LineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(6, 12, 6, 12)
        ));

        cancelButton = new JButton("Cancel");
        cancelButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cancelButton.addActionListener(e -> dispose());

        submitButton = new JButton("Create Reminder");
        submitButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        submitButton.setBackground(new Color(13, 148, 136));
        submitButton.setForeground(Color.WHITE);
        submitButton.setFocusPainted(false);
        submitButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        submitButton.addActionListener(e -> submitForm());

        buttonPanel.add(cancelButton);
        buttonPanel.add(submitButton);
        add(buttonPanel, BorderLayout.SOUTH);

        loadPetsAsync();
    }

    private void loadPetsAsync() {
        SwingWorker<List<PetDTO>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<PetDTO> doInBackground() throws Exception {
                Long ownerId = Session.getInstance().getUserId();
                String uri = (ownerId != null && Session.getInstance().isOwner())
                        ? "/api/pets?ownerId=" + ownerId
                        : "/api/pets";
                return ApiClient.getInstance().getList(uri, PetDTO.class);
            }

            @Override
            protected void done() {
                try {
                    List<PetDTO> pets = get();
                    petComboBox.removeAllItems();
                    petIndexToId.clear();
                    if (pets != null && !pets.isEmpty()) {
                        int idx = 0;
                        int selectedIdx = 0;
                        java.util.Set<String> seen = new java.util.HashSet<>();
                        for (PetDTO p : pets) {
                            String key = (p.getId() != null ? p.getId() : p.getName()) + "";
                            if (seen.add(key)) {
                                petComboBox.addItem(p.getName() + " (" + p.getSpecies() + ")");
                                petIndexToId.put(idx, p.getId());
                                if (preselectedPetId != null && preselectedPetId.equals(p.getId())) {
                                    selectedIdx = idx;
                                }
                                idx++;
                            }
                        }
                        petComboBox.setSelectedIndex(selectedIdx);
                    } else {
                        petComboBox.addItem("(No pets registered)");
                    }
                } catch (Exception ex) {
                    petComboBox.addItem("(Error loading pets)");
                }
            }
        };
        worker.execute();
    }

    private void submitForm() {
        int petIdx = petComboBox.getSelectedIndex();
        Long petId = petIndexToId.get(petIdx);
        if (petId == null) {
            JOptionPane.showMessageDialog(this, "Please select a registered pet.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String dateStr = dueDateField.getText().trim();
        LocalDate dueDate;
        try {
            dueDate = LocalDate.parse(dateStr);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Invalid date format. Use YYYY-MM-DD.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String type = (String) typeComboBox.getSelectedItem();

        Map<String, Object> req = new HashMap<>();
        req.put("petId", petId);
        req.put("type", type);
        req.put("dueDate", dueDate.toString());

        submitButton.setEnabled(false);
        submitButton.setText("Saving...");

        SwingWorker<ReminderDTO, Void> worker = new SwingWorker<>() {
            @Override
            protected ReminderDTO doInBackground() throws Exception {
                return ApiClient.getInstance().post("/api/reminders", req, ReminderDTO.class);
            }

            @Override
            protected void done() {
                submitButton.setEnabled(true);
                submitButton.setText("Create Reminder");
                try {
                    ReminderDTO created = get();
                    JOptionPane.showMessageDialog(
                            CreateReminderDialog.this,
                            "Reminder created successfully!\n" +
                                    "Pet: " + created.getPetName() + "\n" +
                                    "Type: " + created.getType() + "\n" +
                                    "Due Date: " + created.getDueDate(),
                            "Reminder Scheduled",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                    if (onSuccess != null) {
                        onSuccess.run();
                    }
                    dispose();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(
                            CreateReminderDialog.this,
                            "Failed to schedule reminder: " + ex.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        };
        worker.execute();
    }
}
