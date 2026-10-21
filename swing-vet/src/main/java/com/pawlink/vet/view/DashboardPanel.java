package com.pawlink.vet.view;

import com.pawlink.vet.api.ApiClient;
import com.pawlink.vet.api.Session;
import com.pawlink.vet.base.BasePanel;
import com.pawlink.vet.base.BaseTableModel;
import com.pawlink.vet.base.PawComponents;
import com.pawlink.vet.base.PawIcons;
import com.pawlink.vet.base.PawTheme;
import com.pawlink.vet.model.AppointmentDTO;
import com.pawlink.vet.model.ReminderDTO;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Veterinarian Overview Dashboard.
 * Displays today's scheduled consultations and pending reminders count.
 * Modernized with pure Java2D vector icons, high-contrast typography, and tactile buttons.
 */
public class DashboardPanel extends BasePanel {

    private final BaseTableModel<AppointmentDTO> tableModel;
    private final JTable appointmentsTable;
    private final JLabel appointmentsCountLabel;
    private final JLabel remindersCountLabel;
    private final JLabel lastUpdatedLabel;

    public DashboardPanel() {
        super("Veterinarian Clinical Dashboard", "Real-time clinical summary of today's schedule, patient consultations, and alert notifications.");

        setHeaderIcon(PawIcons.stethoscope(22, PawTheme.PRIMARY));
        setHeaderColor(PawTheme.PRIMARY);

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

        PawComponents.PawButton refreshBtn = new PawComponents.PawButton("Refresh Dashboard", PawIcons.refresh(13, Color.WHITE), PawComponents.ButtonStyle.PRIMARY);
        refreshBtn.addActionListener(e -> refreshData());
        headerAccessory.add(refreshBtn);

        setHeaderAccessory(headerAccessory);

        // Center: Metrics Cards (Top) + Today's Appointments Mini Table (Center)
        JPanel mainPanel = new JPanel(new BorderLayout(0, 16));
        mainPanel.setOpaque(false);

        // 1. Two Visual Summary Cards
        JPanel cardsGrid = new JPanel(new GridLayout(1, 2, 20, 0));
        cardsGrid.setOpaque(false);

        // Card 1: Today's Appointments (Cute Sky Tint)
        JPanel appointmentsCard = createMetricCard(
                "Today's Scheduled Consultations",
                appointmentsCountLabel,
                "Consultations scheduled for " + LocalDate.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")),
                PawIcons.calendar(24, PawTheme.SKY),
                PawTheme.SKY,
                PawTheme.SKY_LIGHT
        );
        cardsGrid.add(appointmentsCard);

        // Card 2: Pending Reminders Due (Cute Honey Tint)
        JPanel remindersCard = createMetricCard(
                "Pending Reminders Due",
                remindersCountLabel,
                "Unsent clinical vaccination & checkup alerts",
                PawIcons.bell(24, PawTheme.SUNNY),
                PawTheme.SUNNY,
                PawTheme.SUNNY_LIGHT
        );
        cardsGrid.add(remindersCard);

        mainPanel.add(cardsGrid, BorderLayout.NORTH);

        // 2. Mini Appointments Table Section
        PawComponents.PawCard tableSection = new PawComponents.PawCard(16);
        tableSection.setLayout(new BorderLayout(0, 12));

        JLabel tableTitle = new JLabel("Today's Patient Consultation Schedule");
        tableTitle.setFont(PawTheme.FONT_SECTION);
        tableTitle.setForeground(PawTheme.TEXT_TITLE);
        tableSection.add(tableTitle, BorderLayout.NORTH);

        PawTheme.styleTable(appointmentsTable);

        // Status Renderer
        appointmentsTable.getColumnModel().getColumn(3).setCellRenderer(new PawComponents.StatusTableCellRenderer());
        appointmentsTable.getColumnModel().getColumn(0).setPreferredWidth(120);
        appointmentsTable.getColumnModel().getColumn(1).setPreferredWidth(180);
        appointmentsTable.getColumnModel().getColumn(2).setPreferredWidth(180);
        appointmentsTable.getColumnModel().getColumn(3).setPreferredWidth(120);

        JScrollPane scrollPane = new JScrollPane(appointmentsTable);
        scrollPane.setBorder(new CompoundBorder(
                new LineBorder(PawTheme.BORDER_LIGHT, 1, true),
                new EmptyBorder(0, 0, 0, 0)
        ));
        scrollPane.getViewport().setBackground(PawTheme.SURFACE);
        tableSection.add(scrollPane, BorderLayout.CENTER);

        mainPanel.add(tableSection, BorderLayout.CENTER);

        // 3. Bottom Status Bar
        JPanel bottomBar = new JPanel(new BorderLayout());
        bottomBar.setOpaque(false);
        lastUpdatedLabel.setFont(PawTheme.FONT_BODY);
        lastUpdatedLabel.setForeground(PawTheme.TEXT_MUTED);
        bottomBar.add(lastUpdatedLabel, BorderLayout.WEST);

        JLabel tip = new JLabel("PawLink Clinical Dashboard • Secure Backend Connected");
        tip.setFont(PawTheme.FONT_SMALL);
        tip.setForeground(PawTheme.TEXT_SUBTLE);
        bottomBar.add(tip, BorderLayout.EAST);

        mainPanel.add(bottomBar, BorderLayout.SOUTH);

        setContent(mainPanel);
    }

    private JPanel createMetricCard(String title, JLabel valueLabel, String subtitle, Icon icon, Color accentColor, Color bgTint) {
        PawComponents.PawCard card = new PawComponents.PawCard(16);
        card.setCardBackground(bgTint);
        card.setCardBorderColor(new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), 60));
        card.setLayout(new BorderLayout(12, 10));

        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(PawTheme.FONT_BODY_BOLD);
        titleLbl.setForeground(PawTheme.TEXT_MUTED);
        topRow.add(titleLbl, BorderLayout.WEST);

        if (icon != null) {
            topRow.add(new JLabel(icon), BorderLayout.EAST);
        }
        card.add(topRow, BorderLayout.NORTH);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 36));
        valueLabel.setForeground(accentColor);
        card.add(valueLabel, BorderLayout.CENTER);

        JLabel subLbl = new JLabel(subtitle);
        subLbl.setFont(PawTheme.FONT_SMALL);
        subLbl.setForeground(PawTheme.TEXT_MUTED);
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

        // 1. Fetch Today's Appointments
        SwingWorker<List<AppointmentDTO>, Void> apptWorker = new SwingWorker<>() {
            @Override
            protected List<AppointmentDTO> doInBackground() {
                try {
                    return ApiClient.getInstance().getList("/api/appointments?vetId=" + vetId + "&date=today", AppointmentDTO.class);
                } catch (Exception ex) {
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

        // 2. Fetch Due Reminders
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

    @Override
    public void refresh() {
        refreshData();
    }
}
