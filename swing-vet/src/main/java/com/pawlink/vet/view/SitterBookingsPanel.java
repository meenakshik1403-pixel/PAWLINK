package com.pawlink.vet.view;

import com.pawlink.vet.api.ApiClient;
import com.pawlink.vet.api.Session;
import com.pawlink.vet.base.BasePanel;
import com.pawlink.vet.base.BaseTableModel;
import com.pawlink.vet.base.Dialogs;
import com.pawlink.vet.base.PawComponents;
import com.pawlink.vet.base.PawIcons;
import com.pawlink.vet.base.PawTheme;
import com.pawlink.vet.model.SittingBookingDTO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Modernized Panel for managing Sitter Bookings, assigned jobs, and tracking check-ins.
 * Adaptive: Displays sitter's assigned jobs or owner's booked sitters based on logged-in role.
 * Includes Cancel Booking and End Session (Release Payment) workflows with modern vector styling.
 */
public class SitterBookingsPanel extends BasePanel {

    private final BaseTableModel<SittingBookingDTO> tableModel;
    private final JTable table;
    private final JLabel statusCountLabel;
    private List<SittingBookingDTO> bookingList = new ArrayList<>();

    // Actions
    private JButton checkinBtn;
    private JButton timelineBtn;
    private JButton cancelBtn;
    private JButton endSessionBtn;

    public SitterBookingsPanel() {
        super(getAdaptiveTitle(), getAdaptiveSubtitle());
        setHeaderIcon(PawIcons.sitter(26, PawTheme.SUNNY));
        setHeaderColor(PawTheme.SUNNY_TEXT);

        String[] columns = {"Booking #", "Pet Name", "Owner", "Sitter", "Start Time", "End Time", "Status", "Total Fee", "Escrow Status"};
        this.tableModel = new BaseTableModel<>(columns) {
            @Override
            public Object getValueAt(SittingBookingDTO b, int col) {
                return switch (col) {
                    case 0 -> "#" + b.getId();
                    case 1 -> b.getPetName() != null ? b.getPetName() : ("Pet #" + b.getPetId());
                    case 2 -> b.getOwnerName() != null ? b.getOwnerName() : ("Owner #" + b.getOwnerId());
                    case 3 -> b.getSitterName() != null ? b.getSitterName() : ("Sitter #" + b.getSitterId());
                    case 4 -> b.getFormattedStart();
                    case 5 -> b.getFormattedEnd();
                    case 6 -> b.getStatus();
                    case 7 -> "₹" + (b.getTotalAmount() != null ? b.getTotalAmount() : "0.00");
                    case 8 -> b.getPaymentStatus() != null ? b.getPaymentStatus() : "HELD";
                    default -> "";
                };
            }
        };

        this.table = new JTable(tableModel);
        this.statusCountLabel = new JLabel("Loading bookings...");

        setupUI();
        loadBookings();
    }

    private static String getAdaptiveTitle() {
        Session session = Session.getInstance();
        if (session.isOwner()) {
            return "My Booked Pet Sitters";
        }
        return "Sitter Bookings & Jobs";
    }

    private static String getAdaptiveSubtitle() {
        Session session = Session.getInstance();
        if (session.isOwner()) {
            return "Track sitting sessions, inspect real-time care updates, and release escrow payment.";
        }
        return "View scheduled sitting appointments, monitor escrow status, and log pet care updates.";
    }

    private void setupUI() {
        PawTheme.styleTable(table);

        // Modern Pill Renderers for Status (Col 6) and Escrow (Col 8)
        table.getColumnModel().getColumn(6).setCellRenderer(new PawComponents.StatusTableCellRenderer());
        table.getColumnModel().getColumn(8).setCellRenderer(new PawComponents.StatusTableCellRenderer());

        table.getColumnModel().getColumn(0).setPreferredWidth(75);
        table.getColumnModel().getColumn(0).setMaxWidth(90);
        table.getColumnModel().getColumn(1).setPreferredWidth(110);
        table.getColumnModel().getColumn(2).setPreferredWidth(120);
        table.getColumnModel().getColumn(3).setPreferredWidth(120);
        table.getColumnModel().getColumn(4).setPreferredWidth(135);
        table.getColumnModel().getColumn(5).setPreferredWidth(135);
        table.getColumnModel().getColumn(6).setPreferredWidth(110);
        table.getColumnModel().getColumn(7).setPreferredWidth(95);
        table.getColumnModel().getColumn(8).setPreferredWidth(110);

        // Selection Listener to update action buttons dynamically
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateActionButtonsState();
            }
        });

        // Double click to view timeline
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedRow() >= 0) {
                    openTimelineDialog();
                }
            }
        });

        // Header Accessory: Refresh and View Timeline (and Log Check-In for Sitter)
        JPanel accessoryPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        accessoryPanel.setOpaque(false);

        JButton refreshBtn = new PawComponents.PawButton("Refresh", PawIcons.refresh(14, PawTheme.TEXT_BODY), PawComponents.ButtonStyle.SECONDARY);
        refreshBtn.addActionListener(e -> loadBookings());
        accessoryPanel.add(refreshBtn);

        Session session = Session.getInstance();
        if (session.isSitter()) {
            checkinBtn = new PawComponents.PawButton("Log Check-In", PawIcons.clock(14, Color.WHITE), PawComponents.ButtonStyle.MINT);
            checkinBtn.setEnabled(false);
            checkinBtn.addActionListener(e -> openAddCheckinDialog());
            accessoryPanel.add(checkinBtn);
        }

        timelineBtn = new PawComponents.PawButton("View Timeline", PawIcons.eye(14, PawTheme.LILAC_TEXT), PawComponents.ButtonStyle.LILAC);
        timelineBtn.setEnabled(false);
        timelineBtn.addActionListener(e -> openTimelineDialog());
        accessoryPanel.add(timelineBtn);

        if (session.isOwner()) {
            cancelBtn = new PawComponents.PawButton("Cancel Booking", PawIcons.cross(14, Color.WHITE), PawComponents.ButtonStyle.DANGER);
            cancelBtn.setEnabled(false);
            cancelBtn.addActionListener(e -> handleCancelBooking());

            endSessionBtn = new PawComponents.PawButton("End Session & Release Payment", PawIcons.wallet(14, Color.WHITE), PawComponents.ButtonStyle.MINT);
            endSessionBtn.setEnabled(false);
            endSessionBtn.addActionListener(e -> handleEndSession());
        }

        setHeaderAccessory(accessoryPanel);

        // Center Content: Scroll Pane inside cute PawCard + Bottom Bar
        PawComponents.PawCard tableCard = new PawComponents.PawCard();
        tableCard.setLayout(new BorderLayout(0, 10));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(PawTheme.BORDER_LIGHT));
        tableCard.add(scrollPane, BorderLayout.CENTER);

        JPanel bottomBar = new JPanel(new BorderLayout(14, 0));
        bottomBar.setOpaque(false);
        bottomBar.setBorder(new EmptyBorder(8, 6, 4, 6));

        statusCountLabel.setFont(PawTheme.FONT_SUBTITLE);
        statusCountLabel.setForeground(PawTheme.TEXT_MUTED);
        bottomBar.add(statusCountLabel, BorderLayout.WEST);

        if (session.isOwner()) {
            JPanel ownerActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
            ownerActions.setOpaque(false);
            ownerActions.add(cancelBtn);
            ownerActions.add(endSessionBtn);
            bottomBar.add(ownerActions, BorderLayout.EAST);
        } else {
            String tipText = "Tip: Select a booking and click 'Log Check-In' to update pet owners in real time.";
            JLabel tip = new JLabel(tipText);
            tip.setFont(PawTheme.FONT_SMALL);
            tip.setForeground(PawTheme.TEXT_SUBTLE);
            bottomBar.add(tip, BorderLayout.EAST);
        }

        tableCard.add(bottomBar, BorderLayout.SOUTH);
        setContent(tableCard);
    }

    private void updateActionButtonsState() {
        int row = table.getSelectedRow();
        if (row < 0) {
            if (checkinBtn != null) checkinBtn.setEnabled(false);
            if (timelineBtn != null) timelineBtn.setEnabled(false);
            if (cancelBtn != null) cancelBtn.setEnabled(false);
            if (endSessionBtn != null) endSessionBtn.setEnabled(false);
            return;
        }

        SittingBookingDTO booking = tableModel.getItem(row);
        boolean isConfirmed = booking != null && "CONFIRMED".equalsIgnoreCase(booking.getStatus());

        if (checkinBtn != null) {
            checkinBtn.setEnabled(isConfirmed);
            checkinBtn.setToolTipText(isConfirmed ? "Log real-time care check-in" : "Check-in disabled: Booking status is " + (booking != null ? booking.getStatus() : "closed"));
        }
        if (timelineBtn != null) {
            timelineBtn.setEnabled(true);
        }
        if (cancelBtn != null) {
            cancelBtn.setEnabled(isConfirmed);
        }
        if (endSessionBtn != null) {
            endSessionBtn.setEnabled(isConfirmed);
        }
    }

    @Override
    public void refresh() {
        loadBookings();
    }

    public void loadBookings() {
        Session session = Session.getInstance();
        Long userId = session.getUserId();
        String role = session.getRole();

        String endpoint;
        if ("SITTER".equalsIgnoreCase(role)) {
            endpoint = "/api/sitter-bookings?sitterId=" + userId;
        } else if ("OWNER".equalsIgnoreCase(role)) {
            endpoint = "/api/sitter-bookings?ownerId=" + userId;
        } else {
            endpoint = "/api/sitter-bookings";
        }

        SwingWorker<List<SittingBookingDTO>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<SittingBookingDTO> doInBackground() {
                return ApiClient.getInstance().getList(endpoint, SittingBookingDTO.class);
            }

            @Override
            protected void done() {
                try {
                    bookingList = get();
                    tableModel.setItems(bookingList);
                    statusCountLabel.setText("Found " + bookingList.size() + " booking(s).");
                    if (!bookingList.isEmpty() && table.getSelectedRow() < 0) {
                        table.setRowSelectionInterval(0, 0);
                    }
                    updateActionButtonsState();
                } catch (Exception ex) {
                    showError("Failed to fetch bookings: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void handleCancelBooking() {
        int row = table.getSelectedRow();
        if (row < 0) {
            Dialogs.showWarning(this, "Please select a booking to cancel.");
            return;
        }

        SittingBookingDTO booking = tableModel.getItem(row);
        if (booking == null) return;

        boolean confirm = Dialogs.showConfirm(this,
                "Are you sure you want to cancel Booking #" + booking.getId() + " for " + booking.getPetName() + "?\n" +
                "Any funds currently held in escrow will be refunded.",
                "Cancel Sitter Booking");
        if (!confirm) return;

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                ApiClient.getInstance().post("/api/sitter-bookings/" + booking.getId() + "/cancel", null, SittingBookingDTO.class);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    Dialogs.showSuccess(SitterBookingsPanel.this, "Booking #" + booking.getId() + " has been cancelled and escrow refunded.");
                    loadBookings();
                } catch (Exception ex) {
                    showError("Failed to cancel booking: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void handleEndSession() {
        int row = table.getSelectedRow();
        if (row < 0) {
            Dialogs.showWarning(this, "Please select a booking to end session.");
            return;
        }

        SittingBookingDTO booking = tableModel.getItem(row);
        if (booking == null) return;

        boolean confirm = Dialogs.showConfirm(this,
                "End sitting session for " + booking.getPetName() + "?\n" +
                "This will mark the booking as COMPLETED and release payment (₹" +
                (booking.getTotalAmount() != null ? booking.getTotalAmount() : "0.00") + ") from escrow to " +
                (booking.getSitterName() != null ? booking.getSitterName() : "the sitter") + ".",
                "End Session & Release Payment");
        if (!confirm) return;

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                ApiClient.getInstance().post("/api/sitter-bookings/" + booking.getId() + "/complete", null, SittingBookingDTO.class);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    Dialogs.showSuccess(SitterBookingsPanel.this, "Session completed! Escrow payment has been released.");
                    loadBookings();
                } catch (Exception ex) {
                    showError("Failed to complete session: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void openAddCheckinDialog() {
        int row = table.getSelectedRow();
        if (row < 0) {
            Dialogs.showWarning(this, "Please select a booking from the table to log a check-in note.");
            return;
        }

        SittingBookingDTO booking = tableModel.getItem(row);
        if (booking == null) return;
        if (!"CONFIRMED".equalsIgnoreCase(booking.getStatus())) {
            Dialogs.showWarning(this, "Check-in notes can only be posted for active 'CONFIRMED' bookings. This booking is already " + booking.getStatus() + ".");
            return;
        }

        Frame parent = (Frame) SwingUtilities.getWindowAncestor(this);
        AddCheckinDialog dialog = new AddCheckinDialog(parent, booking.getId(), booking.getPetName(), this::loadBookings);
        dialog.setVisible(true);
    }

    private void openTimelineDialog() {
        int row = table.getSelectedRow();
        if (row < 0) {
            Dialogs.showWarning(this, "Please select a booking to view its check-in timeline.");
            return;
        }

        SittingBookingDTO booking = tableModel.getItem(row);
        if (booking == null) return;

        Frame parent = (Frame) SwingUtilities.getWindowAncestor(this);
        CheckinsTimelineDialog dialog = new CheckinsTimelineDialog(parent, booking.getId(), booking.getPetName());
        dialog.setVisible(true);
    }
}
