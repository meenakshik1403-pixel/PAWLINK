package com.pawlink.vet.view;

import com.pawlink.vet.api.ApiClient;
import com.pawlink.vet.api.Session;
import com.pawlink.vet.base.BasePanel;
import com.pawlink.vet.base.BaseTableModel;
import com.pawlink.vet.base.Dialogs;
import com.pawlink.vet.model.SittingBookingDTO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Panel for managing Sitter Bookings, assigned jobs, and tracking check-ins.
 * Adaptive: Displays sitter's assigned jobs or owner's booked sitters based on logged-in role.
 * Includes Cancel Booking and End Session (Release Payment) workflows for Pet Owners.
 */
public class SitterBookingsPanel extends BasePanel {

    private final BaseTableModel<SittingBookingDTO> tableModel;
    private final JTable table;
    private final JLabel statusCountLabel;
    private List<SittingBookingDTO> bookingList = new ArrayList<>();

    // Owner Actions
    private JButton cancelBtn;
    private JButton endSessionBtn;

    public SitterBookingsPanel() {
        super(getAdaptiveTitle(), getAdaptiveSubtitle());

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
            return "🐾 My Booked Pet Sitters";
        }
        return "📋 Sitter Bookings & Jobs";
    }

    private static String getAdaptiveSubtitle() {
        Session session = Session.getInstance();
        if (session.isOwner()) {
            return "Track active and past sitting sessions, inspect care updates, cancel bookings or end session to release payment.";
        }
        return "View scheduled sitting appointments, monitor escrow status, and log pet care updates.";
    }

    private void setupUI() {
        table.setRowHeight(36);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setShowGrid(true);
        table.setGridColor(new Color(241, 245, 249));

        // Renderer for Status Column (index 6)
        table.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int col) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, col);
                label.setHorizontalAlignment(SwingConstants.CENTER);
                String val = String.valueOf(value);
                if ("CONFIRMED".equalsIgnoreCase(val)) {
                    label.setText("✓ Confirmed");
                    label.setForeground(new Color(16, 185, 129));
                    label.setFont(label.getFont().deriveFont(Font.BOLD));
                } else if ("COMPLETED".equalsIgnoreCase(val)) {
                    label.setText("★ Completed");
                    label.setForeground(new Color(59, 130, 246));
                    label.setFont(label.getFont().deriveFont(Font.PLAIN));
                } else if ("CANCELLED".equalsIgnoreCase(val)) {
                    label.setText("✗ Cancelled");
                    label.setForeground(new Color(239, 68, 68));
                    label.setFont(label.getFont().deriveFont(Font.PLAIN));
                }
                return label;
            }
        });

        // Renderer for Escrow Status Column (index 8)
        table.getColumnModel().getColumn(8).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int col) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, col);
                label.setHorizontalAlignment(SwingConstants.CENTER);
                String val = String.valueOf(value);
                if ("HELD".equalsIgnoreCase(val)) {
                    label.setText("🔒 Escrow HELD");
                    label.setForeground(new Color(217, 119, 6)); // Amber
                } else if ("RELEASED".equalsIgnoreCase(val)) {
                    label.setText("✓ Escrow Released");
                    label.setForeground(new Color(16, 185, 129));
                } else if ("REFUNDED".equalsIgnoreCase(val)) {
                    label.setText("↩ Refunded");
                    label.setForeground(new Color(100, 116, 139));
                }
                return label;
            }
        });

        // Selection Listener to update button states
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

        // Header Accessory
        JPanel accessoryPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        accessoryPanel.setOpaque(false);

        JButton refreshBtn = new JButton("🔄 Refresh");
        refreshBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        refreshBtn.addActionListener(e -> loadBookings());
        accessoryPanel.add(refreshBtn);

        Session session = Session.getInstance();
        if (session.isSitter()) {
            JButton checkinBtn = new JButton("📸 Log Check-In");
            checkinBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
            checkinBtn.setBackground(new Color(16, 185, 129)); // Green
            checkinBtn.setForeground(Color.WHITE);
            checkinBtn.setFocusable(false);
            checkinBtn.addActionListener(e -> openAddCheckinDialog());
            accessoryPanel.add(checkinBtn);
        }

        JButton timelineBtn = new JButton("📋 View Timeline");
        timelineBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        timelineBtn.setBackground(new Color(79, 70, 229)); // Indigo
        timelineBtn.setForeground(Color.WHITE);
        timelineBtn.setFocusable(false);
        timelineBtn.addActionListener(e -> openTimelineDialog());
        accessoryPanel.add(timelineBtn);

        if (session.isOwner()) {
            cancelBtn = new JButton("❌ Cancel Booking");
            cancelBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
            cancelBtn.setBackground(new Color(220, 38, 38)); // Red
            cancelBtn.setForeground(Color.WHITE);
            cancelBtn.setFocusable(false);
            cancelBtn.setEnabled(false);
            cancelBtn.addActionListener(e -> handleCancelBooking());
            accessoryPanel.add(cancelBtn);

            endSessionBtn = new JButton("🏁 End Session & Release Payment");
            endSessionBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
            endSessionBtn.setBackground(new Color(22, 163, 74)); // Green
            endSessionBtn.setForeground(Color.WHITE);
            endSessionBtn.setFocusable(false);
            endSessionBtn.setEnabled(false);
            endSessionBtn.addActionListener(e -> handleEndSession());
            accessoryPanel.add(endSessionBtn);
        }

        setHeaderAccessory(accessoryPanel);

        // Center Content
        JPanel centerPanel = new JPanel(new BorderLayout(0, 10));
        centerPanel.setOpaque(false);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel bottomBar = new JPanel(new BorderLayout());
        bottomBar.setOpaque(false);
        bottomBar.setBorder(new EmptyBorder(5, 5, 5, 5));

        statusCountLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusCountLabel.setForeground(new Color(100, 116, 139));
        bottomBar.add(statusCountLabel, BorderLayout.WEST);

        String tipText = session.isOwner()
                ? "💡 Tip: When your pet sitting finishes, click 'End Session & Release Payment' to transfer escrow to the sitter."
                : "💡 Tip: Select a booking and click 'Log Check-In' to update pet owners in real time.";
        JLabel tip = new JLabel(tipText);
        tip.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        tip.setForeground(new Color(148, 163, 184));
        bottomBar.add(tip, BorderLayout.EAST);

        centerPanel.add(bottomBar, BorderLayout.SOUTH);

        setContent(centerPanel);
    }

    private void updateActionButtonsState() {
        if (cancelBtn == null && endSessionBtn == null) return;
        int row = table.getSelectedRow();
        if (row < 0) {
            if (cancelBtn != null) cancelBtn.setEnabled(false);
            if (endSessionBtn != null) endSessionBtn.setEnabled(false);
            return;
        }

        SittingBookingDTO booking = tableModel.getItem(row);
        boolean isConfirmed = booking != null && "CONFIRMED".equalsIgnoreCase(booking.getStatus());
        if (cancelBtn != null) cancelBtn.setEnabled(isConfirmed);
        if (endSessionBtn != null) endSessionBtn.setEnabled(isConfirmed);
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
