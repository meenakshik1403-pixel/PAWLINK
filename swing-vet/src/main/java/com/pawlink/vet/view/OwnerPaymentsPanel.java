package com.pawlink.vet.view;

import com.pawlink.vet.api.ApiClient;
import com.pawlink.vet.api.Session;
import com.pawlink.vet.base.BasePanel;
import com.pawlink.vet.base.BaseTableModel;
import com.pawlink.vet.base.Dialogs;
import com.pawlink.vet.model.PaymentResponseDTO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Pet Owner Panel to review payment transactions, methods (UPI/CARD/WALLET), and escrow status.
 * Extends BasePanel and uses BaseTableModel<PaymentResponseDTO>.
 */
public class OwnerPaymentsPanel extends BasePanel {

    private final BaseTableModel<PaymentResponseDTO> tableModel;
    private final JTable table;
    private final JLabel totalSpentLabel;
    private final JLabel escrowHeldLabel;
    private final JLabel countLabel;
    private List<PaymentResponseDTO> paymentList = new ArrayList<>();

    public OwnerPaymentsPanel() {
        super("💳 Payment & Escrow History", "Review past care transactions, verify payment channels, and monitor escrow holds.");

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

        JButton refreshBtn = new JButton("🔄 Refresh Payments");
        refreshBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        refreshBtn.addActionListener(e -> loadPayments());
        headerAccessory.add(refreshBtn);

        setHeaderAccessory(headerAccessory);

        // Center: Summary KPI cards + Table
        JPanel mainPanel = new JPanel(new BorderLayout(0, 14));
        mainPanel.setOpaque(false);

        // 3 KPI Cards
        JPanel kpiPanel = new JPanel(new GridLayout(1, 3, 16, 0));
        kpiPanel.setOpaque(false);

        kpiPanel.add(createKpiCard("Total Payments", countLabel, new Color(79, 70, 229)));
        kpiPanel.add(createKpiCard("Total Volume", totalSpentLabel, new Color(16, 185, 129)));
        kpiPanel.add(createKpiCard("Secured in Escrow", escrowHeldLabel, new Color(217, 119, 6)));

        mainPanel.add(kpiPanel, BorderLayout.NORTH);

        // Table
        table.setRowHeight(36);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.setShowGrid(true);
        table.setGridColor(new Color(241, 245, 249));

        // Method Renderer (col 6)
        table.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int col) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, col);
                l.setHorizontalAlignment(SwingConstants.CENTER);
                String val = String.valueOf(value);
                l.setFont(l.getFont().deriveFont(Font.BOLD));
                if ("UPI".equalsIgnoreCase(val)) {
                    l.setForeground(new Color(99, 102, 241)); // Indigo
                } else if ("CARD".equalsIgnoreCase(val)) {
                    l.setForeground(new Color(14, 165, 233)); // Sky
                } else {
                    l.setForeground(new Color(168, 85, 247)); // Purple
                }
                return l;
            }
        });

        // Status Renderer (col 7)
        table.getColumnModel().getColumn(7).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int col) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, col);
                l.setHorizontalAlignment(SwingConstants.CENTER);
                String val = String.valueOf(value);
                if ("HELD".equalsIgnoreCase(val)) {
                    l.setText("🔒 Escrow HELD");
                    l.setForeground(new Color(217, 119, 6)); // Amber
                } else if ("RELEASED".equalsIgnoreCase(val)) {
                    l.setText("✓ Escrow Released");
                    l.setForeground(new Color(16, 185, 129)); // Green
                } else if ("REFUNDED".equalsIgnoreCase(val)) {
                    l.setText("↩ Refunded");
                    l.setForeground(new Color(59, 130, 246)); // Blue
                } else {
                    l.setText("✗ " + val);
                    l.setForeground(new Color(239, 68, 68));
                }
                return l;
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        setContent(mainPanel);
    }

    private JPanel createKpiCard(String title, JLabel valueLabel, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout(6, 6));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, accentColor),
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(226, 232, 240)),
                        new EmptyBorder(12, 16, 12, 16)
                )
        ));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        titleLbl.setForeground(new Color(100, 116, 139));
        card.add(titleLbl, BorderLayout.NORTH);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        valueLabel.setForeground(accentColor);
        card.add(valueLabel, BorderLayout.CENTER);

        return card;
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
                            if ("HELD".equalsIgnoreCase(p.getStatus())) {
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

