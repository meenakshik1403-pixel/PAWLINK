package com.pawlink.vet.view;

import com.pawlink.vet.api.ApiClient;
import com.pawlink.vet.base.Dialogs;
import com.pawlink.vet.model.CheckinDTO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

/**
 * Modal dialog showing chronological care check-in updates for a booking.
 */
public class CheckinsTimelineDialog extends JDialog {

    private final Long bookingId;
    private final String petName;
    private final JPanel timelineContainer;
    private final JLabel countLabel;

    public CheckinsTimelineDialog(Frame parent, Long bookingId, String petName) {
        super(parent, "📸 Care Timeline - Booking #" + bookingId + " (" + (petName != null ? petName : "") + ")", true);
        this.bookingId = bookingId;
        this.petName = petName;

        this.timelineContainer = new JPanel();
        this.timelineContainer.setLayout(new BoxLayout(timelineContainer, BoxLayout.Y_AXIS));
        this.timelineContainer.setBackground(Color.WHITE);

        this.countLabel = new JLabel("Loading check-ins...");

        initUI();
        loadTimeline();
    }

    private void initUI() {
        setSize(540, 560);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout());

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(30, 41, 59));
        header.setBorder(new EmptyBorder(14, 20, 14, 20));

        JLabel title = new JLabel("📸 Pet Care Check-In Timeline");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(Color.WHITE);
        header.add(title, BorderLayout.NORTH);

        JLabel sub = new JLabel("Real-time updates posted by sitter for " + (petName != null ? petName : "pet"));
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        sub.setForeground(new Color(203, 213, 225));
        header.add(sub, BorderLayout.SOUTH);

        add(header, BorderLayout.NORTH);

        // Center ScrollPane
        JScrollPane scrollPane = new JScrollPane(timelineContainer);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        // Footer Bar
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(new Color(248, 250, 252));
        footer.setBorder(new EmptyBorder(10, 18, 10, 18));

        countLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        countLabel.setForeground(new Color(100, 116, 139));
        footer.add(countLabel, BorderLayout.WEST);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnPanel.setOpaque(false);

        JButton refreshBtn = new JButton("🔄 Refresh");
        refreshBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        refreshBtn.addActionListener(e -> loadTimeline());
        btnPanel.add(refreshBtn);

        JButton closeBtn = new JButton("Close");
        closeBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
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
                        emptyLabel.setFont(new Font("Segoe UI", Font.ITALIC, 13));
                        emptyLabel.setForeground(new Color(148, 163, 184));
                        emptyPanel.add(emptyLabel);
                        timelineContainer.add(emptyPanel);
                        countLabel.setText("0 updates logged.");
                    } else {
                        for (CheckinDTO c : checkins) {
                            timelineContainer.add(createCheckinCard(c));
                            timelineContainer.add(Box.createRigidArea(new Dimension(0, 10)));
                        }
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
        JPanel card = new JPanel(new BorderLayout(8, 6));
        card.setBackground(new Color(248, 250, 252));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, new Color(79, 70, 229)), // Indigo left bar
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(226, 232, 240)),
                        new EmptyBorder(10, 14, 10, 14)
                )
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel timeLabel = new JLabel("🕒 " + c.getFormattedTimestamp());
        timeLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        timeLabel.setForeground(new Color(79, 70, 229));
        header.add(timeLabel, BorderLayout.WEST);

        JLabel tag = new JLabel("Care Log #" + c.getId());
        tag.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        tag.setForeground(new Color(148, 163, 184));
        header.add(tag, BorderLayout.EAST);

        card.add(header, BorderLayout.NORTH);

        JLabel noteLabel = new JLabel("<html><body style='width: 380px;'>" + escapeHtml(c.getNote()) + "</body></html>");
        noteLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        noteLabel.setForeground(new Color(30, 41, 59));
        card.add(noteLabel, BorderLayout.CENTER);

        return card;
    }

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\n", "<br/>");
    }
}
