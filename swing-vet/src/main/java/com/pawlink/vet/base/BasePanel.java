package com.pawlink.vet.base;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Abstract BasePanel for all Swing panels in PawLink.
 * Enforces standard BorderLayout, branded header banner, padded borders,
 * and standard error/info notification dialogs.
 */
public abstract class BasePanel extends JPanel {

    protected JPanel headerPanel;
    protected JLabel titleLabel;
    protected JLabel subtitleLabel;
    protected JPanel contentPanel;

    public BasePanel(String title, String subtitle) {
        super(new BorderLayout());
        setBorder(new EmptyBorder(15, 20, 20, 20));
        setBackground(new Color(248, 250, 252)); // Light slate background

        initHeader(title, subtitle);
        initContent();
    }

    public BasePanel(String title) {
        this(title, null);
    }

    private void initHeader(String title, String subtitle) {
        headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(0, 0, 15, 0));

        JPanel titleContainer = new JPanel(new GridLayout(subtitle != null ? 2 : 1, 1, 0, 4));
        titleContainer.setOpaque(false);

        titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(new Color(30, 41, 59));
        titleContainer.add(titleLabel);

        if (subtitle != null) {
            subtitleLabel = new JLabel(subtitle);
            subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            subtitleLabel.setForeground(new Color(100, 116, 139));
            titleContainer.add(subtitleLabel);
        }

        headerPanel.add(titleContainer, BorderLayout.WEST);
        add(headerPanel, BorderLayout.NORTH);
    }

    private void initContent() {
        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setOpaque(false);
        add(contentPanel, BorderLayout.CENTER);
    }

    /**
     * Subclasses and callers can add action buttons or filters to the right side of the header.
     */
    public void setHeaderAccessory(JComponent accessory) {
        headerPanel.add(accessory, BorderLayout.EAST);
        headerPanel.revalidate();
        headerPanel.repaint();
    }

    /**
     * Sets the main content component.
     */
    public void setContent(JComponent component) {
        contentPanel.removeAll();
        contentPanel.add(component, BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    public void showError(String msg) {
        Dialogs.showError(this, msg);
    }

    public void showInfo(String msg) {
        Dialogs.showInfo(this, msg);
    }

    public void showSuccess(String msg) {
        Dialogs.showSuccess(this, msg);
    }
}
