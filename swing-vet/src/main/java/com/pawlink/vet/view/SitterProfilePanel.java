package com.pawlink.vet.view;

import com.pawlink.vet.api.ApiClient;
import com.pawlink.vet.api.Session;
import com.pawlink.vet.base.BasePanel;
import com.pawlink.vet.base.Dialogs;
import com.pawlink.vet.model.SitterDTO;
import com.pawlink.vet.model.SittingBookingDTO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

/**
 * Profile panel for pet sitters to review verification status and stats.
 */
public class SitterProfilePanel extends BasePanel {

    private final JLabel nameLabel = new JLabel("-");
    private final JLabel emailLabel = new JLabel("-");
    private final JLabel phoneLabel = new JLabel("-");
    private final JLabel rateLabel = new JLabel("-");
    private final JLabel statusBadge = new JLabel("Checking...");
    private final JLabel totalJobsLabel = new JLabel("0");

    public SitterProfilePanel() {
        super("👤 Sitter Profile & Credentials", "Verify your credentials, review your published rate, and track platform jobs.");

        setupUI();
        loadProfile();
    }

    private void setupUI() {
        JPanel accessory = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        accessory.setOpaque(false);
        JButton refreshBtn = new JButton("🔄 Refresh");
        refreshBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        refreshBtn.addActionListener(e -> loadProfile());
        accessory.add(refreshBtn);
        setHeaderAccessory(accessory);

        JPanel mainCard = new JPanel(new GridBagLayout());
        mainCard.setBackground(Color.WHITE);
        mainCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                new EmptyBorder(30, 40, 30, 40)
        ));

        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(10, 15, 10, 15);
        g.gridx = 0;
        g.gridy = 0;

        // Status Badge at Top
        g.gridwidth = 2;
        statusBadge.setFont(new Font("Segoe UI", Font.BOLD, 16));
        statusBadge.setOpaque(true);
        statusBadge.setBorder(new EmptyBorder(8, 16, 8, 16));
        statusBadge.setHorizontalAlignment(SwingConstants.CENTER);
        mainCard.add(statusBadge, g);

        // Fields
        g.gridwidth = 1;
        g.gridy = 1;
        addField(mainCard, g, "Full Name:", nameLabel);

        g.gridy = 2;
        addField(mainCard, g, "Email:", emailLabel);

        g.gridy = 3;
        addField(mainCard, g, "Phone:", phoneLabel);

        g.gridy = 4;
        addField(mainCard, g, "Hourly Rate:", rateLabel);

        g.gridy = 5;
        addField(mainCard, g, "Total Assigned Jobs:", totalJobsLabel);

        JPanel container = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 20));
        container.setOpaque(false);
        container.add(mainCard);

        setContent(container);
    }

    private void addField(JPanel panel, GridBagConstraints g, String title, JLabel value) {
        g.gridx = 0;
        JLabel l = new JLabel(title);
        l.setFont(new Font("Segoe UI", Font.BOLD, 13));
        l.setForeground(new Color(100, 116, 139));
        panel.add(l, g);

        g.gridx = 1;
        value.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        value.setForeground(new Color(30, 41, 59));
        panel.add(value, g);
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
                    // Fallback to searching in list
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

                    if (s.isVerified()) {
                        statusBadge.setText("✓ VERIFIED PET SITTER (Eligible for bookings)");
                        statusBadge.setBackground(new Color(209, 250, 229)); // Light Green
                        statusBadge.setForeground(new Color(6, 95, 70));
                    } else {
                        statusBadge.setText("⏳ PENDING VERIFICATION (Awaiting Admin Approval)");
                        statusBadge.setBackground(new Color(254, 243, 199)); // Light Yellow
                        statusBadge.setForeground(new Color(146, 64, 14));
                    }

                    // Count jobs
                    List<SittingBookingDTO> jobs = ApiClient.getInstance().getList("/api/sitter-bookings?sitterId=" + userId, SittingBookingDTO.class);
                    totalJobsLabel.setText(jobs.size() + " job(s)");
                } catch (Exception ex) {
                    nameLabel.setText(Session.getInstance().getName() != null ? Session.getInstance().getName() : "Sitter");
                    emailLabel.setText(Session.getInstance().getEmail() != null ? Session.getInstance().getEmail() : "-");
                    statusBadge.setText("⏳ Status Pending");
                    statusBadge.setBackground(new Color(241, 245, 249));
                    statusBadge.setForeground(new Color(100, 116, 139));
                }
            }
        };
        worker.execute();
    }
}
