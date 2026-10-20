package com.pawlink.vet.view;

import com.pawlink.vet.api.ApiClient;
import com.pawlink.vet.api.Session;
import com.pawlink.vet.base.Dialogs;
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
 * Modal dialog for inspecting submitted verification documents and setting
 * an approved hourly rate (₹/hr) when an administrator approves a Pet Sitter.
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

        setSize(560, 480);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        JPanel root = new JPanel(new BorderLayout(0, 14));
        root.setBackground(new Color(248, 250, 252));
        root.setBorder(new EmptyBorder(16, 20, 16, 20));

        // 1. Header Banner
        JPanel header = new JPanel(new BorderLayout(10, 0));
        header.setOpaque(true);
        header.setBackground(Color.WHITE);
        header.setBorder(new CompoundBorder(
                new LineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(12, 16, 12, 16)
        ));

        JLabel iconLabel = new JLabel("🐾");
        iconLabel.setFont(new Font("Segoe UI", Font.PLAIN, 32));
        header.add(iconLabel, BorderLayout.WEST);

        JPanel headerText = new JPanel(new GridLayout(2, 1, 0, 3));
        headerText.setOpaque(false);
        JLabel nameLabel = new JLabel("Approve Sitter: " + sitter.getName());
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        nameLabel.setForeground(new Color(30, 41, 59));
        headerText.add(nameLabel);

        JLabel subLabel = new JLabel("Email: " + sitter.getEmail() + "  |  Phone: " + (sitter.getPhone() != null ? sitter.getPhone() : "-"));
        subLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subLabel.setForeground(new Color(100, 116, 139));
        headerText.add(subLabel);
        header.add(headerText, BorderLayout.CENTER);

        root.add(header, BorderLayout.NORTH);

        // 2. Center Content: Uploaded Documents list + Rate input
        JPanel centerPanel = new JPanel(new BorderLayout(0, 12));
        centerPanel.setOpaque(false);

        // 2a. Documents section
        JPanel docsSection = new JPanel(new BorderLayout(0, 6));
        docsSection.setOpaque(false);

        JLabel docsTitle = new JLabel("Submitted Verification Documents (Review proofs before approving):");
        docsTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        docsTitle.setForeground(new Color(51, 65, 85));
        docsSection.add(docsTitle, BorderLayout.NORTH);

        docsContainer = new JPanel();
        docsContainer.setLayout(new BoxLayout(docsContainer, BoxLayout.Y_AXIS));
        docsContainer.setBackground(Color.WHITE);
        docsContainer.setBorder(new EmptyBorder(8, 8, 8, 8));

        loadingLabel = new JLabel("Fetching submitted certificates & ID documents...");
        loadingLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        loadingLabel.setForeground(new Color(100, 116, 139));
        docsContainer.add(loadingLabel);

        JScrollPane docsScroll = new JScrollPane(docsContainer);
        docsScroll.setPreferredSize(new Dimension(500, 160));
        docsScroll.setBorder(new LineBorder(new Color(226, 232, 240), 1, true));
        docsSection.add(docsScroll, BorderLayout.CENTER);

        centerPanel.add(docsSection, BorderLayout.CENTER);

        // 2b. Hourly Rate Form Card
        JPanel rateCard = new JPanel(new BorderLayout(0, 6));
        rateCard.setOpaque(true);
        rateCard.setBackground(Color.WHITE);
        rateCard.setBorder(new CompoundBorder(
                new LineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(12, 14, 12, 14)
        ));

        JPanel rateInputRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        rateInputRow.setOpaque(false);

        JLabel rateLabel = new JLabel("Set Approved Hourly Rate (₹/hr) *:");
        rateLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        rateLabel.setForeground(new Color(30, 41, 59));
        rateInputRow.add(rateLabel);

        String initialRate = sitter.getHourlyRate() != null ? sitter.getHourlyRate().toString() : "250.00";
        rateField = new JTextField(initialRate, 10);
        rateField.setFont(new Font("Segoe UI", Font.BOLD, 14));
        rateInputRow.add(rateField);

        rateCard.add(rateInputRow, BorderLayout.NORTH);

        JLabel hintLabel = new JLabel("💡 Evaluate their experience & qualification certificates above, then set their final booking rate in ₹ INR.");
        hintLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        hintLabel.setForeground(new Color(100, 116, 139));
        rateCard.add(hintLabel, BorderLayout.SOUTH);

        centerPanel.add(rateCard, BorderLayout.SOUTH);

        root.add(centerPanel, BorderLayout.CENTER);

        // 3. Bottom Actions
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        footer.setOpaque(false);

        JButton cancelButton = new JButton("Cancel");
        cancelButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cancelButton.addActionListener(e -> dispose());
        footer.add(cancelButton);

        approveButton = new JButton("✅ Approve Sitter & Save Rate");
        approveButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        approveButton.setBackground(new Color(16, 185, 129)); // Green
        approveButton.setForeground(Color.WHITE);
        approveButton.setFocusable(false);
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
                        noneLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
                        noneLabel.setForeground(new Color(156, 163, 175));
                        docsContainer.add(noneLabel);
                    } else {
                        for (UserDocumentDTO doc : docs) {
                            JPanel card = new JPanel(new BorderLayout(10, 0));
                            card.setBackground(new Color(248, 250, 252));
                            card.setBorder(BorderFactory.createCompoundBorder(
                                    BorderFactory.createLineBorder(new Color(226, 232, 240)),
                                    BorderFactory.createEmptyBorder(6, 10, 6, 10)
                            ));

                            String typeDisplay = "ID_CARD".equalsIgnoreCase(doc.getDocumentType()) ? "🪪 ID Card Proof" : "📜 Qualifications Certificate";
                            JLabel info = new JLabel("<html><b>" + typeDisplay + "</b>: " + doc.getFileName() +
                                    " <span style='color:#6b7280;'>(" + String.format("%.2f", doc.getFileSize() / (1024.0 * 1024.0)) + " MB)</span></html>");
                            info.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                            card.add(info, BorderLayout.CENTER);

                            JButton openBtn = new JButton("🌐 Open PDF");
                            openBtn.setFont(new Font("Segoe UI", Font.BOLD, 11));
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
                            docsContainer.add(Box.createVerticalStrut(6));
                        }
                    }
                } catch (Exception ex) {
                    JLabel errLabel = new JLabel("Failed to load documents: " + ex.getMessage());
                    errLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                    errLabel.setForeground(new Color(239, 68, 68));
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
                    Dialogs.showSuccess(getParent(), "✓ Sitter '" + sitter.getName() + "' has been approved with hourly rate ₹" + hourlyRate + "/hr!");
                    if (onSuccess != null) {
                        onSuccess.run();
                    }
                } catch (Exception ex) {
                    approveButton.setEnabled(true);
                    approveButton.setText("✅ Approve Sitter & Save Rate");
                    Dialogs.showError(ApproveSitterDialog.this, "Failed to approve sitter: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }
}

