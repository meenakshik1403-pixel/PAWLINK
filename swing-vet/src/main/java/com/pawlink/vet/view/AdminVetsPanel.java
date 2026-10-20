package com.pawlink.vet.view;

import com.pawlink.vet.api.ApiClient;
import com.pawlink.vet.api.Session;
import com.pawlink.vet.base.BasePanel;
import com.pawlink.vet.base.BaseTableModel;
import com.pawlink.vet.base.Dialogs;
import com.pawlink.vet.model.UserDocumentDTO;
import com.pawlink.vet.model.VetAdminDTO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * Admin Panel for verifying Veterinarians and reviewing their submitted ID cards and Medical Qualifications.
 * Extends BasePanel.
 */
public class AdminVetsPanel extends BasePanel {

    private final BaseTableModel<VetAdminDTO> vetTableModel;
    private final JTable vetTable;
    private final JLabel vetCountLabel = new JLabel("Loading veterinarians...");
    private List<VetAdminDTO> vetsList = new ArrayList<>();

    public AdminVetsPanel() {
        super("🩺 Veterinarian Approvals", "Review medical qualifications, ID cards, and veterinary credentials to approve Doctors.");

        // Setup Vets Table
        String[] vetCols = {"User ID", "Doctor Name", "Email Address", "Phone", "Status"};
        this.vetTableModel = new BaseTableModel<>(vetCols) {
            @Override
            public Object getValueAt(VetAdminDTO v, int col) {
                return switch (col) {
                    case 0 -> v.getId();
                    case 1 -> v.getName();
                    case 2 -> v.getEmail();
                    case 3 -> v.getPhone() != null ? v.getPhone() : "-";
                    case 4 -> v.isVerified() ? "VERIFIED" : "PENDING";
                    default -> "";
                };
            }
        };
        this.vetTable = new JTable(vetTableModel);

        setupUI();
        loadVets();
    }

    private void setupUI() {
        // Header Accessory: Refresh and Action buttons
        JPanel accessory = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        accessory.setOpaque(false);

        JButton refreshBtn = new JButton("🔄 Refresh");
        refreshBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        refreshBtn.addActionListener(e -> loadVets());
        accessory.add(refreshBtn);

        JButton viewDocsBtn = new JButton("📄 View Documents");
        viewDocsBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        viewDocsBtn.addActionListener(e -> handleViewDocuments());
        accessory.add(viewDocsBtn);

        JButton approveBtn = new JButton("✅ Approve Doctor");
        approveBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        approveBtn.setBackground(new Color(16, 185, 129)); // Green
        approveBtn.setForeground(Color.WHITE);
        approveBtn.setFocusable(false);
        approveBtn.addActionListener(e -> handleApproveSelected());
        accessory.add(approveBtn);

        setHeaderAccessory(accessory);

        // Style Table
        styleTable(vetTable, 4);

        // Build Panel
        JPanel mainPanel = new JPanel(new BorderLayout(0, 10));
        mainPanel.setOpaque(false);
        mainPanel.add(new JScrollPane(vetTable), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        bottom.setBorder(new EmptyBorder(6, 6, 6, 6));
        vetCountLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        vetCountLabel.setForeground(new Color(100, 116, 139));
        bottom.add(vetCountLabel, BorderLayout.WEST);
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

    public void loadVets() {
        vetCountLabel.setText("Fetching veterinarians...");
        SwingWorker<List<VetAdminDTO>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<VetAdminDTO> doInBackground() {
                return ApiClient.getInstance().getList("/api/admin/vets", VetAdminDTO.class);
            }

            @Override
            protected void done() {
                try {
                    vetsList = get();
                    vetTableModel.setItems(vetsList);
                    long pendingCount = vetsList.stream().filter(v -> !v.isVerified()).count();
                    vetCountLabel.setText("Total: " + vetsList.size() + " veterinarians (" + pendingCount + " pending approval).");
                } catch (Exception ex) {
                    vetCountLabel.setText("Failed to load veterinarians: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void handleViewDocuments() {
        int row = vetTable.getSelectedRow();
        if (row < 0) {
            Dialogs.showWarning(this, "Please select a veterinarian to view their uploaded documents.");
            return;
        }
        VetAdminDTO v = vetTableModel.getItem(row);
        Long targetUserId = v.getId();
        String targetName = v.getName();

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
                        Dialogs.showInfo(AdminVetsPanel.this, "No uploaded documents found for Dr. " + targetName + ".");
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
        JDialog docDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Verification Documents - Dr. " + name, true);
        docDialog.setSize(520, 320);
        docDialog.setLocationRelativeTo(this);
        docDialog.setLayout(new BorderLayout());

        JPanel header = new JPanel(new GridLayout(2, 1, 0, 4));
        header.setBorder(new EmptyBorder(12, 16, 12, 16));
        header.setBackground(new Color(238, 242, 255));

        JLabel t = new JLabel("Submitted Verification Documents for Dr. " + name);
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

            String typeDisplay = "ID_CARD".equalsIgnoreCase(doc.getDocumentType()) ? "🪪 ID Card Proof" : "🩺 Medical Qualification Certificate";
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
        int row = vetTable.getSelectedRow();
        if (row < 0) {
            Dialogs.showWarning(this, "Please select a veterinarian from the queue to approve.");
            return;
        }
        VetAdminDTO v = vetTableModel.getItem(row);
        if (v.isVerified()) {
            Dialogs.showInfo(this, "Veterinarian '" + v.getName() + "' is already verified.");
            return;
        }

        boolean confirm = Dialogs.showConfirm(this,
                "Are you sure you want to approve veterinarian '" + v.getName() + "'?\n" +
                "This will enable them to log in and conduct clinical appointments.",
                "Confirm Veterinarian Approval");
        if (!confirm) return;

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                ApiClient.getInstance().patch("/api/admin/vets/" + v.getId() + "/approve", null, Void.class);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    Dialogs.showSuccess(AdminVetsPanel.this, "✓ Dr. " + v.getName() + " has been successfully verified!");
                    loadVets();
                } catch (Exception ex) {
                    showError("Failed to approve veterinarian: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }
}

