package com.pawlink.vet.view;

import com.pawlink.vet.api.ApiClient;
import com.pawlink.vet.api.Session;
import com.pawlink.vet.base.BasePanel;
import com.pawlink.vet.base.BaseTableModel;
import com.pawlink.vet.base.Dialogs;
import com.pawlink.vet.model.CheckinDTO;
import com.pawlink.vet.model.SittingBookingDTO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Dedicated Interactive Check-Ins Workstation Panel.
 * Allows Sitters to select an active booking, see previous check-in updates,
 * and publish new care updates with one click.
 */
public class SitterCheckinsPanel extends BasePanel {

    private final BaseTableModel<SittingBookingDTO> bookingTableModel;
    private final JTable bookingTable;
    private final JPanel timelineContainer;
    private final JTextArea newNoteArea;
    private final JButton postButton;
    private final JLabel currentBookingLabel;
    private SittingBookingDTO selectedBooking;

    public SitterCheckinsPanel() {
        super("📸 Pet Care Check-Ins", "Log feeding, walking, and wellness updates in real-time for pet parents.");

        String[] cols = {"ID", "Pet", "Owner", "Start Time", "Status"};
        this.bookingTableModel = new BaseTableModel<>(cols) {
            @Override
            public Object getValueAt(SittingBookingDTO b, int col) {
                return switch (col) {
                    case 0 -> "#" + b.getId();
                    case 1 -> b.getPetName() != null ? b.getPetName() : "Pet";
                    case 2 -> b.getOwnerName() != null ? b.getOwnerName() : "Owner";
                    case 3 -> b.getFormattedStart();
                    case 4 -> b.getStatus();
                    default -> "";
                };
            }
        };

        this.bookingTable = new JTable(bookingTableModel);
        this.timelineContainer = new JPanel();
        this.timelineContainer.setLayout(new BoxLayout(timelineContainer, BoxLayout.Y_AXIS));
        this.timelineContainer.setBackground(Color.WHITE);

        this.newNoteArea = new JTextArea(3, 20);
        this.postButton = new JButton("Publish Update");
        this.currentBookingLabel = new JLabel("Select a booking from the left to view and post check-ins.");

        setupUI();
        loadBookings();
    }

    private void setupUI() {
        bookingTable.setRowHeight(32);
        bookingTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        bookingTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        bookingTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = bookingTable.getSelectedRow();
                if (row >= 0) {
                    selectedBooking = bookingTableModel.getItem(row);
                    onBookingSelected();
                }
            }
        });

        // Split Pane: Left = Bookings, Right = Timeline + Composer
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(360);
        splitPane.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));

        // Left Panel
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBackground(Color.WHITE);
        JLabel leftHeader = new JLabel("Select Booking / Job:");
        leftHeader.setFont(new Font("Segoe UI", Font.BOLD, 13));
        leftHeader.setBorder(new EmptyBorder(10, 12, 10, 12));
        leftPanel.add(leftHeader, BorderLayout.NORTH);

        JScrollPane tableScroll = new JScrollPane(bookingTable);
        tableScroll.setBorder(BorderFactory.createEmptyBorder());
        leftPanel.add(tableScroll, BorderLayout.CENTER);

        splitPane.setLeftComponent(leftPanel);

        // Right Panel: Top status, Center timeline, Bottom composer
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(Color.WHITE);

        JPanel rightHeader = new JPanel(new BorderLayout());
        rightHeader.setBackground(new Color(241, 245, 249));
        rightHeader.setBorder(new EmptyBorder(8, 14, 8, 14));
        currentBookingLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        currentBookingLabel.setForeground(new Color(30, 41, 59));
        rightHeader.add(currentBookingLabel, BorderLayout.WEST);

        JButton refreshBtn = new JButton("🔄 Refresh");
        refreshBtn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        refreshBtn.addActionListener(e -> {
            loadBookings();
            if (selectedBooking != null) loadTimeline(selectedBooking.getId());
        });
        rightHeader.add(refreshBtn, BorderLayout.EAST);
        rightPanel.add(rightHeader, BorderLayout.NORTH);

        JScrollPane timelineScroll = new JScrollPane(timelineContainer);
        timelineScroll.setBorder(BorderFactory.createEmptyBorder());
        timelineScroll.getVerticalScrollBar().setUnitIncrement(16);
        rightPanel.add(timelineScroll, BorderLayout.CENTER);

        // Composer Panel
        JPanel composerPanel = new JPanel(new BorderLayout(8, 8));
        composerPanel.setBackground(new Color(248, 250, 252));
        composerPanel.setBorder(new EmptyBorder(10, 14, 12, 14));

        // Chips
        JPanel chips = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 4));
        chips.setOpaque(false);
        addChip(chips, "🍽️ Fed pet");
        addChip(chips, "🦮 30m walk");
        addChip(chips, "💊 Medicine given");
        addChip(chips, "🎾 Playtime");
        addChip(chips, "💤 Resting");
        composerPanel.add(chips, BorderLayout.NORTH);

        newNoteArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        newNoteArea.setLineWrap(true);
        newNoteArea.setWrapStyleWord(true);
        newNoteArea.setEnabled(false);
        JScrollPane noteScroll = new JScrollPane(newNoteArea);
        noteScroll.setBorder(BorderFactory.createLineBorder(new Color(203, 213, 225)));
        composerPanel.add(noteScroll, BorderLayout.CENTER);

        postButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        postButton.setBackground(new Color(16, 185, 129));
        postButton.setForeground(Color.WHITE);
        postButton.setFocusable(false);
        postButton.setEnabled(false);
        postButton.addActionListener(e -> handlePostCheckin());
        composerPanel.add(postButton, BorderLayout.EAST);

        rightPanel.add(composerPanel, BorderLayout.SOUTH);
        splitPane.setRightComponent(rightPanel);

        setContent(splitPane);
    }

    private void addChip(JPanel panel, String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btn.setBackground(Color.WHITE);
        btn.setFocusable(false);
        btn.addActionListener(e -> {
            if (newNoteArea.isEnabled()) {
                String cur = newNoteArea.getText().trim();
                newNoteArea.setText(cur.isEmpty() ? text + "." : cur + " " + text + ".");
            }
        });
        panel.add(btn);
    }

    private void loadBookings() {
        Session s = Session.getInstance();
        String ep = "SITTER".equalsIgnoreCase(s.getRole())
                ? "/api/sitter-bookings?sitterId=" + s.getUserId()
                : "/api/sitter-bookings?ownerId=" + s.getUserId();

        SwingWorker<List<SittingBookingDTO>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<SittingBookingDTO> doInBackground() {
                return ApiClient.getInstance().getList(ep, SittingBookingDTO.class);
            }

            @Override
            protected void done() {
                try {
                    List<SittingBookingDTO> list = get();
                    bookingTableModel.setItems(list);
                    if (!list.isEmpty() && bookingTable.getSelectedRow() < 0) {
                        bookingTable.setRowSelectionInterval(0, 0);
                    }
                } catch (Exception ex) {
                    showError("Failed to load bookings: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void onBookingSelected() {
        if (selectedBooking == null) return;
        currentBookingLabel.setText("Active Job: #" + selectedBooking.getId() +
                " (" + (selectedBooking.getPetName() != null ? selectedBooking.getPetName() : "Pet") +
                " for " + selectedBooking.getOwnerName() + ")");
        newNoteArea.setEnabled(true);
        postButton.setEnabled(true);
        loadTimeline(selectedBooking.getId());
    }

    private void loadTimeline(Long bookingId) {
        timelineContainer.removeAll();
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
                        JPanel empty = new JPanel(new GridBagLayout());
                        empty.setOpaque(false);
                        JLabel l = new JLabel("No check-in updates yet. Use the composer below to post one!");
                        l.setFont(new Font("Segoe UI", Font.ITALIC, 12));
                        l.setForeground(new Color(148, 163, 184));
                        empty.add(l);
                        timelineContainer.add(empty);
                    } else {
                        for (CheckinDTO c : checkins) {
                            timelineContainer.add(createCard(c));
                            timelineContainer.add(Box.createRigidArea(new Dimension(0, 8)));
                        }
                    }
                    timelineContainer.revalidate();
                    timelineContainer.repaint();
                } catch (Exception ex) {
                    showError("Failed to fetch check-ins: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private JPanel createCard(CheckinDTO c) {
        JPanel card = new JPanel(new BorderLayout(6, 4));
        card.setBackground(new Color(248, 250, 252));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 3, 0, 0, new Color(79, 70, 229)),
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(226, 232, 240)),
                        new EmptyBorder(8, 12, 8, 12)
                )
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JLabel time = new JLabel("🕒 " + c.getFormattedTimestamp());
        time.setFont(new Font("Segoe UI", Font.BOLD, 11));
        time.setForeground(new Color(79, 70, 229));
        top.add(time, BorderLayout.WEST);

        card.add(top, BorderLayout.NORTH);

        JLabel note = new JLabel("<html><body>" + c.getNote() + "</body></html>");
        note.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        note.setForeground(new Color(30, 41, 59));
        card.add(note, BorderLayout.CENTER);

        return card;
    }

    private void handlePostCheckin() {
        if (selectedBooking == null) return;
        String note = newNoteArea.getText().trim();
        if (note.isEmpty()) {
            Dialogs.showWarning(this, "Please enter a check-in note.");
            return;
        }

        postButton.setEnabled(false);
        Map<String, String> body = new HashMap<>();
        body.put("note", note);

        SwingWorker<CheckinDTO, Void> worker = new SwingWorker<>() {
            @Override
            protected CheckinDTO doInBackground() {
                return ApiClient.getInstance().post("/api/sitter-bookings/" + selectedBooking.getId() + "/checkins", body, CheckinDTO.class);
            }

            @Override
            protected void done() {
                postButton.setEnabled(true);
                try {
                    get();
                    newNoteArea.setText("");
                    loadTimeline(selectedBooking.getId());
                } catch (Exception ex) {
                    showError("Failed to post check-in: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }
}
