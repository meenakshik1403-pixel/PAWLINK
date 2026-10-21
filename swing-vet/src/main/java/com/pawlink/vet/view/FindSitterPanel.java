package com.pawlink.vet.view;

import com.pawlink.vet.api.ApiClient;
import com.pawlink.vet.base.BasePanel;
import com.pawlink.vet.base.BaseTableModel;
import com.pawlink.vet.base.Dialogs;
import com.pawlink.vet.base.PawComponents;
import com.pawlink.vet.base.PawIcons;
import com.pawlink.vet.base.PawTheme;
import com.pawlink.vet.model.SitterDTO;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Modernized Pet Owner Panel to browse verified pet sitters, inspect rates, and initiate bookings.
 * Completely eliminates emoji tofu box artifacts by utilizing scalable vector icons and modern status pills.
 */
public class FindSitterPanel extends BasePanel {

    private final BaseTableModel<SitterDTO> tableModel;
    private final JTable table;
    private final JTextField searchField;
    private final JLabel statsLabel;
    private List<SitterDTO> allSitters = new ArrayList<>();

    public FindSitterPanel() {
        super("Find a Pet Sitter", "Browse verified sitters, compare rates, and book care with escrow protection.");
        setHeaderIcon(PawIcons.sitter(26, PawTheme.SUNNY));
        setHeaderColor(PawTheme.SUNNY_TEXT);

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
                    case 4 -> "₹" + (sitter.getHourlyRate() != null ? sitter.getHourlyRate() : "0.00") + " / hr";
                    case 5 -> sitter.isVerified() ? "VERIFIED" : "PENDING";
                    default -> "";
                };
            }
        };

        this.table = new JTable(tableModel);
        this.searchField = new JTextField(16);
        this.statsLabel = new JLabel("Loading sitters...");

        setupUI();
        loadSitters();
    }

    private void setupUI() {
        PawTheme.styleTable(table);
        table.getColumnModel().getColumn(5).setCellRenderer(new PawComponents.StatusTableCellRenderer());

        table.getColumnModel().getColumn(0).setPreferredWidth(60);
        table.getColumnModel().getColumn(0).setMaxWidth(80);
        table.getColumnModel().getColumn(1).setPreferredWidth(160);
        table.getColumnModel().getColumn(2).setPreferredWidth(200);
        table.getColumnModel().getColumn(3).setPreferredWidth(140);
        table.getColumnModel().getColumn(4).setPreferredWidth(130);
        table.getColumnModel().getColumn(5).setPreferredWidth(120);

        // Double-click row to book
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedRow() >= 0) {
                    openBookDialog();
                }
            }
        });

        // Header Accessory: Refresh and Book Sitter Action
        JPanel accessoryPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        accessoryPanel.setOpaque(false);

        JButton refreshBtn = new PawComponents.PawButton("Refresh", PawIcons.refresh(14, PawTheme.TEXT_BODY), PawComponents.ButtonStyle.SECONDARY);
        refreshBtn.addActionListener(e -> loadSitters());
        accessoryPanel.add(refreshBtn);

        JButton bookBtn = new PawComponents.PawButton("Book Sitter", PawIcons.calendar(14, PawTheme.SUNNY_TEXT), PawComponents.ButtonStyle.SUNNY);
        bookBtn.addActionListener(e -> openBookDialog());
        accessoryPanel.add(bookBtn);

        setHeaderAccessory(accessoryPanel);

        // Center Content: Scroll Pane inside cute PawCard + Search Toolbar + Bottom Bar
        PawComponents.PawCard tableCard = new PawComponents.PawCard();
        tableCard.setLayout(new BorderLayout(0, 10));

        // Dedicated search toolbar above table
        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        searchBar.setBackground(PawTheme.SURFACE);
        searchBar.setBorder(new CompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, PawTheme.BORDER_LIGHT),
                new EmptyBorder(4, 12, 4, 12)
        ));

        JLabel searchIcon = new JLabel(PawIcons.search(16, PawTheme.TEXT_MUTED));
        searchBar.add(searchIcon);

        PawTheme.styleField(searchField);
        searchField.putClientProperty("JTextField.placeholderText", "Search sitters by name or email...");
        searchField.setPreferredSize(new Dimension(240, 36));
        searchField.addActionListener(e -> filterSitters());
        searchBar.add(searchField);

        JButton filterBtn = new PawComponents.PawButton("Filter", PawComponents.ButtonStyle.SECONDARY);
        filterBtn.addActionListener(e -> filterSitters());
        searchBar.add(filterBtn);

        JButton clearBtn = new PawComponents.PawButton("Clear", PawComponents.ButtonStyle.SECONDARY);
        clearBtn.addActionListener(e -> {
            searchField.setText("");
            filterSitters();
        });
        searchBar.add(clearBtn);

        tableCard.add(searchBar, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(PawTheme.BORDER_LIGHT));
        tableCard.add(scrollPane, BorderLayout.CENTER);

        JPanel bottomBar = new JPanel(new BorderLayout(14, 0));
        bottomBar.setOpaque(false);
        bottomBar.setBorder(new EmptyBorder(8, 6, 4, 6));

        statsLabel.setFont(PawTheme.FONT_SUBTITLE);
        statsLabel.setForeground(PawTheme.TEXT_MUTED);
        bottomBar.add(statsLabel, BorderLayout.WEST);

        JLabel hintLabel = new JLabel("Tip: Double-click any verified sitter to initiate an escrow booking request.");
        hintLabel.setFont(PawTheme.FONT_SMALL);
        hintLabel.setForeground(PawTheme.TEXT_SUBTLE);
        bottomBar.add(hintLabel, BorderLayout.EAST);

        tableCard.add(bottomBar, BorderLayout.SOUTH);
        setContent(tableCard);
    }

    @Override
    public void refresh() {
        loadSitters();
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
            if (!s.isVerified()) {
                continue; // Do not show unverified sitters
            }
            verifiedCount++;
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
