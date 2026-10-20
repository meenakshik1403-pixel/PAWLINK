package com.pawlink.vet.view;

import com.pawlink.vet.api.ApiClient;
import com.pawlink.vet.base.BasePanel;
import com.pawlink.vet.base.BaseTableModel;
import com.pawlink.vet.base.Dialogs;
import com.pawlink.vet.model.SitterDTO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * Admin Panel for verifying pet sitters (P8 Admin Vertical).
 * Extends BasePanel and uses BaseTableModel<SitterDTO>.
 */
public class AdminSittersPanel extends BasePanel {

    private final BaseTableModel<SitterDTO> tableModel;
    private final JTable table;
    private final JLabel countLabel = new JLabel("Loading sitters...");
    private List<SitterDTO> sittersList = new ArrayList<>();

    public AdminSittersPanel() {
        super("✅ Pet Sitter Verification Queue", "Review registered sitter applications and approve verified sitters for public bookings.");

        String[] cols = {"User ID", "Sitter Name", "Email Address", "Phone", "Hourly Rate", "Status"};
        this.tableModel = new BaseTableModel<>(cols) {
            @Override
            public Object getValueAt(SitterDTO s, int col) {
                return switch (col) {
                    case 0 -> s.getUserId();
                    case 1 -> s.getName();
                    case 2 -> s.getEmail();
                    case 3 -> s.getPhone() != null ? s.getPhone() : "-";
                    case 4 -> "$" + (s.getHourlyRate() != null ? s.getHourlyRate() : "0.00") + " / hr";
                    case 5 -> s.isVerified() ? "VERIFIED" : "PENDING";
                    default -> "";
                };
            }
        };

        this.table = new JTable(tableModel);

        setupUI();
        loadSitters();
    }

    private void setupUI() {
        // Header Accessory
        JPanel accessory = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        accessory.setOpaque(false);

        JButton refreshBtn = new JButton("🔄 Refresh");
        refreshBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        refreshBtn.addActionListener(e -> loadSitters());
        accessory.add(refreshBtn);

        JButton approveBtn = new JButton("✅ Approve Selected Sitter");
        approveBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        approveBtn.setBackground(new Color(16, 185, 129)); // Green
        approveBtn.setForeground(Color.WHITE);
        approveBtn.setFocusable(false);
        approveBtn.addActionListener(e -> handleApproveSelected());
        accessory.add(approveBtn);

        setHeaderAccessory(accessory);

        // Center Table
        JPanel centerPanel = new JPanel(new BorderLayout(0, 10));
        centerPanel.setOpaque(false);

        table.setRowHeight(36);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setShowGrid(true);
        table.setGridColor(new Color(241, 245, 249));

        // Verification Status Renderer
        table.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int col) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, col);
                l.setHorizontalAlignment(SwingConstants.CENTER);
                String val = String.valueOf(value);
                if ("VERIFIED".equals(val)) {
                    l.setText("✓ Verified");
                    l.setForeground(new Color(16, 185, 129));
                    l.setFont(l.getFont().deriveFont(Font.BOLD));
                } else {
                    l.setText("⏳ Pending Approval");
                    l.setForeground(new Color(217, 119, 6));
                    l.setFont(l.getFont().deriveFont(Font.BOLD));
                }
                return l;
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        // Bottom Bar
        JPanel bottomBar = new JPanel(new BorderLayout());
        bottomBar.setOpaque(false);
        bottomBar.setBorder(new EmptyBorder(6, 6, 6, 6));

        countLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        countLabel.setForeground(new Color(100, 116, 139));
        bottomBar.add(countLabel, BorderLayout.WEST);

        JLabel tip = new JLabel("💡 Approving a sitter unlocks their profile for owner bookings immediately.");
        tip.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        tip.setForeground(new Color(148, 163, 184));
        bottomBar.add(tip, BorderLayout.EAST);

        centerPanel.add(bottomBar, BorderLayout.SOUTH);

        setContent(centerPanel);
    }

    public void loadSitters() {
        countLabel.setText("Fetching sitters...");

        SwingWorker<List<SitterDTO>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<SitterDTO> doInBackground() {
                try {
                    return ApiClient.getInstance().getList("/api/admin/sitters", SitterDTO.class);
                } catch (Exception ex) {
                    return ApiClient.getInstance().getList("/api/sitters", SitterDTO.class);
                }
            }

            @Override
            protected void done() {
                try {
                    sittersList = get();
                    tableModel.setItems(sittersList);
                    long pendingCount = sittersList.stream().filter(s -> !s.isVerified()).count();
                    countLabel.setText("Total: " + sittersList.size() + " sitters (" + pendingCount + " pending approval).");
                } catch (Exception ex) {
                    showError("Failed to load sitters: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void handleApproveSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            Dialogs.showWarning(this, "Please select a sitter from the queue to approve.");
            return;
        }

        SitterDTO selected = tableModel.getItem(row);
        if (selected == null) return;

        if (selected.isVerified()) {
            Dialogs.showInfo(this, "Sitter '" + selected.getName() + "' is already verified.");
            return;
        }

        boolean confirm = Dialogs.showConfirm(this,
                "Are you sure you want to approve sitter '" + selected.getName() + "'?\n" +
                "This will allow pet parents to book sessions with them immediately.",
                "Confirm Sitter Approval");

        if (!confirm) return;

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                // Execute PATCH /api/admin/sitters/{id}/approve
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:8080/api/admin/sitters/" + selected.getUserId() + "/approve"))
                        .method("PATCH", HttpRequest.BodyPublishers.noBody())
                        .header("Accept", "application/json")
                        .build();

                java.net.http.HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.discarding());
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    Dialogs.showSuccess(AdminSittersPanel.this,
                            "✓ Sitter '" + selected.getName() + "' has been successfully verified!");
                    loadSitters();
                } catch (Exception ex) {
                    showError("Failed to approve sitter: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }
}

