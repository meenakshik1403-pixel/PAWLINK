package com.pawlink.vet.base;

import javax.swing.*;
import java.awt.*;

/**
 * Static utility helper wrapping JOptionPane dialogs with clean, uniform titles and vector icons.
 */
public class Dialogs {

    private Dialogs() {
    }

    public static void showError(Component parent, String message) {
        JOptionPane.showMessageDialog(
                parent,
                message != null ? message : "An unexpected error occurred.",
                "PawLink - Error",
                JOptionPane.ERROR_MESSAGE,
                PawIcons.cross(28, PawTheme.DANGER)
        );
    }

    public static void showInfo(Component parent, String message) {
        JOptionPane.showMessageDialog(
                parent,
                message,
                "PawLink - Information",
                JOptionPane.INFORMATION_MESSAGE,
                PawIcons.paw(28, PawTheme.SKY)
        );
    }

    public static void showSuccess(Component parent, String message) {
        JOptionPane.showMessageDialog(
                parent,
                message,
                "PawLink - Success",
                JOptionPane.INFORMATION_MESSAGE,
                PawIcons.check(28, PawTheme.MINT)
        );
    }

    public static void showWarning(Component parent, String message) {
        JOptionPane.showMessageDialog(
                parent,
                message,
                "PawLink - Warning",
                JOptionPane.WARNING_MESSAGE,
                PawIcons.clock(28, PawTheme.SUNNY)
        );
    }

    public static boolean showConfirm(Component parent, String message, String title) {
        int result = JOptionPane.showConfirmDialog(
                parent,
                message,
                title != null ? title : "PawLink - Confirm",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                PawIcons.paw(28, PawTheme.CORAL)
        );
        return result == JOptionPane.YES_OPTION;
    }
}
