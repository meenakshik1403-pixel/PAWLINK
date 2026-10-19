package com.pawlink.vet.view;

import com.pawlink.vet.api.ApiClient;
import com.pawlink.vet.api.Session;
import com.pawlink.vet.base.BasePanel;
import com.pawlink.vet.base.BaseTableModel;
import com.pawlink.vet.model.PetDTO;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.List;

/**
 * Pet Owner Management Panel in Swing (replaces Thymeleaf for a full-Swing experience).
 * Allows pet owners to view their registered pets and register new pets.
 * Enforces ownership validation: only pets belonging to the logged-in owner are displayed.
 */
public class OwnerPetsPanel extends BasePanel {

    private final PetTableModel tableModel;
    private final JTable petTable;

    private final JButton addPetButton;
    private final JButton refreshButton;

    // Bottom Care Guidelines Detail Card
    private final JLabel petHeaderLabel;
    private final JLabel speciesBadgeLabel;
    private final JTextArea guidelinesTextArea;
    private final JLabel metadataLabel;

    public OwnerPetsPanel() {
        super("🐶 My Pets", "Manage your registered pets and view their personalized care guidelines");

        // 1. Initialize Table Model
        tableModel = new PetTableModel();
        petTable = new JTable(tableModel);
        setupTableAppearance();

        // 2. Action Buttons in Header Accessory
        addPetButton = new JButton("+ Register New Pet");
        addPetButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        addPetButton.setBackground(new Color(37, 99, 235)); // Royal Blue
        addPetButton.setForeground(Color.WHITE);
        addPetButton.setFocusPainted(false);
        addPetButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        refreshButton = new JButton("Refresh");
        refreshButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        refreshButton.setBackground(new Color(226, 232, 240));
        refreshButton.setForeground(new Color(30, 41, 59));
        refreshButton.setFocusPainted(false);
        refreshButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionsPanel.setOpaque(false);
        actionsPanel.add(refreshButton);
        actionsPanel.add(addPetButton);
        setHeaderAccessory(actionsPanel);

        // 3. Bottom Detail Card
        petHeaderLabel = new JLabel("Select a pet to view its care guidelines");
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

        // 4. Assemble Center Content
        JPanel mainContent = new JPanel(new BorderLayout(0, 12));
        mainContent.setOpaque(false);

        JScrollPane scrollPane = new JScrollPane(petTable);
        scrollPane.setBorder(new LineBorder(new Color(226, 232, 240), 1, true));
        scrollPane.getViewport().setBackground(Color.WHITE);
        mainContent.add(scrollPane, BorderLayout.CENTER);

        mainContent.add(detailCard, BorderLayout.SOUTH);

        setContent(mainContent);

        // 5. Setup Action Listeners
        addPetButton.addActionListener(e -> openAddPetDialog());
        refreshButton.addActionListener(e -> loadOwnerPets());

        petTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateDetailCard();
            }
        });

        // 6. Initial Load
        loadOwnerPets();
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
        petTable.setSelectionBackground(new Color(219, 234, 254)); // Light blue highlight
        petTable.setSelectionForeground(new Color(15, 23, 42));
        petTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        petTable.getTableHeader().setBackground(new Color(248, 250, 252));
        petTable.getTableHeader().setForeground(new Color(71, 85, 105));
    }

    private void openAddPetDialog() {
        Frame topFrame = (Frame) SwingUtilities.getWindowAncestor(this);
        AddPetDialog dialog = new AddPetDialog(topFrame);
        dialog.setVisible(true);
        if (dialog.isPetCreated()) {
            loadOwnerPets();
        }
    }

    public void loadOwnerPets() {
        Long ownerId = Session.getInstance().getUserId();
        if (ownerId == null) {
            clearDetailCard("No owner session active.");
            return;
        }

        refreshButton.setEnabled(false);
        refreshButton.setText("Loading...");

        SwingWorker<List<PetDTO>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<PetDTO> doInBackground() {
                // Strict ownership validation: filter by ownerId parameter
                return ApiClient.getInstance().getList("/api/pets?ownerId=" + ownerId, PetDTO.class);
            }

            @Override
            protected void done() {
                refreshButton.setEnabled(true);
                refreshButton.setText("Refresh");
                try {
                    List<PetDTO> pets = get();
                    tableModel.setItems(pets);
                    if (pets != null && !pets.isEmpty()) {
                        petTable.setRowSelectionInterval(0, 0);
                        updateDetailCard();
                    } else {
                        clearDetailCard("You currently have no registered pets. Click '+ Register New Pet' to get started.");
                    }
                } catch (Exception ex) {
                    showError("Failed to load your pets: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void updateDetailCard() {
        int selectedRow = petTable.getSelectedRow();
        if (selectedRow < 0) {
            clearDetailCard("Select a pet to view its care guidelines");
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

        metadataLabel.setText("Pet ID: #" + pet.getId() + "  |  Date of Birth: " + (pet.getDob() != null ? pet.getDob() : "N/A"));

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
     * Parameterized TableModel for Owner's Pet records.
     */
    private static class PetTableModel extends BaseTableModel<PetDTO> {

        public PetTableModel() {
            super(new String[]{"ID", "Name", "Species", "Date of Birth", "Care Guidelines"});
        }

        @Override
        public Object getValueAt(PetDTO pet, int columnIndex) {
            return switch (columnIndex) {
                case 0 -> pet.getId();
                case 1 -> pet.getName();
                case 2 -> pet.getSpecies();
                case 3 -> pet.getDob();
                case 4 -> pet.getCareGuidelines();
                default -> "";
            };
        }
    }
}
