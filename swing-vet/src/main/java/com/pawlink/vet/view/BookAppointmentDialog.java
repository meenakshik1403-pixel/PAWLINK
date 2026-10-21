package com.pawlink.vet.view;

import com.pawlink.vet.api.ApiClient;
import com.pawlink.vet.api.Session;
import com.pawlink.vet.base.PawComponents;
import com.pawlink.vet.base.PawDatePicker;
import com.pawlink.vet.base.PawIcons;
import com.pawlink.vet.base.PawTheme;
import com.pawlink.vet.model.AppointmentDTO;
import com.pawlink.vet.model.PetDTO;
import com.pawlink.vet.model.UserDTO;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
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
 * User selects pet and doctor from intuitive dropdowns without typing manual IDs.
 * Modernized with pure Java2D vector icons, FlatLaf styling, and micro-animated buttons.
 */
public class BookAppointmentDialog extends JDialog {

    private static final String[] ALL_SLOTS = {
        "09:00", "09:30", "10:00", "10:30", "11:00", "11:30",
        "14:00", "14:30", "15:00", "15:30", "16:00", "16:30", "17:00"
    };

    private final JComboBox<String> vetComboBox;
    private final Map<Integer, Long> vetIndexToId = new HashMap<>();

    private final JComboBox<String> petComboBox;
    private final Map<Integer, Long> petIndexToId = new HashMap<>();

    private final PawDatePicker dateField;
    private final JComboBox<String> timeSlotComboBox;
    private final JComboBox<String> durationComboBox;

    private final PawComponents.PawButton bookButton;
    private final PawComponents.PawButton cancelButton;
    private final PawComponents.PawButton addPetQuickButton;
    private final Runnable onSuccess;

    public BookAppointmentDialog(Frame parent, Runnable onSuccess) {
        super(parent, "Schedule Veterinary Appointment", true);
        this.onSuccess = onSuccess;

        setSize(560, 530);
        setLocationRelativeTo(parent);
        setResizable(false);
        setLayout(new BorderLayout());

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout(14, 0));
        headerPanel.setBackground(PawTheme.SIDEBAR_BG);
        headerPanel.setBorder(new EmptyBorder(18, 24, 18, 24));

        JLabel iconLabel = new JLabel(PawIcons.calendar(26, PawTheme.SKY));
        headerPanel.add(iconLabel, BorderLayout.WEST);

        JPanel titleBlock = new JPanel(new GridLayout(2, 1, 0, 4));
        titleBlock.setOpaque(false);

        JLabel titleLabel = new JLabel("Schedule Clinic Appointment");
        titleLabel.setFont(PawTheme.FONT_TITLE);
        titleLabel.setForeground(Color.WHITE);
        titleBlock.add(titleLabel);

        JLabel subLabel = new JLabel("Select your pet, choose an available veterinarian, and pick a slot");
        subLabel.setFont(PawTheme.FONT_SMALL);
        subLabel.setForeground(PawTheme.TEXT_SUBTLE);
        titleBlock.add(subLabel);

        headerPanel.add(titleBlock, BorderLayout.CENTER);
        add(headerPanel, BorderLayout.NORTH);

        // Form Fields
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(22, 28, 22, 28));
        form.setBackground(PawTheme.CANVAS);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 8, 10, 8);

        // 1. Doctor / Vet Selection
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.32;
        JLabel vetLabel = new JLabel("Veterinarian *:");
        vetLabel.setFont(PawTheme.FONT_BODY_BOLD);
        vetLabel.setForeground(PawTheme.TEXT_BODY);
        form.add(vetLabel, gbc);

        gbc.gridx = 1; gbc.weightx = 0.68;
        vetComboBox = new JComboBox<>();
        vetComboBox.setFont(PawTheme.FONT_BODY);
        vetComboBox.setPreferredSize(new Dimension(vetComboBox.getPreferredSize().width, 38));
        form.add(vetComboBox, gbc);

        // 2. Pet Selection
        gbc.gridx = 0; gbc.gridy = 1;
        JLabel petLabel = new JLabel("Select Pet *:");
        petLabel.setFont(PawTheme.FONT_BODY_BOLD);
        petLabel.setForeground(PawTheme.TEXT_BODY);
        form.add(petLabel, gbc);

        gbc.gridx = 1;
        JPanel petRow = new JPanel(new BorderLayout(8, 0));
        petRow.setOpaque(false);

        petComboBox = new JComboBox<>();
        petComboBox.setFont(PawTheme.FONT_BODY);
        petComboBox.setPreferredSize(new Dimension(petComboBox.getPreferredSize().width, 38));

        addPetQuickButton = new PawComponents.PawButton("Add Pet", PawIcons.plus(11, PawTheme.SUNNY_TEXT), PawComponents.ButtonStyle.SUNNY);
        addPetQuickButton.addActionListener(e -> openAddPetDialog());

        petRow.add(petComboBox, BorderLayout.CENTER);
        petRow.add(addPetQuickButton, BorderLayout.EAST);
        form.add(petRow, gbc);

        // 3. Appointment Date
        gbc.gridx = 0; gbc.gridy = 2;
        JLabel dateLabel = new JLabel("Date *:");
        dateLabel.setFont(PawTheme.FONT_BODY_BOLD);
        dateLabel.setForeground(PawTheme.TEXT_BODY);
        form.add(dateLabel, gbc);

        gbc.gridx = 1;
        // Default to today, but if today's clinic hours (17:00) have already passed, auto-advance to tomorrow
        LocalDate initialDate = LocalDate.now();
        if (LocalTime.now().isAfter(LocalTime.of(17, 0))) {
            initialDate = initialDate.plusDays(1);
        }
        dateField = new PawDatePicker(initialDate, PawTheme.SKY);
        dateField.addDateChangeListener(d -> updateAvailableSlots());
        form.add(dateField, gbc);

        // 4. Time Slot
        gbc.gridx = 0; gbc.gridy = 3;
        JLabel timeLabel = new JLabel("Start Time *:");
        timeLabel.setFont(PawTheme.FONT_BODY_BOLD);
        timeLabel.setForeground(PawTheme.TEXT_BODY);
        form.add(timeLabel, gbc);

        gbc.gridx = 1;
        timeSlotComboBox = new JComboBox<>();
        timeSlotComboBox.setFont(PawTheme.FONT_BODY);
        timeSlotComboBox.setPreferredSize(new Dimension(timeSlotComboBox.getPreferredSize().width, 38));
        form.add(timeSlotComboBox, gbc);

        // 5. Duration
        gbc.gridx = 0; gbc.gridy = 4;
        JLabel durationLabel = new JLabel("Duration *:");
        durationLabel.setFont(PawTheme.FONT_BODY_BOLD);
        durationLabel.setForeground(PawTheme.TEXT_BODY);
        form.add(durationLabel, gbc);

        gbc.gridx = 1;
        durationComboBox = new JComboBox<>(new String[]{"30 Minutes", "60 Minutes"});
        durationComboBox.setFont(PawTheme.FONT_BODY);
        durationComboBox.setPreferredSize(new Dimension(durationComboBox.getPreferredSize().width, 38));
        form.add(durationComboBox, gbc);

        add(form, BorderLayout.CENTER);

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 14));
        buttonPanel.setBackground(PawTheme.SURFACE);
        buttonPanel.setBorder(new CompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, PawTheme.BORDER_LIGHT),
                new EmptyBorder(8, 16, 10, 16)
        ));

        cancelButton = new PawComponents.PawButton("Cancel", PawComponents.ButtonStyle.SECONDARY);
        cancelButton.addActionListener(e -> dispose());

        bookButton = new PawComponents.PawButton("Confirm Booking", PawIcons.calendar(14, Color.WHITE), PawComponents.ButtonStyle.CORAL);
        bookButton.addActionListener(e -> submitBooking());

        buttonPanel.add(cancelButton);
        buttonPanel.add(bookButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // Populate available slots for the selected date
        updateAvailableSlots();

        // Load dropdown options asynchronously
        loadVetsAndPets();
    }

    private void openAddPetDialog() {
        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        Frame parentFrame = parentWindow instanceof Frame ? (Frame) parentWindow : null;
        AddPetDialog dialog = new AddPetDialog(parentFrame, this::loadVetsAndPets);
        dialog.setVisible(true);
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
                        vetComboBox.addItem(vet.getName());
                        vetIndexToId.put(i++, vet.getId());
                    }
                } else {
                    vetComboBox.addItem("Dr. Sarah Jenkins");
                    vetIndexToId.put(0, 1L);
                    vetComboBox.addItem("Dr. Michael Brown");
                    vetIndexToId.put(1, 2L);
                }

                petComboBox.removeAllItems();
                petIndexToId.clear();
                if (pets != null && !pets.isEmpty()) {
                    int i = 0;
                    java.util.Set<String> seen = new java.util.HashSet<>();
                    for (PetDTO p : pets) {
                        String key = (p.getId() != null ? p.getId() : p.getName()) + "";
                        if (seen.add(key)) {
                            petComboBox.addItem(p.getName() + " (" + p.getSpecies() + ")");
                            petIndexToId.put(i++, p.getId());
                        }
                    }
                } else {
                    petComboBox.addItem("(No pets registered - click Add Pet)");
                }
            }
        };
        worker.execute();
    }

    private void submitBooking() {
        int petIdx = petComboBox.getSelectedIndex();
        Long petId = petIndexToId.get(petIdx);
        if (petId == null) {
            JOptionPane.showMessageDialog(this, "Please select a pet for the appointment. If you haven't registered a pet yet, click 'Add Pet'.", "Pet Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int vetIdx = vetComboBox.getSelectedIndex();
        Long vetId = vetIndexToId.get(vetIdx);
        if (vetId == null) {
            JOptionPane.showMessageDialog(this, "Please select a veterinarian.", "Doctor Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        LocalDate date = dateField.getDate();
        if (date == null) {
            JOptionPane.showMessageDialog(this, "Please select an appointment date (YYYY-MM-DD).", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (date.isBefore(LocalDate.now())) {
            JOptionPane.showMessageDialog(this, "Appointment date cannot be in the past.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String slotStr = (String) timeSlotComboBox.getSelectedItem();
        if (slotStr == null || slotStr.startsWith("(")) {
            JOptionPane.showMessageDialog(this, "Please select an available future time slot.", "Invalid Time Slot", JOptionPane.WARNING_MESSAGE);
            return;
        }

        LocalTime startTime;
        try {
            startTime = LocalTime.parse(slotStr);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Invalid time format.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int durationMins = "60 Minutes".equals(durationComboBox.getSelectedItem()) ? 60 : 30;
        LocalTime endTime = startTime.plusMinutes(durationMins);

        LocalDateTime startDateTime = LocalDateTime.of(date, startTime);
        LocalDateTime endDateTime = LocalDateTime.of(date, endTime);

        if (startDateTime.isBefore(LocalDateTime.now())) {
            JOptionPane.showMessageDialog(this,
                    "The selected appointment time has already passed. Please select a future time slot.",
                    "Invalid Appointment Time",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        Long ownerId = Session.getInstance().getUserId();
        if (ownerId == null) {
            ownerId = 6L;
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
                    if (msg != null && (msg.contains("409") || msg.toLowerCase().contains("not available") || msg.toLowerCase().contains("collide"))) {
                        JOptionPane.showMessageDialog(
                                BookAppointmentDialog.this,
                                "The selected veterinarian is already booked during this time interval.\n" +
                                        "Please select another time slot or another doctor.",
                                "Conflict: Slot Unavailable",
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

    private void updateAvailableSlots() {
        if (timeSlotComboBox == null) return;
        LocalDate selectedDate = dateField.getDate();
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        timeSlotComboBox.removeAllItems();

        if (selectedDate == null) {
            timeSlotComboBox.addItem("(Select a date first)");
            if (bookButton != null) bookButton.setEnabled(false);
            return;
        }

        if (selectedDate.isBefore(today)) {
            timeSlotComboBox.addItem("(Past date selected)");
            if (bookButton != null) bookButton.setEnabled(false);
            return;
        }

        boolean isToday = selectedDate.equals(today);
        int availableCount = 0;
        for (String slot : ALL_SLOTS) {
            LocalTime slotTime = LocalTime.parse(slot);
            if (isToday && !slotTime.isAfter(now)) {
                // slot has already passed today
                continue;
            }
            timeSlotComboBox.addItem(slot);
            availableCount++;
        }

        if (availableCount == 0) {
            if (isToday) {
                timeSlotComboBox.addItem("(No slots remaining today - choose tomorrow)");
            } else {
                timeSlotComboBox.addItem("(No slots available)");
            }
            if (bookButton != null) bookButton.setEnabled(false);
        } else {
            if (bookButton != null) bookButton.setEnabled(true);
        }
    }
}
