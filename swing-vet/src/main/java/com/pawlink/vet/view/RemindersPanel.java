package com.pawlink.vet.view;

import com.pawlink.vet.api.ApiClient;
import com.pawlink.vet.api.Session;
import com.pawlink.vet.base.BasePanel;
import com.pawlink.vet.base.BaseTableModel;
import com.pawlink.vet.base.PawComponents;
import com.pawlink.vet.base.PawIcons;
import com.pawlink.vet.base.PawTheme;
import com.pawlink.vet.model.ReminderDTO;
import com.pawlink.vet.model.ReminderRunResponseDTO;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Modernized Automated Reminders & Observer Notification Engine Panel.
 * Features high-contrast typography, pure Java2D vector icons, and tactile micro-animated controls.
 */
public class RemindersPanel extends BasePanel {

    private final PawComponents.PawButton runCheckButton;
    private final JComboBox<String> filterComboBox;
    private final PawComponents.PawButton createReminderButton;
    private final PawComponents.PawButton refreshButton;

    private final ReminderTableModel tableModel;
    private final JTable reminderTable;

    private final JLabel totalCountLabel;
    private final JLabel dueCountLabel;
    private final JLabel sentCountLabel;

    private List<ReminderDTO> allReminders = new ArrayList<>();

    public RemindersPanel() {
        super("Automated Reminders & Care Engine",
                "Scan upcoming vaccination and medication due dates; trigger multi-channel email/SMS dispatches");

        setHeaderIcon(PawIcons.bell(24, PawTheme.SUNNY));
        setHeaderColor(PawTheme.SUNNY_TEXT);

        // 1. Top Controls Bar
        runCheckButton = new PawComponents.PawButton("Run Observer Scan", PawIcons.clock(15, PawTheme.SUNNY_TEXT), PawComponents.ButtonStyle.SUNNY);
        runCheckButton.setToolTipText("Manually scan and dispatch notifications for all due and overdue pet care reminders");

        filterComboBox = new JComboBox<>(new String[]{"All Reminders", "Due / Overdue Only", "Sent / Dispatched Only", "Upcoming Only"});
        filterComboBox.setFont(PawTheme.FONT_BODY);
        filterComboBox.setPreferredSize(new Dimension(190, 36));

        createReminderButton = new PawComponents.PawButton("Schedule Reminder", PawIcons.plus(13, Color.WHITE), PawComponents.ButtonStyle.CORAL);
        refreshButton = new PawComponents.PawButton("Refresh", PawIcons.refresh(13, PawTheme.TEXT_BODY), PawComponents.ButtonStyle.SECONDARY);

        JPanel topBar = createTopBar();

        // 2. Center Table inside PawCard
        tableModel = new ReminderTableModel();
        reminderTable = new JTable(tableModel);
        PawTheme.styleTable(reminderTable);
        setupTableRenderers();

        PawComponents.PawCard tableCard = new PawComponents.PawCard();
        tableCard.setLayout(new BorderLayout(0, 10));

        JScrollPane scrollPane = new JScrollPane(reminderTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(PawTheme.BORDER_LIGHT));
        scrollPane.getViewport().setBackground(PawTheme.SURFACE);
        tableCard.add(scrollPane, BorderLayout.CENTER);

        // 3. Bottom Status & Metrics Bar
        totalCountLabel = createStatBadge("Total:", "0", PawTheme.INDIGO);
        dueCountLabel = createStatBadge("Due / Overdue:", "0", PawTheme.CORAL);
        sentCountLabel = createStatBadge("Dispatched:", "0", PawTheme.MINT);

        JPanel bottomBar = createBottomBar();
        tableCard.add(bottomBar, BorderLayout.SOUTH);

        // Main assembly
        JPanel container = new JPanel(new BorderLayout(0, 14));
        container.setOpaque(false);
        container.add(topBar, BorderLayout.NORTH);
        container.add(tableCard, BorderLayout.CENTER);

        setContent(container);

        initListeners();
        loadReminders();
    }

    private JLabel createStatBadge(String prefix, String val, Color highlight) {
        JLabel lbl = new JLabel(prefix + " " + val);
        lbl.setFont(PawTheme.FONT_BODY_BOLD);
        lbl.setForeground(highlight);
        return lbl;
    }

    private JPanel createTopBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        bar.setBackground(PawTheme.SURFACE);
        bar.setBorder(new CompoundBorder(
                new LineBorder(PawTheme.BORDER_LIGHT, 1, true),
                new EmptyBorder(8, 14, 8, 14)
        ));

        bar.add(runCheckButton);

        JSeparator sep1 = new JSeparator(SwingConstants.VERTICAL);
        sep1.setPreferredSize(new Dimension(1, 26));
        sep1.setForeground(PawTheme.BORDER_LIGHT);
        bar.add(sep1);

        JLabel filterLbl = new JLabel("Filter:");
        filterLbl.setFont(PawTheme.FONT_BODY_BOLD);
        filterLbl.setForeground(PawTheme.TEXT_BODY);
        bar.add(filterLbl);
        bar.add(filterComboBox);

        JSeparator sep2 = new JSeparator(SwingConstants.VERTICAL);
        sep2.setPreferredSize(new Dimension(1, 26));
        sep2.setForeground(PawTheme.BORDER_LIGHT);
        bar.add(sep2);

        bar.add(createReminderButton);
        bar.add(refreshButton);

        return bar;
    }

    private JPanel createBottomBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(PawTheme.SURFACE);
        bar.setBorder(new CompoundBorder(
                new LineBorder(PawTheme.BORDER_LIGHT, 1, true),
                new EmptyBorder(10, 16, 10, 16)
        ));

        JPanel statsRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        statsRow.setOpaque(false);
        statsRow.add(totalCountLabel);
        statsRow.add(new JSeparator(SwingConstants.VERTICAL));
        statsRow.add(dueCountLabel);
        statsRow.add(new JSeparator(SwingConstants.VERTICAL));
        statsRow.add(sentCountLabel);

        bar.add(statsRow, BorderLayout.WEST);

        JLabel helpNote = new JLabel("Observer Pattern Engine dispatches real-time reminders via Email & SMS adapters");
        helpNote.setFont(PawTheme.FONT_SMALL);
        helpNote.setForeground(PawTheme.TEXT_MUTED);
        bar.add(helpNote, BorderLayout.EAST);

        return bar;
    }

    private void setupTableRenderers() {
        // Due Date Renderer (Column 4 - Centered)
        DefaultTableCellRenderer dateRenderer = new DefaultTableCellRenderer();
        dateRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        reminderTable.getColumnModel().getColumn(4).setCellRenderer(dateRenderer);

        // Urgency Chip Renderer (Column 5)
        reminderTable.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                String urgency = value != null ? value.toString() : "";
                PawComponents.StatusPill pill;

                switch (urgency) {
                    case "OVERDUE" -> pill = new PawComponents.StatusPill("OVERDUE", PawTheme.DANGER_LIGHT, PawTheme.DANGER_TEXT, PawTheme.DANGER);
                    case "DUE_TODAY" -> pill = new PawComponents.StatusPill("DUE TODAY", PawTheme.WARNING_LIGHT, PawTheme.WARNING_TEXT, PawTheme.WARNING);
                    case "UPCOMING" -> pill = new PawComponents.StatusPill("UPCOMING", PawTheme.INFO_LIGHT, PawTheme.INFO_TEXT, PawTheme.INFO);
                    case "SENT" -> pill = new PawComponents.StatusPill("DISPATCHED", PawTheme.SUCCESS_LIGHT, PawTheme.SUCCESS_TEXT, PawTheme.SUCCESS);
                    default -> pill = new PawComponents.StatusPill(urgency.isEmpty() ? "STANDARD" : urgency, PawTheme.SURFACE_MUTED, PawTheme.TEXT_MUTED, PawTheme.TEXT_MUTED);
                }

                JPanel holder = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 4));
                holder.setOpaque(isSelected);
                if (isSelected) {
                    holder.setBackground(table.getSelectionBackground());
                } else {
                    holder.setBackground(row % 2 == 0 ? PawTheme.SURFACE : new Color(250, 250, 252));
                }
                holder.add(pill);
                return holder;
            }
        });

        // Sent Status Badge Renderer (Column 6)
        reminderTable.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                boolean sent = Boolean.TRUE.equals(value);
                PawComponents.StatusPill pill = sent
                        ? new PawComponents.StatusPill("DISPATCHED", PawTheme.SUCCESS_LIGHT, PawTheme.SUCCESS_TEXT, PawTheme.SUCCESS)
                        : new PawComponents.StatusPill("PENDING", PawTheme.WARNING_LIGHT, PawTheme.WARNING_TEXT, PawTheme.WARNING);

                JPanel holder = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 4));
                holder.setOpaque(isSelected);
                if (isSelected) {
                    holder.setBackground(table.getSelectionBackground());
                } else {
                    holder.setBackground(row % 2 == 0 ? PawTheme.SURFACE : new Color(250, 250, 252));
                }
                holder.add(pill);
                return holder;
            }
        });

        // Column widths
        reminderTable.getColumnModel().getColumn(0).setPreferredWidth(50);  // ID
        reminderTable.getColumnModel().getColumn(1).setPreferredWidth(130); // Pet
        reminderTable.getColumnModel().getColumn(2).setPreferredWidth(170); // Owner Contact
        reminderTable.getColumnModel().getColumn(3).setPreferredWidth(130); // Type
        reminderTable.getColumnModel().getColumn(4).setPreferredWidth(110); // Due Date
        reminderTable.getColumnModel().getColumn(5).setPreferredWidth(130); // Urgency
        reminderTable.getColumnModel().getColumn(6).setPreferredWidth(120); // Sent Status
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

    @Override
    public void refresh() {
        loadReminders();
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
                    allReminders = deduplicateReminders(get());
                    applyFilter();
                } catch (Exception ex) {
                    showError("Failed to fetch reminders: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private List<ReminderDTO> deduplicateReminders(List<ReminderDTO> list) {
        if (list == null) return Collections.emptyList();
        Map<String, ReminderDTO> map = new LinkedHashMap<>();
        for (ReminderDTO r : list) {
            String key = r.getPetId() + "_" + (r.getType() != null ? r.getType().toUpperCase() : "") + "_" + r.getDueDate();
            ReminderDTO existing = map.get(key);
            if (existing == null) {
                map.put(key, r);
            } else if (!existing.isSent() && r.isSent()) {
                // Keep dispatched status if one was sent
                map.put(key, r);
            }
        }
        return new ArrayList<>(map.values());
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

        totalCountLabel.setText("Total: " + total);
        dueCountLabel.setText("Due / Overdue: " + dueCount);
        sentCountLabel.setText("Dispatched: " + sentCount);
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
                runCheckButton.setText("Run Observer Scan");
                try {
                    ReminderRunResponseDTO resp = get();
                    int count = resp != null ? resp.getProcessedCount() : 0;
                    String message = resp != null ? resp.getMessage() : "Check complete.";

                    JOptionPane.showMessageDialog(
                            RemindersPanel.this,
                            message + "\n\n" +
                                    "Dispatched Alerts: " + count + "\n" +
                                    "Channels: Email & SMS adapters active",
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
            super(new String[]{"ID", "Pet", "Owner Contact", "Reminder Type", "Due Date", "Urgency Status", "Sent"});
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
                case 4 -> r.getDueDate() != null ? r.getDueDate().toString() : "-";
                case 5 -> r.getUrgency();
                case 6 -> r.isSent();
                default -> null;
            };
        }
    }
}
