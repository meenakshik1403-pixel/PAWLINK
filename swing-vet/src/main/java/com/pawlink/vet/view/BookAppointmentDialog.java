package com.pawlink.vet.view;

import com.pawlink.vet.api.ApiClient;
import com.pawlink.vet.api.ApiException;
import com.pawlink.vet.api.Session;
import com.pawlink.vet.model.AppointmentDTO;
import com.pawlink.vet.model.PetDTO;
import com.pawlink.vet.model.UserDTO;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Modal dialog for booking a veterinary appointment with conflict collision detection.
 */
public class BookAppointmentDialog extends JDialog {

    private final JComboBox<String> vetComboBox;
    private final Map<Integer, Long> vetIndexToId = new HashMap<>();

    private final JComboBox<String> petComboBox;
    private final Map<Integer, Long> petIndexToId = new HashMap<>();

    private final JTextField petIdManualField;
    private final JTextField dateField;
    private final JComboBox<String> timeSlotComboBox;
    private final JComboBox<String> durationComboBox;

    private final JButton bookButton;
    private final JButton cancelButton;
    private final Runnable onSuccess;

    public BookAppointmentDialog(Frame parent, Runnable onSuccess) {
        super(parent, "📅 Book Veterinary Appointment", true);
        this.onSuccess = onSuccess;

        setSize(520, 500);
        setLocationRelativeTo(parent);
        setResizable(false);
        setLayout(new BorderLayout());

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(30, 41, 59));
        headerPanel.setBorder(new EmptyBorder(16, 20, 16, 20));

        JLabel titleLabel = new JLabel("Schedule Clinic Appointment");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);

        JLabel subLabel = new JLabel("Time intervals are checked in real-time to avoid double-booking");
        subLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subLabel.setForeground(new Color(148, 163, 184));

        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(subLabel, BorderLayout.SOUTH);
        add(headerPanel, BorderLayout.NORTH);

        // Form Fields
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(20, 24, 20, 24));
        form.setBackground(new Color(248, 250, 252));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 6, 8, 6);

        // 1. Doctor / Vet Selection
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.3;
        JLabel vetLabel = new JLabel("Veterinarian *:");
        vetLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        form.add(vetLabel, gbc);

        gbc.gridx = 1; gbc.weightx = 0.7;
        vetComboBox = new JComboBox<>();
        vetComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        form.add(vetComboBox, gbc);

        // 2. Pet Selection / Pet ID
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.3;
        JLabel petLabel = new JLabel("Select Pet *:");
        petLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        form.add(petLabel, gbc);

        gbc.gridx = 1; gbc.weightx = 0.7;
        JPanel petRow = new JPanel(new BorderLayout(8, 0));
        petRow.setOpaque(false);
        petComboBox = new JComboBox<>();
        petComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        petIdManualField = new JTextField("1", 5);
        petIdManualField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        petIdManualField.setToolTipText("Manual Pet ID fallback");

        petRow.add(petComboBox, BorderLayout.CENTER);
        petRow.add(petIdManualField, BorderLayout.EAST);
        form.add(petRow, gbc);

        // 3. Appointment Date
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.3;
        JLabel dateLabel = new JLabel("Date (YYYY-MM-DD) *:");
        dateLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        form.add(dateLabel, gbc);

        gbc.gridx = 1; gbc.weightx = 0.7;
        dateField = new JTextField(LocalDate.now().plusDays(1).toString());
        dateField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        form.add(dateField, gbc);

        // 4. Time Slot
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0.3;
        JLabel timeLabel = new JLabel("Start Time *:");
        timeLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        form.add(timeLabel, gbc);

        gbc.gridx = 1; gbc.weightx = 0.7;
        String[] slots = {"09:00", "09:30", "10:00", "10:30", "11:00", "11:30",
                          "14:00", "14:30", "15:00", "15:30", "16:00", "16:30", "17:00"};
        timeSlotComboBox = new JComboBox<>(slots);
        timeSlotComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        form.add(timeSlotComboBox, gbc);

        // 5. Duration
        gbc.gridx = 0; gbc.gridy = 4; gbc.weightx = 0.3;
        JLabel durationLabel = new JLabel("Duration *:");
        durationLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        form.add(durationLabel, gbc);

        gbc.gridx = 1; gbc.weightx = 0.7;
        durationComboBox = new JComboBox<>(new String[]{"30 Minutes", "60 Minutes"});
        durationComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        form.add(durationComboBox, gbc);

        add(form, BorderLayout.CENTER);

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 14));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(new CompoundBorder(
                new LineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(6, 12, 6, 12)
        ));

        cancelButton = new JButton("Cancel");
        cancelButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cancelButton.addActionListener(e -> dispose());

        bookButton = new JButton("Confirm Booking");
        bookButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        bookButton.setBackground(new Color(37, 99, 235));
        bookButton.setForeground(Color.WHITE);
        bookButton.setFocusPainted(false);
        bookButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        bookButton.addActionListener(e -> submitBooking());

        buttonPanel.add(cancelButton);
        buttonPanel.add(bookButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // Load dropdown options asynchronously
        loadVetsAndPets();
    }

    private void loadVetsAndPets() {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            private List<UserDTO> vets;
            private List<PetDTO> pets;

            @Override
            protected Void doInBackground() throws Exception {
                try {
                    vets = ApiClient.getInstance().getList("/api/vets", UserDTO.class);
                } catch (Exception ignored) {}

                Long ownerId = Session.getInstance().getUserId();
                if (ownerId != null) {
                    try {
                        pets = ApiClient.getInstance().getList("/api/pets?ownerId=" + ownerId, PetDTO.class);
                    } catch (Exception ignored) {}
                }
                return null;
            }

            @Override
            protected void done() {
                vetComboBox.removeAllItems();
                vetIndexToId.clear();
                if (vets != null && !vets.isEmpty()) {
                    int i = 0;
                    for (UserDTO vet : vets) {
                        vetComboBox.addItem(vet.getName() + " (ID: " + vet.getId() + ")");
                        vetIndexToId.put(i++, vet.getId());
                    }
                } else {
                    vetComboBox.addItem("Dr. Sarah Jenkins (ID: 1)");
                    vetIndexToId.put(0, 1L);
                    vetComboBox.addItem("Dr. Michael Brown (ID: 2)");
                    vetIndexToId.put(1, 2L);
                }

                petComboBox.removeAllItems();
                petIndexToId.clear();
                if (pets != null && !pets.isEmpty()) {
                    int i = 0;
                    for (PetDTO p : pets) {
                        petComboBox.addItem(p.getName() + " (" + p.getSpecies() + ", ID: " + p.getId() + ")");
                        petIndexToId.put(i++, p.getId());
                    }
                    if (!pets.isEmpty()) {
                        petIdManualField.setText(String.valueOf(pets.get(0).getId()));
                    }
                } else {
                    petComboBox.addItem("Use Manual Pet ID ->");
                }

                petComboBox.addActionListener(e -> {
                    int sel = petComboBox.getSelectedIndex();
                    if (petIndexToId.containsKey(sel)) {
                        petIdManualField.setText(String.valueOf(petIndexToId.get(sel)));
                    }
                });
            }
        };
        worker.execute();
    }

    private void submitBooking() {
        int vetIdx = vetComboBox.getSelectedIndex();
        Long vetId = vetIndexToId.getOrDefault(vetIdx, 1L);

        Long petId;
        try {
            petId = Long.parseLong(petIdManualField.getText().trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid numeric Pet ID.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String dateStr = dateField.getText().trim();
        LocalDate date;
        try {
            date = LocalDate.parse(dateStr);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Invalid date format. Use YYYY-MM-DD.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String slotStr = (String) timeSlotComboBox.getSelectedItem();
        LocalTime startTime = LocalTime.parse(slotStr);
        int durationMins = "60 Minutes".equals(durationComboBox.getSelectedItem()) ? 60 : 30;
        LocalTime endTime = startTime.plusMinutes(durationMins);

        LocalDateTime startDateTime = LocalDateTime.of(date, startTime);
        LocalDateTime endDateTime = LocalDateTime.of(date, endTime);

        Long ownerId = Session.getInstance().getUserId();
        if (ownerId == null) {
            ownerId = 6L; // Default fallback John Doe
        }

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        Map<String, Object> req = new HashMap<>();
        req.put("vetId", vetId);
        req.put("petId", petId);
        req.put("ownerId", ownerId);
        req.put("startTime", startDateTime.format(dtf));
        req.put("endTime", endDateTime.format(dtf));

        bookButton.setEnabled(false);
        bookButton.setText("Booking...");

        SwingWorker<AppointmentDTO, Void> worker = new SwingWorker<>() {
            @Override
            protected AppointmentDTO doInBackground() throws Exception {
                return ApiClient.getInstance().post("/api/appointments", req, AppointmentDTO.class);
            }

            @Override
            protected void done() {
                bookButton.setEnabled(true);
                bookButton.setText("Confirm Booking");
                try {
                    AppointmentDTO booked = get();
                    JOptionPane.showMessageDialog(
                            BookAppointmentDialog.this,
                            "Appointment successfully booked for " + booked.getPetName() + "!\n" +
                                    "Doctor: " + booked.getVetName() + "\n" +
                                    "Time: " + booked.getTimeSlotFormatted() + "\n" +
                                    "Status: " + booked.getStatus(),
                            "Booking Confirmed",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                    if (onSuccess != null) {
                        onSuccess.run();
                    }
                    dispose();
                } catch (Exception ex) {
                    String msg = ex.getMessage();
                    if (msg != null && msg.contains("409")) {
                        JOptionPane.showMessageDialog(
                                BookAppointmentDialog.this,
                                "The selected veterinarian is already booked during this time interval.\n" +
                                        "Please select another time slot.",
                                "Conflict: Slot Unavailable (409)",
                                JOptionPane.WARNING_MESSAGE
                        );
                    } else {
                        JOptionPane.showMessageDialog(
                                BookAppointmentDialog.this,
                                "Failed to book appointment: " + msg,
                                "Booking Failed",
                                JOptionPane.ERROR_MESSAGE
                        );
                    }
                }
            }
        };
        worker.execute();
    }
}
