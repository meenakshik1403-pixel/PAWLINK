package com.pawlink.vet.view;

import com.pawlink.vet.api.ApiClient;
import com.pawlink.vet.api.Session;
import com.pawlink.vet.base.BasePanel;
import com.pawlink.vet.base.BaseTableModel;
import com.pawlink.vet.base.Dialogs;
import com.pawlink.vet.base.PawComponents;
import com.pawlink.vet.base.PawIcons;
import com.pawlink.vet.base.PawTheme;
import com.pawlink.vet.model.SitterDTO;
import com.pawlink.vet.model.UserDocumentDTO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * Modernized Admin Panel for verifying Pet Sitters and reviewing their submitted ID cards and qualifications.
 * Features modern status pills, vector action buttons, and eliminates emoji tofu box artifacts.
 */
public class AdminSittersPanel extends BasePanel {

    private final BaseTableModel<SitterDTO> sitterTableModel;
    private final JTable sitterTable;
    private final JLabel sitterCountLabel = new JLabel("Loading sitters...");
    private List<SitterDTO> sittersList = new ArrayList<>();

    public AdminSittersPanel() {
        super("Pet Sitter Approvals", "Review submitted credentials, ID cards, and qualification proof to approve Pet Sitters.");
        setHeaderIcon(PawIcons.shield(26, PawTheme.MINT));
        setHeaderColor(PawTheme.MINT_TEXT);

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
        JPanel accessory = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        accessory.setOpaque(false);

        JButton refreshBtn = new PawComponents.PawButton("Refresh", PawIcons.refresh(14, PawTheme.TEXT_BODY), PawComponents.ButtonStyle.SECONDARY);
        refreshBtn.addActionListener(e -> loadSitters());
        accessory.add(refreshBtn);

        JButton viewDocsBtn = new PawComponents.PawButton("View Documents", PawIcons.document(14, PawTheme.INDIGO_TEXT), PawComponents.ButtonStyle.INDIGO);
        viewDocsBtn.addActionListener(e -> handleViewDocuments());
        accessory.add(viewDocsBtn);

        JButton approveBtn = new PawComponents.PawButton("Approve Sitter", PawIcons.check(14, Color.WHITE), PawComponents.ButtonStyle.MINT);
        approveBtn.addActionListener(e -> handleApproveSelected());
        accessory.add(approveBtn);

        setHeaderAccessory(accessory);

        // Style Table
        PawTheme.styleTable(sitterTable);
        sitterTable.getColumnModel().getColumn(5).setCellRenderer(new PawComponents.StatusTableCellRenderer());

        sitterTable.getColumnModel().getColumn(0).setPreferredWidth(75);
        sitterTable.getColumnModel().getColumn(0).setMaxWidth(90);
        sitterTable.getColumnModel().getColumn(1).setPreferredWidth(160);
        sitterTable.getColumnModel().getColumn(2).setPreferredWidth(200);
        sitterTable.getColumnModel().getColumn(3).setPreferredWidth(140);
        sitterTable.getColumnModel().getColumn(4).setPreferredWidth(130);
        sitterTable.getColumnModel().getColumn(5).setPreferredWidth(120);

        // Build Panel inside cute PawCard
        PawComponents.PawCard tableCard = new PawComponents.PawCard();
        tableCard.setLayout(new BorderLayout(0, 10));

        JScrollPane scrollPane = new JScrollPane(sitterTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(PawTheme.BORDER_LIGHT));
        tableCard.add(scrollPane, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout(14, 0));
        bottom.setOpaque(false);
        bottom.setBorder(new EmptyBorder(8, 6, 4, 6));

        sitterCountLabel.setFont(PawTheme.FONT_SUBTITLE);
        sitterCountLabel.setForeground(PawTheme.TEXT_MUTED);
        bottom.add(sitterCountLabel, BorderLayout.WEST);

        JLabel tip = new JLabel("Tip: Click 'Approve Sitter' to inspect uploaded documents and confirm their approved rate.");
        tip.setFont(PawTheme.FONT_SMALL);
        tip.setForeground(PawTheme.TEXT_SUBTLE);
        bottom.add(tip, BorderLayout.EAST);

        tableCard.add(bottom, BorderLayout.SOUTH);
        setContent(tableCard);
    }

    @Override
    public void refresh() {
        loadSitters();
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
        docDialog.setSize(620, 420);
        docDialog.setLocationRelativeTo(this);
        docDialog.setLayout(new BorderLayout());

        JPanel header = new JPanel(new GridLayout(2, 1, 0, 4));
        header.setBorder(new EmptyBorder(16, 22, 16, 22));
        header.setBackground(PawTheme.SIDEBAR_BG);

        JLabel t = new JLabel("Submitted Verification Documents for " + name);
        t.setFont(PawTheme.FONT_SECTION);
        t.setForeground(Color.WHITE);
        header.add(t);

        JLabel sub = new JLabel("Click any document below to inspect the uploaded PDF.");
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
            String typeDisplay = isId ? "Government ID Proof" : "Qualifications & Experience Certificate";
            Icon docIcon = isId ? PawIcons.idCard(22, PawTheme.INDIGO) : PawIcons.document(22, PawTheme.LILAC);
            if (isId) {
                card.setBackgroundTint(PawTheme.INDIGO_LIGHT);
                card.setBorderTint(PawTheme.INDIGO);
            } else {
                card.setBackgroundTint(PawTheme.LILAC_LIGHT);
                card.setBorderTint(PawTheme.LILAC);
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

            JButton openBtn = new PawComponents.PawButton("Open PDF", PawIcons.document(13, isId ? PawTheme.INDIGO_TEXT : PawTheme.LILAC_TEXT), isId ? PawComponents.ButtonStyle.INDIGO : PawComponents.ButtonStyle.LILAC);
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
