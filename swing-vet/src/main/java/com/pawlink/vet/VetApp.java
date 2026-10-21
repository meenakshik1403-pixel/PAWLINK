package com.pawlink.vet;

import com.pawlink.vet.base.PawTheme;
import com.pawlink.vet.view.LoginFrame;

import javax.swing.*;

/**
 * Desktop Application Entrypoint for PawLink.
 * Initializes modern PawTheme (FlatLaf + vector typography + styling) and presents the LoginFrame.
 */
public class VetApp {

    public static void main(String[] args) {
        // Configure Look and Feel & PawTheme
        PawTheme.install();

        // Launch on Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            try {
                System.out.println("[PawLink] Initializing LoginFrame on EventDispatchThread...");
                LoginFrame loginFrame = new LoginFrame();
                loginFrame.setVisible(true);
                loginFrame.toFront();
                loginFrame.requestFocus();
                System.out.println("[PawLink] LoginFrame is now visible on screen.");
            } catch (Throwable t) {
                System.err.println("[PawLink ERROR] Failed to display LoginFrame:");
                t.printStackTrace();
            }
        });
    }
}
