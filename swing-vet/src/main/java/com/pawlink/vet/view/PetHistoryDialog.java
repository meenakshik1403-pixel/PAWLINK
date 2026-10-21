package com.pawlink.vet.view;

import com.pawlink.vet.api.ApiClient;
import com.pawlink.vet.base.BaseTableModel;
import com.pawlink.vet.base.PawComponents;
import com.pawlink.vet.base.PawIcons;
import com.pawlink.vet.base.PawTheme;
import com.pawlink.vet.model.PetDTO;
import com.pawlink.vet.model.PetMedicalTimelineDTO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.List;

/**
 * Modal Dialog displaying a pet's complete medical and vaccination history in Swing.
 * Modernized with pure Java2D vector icons, custom status pills, and high-contrast typography.
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

        setSize(900, 600);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        JPanel root = new JPanel(new BorderLayout(0, 16));
        root.setBackground(PawTheme.CANVAS);
        root.setBorder(new EmptyBorder(18, 22, 18, 22));

        // 1. Header Banner
        JPanel header = new JPanel(new BorderLayout(14, 0));
        header.setBackground(PawTheme.SIDEBAR_BG);
        header.setBorder(new EmptyBorder(18, 24, 18, 24));

        JLabel iconLabel = new JLabel(PawIcons.medicalRecord(26, PawTheme.LILAC));
        header.add(iconLabel, BorderLayout.WEST);

        JPanel titleBlock = new JPanel(new GridLayout(2, 1, 0, 4));
        titleBlock.setOpaque(false);

        headerTitle = new JLabel((pet != null ? pet.getName() : "Pet") + " - Complete Clinical History");
        headerTitle.setFont(PawTheme.FONT_TITLE);
        headerTitle.setForeground(Color.WHITE);
        titleBlock.add(headerTitle);

        subtitleLabel = new JLabel(pet != null
                ? "Species: " + pet.getSpecies() + "  |  Pet ID: #" + pet.getId() + "  |  Date of Birth: " + pet.getDob()
                : "");
        subtitleLabel.setFont(PawTheme.FONT_SMALL);
        subtitleLabel.setForeground(PawTheme.TEXT_SUBTLE);
        titleBlock.add(subtitleLabel);

        header.add(titleBlock, BorderLayout.CENTER);
        root.add(header, BorderLayout.NORTH);

        // 2. Table inside PawCard
        tableModel = new TimelineTableModel();
        historyTable = new JTable(tableModel);
        PawTheme.styleTable(historyTable);
        setupTableAppearance();

        PawComponents.PawCard tableCard = new PawComponents.PawCard();
        tableCard.setLayout(new BorderLayout(0, 10));

        JScrollPane scrollPane = new JScrollPane(historyTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(PawTheme.BORDER_LIGHT));
        scrollPane.getViewport().setBackground(PawTheme.SURFACE);
        tableCard.add(scrollPane, BorderLayout.CENTER);
        root.add(tableCard, BorderLayout.CENTER);

        // 3. Close Button
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 4));
        bottom.setOpaque(false);
        PawComponents.PawButton closeButton = new PawComponents.PawButton("Close", PawComponents.ButtonStyle.SECONDARY);
        closeButton.addActionListener(e -> dispose());
        bottom.add(closeButton);
        root.add(bottom, BorderLayout.SOUTH);

        add(root);

        loadTimeline();
    }

    private void setupTableAppearance() {
        historyTable.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                String type = String.valueOf(value);
                PawComponents.StatusPill pill;
                if ("MEDICAL".equalsIgnoreCase(type)) {
                    pill = new PawComponents.StatusPill("CONSULTATION", PawTheme.LILAC_LIGHT, PawTheme.LILAC_TEXT, PawTheme.LILAC);
                } else if ("VACCINATION".equalsIgnoreCase(type)) {
                    pill = new PawComponents.StatusPill("VACCINE", PawTheme.MINT_LIGHT, PawTheme.MINT_TEXT, PawTheme.MINT);
                } else {
                    pill = new PawComponents.StatusPill(type, PawTheme.SURFACE_MUTED, PawTheme.TEXT_MUTED, PawTheme.TEXT_MUTED);
                }

                JPanel holder = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 4));
                holder.setOpaque(isSelected);
                if (isSelected) {
                    holder.setBackground(table.getSelectionBackground());
                } else {
                    holder.setBackground(row % 2 == 0 ? PawTheme.SURFACE : new Color(250, 250, 252));
                }
                holder.add(pill);
                return holder;
            }
        });

        historyTable.getColumnModel().getColumn(0).setPreferredWidth(100);
        historyTable.getColumnModel().getColumn(1).setPreferredWidth(125);
        historyTable.getColumnModel().getColumn(2).setPreferredWidth(210);
        historyTable.getColumnModel().getColumn(3).setPreferredWidth(240);
        historyTable.getColumnModel().getColumn(4).setPreferredWidth(150);
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
