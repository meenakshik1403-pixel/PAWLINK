package com.pawlink.vet.base;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Modern, tactile Date & Time Selector for PawLink.
 * Combines the Android Material PawDatePicker with an adaptive time selector.
 * Supports future-time enforcement to prevent booking past slots.
 */
public class PawDateTimePicker extends JPanel {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private final PawDatePicker datePicker;
    private final JComboBox<String> timeComboBox;
    private final JLabel clockIcon;
    private boolean enforceFuture = false;
    private boolean updatingTimes = false;
    private Color accentColor = PawTheme.PRIMARY;
    private final List<Consumer<LocalDateTime>> listeners = new ArrayList<>();

    public PawDateTimePicker() {
        this(LocalDateTime.now().plusHours(1).withMinute(0).withSecond(0), PawTheme.PRIMARY, false);
    }

    public PawDateTimePicker(LocalDateTime initialDateTime, Color accentColor, boolean enforceFuture) {
        super(new BorderLayout(8, 0));
        setOpaque(false);
        this.accentColor = accentColor != null ? accentColor : PawTheme.PRIMARY;
        this.enforceFuture = enforceFuture;

        LocalDateTime init = initialDateTime != null ? initialDateTime : LocalDateTime.now();

        // 1. Date Picker component
        this.datePicker = new PawDatePicker(init.toLocalDate(), this.accentColor);
        add(datePicker, BorderLayout.CENTER);

        // 2. Time Selector Panel
        JPanel timePanel = new JPanel(new BorderLayout(4, 0));
        timePanel.setOpaque(false);

        clockIcon = new JLabel(PawIcons.clock(15, this.accentColor));
        clockIcon.setBorder(new EmptyBorder(0, 4, 0, 2));
        timePanel.add(clockIcon, BorderLayout.WEST);

        timeComboBox = new JComboBox<>();
        timeComboBox.setFont(PawTheme.FONT_BODY);
        timeComboBox.setPreferredSize(new Dimension(95, 38));
        timeComboBox.setEditable(true);
        timePanel.add(timeComboBox, BorderLayout.CENTER);

        add(timePanel, BorderLayout.EAST);

        // Wire up date change to time options
        datePicker.addDateChangeListener(date -> {
            if (this.enforceFuture && date != null && date.isBefore(LocalDate.now())) {
                datePicker.setDate(LocalDate.now());
                return;
            }
            updateAvailableTimes();
            notifyListeners();
        });

        timeComboBox.addActionListener(e -> {
            if (!updatingTimes) {
                notifyListeners();
            }
        });

        // Initial populate of times
        updateAvailableTimes();

        // Select initial time
        String timeStr = init.toLocalTime().format(TIME_FORMATTER);
        timeComboBox.setSelectedItem(timeStr);
    }

    public void setEnforceFuture(boolean enforce) {
        this.enforceFuture = enforce;
        updateAvailableTimes();
    }

    public void updateAvailableTimes() {
        updatingTimes = true;
        try {
            String currentSelection = (String) timeComboBox.getSelectedItem();
            timeComboBox.removeAllItems();

            LocalDate selectedDate = datePicker.getDate();
            LocalDate today = LocalDate.now();
            LocalTime now = LocalTime.now();

            for (int h = 0; h < 24; h++) {
                for (int m = 0; m < 60; m += 30) {
                    LocalTime t = LocalTime.of(h, m);
                    if (enforceFuture && selectedDate != null && selectedDate.isEqual(today)) {
                        // Only slots strictly in the future for today
                        if (t.isBefore(now)) {
                            continue;
                        }
                    }
                    timeComboBox.addItem(t.format(TIME_FORMATTER));
                }
            }

            // If today has no slots remaining and enforceFuture is on, auto-advance to tomorrow
            if (enforceFuture && selectedDate != null && selectedDate.isEqual(today) && timeComboBox.getItemCount() == 0) {
                datePicker.setDate(today.plusDays(1));
                return;
            }

            // Restore selection if valid, or default to first available
            if (currentSelection != null && !currentSelection.trim().isEmpty()) {
                boolean found = false;
                for (int i = 0; i < timeComboBox.getItemCount(); i++) {
                    if (currentSelection.equals(timeComboBox.getItemAt(i))) {
                        timeComboBox.setSelectedIndex(i);
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    timeComboBox.setSelectedItem(currentSelection);
                }
            } else if (timeComboBox.getItemCount() > 0) {
                timeComboBox.setSelectedIndex(0);
            }
        } finally {
            updatingTimes = false;
        }
    }

    public LocalDateTime getDateTime() {
        LocalDate date = datePicker.getDate();
        if (date == null) return null;

        String timeStr = (String) timeComboBox.getSelectedItem();
        if (timeStr == null || timeStr.trim().isEmpty()) {
            return date.atStartOfDay();
        }

        try {
            LocalTime time = LocalTime.parse(timeStr.trim(), TIME_FORMATTER);
            return LocalDateTime.of(date, time);
        } catch (DateTimeParseException ex) {
            // Lenient fallback for manual H:mm entry
            try {
                String[] parts = timeStr.trim().split(":");
                int h = Integer.parseInt(parts[0]);
                int m = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
                return LocalDateTime.of(date, LocalTime.of(h, m));
            } catch (Exception e) {
                return date.atTime(12, 0);
            }
        }
    }

    public void setDateTime(LocalDateTime dt) {
        if (dt == null) return;
        datePicker.setDate(dt.toLocalDate());
        updateAvailableTimes();
        String timeStr = dt.toLocalTime().format(TIME_FORMATTER);
        timeComboBox.setSelectedItem(timeStr);
        notifyListeners();
    }

    public PawDatePicker getDatePicker() {
        return datePicker;
    }

    public JComboBox<String> getTimeComboBox() {
        return timeComboBox;
    }

    public void addDateTimeChangeListener(Consumer<LocalDateTime> listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }

    private void notifyListeners() {
        LocalDateTime dt = getDateTime();
        for (Consumer<LocalDateTime> listener : listeners) {
            listener.accept(dt);
        }
    }

    public void setAccentColor(Color color) {
        if (color != null) {
            this.accentColor = color;
            this.datePicker.setAccentColor(color);
            this.clockIcon.setIcon(PawIcons.clock(15, color));
        }
    }
}
