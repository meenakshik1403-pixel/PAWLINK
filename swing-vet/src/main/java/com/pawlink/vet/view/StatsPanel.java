package com.pawlink.vet.view;

import com.pawlink.vet.api.ApiClient;
import com.pawlink.vet.base.BasePanel;
import com.pawlink.vet.base.BaseTableModel;
import com.pawlink.vet.model.SystemStatsDTO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Statistics and Platform Analytics Panel (P8).
 * Extends BasePanel and uses BaseTableModel to display aggregated metrics.
 */
public class StatsPanel extends BasePanel {

    private final BaseTableModel<Map.Entry<String, Integer>> vetApptModel;
    private final BaseTableModel<Map.Entry<String, Integer>> roleModel;
    private final BaseTableModel<Map.Entry<String, Integer>> sitterBookingModel;

    private final JTable vetApptTable;
    private final JTable roleTable;
    private final JTable sitterBookingTable;

    private final JLabel totalUsersKpi = new JLabel("0");
    private final JLabel totalApptsKpi = new JLabel("0");
    private final JLabel totalBookingsKpi = new JLabel("0");
    private final JLabel statusLabel = new JLabel("Loading analytics...");

    public StatsPanel() {
        super("Clinic Performance & Appointment Statistics", "System-wide aggregations of consultations, appointments, and registered users.");

        // Table 1: Appointments per Veterinarian
        this.vetApptModel = new BaseTableModel<>(new String[]{"Veterinarian Name", "Appointments Count"}) {
            @Override
            public Object getValueAt(Map.Entry<String, Integer> item, int col) {
                return col == 0 ? item.getKey() : item.getValue() + " appointment(s)";
            }
        };
        this.vetApptTable = new JTable(vetApptModel);

        // Table 2: User Breakdown Across Platform
        this.roleModel = new BaseTableModel<>(new String[]{"Platform Role", "Active User Count"}) {
            @Override
            public Object getValueAt(Map.Entry<String, Integer> item, int col) {
                return col == 0 ? item.getKey() : item.getValue() + " registered";
            }
        };
        this.roleTable = new JTable(roleModel);

        // Table 3: Bookings per Sitter
        this.sitterBookingModel = new BaseTableModel<>(new String[]{"Pet Sitter", "Bookings Assigned"}) {
            @Override
            public Object getValueAt(Map.Entry<String, Integer> item, int col) {
                return col == 0 ? item.getKey() : item.getValue() + " booking(s)";
            }
        };
        this.sitterBookingTable = new JTable(sitterBookingModel);

        setupUI();
        loadStats();
    }

    private void setupUI() {
        // Header Accessory
        JPanel accessory = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        accessory.setOpaque(false);

        JButton refreshBtn = new JButton("🔄 Refresh Analytics");
        refreshBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        refreshBtn.setBackground(new Color(79, 70, 229)); // Indigo
        refreshBtn.setForeground(Color.WHITE);
        refreshBtn.setFocusable(false);
        refreshBtn.addActionListener(e -> loadStats());
        accessory.add(refreshBtn);

        setHeaderAccessory(accessory);

        // Main Center Content
        JPanel mainContent = new JPanel(new BorderLayout(0, 16));
        mainContent.setOpaque(false);

        // 1. KPI Metric Summary Cards
        JPanel kpiGrid = new JPanel(new GridLayout(1, 3, 16, 0));
        kpiGrid.setOpaque(false);

        kpiGrid.add(createKpiCard("Total Registered Users", totalUsersKpi, new Color(79, 70, 229)));
        kpiGrid.add(createKpiCard("Total Clinic Appointments", totalApptsKpi, new Color(13, 148, 136)));
        kpiGrid.add(createKpiCard("Total Sitter Bookings", totalBookingsKpi, new Color(217, 119, 6)));

        mainContent.add(kpiGrid, BorderLayout.NORTH);

        // 2. Tables Grid (Left: Appointments per Vet, Right: Users by Role + Sitters)
        JPanel tablesGrid = new JPanel(new GridLayout(1, 2, 16, 0));
        tablesGrid.setOpaque(false);

        // Left Card: Appointments per Vet
        JPanel leftCard = createTableCard("🏥 Appointments Count per Veterinarian", vetApptTable);
        tablesGrid.add(leftCard);

        // Right Card: Split between User Breakdown and Sitter Bookings
        JPanel rightSplit = new JPanel(new GridLayout(2, 1, 0, 12));
        rightSplit.setOpaque(false);
        rightSplit.add(createTableCard("👥 User Breakdown Across Platform", roleTable));
        rightSplit.add(createTableCard("🏡 Bookings per Pet Sitter", sitterBookingTable));
        tablesGrid.add(rightSplit);

        mainContent.add(tablesGrid, BorderLayout.CENTER);

        // 3. Bottom Status
        JPanel bottomBar = new JPanel(new BorderLayout());
        bottomBar.setOpaque(false);
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(new Color(100, 116, 139));
        bottomBar.add(statusLabel, BorderLayout.WEST);

        JLabel dbTip = new JLabel("⚡ Aggregations calculated directly via SQL COUNT() and GROUP BY");
        dbTip.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        dbTip.setForeground(new Color(148, 163, 184));
        bottomBar.add(dbTip, BorderLayout.EAST);

        mainContent.add(bottomBar, BorderLayout.SOUTH);

        setContent(mainContent);
    }

    private JPanel createKpiCard(String title, JLabel valueLabel, Color accent) {
        JPanel card = new JPanel(new BorderLayout(6, 6));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, accent),
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(226, 232, 240)),
                        new EmptyBorder(14, 18, 14, 18)
                )
        ));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        titleLbl.setForeground(new Color(100, 116, 139));
        card.add(titleLbl, BorderLayout.NORTH);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        valueLabel.setForeground(accent);
        card.add(valueLabel, BorderLayout.CENTER);

        return card;
    }

    private JPanel createTableCard(String title, JTable table) {
        JPanel card = new JPanel(new BorderLayout(0, 8));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                new EmptyBorder(12, 14, 12, 14)
        ));

        JLabel header = new JLabel(title);
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setForeground(new Color(30, 41, 59));
        card.add(header, BorderLayout.NORTH);

        table.setRowHeight(32);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.setShowGrid(true);
        table.setGridColor(new Color(241, 245, 249));

        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        table.getColumnModel().getColumn(1).setCellRenderer(rightRenderer);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(241, 245, 249)));
        card.add(scroll, BorderLayout.CENTER);

        return card;
    }

    public void loadStats() {
        statusLabel.setText("Fetching platform metrics...");

        SwingWorker<SystemStatsDTO, Void> worker = new SwingWorker<>() {
            @Override
            protected SystemStatsDTO doInBackground() {
                return ApiClient.getInstance().get("/api/admin/stats", SystemStatsDTO.class);
            }

            @Override
            protected void done() {
                try {
                    SystemStatsDTO stats = get();
                    if (stats != null) {
                        totalUsersKpi.setText(String.valueOf(stats.getTotalUsers()));
                        totalApptsKpi.setText(String.valueOf(stats.getTotalAppointments()));
                        totalBookingsKpi.setText(String.valueOf(stats.getTotalBookings()));

                        List<Map.Entry<String, Integer>> vetList = new ArrayList<>(stats.getAppointmentsPerVet().entrySet());
                        vetApptModel.setItems(vetList);

                        List<Map.Entry<String, Integer>> roleList = new ArrayList<>(stats.getUsersByRole().entrySet());
                        roleModel.setItems(roleList);

                        List<Map.Entry<String, Integer>> sitterList = new ArrayList<>(stats.getBookingsPerSitter().entrySet());
                        sitterBookingModel.setItems(sitterList);

                        statusLabel.setText("Aggregated metrics up to date.");
                    }
                } catch (Exception ex) {
                    statusLabel.setText("Failed to load statistics: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }
}

