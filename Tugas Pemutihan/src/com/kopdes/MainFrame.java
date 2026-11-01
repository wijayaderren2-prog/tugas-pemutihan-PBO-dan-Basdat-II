
package com.kopdes;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class MainFrame extends JFrame {

    private JTable table;
    private DefaultTableModel model;

    private JTextField txtSku;
    private JTextField txtNama;
    private JTextField txtHarga;
    private JTextField txtStok;

    private final ProdukDAO dao = new ProdukDAO();

    public MainFrame() {

        setTitle("Kopdes KMP - Management Product");
        setSize(750, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(5, 5));

        // =========================
        // TABEL PRODUK
        // =========================

        model = new DefaultTableModel(
            new String[]{"SKU", "Nama Produk", "Harga", "Stok"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(model);
        table.setSelectionMode(
            ListSelectionModel.SINGLE_SELECTION
        );

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        // =========================
        // FORM INPUT
        // =========================

        JPanel formPanel = new JPanel(
            new GridLayout(4, 2, 5, 5)
        );

        formPanel.setBorder(
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        );

        txtSku = new JTextField();
        txtNama = new JTextField();
        txtHarga = new JTextField();
        txtStok = new JTextField();

        formPanel.add(new JLabel("SKU:"));
        formPanel.add(txtSku);

        formPanel.add(new JLabel("Nama Produk:"));
        formPanel.add(txtNama);

        formPanel.add(new JLabel("Harga:"));
        formPanel.add(txtHarga);

        formPanel.add(new JLabel("Stok:"));
        formPanel.add(txtStok);

        // =========================
        // TOMBOL
        // =========================

        JButton btnAdd = new JButton("Tambah");
        JButton btnUpdate = new JButton("Update");
        JButton btnDelete = new JButton("Hapus");
        JButton btnClear = new JButton("Bersihkan");

        JPanel buttonPanel = new JPanel();

        buttonPanel.add(btnAdd);
        buttonPanel.add(btnUpdate);
        buttonPanel.add(btnDelete);
        buttonPanel.add(btnClear);

        JPanel southPanel = new JPanel(
            new BorderLayout()
        );

        southPanel.add(formPanel, BorderLayout.CENTER);
        southPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(southPanel, BorderLayout.SOUTH);

        // =========================
        // LOAD DATA AWAL
        // =========================

        loadData();

        // =========================
        // TOMBOL TAMBAH
        // =========================

        btnAdd.addActionListener(e -> {

            try {

                Produk p = getProdukFromForm();

                dao.insertProduk(p);

                JOptionPane.showMessageDialog(
                    this,
                    "Produk berhasil ditambahkan!"
                );

                loadData();
                clearForm();

            } catch (NumberFormatException ex) {

                showError(
                    "Harga dan stok harus berupa angka."
                );

            } catch (Exception ex) {

                showError(
                    "Gagal menambahkan produk:\n" + ex.getMessage()
                );
            }
        });

        // =========================
        // TOMBOL UPDATE
        // =========================

        btnUpdate.addActionListener(e -> {

            try {

                if (txtSku.getText().trim().isEmpty()) {
                    showError("Pilih produk yang ingin diupdate.");
                    return;
                }

                Produk p = getProdukFromForm();

                dao.updateProduk(p);

                JOptionPane.showMessageDialog(
                    this,
                    "Produk berhasil diperbarui!"
                );

                loadData();
                clearForm();

            } catch (NumberFormatException ex) {

                showError(
                    "Harga dan stok harus berupa angka."
                );

            } catch (Exception ex) {

                showError(
                    "Gagal memperbarui produk:\n" + ex.getMessage()
                );
            }
        });

        // =========================
        // TOMBOL HAPUS
        // =========================

        btnDelete.addActionListener(e -> {

            String sku = txtSku.getText().trim();

            if (sku.isEmpty()) {
                showError("Pilih produk yang ingin dihapus.");
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(
                this,
                "Yakin ingin menghapus produk dengan SKU " + sku + "?",
                "Konfirmasi Hapus",
                JOptionPane.YES_NO_OPTION
            );

            if (confirm == JOptionPane.YES_OPTION) {

                try {

                    dao.deleteProduk(sku);

                    JOptionPane.showMessageDialog(
                        this,
                        "Produk berhasil dihapus!"
                    );

                    loadData();
                    clearForm();

                } catch (Exception ex) {

                    showError(
                        "Gagal menghapus produk:\n" + ex.getMessage()
                    );
                }
            }
        });

        // =========================
        // TOMBOL BERSIHKAN
        // =========================

        btnClear.addActionListener(e -> clearForm());

        // =========================
        // KLIK TABEL
        // =========================

        table.addMouseListener(
            new java.awt.event.MouseAdapter() {

                @Override
                public void mouseClicked(
                    java.awt.event.MouseEvent evt
                ) {

                    int row = table.getSelectedRow();

                    if (row >= 0) {

                        int modelRow =
                            table.convertRowIndexToModel(row);

                        txtSku.setText(
                            model.getValueAt(modelRow, 0).toString()
                        );

                        txtNama.setText(
                            model.getValueAt(modelRow, 1).toString()
                        );

                        txtHarga.setText(
                            model.getValueAt(modelRow, 2).toString()
                        );

                        txtStok.setText(
                            model.getValueAt(modelRow, 3).toString()
                        );
                    }
                }
            }
        );
    }

    // =========================
    // MENGAMBIL DATA FORM
    // =========================

    private Produk getProdukFromForm() {

        String sku = txtSku.getText().trim();
        String nama = txtNama.getText().trim();
        String hargaText = txtHarga.getText().trim();
        String stokText = txtStok.getText().trim();

        if (sku.isEmpty() ||
            nama.isEmpty() ||
            hargaText.isEmpty() ||
            stokText.isEmpty()) {

            throw new IllegalArgumentException(
                "Semua kolom wajib diisi."
            );
        }

        double harga = Double.parseDouble(hargaText);
        int stok = Integer.parseInt(stokText);

        if (harga < 0 || stok < 0) {
            throw new IllegalArgumentException(
                "Harga dan stok tidak boleh negatif."
            );
        }

        return new Produk(
            sku,
            nama,
            1,
            "Deskripsi",
            harga,
            stok,
            "pcs"
        );
    }

    // =========================
    // MENAMPILKAN DATA
    // =========================

    private void loadData() {

        model.setRowCount(0);

        try {

            List<Produk> list = dao.getAllProduk();

            for (Produk p : list) {

                model.addRow(new Object[]{
                    p.getSku(),
                    p.getNamaProduk(),
                    p.getHarga(),
                    p.getStok()
                });
            }

        } catch (Exception ex) {

            showError(
                "Gagal memuat data:\n" + ex.getMessage()
            );
        }
    }

    // =========================
    // MEMBERSIHKAN FORM
    // =========================

    private void clearForm() {

        txtSku.setText("");
        txtNama.setText("");
        txtHarga.setText("");
        txtStok.setText("");

        table.clearSelection();
        txtSku.requestFocus();
    }

    // =========================
    // PESAN ERROR
    // =========================

    private void showError(String message) {

        JOptionPane.showMessageDialog(
            this,
            message,
            "Terjadi Kesalahan",
            JOptionPane.ERROR_MESSAGE
        );
    }

    // =========================
    // MAIN CLASS
    // =========================

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {

            new MainFrame().setVisible(true);

        });
    }
}
