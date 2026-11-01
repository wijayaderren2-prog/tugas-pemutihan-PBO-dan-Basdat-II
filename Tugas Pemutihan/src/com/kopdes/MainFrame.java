
package com.kopdes;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class MainFrame extends JFrame {

    private JTable table;
    private DefaultTableModel model;

    private JTextField txtSku, txtNama, txtHarga, txtStok;

    private ProdukDAO dao = new ProdukDAO();

    public MainFrame() {

        setTitle("Kopdes KMP - Management Product");
        setSize(750, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Tabel Produk
        model = new DefaultTableModel(
                new String[]{"SKU", "Nama Produk", "Harga", "Stok"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        add(new JScrollPane(table), BorderLayout.CENTER);

        // Form Input
        JPanel formPanel = new JPanel(new GridLayout(4, 2, 5, 5));
        formPanel.setBorder(
                BorderFactory.createEmptyBorder(10, 10, 10, 10));

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

        // Tombol
        JButton btnAdd = new JButton("Tambah");
        JButton btnUpdate = new JButton("Update");
        JButton btnDelete = new JButton("Hapus");
        JButton btnClear = new JButton("Bersihkan");

        JPanel btnPanel = new JPanel();

        btnPanel.add(btnAdd);
        btnPanel.add(btnUpdate);
        btnPanel.add(btnDelete);
        btnPanel.add(btnClear);

        JPanel southPanel = new JPanel(new BorderLayout());

        southPanel.add(formPanel, BorderLayout.CENTER);
        southPanel.add(btnPanel, BorderLayout.SOUTH);

        add(southPanel, BorderLayout.SOUTH);

        // Tampilkan Data
        loadData();

        // Tombol Tambah
        btnAdd.addActionListener(e -> {
            try {
                Produk p = getProdukFromForm();

                dao.insertProduk(p);

                JOptionPane.showMessageDialog(
                        this, "Produk berhasil ditambahkan!");

                loadData();
                clearForm();

            } catch (NumberFormatException ex) {
                showError("Harga dan stok harus berupa angka.");
            } catch (Exception ex) {
                showError("Error Insert: " + ex.getMessage());
            }
        });

        // Tombol Update
        btnUpdate.addActionListener(e -> {
            try {
                if (txtSku.getText().trim().isEmpty()) {
                    showError("Pilih atau masukkan SKU produk terlebih dahulu.");
                    return;
                }

                Produk p = getProdukFromForm();

                dao.updateProduk(p);

                JOptionPane.showMessageDialog(
                        this, "Produk berhasil diperbarui!");

                loadData();
                clearForm();

            } catch (NumberFormatException ex) {
                showError("Harga dan stok harus berupa angka.");
            } catch (Exception ex) {
                showError("Error Update: " + ex.getMessage());
            }
        });

        // Tombol Hapus
        btnDelete.addActionListener(e -> {

            String sku = txtSku.getText().trim();

            if (sku.isEmpty()) {
                showError("Pilih produk yang ingin dihapus.");
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Apakah Anda yakin ingin menghapus produk dengan SKU " + sku + "?",
                    "Konfirmasi Hapus",
                    JOptionPane.YES_NO_OPTION
            );

            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    dao.deleteProduk(sku);

                    JOptionPane.showMessageDialog(
                            this, "Produk berhasil dihapus!");

                    loadData();
                    clearForm();

                } catch (Exception ex) {
                    showError("Error Delete: " + ex.getMessage());
                }
            }
        });

        // Tombol Bersihkan
        btnClear.addActionListener(e -> clearForm());

        // Klik Baris Tabel
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {

                int row = table.getSelectedRow();

                if (row >= 0) {
                    int modelRow = table.convertRowIndexToModel(row);

                    txtSku.setText(
                            model.getValueAt(modelRow, 0).toString());

                    txtNama.setText(
                            model.getValueAt(modelRow, 1).toString());

                    txtHarga.setText(
                            model.getValueAt(modelRow, 2).toString());

                    txtStok.setText(
                            model.getValueAt(modelRow, 3).toString());
                }
            }
        });
    }

    // Mengambil Data dari Form
    private Produk getProdukFromForm() {

        String sku = txtSku.getText().trim();
        String nama = txtNama.getText().trim();
        String hargaText = txtHarga.getText().trim();
        String stokText = txtStok.getText().trim();

        if (sku.isEmpty() || nama.isEmpty()
                || hargaText.isEmpty() || stokText.isEmpty()) {
            throw new IllegalArgumentException(
                    "Semua kolom wajib diisi.");
        }

        double harga = Double.parseDouble(hargaText);
        int stok = Integer.parseInt(stokText);

        if (harga < 0 || stok < 0) {
            throw new IllegalArgumentException(
                    "Harga dan stok tidak boleh negatif.");
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

    // Memuat Data dari Database
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

        } catch (Exception e) {
            showError("Gagal memuat data: " + e.getMessage());
        }
    }

    // Membersihkan Form
    private void clearForm() {

        txtSku.setText("");
        txtNama.setText("");
        txtHarga.setText("");
        txtStok.setText("");

        table.clearSelection();
    }

    // Menampilkan Pesan Error
    private void showError(String message) {
        JOptionPane.showMessageDialog(
                this,
                message,
                "Terjadi Kesalahan",
                JOptionPane.ERROR_MESSAGE
        );
    }

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {
            new MainFrame().setVisible(true);
        });
    }

    private static class ProdukDAO {

        public ProdukDAO() {
        }

        private void updateProduk(Produk p) {
            throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
        }

        private void deleteProduk(String sku) {
            throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
        }

        private List<Produk> getAllProduk() {
            throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
        }

        private void insertProduk(Produk p) {
            throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
        }
    }
}
