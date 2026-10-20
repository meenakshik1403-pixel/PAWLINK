package com.pawlink.vet.view;

import com.pawlink.vet.api.ApiClient;
import com.pawlink.vet.api.Session;
import com.pawlink.vet.base.BasePanel;
import com.pawlink.vet.base.BaseTableModel;
import com.pawlink.vet.model.PetDTO;
import com.pawlink.vet.model.ReminderDTO;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Pet Owner Management Panel in Swing.
 * Allows pet owners to view registered pets, inspect personalized care guidelines,
 * track upcoming vaccination/medication schedules ahead, and schedule care reminders.
 */
public class OwnerPetsPanel extends BasePanel {

    private final PetTableModel tableModel;
    private final JTable petTable;

    private final JButton addPetButton;
    private final JButton refreshButton;

    // Bottom Care Guidelines & Reminders Detail Card
    private final JLabel petHeaderLabel;
    private final JLabel speciesBadgeLabel;
    private final JLabel metadataLabel;
    private final JTextArea guidelinesTextArea;
    private final JTextArea scheduleTextArea;
    private final JButton addReminderQuickButton;

    public OwnerPetsPanel() {
        super("🐶 My Pets & Care Schedule", "Manage your registered pets, view breed care guidelines, and monitor upcoming schedules ahead");

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

        JButton historyButton = new JButton("📋 Medical & Vaccines");
        historyButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        historyButton.setBackground(new Color(13, 148, 136)); // Teal
        historyButton.setForeground(Color.WHITE);
        historyButton.setFocusPainted(false);
        historyButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        historyButton.addActionListener(e -> openMedicalHistoryDialog());

        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionsPanel.setOpaque(false);
        actionsPanel.add(refreshButton);
        actionsPanel.add(historyButton);
        actionsPanel.add(addPetButton);
        setHeaderAccessory(actionsPanel);

        // 3. Bottom Detail Card Components
        petHeaderLabel = new JLabel("Select a pet to view care guidelines & schedule ahead");
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

        guidelinesTextArea = new JTextArea(3, 25);
        guidelinesTextArea.setEditable(false);
        guidelinesTextArea.setLineWrap(true);
        guidelinesTextArea.setWrapStyleWord(true);
        guidelinesTextArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        guidelinesTextArea.setForeground(new Color(15, 23, 42));
        guidelinesTextArea.setBackground(new Color(248, 250, 252));
        guidelinesTextArea.setBorder(new CompoundBorder(
                new LineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(8, 10, 8, 10)
        ));

        scheduleTextArea = new JTextArea(3, 25);
        scheduleTextArea.setEditable(false);
        scheduleTextArea.setLineWrap(true);
        scheduleTextArea.setWrapStyleWord(true);
        scheduleTextArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        scheduleTextArea.setForeground(new Color(15, 23, 42));
        scheduleTextArea.setBackground(new Color(248, 250, 252));
        scheduleTextArea.setBorder(new CompoundBorder(
                new LineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(8, 10, 8, 10)
        ));

        addReminderQuickButton = new JButton("+ Add Reminder");
        addReminderQuickButton.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        addReminderQuickButton.setMargin(new Insets(2, 6, 2, 6));
        addReminderQuickButton.setFocusable(false);
        addReminderQuickButton.addActionListener(e -> openAddReminderDialog());

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
        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.setOpaque(true);
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(
                new LineBorder(new Color(203, 213, 225), 1, true),
                new EmptyBorder(12, 16, 12, 16)
        ));

        // Top info header
        JPanel topInfo = new JPanel(new BorderLayout(8, 4));
        topInfo.setOpaque(false);

        JPanel leftHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftHeader.setOpaque(false);
        leftHeader.add(petHeaderLabel);
        leftHeader.add(speciesBadgeLabel);

        topInfo.add(leftHeader, BorderLayout.NORTH);
        topInfo.add(metadataLabel, BorderLayout.SOUTH);
        card.add(topInfo, BorderLayout.NORTH);

        // Center: Dual Panels (Care Guidelines on Left, Schedule Ahead on Right)
        JPanel dualPanel = new JPanel(new GridLayout(1, 2, 14, 0));
        dualPanel.setOpaque(false);

        // Left: Care Guidelines
        JPanel leftBox = new JPanel(new BorderLayout(0, 6));
        leftBox.setOpaque(false);
        JLabel guidelinesTitle = new JLabel("📖 Breed & Species Guidelines");
        guidelinesTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        guidelinesTitle.setForeground(new Color(71, 85, 105));
        leftBox.add(guidelinesTitle, BorderLayout.NORTH);
        leftBox.add(new JScrollPane(guidelinesTextArea), BorderLayout.CENTER);
        dualPanel.add(leftBox);

        // Right: Upcoming Care Schedule
        JPanel rightBox = new JPanel(new BorderLayout(0, 6));
        rightBox.setOpaque(false);

        JPanel rightHeader = new JPanel(new BorderLayout());
        rightHeader.setOpaque(false);
        JLabel scheduleTitle = new JLabel("⏰ Upcoming Schedule & Next Due Dates");
        scheduleTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        scheduleTitle.setForeground(new Color(13, 148, 136)); // Teal
        rightHeader.add(scheduleTitle, BorderLayout.WEST);
        rightHeader.add(addReminderQuickButton, BorderLayout.EAST);

        rightBox.add(rightHeader, BorderLayout.NORTH);
        rightBox.add(new JScrollPane(scheduleTextArea), BorderLayout.CENTER);
        dualPanel.add(rightBox);

        card.add(dualPanel, BorderLayout.CENTER);

        return card;
    }

    private void setupTableAppearance() {
        petTable.setRowHeight(32);
        petTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        petTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        petTable.setGridColor(new Color(241, 245, 249));
        petTable.setSelectionBackground(new Color(219, 234, 254));
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

    private void openAddReminderDialog() {
        int selectedRow = petTable.getSelectedRow();
        Long petId = null;
        if (selectedRow >= 0) {
            PetDTO p = tableModel.getItem(selectedRow);
            if (p != null) petId = p.getId();
        }

        Frame topFrame = (Frame) SwingUtilities.getWindowAncestor(this);
        final Long currentPetId = petId;
        CreateReminderDialog dialog = new CreateReminderDialog(topFrame, petId, () -> {
            if (currentPetId != null) {
                loadRemindersForPet(currentPetId);
            }
        });
        dialog.setVisible(true);
    }

    private void openMedicalHistoryDialog() {
        int selectedRow = petTable.getSelectedRow();
        if (selectedRow < 0) {
            showError("Please select a pet from the table to view its medical and vaccination history.");
            return;
        }

        PetDTO selectedPet = tableModel.getItem(selectedRow);
        if (selectedPet != null) {
            Frame topFrame = (Frame) SwingUtilities.getWindowAncestor(this);
            PetHistoryDialog dialog = new PetHistoryDialog(topFrame, selectedPet);
            dialog.setVisible(true);
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
            clearDetailCard("Select a pet to view its care guidelines & schedule ahead");
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
            guidelinesTextArea.setText(guidelines);
        } else {
            guidelinesTextArea.setText("No specific care guidelines registered for this species.");
        }

        // Asynchronously load schedule / reminders for this selected pet
        loadRemindersForPet(pet.getId());
    }

    private void loadRemindersForPet(Long petId) {
        scheduleTextArea.setText("Loading care schedule ahead...");

        SwingWorker<List<ReminderDTO>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<ReminderDTO> doInBackground() throws Exception {
                return ApiClient.getInstance().getList("/api/reminders?petId=" + petId, ReminderDTO.class);
            }

            @Override
            protected void done() {
                try {
                    List<ReminderDTO> reminders = get();
                    if (reminders == null || reminders.isEmpty()) {
                        scheduleTextArea.setText("✅ All vaccinations & care treatments are up to date!\nNo pending reminders ahead.");
                    } else {
                        StringBuilder sb = new StringBuilder();
                        LocalDate today = LocalDate.now();
                        for (ReminderDTO r : reminders) {
                            String typeIcon = switch (r.getType() != null ? r.getType() : "") {
                                case "VACCINATION" -> "💉";
                                case "MEDICATION" -> "💊";
                                default -> "🩺";
                            };

                            sb.append(typeIcon).append(" ").append(r.getType());
                            if (r.getDueDate() != null) {
                                long days = ChronoUnit.DAYS.between(today, r.getDueDate());
                                if (days < 0) {
                                    sb.append(": Due ").append(r.getDueDate()).append(" (⚠️ OVERDUE by ").append(Math.abs(days)).append(" days)");
                                } else if (days == 0) {
                                    sb.append(": 🚨 DUE TODAY (").append(r.getDueDate()).append(")");
                                } else {
                                    sb.append(": Due ").append(r.getDueDate()).append(" (in ").append(days).append(" days)");
                                }
                            }
                            sb.append("\n");
                        }
                        scheduleTextArea.setText(sb.toString().trim());
                    }
                } catch (Exception ex) {
                    scheduleTextArea.setText("Could not load care schedule: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void clearDetailCard(String prompt) {
        petHeaderLabel.setText(prompt);
        speciesBadgeLabel.setVisible(false);
        metadataLabel.setText(" ");
        guidelinesTextArea.setText("");
        scheduleTextArea.setText("");
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
