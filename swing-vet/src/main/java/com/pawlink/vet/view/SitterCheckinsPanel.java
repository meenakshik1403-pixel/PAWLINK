package com.pawlink.vet.view;

import com.pawlink.vet.api.ApiClient;
import com.pawlink.vet.api.Session;
import com.pawlink.vet.base.BasePanel;
import com.pawlink.vet.base.BaseTableModel;
import com.pawlink.vet.base.Dialogs;
import com.pawlink.vet.base.PawComponents;
import com.pawlink.vet.base.PawIcons;
import com.pawlink.vet.base.PawTheme;
import com.pawlink.vet.model.CheckinDTO;
import com.pawlink.vet.model.SittingBookingDTO;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Dedicated Interactive Check-Ins Workstation Panel.
 * Allows Sitters to select an active booking, see previous check-in updates,
 * and publish new care updates with tactile controls and pure vector icons.
 */
public class SitterCheckinsPanel extends BasePanel {

    private final BaseTableModel<SittingBookingDTO> bookingTableModel;
    private final JTable bookingTable;
    private final JPanel timelineContainer;
    private final JTextArea newNoteArea;
    private final PawComponents.PawButton postButton;
    private final JLabel currentBookingLabel;
    private final List<JButton> chipButtons = new ArrayList<>();
    private SittingBookingDTO selectedBooking;

    public SitterCheckinsPanel() {
        super("Pet Care Check-Ins", "Log feeding, walking, and wellness updates in real-time for pet parents.");

        setHeaderIcon(PawIcons.sitter(22, PawTheme.PRIMARY));
        setHeaderColor(PawTheme.PRIMARY);

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
        this.timelineContainer.setBackground(PawTheme.SURFACE);

        this.newNoteArea = new JTextArea(3, 20);
        this.postButton = new PawComponents.PawButton("Publish Update", PawIcons.check(14, Color.WHITE), PawComponents.ButtonStyle.PRIMARY);
        this.currentBookingLabel = new JLabel("Select a booking from the left to view and post check-ins.");

        setupUI();
        loadBookings();
    }

    private void setupUI() {
        PawTheme.styleTable(bookingTable);
        bookingTable.getColumnModel().getColumn(4).setCellRenderer(new PawComponents.StatusTableCellRenderer());
        bookingTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Column widths optimized for clean readability without squeezing right panel
        bookingTable.getColumnModel().getColumn(0).setPreferredWidth(45);
        bookingTable.getColumnModel().getColumn(0).setMaxWidth(55);
        bookingTable.getColumnModel().getColumn(1).setPreferredWidth(85);
        bookingTable.getColumnModel().getColumn(2).setPreferredWidth(110);
        bookingTable.getColumnModel().getColumn(3).setPreferredWidth(125);
        bookingTable.getColumnModel().getColumn(4).setPreferredWidth(105);

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
        splitPane.setDividerLocation(470);
        splitPane.setResizeWeight(0.38);
        splitPane.setBorder(new CompoundBorder(
                new LineBorder(PawTheme.BORDER_LIGHT, 1, true),
                new EmptyBorder(0, 0, 0, 0)
        ));

        // Left Panel with matching styled header
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBackground(PawTheme.SURFACE);

        JPanel leftHeader = new JPanel(new BorderLayout());
        leftHeader.setBackground(PawTheme.SURFACE_MUTED);
        leftHeader.setBorder(new CompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, PawTheme.BORDER_LIGHT),
                new EmptyBorder(12, 16, 12, 16)
        ));

        JLabel leftHeaderLabel = new JLabel("Select Booking / Job:");
        leftHeaderLabel.setFont(PawTheme.FONT_BODY_BOLD);
        leftHeaderLabel.setForeground(PawTheme.TEXT_BODY);
        leftHeader.add(leftHeaderLabel, BorderLayout.WEST);
        leftPanel.add(leftHeader, BorderLayout.NORTH);

        JScrollPane tableScroll = new JScrollPane(bookingTable);
        tableScroll.setBorder(BorderFactory.createEmptyBorder());
        tableScroll.getViewport().setBackground(PawTheme.SURFACE);
        leftPanel.add(tableScroll, BorderLayout.CENTER);

        splitPane.setLeftComponent(leftPanel);

        // Right Panel: Top status, Center timeline, Bottom composer
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(PawTheme.SURFACE);

        JPanel rightHeader = new JPanel(new BorderLayout(12, 0));
        rightHeader.setBackground(PawTheme.SURFACE_MUTED);
        rightHeader.setBorder(new CompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, PawTheme.BORDER_LIGHT),
                new EmptyBorder(10, 16, 10, 16)
        ));
        currentBookingLabel.setFont(PawTheme.FONT_BODY_BOLD);
        currentBookingLabel.setForeground(PawTheme.TEXT_TITLE);
        rightHeader.add(currentBookingLabel, BorderLayout.CENTER);

        PawComponents.PawButton refreshBtn = new PawComponents.PawButton("Refresh", PawIcons.refresh(12, PawTheme.TEXT_BODY), PawComponents.ButtonStyle.SECONDARY);
        refreshBtn.addActionListener(e -> {
            loadBookings();
            if (selectedBooking != null) loadTimeline(selectedBooking.getId());
        });
        rightHeader.add(refreshBtn, BorderLayout.EAST);
        rightPanel.add(rightHeader, BorderLayout.NORTH);

        // Timeline ScrollPane: padding inside container keeps scrollbar flush against panel border
        timelineContainer.setBorder(new EmptyBorder(14, 16, 14, 16));
        JScrollPane timelineScroll = new JScrollPane(timelineContainer);
        timelineScroll.setBorder(BorderFactory.createEmptyBorder());
        timelineScroll.getViewport().setBackground(PawTheme.SURFACE);
        timelineScroll.getVerticalScrollBar().setUnitIncrement(16);
        rightPanel.add(timelineScroll, BorderLayout.CENTER);

        // Composer Panel
        JPanel composerPanel = new JPanel(new BorderLayout(0, 10));
        composerPanel.setBackground(PawTheme.CANVAS);
        composerPanel.setBorder(new CompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, PawTheme.BORDER_LIGHT),
                new EmptyBorder(12, 16, 14, 16)
        ));

        // Quick Preset Chips (Cute, Multi-Coloured Pastels)
        JPanel chips = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        chips.setOpaque(false);
        chipButtons.clear();
        addChip(chips, "Meal & Water", "Fed yummy meal & fresh water", PawTheme.MINT_LIGHT, PawTheme.MINT_TEXT, PawIcons.bone(12, PawTheme.MINT_TEXT));
        addChip(chips, "30-Min Walk", "Completed sunny 30-minute walk", PawTheme.SUNNY_LIGHT, PawTheme.SUNNY_TEXT, PawIcons.paw(12, PawTheme.SUNNY_TEXT));
        addChip(chips, "Medication", "Administered scheduled medication", PawTheme.LILAC_LIGHT, PawTheme.LILAC_TEXT, PawIcons.vaccine(12, PawTheme.LILAC_TEXT));
        addChip(chips, "Playtime", "Engaged in fun playtime & cuddles", PawTheme.CORAL_LIGHT, PawTheme.CORAL_TEXT, PawIcons.heart(12, PawTheme.CORAL_TEXT));
        addChip(chips, "Resting Cozy", "Resting cozy, calm & safe", PawTheme.SKY_LIGHT, PawTheme.SKY_TEXT, PawIcons.star(12, PawTheme.SKY_TEXT));
        composerPanel.add(chips, BorderLayout.NORTH);

        // Input row with text area and neatly-sized button
        JPanel inputRow = new JPanel(new BorderLayout(12, 0));
        inputRow.setOpaque(false);

        newNoteArea.setFont(PawTheme.FONT_BODY);
        newNoteArea.setLineWrap(true);
        newNoteArea.setWrapStyleWord(true);
        newNoteArea.setEnabled(false);
        newNoteArea.setBorder(new EmptyBorder(8, 12, 8, 12));

        JScrollPane noteScroll = new JScrollPane(newNoteArea);
        noteScroll.setBorder(new CompoundBorder(
                new LineBorder(PawTheme.BORDER_STRONG, 1, true),
                new EmptyBorder(0, 0, 0, 0)
        ));
        inputRow.add(noteScroll, BorderLayout.CENTER);

        JPanel actionCol = new JPanel(new BorderLayout());
        actionCol.setOpaque(false);
        postButton.setPreferredSize(new Dimension(160, 42));
        postButton.setEnabled(false);
        postButton.addActionListener(e -> handlePostCheckin());
        actionCol.add(postButton, BorderLayout.SOUTH);
        inputRow.add(actionCol, BorderLayout.EAST);

        composerPanel.add(inputRow, BorderLayout.CENTER);
        rightPanel.add(composerPanel, BorderLayout.SOUTH);
        splitPane.setRightComponent(rightPanel);

        setContent(splitPane);
    }

    private void addChip(JPanel panel, String label, String fullText, Color bg, Color fg, Icon icon) {
        JButton btn = new JButton(label, icon);
        btn.setFont(PawTheme.FONT_SMALL_BOLD);
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusable(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setIconTextGap(6);
        btn.setEnabled(false);
        btn.setToolTipText("Add update: \"" + fullText + "\"");
        btn.setBorder(new CompoundBorder(
                new LineBorder(new Color(fg.getRed(), fg.getGreen(), fg.getBlue(), 60), 1, true),
                new EmptyBorder(6, 12, 6, 12)
        ));
        btn.addActionListener(e -> {
            if (newNoteArea.isEnabled()) {
                String cur = newNoteArea.getText().trim();
                newNoteArea.setText(cur.isEmpty() ? fullText + "." : cur + " " + fullText + ".");
                newNoteArea.requestFocusInWindow();
            }
        });
        chipButtons.add(btn);
        panel.add(btn);
    }

    @Override
    public void refresh() {
        loadBookings();
    }

    public void loadBookings() {
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
        boolean isConfirmed = "CONFIRMED".equalsIgnoreCase(selectedBooking.getStatus());
        String statusStr = selectedBooking.getStatus() != null ? selectedBooking.getStatus() : "UNKNOWN";
        String petName = selectedBooking.getPetName() != null ? selectedBooking.getPetName() : "Pet";

        currentBookingLabel.setText("Job #" + selectedBooking.getId() + ": " + petName + " · " + statusStr);
        currentBookingLabel.setToolTipText("Active Job #" + selectedBooking.getId() + " (" + petName +
                " for " + selectedBooking.getOwnerName() + ") · " + statusStr);

        newNoteArea.setEnabled(isConfirmed);
        postButton.setEnabled(isConfirmed);

        for (JButton chip : chipButtons) {
            chip.setEnabled(isConfirmed);
        }

        if (!isConfirmed) {
            newNoteArea.setText("");
            newNoteArea.setToolTipText("Check-in updates are disabled because this booking is " + statusStr);
            postButton.setToolTipText("Cannot publish update: Booking status is " + statusStr);
        } else {
            newNoteArea.setToolTipText("Type a care update for pet owner (e.g. food, water, walks, medication)");
            postButton.setToolTipText("Publish this update to pet owner's real-time timeline");
        }

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
                        JLabel l = new JLabel("No check-in updates yet. Use the composer below to publish one.");
                        l.setFont(PawTheme.FONT_BODY);
                        l.setForeground(PawTheme.TEXT_MUTED);
                        empty.add(l);
                        timelineContainer.add(empty);
                    } else {
                        for (CheckinDTO c : checkins) {
                            JPanel card = createCard(c);
                            card.setAlignmentX(Component.LEFT_ALIGNMENT);
                            timelineContainer.add(card);
                            timelineContainer.add(Box.createVerticalStrut(8));
                        }
                        timelineContainer.add(Box.createVerticalGlue());
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
        PawComponents.PawCard card = new PawComponents.PawCard(10);
        card.setLayout(new BorderLayout(8, 6));
        card.setBorder(new EmptyBorder(10, 14, 10, 14));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        top.setOpaque(false);
        top.add(new JLabel(PawIcons.clock(12, PawTheme.PRIMARY)));

        JLabel time = new JLabel(c.getFormattedTimestamp());
        time.setFont(PawTheme.FONT_SMALL_BOLD);
        time.setForeground(PawTheme.PRIMARY);
        top.add(time);

        card.add(top, BorderLayout.NORTH);

        String noteText = BasePanel.cleanEmoji(c.getNote());
        JLabel note = new JLabel("<html><body>" + (noteText.isEmpty() ? c.getNote() : noteText) + "</body></html>");
        note.setFont(PawTheme.FONT_BODY);
        note.setForeground(PawTheme.TEXT_TITLE);
        card.add(note, BorderLayout.CENTER);

        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, card.getPreferredSize().height));
        return card;
    }

    private void handlePostCheckin() {
        if (selectedBooking == null) return;
        if (!"CONFIRMED".equalsIgnoreCase(selectedBooking.getStatus())) {
            Dialogs.showWarning(this, "Cannot publish update: This booking is already " + selectedBooking.getStatus() + ".");
            return;
        }

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
