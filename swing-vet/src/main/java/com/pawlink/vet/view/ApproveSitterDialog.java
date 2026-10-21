package com.pawlink.vet.view;

import com.pawlink.vet.api.ApiClient;
import com.pawlink.vet.api.Session;
import com.pawlink.vet.base.Dialogs;
import com.pawlink.vet.base.PawComponents;
import com.pawlink.vet.base.PawIcons;
import com.pawlink.vet.base.PawTheme;
import com.pawlink.vet.model.SitterDTO;
import com.pawlink.vet.model.UserDocumentDTO;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.math.BigDecimal;
import java.net.URI;
import java.util.List;
import java.util.Map;

/**
 * Modernized Modal dialog for inspecting submitted verification documents and setting
 * an approved hourly rate (₹/hr) when an administrator approves a Pet Sitter.
 * Uses scalable vector icons and eliminates emoji tofu boxes.
 */
public class ApproveSitterDialog extends JDialog {

    private final SitterDTO sitter;
    private final Runnable onSuccess;

    private final JTextField rateField;
    private final JPanel docsContainer;
    private final JLabel loadingLabel;
    private final JButton approveButton;

    public ApproveSitterDialog(Frame parent, SitterDTO sitter, Runnable onSuccess) {
        super(parent, "Approve Pet Sitter - " + sitter.getName(), true);
        this.sitter = sitter;
        this.onSuccess = onSuccess;

        setSize(620, 580);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        JPanel root = new JPanel(new BorderLayout(0, 16));
        root.setBackground(PawTheme.CANVAS);
        root.setBorder(new EmptyBorder(20, 24, 20, 24));

        // 1. Header Banner
        JPanel header = new JPanel(new BorderLayout(14, 0));
        header.setBackground(PawTheme.SIDEBAR_BG);
        header.setBorder(new EmptyBorder(18, 24, 18, 24));

        JLabel iconLabel = new JLabel(PawIcons.sitter(26, PawTheme.SUNNY));
        header.add(iconLabel, BorderLayout.WEST);

        JPanel headerText = new JPanel(new GridLayout(2, 1, 0, 4));
        headerText.setOpaque(false);
        JLabel nameLabel = new JLabel("Approve Sitter: " + sitter.getName());
        nameLabel.setFont(PawTheme.FONT_TITLE);
        nameLabel.setForeground(Color.WHITE);
        headerText.add(nameLabel);

        JLabel subLabel = new JLabel("Email: " + sitter.getEmail() + "  |  Phone: " + (sitter.getPhone() != null ? sitter.getPhone() : "-"));
        subLabel.setFont(PawTheme.FONT_SMALL);
        subLabel.setForeground(PawTheme.TEXT_SUBTLE);
        headerText.add(subLabel);
        header.add(headerText, BorderLayout.CENTER);

        root.add(header, BorderLayout.NORTH);

        // 2. Center Content: Uploaded Documents list + Rate input
        JPanel centerPanel = new JPanel(new BorderLayout(0, 14));
        centerPanel.setOpaque(false);

        // 2a. Documents section
        JPanel docsSection = new JPanel(new BorderLayout(0, 8));
        docsSection.setOpaque(false);

        JLabel docsTitle = new JLabel("Submitted Verification Documents (Review proofs before approving):");
        docsTitle.setFont(PawTheme.FONT_BODY_BOLD);
        docsTitle.setForeground(PawTheme.TEXT_TITLE);
        docsSection.add(docsTitle, BorderLayout.NORTH);

        docsContainer = new JPanel();
        docsContainer.setLayout(new BoxLayout(docsContainer, BoxLayout.Y_AXIS));
        docsContainer.setBackground(PawTheme.SURFACE);
        docsContainer.setBorder(new EmptyBorder(12, 12, 12, 12));

        loadingLabel = new JLabel("Fetching submitted certificates & ID documents...");
        loadingLabel.setFont(PawTheme.FONT_SUBTITLE);
        loadingLabel.setForeground(PawTheme.TEXT_MUTED);
        docsContainer.add(loadingLabel);

        JScrollPane docsScroll = new JScrollPane(docsContainer);
        docsScroll.setPreferredSize(new Dimension(540, 200));
        docsScroll.setBorder(new LineBorder(PawTheme.BORDER_LIGHT, 1, true));
        docsSection.add(docsScroll, BorderLayout.CENTER);

        centerPanel.add(docsSection, BorderLayout.CENTER);

        // 2b. Hourly Rate Form Card
        JPanel rateCard = new JPanel(new BorderLayout(0, 8));
        rateCard.setOpaque(true);
        rateCard.setBackground(PawTheme.SUNNY_LIGHT);
        rateCard.setBorder(new CompoundBorder(
                new LineBorder(PawTheme.SUNNY, 1, true),
                new EmptyBorder(14, 18, 14, 18)
        ));

        JPanel rateInputRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        rateInputRow.setOpaque(false);

        JLabel rateLabel = new JLabel("Set Approved Hourly Rate (₹/hr) *:");
        rateLabel.setFont(PawTheme.FONT_BODY_BOLD);
        rateLabel.setForeground(PawTheme.SUNNY_TEXT);
        rateInputRow.add(rateLabel);

        String initialRate = sitter.getHourlyRate() != null ? sitter.getHourlyRate().toString() : "250.00";
        rateField = new JTextField(initialRate, 10);
        PawTheme.styleField(rateField);
        rateField.setFont(PawTheme.FONT_BODY_BOLD);
        rateInputRow.add(rateField);

        rateCard.add(rateInputRow, BorderLayout.NORTH);

        JLabel hintLabel = new JLabel("Evaluate experience and qualification certificates above, then set their final booking rate in ₹ INR.");
        hintLabel.setFont(PawTheme.FONT_SMALL);
        hintLabel.setForeground(PawTheme.TEXT_MUTED);
        rateCard.add(hintLabel, BorderLayout.SOUTH);

        centerPanel.add(rateCard, BorderLayout.SOUTH);

        root.add(centerPanel, BorderLayout.CENTER);

        // 3. Bottom Actions
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        footer.setOpaque(false);

        JButton cancelButton = new PawComponents.PawButton("Cancel", PawComponents.ButtonStyle.SECONDARY);
        cancelButton.addActionListener(e -> dispose());
        footer.add(cancelButton);

        approveButton = new PawComponents.PawButton("Approve Sitter & Save Rate", PawIcons.check(14, Color.WHITE), PawComponents.ButtonStyle.MINT);
        approveButton.addActionListener(e -> handleApproveWithRate());
        footer.add(approveButton);

        root.add(footer, BorderLayout.SOUTH);

        add(root);

        loadDocumentsAsync();
    }

    private void loadDocumentsAsync() {
        SwingWorker<List<UserDocumentDTO>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<UserDocumentDTO> doInBackground() {
                return ApiClient.getInstance().getList("/api/admin/documents/" + sitter.getUserId(), UserDocumentDTO.class);
            }

            @Override
            protected void done() {
                docsContainer.removeAll();
                try {
                    List<UserDocumentDTO> docs = get();
                    if (docs == null || docs.isEmpty()) {
                        JLabel noneLabel = new JLabel("No verification documents found for this sitter.");
                        noneLabel.setFont(PawTheme.FONT_SUBTITLE);
                        noneLabel.setForeground(PawTheme.TEXT_SUBTLE);
                        docsContainer.add(noneLabel);
                    } else {
                        for (UserDocumentDTO doc : docs) {
                            PawComponents.PawCard card = new PawComponents.PawCard(10);
                            card.setLayout(new BorderLayout(12, 0));
                            card.setBorder(new EmptyBorder(10, 14, 10, 14));

                            boolean isId = "ID_CARD".equalsIgnoreCase(doc.getDocumentType());
                            String typeDisplay = isId ? "Government ID Proof" : "Qualifications Certificate";
                            Icon docIcon = isId ? PawIcons.idCard(20, PawTheme.INDIGO) : PawIcons.document(20, PawTheme.SUNNY);
                            if (isId) {
                                card.setBackgroundTint(PawTheme.INDIGO_LIGHT);
                                card.setBorderTint(PawTheme.INDIGO);
                            } else {
                                card.setBackgroundTint(PawTheme.SUNNY_LIGHT);
                                card.setBorderTint(PawTheme.SUNNY);
                            }

                            JPanel infoCol = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
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

                            JButton openBtn = new PawComponents.PawButton("Open PDF", PawIcons.document(13, isId ? PawTheme.INDIGO_TEXT : PawTheme.SUNNY_TEXT), isId ? PawComponents.ButtonStyle.INDIGO : PawComponents.ButtonStyle.SUNNY);
                            openBtn.addActionListener(e -> {
                                try {
                                    String url = "http://localhost:8080/api/admin/documents/download/" + doc.getId() +
                                            "?adminId=" + Session.getInstance().getUserId();
                                    Desktop.getDesktop().browse(URI.create(url));
                                } catch (Exception ex) {
                                    Dialogs.showError(ApproveSitterDialog.this, "Could not open document viewer: " + ex.getMessage());
                                }
                            });
                            card.add(openBtn, BorderLayout.EAST);

                            docsContainer.add(card);
                            docsContainer.add(Box.createVerticalStrut(8));
                        }
                    }
                } catch (Exception ex) {
                    JLabel errLabel = new JLabel("Failed to load documents: " + ex.getMessage());
                    errLabel.setFont(PawTheme.FONT_SMALL);
                    errLabel.setForeground(PawTheme.DANGER);
                    docsContainer.add(errLabel);
                }
                docsContainer.revalidate();
                docsContainer.repaint();
            }
        };
        worker.execute();
    }

    private void handleApproveWithRate() {
        String rateText = rateField.getText().trim();
        BigDecimal hourlyRate;
        try {
            hourlyRate = new BigDecimal(rateText);
            if (hourlyRate.compareTo(BigDecimal.ZERO) <= 0) {
                Dialogs.showWarning(this, "Hourly rate must be a positive number greater than 0.");
                return;
            }
        } catch (Exception ex) {
            Dialogs.showWarning(this, "Please enter a valid numeric hourly rate (e.g. 250.00).");
            return;
        }

        approveButton.setEnabled(false);
        approveButton.setText("Approving...");

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                ApiClient.getInstance().patch(
                        "/api/admin/sitters/" + sitter.getUserId() + "/approve",
                        Map.of("hourlyRate", hourlyRate),
                        Void.class
                );
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    dispose();
                    Dialogs.showSuccess(getParent(), "Sitter '" + sitter.getName() + "' has been approved with hourly rate ₹" + hourlyRate + "/hr!");
                    if (onSuccess != null) {
                        onSuccess.run();
                    }
                } catch (Exception ex) {
                    approveButton.setEnabled(true);
                    approveButton.setText("Approve Sitter & Save Rate");
                    Dialogs.showError(ApproveSitterDialog.this, "Failed to approve sitter: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }
}
