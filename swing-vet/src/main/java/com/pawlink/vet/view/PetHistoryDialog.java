package com.pawlink.vet.view;

import com.pawlink.vet.api.ApiClient;
import com.pawlink.vet.base.BaseTableModel;
import com.pawlink.vet.model.PetDTO;
import com.pawlink.vet.model.PetMedicalTimelineDTO;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.List;

/**
 * Modal Dialog displaying a pet's complete medical and vaccination history in Swing.
 * Ensures a full Swing client experience for reviewing clinical timelines and vaccinations.
 */
public class PetHistoryDialog extends JDialog {

    private final PetDTO pet;
    private final TimelineTableModel tableModel;
    private final JTable historyTable;
    private final JLabel headerTitle;
    private final JLabel subtitleLabel;

    public PetHistoryDialog(Frame parent, PetDTO pet) {
        super(parent, "Medical & Vaccination History - " + (pet != null ? pet.getName() : "Pet"), true);
        this.pet = pet;

        setSize(850, 560);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        JPanel root = new JPanel(new BorderLayout(0, 14));
        root.setBackground(new Color(248, 250, 252));
        root.setBorder(new EmptyBorder(18, 20, 18, 20));

        // 1. Header Banner
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(true);
        header.setBackground(Color.WHITE);
        header.setBorder(new CompoundBorder(
                new LineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(12, 16, 12, 16)
        ));

        headerTitle = new JLabel("🐾 " + (pet != null ? pet.getName() : "Pet") + " - Complete Clinical History");
        headerTitle.setFont(new Font("Segoe UI", Font.BOLD, 17));
        headerTitle.setForeground(new Color(30, 41, 59));
        header.add(headerTitle, BorderLayout.NORTH);

        subtitleLabel = new JLabel(pet != null
                ? "Species: " + pet.getSpecies() + "  |  Pet ID: #" + pet.getId() + "  |  Date of Birth: " + pet.getDob()
                : "");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitleLabel.setForeground(new Color(100, 116, 139));
        header.add(subtitleLabel, BorderLayout.SOUTH);

        root.add(header, BorderLayout.NORTH);

        // 2. Table
        tableModel = new TimelineTableModel();
        historyTable = new JTable(tableModel);
        setupTableAppearance();

        JScrollPane scrollPane = new JScrollPane(historyTable);
        scrollPane.setBorder(new LineBorder(new Color(226, 232, 240), 1, true));
        scrollPane.getViewport().setBackground(Color.WHITE);
        root.add(scrollPane, BorderLayout.CENTER);

        // 3. Close Button
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        bottom.setOpaque(false);
        JButton closeButton = new JButton("Close");
        closeButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        closeButton.addActionListener(e -> dispose());
        bottom.add(closeButton);
        root.add(bottom, BorderLayout.SOUTH);

        add(root);

        loadTimeline();
    }

    private void setupTableAppearance() {
        historyTable.setRowHeight(32);
        historyTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        historyTable.setGridColor(new Color(241, 245, 249));
        historyTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        historyTable.getTableHeader().setBackground(new Color(248, 250, 252));
        historyTable.getTableHeader().setForeground(new Color(71, 85, 105));

        historyTable.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                label.setHorizontalAlignment(SwingConstants.CENTER);
                String type = String.valueOf(value);
                if ("MEDICAL".equalsIgnoreCase(type)) {
                    label.setText("🩺 MEDICAL");
                    label.setForeground(new Color(37, 99, 235));
                    label.setFont(new Font("Segoe UI", Font.BOLD, 12));
                } else if ("VACCINATION".equalsIgnoreCase(type)) {
                    label.setText("💉 VACCINE");
                    label.setForeground(new Color(16, 185, 129));
                    label.setFont(new Font("Segoe UI", Font.BOLD, 12));
                }
                return label;
            }
        });
    }

    private void loadTimeline() {
        if (pet == null || pet.getId() == null) return;

        SwingWorker<List<PetMedicalTimelineDTO>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<PetMedicalTimelineDTO> doInBackground() {
                return ApiClient.getInstance().getList("/api/pets/" + pet.getId() + "/records", PetMedicalTimelineDTO.class);
            }

            @Override
            protected void done() {
                try {
                    List<PetMedicalTimelineDTO> items = get();
                    tableModel.setItems(items);
                } catch (Exception ignored) {
                }
            }
        };
        worker.execute();
    }

    private static class TimelineTableModel extends BaseTableModel<PetMedicalTimelineDTO> {
        public TimelineTableModel() {
            super(new String[]{"Date", "Type", "Diagnosis / Vaccine", "Prescription / Next Due", "Veterinarian"});
        }

        @Override
        public Object getValueAt(PetMedicalTimelineDTO item, int columnIndex) {
            return switch (columnIndex) {
                case 0 -> item.getDate();
                case 1 -> item.getType();
                case 2 -> item.getTitle();
                case 3 -> item.getDetails();
                case 4 -> item.getVetName() != null ? item.getVetName() : "Scheduled / Admin";
                default -> "";
            };
        }
    }
}
