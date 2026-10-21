package com.pawlink.vet.view;

import com.pawlink.vet.api.ApiClient;
import com.pawlink.vet.api.Session;
import com.pawlink.vet.base.BasePanel;
import com.pawlink.vet.base.BaseTableModel;
import com.pawlink.vet.base.PawComponents;
import com.pawlink.vet.base.PawIcons;
import com.pawlink.vet.base.PawTheme;
import com.pawlink.vet.model.PaymentResponseDTO;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Modernized Pet Owner Panel to review payment transactions, methods (UPI/CARD/WALLET), and escrow status.
 * Features cute multi-coloured pastel KPI cards, responsive margins, and vector pill badges.
 */
public class OwnerPaymentsPanel extends BasePanel {

    private final BaseTableModel<PaymentResponseDTO> tableModel;
    private final JTable table;
    private final JLabel totalSpentLabel;
    private final JLabel escrowHeldLabel;
    private final JLabel countLabel;
    private List<PaymentResponseDTO> paymentList = new ArrayList<>();

    public OwnerPaymentsPanel() {
        super("Payment & Escrow History", "Review past care transactions, verify payment channels, and monitor escrow holds.");
        setHeaderIcon(PawIcons.wallet(24, PawTheme.INDIGO));
        setHeaderColor(PawTheme.INDIGO_TEXT);

        String[] cols = {"Payment ID", "Booking #", "Date & Time", "Pet", "Sitter", "Amount", "Method", "Status"};
        this.tableModel = new BaseTableModel<>(cols) {
            @Override
            public Object getValueAt(PaymentResponseDTO p, int col) {
                return switch (col) {
                    case 0 -> "TXN-" + p.getId();
                    case 1 -> "#" + p.getBookingId();
                    case 2 -> p.getFormattedDate();
                    case 3 -> p.getPetName() != null ? p.getPetName() : "-";
                    case 4 -> p.getSitterName() != null ? p.getSitterName() : "-";
                    case 5 -> "₹" + (p.getAmount() != null ? p.getAmount() : "0.00");
                    case 6 -> p.getMethod() != null ? p.getMethod() : "CARD";
                    case 7 -> p.getStatus() != null ? p.getStatus() : "HELD";
                    default -> "";
                };
            }
        };

        this.table = new JTable(tableModel);
        this.totalSpentLabel = new JLabel("₹0.00");
        this.escrowHeldLabel = new JLabel("₹0.00");
        this.countLabel = new JLabel("0");

        setupUI();
        loadPayments();
    }

    private void setupUI() {
        // Header Accessory: Refresh Button
        JPanel headerAccessory = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        headerAccessory.setOpaque(false);

        JButton refreshBtn = new PawComponents.PawButton("Refresh", PawIcons.refresh(13, PawTheme.TEXT_BODY), PawComponents.ButtonStyle.SECONDARY);
        refreshBtn.addActionListener(e -> loadPayments());
        headerAccessory.add(refreshBtn);

        setHeaderAccessory(headerAccessory);

        // Center: Summary KPI cards + Table
        JPanel mainPanel = new JPanel(new BorderLayout(0, 16));
        mainPanel.setOpaque(false);

        // 3 Modern Multi-Coloured Cute KPI Cards
        JPanel kpiPanel = new JPanel(new GridLayout(1, 3, 18, 0));
        kpiPanel.setOpaque(false);

        kpiPanel.add(createKpiCard("Total Payments", countLabel, PawIcons.document(24, PawTheme.INDIGO), PawTheme.INDIGO, PawTheme.INDIGO_LIGHT));
        kpiPanel.add(createKpiCard("Total Volume", totalSpentLabel, PawIcons.wallet(24, PawTheme.MINT), PawTheme.MINT, PawTheme.MINT_LIGHT));
        kpiPanel.add(createKpiCard("Secured in Escrow", escrowHeldLabel, PawIcons.shield(24, PawTheme.SUNNY), PawTheme.SUNNY, PawTheme.SUNNY_LIGHT));

        mainPanel.add(kpiPanel, BorderLayout.NORTH);

        // Table
        PawTheme.styleTable(table);

        // Method Renderer (col 6) & Status Renderer (col 7)
        table.getColumnModel().getColumn(6).setCellRenderer(new PawComponents.StatusTableCellRenderer());
        table.getColumnModel().getColumn(7).setCellRenderer(new PawComponents.StatusTableCellRenderer());

        table.getColumnModel().getColumn(0).setPreferredWidth(110);
        table.getColumnModel().getColumn(1).setPreferredWidth(90);
        table.getColumnModel().getColumn(2).setPreferredWidth(160);
        table.getColumnModel().getColumn(3).setPreferredWidth(130);
        table.getColumnModel().getColumn(4).setPreferredWidth(140);
        table.getColumnModel().getColumn(5).setPreferredWidth(110);
        table.getColumnModel().getColumn(6).setPreferredWidth(110);
        table.getColumnModel().getColumn(7).setPreferredWidth(120);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(new CompoundBorder(
                new LineBorder(PawTheme.BORDER_LIGHT, 1, true),
                new EmptyBorder(0, 0, 0, 0)
        ));
        scrollPane.getViewport().setBackground(PawTheme.SURFACE);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        setContent(mainPanel);
    }

    private JPanel createKpiCard(String title, JLabel valueLabel, Icon icon, Color accentColor, Color bgTint) {
        PawComponents.PawCard card = new PawComponents.PawCard(16);
        card.setCardBackground(bgTint);
        card.setCardBorderColor(new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), 60));
        card.setLayout(new BorderLayout(10, 8));

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

        valueLabel.setFont(PawTheme.FONT_TITLE);
        valueLabel.setForeground(accentColor);
        card.add(valueLabel, BorderLayout.CENTER);

        return card;
    }

    @Override
    public void refresh() {
        loadPayments();
    }

    public void loadPayments() {
        Long userId = Session.getInstance().getUserId();
        if (userId == null) return;

        SwingWorker<List<PaymentResponseDTO>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<PaymentResponseDTO> doInBackground() {
                return ApiClient.getInstance().getList("/api/payments?ownerId=" + userId, PaymentResponseDTO.class);
            }

            @Override
            protected void done() {
                try {
                    paymentList = get();
                    tableModel.setItems(paymentList);

                    BigDecimal totalSpent = BigDecimal.ZERO;
                    BigDecimal totalHeld = BigDecimal.ZERO;

                    for (PaymentResponseDTO p : paymentList) {
                        if (p.getAmount() != null) {
                            totalSpent = totalSpent.add(p.getAmount());
                            if ("HELD".equalsIgnoreCase(p.getStatus()) || "ESCROW_HELD".equalsIgnoreCase(p.getStatus())) {
                                totalHeld = totalHeld.add(p.getAmount());
                            }
                        }
                    }

                    countLabel.setText(paymentList.size() + " txn(s)");
                    totalSpentLabel.setText("₹" + totalSpent);
                    escrowHeldLabel.setText("₹" + totalHeld);
                } catch (Exception ex) {
                    showError("Failed to load payment history: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }
}
