package com.pawlink.vet.view;

import com.pawlink.vet.api.ApiClient;
import com.pawlink.vet.api.ApiException;
import com.pawlink.vet.api.Session;
import com.pawlink.vet.base.BasePanel;
import com.pawlink.vet.base.BaseTableModel;
import com.pawlink.vet.model.MedicalRecordDTO;
import com.pawlink.vet.model.PetDTO;
import com.pawlink.vet.model.PetMedicalTimelineDTO;
import com.pawlink.vet.model.VaccinationRecordDTO;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Clinical Medical & Vaccination Records Management Panel.
 * Extends BasePanel as per team architecture.
 *
 * Layout:
 * - Top bar: Pet ID input + "Load Records" button + active patient badge.
 * - Left side (Medical Form): Diagnosis (JTextArea), Prescription (JTextArea), "Save Medical Visit" button.
 * - Right side (Vaccination Sub-form): Vaccine name (JTextField), Next due date picker, "Save Vaccine" button.
 * - Bottom: JTable showing the pet's unified chronological medical and vaccination history.
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
    private final JTextField nextDueDateField;
    private final JButton saveVaccineButton;

    // Bottom History Table
    private final TimelineTableModel tableModel;
    private final JTable historyTable;

    private Long currentPetId = null;

    public RecordsPanel() {
        super("📋 Clinical Medical & Vaccination Records", "Record examination diagnoses, prescriptions, and schedule vaccination reminders");

        // 1. Top Controls Bar
        petIdField = new JTextField(8);
        petIdField.setFont(new Font("Segoe UI", Font.BOLD, 14));
        petIdField.setText("1"); // Default to seed pet

        loadButton = new JButton("Load Records");
        loadButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        loadButton.setBackground(new Color(13, 148, 136)); // Teal
        loadButton.setForeground(Color.WHITE);
        loadButton.setFocusPainted(false);
        loadButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        activePetLabel = new JLabel("No patient loaded");
        activePetLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        activePetLabel.setForeground(new Color(71, 85, 105));

        JPanel topBar = createTopBar();

        // 2. Forms Panel (Split Left & Right)
        diagnosisArea = new JTextArea(4, 25);
        diagnosisArea.setLineWrap(true);
        diagnosisArea.setWrapStyleWord(true);
        diagnosisArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        prescriptionArea = new JTextArea(4, 25);
        prescriptionArea.setLineWrap(true);
        prescriptionArea.setWrapStyleWord(true);
        prescriptionArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        saveMedicalButton = new JButton("Save Medical Visit");
        saveMedicalButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        saveMedicalButton.setBackground(new Color(37, 99, 235)); // Blue
        saveMedicalButton.setForeground(Color.WHITE);
        saveMedicalButton.setFocusPainted(false);
        saveMedicalButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        vaccineNameField = new JTextField(20);
        vaccineNameField.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        nextDueDateField = new JTextField(LocalDate.now().plusYears(1).toString(), 15);
        nextDueDateField.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        saveVaccineButton = new JButton("Save Vaccine & Alert");
        saveVaccineButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        saveVaccineButton.setBackground(new Color(16, 185, 129)); // Emerald Green
        saveVaccineButton.setForeground(Color.WHITE);
        saveVaccineButton.setFocusPainted(false);
        saveVaccineButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JPanel formsPanel = createFormsPanel();

        // 3. Bottom Table
        tableModel = new TimelineTableModel();
        historyTable = new JTable(tableModel);
        setupTableAppearance();

        JPanel tablePanel = new JPanel(new BorderLayout(0, 8));
        tablePanel.setOpaque(false);
        JLabel tableTitle = new JLabel("📊 Chronological Medical & Vaccination Timeline");
        tableTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tableTitle.setForeground(new Color(30, 41, 59));
        tablePanel.add(tableTitle, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(historyTable);
        scrollPane.setBorder(new LineBorder(new Color(226, 232, 240), 1, true));
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setPreferredSize(new Dimension(800, 240));
        scrollPane.setMinimumSize(new Dimension(500, 160));
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        // 4. Assemble Center Content
        JPanel centerContent = new JPanel(new BorderLayout(0, 15));
        centerContent.setOpaque(false);
        centerContent.add(formsPanel, BorderLayout.NORTH);
        centerContent.add(tablePanel, BorderLayout.CENTER);

        JPanel mainLayout = new JPanel(new BorderLayout(0, 12));
        mainLayout.setOpaque(false);
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
        bar.setBackground(Color.WHITE);
        bar.setBorder(new CompoundBorder(
                new LineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(6, 12, 6, 12)
        ));

        JLabel label = new JLabel("Select Pet ID:");
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(new Color(51, 65, 85));
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
        JPanel container = new JPanel(new GridLayout(1, 2, 16, 0));
        container.setOpaque(false);

        // Left: Medical Form
        JPanel medPanel = new JPanel(new BorderLayout(0, 10));
        medPanel.setOpaque(true);
        medPanel.setBackground(Color.WHITE);
        medPanel.setBorder(new CompoundBorder(
                new LineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(12, 14, 12, 14)
        ));

        JLabel medTitle = new JLabel("🩺 Log Clinical Consultation");
        medTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        medTitle.setForeground(new Color(37, 99, 235));
        medPanel.add(medTitle, BorderLayout.NORTH);

        JPanel medForm = new JPanel(new GridLayout(4, 1, 0, 6));
        medForm.setOpaque(false);

        medForm.add(new JLabel("Diagnosis / Clinical Assessment *:"));
        JScrollPane diagScroll = new JScrollPane(diagnosisArea);
        diagScroll.setPreferredSize(new Dimension(100, 55));
        medForm.add(diagScroll);

        medForm.add(new JLabel("Prescription / Treatment Plan *:"));
        JScrollPane rxScroll = new JScrollPane(prescriptionArea);
        rxScroll.setPreferredSize(new Dimension(100, 55));
        medForm.add(rxScroll);

        medPanel.add(medForm, BorderLayout.CENTER);

        JPanel medBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 8));
        medBottom.setOpaque(false);
        medBottom.add(saveMedicalButton);
        medPanel.add(medBottom, BorderLayout.SOUTH);

        // Right: Vaccination Form
        JPanel vacPanel = new JPanel(new BorderLayout(0, 10));
        vacPanel.setOpaque(true);
        vacPanel.setBackground(Color.WHITE);
        vacPanel.setBorder(new CompoundBorder(
                new LineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(12, 14, 12, 14)
        ));

        JLabel vacTitle = new JLabel("💉 Record Vaccination & Due Date");
        vacTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        vacTitle.setForeground(new Color(16, 185, 129));
        vacPanel.add(vacTitle, BorderLayout.NORTH);

        JPanel vacForm = new JPanel(new GridLayout(4, 1, 0, 6));
        vacForm.setOpaque(false);

        vacForm.add(new JLabel("Vaccine Name (e.g., Rabies, DHPP, FVRCP) *:"));
        vacForm.add(vaccineNameField);

        vacForm.add(new JLabel("Next Due Date (YYYY-MM-DD) *:"));
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
        historyTable.setRowHeight(32);
        historyTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        historyTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        historyTable.setGridColor(new Color(241, 245, 249));
        historyTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        historyTable.getTableHeader().setBackground(new Color(248, 250, 252));
        historyTable.getTableHeader().setForeground(new Color(71, 85, 105));

        // Custom Cell Renderer for Type column (badge style)
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
                loadButton.setText("Load Records");
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
                saveMedicalButton.setText("Save Medical Visit");
                try {
                    MedicalRecordDTO created = get();
                    diagnosisArea.setText("");
                    prescriptionArea.setText("");
                    showSuccess("Medical consultation saved successfully for Pet #" + currentPetId + "!");
                    loadPetRecords(); // Reload chronological history
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
                    loadPetRecords(); // Reload chronological history
                } catch (Exception ex) {
                    showError("Failed to save vaccination: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    /**
     * Parameterized TableModel for unified Pet Medical & Vaccination History Timeline.
     */
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
