package com.pawlink.vet.view;

import com.pawlink.vet.api.ApiClient;
import com.pawlink.vet.api.Session;
import com.pawlink.vet.base.BasePanel;
import com.pawlink.vet.base.BaseTableModel;
import com.pawlink.vet.base.Dialogs;
import com.pawlink.vet.base.PawComponents;
import com.pawlink.vet.base.PawIcons;
import com.pawlink.vet.base.PawTheme;
import com.pawlink.vet.model.UserDocumentDTO;
import com.pawlink.vet.model.VetAdminDTO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * Modernized Admin Panel for verifying Veterinarians and reviewing their submitted ID cards and Medical Qualifications.
 * Eliminates all emoji tofu boxes by utilizing vector icons and modern status pills.
 */
public class AdminVetsPanel extends BasePanel {

    private final BaseTableModel<VetAdminDTO> vetTableModel;
    private final JTable vetTable;
    private final JLabel vetCountLabel = new JLabel("Loading veterinarians...");
    private List<VetAdminDTO> vetsList = new ArrayList<>();

    public AdminVetsPanel() {
        super("Veterinarian Approvals", "Review medical qualifications, ID cards, and veterinary credentials to approve Doctors.");
        setHeaderIcon(PawIcons.stethoscope(26, PawTheme.SKY));
        setHeaderColor(PawTheme.SKY_TEXT);

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
        JPanel accessory = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        accessory.setOpaque(false);

        JButton refreshBtn = new PawComponents.PawButton("Refresh", PawIcons.refresh(14, PawTheme.TEXT_BODY), PawComponents.ButtonStyle.SECONDARY);
        refreshBtn.addActionListener(e -> loadVets());
        accessory.add(refreshBtn);

        JButton viewDocsBtn = new PawComponents.PawButton("View Documents", PawIcons.document(14, PawTheme.INDIGO_TEXT), PawComponents.ButtonStyle.INDIGO);
        viewDocsBtn.addActionListener(e -> handleViewDocuments());
        accessory.add(viewDocsBtn);

        JButton approveBtn = new PawComponents.PawButton("Approve Doctor", PawIcons.check(14, Color.WHITE), PawComponents.ButtonStyle.MINT);
        approveBtn.addActionListener(e -> handleApproveSelected());
        accessory.add(approveBtn);

        setHeaderAccessory(accessory);

        // Style Table
        PawTheme.styleTable(vetTable);
        vetTable.getColumnModel().getColumn(4).setCellRenderer(new PawComponents.StatusTableCellRenderer());

        vetTable.getColumnModel().getColumn(0).setPreferredWidth(75);
        vetTable.getColumnModel().getColumn(0).setMaxWidth(90);
        vetTable.getColumnModel().getColumn(1).setPreferredWidth(180);
        vetTable.getColumnModel().getColumn(2).setPreferredWidth(220);
        vetTable.getColumnModel().getColumn(3).setPreferredWidth(140);
        vetTable.getColumnModel().getColumn(4).setPreferredWidth(120);

        // Build Panel inside cute PawCard
        PawComponents.PawCard tableCard = new PawComponents.PawCard();
        tableCard.setLayout(new BorderLayout(0, 10));

        JScrollPane scrollPane = new JScrollPane(vetTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(PawTheme.BORDER_LIGHT));
        tableCard.add(scrollPane, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout(14, 0));
        bottom.setOpaque(false);
        bottom.setBorder(new EmptyBorder(8, 6, 4, 6));

        vetCountLabel.setFont(PawTheme.FONT_SUBTITLE);
        vetCountLabel.setForeground(PawTheme.TEXT_MUTED);
        bottom.add(vetCountLabel, BorderLayout.WEST);

        JLabel tip = new JLabel("Tip: Review Doctor credentials and medical degrees before granting clinical privileges.");
        tip.setFont(PawTheme.FONT_SMALL);
        tip.setForeground(PawTheme.TEXT_SUBTLE);
        bottom.add(tip, BorderLayout.EAST);

        tableCard.add(bottom, BorderLayout.SOUTH);
        setContent(tableCard);
    }

    @Override
    public void refresh() {
        loadVets();
    }

    public void loadVets() {
        vetCountLabel.setText("Fetching veterinarians...");
        SwingWorker<List<VetAdminDTO>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<VetAdminDTO> doInBackground() {
                try {
                    return ApiClient.getInstance().getList("/api/admin/vets", VetAdminDTO.class);
                } catch (Exception ex) {
                    return ApiClient.getInstance().getList("/api/vets", VetAdminDTO.class);
                }
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
        docDialog.setSize(620, 420);
        docDialog.setLocationRelativeTo(this);
        docDialog.setLayout(new BorderLayout());

        JPanel header = new JPanel(new GridLayout(2, 1, 0, 4));
        header.setBorder(new EmptyBorder(16, 22, 16, 22));
        header.setBackground(PawTheme.SIDEBAR_BG);

        JLabel t = new JLabel("Submitted Verification Documents for Dr. " + name);
        t.setFont(PawTheme.FONT_SECTION);
        t.setForeground(Color.WHITE);
        header.add(t);

        JLabel sub = new JLabel("Click any document below to inspect the uploaded medical credentials.");
        sub.setFont(PawTheme.FONT_SUBTITLE);
        sub.setForeground(PawTheme.TEXT_SUBTLE);
        header.add(sub);
        docDialog.add(header, BorderLayout.NORTH);

        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBorder(new EmptyBorder(16, 22, 16, 22));
        listPanel.setBackground(PawTheme.CANVAS);

        for (UserDocumentDTO doc : docs) {
            PawComponents.PawCard card = new PawComponents.PawCard(12);
            card.setLayout(new BorderLayout(14, 0));
            card.setBorder(new EmptyBorder(12, 16, 12, 16));

            boolean isId = "ID_CARD".equalsIgnoreCase(doc.getDocumentType());
            String typeDisplay = isId ? "Government ID Proof" : "Medical Qualification Certificate";
            Icon docIcon = isId ? PawIcons.idCard(22, PawTheme.INDIGO) : PawIcons.stethoscope(22, PawTheme.SKY);
            if (isId) {
                card.setBackgroundTint(PawTheme.INDIGO_LIGHT);
                card.setBorderTint(PawTheme.INDIGO);
            } else {
                card.setBackgroundTint(PawTheme.SKY_LIGHT);
                card.setBorderTint(PawTheme.SKY);
            }

            JPanel infoCol = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
            infoCol.setOpaque(false);
            infoCol.add(new JLabel(docIcon));

            JPanel textCol = new JPanel();
            textCol.setLayout(new BoxLayout(textCol, BoxLayout.Y_AXIS));
            textCol.setOpaque(false);

            JLabel typeLbl = new JLabel(typeDisplay);
            typeLbl.setFont(PawTheme.FONT_BODY_BOLD);
            typeLbl.setForeground(PawTheme.TEXT_TITLE);
            textCol.add(typeLbl);

            JLabel fileLbl = new JLabel(doc.getFileName() + " (" + String.format("%.2f", doc.getFileSize() / (1024.0 * 1024.0)) + " MB)");
            fileLbl.setFont(PawTheme.FONT_SMALL);
            fileLbl.setForeground(PawTheme.TEXT_MUTED);
            textCol.add(fileLbl);

            infoCol.add(textCol);
            card.add(infoCol, BorderLayout.CENTER);

            JButton openBtn = new PawComponents.PawButton("Open PDF", PawIcons.document(13, isId ? PawTheme.INDIGO_TEXT : PawTheme.SKY_TEXT), isId ? PawComponents.ButtonStyle.INDIGO : PawComponents.ButtonStyle.SKY);
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
            listPanel.add(Box.createVerticalStrut(10));
        }

        docDialog.add(new JScrollPane(listPanel), BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 12));
        footer.setBackground(PawTheme.SURFACE);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, PawTheme.BORDER_LIGHT));

        JButton closeBtn = new PawComponents.PawButton("Close", PawComponents.ButtonStyle.SECONDARY);
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
                    Dialogs.showSuccess(AdminVetsPanel.this, "Dr. " + v.getName() + " has been successfully verified!");
                    loadVets();
                } catch (Exception ex) {
                    showError("Failed to approve veterinarian: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }
}
