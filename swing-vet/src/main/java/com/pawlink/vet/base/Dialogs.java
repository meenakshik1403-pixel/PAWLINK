package com.pawlink.vet.base;

import javax.swing.*;
import java.awt.*;

/**
 * Static utility helper wrapping JOptionPane dialogs with clean, uniform titles.
 */
public class Dialogs {

    private Dialogs() {
    }

    public static void showError(Component parent, String message) {
        JOptionPane.showMessageDialog(
                parent,
                message != null ? message : "An unexpected error occurred.",
                "PawLink - Error",
                JOptionPane.ERROR_MESSAGE
        );
    }

    public static void showInfo(Component parent, String message) {
        JOptionPane.showMessageDialog(
                parent,
                message,
                "PawLink - Information",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    public static void showSuccess(Component parent, String message) {
        JOptionPane.showMessageDialog(
                parent,
                message,
                "PawLink - Success",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    public static void showWarning(Component parent, String message) {
        JOptionPane.showMessageDialog(
                parent,
                message,
                "PawLink - Warning",
                JOptionPane.WARNING_MESSAGE
        );
    }

    public static boolean showConfirm(Component parent, String message, String title) {
        int result = JOptionPane.showConfirmDialog(
                parent,
                message,
                title != null ? title : "PawLink - Confirm",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        return result == JOptionPane.YES_OPTION;
    }
}
