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
import java.awt.*;
import java.util.List;

/**
 * Modal dialog showing chronological care check-in updates for a booking.
 * Modernized with pure Java2D vector icons, card feeds, and high-contrast typography.
 */
public class CheckinsTimelineDialog extends JDialog {

    private final Long bookingId;
    private final String petName;
    private final JPanel timelineContainer;
    private final JLabel countLabel;

    public CheckinsTimelineDialog(Frame parent, Long bookingId, String petName) {
        super(parent, "Care Timeline - Booking #" + bookingId + " (" + (petName != null ? petName : "") + ")", true);
        this.bookingId = bookingId;
        this.petName = petName;

        this.timelineContainer = new JPanel();
        this.timelineContainer.setLayout(new BoxLayout(timelineContainer, BoxLayout.Y_AXIS));
        this.timelineContainer.setBackground(PawTheme.SURFACE);

        this.countLabel = new JLabel("Loading check-ins...");

        initUI();
        loadTimeline();
    }

    private void initUI() {
        setSize(580, 600);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout());

        // Header
        JPanel header = new JPanel(new BorderLayout(14, 0));
        header.setBackground(PawTheme.SIDEBAR_BG);
        header.setBorder(new EmptyBorder(18, 24, 18, 24));

        JLabel iconLabel = new JLabel(PawIcons.clock(26, PawTheme.SUNNY));
        header.add(iconLabel, BorderLayout.WEST);

        JPanel titleBlock = new JPanel(new GridLayout(2, 1, 0, 4));
        titleBlock.setOpaque(false);

        JLabel title = new JLabel("Pet Care Check-In Timeline");
        title.setFont(PawTheme.FONT_TITLE);
        title.setForeground(Color.WHITE);
        titleBlock.add(title);

        JLabel sub = new JLabel("Real-time updates posted by sitter for " + (petName != null ? petName : "pet"));
        sub.setFont(PawTheme.FONT_SMALL);
        sub.setForeground(PawTheme.TEXT_SUBTLE);
        titleBlock.add(sub);

        header.add(titleBlock, BorderLayout.CENTER);
        add(header, BorderLayout.NORTH);

        // Center ScrollPane: padding inside container keeps scrollbar flush against panel border
        timelineContainer.setBorder(new EmptyBorder(16, 20, 16, 20));
        JScrollPane scrollPane = new JScrollPane(timelineContainer);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(PawTheme.SURFACE);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        // Footer Bar
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(PawTheme.SURFACE);
        footer.setBorder(new CompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, PawTheme.BORDER_LIGHT),
                new EmptyBorder(12, 20, 12, 20)
        ));

        countLabel.setFont(PawTheme.FONT_BODY);
        countLabel.setForeground(PawTheme.TEXT_MUTED);
        footer.add(countLabel, BorderLayout.WEST);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setOpaque(false);

        PawComponents.PawButton refreshBtn = new PawComponents.PawButton("Refresh", PawIcons.refresh(12, PawTheme.TEXT_BODY), PawComponents.ButtonStyle.SECONDARY);
        refreshBtn.addActionListener(e -> loadTimeline());
        btnPanel.add(refreshBtn);

        PawComponents.PawButton closeBtn = new PawComponents.PawButton("Close", PawComponents.ButtonStyle.SECONDARY);
        closeBtn.addActionListener(e -> dispose());
        btnPanel.add(closeBtn);

        footer.add(btnPanel, BorderLayout.EAST);
        add(footer, BorderLayout.SOUTH);
    }

    public void loadTimeline() {
        timelineContainer.removeAll();
        countLabel.setText("Fetching check-ins...");

        SwingWorker<List<CheckinDTO>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<CheckinDTO> doInBackground() {
                return ApiClient.getInstance().getList("/api/sitter-bookings/" + bookingId + "/checkins", CheckinDTO.class);
            }

            @Override
            protected void done() {
                try {
                    List<CheckinDTO> checkins = get();
                    timelineContainer.removeAll();

                    if (checkins == null || checkins.isEmpty()) {
                        JPanel emptyPanel = new JPanel(new GridBagLayout());
                        emptyPanel.setOpaque(false);
                        JLabel emptyLabel = new JLabel("No check-in updates recorded yet.");
                        emptyLabel.setFont(PawTheme.FONT_BODY);
                        emptyLabel.setForeground(PawTheme.TEXT_MUTED);
                        emptyPanel.add(emptyLabel);
                        timelineContainer.add(emptyPanel);
                        countLabel.setText("0 updates logged.");
                    } else {
                        for (CheckinDTO c : checkins) {
                            JPanel card = createCheckinCard(c);
                            card.setAlignmentX(Component.LEFT_ALIGNMENT);
                            timelineContainer.add(card);
                            timelineContainer.add(Box.createRigidArea(new Dimension(0, 10)));
                        }
                        timelineContainer.add(Box.createVerticalGlue());
                        countLabel.setText(checkins.size() + " update(s) recorded.");
                    }

                    timelineContainer.revalidate();
                    timelineContainer.repaint();
                } catch (Exception ex) {
                    countLabel.setText("Failed to load check-ins.");
                    Dialogs.showError(CheckinsTimelineDialog.this, "Error fetching check-ins: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private JPanel createCheckinCard(CheckinDTO c) {
        PawComponents.PawCard card = new PawComponents.PawCard(12);
        card.setBackgroundTint(PawTheme.SUNNY_LIGHT);
        card.setBorderTint(PawTheme.SUNNY);
        card.setBorder(new EmptyBorder(10, 14, 10, 14));
        card.setLayout(new BorderLayout(8, 6));

        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        header.setOpaque(false);
        header.add(new JLabel(PawIcons.clock(14, PawTheme.SUNNY_TEXT)));

        JLabel timeLabel = new JLabel(c.getFormattedTimestamp());
        timeLabel.setFont(PawTheme.FONT_SMALL_BOLD);
        timeLabel.setForeground(PawTheme.SUNNY_TEXT);
        header.add(timeLabel);

        card.add(header, BorderLayout.NORTH);

        String noteText = com.pawlink.vet.base.BasePanel.cleanEmoji(c.getNote());
        JLabel noteLabel = new JLabel("<html><body>" + (noteText.isEmpty() ? c.getNote() : noteText) + "</body></html>");
        noteLabel.setFont(PawTheme.FONT_BODY);
        noteLabel.setForeground(PawTheme.TEXT_TITLE);
        card.add(noteLabel, BorderLayout.CENTER);

        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, card.getPreferredSize().height));
        return card;
    }
}
