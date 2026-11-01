package com.kopdes.kmp.ui;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    public MainFrame() {
        setTitle("Kopdes KMP — Manajemen Inventaris");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1100, 700);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Sidebar sederhana
        JPanel sidebar = new JPanel();
        sidebar.setBackground(new Color(15, 45, 25));
        sidebar.setPreferredSize(new Dimension(200, 0));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));

        JLabel brand = new JLabel("🌿 Kopdes KMP");
        brand.setForeground(Color.WHITE);
        brand.setFont(new Font("SansSerif", Font.BOLD, 18));
        brand.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        sidebar.add(brand);

        String[] menus = {"Dashboard", "Inventaris", "Penjualan", "Pengaturan", "Laporan"};
        for (String m : menus) {
            JButton btn = new JButton(m);
            btn.setMaximumSize(new Dimension(200, 40));
            btn.setHorizontalAlignment(SwingConstants.LEFT);
            btn.setForeground(Color.WHITE);
            btn.setBackground(new Color(15, 45, 25));
            btn.setBorderPainted(false);
            btn.setFocusPainted(false);
            if (m.equals("Inventaris")) {
                btn.setBackground(new Color(30, 80, 45));
            }
            btn.addActionListener(e -> {
                // hanya Inventaris yang aktif
            });
            sidebar.add(btn);
        }
        add(sidebar, BorderLayout.WEST);

        add(new ProductListPanel(), BorderLayout.CENTER);
    }
}