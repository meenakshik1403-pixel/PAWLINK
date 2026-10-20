package com.pawlink.vet.view;

import com.pawlink.vet.api.ApiClient;
import com.pawlink.vet.api.Session;
import com.pawlink.vet.base.BasePanel;
import com.pawlink.vet.base.BaseTableModel;
import com.pawlink.vet.base.Dialogs;
import com.pawlink.vet.model.AppointmentDTO;
import com.pawlink.vet.model.ReminderDTO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Veterinarian Overview Dashboard (Home Tab for VET in Swing Vet Client).
 * Displays today's scheduled consultations and pending reminders count.
 */
public class DashboardPanel extends BasePanel {

    private final BaseTableModel<AppointmentDTO> tableModel;
    private final JTable appointmentsTable;
    private final JLabel appointmentsCountLabel;
    private final JLabel remindersCountLabel;
    private final JLabel lastUpdatedLabel;

    public DashboardPanel() {
        super("📊 Veterinarian Overview Dashboard", "Real-time clinical summary of today's schedule, patient consultations, and alert notifications.");

        String[] cols = {"Time Slot", "Patient / Pet", "Pet Owner", "Status"};
        this.tableModel = new BaseTableModel<>(cols) {
            @Override
            public Object getValueAt(AppointmentDTO a, int col) {
                return switch (col) {
                    case 0 -> a.getTimeSlotFormatted();
                    case 1 -> a.getPetName() != null ? a.getPetName() : ("Pet #" + a.getPetId());
                    case 2 -> a.getOwnerName() != null ? a.getOwnerName() : ("Owner #" + a.getOwnerId());
                    case 3 -> a.getStatus() != null ? a.getStatus() : "PENDING";
                    default -> "";
                };
            }
        };

        this.appointmentsTable = new JTable(tableModel);
        this.appointmentsCountLabel = new JLabel("0");
        this.remindersCountLabel = new JLabel("0");
        this.lastUpdatedLabel = new JLabel("Loading clinical metrics...");

        setupUI();
        refreshData();
    }

    private void setupUI() {
        // Header Accessory: Refresh Button
        JPanel headerAccessory = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        headerAccessory.setOpaque(false);

        JButton refreshBtn = new JButton("🔄 Refresh Dashboard");
        refreshBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        refreshBtn.setBackground(new Color(13, 148, 136)); // Teal
        refreshBtn.setForeground(Color.WHITE);
        refreshBtn.setFocusable(false);
        refreshBtn.addActionListener(e -> refreshData());
        headerAccessory.add(refreshBtn);

        setHeaderAccessory(headerAccessory);

        // Center: Metrics Cards (Top) + Today's Appointments Mini Table (Center)
        JPanel mainPanel = new JPanel(new BorderLayout(0, 16));
        mainPanel.setOpaque(false);

        // 1. Two Visual Summary Cards
        JPanel cardsGrid = new JPanel(new GridLayout(1, 2, 20, 0));
        cardsGrid.setOpaque(false);

        // Card 1: Today's Appointments
        JPanel appointmentsCard = createMetricCard(
                "📅 Today's Scheduled Appointments",
                appointmentsCountLabel,
                "Consultations scheduled for " + LocalDate.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")),
                new Color(37, 99, 235), // Blue accent
                new Color(239, 246, 255)
        );
        cardsGrid.add(appointmentsCard);

        // Card 2: Pending Reminders Due
        JPanel remindersCard = createMetricCard(
                "🔔 Pending Reminders Due",
                remindersCountLabel,
                "Unsent clinical vaccination & checkup alerts",
                new Color(217, 119, 6), // Amber accent
                new Color(254, 243, 199)
        );
        cardsGrid.add(remindersCard);

        mainPanel.add(cardsGrid, BorderLayout.NORTH);

        // 2. Mini Appointments Table Section
        JPanel tableSection = new JPanel(new BorderLayout(0, 8));
        tableSection.setBackground(Color.WHITE);
        tableSection.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                new EmptyBorder(14, 16, 14, 16)
        ));

        JLabel tableTitle = new JLabel("Today's Patient Consultation Schedule");
        tableTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        tableTitle.setForeground(new Color(30, 41, 59));
        tableSection.add(tableTitle, BorderLayout.NORTH);

        appointmentsTable.setRowHeight(36);
        appointmentsTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        appointmentsTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        appointmentsTable.setShowGrid(true);
        appointmentsTable.setGridColor(new Color(241, 245, 249));

        // Status Renderer
        appointmentsTable.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int col) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, col);
                l.setHorizontalAlignment(SwingConstants.CENTER);
                String val = String.valueOf(value);
                if ("CONFIRMED".equalsIgnoreCase(val)) {
                    l.setText("✓ Confirmed");
                    l.setForeground(new Color(16, 185, 129));
                    l.setFont(l.getFont().deriveFont(Font.BOLD));
                } else if ("COMPLETED".equalsIgnoreCase(val)) {
                    l.setText("★ Completed");
                    l.setForeground(new Color(59, 130, 246));
                } else if ("CANCELLED".equalsIgnoreCase(val)) {
                    l.setText("✗ Cancelled");
                    l.setForeground(new Color(239, 68, 68));
                } else {
                    l.setText("⏳ " + val);
                    l.setForeground(new Color(217, 119, 6));
                }
                return l;
            }
        });

        JScrollPane scrollPane = new JScrollPane(appointmentsTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
        tableSection.add(scrollPane, BorderLayout.CENTER);

        mainPanel.add(tableSection, BorderLayout.CENTER);

        // 3. Bottom Status Bar
        JPanel bottomBar = new JPanel(new BorderLayout());
        bottomBar.setOpaque(false);
        lastUpdatedLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lastUpdatedLabel.setForeground(new Color(100, 116, 139));
        bottomBar.add(lastUpdatedLabel, BorderLayout.WEST);

        JLabel tip = new JLabel("💡 PawLink Clinical Dashboard • Synchronized with port 8080");
        tip.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        tip.setForeground(new Color(148, 163, 184));
        bottomBar.add(tip, BorderLayout.EAST);

        mainPanel.add(bottomBar, BorderLayout.SOUTH);

        setContent(mainPanel);
    }

    private JPanel createMetricCard(String title, JLabel valueLabel, String subtitle, Color accentColor, Color bgLight) {
        JPanel card = new JPanel(new BorderLayout(8, 8));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 5, 0, 0, accentColor),
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(226, 232, 240)),
                        new EmptyBorder(16, 20, 16, 20)
                )
        ));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        titleLbl.setForeground(new Color(71, 85, 105));
        card.add(titleLbl, BorderLayout.NORTH);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 36));
        valueLabel.setForeground(accentColor);
        card.add(valueLabel, BorderLayout.CENTER);

        JLabel subLbl = new JLabel(subtitle);
        subLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subLbl.setForeground(new Color(100, 116, 139));
        card.add(subLbl, BorderLayout.SOUTH);

        return card;
    }

    public void refreshData() {
        Long vetId = Session.getInstance().getUserId();
        if (vetId == null) {
            lastUpdatedLabel.setText("Session inactive.");
            return;
        }

        lastUpdatedLabel.setText("Refreshing metrics...");

        // 1. Fetch Today's Appointments: GET /api/appointments?vetId={id}&date=today
        SwingWorker<List<AppointmentDTO>, Void> apptWorker = new SwingWorker<>() {
            @Override
            protected List<AppointmentDTO> doInBackground() {
                try {
                    return ApiClient.getInstance().getList("/api/appointments?vetId=" + vetId + "&date=today", AppointmentDTO.class);
                } catch (Exception ex) {
                    // Fallback to ISO date
                    String today = LocalDate.now().toString();
                    return ApiClient.getInstance().getList("/api/appointments?vetId=" + vetId + "&date=" + today, AppointmentDTO.class);
                }
            }

            @Override
            protected void done() {
                try {
                    List<AppointmentDTO> appts = get();
                    tableModel.setItems(appts);
                    appointmentsCountLabel.setText(String.valueOf(appts.size()));
                } catch (Exception ex) {
                    appointmentsCountLabel.setText("0");
                }
            }
        };
        apptWorker.execute();

        // 2. Fetch Due Reminders: GET /api/reminders?due=true
        SwingWorker<List<ReminderDTO>, Void> reminderWorker = new SwingWorker<>() {
            @Override
            protected List<ReminderDTO> doInBackground() {
                return ApiClient.getInstance().getList("/api/reminders?due=true", ReminderDTO.class);
            }

            @Override
            protected void done() {
                try {
                    List<ReminderDTO> reminders = get();
                    remindersCountLabel.setText(String.valueOf(reminders.size()));
                    lastUpdatedLabel.setText("Last updated: " + java.time.LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
                } catch (Exception ex) {
                    remindersCountLabel.setText("0");
                    lastUpdatedLabel.setText("Could not fetch reminders.");
                }
            }
        };
        reminderWorker.execute();
    }
}

