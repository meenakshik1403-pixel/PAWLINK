package com.pawlink.vet.view;

import com.pawlink.vet.api.ApiClient;
import com.pawlink.vet.api.Session;
import com.pawlink.vet.base.BasePanel;
import com.pawlink.vet.base.BaseTableModel;
import com.pawlink.vet.model.ReminderDTO;
import com.pawlink.vet.model.ReminderRunResponseDTO;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Automated Reminders & Observer Notification Engine Panel.
 * Extends BasePanel as per team architecture.
 *
 * UI Layout:
 * - Top bar: Title + prominent "Run Reminder Check Now" JButton + Filter dropdown + "Schedule Reminder".
 * - Center: JTable showing Pet Name, Owner Name, Reminder Type, Due Date, Urgency, and Sent Status.
 * - Bottom: Real-time metrics bar (Total, Due, Sent counts) and manual dispatch actions.
 * - Clicking "Run Reminder Check Now" sends POST /api/reminders/run via ApiClient and reloads table.
 */
public class RemindersPanel extends BasePanel {

    private final JButton runCheckButton;
    private final JComboBox<String> filterComboBox;
    private final JButton createReminderButton;
    private final JButton refreshButton;

    private final ReminderTableModel tableModel;
    private final JTable reminderTable;

    private final JLabel statsLabel;

    private List<ReminderDTO> allReminders = new ArrayList<>();

    public RemindersPanel() {
        super("🔔 Automated Reminders & Observer Engine",
                "Scan upcoming vaccination and medication due dates; trigger multi-channel email/SMS dispatches");

        // 1. Top Controls Bar
        runCheckButton = new JButton("⚡ Run Reminder Check Now");
        runCheckButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        runCheckButton.setBackground(new Color(234, 88, 12)); // Bold Orange
        runCheckButton.setForeground(Color.WHITE);
        runCheckButton.setFocusPainted(false);
        runCheckButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        runCheckButton.setToolTipText("Manually scan and dispatch notifications for all due and overdue pet care reminders");

        filterComboBox = new JComboBox<>(new String[]{"All Reminders", "Due / Overdue Only", "Sent / Dispatched Only", "Upcoming Only"});
        filterComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        createReminderButton = new JButton("➕ Schedule Reminder");
        createReminderButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        createReminderButton.setBackground(new Color(13, 148, 136)); // Teal
        createReminderButton.setForeground(Color.WHITE);
        createReminderButton.setFocusPainted(false);
        createReminderButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        refreshButton = new JButton("🔄 Refresh");
        refreshButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        refreshButton.setFocusPainted(false);
        refreshButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JPanel topBar = createTopBar();

        // 2. Center Table
        tableModel = new ReminderTableModel();
        reminderTable = new JTable(tableModel);
        reminderTable.setRowHeight(36);
        reminderTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        reminderTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        reminderTable.getTableHeader().setBackground(new Color(241, 245, 249));
        reminderTable.getTableHeader().setForeground(new Color(30, 41, 59));
        reminderTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        reminderTable.setShowGrid(true);
        reminderTable.setGridColor(new Color(226, 232, 240));

        setupTableRenderers();

        JScrollPane scrollPane = new JScrollPane(reminderTable);
        scrollPane.setBorder(new CompoundBorder(
                new LineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(0, 0, 0, 0)
        ));

        // 3. Bottom Status & Metrics Bar
        statsLabel = new JLabel("Loading reminders...");
        statsLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        statsLabel.setForeground(new Color(71, 85, 105));

        JPanel bottomBar = createBottomBar();

        // Main assembly
        JPanel container = new JPanel(new BorderLayout(0, 12));
        container.setOpaque(false);
        container.add(topBar, BorderLayout.NORTH);
        container.add(scrollPane, BorderLayout.CENTER);
        container.add(bottomBar, BorderLayout.SOUTH);

        setContent(container);

        initListeners();
        loadReminders();
    }

    private JPanel createTopBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        bar.setBackground(Color.WHITE);
        bar.setBorder(new CompoundBorder(
                new LineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(8, 12, 8, 12)
        ));

        bar.add(runCheckButton);

        JSeparator sep1 = new JSeparator(SwingConstants.VERTICAL);
        sep1.setPreferredSize(new Dimension(2, 26));
        bar.add(sep1);

        JLabel filterLbl = new JLabel("Filter View:");
        filterLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        bar.add(filterLbl);
        bar.add(filterComboBox);

        JSeparator sep2 = new JSeparator(SwingConstants.VERTICAL);
        sep2.setPreferredSize(new Dimension(2, 26));
        bar.add(sep2);

        bar.add(createReminderButton);
        bar.add(refreshButton);

        return bar;
    }

    private JPanel createBottomBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(Color.WHITE);
        bar.setBorder(new CompoundBorder(
                new LineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(10, 16, 10, 16)
        ));

        bar.add(statsLabel, BorderLayout.WEST);

        JLabel helpNote = new JLabel("💡 Reminders automatically trigger Observer notifications to Pet Owners & Sitters via Email/SMS adapters");
        helpNote.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        helpNote.setForeground(new Color(100, 116, 139));
        bar.add(helpNote, BorderLayout.EAST);

        return bar;
    }

    private void setupTableRenderers() {
        // Urgency Chip Renderer (Column 4)
        reminderTable.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                label.setHorizontalAlignment(SwingConstants.CENTER);
                String urgency = value != null ? value.toString() : "";

                if (!isSelected) {
                    switch (urgency) {
                        case "OVERDUE" -> {
                            label.setBackground(new Color(254, 226, 226)); // soft red
                            label.setForeground(new Color(185, 28, 28));   // deep red
                            label.setText("⚠ OVERDUE");
                        }
                        case "DUE_TODAY" -> {
                            label.setBackground(new Color(254, 243, 199)); // amber
                            label.setForeground(new Color(180, 83, 9));
                            label.setText("⏱ DUE TODAY");
                        }
                        case "UPCOMING" -> {
                            label.setBackground(new Color(224, 242, 254)); // light blue
                            label.setForeground(new Color(3, 105, 161));
                            label.setText("📅 UPCOMING");
                        }
                        case "SENT" -> {
                            label.setBackground(new Color(220, 252, 231)); // green
                            label.setForeground(new Color(22, 101, 52));
                            label.setText("✔ DISPATCHED");
                        }
                        default -> {
                            label.setBackground(Color.WHITE);
                            label.setForeground(Color.BLACK);
                        }
                    }
                    label.setOpaque(true);
                }
                return label;
            }
        });

        // Sent Status Badge Renderer (Column 5)
        reminderTable.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                label.setHorizontalAlignment(SwingConstants.CENTER);
                boolean sent = Boolean.TRUE.equals(value);
                if (!isSelected) {
                    if (sent) {
                        label.setBackground(new Color(240, 253, 244));
                        label.setForeground(new Color(22, 163, 74));
                        label.setText("Sent (TRUE)");
                    } else {
                        label.setBackground(new Color(254, 242, 242));
                        label.setForeground(new Color(220, 38, 38));
                        label.setText("Pending (FALSE)");
                    }
                    label.setOpaque(true);
                }
                return label;
            }
        });

        // Column widths
        reminderTable.getColumnModel().getColumn(0).setPreferredWidth(50);  // ID
        reminderTable.getColumnModel().getColumn(1).setPreferredWidth(120); // Pet
        reminderTable.getColumnModel().getColumn(2).setPreferredWidth(150); // Owner Contact
        reminderTable.getColumnModel().getColumn(3).setPreferredWidth(130); // Type
        reminderTable.getColumnModel().getColumn(4).setPreferredWidth(120); // Urgency
        reminderTable.getColumnModel().getColumn(5).setPreferredWidth(110); // Sent Status
    }

    private void initListeners() {
        runCheckButton.addActionListener(e -> triggerObserverCheck());

        filterComboBox.addActionListener(e -> applyFilter());

        createReminderButton.addActionListener(e -> {
            Window parentWindow = SwingUtilities.getWindowAncestor(this);
            Frame parentFrame = parentWindow instanceof Frame ? (Frame) parentWindow : null;
            CreateReminderDialog dialog = new CreateReminderDialog(parentFrame, this::loadReminders);
            dialog.setVisible(true);
        });

        refreshButton.addActionListener(e -> loadReminders());
    }

    public void loadReminders() {
        refreshButton.setEnabled(false);

        SwingWorker<List<ReminderDTO>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<ReminderDTO> doInBackground() throws Exception {
                String uri;
                Long userId = Session.getInstance().getUserId();
                if (userId != null && Session.getInstance().isOwner()) {
                    uri = "/api/reminders?ownerId=" + userId;
                } else {
                    uri = "/api/reminders";
                }
                return ApiClient.getInstance().getList(uri, ReminderDTO.class);
            }

            @Override
            protected void done() {
                refreshButton.setEnabled(true);
                try {
                    allReminders = get();
                    applyFilter();
                } catch (Exception ex) {
                    showError("Failed to fetch reminders: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void applyFilter() {
        String filter = (String) filterComboBox.getSelectedItem();
        List<ReminderDTO> filtered;

        if ("Due / Overdue Only".equals(filter)) {
            filtered = allReminders.stream()
                    .filter(r -> "OVERDUE".equals(r.getUrgency()) || "DUE_TODAY".equals(r.getUrgency()))
                    .collect(Collectors.toList());
        } else if ("Sent / Dispatched Only".equals(filter)) {
            filtered = allReminders.stream()
                    .filter(ReminderDTO::isSent)
                    .collect(Collectors.toList());
        } else if ("Upcoming Only".equals(filter)) {
            filtered = allReminders.stream()
                    .filter(r -> "UPCOMING".equals(r.getUrgency()))
                    .collect(Collectors.toList());
        } else {
            filtered = new ArrayList<>(allReminders);
        }

        tableModel.setItems(filtered);
        updateStats();
    }

    private void updateStats() {
        long total = allReminders.size();
        long dueCount = allReminders.stream()
                .filter(r -> !r.isSent() && ("OVERDUE".equals(r.getUrgency()) || "DUE_TODAY".equals(r.getUrgency())))
                .count();
        long sentCount = allReminders.stream().filter(ReminderDTO::isSent).count();

        statsLabel.setText(String.format("📊 Total Reminders: %d | ⚡ Due / Overdue for Dispatch: %d | ✔ Dispatched: %d",
                total, dueCount, sentCount));
    }

    /**
     * Executes the backend Observer Pattern Scheduler engine: POST /api/reminders/run
     */
    private void triggerObserverCheck() {
        runCheckButton.setEnabled(false);
        runCheckButton.setText("Scanning & Dispatching...");

        SwingWorker<ReminderRunResponseDTO, Void> worker = new SwingWorker<>() {
            @Override
            protected ReminderRunResponseDTO doInBackground() throws Exception {
                return ApiClient.getInstance().post("/api/reminders/run", null, ReminderRunResponseDTO.class);
            }

            @Override
            protected void done() {
                runCheckButton.setEnabled(true);
                runCheckButton.setText("⚡ Run Reminder Check Now");
                try {
                    ReminderRunResponseDTO resp = get();
                    int count = resp != null ? resp.getProcessedCount() : 0;
                    String message = resp != null ? resp.getMessage() : "Check complete.";

                    JOptionPane.showMessageDialog(
                            RemindersPanel.this,
                            message + "\n\n" +
                                    "Dispatched Alerts: " + count + "\n" +
                                    "Channels: Email & SMS (Simulated Adapter logs in server console)",
                            "Observer Engine Execution Complete",
                            JOptionPane.INFORMATION_MESSAGE
                    );

                    // Reload the table
                    loadReminders();
                } catch (Exception ex) {
                    showError("Failed to run reminder engine: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    /**
     * Table Model mapping ReminderDTOs to table columns.
     */
    private static class ReminderTableModel extends BaseTableModel<ReminderDTO> {

        public ReminderTableModel() {
            super(new String[]{"ID", "Pet", "Owner Contact", "Reminder Type", "Urgency Status", "Sent"});
        }

        @Override
        public Object getValueAt(ReminderDTO r, int columnIndex) {
            if (r == null) return null;
            return switch (columnIndex) {
                case 0 -> r.getId();
                case 1 -> r.getPetName() != null ? r.getPetName() : "Pet #" + r.getPetId();
                case 2 -> {
                    String name = r.getOwnerName() != null ? r.getOwnerName() : "Owner #" + r.getOwnerId();
                    yield r.getOwnerEmail() != null ? name + " (" + r.getOwnerEmail() + ")" : name;
                }
                case 3 -> r.getType();
                case 4 -> r.getUrgency();
                case 5 -> r.isSent();
                default -> null;
            };
        }
    }
}

