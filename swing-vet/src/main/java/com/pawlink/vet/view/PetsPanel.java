package com.pawlink.vet.view;

import com.pawlink.vet.api.ApiClient;
import com.pawlink.vet.base.BasePanel;
import com.pawlink.vet.base.BaseTableModel;
import com.pawlink.vet.model.PetDTO;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Veterinary Patients / Pets Management Panel.
 * Extends BasePanel as per team architecture.
 *
 * Features:
 * 1. Top search bar with JTextField and 'Search' JButton (queries pet name or owner ID).
 * 2. Center JTable showing Pet ID, Name, Species, DOB, Owner ID.
 * 3. Bottom detail card dynamically displaying the polymorphic care guidelines
 *    when a table row is selected.
 */
public class PetsPanel extends BasePanel {

    private final JTextField searchField;
    private final JButton searchButton;
    private final JButton refreshButton;

    private final PetTableModel tableModel;
    private final JTable petTable;

    // Bottom Care Guidelines Detail Card Components
    private final JLabel petHeaderLabel;
    private final JLabel speciesBadgeLabel;
    private final JTextArea guidelinesTextArea;
    private final JLabel metadataLabel;

    public PetsPanel() {
        super("🐕 Patients / Pet Directory", "Search patient records and review polymorphic species care guidelines");

        // 1. Initialize Table Model & Table
        tableModel = new PetTableModel();
        petTable = new JTable(tableModel);
        setupTableAppearance();

        // 2. Top Search Controls
        searchField = new JTextField(20);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        searchField.setToolTipText("Search by pet name or owner ID...");

        searchButton = new JButton("Search");
        searchButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        searchButton.setBackground(new Color(13, 148, 136)); // Teal accent
        searchButton.setForeground(Color.WHITE);
        searchButton.setFocusPainted(false);
        searchButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        refreshButton = new JButton("Show All");
        refreshButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        refreshButton.setBackground(new Color(226, 232, 240));
        refreshButton.setForeground(new Color(30, 41, 59));
        refreshButton.setFocusPainted(false);
        refreshButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JPanel searchBarPanel = createSearchBar();

        // 3. Bottom Detail Card
        petHeaderLabel = new JLabel("Select a patient to inspect clinical care guidelines");
        petHeaderLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        petHeaderLabel.setForeground(new Color(30, 41, 59));

        speciesBadgeLabel = new JLabel();
        speciesBadgeLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        speciesBadgeLabel.setOpaque(true);
        speciesBadgeLabel.setBorder(new EmptyBorder(3, 8, 3, 8));
        speciesBadgeLabel.setVisible(false);

        metadataLabel = new JLabel(" ");
        metadataLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        metadataLabel.setForeground(new Color(100, 116, 139));

        guidelinesTextArea = new JTextArea(3, 40);
        guidelinesTextArea.setEditable(false);
        guidelinesTextArea.setLineWrap(true);
        guidelinesTextArea.setWrapStyleWord(true);
        guidelinesTextArea.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        guidelinesTextArea.setForeground(new Color(15, 23, 42));
        guidelinesTextArea.setBackground(new Color(241, 245, 249));
        guidelinesTextArea.setBorder(new CompoundBorder(
                new LineBorder(new Color(203, 213, 225), 1, true),
                new EmptyBorder(10, 12, 10, 12)
        ));

        JPanel detailCard = createDetailCard();

        // 4. Assemble Layout into Content Panel
        JPanel mainContent = new JPanel(new BorderLayout(0, 12));
        mainContent.setOpaque(false);
        mainContent.add(searchBarPanel, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(petTable);
        scrollPane.setBorder(new LineBorder(new Color(226, 232, 240), 1, true));
        scrollPane.getViewport().setBackground(Color.WHITE);
        mainContent.add(scrollPane, BorderLayout.CENTER);

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
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        bar.setOpaque(true);
        bar.setBackground(Color.WHITE);
        bar.setBorder(new CompoundBorder(
                new LineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(6, 12, 6, 12)
        ));

        JLabel iconLabel = new JLabel("🔍 Search Patients:");
        iconLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        iconLabel.setForeground(new Color(51, 65, 85));
        bar.add(iconLabel);

        bar.add(searchField);
        bar.add(searchButton);
        bar.add(refreshButton);

        return bar;
    }

    private JPanel createDetailCard() {
        JPanel card = new JPanel(new BorderLayout(10, 8));
        card.setOpaque(true);
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(
                new LineBorder(new Color(203, 213, 225), 1, true),
                new EmptyBorder(14, 16, 14, 16)
        ));

        JPanel topInfo = new JPanel(new BorderLayout(8, 4));
        topInfo.setOpaque(false);

        JPanel leftHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftHeader.setOpaque(false);
        leftHeader.add(petHeaderLabel);
        leftHeader.add(speciesBadgeLabel);

        topInfo.add(leftHeader, BorderLayout.NORTH);
        topInfo.add(metadataLabel, BorderLayout.SOUTH);

        card.add(topInfo, BorderLayout.NORTH);
        card.add(guidelinesTextArea, BorderLayout.CENTER);

        return card;
    }

    private void setupTableAppearance() {
        petTable.setRowHeight(32);
        petTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        petTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        petTable.setGridColor(new Color(241, 245, 249));
        petTable.setSelectionBackground(new Color(204, 251, 241)); // Light teal highlight
        petTable.setSelectionForeground(new Color(15, 23, 42));
        petTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        petTable.getTableHeader().setBackground(new Color(248, 250, 252));
        petTable.getTableHeader().setForeground(new Color(71, 85, 105));
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

        String icon = switch (pet.getSpecies() != null ? pet.getSpecies().toUpperCase() : "") {
            case "DOG" -> "🐶";
            case "CAT" -> "🐱";
            case "BIRD" -> "🦜";
            case "REPTILE" -> "🦎";
            default -> "🐾";
        };

        petHeaderLabel.setText(icon + " " + pet.getName());
        speciesBadgeLabel.setText(" " + (pet.getSpecies() != null ? pet.getSpecies().toUpperCase() : "UNKNOWN") + " ");
        speciesBadgeLabel.setBackground(getSpeciesColor(pet.getSpecies()));
        speciesBadgeLabel.setForeground(Color.WHITE);
        speciesBadgeLabel.setVisible(true);

        metadataLabel.setText("Patient ID: #" + pet.getId() + "  |  Owner ID: #" + pet.getOwnerId() + "  |  Date of Birth: " + (pet.getDob() != null ? pet.getDob() : "N/A"));

        String guidelines = pet.getCareGuidelines();
        if (guidelines != null && !guidelines.isBlank()) {
            guidelinesTextArea.setText("Species Care Guidelines:\n" + guidelines);
        } else {
            guidelinesTextArea.setText("No specific care guidelines registered for this species.");
        }
    }

    private void clearDetailCard(String prompt) {
        petHeaderLabel.setText(prompt);
        speciesBadgeLabel.setVisible(false);
        metadataLabel.setText(" ");
        guidelinesTextArea.setText("");
    }

    private Color getSpeciesColor(String species) {
        if (species == null) return new Color(100, 116, 139);
        return switch (species.toUpperCase()) {
            case "DOG" -> new Color(37, 99, 235);    // Blue
            case "CAT" -> new Color(147, 51, 234);   // Purple
            case "BIRD" -> new Color(13, 148, 136);   // Teal
            case "REPTILE" -> new Color(217, 119, 6); // Amber
            default -> new Color(100, 116, 139);
        };
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
