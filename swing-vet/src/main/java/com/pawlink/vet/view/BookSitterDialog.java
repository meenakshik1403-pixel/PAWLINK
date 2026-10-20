package com.pawlink.vet.view;

import com.pawlink.vet.api.ApiClient;
import com.pawlink.vet.api.ApiException;
import com.pawlink.vet.api.Session;
import com.pawlink.vet.base.Dialogs;
import com.pawlink.vet.model.PetDTO;
import com.pawlink.vet.model.SitterDTO;
import com.pawlink.vet.model.SittingBookingDTO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Modal Dialog for pet owners to configure dates, choose pets, and confirm sitter bookings.
 * Pairs with P7 payment processing by displaying the escrow payment method and total fee.
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
        this.paymentMethodCombo = new JComboBox<>(new String[]{"CARD", "UPI", "WALLET"});
        this.calculationLabel = new JLabel("Estimated total: $0.00 (0 hours)");
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

        JLabel sitterInfo = new JLabel("Sitter: " + sitter.getName() + " | Rate: $" +
                (sitter.getHourlyRate() != null ? sitter.getHourlyRate() : "25.00") + " / hr " +
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
        gbc.gridx = 0;
        gbc.gridy = 0;

        // 1. Pet Selector
        gbc.gridwidth = 1;
        JLabel petLabel = new JLabel("Select Pet:");
        petLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        bodyPanel.add(petLabel, gbc);

        gbc.gridx = 1;
        petComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        bodyPanel.add(petComboBox, gbc);

        // 2. Start Date & Time
        gbc.gridx = 0;
        gbc.gridy = 1;
        JLabel startLabel = new JLabel("Start Time:");
        startLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        bodyPanel.add(startLabel, gbc);

        gbc.gridx = 1;
        JSpinner.DateEditor startEditor = new JSpinner.DateEditor(startSpinner, "yyyy-MM-dd HH:mm");
        startSpinner.setEditor(startEditor);
        Date defaultStart = new Date(System.currentTimeMillis() + 86400000L); // Tomorrow
        startSpinner.setValue(defaultStart);
        startSpinner.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        startSpinner.addChangeListener(e -> updateCalculation());
        bodyPanel.add(startSpinner, gbc);

        // 3. End Date & Time
        gbc.gridx = 0;
        gbc.gridy = 2;
        JLabel endLabel = new JLabel("End Time:");
        endLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        bodyPanel.add(endLabel, gbc);

        gbc.gridx = 1;
        JSpinner.DateEditor endEditor = new JSpinner.DateEditor(endSpinner, "yyyy-MM-dd HH:mm");
        endSpinner.setEditor(endEditor);
        Date defaultEnd = new Date(defaultStart.getTime() + 14400000L); // +4 hours
        endSpinner.setValue(defaultEnd);
        endSpinner.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        endSpinner.addChangeListener(e -> updateCalculation());
        bodyPanel.add(endSpinner, gbc);

        // 4. Payment Method
        gbc.gridx = 0;
        gbc.gridy = 3;
        JLabel paymentLabel = new JLabel("Payment Method:");
        paymentLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        bodyPanel.add(paymentLabel, gbc);

        gbc.gridx = 1;
        paymentMethodCombo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        bodyPanel.add(paymentMethodCombo, gbc);

        // 5. Fee Summary Box
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        JPanel calcBox = new JPanel(new BorderLayout());
        calcBox.setBackground(new Color(241, 245, 249));
        calcBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225)),
                new EmptyBorder(12, 12, 12, 12)
        ));

        calculationLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        calculationLabel.setForeground(new Color(30, 41, 59));
        calcBox.add(calculationLabel, BorderLayout.NORTH);

        JLabel escrowNote = new JLabel("🔒 Funds will be held in Escrow (status: HELD) until job is completed.");
        escrowNote.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        escrowNote.setForeground(new Color(100, 116, 139));
        calcBox.add(escrowNote, BorderLayout.SOUTH);

        bodyPanel.add(calcBox, gbc);

        add(bodyPanel, BorderLayout.CENTER);

        // Footer Actions
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 14));
        footerPanel.setBackground(new Color(248, 250, 252));
        footerPanel.setBorder(new EmptyBorder(4, 15, 8, 15));

        JButton cancelButton = new JButton("Cancel");
        cancelButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cancelButton.addActionListener(e -> dispose());
        footerPanel.add(cancelButton);

        confirmButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        confirmButton.setBackground(new Color(16, 185, 129)); // Green
        confirmButton.setForeground(Color.WHITE);
        confirmButton.setFocusable(false);
        confirmButton.addActionListener(e -> handleConfirmBooking());
        footerPanel.add(confirmButton);

        add(footerPanel, BorderLayout.SOUTH);

        updateCalculation();
    }

    private void loadOwnerPets() {
        Long ownerId = Session.getInstance().getUserId();
        if (ownerId == null) {
            Dialogs.showError(this, "Session expired or user not logged in.");
            return;
        }

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
                    for (PetDTO pet : pets) {
                        petComboBox.addItem(pet);
                    }
                    if (pets.isEmpty()) {
                        Dialogs.showWarning(BookSitterDialog.this,
                                "You haven't registered any pets yet! Please add a pet in 'My Pets' first.");
                        confirmButton.setEnabled(false);
                    }
                } catch (Exception ex) {
                    Dialogs.showError(BookSitterDialog.this, "Failed to load your pets: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void updateCalculation() {
        Date startDate = (Date) startSpinner.getValue();
        Date endDate = (Date) endSpinner.getValue();

        if (startDate == null || endDate == null) {
            calculationLabel.setText("Estimated total: $0.00");
            return;
        }

        long diffMs = endDate.getTime() - startDate.getTime();
        long minutes = Math.max(diffMs / 60000, 60);
        long hours = (long) Math.ceil(minutes / 60.0);

        BigDecimal rate = sitter.getHourlyRate() != null ? sitter.getHourlyRate() : BigDecimal.valueOf(25.00);
        BigDecimal total = rate.multiply(BigDecimal.valueOf(hours));

        calculationLabel.setText("Estimated Total: $" + total + " (" + hours + " hrs @ $" + rate + "/hr)");
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

        Map<String, Object> req = new HashMap<>();
        req.put("petId", selectedPet.getPetId());
        req.put("sitterId", sitter.getUserId());
        req.put("ownerId", ownerId);
        req.put("startTime", start.toString());
        req.put("endTime", end.toString());
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
                            "Total Fee: $" + booking.getTotalAmount() + "\n" +
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
