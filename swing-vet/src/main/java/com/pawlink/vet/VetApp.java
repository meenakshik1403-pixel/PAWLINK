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
            try {
                System.out.println("[PawLink] Initializing LoginFrame on EventDispatchThread...");
                LoginFrame loginFrame = new LoginFrame();
                loginFrame.setVisible(true);
                loginFrame.toFront();
                loginFrame.requestFocus();
                loginFrame.setAlwaysOnTop(true);
                loginFrame.setAlwaysOnTop(false);
                System.out.println("[PawLink] LoginFrame is now visible on screen.");
            } catch (Throwable t) {
                System.err.println("[PawLink ERROR] Failed to display LoginFrame:");
                t.printStackTrace();
            }
        });
    }
}
