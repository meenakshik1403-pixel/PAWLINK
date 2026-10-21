package com.pawlink.vet.view;

import com.pawlink.vet.api.ApiClient;
import com.pawlink.vet.base.Dialogs;
import com.pawlink.vet.base.PawComponents;
import com.pawlink.vet.base.PawIcons;
import com.pawlink.vet.base.PawTheme;
import com.pawlink.vet.model.CheckinDTO;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Modal dialog for pet sitters to publish timestamped care check-in updates.
 * Modernized with pure Java2D vector icons, FlatLaf styling, and micro-animated buttons.
 */
public class AddCheckinDialog extends JDialog {

    private final Long bookingId;
    private final String petName;
    private final Runnable onSuccess;
    private final JTextArea noteArea;
    private final PawComponents.PawButton submitButton;

    public AddCheckinDialog(Frame parent, Long bookingId, String petName, Runnable onSuccess) {
        super(parent, "Post Pet Care Check-In - " + (petName != null ? petName : ""), true);
        this.bookingId = bookingId;
        this.petName = petName;
        this.onSuccess = onSuccess;

        this.noteArea = new JTextArea(5, 30);
        this.submitButton = new PawComponents.PawButton("Publish Check-In Update", PawIcons.check(14, Color.WHITE), PawComponents.ButtonStyle.MINT);

        initUI();
    }

    private void initUI() {
        setSize(520, 500);
        setLocationRelativeTo(getParent());
        setResizable(false);
        setLayout(new BorderLayout());

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout(14, 0));
        headerPanel.setBackground(PawTheme.SIDEBAR_BG);
        headerPanel.setBorder(new EmptyBorder(18, 24, 18, 24));

        JLabel iconLabel = new JLabel(PawIcons.sitter(26, PawTheme.MINT));
        headerPanel.add(iconLabel, BorderLayout.WEST);

        JPanel titleBlock = new JPanel(new GridLayout(2, 1, 0, 4));
        titleBlock.setOpaque(false);

        JLabel title = new JLabel("Log Real-Time Care Update");
        title.setFont(PawTheme.FONT_TITLE);
        title.setForeground(Color.WHITE);
        titleBlock.add(title);

        JLabel sub = new JLabel("Booking #" + bookingId + " for " + (petName != null ? petName : "Pet"));
        sub.setFont(PawTheme.FONT_SMALL);
        sub.setForeground(PawTheme.TEXT_SUBTLE);
        titleBlock.add(sub);

        headerPanel.add(titleBlock, BorderLayout.CENTER);
        add(headerPanel, BorderLayout.NORTH);

        // Body
        JPanel bodyPanel = new JPanel(new BorderLayout(0, 14));
        bodyPanel.setBorder(new EmptyBorder(18, 24, 18, 24));
        bodyPanel.setBackground(PawTheme.CANVAS);

        // Quick Preset Chips
        JPanel chipsContainer = new JPanel(new BorderLayout(0, 8));
        chipsContainer.setOpaque(false);

        JLabel presetsLabel = new JLabel("Quick Presets (tap to add to note):");
        presetsLabel.setFont(PawTheme.FONT_BODY_BOLD);
        presetsLabel.setForeground(PawTheme.TEXT_BODY);
        chipsContainer.add(presetsLabel, BorderLayout.NORTH);

        JPanel chipsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        chipsPanel.setOpaque(false);

        addQuickChip(chipsPanel, PawIcons.bone(13, PawTheme.SUNNY_TEXT), "Fed meal & fresh water", PawTheme.SUNNY_LIGHT, PawTheme.SUNNY_TEXT, PawTheme.SUNNY);
        addQuickChip(chipsPanel, PawIcons.paw(13, PawTheme.MINT_TEXT), "30-min walk completed", PawTheme.MINT_LIGHT, PawTheme.MINT_TEXT, PawTheme.MINT);
        addQuickChip(chipsPanel, PawIcons.vaccine(13, PawTheme.SKY_TEXT), "Medication administered", PawTheme.SKY_LIGHT, PawTheme.SKY_TEXT, PawTheme.SKY);
        addQuickChip(chipsPanel, PawIcons.heart(13, PawTheme.CORAL_TEXT), "Fun playtime in yard", PawTheme.CORAL_LIGHT, PawTheme.CORAL_TEXT, PawTheme.CORAL);
        addQuickChip(chipsPanel, PawIcons.star(13, PawTheme.LILAC_TEXT), "Resting comfortably", PawTheme.LILAC_LIGHT, PawTheme.LILAC_TEXT, PawTheme.LILAC);

        chipsContainer.add(chipsPanel, BorderLayout.CENTER);
        bodyPanel.add(chipsContainer, BorderLayout.NORTH);

        // Text area
        noteArea.setFont(PawTheme.FONT_BODY);
        noteArea.setLineWrap(true);
        noteArea.setWrapStyleWord(true);
        noteArea.setBorder(new EmptyBorder(12, 14, 12, 14));

        JScrollPane scrollPane = new JScrollPane(noteArea);
        scrollPane.setBorder(new CompoundBorder(
                new LineBorder(PawTheme.BORDER_STRONG, 1, true),
                new EmptyBorder(0, 0, 0, 0)
        ));
        bodyPanel.add(scrollPane, BorderLayout.CENTER);

        add(bodyPanel, BorderLayout.CENTER);

        // Footer Actions
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 14));
        footerPanel.setBackground(PawTheme.SURFACE);
        footerPanel.setBorder(new CompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, PawTheme.BORDER_LIGHT),
                new EmptyBorder(8, 20, 10, 20)
        ));

        PawComponents.PawButton cancelBtn = new PawComponents.PawButton("Cancel", PawComponents.ButtonStyle.SECONDARY);
        cancelBtn.addActionListener(e -> dispose());
        footerPanel.add(cancelBtn);

        submitButton.addActionListener(e -> handleSubmit());
        footerPanel.add(submitButton);

        add(footerPanel, BorderLayout.SOUTH);
    }

    private void addQuickChip(JPanel container, Icon icon, String text, Color bg, Color fg, Color borderColor) {
        JButton chip = new JButton(text, icon);
        chip.setFont(PawTheme.FONT_SMALL_BOLD);
        chip.setBackground(bg);
        chip.setForeground(fg);
        chip.setFocusable(false);
        chip.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        chip.setIconTextGap(6);
        chip.setBorder(new CompoundBorder(
                new LineBorder(new Color(borderColor.getRed(), borderColor.getGreen(), borderColor.getBlue(), 120), 1, true),
                new EmptyBorder(6, 12, 6, 12)
        ));
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
