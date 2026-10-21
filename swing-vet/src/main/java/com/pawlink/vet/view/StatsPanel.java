package com.pawlink.vet.view;

import com.pawlink.vet.api.ApiClient;
import com.pawlink.vet.base.BasePanel;
import com.pawlink.vet.base.BaseTableModel;
import com.pawlink.vet.base.PawComponents;
import com.pawlink.vet.base.PawIcons;
import com.pawlink.vet.base.PawTheme;
import com.pawlink.vet.model.SystemStatsDTO;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Statistics and Platform Analytics Panel.
 * Modernized with pure Java2D vector icons, card styling, and high-contrast typography.
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
        super("Clinic Performance & Analytics", "System-wide aggregations of consultations, appointments, and registered users.");

        setHeaderIcon(PawIcons.stats(22, PawTheme.LILAC));
        setHeaderColor(PawTheme.LILAC_TEXT);

        // Table 1: Appointments per Veterinarian
        this.vetApptModel = new BaseTableModel<>(new String[]{"Veterinarian Name", "Appointments Count"}) {
            @Override
            public Object getValueAt(Map.Entry<String, Integer> item, int col) {
                return col == 0 ? item.getKey() : item.getValue() + " consultation(s)";
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

        PawComponents.PawButton refreshBtn = new PawComponents.PawButton("Refresh Analytics", PawIcons.refresh(13, Color.WHITE), PawComponents.ButtonStyle.LILAC);
        refreshBtn.addActionListener(e -> loadStats());
        accessory.add(refreshBtn);

        setHeaderAccessory(accessory);

        // Main Center Content
        JPanel mainContent = new JPanel(new BorderLayout(0, 16));
        mainContent.setOpaque(false);

        // 1. KPI Metric Summary Cards (Multi-Coloured Cute Pastels)
        JPanel kpiGrid = new JPanel(new GridLayout(1, 3, 18, 0));
        kpiGrid.setOpaque(false);

        kpiGrid.add(createKpiCard("Total Registered Users", totalUsersKpi, PawIcons.user(24, PawTheme.LILAC), PawTheme.LILAC, PawTheme.LILAC_LIGHT));
        kpiGrid.add(createKpiCard("Total Clinic Appointments", totalApptsKpi, PawIcons.calendar(24, PawTheme.MINT), PawTheme.MINT, PawTheme.MINT_LIGHT));
        kpiGrid.add(createKpiCard("Total Sitter Bookings", totalBookingsKpi, PawIcons.sitter(24, PawTheme.PEACH), PawTheme.PEACH, PawTheme.PEACH_LIGHT));

        mainContent.add(kpiGrid, BorderLayout.NORTH);

        // 2. Tables Grid (Left: Appointments per Vet, Right: Users by Role + Sitters)
        JPanel tablesGrid = new JPanel(new GridLayout(1, 2, 18, 0));
        tablesGrid.setOpaque(false);

        // Left Card: Appointments per Vet
        JPanel leftCard = createTableCard("Appointments Count per Veterinarian", vetApptTable);
        tablesGrid.add(leftCard);

        // Right Card: Split between User Breakdown and Sitter Bookings
        JPanel rightSplit = new JPanel(new GridLayout(2, 1, 0, 14));
        rightSplit.setOpaque(false);
        rightSplit.add(createTableCard("User Breakdown Across Platform", roleTable));
        rightSplit.add(createTableCard("Bookings per Pet Sitter", sitterBookingTable));
        tablesGrid.add(rightSplit);

        mainContent.add(tablesGrid, BorderLayout.CENTER);

        // 3. Bottom Status
        JPanel bottomBar = new JPanel(new BorderLayout());
        bottomBar.setOpaque(false);
        statusLabel.setFont(PawTheme.FONT_BODY);
        statusLabel.setForeground(PawTheme.TEXT_MUTED);
        bottomBar.add(statusLabel, BorderLayout.WEST);

        JLabel dbTip = new JLabel("Aggregations calculated directly via SQL COUNT() and GROUP BY");
        dbTip.setFont(PawTheme.FONT_SMALL);
        dbTip.setForeground(PawTheme.TEXT_SUBTLE);
        bottomBar.add(dbTip, BorderLayout.EAST);

        mainContent.add(bottomBar, BorderLayout.SOUTH);

        setContent(mainContent);
    }

    private JPanel createKpiCard(String title, JLabel valueLabel, Icon icon, Color accent, Color bgTint) {
        PawComponents.PawCard card = new PawComponents.PawCard(16);
        card.setCardBackground(bgTint);
        card.setCardBorderColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 60));
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

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 34));
        valueLabel.setForeground(accent);
        card.add(valueLabel, BorderLayout.CENTER);

        return card;
    }

    private JPanel createTableCard(String title, JTable table) {
        PawComponents.PawCard card = new PawComponents.PawCard(16);
        card.setLayout(new BorderLayout(0, 12));

        JLabel header = new JLabel(title);
        header.setFont(PawTheme.FONT_SECTION);
        header.setForeground(PawTheme.TEXT_TITLE);
        card.add(header, BorderLayout.NORTH);

        PawTheme.styleTable(table);

        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        table.getColumnModel().getColumn(1).setCellRenderer(rightRenderer);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new CompoundBorder(
                new LineBorder(PawTheme.BORDER_LIGHT, 1, true),
                new EmptyBorder(0, 0, 0, 0)
        ));
        scroll.getViewport().setBackground(PawTheme.SURFACE);
        card.add(scroll, BorderLayout.CENTER);

        return card;
    }

    @Override
    public void refresh() {
        loadStats();
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

                        statusLabel.setText("Aggregated platform metrics up to date.");
                    }
                } catch (Exception ex) {
                    statusLabel.setText("Failed to load statistics: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }
}
