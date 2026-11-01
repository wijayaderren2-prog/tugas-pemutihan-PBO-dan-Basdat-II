package com.kopdes.kmp;

import com.kopdes.kmp.ui.MainFrame;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // Look & Feel modern
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}