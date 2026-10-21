package com.pawlink.vet.view;

import com.pawlink.vet.api.ApiClient;
import com.pawlink.vet.api.Session;
import com.pawlink.vet.base.BasePanel;
import com.pawlink.vet.base.BaseTableModel;
import com.pawlink.vet.base.PawComponents;
import com.pawlink.vet.base.PawIcons;
import com.pawlink.vet.base.PawTheme;
import com.pawlink.vet.model.PetDTO;
import com.pawlink.vet.model.ReminderDTO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
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
        super("My Pets & Care Schedule", "Manage registered pets, review breed care guidelines, and monitor upcoming care schedules.");
        setHeaderIcon(PawIcons.paw(24, PawTheme.CORAL));
        setHeaderColor(PawTheme.CORAL_TEXT);

        // 1. Initialize Table Model
        tableModel = new PetTableModel();
        petTable = new JTable(tableModel);
        setupTableAppearance();

        // 2. Action Buttons in Header Accessory
        addPetButton = new PawComponents.PawButton("Register Pet", PawIcons.paw(15, Color.WHITE), PawComponents.ButtonStyle.CORAL);
        refreshButton = new PawComponents.PawButton("Refresh", PawIcons.refresh(13, PawTheme.TEXT_BODY), PawComponents.ButtonStyle.SECONDARY);

        JButton historyButton = new PawComponents.PawButton("Medical History", PawIcons.medicalRecord(14, Color.WHITE), PawComponents.ButtonStyle.LILAC);
        historyButton.addActionListener(e -> openMedicalHistoryDialog());

        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionsPanel.setOpaque(false);
        actionsPanel.add(refreshButton);
        actionsPanel.add(historyButton);
        actionsPanel.add(addPetButton);
        setHeaderAccessory(actionsPanel);

        // 3. Bottom Detail Card Components
        petHeaderLabel = new JLabel("Select a pet to view care guidelines & schedule ahead");
        petHeaderLabel.setFont(PawTheme.FONT_SECTION);
        petHeaderLabel.setForeground(PawTheme.TEXT_TITLE);

        speciesBadgeLabel = new JLabel();
        speciesBadgeLabel.setFont(PawTheme.FONT_BADGE);
        speciesBadgeLabel.setOpaque(true);
        speciesBadgeLabel.setBorder(new EmptyBorder(4, 10, 4, 10));
        speciesBadgeLabel.setVisible(false);

        metadataLabel = new JLabel(" ");
        metadataLabel.setFont(PawTheme.FONT_BODY);
        metadataLabel.setForeground(PawTheme.TEXT_MUTED);

        guidelinesTextArea = new JTextArea(4, 25);
        guidelinesTextArea.setEditable(false);
        guidelinesTextArea.setLineWrap(true);
        guidelinesTextArea.setWrapStyleWord(true);
        guidelinesTextArea.setFont(PawTheme.FONT_BODY);
        guidelinesTextArea.setForeground(PawTheme.TEXT_BODY);
        guidelinesTextArea.setBackground(new Color(250, 251, 254));
        guidelinesTextArea.setBorder(new EmptyBorder(10, 12, 10, 12));

        scheduleTextArea = new JTextArea(4, 25);
        scheduleTextArea.setEditable(false);
        scheduleTextArea.setLineWrap(true);
        scheduleTextArea.setWrapStyleWord(true);
        scheduleTextArea.setFont(PawTheme.FONT_BODY);
        scheduleTextArea.setForeground(PawTheme.TEXT_BODY);
        scheduleTextArea.setBackground(new Color(254, 252, 248));
        scheduleTextArea.setBorder(new EmptyBorder(10, 12, 10, 12));

        addReminderQuickButton = new PawComponents.PawButton("Add Reminder", PawIcons.bell(13, Color.WHITE), PawComponents.ButtonStyle.SUNNY);
        addReminderQuickButton.setFocusable(false);
        addReminderQuickButton.addActionListener(e -> openAddReminderDialog());

        JPanel detailCard = createDetailCard();

        // 4. Assemble Center Content inside PawCard
        PawComponents.PawCard tableCard = new PawComponents.PawCard();
        tableCard.setLayout(new BorderLayout(0, 10));

        JScrollPane scrollPane = new JScrollPane(petTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(PawTheme.BORDER_LIGHT));
        scrollPane.getViewport().setBackground(PawTheme.SURFACE);
        tableCard.add(scrollPane, BorderLayout.CENTER);

        JPanel mainContent = new JPanel(new BorderLayout(0, 16));
        mainContent.setOpaque(false);
        mainContent.add(tableCard, BorderLayout.CENTER);
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
        PawComponents.PawCard card = new PawComponents.PawCard(14);
        card.setBackgroundTint(PawTheme.PEACH_LIGHT);
        card.setBorderTint(PawTheme.PEACH);
        card.setLayout(new BorderLayout(10, 12));
        card.setBorder(new EmptyBorder(14, 18, 14, 18));

        // Top info header
        JPanel topInfo = new JPanel(new BorderLayout(8, 4));
        topInfo.setOpaque(false);

        JPanel leftHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftHeader.setOpaque(false);
        leftHeader.add(petHeaderLabel);
        leftHeader.add(speciesBadgeLabel);

        topInfo.add(leftHeader, BorderLayout.NORTH);
        topInfo.add(metadataLabel, BorderLayout.SOUTH);
        card.add(topInfo, BorderLayout.NORTH);

        // Center: Dual Panels (Care Guidelines on Left, Schedule Ahead on Right)
        JPanel dualPanel = new JPanel(new GridLayout(1, 2, 16, 0));
        dualPanel.setOpaque(false);

        // Left: Care Guidelines
        JPanel leftBox = new JPanel(new BorderLayout(0, 6));
        leftBox.setOpaque(false);
        JLabel guidelinesTitle = new JLabel("Breed & Species Guidelines", PawIcons.document(14, PawTheme.CORAL_TEXT), SwingConstants.LEFT);
        guidelinesTitle.setFont(PawTheme.FONT_BODY_BOLD);
        guidelinesTitle.setForeground(PawTheme.CORAL_TEXT);
        leftBox.add(guidelinesTitle, BorderLayout.NORTH);
        leftBox.add(new JScrollPane(guidelinesTextArea), BorderLayout.CENTER);
        dualPanel.add(leftBox);

        // Right: Upcoming Care Schedule
        JPanel rightBox = new JPanel(new BorderLayout(0, 6));
        rightBox.setOpaque(false);

        JPanel rightHeader = new JPanel(new BorderLayout());
        rightHeader.setOpaque(false);
        JLabel scheduleTitle = new JLabel("Upcoming Schedule & Due Dates", PawIcons.clock(14, PawTheme.SUNNY_TEXT), SwingConstants.LEFT);
        scheduleTitle.setFont(PawTheme.FONT_BODY_BOLD);
        scheduleTitle.setForeground(PawTheme.SUNNY_TEXT);
        rightHeader.add(scheduleTitle, BorderLayout.WEST);
        rightHeader.add(addReminderQuickButton, BorderLayout.EAST);

        rightBox.add(rightHeader, BorderLayout.NORTH);
        rightBox.add(new JScrollPane(scheduleTextArea), BorderLayout.CENTER);
        dualPanel.add(rightBox);

        card.add(dualPanel, BorderLayout.CENTER);

        return card;
    }

    private void setupTableAppearance() {
        PawTheme.styleTable(petTable);
        petTable.getColumnModel().getColumn(2).setCellRenderer(new PawComponents.StatusTableCellRenderer());
        petTable.getColumnModel().getColumn(0).setPreferredWidth(80);
        petTable.getColumnModel().getColumn(1).setPreferredWidth(160);
        petTable.getColumnModel().getColumn(2).setPreferredWidth(130);
        petTable.getColumnModel().getColumn(3).setPreferredWidth(130);
        petTable.getColumnModel().getColumn(4).setPreferredWidth(100);
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

    @Override
    public void refresh() {
        loadOwnerPets();
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

        petHeaderLabel.setText(pet.getName());
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
                        scheduleTextArea.setText("All vaccinations and care treatments are up to date.\nNo pending reminders ahead.");
                    } else {
                        StringBuilder sb = new StringBuilder();
                        LocalDate today = LocalDate.now();
                        for (ReminderDTO r : reminders) {
                            sb.append("[").append(r.getType()).append("] ");
                            if (r.getDueDate() != null) {
                                long days = ChronoUnit.DAYS.between(today, r.getDueDate());
                                if (days < 0) {
                                    sb.append("Due ").append(r.getDueDate()).append(" (OVERDUE by ").append(Math.abs(days)).append(" days)");
                                } else if (days == 0) {
                                    sb.append("DUE TODAY (").append(r.getDueDate()).append(")");
                                } else {
                                    sb.append("Due ").append(r.getDueDate()).append(" (in ").append(days).append(" days)");
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
