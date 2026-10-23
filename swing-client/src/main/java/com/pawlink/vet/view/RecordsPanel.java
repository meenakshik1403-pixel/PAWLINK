package com.pawlink.vet.view;

import com.pawlink.vet.api.ApiClient;
import com.pawlink.vet.api.Session;
import com.pawlink.vet.base.BasePanel;
import com.pawlink.vet.base.BaseTableModel;
import com.pawlink.vet.base.PawComponents;
import com.pawlink.vet.base.PawDatePicker;
import com.pawlink.vet.base.PawIcons;
import com.pawlink.vet.base.PawTheme;
import com.pawlink.vet.model.MedicalRecordDTO;
import com.pawlink.vet.model.PetDTO;
import com.pawlink.vet.model.PetMedicalTimelineDTO;
import com.pawlink.vet.model.VaccinationRecordDTO;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Modernized Clinical Medical & Vaccination Records Management Panel.
 * Uses scalable vector icons, modern card sections, and eliminates all emoji tofu boxes.
 */
public class RecordsPanel extends BasePanel {

    private final JTextField petIdField;
    private final JButton loadButton;
    private final JLabel activePetLabel;

    // Left Medical Form
    private final JTextArea diagnosisArea;
    private final JTextArea prescriptionArea;
    private final JButton saveMedicalButton;

    // Right Vaccination Form
    private final JTextField vaccineNameField;
    private final PawDatePicker nextDueDateField;
    private final JButton saveVaccineButton;

    // Bottom History Table
    private final TimelineTableModel tableModel;
    private final JTable historyTable;

    private Long currentPetId = null;

    public RecordsPanel() {
        super("Clinical Medical & Vaccination Records", "Record examination diagnoses, prescriptions, and schedule vaccination reminders");
        setHeaderIcon(PawIcons.medicalRecord(26, PawTheme.LILAC));
        setHeaderColor(PawTheme.LILAC_TEXT);

        // 1. Top Controls Bar
        petIdField = new JTextField(8);
        PawTheme.styleField(petIdField);
        petIdField.setText("1");

        loadButton = new PawComponents.PawButton("Load Patient", PawIcons.search(14, PawTheme.LILAC_TEXT), PawComponents.ButtonStyle.LILAC);

        activePetLabel = new JLabel("No patient loaded");
        activePetLabel.setFont(PawTheme.FONT_BODY_BOLD);
        activePetLabel.setForeground(PawTheme.TEXT_MUTED);

        JPanel topBar = createTopBar();

        // 2. Forms Panel
        diagnosisArea = new JTextArea(4, 25);
        diagnosisArea.setLineWrap(true);
        diagnosisArea.setWrapStyleWord(true);
        diagnosisArea.setFont(PawTheme.FONT_BODY);
        diagnosisArea.setBorder(new EmptyBorder(8, 10, 8, 10));

        prescriptionArea = new JTextArea(4, 25);
        prescriptionArea.setLineWrap(true);
        prescriptionArea.setWrapStyleWord(true);
        prescriptionArea.setFont(PawTheme.FONT_BODY);
        prescriptionArea.setBorder(new EmptyBorder(8, 10, 8, 10));

        saveMedicalButton = new PawComponents.PawButton("Save Consultation", PawIcons.check(14, PawTheme.LILAC_TEXT), PawComponents.ButtonStyle.LILAC);

        vaccineNameField = new JTextField(20);
        PawTheme.styleField(vaccineNameField);

        nextDueDateField = new PawDatePicker(LocalDate.now().plusYears(1));
        nextDueDateField.setAccentColor(PawTheme.MINT);

        saveVaccineButton = new PawComponents.PawButton("Save Vaccine & Alert", PawIcons.vaccine(14, Color.WHITE), PawComponents.ButtonStyle.MINT);

        JPanel formsPanel = createFormsPanel();

        // 3. Bottom Table inside PawCard
        tableModel = new TimelineTableModel();
        historyTable = new JTable(tableModel);
        setupTableAppearance();

        PawComponents.PawCard tableCard = new PawComponents.PawCard();
        tableCard.setLayout(new BorderLayout(0, 10));
        tableCard.setBorder(new EmptyBorder(16, 18, 16, 18));

        JLabel tableTitle = new JLabel("Chronological Medical & Vaccination Timeline", PawIcons.stats(16, PawTheme.LILAC), SwingConstants.LEFT);
        tableTitle.setFont(PawTheme.FONT_SECTION);
        tableTitle.setForeground(PawTheme.TEXT_TITLE);
        tableCard.add(tableTitle, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(historyTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(PawTheme.BORDER_LIGHT));
        scrollPane.setPreferredSize(new Dimension(800, 280));
        tableCard.add(scrollPane, BorderLayout.CENTER);

        // 4. Assemble Center Content
        JPanel centerContent = new JPanel(new BorderLayout(0, 16));
        centerContent.setOpaque(false);
        centerContent.add(formsPanel, BorderLayout.NORTH);
        centerContent.add(tableCard, BorderLayout.CENTER);

        JPanel mainLayout = new JPanel(new BorderLayout(0, 14));
        mainLayout.setOpaque(false);
        mainLayout.setBorder(new EmptyBorder(0, 0, 20, 0));
        mainLayout.add(topBar, BorderLayout.NORTH);
        mainLayout.add(centerContent, BorderLayout.CENTER);

        JScrollPane pageScrollPane = new JScrollPane(mainLayout);
        pageScrollPane.setBorder(null);
        pageScrollPane.setOpaque(false);
        pageScrollPane.getViewport().setOpaque(false);
        pageScrollPane.getVerticalScrollBar().setUnitIncrement(16);

        setContent(pageScrollPane);

        // 5. Action Listeners
        loadButton.addActionListener(e -> loadPetRecords());
        petIdField.addActionListener(e -> loadPetRecords());
        saveMedicalButton.addActionListener(e -> handleSaveMedicalVisit());
        saveVaccineButton.addActionListener(e -> handleSaveVaccination());

        // 6. Initial Load
        loadPetRecords();
    }

    private JPanel createTopBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        bar.setOpaque(true);
        bar.setBackground(PawTheme.SURFACE);
        bar.setBorder(new CompoundBorder(
                new LineBorder(PawTheme.BORDER_LIGHT, 1, true),
                new EmptyBorder(6, 14, 6, 14)
        ));

        JLabel label = new JLabel("Patient Pet ID:");
        label.setFont(PawTheme.FONT_BODY_BOLD);
        label.setForeground(PawTheme.TEXT_BODY);
        bar.add(label);

        bar.add(petIdField);
        bar.add(loadButton);

        JSeparator sep = new JSeparator(SwingConstants.VERTICAL);
        sep.setPreferredSize(new Dimension(2, 24));
        bar.add(sep);

        bar.add(activePetLabel);

        return bar;
    }

    private JPanel createFormsPanel() {
        JPanel container = new JPanel(new GridLayout(1, 2, 18, 0));
        container.setOpaque(false);

        // Left: Medical Form
        PawComponents.PawCard medPanel = new PawComponents.PawCard(14);
        medPanel.setBackgroundTint(PawTheme.LILAC_LIGHT);
        medPanel.setBorderTint(PawTheme.LILAC);
        medPanel.setLayout(new BorderLayout(0, 12));

        JLabel medTitle = new JLabel("Log Clinical Consultation", PawIcons.stethoscope(18, PawTheme.LILAC), SwingConstants.LEFT);
        medTitle.setFont(PawTheme.FONT_SECTION);
        medTitle.setForeground(PawTheme.LILAC_TEXT);
        medPanel.add(medTitle, BorderLayout.NORTH);

        JPanel medForm = new JPanel(new GridLayout(4, 1, 0, 6));
        medForm.setOpaque(false);

        JLabel diagLbl = new JLabel("Diagnosis & Clinical Findings *:");
        diagLbl.setFont(PawTheme.FONT_SMALL_BOLD);
        diagLbl.setForeground(PawTheme.TEXT_BODY);
        medForm.add(diagLbl);
        medForm.add(new JScrollPane(diagnosisArea));

        JLabel rxLbl = new JLabel("Prescription & Treatment Plan *:");
        rxLbl.setFont(PawTheme.FONT_SMALL_BOLD);
        rxLbl.setForeground(PawTheme.TEXT_BODY);
        medForm.add(rxLbl);
        medForm.add(new JScrollPane(prescriptionArea));

        medPanel.add(medForm, BorderLayout.CENTER);

        JPanel medBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 8));
        medBottom.setOpaque(false);
        medBottom.add(saveMedicalButton);
        medPanel.add(medBottom, BorderLayout.SOUTH);

        // Right: Vaccination Form
        PawComponents.PawCard vacPanel = new PawComponents.PawCard(14);
        vacPanel.setBackgroundTint(PawTheme.MINT_LIGHT);
        vacPanel.setBorderTint(PawTheme.MINT);
        vacPanel.setLayout(new BorderLayout(0, 12));

        JLabel vacTitle = new JLabel("Record Administered Vaccination", PawIcons.vaccine(18, PawTheme.MINT), SwingConstants.LEFT);
        vacTitle.setFont(PawTheme.FONT_SECTION);
        vacTitle.setForeground(PawTheme.MINT_TEXT);
        vacPanel.add(vacTitle, BorderLayout.NORTH);

        JPanel vacForm = new JPanel(new GridLayout(4, 1, 0, 6));
        vacForm.setOpaque(false);

        JLabel vNameLbl = new JLabel("Vaccine Name (e.g., Rabies, DHPP, FVRCP) *:");
        vNameLbl.setFont(PawTheme.FONT_SMALL_BOLD);
        vNameLbl.setForeground(PawTheme.TEXT_BODY);
        vacForm.add(vNameLbl);
        vacForm.add(vaccineNameField);

        JLabel vDueLbl = new JLabel("Next Due Date (YYYY-MM-DD) *:");
        vDueLbl.setFont(PawTheme.FONT_SMALL_BOLD);
        vDueLbl.setForeground(PawTheme.TEXT_BODY);
        vacForm.add(vDueLbl);
        vacForm.add(nextDueDateField);

        vacPanel.add(vacForm, BorderLayout.CENTER);

        JPanel vacBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 8));
        vacBottom.setOpaque(false);
        vacBottom.add(saveVaccineButton);
        vacPanel.add(vacBottom, BorderLayout.SOUTH);

        container.add(medPanel);
        container.add(vacPanel);
        return container;
    }

    private void setupTableAppearance() {
        PawTheme.styleTable(historyTable);

        historyTable.getColumnModel().getColumn(0).setPreferredWidth(110);
        historyTable.getColumnModel().getColumn(1).setPreferredWidth(130);
        historyTable.getColumnModel().getColumn(2).setPreferredWidth(260);
        historyTable.getColumnModel().getColumn(3).setPreferredWidth(220);
        historyTable.getColumnModel().getColumn(4).setPreferredWidth(140);

        // Custom Cell Renderer for Type column (badge style without emojis)
        historyTable.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                String type = String.valueOf(value);
                boolean isMed = "MEDICAL".equalsIgnoreCase(type);
                Color fg = isMed ? PawTheme.LILAC_TEXT : PawTheme.MINT_TEXT;
                Color bg = isMed ? PawTheme.LILAC_LIGHT : PawTheme.MINT_LIGHT;
                Color border = isMed ? PawTheme.LILAC : PawTheme.MINT;
                PawComponents.StatusPill pill = new PawComponents.StatusPill(isMed ? "CONSULTATION" : "VACCINE", bg, fg, border);

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
    }

    public void setPetIdAndLoad(Long petId) {
        if (petId != null) {
            petIdField.setText(String.valueOf(petId));
            loadPetRecords();
        }
    }

    private void loadPetRecords() {
        String input = petIdField.getText().trim();
        if (input.isEmpty()) {
            showError("Please enter a valid Pet ID.");
            return;
        }

        Long petId;
        try {
            petId = Long.parseLong(input);
        } catch (NumberFormatException e) {
            showError("Invalid Pet ID: Must be an integer.");
            return;
        }

        currentPetId = petId;
        loadButton.setEnabled(false);
        loadButton.setText("Loading...");

        SwingWorker<List<PetMedicalTimelineDTO>, Void> worker = new SwingWorker<>() {
            private PetDTO petDetails;

            @Override
            protected List<PetMedicalTimelineDTO> doInBackground() {
                try {
                    petDetails = ApiClient.getInstance().get("/api/pets/" + currentPetId, PetDTO.class);
                } catch (Exception ignored) {
                }
                return ApiClient.getInstance().getList("/api/pets/" + currentPetId + "/records", PetMedicalTimelineDTO.class);
            }

            @Override
            protected void done() {
                loadButton.setEnabled(true);
                loadButton.setText("Load Patient");
                try {
                    List<PetMedicalTimelineDTO> timeline = get();
                    tableModel.setItems(timeline);
                    if (petDetails != null) {
                        activePetLabel.setText("Active Patient: " + petDetails.getName() + " (" + petDetails.getSpecies() + ") | Owner #" + petDetails.getOwnerId());
                    } else {
                        activePetLabel.setText("Active Patient: Pet #" + currentPetId);
                    }
                } catch (Exception ex) {
                    tableModel.clear();
                    activePetLabel.setText("Patient not found or no records available");
                    showError("Could not retrieve records for Pet #" + currentPetId + ": " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void handleSaveMedicalVisit() {
        if (currentPetId == null) {
            showError("Please load a valid Pet ID before saving medical records.");
            return;
        }

        String diagnosis = diagnosisArea.getText().trim();
        String prescription = prescriptionArea.getText().trim();

        if (diagnosis.isEmpty()) {
            showError("Diagnosis cannot be blank.");
            return;
        }
        if (prescription.isEmpty()) {
            showError("Prescription cannot be blank.");
            return;
        }

        Long vetId = Session.getInstance().getUserId();
        if (vetId == null) {
            showError("No active vet session. Please log in as a veterinarian.");
            return;
        }

        saveMedicalButton.setEnabled(false);
        saveMedicalButton.setText("Saving...");

        Map<String, Object> payload = new HashMap<>();
        payload.put("petId", currentPetId);
        payload.put("vetId", vetId);
        payload.put("diagnosis", diagnosis);
        payload.put("prescription", prescription);
        payload.put("recordDate", LocalDate.now().toString());

        SwingWorker<MedicalRecordDTO, Void> worker = new SwingWorker<>() {
            @Override
            protected MedicalRecordDTO doInBackground() {
                return ApiClient.getInstance().post("/api/pets/" + currentPetId + "/medical", payload, MedicalRecordDTO.class);
            }

            @Override
            protected void done() {
                saveMedicalButton.setEnabled(true);
                saveMedicalButton.setText("Save Consultation");
                try {
                    get();
                    diagnosisArea.setText("");
                    prescriptionArea.setText("");
                    showSuccess("Medical consultation saved successfully for Pet #" + currentPetId + "!");
                    loadPetRecords();
                } catch (Exception ex) {
                    showError("Failed to save medical consultation: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void handleSaveVaccination() {
        if (currentPetId == null) {
            showError("Please load a valid Pet ID before recording vaccinations.");
            return;
        }

        String vaccineName = vaccineNameField.getText().trim();
        String nextDueStr = nextDueDateField.getText().trim();

        if (vaccineName.isEmpty()) {
            showError("Vaccine name cannot be blank.");
            return;
        }

        LocalDate nextDueDate;
        try {
            nextDueDate = LocalDate.parse(nextDueStr);
        } catch (DateTimeParseException e) {
            showError("Invalid next due date format. Please use YYYY-MM-DD.");
            return;
        }

        saveVaccineButton.setEnabled(false);
        saveVaccineButton.setText("Saving...");

        Map<String, Object> payload = new HashMap<>();
        payload.put("petId", currentPetId);
        payload.put("vaccineName", vaccineName);
        payload.put("givenDate", LocalDate.now().toString());
        payload.put("nextDueDate", nextDueDate.toString());

        SwingWorker<VaccinationRecordDTO, Void> worker = new SwingWorker<>() {
            @Override
            protected VaccinationRecordDTO doInBackground() {
                return ApiClient.getInstance().post("/api/pets/" + currentPetId + "/vaccinations", payload, VaccinationRecordDTO.class);
            }

            @Override
            protected void done() {
                saveVaccineButton.setEnabled(true);
                saveVaccineButton.setText("Save Vaccine & Alert");
                try {
                    VaccinationRecordDTO created = get();
                    vaccineNameField.setText("");
                    nextDueDateField.setText(LocalDate.now().plusYears(1).toString());
                    showSuccess("Vaccine recorded and automated reminder alert registered for " + created.getNextDueDate() + "!");
                    loadPetRecords();
                } catch (Exception ex) {
                    showError("Failed to save vaccination: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    @Override
    public void refresh() {
        if (currentPetId != null) {
            loadPetRecords();
        }
    }

    private static class TimelineTableModel extends BaseTableModel<PetMedicalTimelineDTO> {

        public TimelineTableModel() {
            super(new String[]{"Date", "Event Type", "Diagnosis / Vaccine", "Prescription / Next Due", "Treating Veterinarian"});
        }

        @Override
        public Object getValueAt(PetMedicalTimelineDTO item, int columnIndex) {
            return switch (columnIndex) {
                case 0 -> item.getDate();
                case 1 -> item.getType();
                case 2 -> item.getTitle();
                case 3 -> item.getDetails();
                case 4 -> item.getVetName() != null ? item.getVetName() : "System / Scheduled";
                default -> "";
            };
        }
    }
}
