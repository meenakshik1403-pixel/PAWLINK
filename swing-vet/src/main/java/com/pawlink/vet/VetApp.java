package com.pawlink.vet;

import com.formdev.flatlaf.FlatLightLaf;
import com.pawlink.vet.view.LoginFrame;

import javax.swing.*;

/**
 * Desktop Application Entrypoint for PawLink.
 * Initializes modern FlatLaf Look and Feel and presents the LoginFrame.
 */
public class VetApp {

    public static void main(String[] args) {
        // Configure Look and Feel
        try {
            FlatLightLaf.setup();
        } catch (Exception ignored) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ex) {
                // Fallback to default Swing Look & Feel
            }
        }

        // Launch on Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}
