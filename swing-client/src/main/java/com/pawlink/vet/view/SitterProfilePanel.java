package com.pawlink.vet.view;

import com.pawlink.vet.api.ApiClient;
import com.pawlink.vet.api.Session;
import com.pawlink.vet.base.BasePanel;
import com.pawlink.vet.base.PawComponents;
import com.pawlink.vet.base.PawIcons;
import com.pawlink.vet.base.PawTheme;
import com.pawlink.vet.model.SitterDTO;
import com.pawlink.vet.model.SittingBookingDTO;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Profile panel for pet sitters to review verification status and stats.
 * Modernized with pure Java2D vector icons, status pills, and high-contrast typography.
 */
public class SitterProfilePanel extends BasePanel {

    private final JLabel nameLabel = new JLabel("-");
    private final JLabel emailLabel = new JLabel("-");
    private final JLabel phoneLabel = new JLabel("-");
    private final JLabel rateLabel = new JLabel("-");
    private final JPanel statusBadgeHolder = new JPanel(new FlowLayout(FlowLayout.CENTER));
    private final JLabel totalJobsLabel = new JLabel("0");

    public SitterProfilePanel() {
        super("Sitter Profile & Credentials", "Verify your credentials, review your published rate, and track platform jobs.");

        setHeaderIcon(PawIcons.user(26, PawTheme.LILAC));
        setHeaderColor(PawTheme.LILAC_TEXT);

        setupUI();
        loadProfile();
    }

    private void setupUI() {
        JPanel accessory = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        accessory.setOpaque(false);
        PawComponents.PawButton refreshBtn = new PawComponents.PawButton("Refresh", PawIcons.refresh(13, PawTheme.TEXT_BODY), PawComponents.ButtonStyle.SECONDARY);
        refreshBtn.addActionListener(e -> loadProfile());
        accessory.add(refreshBtn);
        setHeaderAccessory(accessory);

        statusBadgeHolder.setOpaque(false);

        PawComponents.PawCard mainCard = new PawComponents.PawCard(16);
        mainCard.setBackgroundTint(PawTheme.SUNNY_LIGHT);
        mainCard.setBorderTint(PawTheme.SUNNY);
        mainCard.setLayout(new GridBagLayout());
        mainCard.setPreferredSize(new Dimension(580, 420));

        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(12, 24, 12, 24);
        g.gridx = 0;
        g.gridy = 0;

        // Status Badge at Top
        g.gridwidth = 2;
        mainCard.add(statusBadgeHolder, g);

        // Fields
        g.gridwidth = 1;
        g.gridy = 1;
        addField(mainCard, g, "Full Name:", nameLabel);

        g.gridy = 2;
        addField(mainCard, g, "Email Address:", emailLabel);

        g.gridy = 3;
        addField(mainCard, g, "Phone Contact:", phoneLabel);

        g.gridy = 4;
        addField(mainCard, g, "Approved Hourly Rate:", rateLabel);

        g.gridy = 5;
        addField(mainCard, g, "Total Completed Jobs:", totalJobsLabel);

        JPanel container = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 28));
        container.setOpaque(false);
        container.add(mainCard);

        setContent(container);
    }

    private void addField(JPanel panel, GridBagConstraints g, String title, JLabel value) {
        g.gridx = 0;
        JLabel l = new JLabel(title);
        l.setFont(PawTheme.FONT_BODY_BOLD);
        l.setForeground(PawTheme.TEXT_MUTED);
        panel.add(l, g);

        g.gridx = 1;
        value.setFont(PawTheme.FONT_BODY);
        value.setForeground(PawTheme.TEXT_TITLE);
        panel.add(value, g);
    }

    @Override
    public void refresh() {
        loadProfile();
    }

    public void loadProfile() {
        Long userId = Session.getInstance().getUserId();
        if (userId == null) return;

        SwingWorker<SitterDTO, Void> worker = new SwingWorker<>() {
            @Override
            protected SitterDTO doInBackground() {
                try {
                    return ApiClient.getInstance().get("/api/sitters/" + userId, SitterDTO.class);
                } catch (Exception ex) {
                    List<SitterDTO> list = ApiClient.getInstance().getList("/api/sitters", SitterDTO.class);
                    for (SitterDTO s : list) {
                        if (userId.equals(s.getUserId())) return s;
                    }
                    throw ex;
                }
            }

            @Override
            protected void done() {
                try {
                    SitterDTO s = get();
                    nameLabel.setText(s.getName());
                    emailLabel.setText(s.getEmail());
                    phoneLabel.setText(s.getPhone() != null ? s.getPhone() : "Not provided");
                    rateLabel.setText("₹" + (s.getHourlyRate() != null ? s.getHourlyRate() : "250.00") + " / hour");

                    statusBadgeHolder.removeAll();
                    if (s.isVerified()) {
                        statusBadgeHolder.add(new PawComponents.StatusPill("VERIFIED PET SITTER (Eligible for bookings)",
                                PawTheme.SUCCESS_LIGHT, PawTheme.SUCCESS_TEXT, PawTheme.SUCCESS));
                    } else {
                        statusBadgeHolder.add(new PawComponents.StatusPill("PENDING VERIFICATION (Awaiting Admin Approval)",
                                PawTheme.WARNING_LIGHT, PawTheme.WARNING_TEXT, PawTheme.WARNING));
                    }
                    statusBadgeHolder.revalidate();
                    statusBadgeHolder.repaint();

                    // Count jobs
                    List<SittingBookingDTO> jobs = ApiClient.getInstance().getList("/api/sitter-bookings?sitterId=" + userId, SittingBookingDTO.class);
                    totalJobsLabel.setText(jobs.size() + " job(s)");
                } catch (Exception ex) {
                    nameLabel.setText(Session.getInstance().getName() != null ? Session.getInstance().getName() : "Sitter");
                    emailLabel.setText(Session.getInstance().getEmail() != null ? Session.getInstance().getEmail() : "-");
                    statusBadgeHolder.removeAll();
                    statusBadgeHolder.add(new PawComponents.StatusPill("STATUS PENDING", PawTheme.SURFACE_MUTED, PawTheme.TEXT_MUTED, PawTheme.TEXT_MUTED));
                    statusBadgeHolder.revalidate();
                    statusBadgeHolder.repaint();
                }
            }
        };
        worker.execute();
    }
}
