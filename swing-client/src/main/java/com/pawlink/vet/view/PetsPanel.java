package com.pawlink.vet.view;

import com.pawlink.vet.api.ApiClient;
import com.pawlink.vet.base.BasePanel;
import com.pawlink.vet.base.BaseTableModel;
import com.pawlink.vet.base.PawComponents;
import com.pawlink.vet.base.PawIcons;
import com.pawlink.vet.base.PawTheme;
import com.pawlink.vet.model.PetDTO;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Veterinary Patients / Pets Management Panel.
 * Modernized with pure Java2D vector icons, crisp typography, and responsive detail cards.
 */
public class PetsPanel extends BasePanel {

    private final JTextField searchField;
    private final PawComponents.PawButton searchButton;
    private final PawComponents.PawButton refreshButton;

    private final PetTableModel tableModel;
    private final JTable petTable;

    // Bottom Care Guidelines Detail Card Components
    private final JLabel petHeaderLabel;
    private final JLabel petIconHolder;
    private final PawComponents.StatusPill speciesPill;
    private final JTextArea guidelinesTextArea;
    private final JLabel metadataLabel;

    public PetsPanel() {
        super("Patients & Pet Directory", "Search patient records and review polymorphic species care guidelines");

        setHeaderIcon(PawIcons.paw(26, PawTheme.CORAL));
        setHeaderColor(PawTheme.CORAL_TEXT);

        // 1. Initialize Table Model & Table
        tableModel = new PetTableModel();
        petTable = new JTable(tableModel);
        PawTheme.styleTable(petTable);
        setupTableRenderers();

        // 2. Top Search Controls
        searchField = new JTextField(22);
        PawTheme.styleField(searchField);
        searchField.setToolTipText("Search by pet name or owner ID...");

        searchButton = new PawComponents.PawButton("Search", PawIcons.search(14, Color.WHITE), PawComponents.ButtonStyle.CORAL);
        refreshButton = new PawComponents.PawButton("Show All", PawIcons.refresh(13, PawTheme.TEXT_BODY), PawComponents.ButtonStyle.SECONDARY);

        JPanel searchBarPanel = createSearchBar();

        // 3. Bottom Detail Card
        petIconHolder = new JLabel(PawIcons.paw(22, PawTheme.CORAL));
        petHeaderLabel = new JLabel("Select a patient to inspect clinical care guidelines");
        petHeaderLabel.setFont(PawTheme.FONT_SECTION);
        petHeaderLabel.setForeground(PawTheme.TEXT_TITLE);

        speciesPill = new PawComponents.StatusPill("", PawTheme.SURFACE_MUTED, PawTheme.TEXT_MUTED, PawTheme.TEXT_MUTED);
        speciesPill.setVisible(false);

        metadataLabel = new JLabel(" ");
        metadataLabel.setFont(PawTheme.FONT_BODY);
        metadataLabel.setForeground(PawTheme.TEXT_MUTED);

        guidelinesTextArea = new JTextArea(4, 40);
        guidelinesTextArea.setEditable(false);
        guidelinesTextArea.setLineWrap(true);
        guidelinesTextArea.setWrapStyleWord(true);
        guidelinesTextArea.setFont(PawTheme.FONT_BODY);
        guidelinesTextArea.setForeground(PawTheme.TEXT_TITLE);
        guidelinesTextArea.setBackground(PawTheme.SURFACE);
        guidelinesTextArea.setBorder(new CompoundBorder(
                new LineBorder(PawTheme.BORDER_LIGHT, 1, true),
                new EmptyBorder(12, 14, 12, 14)
        ));

        JPanel detailCard = createDetailCard();

        // 4. Assemble Layout into Content Panel inside PawCard
        PawComponents.PawCard tableCard = new PawComponents.PawCard();
        tableCard.setLayout(new BorderLayout(0, 10));

        JScrollPane scrollPane = new JScrollPane(petTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(PawTheme.BORDER_LIGHT));
        scrollPane.getViewport().setBackground(PawTheme.SURFACE);
        tableCard.add(scrollPane, BorderLayout.CENTER);

        JPanel mainContent = new JPanel(new BorderLayout(0, 14));
        mainContent.setOpaque(false);
        mainContent.add(searchBarPanel, BorderLayout.NORTH);
        mainContent.add(tableCard, BorderLayout.CENTER);
        mainContent.add(detailCard, BorderLayout.SOUTH);

        setContent(mainContent);

        // 5. Setup Action Listeners
        searchButton.addActionListener(e -> performSearch());
        searchField.addActionListener(e -> performSearch());
        refreshButton.addActionListener(e -> {
            searchField.setText("");
            loadPets(null);
        });

        petTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateDetailCard();
            }
        });

        // 6. Initial Data Load
        loadPets(null);
    }

    private JPanel createSearchBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        bar.setOpaque(true);
        bar.setBackground(PawTheme.SURFACE);
        bar.setBorder(new CompoundBorder(
                new LineBorder(PawTheme.BORDER_LIGHT, 1, true),
                new EmptyBorder(6, 14, 6, 14)
        ));

        JLabel iconLabel = new JLabel("Patient Search:");
        iconLabel.setFont(PawTheme.FONT_BODY_BOLD);
        iconLabel.setForeground(PawTheme.TEXT_BODY);
        bar.add(iconLabel);

        bar.add(searchField);
        bar.add(searchButton);
        bar.add(refreshButton);

        return bar;
    }

    private JPanel createDetailCard() {
        PawComponents.PawCard card = new PawComponents.PawCard(14);
        card.setBackgroundTint(PawTheme.PEACH_LIGHT);
        card.setBorderTint(PawTheme.PEACH);
        card.setLayout(new BorderLayout(14, 12));

        JPanel topInfo = new JPanel(new BorderLayout(8, 6));
        topInfo.setOpaque(false);

        JPanel leftHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftHeader.setOpaque(false);
        leftHeader.add(petIconHolder);
        leftHeader.add(petHeaderLabel);
        leftHeader.add(speciesPill);

        topInfo.add(leftHeader, BorderLayout.NORTH);
        topInfo.add(metadataLabel, BorderLayout.SOUTH);

        card.add(topInfo, BorderLayout.NORTH);
        card.add(guidelinesTextArea, BorderLayout.CENTER);

        return card;
    }

    private void setupTableRenderers() {
        // Species Column (Index 2) Pill Renderer using PawComponents.StatusPill.forSpecies
        petTable.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                String sp = value != null ? value.toString() : "UNKNOWN";
                PawComponents.StatusPill pill = PawComponents.StatusPill.forSpecies(sp);

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

        petTable.getColumnModel().getColumn(0).setPreferredWidth(80);
        petTable.getColumnModel().getColumn(1).setPreferredWidth(160);
        petTable.getColumnModel().getColumn(2).setPreferredWidth(130);
        petTable.getColumnModel().getColumn(3).setPreferredWidth(130);
        petTable.getColumnModel().getColumn(4).setPreferredWidth(100);
    }

    @Override
    public void refresh() {
        loadPets(null);
    }

    private void performSearch() {
        String query = searchField.getText().trim();
        loadPets(query.isEmpty() ? null : query);
    }

    private void loadPets(String query) {
        searchButton.setEnabled(false);
        searchButton.setText("Searching...");

        SwingWorker<List<PetDTO>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<PetDTO> doInBackground() {
                String endpoint;
                if (query != null && !query.trim().isEmpty()) {
                    endpoint = "/api/pets?q=" + URLEncoder.encode(query.trim(), StandardCharsets.UTF_8);
                } else {
                    endpoint = "/api/pets";
                }
                return ApiClient.getInstance().getList(endpoint, PetDTO.class);
            }

            @Override
            protected void done() {
                searchButton.setEnabled(true);
                searchButton.setText("Search");
                try {
                    List<PetDTO> pets = get();
                    tableModel.setItems(pets);
                    if (pets != null && !pets.isEmpty()) {
                        petTable.setRowSelectionInterval(0, 0);
                        updateDetailCard();
                    } else {
                        clearDetailCard("No matching patient records found.");
                    }
                } catch (Exception ex) {
                    showError("Failed to fetch pet records: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void updateDetailCard() {
        int selectedRow = petTable.getSelectedRow();
        if (selectedRow < 0) {
            clearDetailCard("Select a patient to inspect clinical care guidelines");
            return;
        }

        PetDTO pet = tableModel.getItem(selectedRow);
        if (pet == null) return;

        petIconHolder.setIcon(PawIcons.paw(22, PawTheme.PRIMARY));
        petHeaderLabel.setText(pet.getName());

        speciesPill.setVisible(true);

        metadataLabel.setText("Patient ID: #" + pet.getId() + "  |  Owner ID: #" + pet.getOwnerId() + "  |  DOB: " + (pet.getDob() != null ? pet.getDob() : "N/A"));

        String guidelines = pet.getCareGuidelines();
        if (guidelines != null && !guidelines.isBlank()) {
            guidelinesTextArea.setText("Species Clinical Care Guidelines:\n" + guidelines);
        } else {
            guidelinesTextArea.setText("No specific care guidelines registered for this species.");
        }
    }

    private void clearDetailCard(String prompt) {
        petHeaderLabel.setText(prompt);
        speciesPill.setVisible(false);
        metadataLabel.setText(" ");
        guidelinesTextArea.setText("");
    }

    /**
     * Parameterized TableModel for Pet records.
     */
    private static class PetTableModel extends BaseTableModel<PetDTO> {

        public PetTableModel() {
            super(new String[]{"Pet ID", "Pet Name", "Species", "Date of Birth", "Owner ID"});
        }

        @Override
        public Object getValueAt(PetDTO pet, int columnIndex) {
            return switch (columnIndex) {
                case 0 -> pet.getId();
                case 1 -> pet.getName();
                case 2 -> pet.getSpecies();
                case 3 -> pet.getDob();
                case 4 -> pet.getOwnerId();
                default -> "";
            };
        }
    }
}
