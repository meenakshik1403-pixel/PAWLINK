package com.pawlink.vet.view;

import com.pawlink.vet.api.ApiClient;
import com.pawlink.vet.api.Session;
import com.pawlink.vet.base.PawComponents;
import com.pawlink.vet.base.PawDatePicker;
import com.pawlink.vet.base.PawIcons;
import com.pawlink.vet.base.PawTheme;
import com.pawlink.vet.model.PetDTO;
import com.pawlink.vet.model.ReminderDTO;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Modal dialog to schedule a new pet care reminder (Medication, Vaccination, Checkup).
 * Modernized with pure Java2D vector icons, FlatLaf styling, and micro-animated buttons.
 */
public class CreateReminderDialog extends JDialog {

    private final JComboBox<String> petComboBox;
    private final Map<Integer, Long> petIndexToId = new HashMap<>();
    private final JComboBox<String> typeComboBox;
    private final PawDatePicker dueDateField;

    private final PawComponents.PawButton submitButton;
    private final PawComponents.PawButton cancelButton;
    private final Runnable onSuccess;
    private final Long preselectedPetId;

    public CreateReminderDialog(Frame parent, Runnable onSuccess) {
        this(parent, null, onSuccess);
    }

    public CreateReminderDialog(Frame parent, Long preselectedPetId, Runnable onSuccess) {
        super(parent, "Schedule Pet Care Reminder", true);
        this.onSuccess = onSuccess;
        this.preselectedPetId = preselectedPetId;

        setSize(520, 450);
        setLocationRelativeTo(parent);
        setResizable(false);
        setLayout(new BorderLayout());

        // Header
        JPanel header = new JPanel(new BorderLayout(14, 0));
        header.setBackground(PawTheme.SIDEBAR_BG);
        header.setBorder(new EmptyBorder(18, 24, 18, 24));

        JLabel iconLabel = new JLabel(PawIcons.bell(26, PawTheme.SUNNY));
        header.add(iconLabel, BorderLayout.WEST);

        JPanel titleBlock = new JPanel(new GridLayout(2, 1, 0, 4));
        titleBlock.setOpaque(false);

        JLabel titleLabel = new JLabel("Schedule Pet Care Reminder");
        titleLabel.setFont(PawTheme.FONT_TITLE);
        titleLabel.setForeground(Color.WHITE);
        titleBlock.add(titleLabel);

        JLabel subLabel = new JLabel("Configure reminders for upcoming medications, vaccinations, or wellness checkups");
        subLabel.setFont(PawTheme.FONT_SMALL);
        subLabel.setForeground(PawTheme.TEXT_SUBTLE);
        titleBlock.add(subLabel);

        header.add(titleBlock, BorderLayout.CENTER);
        add(header, BorderLayout.NORTH);

        // Form Fields
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(22, 28, 22, 28));
        form.setBackground(PawTheme.CANVAS);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 8, 10, 8);

        // 1. Pet Selection
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.32;
        JLabel petLabel = new JLabel("Select Pet *:");
        petLabel.setFont(PawTheme.FONT_BODY_BOLD);
        petLabel.setForeground(PawTheme.TEXT_BODY);
        form.add(petLabel, gbc);

        gbc.gridx = 1; gbc.weightx = 0.68;
        petComboBox = new JComboBox<>();
        petComboBox.setFont(PawTheme.FONT_BODY);
        petComboBox.setPreferredSize(new Dimension(petComboBox.getPreferredSize().width, 38));
        form.add(petComboBox, gbc);

        // 2. Reminder Type
        gbc.gridx = 0; gbc.gridy = 1;
        JLabel typeLabel = new JLabel("Reminder Type *:");
        typeLabel.setFont(PawTheme.FONT_BODY_BOLD);
        typeLabel.setForeground(PawTheme.TEXT_BODY);
        form.add(typeLabel, gbc);

        gbc.gridx = 1;
        typeComboBox = new JComboBox<>(new String[]{"MEDICATION", "VACCINATION", "CHECKUP"});
        typeComboBox.setFont(PawTheme.FONT_BODY);
        typeComboBox.setPreferredSize(new Dimension(typeComboBox.getPreferredSize().width, 38));
        form.add(typeComboBox, gbc);

        // 3. Due Date
        gbc.gridx = 0; gbc.gridy = 2;
        JLabel dateLabel = new JLabel("Due Date *:");
        dateLabel.setFont(PawTheme.FONT_BODY_BOLD);
        dateLabel.setForeground(PawTheme.TEXT_BODY);
        form.add(dateLabel, gbc);

        gbc.gridx = 1;
        dueDateField = new PawDatePicker(LocalDate.now(), PawTheme.SUNNY);
        form.add(dueDateField, gbc);

        add(form, BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 14));
        buttonPanel.setBackground(PawTheme.SURFACE);
        buttonPanel.setBorder(new CompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, PawTheme.BORDER_LIGHT),
                new EmptyBorder(8, 20, 10, 20)
        ));

        cancelButton = new PawComponents.PawButton("Cancel", PawComponents.ButtonStyle.SECONDARY);
        cancelButton.addActionListener(e -> dispose());

        submitButton = new PawComponents.PawButton("Schedule Reminder", PawIcons.bell(14, PawTheme.SUNNY_TEXT), PawComponents.ButtonStyle.SUNNY);
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

        LocalDate dueDate = dueDateField.getDate();
        if (dueDate == null) {
            JOptionPane.showMessageDialog(this, "Please select a due date (YYYY-MM-DD).", "Validation Error", JOptionPane.ERROR_MESSAGE);
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
                submitButton.setText("Schedule Reminder");
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
