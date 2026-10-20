package com.pawlink.vet.view;

import com.pawlink.vet.api.ApiClient;
import com.pawlink.vet.api.Session;
import com.pawlink.vet.base.BasePanel;
import com.pawlink.vet.base.BaseTableModel;
import com.pawlink.vet.base.Dialogs;
import com.pawlink.vet.model.SitterDTO;
import com.pawlink.vet.model.UserDocumentDTO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * Admin Panel for verifying Pet Sitters and reviewing their submitted ID cards and qualifications.
 * Extends BasePanel.
 */
public class AdminSittersPanel extends BasePanel {

    private final BaseTableModel<SitterDTO> sitterTableModel;
    private final JTable sitterTable;
    private final JLabel sitterCountLabel = new JLabel("Loading sitters...");
    private List<SitterDTO> sittersList = new ArrayList<>();

    public AdminSittersPanel() {
        super("✅ Pet Sitter Approvals", "Review submitted credentials, ID cards, and qualification proof to approve Pet Sitters.");

        // Setup Sitters Table
        String[] sitterCols = {"User ID", "Sitter Name", "Email Address", "Phone", "Hourly Rate", "Status"};
        this.sitterTableModel = new BaseTableModel<>(sitterCols) {
            @Override
            public Object getValueAt(SitterDTO s, int col) {
                return switch (col) {
                    case 0 -> s.getUserId();
                    case 1 -> s.getName();
                    case 2 -> s.getEmail();
                    case 3 -> s.getPhone() != null ? s.getPhone() : "-";
                    case 4 -> "₹" + (s.getHourlyRate() != null ? s.getHourlyRate() : "0.00") + " / hr";
                    case 5 -> s.isVerified() ? "VERIFIED" : "PENDING";
                    default -> "";
                };
            }
        };
        this.sitterTable = new JTable(sitterTableModel);

        setupUI();
        loadSitters();
    }

    private void setupUI() {
        // Global Header Accessory: Refresh and Action buttons
        JPanel accessory = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        accessory.setOpaque(false);

        JButton refreshBtn = new JButton("🔄 Refresh");
        refreshBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        refreshBtn.addActionListener(e -> loadSitters());
        accessory.add(refreshBtn);

        JButton viewDocsBtn = new JButton("📄 View Documents");
        viewDocsBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        viewDocsBtn.addActionListener(e -> handleViewDocuments());
        accessory.add(viewDocsBtn);

        JButton approveBtn = new JButton("✅ Approve Sitter");
        approveBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        approveBtn.setBackground(new Color(16, 185, 129)); // Green
        approveBtn.setForeground(Color.WHITE);
        approveBtn.setFocusable(false);
        approveBtn.addActionListener(e -> handleApproveSelected());
        accessory.add(approveBtn);

        setHeaderAccessory(accessory);

        // Style Table
        styleTable(sitterTable, 5);

        // Build Panel
        JPanel mainPanel = new JPanel(new BorderLayout(0, 10));
        mainPanel.setOpaque(false);
        mainPanel.add(new JScrollPane(sitterTable), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        bottom.setBorder(new EmptyBorder(6, 6, 6, 6));
        sitterCountLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        sitterCountLabel.setForeground(new Color(100, 116, 139));
        bottom.add(sitterCountLabel, BorderLayout.WEST);
        mainPanel.add(bottom, BorderLayout.SOUTH);

        setContent(mainPanel);
    }

    private void styleTable(JTable table, int statusColIndex) {
        table.setRowHeight(36);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setShowGrid(true);
        table.setGridColor(new Color(241, 245, 249));

        table.getColumnModel().getColumn(statusColIndex).setCellRenderer(new DefaultTableCellRenderer() {
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
                    l.setFont(l.getFont().deriveFont(Font.PLAIN));
                }
                return l;
            }
        });
    }

    public void loadSitters() {
        sitterCountLabel.setText("Fetching sitters...");
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
                    sitterTableModel.setItems(sittersList);
                    long pendingCount = sittersList.stream().filter(s -> !s.isVerified()).count();
                    sitterCountLabel.setText("Total: " + sittersList.size() + " sitters (" + pendingCount + " pending approval).");
                } catch (Exception ex) {
                    sitterCountLabel.setText("Failed to load sitters: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void handleViewDocuments() {
        int row = sitterTable.getSelectedRow();
        if (row < 0) {
            Dialogs.showWarning(this, "Please select a sitter to view their uploaded documents.");
            return;
        }
        SitterDTO s = sitterTableModel.getItem(row);
        Long targetUserId = s.getUserId();
        String targetName = s.getName();

        SwingWorker<List<UserDocumentDTO>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<UserDocumentDTO> doInBackground() {
                return ApiClient.getInstance().getList("/api/admin/documents/" + targetUserId, UserDocumentDTO.class);
            }

            @Override
            protected void done() {
                try {
                    List<UserDocumentDTO> docs = get();
                    if (docs == null || docs.isEmpty()) {
                        Dialogs.showInfo(AdminSittersPanel.this, "No uploaded documents found for " + targetName + ".");
                        return;
                    }
                    showDocumentsDialog(targetName, docs);
                } catch (Exception ex) {
                    showError("Failed to fetch documents: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void showDocumentsDialog(String name, List<UserDocumentDTO> docs) {
        JDialog docDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Verification Documents - " + name, true);
        docDialog.setSize(520, 320);
        docDialog.setLocationRelativeTo(this);
        docDialog.setLayout(new BorderLayout());

        JPanel header = new JPanel(new GridLayout(2, 1, 0, 4));
        header.setBorder(new EmptyBorder(12, 16, 12, 16));
        header.setBackground(new Color(238, 242, 255));

        JLabel t = new JLabel("Submitted Verification Documents for " + name);
        t.setFont(new Font("Segoe UI", Font.BOLD, 14));
        t.setForeground(new Color(67, 56, 202));
        header.add(t);

        JLabel sub = new JLabel("Click any document below to open and inspect the PDF.");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        sub.setForeground(new Color(107, 114, 128));
        header.add(sub);
        docDialog.add(header, BorderLayout.NORTH);

        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBorder(new EmptyBorder(14, 16, 14, 16));

        for (UserDocumentDTO doc : docs) {
            JPanel card = new JPanel(new BorderLayout(10, 0));
            card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(226, 232, 240)),
                    BorderFactory.createEmptyBorder(8, 12, 8, 12)
            ));

            String typeDisplay = "ID_CARD".equalsIgnoreCase(doc.getDocumentType()) ? "🪪 ID Card Proof" : "📜 Qualifications & Experience Certificate";
            JLabel info = new JLabel("<html><b>" + typeDisplay + "</b><br/>" + doc.getFileName() + " (" + String.format("%.2f", doc.getFileSize() / (1024.0 * 1024.0)) + " MB)</html>");
            info.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            card.add(info, BorderLayout.CENTER);

            JButton openBtn = new JButton("🌐 Open PDF");
            openBtn.setFont(new Font("Segoe UI", Font.BOLD, 11));
            openBtn.addActionListener(e -> {
                try {
                    String url = "http://localhost:8080/api/admin/documents/download/" + doc.getId() + "?adminId=" + Session.getInstance().getUserId();
                    Desktop.getDesktop().browse(URI.create(url));
                } catch (Exception ex) {
                    Dialogs.showError(docDialog, "Could not open document viewer: " + ex.getMessage());
                }
            });
            card.add(openBtn, BorderLayout.EAST);

            listPanel.add(card);
            listPanel.add(Box.createVerticalStrut(8));
        }

        docDialog.add(new JScrollPane(listPanel), BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton closeBtn = new JButton("Close");
        closeBtn.addActionListener(e -> docDialog.dispose());
        footer.add(closeBtn);
        docDialog.add(footer, BorderLayout.SOUTH);

        docDialog.setVisible(true);
    }

    private void handleApproveSelected() {
        int row = sitterTable.getSelectedRow();
        if (row < 0) {
            Dialogs.showWarning(this, "Please select a sitter from the queue to approve.");
            return;
        }
        SitterDTO s = sitterTableModel.getItem(row);
        if (s.isVerified()) {
            Dialogs.showInfo(this, "Sitter '" + s.getName() + "' is already verified.");
            return;
        }

        Frame parent = (Frame) SwingUtilities.getWindowAncestor(this);
        ApproveSitterDialog dialog = new ApproveSitterDialog(parent, s, this::loadSitters);
        dialog.setVisible(true);
    }
}
