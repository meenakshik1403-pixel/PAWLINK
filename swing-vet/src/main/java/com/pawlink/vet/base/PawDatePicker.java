package com.pawlink.vet.base;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

/**
 * Modern Android-inspired Material Date Picker for Swing.
 * Features:
 * - Direct text editing and keyboard navigation
 * - One-click calendar modal with Android Material visual layout
 * - Prominent Year/Month quick-selection views (jump years 1980..2045 effortlessly)
 * - Pure Java2D vector styling matching PawLink's candy pastel aesthetics
 */
public class PawDatePicker extends JPanel {

    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DISPLAY_HEADER_FORMATTER = DateTimeFormatter.ofPattern("EEE, MMM d", Locale.ENGLISH);

    private final JTextField textField;
    private final JButton calendarButton;
    private LocalDate selectedDate;
    private Color accentColor = PawTheme.PRIMARY;
    private final List<Consumer<LocalDate>> listeners = new ArrayList<>();

    public PawDatePicker() {
        this(LocalDate.now());
    }

    public PawDatePicker(LocalDate initialDate, Color accentColor) {
        this(initialDate);
        setAccentColor(accentColor);
    }

    public PawDatePicker(LocalDate initialDate) {
        super(new BorderLayout(6, 0));
        setOpaque(false);

        this.selectedDate = initialDate;

        this.textField = new JTextField(10);
        PawTheme.styleField(textField);
        this.textField.setText(selectedDate != null ? selectedDate.format(ISO_FORMATTER) : "");

        this.calendarButton = new JButton(PawIcons.calendar(15, PawTheme.PRIMARY));
        this.calendarButton.setFocusable(false);
        this.calendarButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        this.calendarButton.setContentAreaFilled(false);
        this.calendarButton.setBorder(new EmptyBorder(4, 6, 4, 6));
        this.calendarButton.setToolTipText("Open Calendar Date Picker");

        // Open dialog when button is clicked or user clicks on field
        this.calendarButton.addActionListener(e -> openPicker());
        this.textField.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1 && !textField.isFocusOwner()) {
                    openPicker();
                }
            }
        });

        // Listen for manual text changes
        this.textField.addActionListener(e -> {
            try {
                String text = textField.getText().trim();
                if (!text.isEmpty()) {
                    LocalDate parsed = LocalDate.parse(text, ISO_FORMATTER);
                    setDate(parsed);
                }
            } catch (DateTimeParseException ignored) {
            }
        });

        add(textField, BorderLayout.CENTER);
        add(calendarButton, BorderLayout.EAST);
    }

    public void setAccentColor(Color color) {
        if (color != null) {
            this.accentColor = color;
            this.calendarButton.setIcon(PawIcons.calendar(15, color));
        }
    }

    public LocalDate getDate() {
        try {
            String text = textField.getText().trim();
            if (!text.isEmpty()) {
                return LocalDate.parse(text, ISO_FORMATTER);
            }
        } catch (DateTimeParseException ignored) {
        }
        return selectedDate;
    }

    public void setDate(LocalDate date) {
        this.selectedDate = date;
        if (date != null) {
            this.textField.setText(date.format(ISO_FORMATTER));
        } else {
            this.textField.setText("");
        }
        notifyListeners();
    }

    public String getText() {
        return textField.getText().trim();
    }

    public void setText(String text) {
        if (text == null || text.trim().isEmpty()) {
            setDate(null);
            return;
        }
        try {
            LocalDate parsed = LocalDate.parse(text.trim(), ISO_FORMATTER);
            setDate(parsed);
        } catch (DateTimeParseException ex) {
            this.textField.setText(text.trim());
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        textField.setEnabled(enabled);
        calendarButton.setEnabled(enabled);
    }

    public void setEditable(boolean editable) {
        textField.setEditable(editable);
    }

    @Override
    public void setToolTipText(String text) {
        super.setToolTipText(text);
        textField.setToolTipText(text);
        calendarButton.setToolTipText(text);
    }

    @Override
    public void setPreferredSize(Dimension preferredSize) {
        super.setPreferredSize(preferredSize);
        textField.setPreferredSize(new Dimension(preferredSize.width - 34, preferredSize.height));
    }

    public JTextField getTextField() {
        return textField;
    }

    public void addDateChangeListener(Consumer<LocalDate> listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }

    private void notifyListeners() {
        for (Consumer<LocalDate> listener : listeners) {
            listener.accept(selectedDate);
        }
    }

    private void openPicker() {
        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        LocalDate initial = getDate() != null ? getDate() : LocalDate.now();
        DatePickerDialog dialog = new DatePickerDialog(parentWindow, initial, accentColor, this::setDate);
        dialog.setVisible(true);
    }

    /**
     * Android-style Material DatePickerDialog modal window.
     */
    private static class DatePickerDialog extends JDialog {

        private LocalDate workingDate;
        private YearMonth currentViewingMonth;
        private final Consumer<LocalDate> onConfirm;
        private final Color accentColor;

        private JLabel yearHeaderLabel;
        private JLabel dateHeaderLabel;

        private CardLayout cardLayout;
        private JPanel cardContainer;
        private JPanel daysGridPanel;
        private JButton monthYearNavButton;

        public DatePickerDialog(Window parent, LocalDate initialDate, Color accentColor, Consumer<LocalDate> onConfirm) {
            super(parent, "Select Date", ModalityType.APPLICATION_MODAL);
            this.workingDate = initialDate != null ? initialDate : LocalDate.now();
            this.currentViewingMonth = YearMonth.from(this.workingDate);
            this.accentColor = accentColor != null ? accentColor : PawTheme.PRIMARY;
            this.onConfirm = onConfirm;

            initUI();
        }

        private void initUI() {
            setSize(380, 510);
            setLocationRelativeTo(getParent());
            setResizable(false);
            setLayout(new BorderLayout());

            // 1. Android-style Material Header Banner
            JPanel headerBanner = new JPanel();
            headerBanner.setLayout(new BoxLayout(headerBanner, BoxLayout.Y_AXIS));
            headerBanner.setBackground(PawTheme.SIDEBAR_BG);
            headerBanner.setBorder(new EmptyBorder(18, 22, 16, 22));

            yearHeaderLabel = new JLabel(String.valueOf(workingDate.getYear()));
            yearHeaderLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
            yearHeaderLabel.setForeground(PawTheme.TEXT_SUBTLE);
            yearHeaderLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            yearHeaderLabel.setToolTipText("Click to select year");
            yearHeaderLabel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    cardLayout.show(cardContainer, "YEAR_MONTH");
                }
            });
            headerBanner.add(yearHeaderLabel);
            headerBanner.add(Box.createVerticalStrut(4));

            dateHeaderLabel = new JLabel(workingDate.format(DISPLAY_HEADER_FORMATTER));
            dateHeaderLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
            dateHeaderLabel.setForeground(Color.WHITE);
            dateHeaderLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            dateHeaderLabel.setToolTipText("Click to return to calendar days");
            dateHeaderLabel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    cardLayout.show(cardContainer, "CALENDAR");
                }
            });
            headerBanner.add(dateHeaderLabel);

            add(headerBanner, BorderLayout.NORTH);

            // 2. Card Container (Switch between Calendar View and Year/Month View)
            cardLayout = new CardLayout();
            cardContainer = new JPanel(cardLayout);
            cardContainer.setBackground(PawTheme.CANVAS);

            JPanel calendarCard = createCalendarCard();
            JPanel yearMonthCard = createYearMonthCard();

            cardContainer.add(calendarCard, "CALENDAR");
            cardContainer.add(yearMonthCard, "YEAR_MONTH");
            add(cardContainer, BorderLayout.CENTER);

            // 3. Footer Action Buttons
            JPanel footer = new JPanel(new BorderLayout());
            footer.setBackground(PawTheme.SURFACE);
            footer.setBorder(new CompoundBorder(
                    BorderFactory.createMatteBorder(1, 0, 0, 0, PawTheme.BORDER_LIGHT),
                    new EmptyBorder(10, 16, 10, 16)
            ));

            JButton todayButton = new PawComponents.PawButton("Today", PawComponents.ButtonStyle.SECONDARY);
            todayButton.setFont(PawTheme.FONT_SMALL_BOLD);
            todayButton.addActionListener(e -> {
                workingDate = LocalDate.now();
                currentViewingMonth = YearMonth.from(workingDate);
                updateHeaders();
                refreshCalendarDays();
            });
            footer.add(todayButton, BorderLayout.WEST);

            JPanel rightButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
            rightButtons.setOpaque(false);

            JButton cancelButton = new PawComponents.PawButton("Cancel", PawComponents.ButtonStyle.SECONDARY);
            cancelButton.addActionListener(e -> dispose());
            rightButtons.add(cancelButton);

            JButton okButton = new PawComponents.PawButton("OK", PawIcons.check(14, Color.WHITE), PawComponents.ButtonStyle.PRIMARY);
            okButton.addActionListener(e -> {
                if (onConfirm != null) {
                    onConfirm.accept(workingDate);
                }
                dispose();
            });
            rightButtons.add(okButton);

            footer.add(rightButtons, BorderLayout.EAST);
            add(footer, BorderLayout.SOUTH);

            // Initial populate
            refreshCalendarDays();
        }

        private JPanel createCalendarCard() {
            JPanel panel = new JPanel(new BorderLayout(0, 8));
            panel.setBackground(PawTheme.SURFACE);
            panel.setBorder(new EmptyBorder(12, 16, 10, 16));

            // Navigation Bar (< Month Year >)
            JPanel navBar = new JPanel(new BorderLayout());
            navBar.setOpaque(false);

            JButton prevButton = new JButton("<");
            prevButton.setFont(PawTheme.FONT_BODY_BOLD);
            prevButton.setFocusable(false);
            prevButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            prevButton.setContentAreaFilled(false);
            prevButton.setBorder(new EmptyBorder(6, 12, 6, 12));
            prevButton.addActionListener(e -> {
                currentViewingMonth = currentViewingMonth.minusMonths(1);
                refreshCalendarDays();
            });
            navBar.add(prevButton, BorderLayout.WEST);

            monthYearNavButton = new JButton();
            monthYearNavButton.setFont(PawTheme.FONT_BODY_BOLD);
            monthYearNavButton.setForeground(PawTheme.TEXT_TITLE);
            monthYearNavButton.setFocusable(false);
            monthYearNavButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            monthYearNavButton.setContentAreaFilled(false);
            monthYearNavButton.setBorder(new EmptyBorder(6, 12, 6, 12));
            monthYearNavButton.setToolTipText("Click to jump to Month/Year selector");
            monthYearNavButton.addActionListener(e -> cardLayout.show(cardContainer, "YEAR_MONTH"));
            navBar.add(monthYearNavButton, BorderLayout.CENTER);

            JButton nextButton = new JButton(">");
            nextButton.setFont(PawTheme.FONT_BODY_BOLD);
            nextButton.setFocusable(false);
            nextButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            nextButton.setContentAreaFilled(false);
            nextButton.setBorder(new EmptyBorder(6, 12, 6, 12));
            nextButton.addActionListener(e -> {
                currentViewingMonth = currentViewingMonth.plusMonths(1);
                refreshCalendarDays();
            });
            navBar.add(nextButton, BorderLayout.EAST);

            panel.add(navBar, BorderLayout.NORTH);

            // Weekday Headers & Days Container
            JPanel gridContainer = new JPanel(new BorderLayout(0, 4));
            gridContainer.setOpaque(false);

            JPanel weekdaysHeader = new JPanel(new GridLayout(1, 7, 2, 0));
            weekdaysHeader.setOpaque(false);
            String[] days = {"S", "M", "T", "W", "T", "F", "S"};
            for (String d : days) {
                JLabel lbl = new JLabel(d, SwingConstants.CENTER);
                lbl.setFont(PawTheme.FONT_SMALL_BOLD);
                lbl.setForeground(PawTheme.TEXT_MUTED);
                weekdaysHeader.add(lbl);
            }
            gridContainer.add(weekdaysHeader, BorderLayout.NORTH);

            daysGridPanel = new JPanel(new GridLayout(6, 7, 4, 4));
            daysGridPanel.setOpaque(false);
            gridContainer.add(daysGridPanel, BorderLayout.CENTER);

            panel.add(gridContainer, BorderLayout.CENTER);
            return panel;
        }

        private JPanel createYearMonthCard() {
            JPanel panel = new JPanel(new BorderLayout(0, 10));
            panel.setBackground(PawTheme.SURFACE);
            panel.setBorder(new EmptyBorder(12, 16, 12, 16));

            JLabel title = new JLabel("Select Year & Month", SwingConstants.CENTER);
            title.setFont(PawTheme.FONT_BODY_BOLD);
            title.setForeground(PawTheme.TEXT_TITLE);
            panel.add(title, BorderLayout.NORTH);

            // Center: 12 Month Pills at Top, Scrollable Years Grid at Bottom
            JPanel center = new JPanel(new BorderLayout(0, 12));
            center.setOpaque(false);

            // Months (12 pills)
            JPanel monthsGrid = new JPanel(new GridLayout(3, 4, 6, 6));
            monthsGrid.setOpaque(false);
            String[] monthNames = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
            for (int i = 1; i <= 12; i++) {
                final int m = i;
                JButton mBtn = new JButton(monthNames[i - 1]);
                mBtn.setFont(PawTheme.FONT_SMALL_BOLD);
                mBtn.setFocusable(false);
                mBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                mBtn.setBackground(currentViewingMonth.getMonthValue() == m ? PawTheme.SKY_LIGHT : PawTheme.SURFACE);
                mBtn.setForeground(currentViewingMonth.getMonthValue() == m ? PawTheme.SKY_TEXT : PawTheme.TEXT_BODY);
                mBtn.setBorder(new LineBorder(currentViewingMonth.getMonthValue() == m ? PawTheme.SKY : PawTheme.BORDER_LIGHT, 1, true));
                mBtn.addActionListener(e -> {
                    currentViewingMonth = YearMonth.of(currentViewingMonth.getYear(), m);
                    refreshCalendarDays();
                    cardLayout.show(cardContainer, "CALENDAR");
                });
                monthsGrid.add(mBtn);
            }
            center.add(monthsGrid, BorderLayout.NORTH);

            // Years Grid (from 1980 to 2045)
            JPanel yearsPanel = new JPanel(new GridLayout(0, 4, 6, 6));
            yearsPanel.setBackground(PawTheme.SURFACE);
            yearsPanel.setBorder(new EmptyBorder(4, 4, 4, 4));

            for (int y = 1980; y <= 2045; y++) {
                final int yr = y;
                JButton yBtn = new JButton(String.valueOf(yr));
                yBtn.setFont(PawTheme.FONT_SMALL);
                yBtn.setFocusable(false);
                yBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

                boolean isCurrent = (currentViewingMonth.getYear() == yr);
                yBtn.setBackground(isCurrent ? PawTheme.SUNNY_LIGHT : PawTheme.SURFACE);
                yBtn.setForeground(isCurrent ? PawTheme.SUNNY_TEXT : PawTheme.TEXT_BODY);
                yBtn.setBorder(new LineBorder(isCurrent ? PawTheme.SUNNY : PawTheme.BORDER_LIGHT, 1, true));

                yBtn.addActionListener(e -> {
                    currentViewingMonth = YearMonth.of(yr, currentViewingMonth.getMonthValue());
                    refreshCalendarDays();
                    cardLayout.show(cardContainer, "CALENDAR");
                });
                yearsPanel.add(yBtn);
            }

            JScrollPane scrollPane = new JScrollPane(yearsPanel);
            scrollPane.setBorder(new LineBorder(PawTheme.BORDER_LIGHT, 1, true));
            scrollPane.getVerticalScrollBar().setUnitIncrement(16);
            center.add(scrollPane, BorderLayout.CENTER);

            panel.add(center, BorderLayout.CENTER);
            return panel;
        }

        private void updateHeaders() {
            yearHeaderLabel.setText(String.valueOf(workingDate.getYear()));
            dateHeaderLabel.setText(workingDate.format(DISPLAY_HEADER_FORMATTER));
            if (monthYearNavButton != null) {
                monthYearNavButton.setText(currentViewingMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH)) + "  ▼");
            }
        }

        private void refreshCalendarDays() {
            updateHeaders();
            daysGridPanel.removeAll();

            LocalDate firstOfMonth = currentViewingMonth.atDay(1);
            int dayOfWeek = firstOfMonth.getDayOfWeek().getValue() % 7; // Sunday = 0
            int daysInMonth = currentViewingMonth.lengthOfMonth();
            LocalDate today = LocalDate.now();

            // Leading empty placeholders
            for (int i = 0; i < dayOfWeek; i++) {
                daysGridPanel.add(new JLabel(""));
            }

            // Month days
            for (int d = 1; d <= daysInMonth; d++) {
                final int dayNum = d;
                final LocalDate thisDate = currentViewingMonth.atDay(dayNum);
                final String text = String.valueOf(dayNum);

                boolean isSelected = thisDate.equals(workingDate);
                boolean isToday = thisDate.equals(today);

                JButton dayBtn = new JButton() {
                    @Override
                    protected void paintComponent(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        try {
                            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                            int size = Math.min(getWidth(), getHeight()) - 4;
                            int x = (getWidth() - size) / 2;
                            int y = (getHeight() - size) / 2;

                            if (isSelected) {
                                g2.setColor(accentColor);
                                g2.fillOval(x, y, size, size);
                            } else if (getModel().isRollover()) {
                                g2.setColor(new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), 35));
                                g2.fillOval(x, y, size, size);
                            } else if (isToday) {
                                g2.setColor(accentColor);
                                g2.setStroke(new BasicStroke(1.4f));
                                g2.drawOval(x, y, size, size);
                            }

                            // Render day number with pixel-perfect centering (never truncates to '...')
                            g2.setFont(new Font("Segoe UI", isSelected || isToday ? Font.BOLD : Font.PLAIN, 13));
                            g2.setColor(isSelected ? Color.WHITE : (isToday ? accentColor : PawTheme.TEXT_TITLE));
                            FontMetrics fm = g2.getFontMetrics();
                            int tx = (getWidth() - fm.stringWidth(text)) / 2;
                            int ty = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                            g2.drawString(text, tx, ty);
                        } finally {
                            g2.dispose();
                        }
                    }
                };

                dayBtn.setMargin(new Insets(0, 0, 0, 0));
                dayBtn.setBorder(new EmptyBorder(0, 0, 0, 0));
                dayBtn.setFocusable(false);
                dayBtn.setContentAreaFilled(false);
                dayBtn.setBorderPainted(false);
                dayBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

                dayBtn.addActionListener(e -> {
                    workingDate = thisDate;
                    updateHeaders();
                    refreshCalendarDays();
                });

                // Double click to confirm
                dayBtn.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (e.getClickCount() == 2) {
                            workingDate = thisDate;
                            if (onConfirm != null) {
                                onConfirm.accept(workingDate);
                            }
                            dispose();
                        }
                    }
                });

                daysGridPanel.add(dayBtn);
            }

            // Trailing cells to fill 42 total slots
            int totalCells = dayOfWeek + daysInMonth;
            for (int i = totalCells; i < 42; i++) {
                daysGridPanel.add(new JLabel(""));
            }

            daysGridPanel.revalidate();
            daysGridPanel.repaint();
        }
    }
}
