package com.pawlink.vet.view;

import com.pawlink.vet.api.ApiClient;
import com.pawlink.vet.api.Session;
import com.pawlink.vet.base.Dialogs;
import com.pawlink.vet.model.PetDTO;
import com.pawlink.vet.model.SitterDTO;
import com.pawlink.vet.model.SittingBookingDTO;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Interactive Dialog for booking an approved pet sitter with escrow payment hold.
 * Allows selecting the pet, date/time interval, and simulated payment method (CARD, UPI, WALLET).
 */
public class BookSitterDialog extends JDialog {

    private final SitterDTO sitter;
    private final Runnable onSuccess;

    private final JComboBox<PetDTO> petComboBox;
    private final JSpinner startSpinner;
    private final JSpinner endSpinner;
    private final JComboBox<String> paymentMethodCombo;
    private final JLabel calculationLabel;
    private final JButton confirmButton;

    public BookSitterDialog(Frame parent, SitterDTO sitter, Runnable onSuccess) {
        super(parent, "🏡 Book Sitter - " + (sitter != null ? sitter.getName() : ""), true);
        this.sitter = sitter;
        this.onSuccess = onSuccess;

        this.petComboBox = new JComboBox<>();
        this.startSpinner = new JSpinner(new SpinnerDateModel());
        this.endSpinner = new JSpinner(new SpinnerDateModel());
        this.paymentMethodCombo = new JComboBox<>(new String[]{"UPI", "CARD", "WALLET"});
        this.calculationLabel = new JLabel("Estimated total: ₹0.00 (0 hours)");
        this.confirmButton = new JButton("Confirm Booking & Escrow Hold");

        initUI();
        loadOwnerPets();
    }

    private void initUI() {
        setSize(520, 560);
        setLocationRelativeTo(getParent());
        setResizable(false);
        setLayout(new BorderLayout());

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(30, 41, 59));
        headerPanel.setBorder(new EmptyBorder(16, 20, 16, 20));

        JLabel titleLabel = new JLabel("Book Pet Sitting Session");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.NORTH);

        JLabel sitterInfo = new JLabel("Sitter: " + sitter.getName() + " | Rate: ₹" +
                (sitter.getHourlyRate() != null ? sitter.getHourlyRate() : "250.00") + " / hr " +
                (sitter.isVerified() ? "✓ Verified" : "⏳ Pending"));
        sitterInfo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        sitterInfo.setForeground(new Color(203, 213, 225));
        headerPanel.add(sitterInfo, BorderLayout.SOUTH);

        add(headerPanel, BorderLayout.NORTH);

        // Form Body
        JPanel bodyPanel = new JPanel(new GridBagLayout());
        bodyPanel.setBorder(new EmptyBorder(20, 24, 20, 24));
        bodyPanel.setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 6, 8, 6);

        // 1. Pet Selector
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.35;
        JLabel petLabel = new JLabel("Select Pet:");
        petLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        bodyPanel.add(petLabel, gbc);

        gbc.gridx = 1; gbc.weightx = 0.65;
        petComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 13));
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
        startLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        bodyPanel.add(startLabel, gbc);

        gbc.gridx = 1;
        startSpinner.setEditor(new JSpinner.DateEditor(startSpinner, "yyyy-MM-dd HH:mm"));
        startSpinner.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        startSpinner.addChangeListener(e -> updateCalculation());
        bodyPanel.add(startSpinner, gbc);

        // 3. End Date & Time
        gbc.gridx = 0; gbc.gridy = 2;
        JLabel endLabel = new JLabel("End Time:");
        endLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        bodyPanel.add(endLabel, gbc);

        gbc.gridx = 1;
        // Default end time = start time + 3 hours
        Date now = new Date();
        startSpinner.setValue(now);
        Date defaultEnd = new Date(now.getTime() + (3 * 3600 * 1000));
        endSpinner.setValue(defaultEnd);
        endSpinner.setEditor(new JSpinner.DateEditor(endSpinner, "yyyy-MM-dd HH:mm"));
        endSpinner.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        endSpinner.addChangeListener(e -> updateCalculation());
        bodyPanel.add(endSpinner, gbc);

        // 4. Payment Method
        gbc.gridx = 0; gbc.gridy = 3;
        JLabel payLabel = new JLabel("Payment Method:");
        payLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        bodyPanel.add(payLabel, gbc);

        gbc.gridx = 1;
        paymentMethodCombo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        bodyPanel.add(paymentMethodCombo, gbc);

        // 5. Escrow Fee Summary Card
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        gbc.insets = new Insets(16, 6, 8, 6);

        JPanel summaryCard = new JPanel(new BorderLayout(8, 6));
        summaryCard.setBackground(new Color(241, 245, 249));
        summaryCard.setBorder(new CompoundBorder(
                new LineBorder(new Color(203, 213, 225), 1, true),
                new EmptyBorder(12, 16, 12, 16)
        ));

        JLabel summaryTitle = new JLabel("🛡️ Escrow Payment Protection");
        summaryTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        summaryTitle.setForeground(new Color(15, 23, 42));
        summaryCard.add(summaryTitle, BorderLayout.NORTH);

        calculationLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        calculationLabel.setForeground(new Color(30, 41, 59));
        summaryCard.add(calculationLabel, BorderLayout.CENTER);

        JLabel escrowNote = new JLabel("<html>Funds are held safely in escrow and only released after sitting completes.</html>");
        escrowNote.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        escrowNote.setForeground(new Color(100, 116, 139));
        summaryCard.add(escrowNote, BorderLayout.SOUTH);

        bodyPanel.add(summaryCard, gbc);

        add(bodyPanel, BorderLayout.CENTER);

        // Footer Actions
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 14));
        footerPanel.setBackground(new Color(248, 250, 252));
        footerPanel.setBorder(new CompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(226, 232, 240)),
                new EmptyBorder(4, 16, 8, 16)
        ));

        JButton cancelButton = new JButton("Cancel");
        cancelButton.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cancelButton.addActionListener(e -> dispose());
        footerPanel.add(cancelButton);

        confirmButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        confirmButton.setBackground(new Color(79, 70, 229)); // Indigo
        confirmButton.setForeground(Color.WHITE);
        confirmButton.setFocusable(false);
        confirmButton.addActionListener(e -> handleConfirmBooking());
        footerPanel.add(confirmButton);

        add(footerPanel, BorderLayout.SOUTH);

        updateCalculation();
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
        Date startDate = (Date) startSpinner.getValue();
        Date endDate = (Date) endSpinner.getValue();

        if (startDate == null || endDate == null) {
            calculationLabel.setText("Estimated total: ₹0.00");
            return;
        }

        long diffMs = endDate.getTime() - startDate.getTime();
        long minutes = Math.max(diffMs / 60000, 60);
        long hours = (long) Math.ceil(minutes / 60.0);

        BigDecimal rate = sitter.getHourlyRate() != null ? sitter.getHourlyRate() : BigDecimal.valueOf(250.00);
        BigDecimal total = rate.multiply(BigDecimal.valueOf(hours));

        calculationLabel.setText("Estimated Total: ₹" + total + " (" + hours + " hrs @ ₹" + rate + "/hr)");
    }

    private void handleConfirmBooking() {
        PetDTO selectedPet = (PetDTO) petComboBox.getSelectedItem();
        if (selectedPet == null) {
            Dialogs.showWarning(this, "Please select a pet to proceed with booking.");
            return;
        }

        Date startDate = (Date) startSpinner.getValue();
        Date endDate = (Date) endSpinner.getValue();

        LocalDateTime start = startDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        LocalDateTime end = endDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();

        if (!start.isBefore(end)) {
            Dialogs.showWarning(this, "Start time must be before end time.");
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
                            "🎉 Sitter booking confirmed successfully!\n\n" +
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
