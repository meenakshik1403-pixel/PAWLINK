package com.pawlink.vet.view;

import com.pawlink.vet.api.ApiClient;
import com.pawlink.vet.api.ApiException;
import com.pawlink.vet.api.Session;
import com.pawlink.vet.base.BasePanel;
import com.pawlink.vet.base.BaseTableModel;
import com.pawlink.vet.base.Dialogs;
import com.pawlink.vet.model.SitterDTO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Pet Owner Panel to browse verified pet sitters, inspect rates, and initiate bookings.
 * Extends BasePanel and uses BaseTableModel<SitterDTO>.
 */
public class FindSitterPanel extends BasePanel {

    private final BaseTableModel<SitterDTO> tableModel;
    private final JTable table;
    private final JTextField searchField;
    private final JLabel statsLabel;
    private List<SitterDTO> allSitters = new ArrayList<>();

    public FindSitterPanel() {
        super("🏡 Find a Pet Sitter", "Browse approved sitters, compare rates, and book care for your pets with escrow protection.");

        // Define generic TableModel
        String[] columns = {"ID", "Sitter Name", "Email Address", "Phone", "Hourly Rate", "Status"};
        this.tableModel = new BaseTableModel<>(columns) {
            @Override
            public Object getValueAt(SitterDTO sitter, int col) {
                return switch (col) {
                    case 0 -> sitter.getUserId();
                    case 1 -> sitter.getName();
                    case 2 -> sitter.getEmail();
                    case 3 -> sitter.getPhone() != null ? sitter.getPhone() : "-";
                    case 4 -> "$" + (sitter.getHourlyRate() != null ? sitter.getHourlyRate() : "0.00") + " / hr";
                    case 5 -> sitter.isVerified() ? "VERIFIED" : "PENDING";
                    default -> "";
                };
            }
        };

        this.table = new JTable(tableModel);
        this.searchField = new JTextField(15);
        this.statsLabel = new JLabel("Loading sitters...");

        setupUI();
        loadSitters();
    }

    private void setupUI() {
        table.setRowHeight(36);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setShowGrid(true);
        table.setGridColor(new Color(241, 245, 249));

        // Custom Cell Renderer for Verification Status Badge
        table.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int col) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, col);
                label.setHorizontalAlignment(SwingConstants.CENTER);
                String val = String.valueOf(value);
                if ("VERIFIED".equals(val)) {
                    label.setText("✓ Verified");
                    label.setForeground(new Color(16, 185, 129));
                    label.setFont(label.getFont().deriveFont(Font.BOLD));
                } else {
                    label.setText("⏳ Pending");
                    label.setForeground(new Color(245, 158, 11));
                    label.setFont(label.getFont().deriveFont(Font.PLAIN));
                }
                return label;
            }
        });

        // Double-click row to book
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedRow() >= 0) {
                    openBookDialog();
                }
            }
        });

        // Header Accessory: Search + Action Buttons
        JPanel accessoryPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        accessoryPanel.setOpaque(false);

        JLabel searchIcon = new JLabel("🔍");
        accessoryPanel.add(searchIcon);

        searchField.putClientProperty("JTextField.placeholderText", "Search sitters...");
        searchField.addActionListener(e -> filterSitters());
        accessoryPanel.add(searchField);

        JButton filterBtn = new JButton("Filter");
        filterBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        filterBtn.addActionListener(e -> filterSitters());
        accessoryPanel.add(filterBtn);

        JButton refreshBtn = new JButton("🔄 Refresh");
        refreshBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        refreshBtn.addActionListener(e -> loadSitters());
        accessoryPanel.add(refreshBtn);

        JButton bookBtn = new JButton("🏡 Book Selected Sitter");
        bookBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        bookBtn.setBackground(new Color(79, 70, 229)); // Indigo
        bookBtn.setForeground(Color.WHITE);
        bookBtn.setFocusable(false);
        bookBtn.addActionListener(e -> openBookDialog());
        accessoryPanel.add(bookBtn);

        setHeaderAccessory(accessoryPanel);

        // Center Content: Scroll Pane + Bottom Bar
        JPanel centerPanel = new JPanel(new BorderLayout(0, 10));
        centerPanel.setOpaque(false);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel bottomBar = new JPanel(new BorderLayout());
        bottomBar.setOpaque(false);
        bottomBar.setBorder(new EmptyBorder(5, 5, 5, 5));

        statsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statsLabel.setForeground(new Color(100, 116, 139));
        bottomBar.add(statsLabel, BorderLayout.WEST);

        JLabel hintLabel = new JLabel("💡 Tip: Select a verified sitter and click 'Book Selected Sitter' or double-click a row.");
        hintLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        hintLabel.setForeground(new Color(148, 163, 184));
        bottomBar.add(hintLabel, BorderLayout.EAST);

        centerPanel.add(bottomBar, BorderLayout.SOUTH);

        setContent(centerPanel);
    }

    public void loadSitters() {
        SwingWorker<List<SitterDTO>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<SitterDTO> doInBackground() {
                return ApiClient.getInstance().getList("/api/sitters", SitterDTO.class);
            }

            @Override
            protected void done() {
                try {
                    allSitters = get();
                    filterSitters();
                } catch (Exception ex) {
                    showError("Failed to load sitters from backend: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void filterSitters() {
        String query = searchField.getText().trim().toLowerCase();
        List<SitterDTO> filtered = new ArrayList<>();
        long verifiedCount = 0;

        for (SitterDTO s : allSitters) {
            if (s.isVerified()) verifiedCount++;
            if (query.isEmpty() ||
                    s.getName().toLowerCase().contains(query) ||
                    s.getEmail().toLowerCase().contains(query)) {
                filtered.add(s);
            }
        }

        tableModel.setItems(filtered);
        statsLabel.setText("Showing " + filtered.size() + " of " + allSitters.size() +
                " sitters (" + verifiedCount + " verified).");
    }

    private void openBookDialog() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            Dialogs.showWarning(this, "Please select a sitter from the table to proceed.");
            return;
        }

        SitterDTO sitter = tableModel.getItem(selectedRow);
        if (sitter == null) return;

        // Domain invariant check upfront for smooth UX
        if (!sitter.isVerified()) {
            Dialogs.showWarning(this,
                    "Sitter '" + sitter.getName() + "' is not verified.\n" +
                    "PawLink safety policy requires sitters to be verified before bookings can be confirmed.");
            return;
        }

        Frame parentFrame = (Frame) SwingUtilities.getWindowAncestor(this);
        BookSitterDialog dialog = new BookSitterDialog(parentFrame, sitter, this::loadSitters);
        dialog.setVisible(true);
    }
}
