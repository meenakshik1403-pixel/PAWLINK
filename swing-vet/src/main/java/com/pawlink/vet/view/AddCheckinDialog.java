package com.pawlink.vet.view;

import com.pawlink.vet.api.ApiClient;
import com.pawlink.vet.base.Dialogs;
import com.pawlink.vet.model.CheckinDTO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Modal dialog for pet sitters to publish timestamped care check-in updates.
 */
public class AddCheckinDialog extends JDialog {

    private final Long bookingId;
    private final String petName;
    private final Runnable onSuccess;
    private final JTextArea noteArea;
    private final JButton submitButton;

    public AddCheckinDialog(Frame parent, Long bookingId, String petName, Runnable onSuccess) {
        super(parent, "📸 Post Pet Care Check-In - " + (petName != null ? petName : ""), true);
        this.bookingId = bookingId;
        this.petName = petName;
        this.onSuccess = onSuccess;

        this.noteArea = new JTextArea(5, 30);
        this.submitButton = new JButton("Publish Check-In Update");

        initUI();
    }

    private void initUI() {
        setSize(480, 420);
        setLocationRelativeTo(getParent());
        setResizable(false);
        setLayout(new BorderLayout());

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(30, 41, 59));
        headerPanel.setBorder(new EmptyBorder(14, 18, 14, 18));

        JLabel title = new JLabel("Log Real-Time Care Update");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(Color.WHITE);
        headerPanel.add(title, BorderLayout.NORTH);

        JLabel sub = new JLabel("Booking #" + bookingId + " for " + (petName != null ? petName : "Pet"));
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        sub.setForeground(new Color(203, 213, 225));
        headerPanel.add(sub, BorderLayout.SOUTH);

        add(headerPanel, BorderLayout.NORTH);

        // Body
        JPanel bodyPanel = new JPanel(new BorderLayout(0, 10));
        bodyPanel.setBorder(new EmptyBorder(15, 18, 15, 18));
        bodyPanel.setBackground(Color.WHITE);

        // Quick Preset Chips
        JPanel chipsContainer = new JPanel(new BorderLayout(0, 6));
        chipsContainer.setOpaque(false);

        JLabel presetsLabel = new JLabel("Quick presets (click to append):");
        presetsLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        presetsLabel.setForeground(new Color(100, 116, 139));
        chipsContainer.add(presetsLabel, BorderLayout.NORTH);

        JPanel chipsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        chipsPanel.setOpaque(false);

        addQuickChip(chipsPanel, "🍽️ Fed meal & fresh water");
        addQuickChip(chipsPanel, "🦮 30-min walk completed");
        addQuickChip(chipsPanel, "💊 Medication administered");
        addQuickChip(chipsPanel, "🎾 Fun playtime in yard");
        addQuickChip(chipsPanel, "💤 Resting comfortably");

        chipsContainer.add(chipsPanel, BorderLayout.CENTER);
        bodyPanel.add(chipsContainer, BorderLayout.NORTH);

        // Text area
        noteArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        noteArea.setLineWrap(true);
        noteArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(noteArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Check-In Note:"));
        bodyPanel.add(scrollPane, BorderLayout.CENTER);

        add(bodyPanel, BorderLayout.CENTER);

        // Footer Actions
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        footerPanel.setBackground(new Color(248, 250, 252));
        footerPanel.setBorder(new EmptyBorder(4, 15, 8, 15));

        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cancelBtn.addActionListener(e -> dispose());
        footerPanel.add(cancelBtn);

        submitButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        submitButton.setBackground(new Color(16, 185, 129));
        submitButton.setForeground(Color.WHITE);
        submitButton.setFocusable(false);
        submitButton.addActionListener(e -> handleSubmit());
        footerPanel.add(submitButton);

        add(footerPanel, BorderLayout.SOUTH);
    }

    private void addQuickChip(JPanel container, String text) {
        JButton chip = new JButton(text);
        chip.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        chip.setBackground(new Color(241, 245, 249));
        chip.setForeground(new Color(51, 65, 85));
        chip.setFocusable(false);
        chip.addActionListener(e -> {
            String current = noteArea.getText().trim();
            if (current.isEmpty()) {
                noteArea.setText(text + ".");
            } else {
                noteArea.setText(current + " " + text + ".");
            }
        });
        container.add(chip);
    }

    private void handleSubmit() {
        String note = noteArea.getText().trim();
        if (note.isEmpty()) {
            Dialogs.showWarning(this, "Please enter a check-in note before submitting.");
            return;
        }

        submitButton.setEnabled(false);
        submitButton.setText("Publishing...");

        Map<String, String> body = new HashMap<>();
        body.put("note", note);

        SwingWorker<CheckinDTO, Void> worker = new SwingWorker<>() {
            @Override
            protected CheckinDTO doInBackground() {
                return ApiClient.getInstance().post("/api/sitter-bookings/" + bookingId + "/checkins", body, CheckinDTO.class);
            }

            @Override
            protected void done() {
                submitButton.setEnabled(true);
                submitButton.setText("Publish Check-In Update");

                try {
                    get();
                    Dialogs.showSuccess(AddCheckinDialog.this, "Check-in note published successfully!");
                    if (onSuccess != null) {
                        onSuccess.run();
                    }
                    dispose();
                } catch (Exception ex) {
                    Dialogs.showError(AddCheckinDialog.this, "Failed to publish check-in: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }
}
