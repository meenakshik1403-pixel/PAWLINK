package com.pawlink.vet.view;

import com.pawlink.vet.api.ApiClient;
import com.pawlink.vet.api.Session;
import com.pawlink.vet.base.Dialogs;
import com.pawlink.vet.base.PawComponents;
import com.pawlink.vet.base.PawDateTimePicker;
import com.pawlink.vet.base.PawIcons;
import com.pawlink.vet.base.PawTheme;
import com.pawlink.vet.model.PetDTO;
import com.pawlink.vet.model.SitterDTO;
import com.pawlink.vet.model.SittingBookingDTO;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Interactive Dialog for booking an approved pet sitter with escrow payment hold.
 * Allows selecting the pet, date/time interval with cool UI pickers, and payment method (UPI, CARD, WALLET).
 * Modernized with pure Java2D vector icons, escrow badge, and high-contrast typography.
 */
public class BookSitterDialog extends JDialog {

    private final SitterDTO sitter;
    private final Runnable onSuccess;

    private final JComboBox<PetDTO> petComboBox;
    private final PawDateTimePicker startPicker;
    private final PawDateTimePicker endPicker;
    private final JComboBox<String> paymentMethodCombo;
    private final JLabel calculationLabel;
    private final PawComponents.PawButton confirmButton;

    public BookSitterDialog(Frame parent, SitterDTO sitter, Runnable onSuccess) {
        super(parent, "Book Pet Sitter - " + (sitter != null ? sitter.getName() : ""), true);
        this.sitter = sitter;
        this.onSuccess = onSuccess;

        this.petComboBox = new JComboBox<>();

        LocalDateTime now = LocalDateTime.now();
        int min = now.getMinute();
        int roundMin = min < 30 ? 30 : 60;
        LocalDateTime initialStart = now.plusMinutes(roundMin - min).withSecond(0).withNano(0);
        LocalDateTime initialEnd = initialStart.plusHours(3);

        this.startPicker = new PawDateTimePicker(initialStart, PawTheme.SUNNY, true);
        this.endPicker = new PawDateTimePicker(initialEnd, PawTheme.SUNNY, true);

        this.paymentMethodCombo = new JComboBox<>(new String[]{"UPI", "CARD", "WALLET"});
        this.calculationLabel = new JLabel("Estimated total: ₹0.00 (0 hours)");
        this.confirmButton = new PawComponents.PawButton("Confirm Booking & Escrow Hold", PawIcons.wallet(14, PawTheme.SUNNY_TEXT), PawComponents.ButtonStyle.SUNNY);

        // Auto-adjust end time if start time moves past it
        this.startPicker.addDateTimeChangeListener(startDt -> {
            if (startDt != null) {
                LocalDateTime endDt = endPicker.getDateTime();
                if (endDt == null || !endDt.isAfter(startDt)) {
                    endPicker.setDateTime(startDt.plusHours(2));
                }
            }
            updateCalculation();
        });

        this.endPicker.addDateTimeChangeListener(endDt -> {
            updateCalculation();
        });

        initUI();
        loadOwnerPets();
    }

    private void initUI() {
        setSize(580, 660);
        setLocationRelativeTo(getParent());
        setResizable(false);
        setLayout(new BorderLayout());

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout(14, 0));
        headerPanel.setBackground(PawTheme.SIDEBAR_BG);
        headerPanel.setBorder(new EmptyBorder(18, 24, 18, 24));

        JLabel iconLabel = new JLabel(PawIcons.sitter(26, PawTheme.SUNNY));
        headerPanel.add(iconLabel, BorderLayout.WEST);

        JPanel titleBlock = new JPanel(new GridLayout(2, 1, 0, 4));
        titleBlock.setOpaque(false);

        JLabel titleLabel = new JLabel("Book Pet Sitting Session");
        titleLabel.setFont(PawTheme.FONT_TITLE);
        titleLabel.setForeground(Color.WHITE);
        titleBlock.add(titleLabel);

        JLabel sitterInfo = new JLabel("Sitter: " + sitter.getName() + " | Rate: ₹" +
                (sitter.getHourlyRate() != null ? sitter.getHourlyRate() : "250.00") + " / hr " +
                (sitter.isVerified() ? "(Verified)" : "(Pending)"));
        sitterInfo.setFont(PawTheme.FONT_SMALL);
        sitterInfo.setForeground(PawTheme.TEXT_SUBTLE);
        titleBlock.add(sitterInfo);

        headerPanel.add(titleBlock, BorderLayout.CENTER);
        add(headerPanel, BorderLayout.NORTH);

        // Form Body
        JPanel bodyPanel = new JPanel(new GridBagLayout());
        bodyPanel.setBorder(new EmptyBorder(18, 26, 18, 26));
        bodyPanel.setBackground(PawTheme.CANVAS);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 8, 8, 8);

        // 1. Pet Selector
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.32;
        JLabel petLabel = new JLabel("Select Pet:");
        petLabel.setFont(PawTheme.FONT_BODY_BOLD);
        petLabel.setForeground(PawTheme.TEXT_BODY);
        bodyPanel.add(petLabel, gbc);

        gbc.gridx = 1; gbc.weightx = 0.68;
        petComboBox.setFont(PawTheme.FONT_BODY);
        petComboBox.setPreferredSize(new Dimension(petComboBox.getPreferredSize().width, 38));
        petComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                           boolean isSelected, boolean cellHasFocus) {
                JLabel lbl = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof PetDTO pet) {
                    lbl.setText(pet.getName() + " (" + pet.getSpecies() + ")");
                }
                return lbl;
            }
        });
        bodyPanel.add(petComboBox, gbc);

        // 2. Start Date & Time
        gbc.gridx = 0; gbc.gridy = 1;
        JLabel startLabel = new JLabel("Start Time:");
        startLabel.setFont(PawTheme.FONT_BODY_BOLD);
        startLabel.setForeground(PawTheme.TEXT_BODY);
        bodyPanel.add(startLabel, gbc);

        gbc.gridx = 1;
        bodyPanel.add(startPicker, gbc);

        // 3. End Date & Time
        gbc.gridx = 0; gbc.gridy = 2;
        JLabel endLabel = new JLabel("End Time:");
        endLabel.setFont(PawTheme.FONT_BODY_BOLD);
        endLabel.setForeground(PawTheme.TEXT_BODY);
        bodyPanel.add(endLabel, gbc);

        gbc.gridx = 1;
        bodyPanel.add(endPicker, gbc);

        // 4. Quick Duration Chips
        gbc.gridx = 0; gbc.gridy = 3;
        JLabel quickLabel = new JLabel("Duration Shortcuts:");
        quickLabel.setFont(PawTheme.FONT_SMALL);
        quickLabel.setForeground(PawTheme.TEXT_MUTED);
        bodyPanel.add(quickLabel, gbc);

        gbc.gridx = 1;
        JPanel quickChips = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        quickChips.setOpaque(false);
        quickChips.add(createDurationChip("+1h", 1));
        quickChips.add(createDurationChip("+2h", 2));
        quickChips.add(createDurationChip("+3h", 3));
        quickChips.add(createDurationChip("+4h", 4));
        quickChips.add(createDurationChip("+8h", 8));
        quickChips.add(createDurationChip("Overnight", 24));
        bodyPanel.add(quickChips, gbc);

        // 5. Payment Method
        gbc.gridx = 0; gbc.gridy = 4;
        JLabel payLabel = new JLabel("Payment Method:");
        payLabel.setFont(PawTheme.FONT_BODY_BOLD);
        payLabel.setForeground(PawTheme.TEXT_BODY);
        bodyPanel.add(payLabel, gbc);

        gbc.gridx = 1;
        paymentMethodCombo.setFont(PawTheme.FONT_BODY);
        paymentMethodCombo.setPreferredSize(new Dimension(paymentMethodCombo.getPreferredSize().width, 38));
        bodyPanel.add(paymentMethodCombo, gbc);

        // 6. Escrow Fee Summary Card
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        gbc.insets = new Insets(12, 6, 8, 6);

        PawComponents.PawCard summaryCard = new PawComponents.PawCard(12);
        summaryCard.setBackgroundTint(PawTheme.SUNNY_LIGHT);
        summaryCard.setBorderTint(PawTheme.SUNNY);
        summaryCard.setLayout(new BorderLayout(8, 8));

        JPanel summaryHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        summaryHeader.setOpaque(false);
        summaryHeader.add(new JLabel(PawIcons.shield(18, PawTheme.SUNNY)));
        JLabel summaryTitle = new JLabel("Escrow Payment Protection");
        summaryTitle.setFont(PawTheme.FONT_BODY_BOLD);
        summaryTitle.setForeground(PawTheme.SUNNY_TEXT);
        summaryHeader.add(summaryTitle);
        summaryCard.add(summaryHeader, BorderLayout.NORTH);

        calculationLabel.setFont(PawTheme.FONT_SECTION);
        calculationLabel.setForeground(PawTheme.SUNNY_TEXT);
        summaryCard.add(calculationLabel, BorderLayout.CENTER);

        JLabel escrowNote = new JLabel("Funds are held securely in escrow and only released after the sitting session concludes.");
        escrowNote.setFont(PawTheme.FONT_SMALL);
        escrowNote.setForeground(PawTheme.TEXT_MUTED);
        summaryCard.add(escrowNote, BorderLayout.SOUTH);

        bodyPanel.add(summaryCard, gbc);

        add(bodyPanel, BorderLayout.CENTER);

        // Footer Actions
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 14));
        footerPanel.setBackground(PawTheme.SURFACE);
        footerPanel.setBorder(new CompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, PawTheme.BORDER_LIGHT),
                new EmptyBorder(8, 20, 10, 20)
        ));

        PawComponents.PawButton cancelButton = new PawComponents.PawButton("Cancel", PawComponents.ButtonStyle.SECONDARY);
        cancelButton.addActionListener(e -> dispose());
        footerPanel.add(cancelButton);

        confirmButton.addActionListener(e -> handleConfirmBooking());
        footerPanel.add(confirmButton);

        add(footerPanel, BorderLayout.SOUTH);

        updateCalculation();
    }

    private JButton createDurationChip(String label, int hours) {
        JButton btn = new JButton(label);
        btn.setFont(PawTheme.FONT_SMALL);
        btn.setForeground(PawTheme.SUNNY_TEXT);
        btn.setBackground(PawTheme.SUNNY_LIGHT);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(254, 215, 170), 1, true),
                new EmptyBorder(4, 9, 4, 9)
        ));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> {
            LocalDateTime start = startPicker.getDateTime();
            if (start == null || start.isBefore(LocalDateTime.now())) {
                start = LocalDateTime.now().plusMinutes(5);
                startPicker.setDateTime(start);
            }
            endPicker.setDateTime(start.plusHours(hours));
            updateCalculation();
        });
        return btn;
    }

    private void loadOwnerPets() {
        Long ownerId = Session.getInstance().getUserId();
        if (ownerId == null) return;

        SwingWorker<List<PetDTO>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<PetDTO> doInBackground() {
                return ApiClient.getInstance().getList("/api/pets?ownerId=" + ownerId, PetDTO.class);
            }

            @Override
            protected void done() {
                try {
                    List<PetDTO> pets = get();
                    petComboBox.removeAllItems();
                    java.util.Set<String> seen = new java.util.HashSet<>();
                    for (PetDTO pet : pets) {
                        String key = (pet.getId() != null ? pet.getId() : pet.getName()) + "";
                        if (seen.add(key)) {
                            petComboBox.addItem(pet);
                        }
                    }
                } catch (Exception ex) {
                    Dialogs.showError(BookSitterDialog.this, "Failed to load owner pets: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void updateCalculation() {
        LocalDateTime start = startPicker != null ? startPicker.getDateTime() : null;
        LocalDateTime end = endPicker != null ? endPicker.getDateTime() : null;

        if (start == null || end == null) {
            calculationLabel.setText("Estimated total: ₹0.00");
            return;
        }

        if (!start.isBefore(end)) {
            calculationLabel.setText("Invalid interval (End time must be after Start time)");
            return;
        }

        Duration diff = Duration.between(start, end);
        long minutes = diff.toMinutes();
        long hours = (long) Math.ceil(minutes / 60.0);
        if (hours < 1) hours = 1;

        BigDecimal rate = sitter != null && sitter.getHourlyRate() != null ? sitter.getHourlyRate() : BigDecimal.valueOf(250.00);
        BigDecimal total = rate.multiply(BigDecimal.valueOf(hours));

        calculationLabel.setText("Estimated Total: ₹" + total + " (" + hours + " hr" + (hours > 1 ? "s" : "") + " @ ₹" + rate + "/hr)");
    }

    private void handleConfirmBooking() {
        PetDTO selectedPet = (PetDTO) petComboBox.getSelectedItem();
        if (selectedPet == null) {
            Dialogs.showWarning(this, "Please select a pet to proceed with booking.");
            return;
        }

        LocalDateTime start = startPicker.getDateTime();
        LocalDateTime end = endPicker.getDateTime();

        if (start == null || end == null) {
            Dialogs.showWarning(this, "Please select both a valid start time and end time.");
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        // Allow a small 1-minute buffer for form fill time
        if (start.isBefore(now.minusMinutes(1))) {
            Dialogs.showWarning(this, "Sitting start time cannot be in the past. Please select a current or future time.");
            return;
        }

        if (!start.isBefore(end)) {
            Dialogs.showWarning(this, "Start time must be before end time.");
            return;
        }

        Duration duration = Duration.between(start, end);
        if (duration.toMinutes() < 30) {
            Dialogs.showWarning(this, "Minimum pet sitting booking duration is 30 minutes.");
            return;
        }

        String paymentMethod = (String) paymentMethodCombo.getSelectedItem();
        Long ownerId = Session.getInstance().getUserId();

        DateTimeFormatter isoFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

        Map<String, Object> req = new HashMap<>();
        req.put("petId", selectedPet.getPetId());
        req.put("sitterId", sitter.getUserId());
        req.put("ownerId", ownerId);
        req.put("startTime", start.format(isoFmt));
        req.put("endTime", end.format(isoFmt));
        req.put("paymentMethod", paymentMethod);

        confirmButton.setEnabled(false);
        confirmButton.setText("Processing Booking & Escrow...");

        SwingWorker<SittingBookingDTO, Void> worker = new SwingWorker<>() {
            @Override
            protected SittingBookingDTO doInBackground() {
                return ApiClient.getInstance().post("/api/sitter-bookings", req, SittingBookingDTO.class);
            }

            @Override
            protected void done() {
                confirmButton.setEnabled(true);
                confirmButton.setText("Confirm Booking & Escrow Hold");

                try {
                    SittingBookingDTO booking = get();
                    Dialogs.showSuccess(BookSitterDialog.this,
                            "Sitter booking confirmed successfully!\n\n" +
                            "Booking ID: #" + booking.getId() + "\n" +
                            "Pet: " + (booking.getPetName() != null ? booking.getPetName() : selectedPet.getName()) + "\n" +
                            "Sitter: " + (booking.getSitterName() != null ? booking.getSitterName() : sitter.getName()) + "\n" +
                            "Total Fee: ₹" + booking.getTotalAmount() + "\n" +
                            "Payment Status: " + booking.getPaymentStatus() + " (Escrow Secured)"
                    );

                    if (onSuccess != null) {
                        onSuccess.run();
                    }
                    dispose();
                } catch (Exception ex) {
                    String msg = ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage();
                    Dialogs.showError(BookSitterDialog.this, "Booking failed: " + msg);
                }
            }
        };
        worker.execute();
    }
}
