package com.pawlink.vet.view;

import com.pawlink.vet.api.ApiClient;
import com.pawlink.vet.api.Session;
import com.pawlink.vet.base.BasePanel;
import com.pawlink.vet.base.BaseTableModel;
import com.pawlink.vet.base.PawComponents;
import com.pawlink.vet.base.PawDatePicker;
import com.pawlink.vet.base.PawIcons;
import com.pawlink.vet.base.PawTheme;
import com.pawlink.vet.model.AppointmentDTO;
import com.pawlink.vet.model.UserDTO;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Vet Appointments & Scheduling Vertical Panel.
 * Dynamically adapts to logged-in user role:
 * - OWNER: Displays their pets' appointments with date and doctor filters, book new appointment button.
 * - VET: Displays clinician's appointments with today/tomorrow/all filters and confirm/complete actions.
 * - ADMIN: Displays all platform appointments with doctor dropdown filter.
 */
public class AppointmentsPanel extends BasePanel {

    private final JComboBox<String> dateQuickSelect;
    private final PawDatePicker dateField;
    private final JComboBox<String> doctorFilterComboBox;
    private final Map<Integer, Long> doctorIndexToId = new HashMap<>();
    private final JButton loadButton;
    private final JButton bookNewButton;
    private final JLabel statusHintLabel;

    private final AppointmentTableModel tableModel;
    private final JTable appointmentTable;

    private final JButton confirmButton;
    private final JButton completeButton;
    private final JButton cancelButton;

    private final Session session;

    public AppointmentsPanel() {
        super(
                getAdaptiveTitle(),
                getAdaptiveSubtitle()
        );
        this.session = Session.getInstance();

        // 1. Date filter setup - defaults to Today for all roles
        String[] dateOptions = {"Today", "Tomorrow", "Next 7 Days", "All Dates", "Custom Date"};
        dateQuickSelect = new JComboBox<>(dateOptions);
        dateQuickSelect.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        dateQuickSelect.setSelectedItem("Today");

        dateField = new PawDatePicker(LocalDate.now());
        dateField.setAccentColor(PawTheme.SKY);
        dateField.setEnabled(false);

        // 2. Doctor selection (dropdown for Owner/Admin, label for Vet)
        doctorFilterComboBox = new JComboBox<>();
        doctorFilterComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        doctorFilterComboBox.addItem("All Doctors");
        doctorIndexToId.put(0, null);

        // Modern Tactile Buttons
        loadButton = new PawComponents.PawButton("Refresh", PawIcons.refresh(13, PawTheme.TEXT_BODY), PawComponents.ButtonStyle.SECONDARY);

        bookNewButton = new PawComponents.PawButton("Book Appointment", PawIcons.plus(15, Color.WHITE), PawComponents.ButtonStyle.CORAL);
        if (session.isOwner()) {
            bookNewButton.setPreferredSize(new Dimension(195, 42));
            setHeaderAccessory(bookNewButton);
        }

        JPanel topBar = createTopBar();

        // 3. Center Table Configuration inside PawCard
        tableModel = new AppointmentTableModel();
        appointmentTable = new JTable(tableModel);
        PawTheme.styleTable(appointmentTable);
        setupTableRenderers();

        JScrollPane scrollPane = new JScrollPane(appointmentTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(PawTheme.BORDER_LIGHT));
        scrollPane.getViewport().setBackground(PawTheme.SURFACE);

        // 4. Bottom Action Buttons & Status Hint
        statusHintLabel = new JLabel("Loading appointments...");
        statusHintLabel.setFont(PawTheme.FONT_BODY);
        statusHintLabel.setForeground(PawTheme.TEXT_MUTED);

        confirmButton = new PawComponents.PawButton("Confirm Slot", PawIcons.check(14, Color.WHITE), PawComponents.ButtonStyle.MINT);
        confirmButton.setEnabled(false);

        completeButton = new PawComponents.PawButton("Mark Completed", PawIcons.stethoscope(14, Color.WHITE), PawComponents.ButtonStyle.SKY);
        completeButton.setEnabled(false);

        cancelButton = new PawComponents.PawButton("Cancel Booking", PawIcons.cross(14, Color.WHITE), PawComponents.ButtonStyle.DANGER);
        cancelButton.setEnabled(false);

        JPanel bottomBar = createBottomBar();

        PawComponents.PawCard tableCard = new PawComponents.PawCard();
        tableCard.setLayout(new BorderLayout(0, 10));
        tableCard.add(scrollPane, BorderLayout.CENTER);
        tableCard.add(bottomBar, BorderLayout.SOUTH);

        // Assemble Content
        JPanel mainContainer = new JPanel(new BorderLayout(0, 16));
        mainContainer.setOpaque(false);
        mainContainer.add(topBar, BorderLayout.NORTH);
        mainContainer.add(tableCard, BorderLayout.CENTER);

        setContent(mainContainer);
        setHeaderIcon(PawIcons.calendar(24, PawTheme.SKY));
        setHeaderColor(PawTheme.SKY_TEXT);

        // Listeners & Initial Load
        initListeners();
        loadDoctorsDropdown();
        loadAppointments();
    }

    private static String getAdaptiveTitle() {
        Session s = Session.getInstance();
        if (s.isOwner()) {
            return "My Pet Appointments";
        } else if (s.isVet()) {
            return "Clinical Consultations & Schedule";
        } else {
            return "Clinic Appointments & Scheduling";
        }
    }

    private static String getAdaptiveSubtitle() {
        Session s = Session.getInstance();
        if (s.isOwner()) {
            return "View scheduled clinic consultations, check appointment status, or book a doctor";
        } else if (s.isVet()) {
            return "Manage daily patient consultations, confirm bookings, and mark clinic visits completed";
        } else {
            return "Platform-wide appointment schedule, booking conflicts, and status workflows";
        }
    }

    private JPanel createTopBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 8));
        bar.setBackground(PawTheme.SURFACE);
        bar.setBorder(new CompoundBorder(
                new LineBorder(PawTheme.BORDER_LIGHT, 1, true),
                new EmptyBorder(12, 16, 12, 16)
        ));

        if (session.isVet()) {
            JLabel doctorBadge = new JLabel("Doctor: " + (session.getName() != null ? session.getName() : "Dr. Sarah"));
            doctorBadge.setFont(PawTheme.FONT_BODY_BOLD);
            doctorBadge.setForeground(PawTheme.SKY);
            bar.add(doctorBadge);

            JSeparator sep0 = new JSeparator(SwingConstants.VERTICAL);
            sep0.setPreferredSize(new Dimension(1, 26));
            sep0.setForeground(PawTheme.BORDER_LIGHT);
            bar.add(sep0);
        } else {
            // For Owner or Admin, show doctor filter dropdown
            JLabel docLabel = new JLabel("Doctor:");
            docLabel.setFont(PawTheme.FONT_BODY_BOLD);
            docLabel.setForeground(PawTheme.TEXT_BODY);
            bar.add(docLabel);
            doctorFilterComboBox.setPreferredSize(new Dimension(180, 38));
            bar.add(doctorFilterComboBox);
        }

        JLabel filterLabel = new JLabel("Date:");
        filterLabel.setFont(PawTheme.FONT_BODY_BOLD);
        filterLabel.setForeground(PawTheme.TEXT_BODY);
        bar.add(filterLabel);

        dateQuickSelect.setPreferredSize(new Dimension(150, 38));
        bar.add(dateQuickSelect);

        dateField.setPreferredSize(new Dimension(160, 38));
        bar.add(dateField);

        bar.add(loadButton);

        return bar;
    }

    private JPanel createBottomBar() {
        JPanel bar = new JPanel(new BorderLayout(14, 0));
        bar.setBackground(PawTheme.SURFACE);
        bar.setBorder(new CompoundBorder(
                new LineBorder(PawTheme.BORDER_LIGHT, 1, true),
                new EmptyBorder(12, 18, 12, 18)
        ));

        bar.add(statusHintLabel, BorderLayout.WEST);

        JPanel actionGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        actionGroup.setOpaque(false);

        // Owners only cancel; Vets and Admins can Confirm, Complete, and Cancel
        if (!session.isOwner()) {
            actionGroup.add(confirmButton);
            actionGroup.add(completeButton);
        }
        actionGroup.add(cancelButton);

        bar.add(actionGroup, BorderLayout.EAST);
        return bar;
    }

    private void setupTableRenderers() {
        // Modern Pill Status Badge Renderer
        appointmentTable.getColumnModel().getColumn(5).setCellRenderer(new PawComponents.StatusTableCellRenderer());

        // Column widths
        appointmentTable.getColumnModel().getColumn(0).setPreferredWidth(60);  // ID
        appointmentTable.getColumnModel().getColumn(1).setPreferredWidth(120); // Pet
        appointmentTable.getColumnModel().getColumn(2).setPreferredWidth(130); // Owner
        appointmentTable.getColumnModel().getColumn(3).setPreferredWidth(140); // Doctor
        appointmentTable.getColumnModel().getColumn(4).setPreferredWidth(180); // Time Slot
        appointmentTable.getColumnModel().getColumn(5).setPreferredWidth(110); // Status
    }

    private void initListeners() {
        dateQuickSelect.addActionListener(e -> {
            String selected = (String) dateQuickSelect.getSelectedItem();
            if ("Today".equals(selected)) {
                dateField.setDate(LocalDate.now());
                dateField.setEnabled(false);
            } else if ("Tomorrow".equals(selected)) {
                dateField.setDate(LocalDate.now().plusDays(1));
                dateField.setEnabled(false);
            } else if ("Next 7 Days".equals(selected) || "All Dates".equals(selected)) {
                dateField.setDate(null);
                dateField.setEnabled(false);
            } else {
                dateField.setEnabled(true);
                if (dateField.getDate() == null) {
                    dateField.setDate(LocalDate.now());
                }
            }
            loadAppointments();
        });

        dateField.addDateChangeListener(d -> loadAppointments());

        loadButton.addActionListener(e -> loadAppointments());

        appointmentTable.getSelectionModel().addListSelectionListener(e -> {
            boolean hasSel = appointmentTable.getSelectedRow() >= 0;
            confirmButton.setEnabled(hasSel);
            completeButton.setEnabled(hasSel);
            cancelButton.setEnabled(hasSel);
        });

        confirmButton.addActionListener(e -> updateSelectedStatus("CONFIRMED"));
        completeButton.addActionListener(e -> updateSelectedStatus("COMPLETED"));
        cancelButton.addActionListener(e -> updateSelectedStatus("CANCELLED"));

        bookNewButton.addActionListener(e -> {
            Window parentWindow = SwingUtilities.getWindowAncestor(this);
            Frame parentFrame = parentWindow instanceof Frame ? (Frame) parentWindow : null;
            BookAppointmentDialog dialog = new BookAppointmentDialog(parentFrame, this::loadAppointments);
            dialog.setVisible(true);
        });
    }

    private void loadDoctorsDropdown() {
        if (session.isVet()) return;

        SwingWorker<List<UserDTO>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<UserDTO> doInBackground() throws Exception {
                return ApiClient.getInstance().getList("/api/vets", UserDTO.class);
            }

            @Override
            protected void done() {
                try {
                    List<UserDTO> vets = get();
                    doctorFilterComboBox.removeAllItems();
                    doctorIndexToId.clear();

                    doctorFilterComboBox.addItem("All Doctors");
                    doctorIndexToId.put(0, null);

                    int idx = 1;
                    for (UserDTO v : vets) {
                        doctorFilterComboBox.addItem(v.getName());
                        doctorIndexToId.put(idx++, v.getId());
                    }
                } catch (Exception ignored) {}
            }
        };
        worker.execute();
    }

    public void loadAppointments() {
        StringBuilder url = new StringBuilder("/api/appointments");
        List<String> params = new ArrayList<>();

        if (session.isOwner()) {
            params.add("ownerId=" + session.getUserId());
        } else if (session.isVet()) {
            params.add("vetId=" + session.getUserId());
        } else {
            // Admin: check selected doctor in dropdown
            int selIdx = doctorFilterComboBox.getSelectedIndex();
            Long vetId = doctorIndexToId.get(selIdx);
            if (vetId != null) {
                params.add("vetId=" + vetId);
            }
        }

        String selectedOption = (String) dateQuickSelect.getSelectedItem();
        if ("Today".equals(selectedOption)) {
            params.add("date=today");
        } else if ("Tomorrow".equals(selectedOption)) {
            params.add("date=tomorrow");
        } else if ("Custom Date".equals(selectedOption)) {
            String dateText = dateField.getText().trim();
            if (!dateText.isEmpty()) {
                try {
                    LocalDate.parse(dateText);
                    params.add("date=" + dateText);
                } catch (Exception ignored) {}
            }
        }
        // "All Dates" and "Next 7 Days" fetch all relevant appointments without single-date restriction

        if (!params.isEmpty()) {
            url.append("?").append(String.join("&", params));
        }

        loadButton.setEnabled(false);
        loadButton.setText("Loading...");
        statusHintLabel.setText("Fetching appointments...");

        final String requestUrl = url.toString();

        SwingWorker<List<AppointmentDTO>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<AppointmentDTO> doInBackground() throws Exception {
                return ApiClient.getInstance().getList(requestUrl, AppointmentDTO.class);
            }

            @Override
            protected void done() {
                loadButton.setEnabled(true);
                loadButton.setText("Refresh");
                try {
                    List<AppointmentDTO> list = get();

                    // If Owner selected a specific doctor in the dropdown, filter client-side:
                    if (session.isOwner()) {
                        int selIdx = doctorFilterComboBox.getSelectedIndex();
                        Long filterVetId = doctorIndexToId.get(selIdx);
                        if (filterVetId != null) {
                            list = list.stream().filter(a -> filterVetId.equals(a.getVetId())).toList();
                        }
                    }

                    tableModel.setItems(list);

                    // Update subtle status label - NO ANNOYING MODAL POPUP!
                    if (list.isEmpty()) {
                        statusHintLabel.setText("No appointments found for the selected criteria.");
                    } else {
                        statusHintLabel.setText(list.size() + " appointment(s) displayed. Select a row to update.");
                    }
                } catch (Exception ex) {
                    statusHintLabel.setText("Failed to load appointments: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void updateSelectedStatus(String newStatus) {
        int row = appointmentTable.getSelectedRow();
        if (row < 0) {
            showError("Please select an appointment from the table.");
            return;
        }

        AppointmentDTO appt = tableModel.getItem(row);
        if (appt == null || appt.getId() == null) {
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Change status of Appointment #" + appt.getId() + " (" + appt.getPetName() + ") to " + newStatus + "?",
                "Confirm Status Change",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        Map<String, String> body = new HashMap<>();
        body.put("status", newStatus);

        SwingWorker<AppointmentDTO, Void> worker = new SwingWorker<>() {
            @Override
            protected AppointmentDTO doInBackground() throws Exception {
                return ApiClient.getInstance().patch("/api/appointments/" + appt.getId() + "/status", body, AppointmentDTO.class);
            }

            @Override
            protected void done() {
                try {
                    AppointmentDTO updated = get();
                    showSuccess("Appointment #" + updated.getId() + " successfully set to " + updated.getStatus());
                    loadAppointments(); // Refresh table on success
                } catch (Exception ex) {
                    showError("Failed to update status: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    @Override
    public void refresh() {
        loadAppointments();
    }

    /**
     * Table Model mapping AppointmentDTOs to columns.
     */
    private static class AppointmentTableModel extends BaseTableModel<AppointmentDTO> {

        public AppointmentTableModel() {
            super(new String[]{"ID", "Pet", "Owner", "Doctor / Vet", "Time Slot", "Status"});
        }

        @Override
        public Object getValueAt(AppointmentDTO a, int columnIndex) {
            if (a == null) return null;
            return switch (columnIndex) {
                case 0 -> a.getId();
                case 1 -> a.getPetName() != null ? a.getPetName() : "Pet #" + a.getPetId();
                case 2 -> a.getOwnerName() != null ? a.getOwnerName() : "Owner #" + a.getOwnerId();
                case 3 -> a.getVetName() != null ? a.getVetName() : "Dr. #" + a.getVetId();
                case 4 -> a.getTimeSlotFormatted();
                case 5 -> a.getStatus();
                default -> null;
            };
        }
    }
}
