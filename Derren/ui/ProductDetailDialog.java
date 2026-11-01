package com.kopdes.kmp.ui;

import com.kopdes.kmp.model.Product;
import com.kopdes.kmp.model.StockHistory;
import com.kopdes.kmp.service.ProductService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * Halaman detail produk (modal dialog).
 *
 * Menampilkan:
 *  - Header: nama produk, kategori, harga.
 *  - Info umum: SKU, stok, satuan, deskripsi.
 *  - Stok & Harga: stok saat ini + harga.
 *  - Riwayat stok: 5 perubahan terakhir (waktu, jumlah, jenis, catatan).
 *  - Aksi: Edit (buka ProductFormDialog) dan Hapus (konfirmasi).
 *
 * Tanggung jawab UI murni: tidak menyentuh JDBC secara langsung.
 */
public class ProductDetailDialog extends JDialog {

    private static final NumberFormat RUPIAH =
            NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
    private static final SimpleDateFormat DATE_FMT =
            new SimpleDateFormat("dd MMM yyyy HH:mm", new Locale("id", "ID"));

    private final ProductService service = new ProductService();

    private Product product;             // mutable: bisa berubah setelah edit
    private boolean changed = false;     // true jika diedit/dihapus

    // ===== Komponen yang perlu di-update saat data berubah =====
    private JLabel lblName, lblCategory, lblPriceHero;
    private JLabel lblSkuValue, lblStockValue, lblUnitValue;
    private JTextArea txtDescription;

    private JLabel lblStockBig, lblPriceBig;
    private DefaultTableModel historyModel;

    // ===================================================================
    // Constructor
    // ===================================================================
    public ProductDetailDialog(Window owner, Product product) {
        super(owner, "Detail Produk", ModalityType.APPLICATION_MODAL);
        this.product = product;

        initComponents();
        layoutComponents();
        bindEvents();
        refreshAll();

        pack();
        setMinimumSize(new Dimension(860, 560));
        setLocationRelativeTo(owner);

        // ESC = tutup
        getRootPane().registerKeyboardAction(
                e -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    /** True jika produk diedit atau dihapus saat dialog ini terbuka. */
    public boolean isChanged() {
        return changed;
    }

    // ===================================================================
    // 1. Inisialisasi komponen
    // ===================================================================
    private void initComponents() {
        lblName        = new JLabel(" ");
        lblCategory    = new JLabel(" ");
        lblPriceHero   = new JLabel(" ");

        lblSkuValue    = new JLabel(" ");
        lblStockValue  = new JLabel(" ");
        lblUnitValue   = new JLabel(" ");

        txtDescription = new JTextArea(4, 30);
        txtDescription.setLineWrap(true);
        txtDescription.setWrapStyleWord(true);
        txtDescription.setEditable(false);
        txtDescription.setOpaque(false);
        txtDescription.setFont(txtDescription.getFont().deriveFont(13f));

        lblStockBig  = new JLabel("0");
        lblPriceBig  = new JLabel("Rp 0");

        String[] histCols = {"Waktu", "Jumlah", "Jenis", "Catatan"};
        historyModel = new DefaultTableModel(histCols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
    }

    // ===================================================================
    // 2. Layout utama
    // ===================================================================
    private void layoutComponents() {
        setLayout(new BorderLayout(0, 12));
        getContentPane().setBackground(new Color(0xF7F8FA));

        // ---------- HEADER ----------
        JPanel header = buildHeader();
        add(header, BorderLayout.NORTH);

        // ---------- BODY: 2 kolom ----------
        JPanel body = new JPanel(new GridLayout(1, 2, 16, 0));
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(0, 20, 0, 20));
        body.add(buildLeftCard());
        body.add(buildRightCard());

        add(body, BorderLayout.CENTER);

        // ---------- FOOTER: tombol aksi ----------
        add(buildFooter(), BorderLayout.SOUTH);
    }

    // -------- Header: nama, kategori, harga --------
    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout(12, 0));
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
                new EmptyBorder(20, 20, 20, 20),
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xE5E7EB))));

        // Ikon produk sederhana (emoji kotak / inisial)
        JLabel icon = new JLabel("📦", SwingConstants.CENTER);
        icon.setFont(icon.getFont().deriveFont(40f));
        icon.setPreferredSize(new Dimension(72, 72));
        icon.setOpaque(true);
        icon.setBackground(new Color(0xF3F4F6));
        icon.setBorder(BorderFactory.createLineBorder(new Color(0xE5E7EB)));

        // Judul + kategori + harga
        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));

        lblName.setFont(lblName.getFont().deriveFont(Font.BOLD, 20f));
        lblName.setForeground(new Color(0x111827));

        lblCategory.setFont(lblCategory.getFont().deriveFont(Font.PLAIN, 12f));
        lblCategory.setForeground(new Color(0x1E7A46));
        lblCategory.setOpaque(true);
        lblCategory.setBackground(new Color(0xDFF5E7));
        lblCategory.setBorder(new EmptyBorder(2, 8, 2, 8));

        lblPriceHero.setFont(lblPriceHero.getFont().deriveFont(Font.BOLD, 16f));
        lblPriceHero.setForeground(new Color(0x111827));

        JPanel catWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 4));
        catWrap.setOpaque(false);
        catWrap.add(lblCategory);

        center.add(lblName);
        center.add(catWrap);
        center.add(Box.createRigidArea(new Dimension(0, 4)));
        center.add(lblPriceHero);

        p.add(icon, BorderLayout.WEST);
        p.add(center, BorderLayout.CENTER);

        return p;
    }

    // -------- Kartu kiri: Informasi Umum --------
    private JPanel buildLeftCard() {
        JPanel card = new JPanel(new BorderLayout(0, 10));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xE5E7EB)),
                new EmptyBorder(16, 18, 16, 18)));

        JLabel title = new JLabel("Informasi Umum");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 14f));

        JPanel grid = new JPanel(new GridLayout(0, 2, 8, 8));
        grid.setOpaque(false);
        grid.add(keyLabel("SKU"));
        grid.add(lblSkuValue);
        grid.add(keyLabel("Stok"));
        grid.add(lblStockValue);
        grid.add(keyLabel("Satuan"));
        grid.add(lblUnitValue);

        JLabel lblDesc = new JLabel("Deskripsi");
        lblDesc.setFont(lblDesc.getFont().deriveFont(Font.BOLD, 12f));
        lblDesc.setForeground(new Color(0x6B7280));

        JScrollPane descScroll = new JScrollPane(txtDescription);
        descScroll.setBorder(BorderFactory.createLineBorder(new Color(0xE5E7EB)));
        descScroll.getViewport().setBackground(Color.WHITE);

        JPanel bottom = new JPanel(new BorderLayout(0, 6));
        bottom.setOpaque(false);
        bottom.add(lblDesc, BorderLayout.NORTH);
        bottom.add(descScroll, BorderLayout.CENTER);

        card.add(title, BorderLayout.NORTH);
        card.add(grid, BorderLayout.CENTER);
        card.add(bottom, BorderLayout.SOUTH);

        return card;
    }

    // -------- Kartu kanan: Stok & Harga + Riwayat --------
    private JPanel buildRightCard() {
        JPanel card = new JPanel(new BorderLayout(0, 12));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xE5E7EB)),
                new EmptyBorder(16, 18, 16, 18)));

        JLabel title = new JLabel("Stok & Harga");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 14f));

        // Ringkasan stok/harga
        JPanel summary = new JPanel(new GridLayout(1, 2, 12, 0));
        summary.setOpaque(false);
        summary.add(summaryTile("Stok saat ini", lblStockBig));
        summary.add(summaryTile("Harga", lblPriceBig));

        // Tabel riwayat
        JLabel histTitle = new JLabel("Riwayat Stok (5 Terakhir)");
        histTitle.setFont(histTitle.getFont().deriveFont(Font.BOLD, 12f));
        histTitle.setForeground(new Color(0x6B7280));

        JTable historyTable = new JTable(historyModel);
        historyTable.setRowHeight(28);
        historyTable.setShowHorizontalLines(true);
        historyTable.setGridColor(new Color(0xEEEEEE));
        historyTable.getColumnModel().getColumn(1).setMaxWidth(70);
        historyTable.getColumnModel().getColumn(2).setMaxWidth(80);
        historyTable.getColumnModel().getColumn(2)
                .setCellRenderer(new TypeBadgeRenderer());

        JScrollPane histScroll = new JScrollPane(historyTable);
        histScroll.setPreferredSize(new Dimension(0, 180));
        histScroll.setBorder(BorderFactory.createLineBorder(new Color(0xE5E7EB)));

        JPanel historyBlock = new JPanel(new BorderLayout(0, 6));
        historyBlock.setOpaque(false);
        historyBlock.add(histTitle, BorderLayout.NORTH);
        historyBlock.add(histScroll, BorderLayout.CENTER);

        card.add(title, BorderLayout.NORTH);
        card.add(summary, BorderLayout.CENTER);
        card.add(historyBlock, BorderLayout.SOUTH);

        return card;
    }

    private JPanel summaryTile(String caption, JLabel value) {
        JPanel tile = new JPanel(new BorderLayout());
        tile.setOpaque(true);
        tile.setBackground(new Color(0xF9FAFB));
        tile.setBorder(new EmptyBorder(12, 14, 12, 14));

        JLabel cap = new JLabel(caption);
        cap.setFont(cap.getFont().deriveFont(Font.PLAIN, 12f));
        cap.setForeground(new Color(0x6B7280));

        value.setFont(value.getFont().deriveFont(Font.BOLD, 18f));
        value.setForeground(new Color(0x111827));

        tile.add(cap,   BorderLayout.NORTH);
        tile.add(value, BorderLayout.CENTER);
        return tile;
    }

    // -------- Footer: tombol aksi --------
    private JPanel buildFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(0, 20, 16, 20));

        JButton btnClose = new JButton("Tutup");
        btnClose.addActionListener(e -> dispose());

        JButton btnDelete = new JButton("Hapus");
        btnDelete.setBackground(new Color(0xC0392B));
        btnDelete.setForeground(Color.WHITE);
        btnDelete.setFocusPainted(false);
        btnDelete.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnDelete.addActionListener(e -> onDelete());

        JButton btnEdit = new JButton("Edit");
        btnEdit.setBackground(new Color(0x1E9E5A));
        btnEdit.setForeground(Color.WHITE);
        btnEdit.setFocusPainted(false);
        btnEdit.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnEdit.addActionListener(e -> onEdit());

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        right.add(btnDelete);
        right.add(btnEdit);

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        left.setOpaque(false);
        left.add(btnClose);

        footer.add(left, BorderLayout.WEST);
        footer.add(right, BorderLayout.EAST);
        return footer;
    }

    private JLabel keyLabel(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(new Color(0x6B7280));
        l.setFont(l.getFont().deriveFont(Font.PLAIN, 12f));
        return l;
    }

    // ===================================================================
    // 3. Event binding tambahan
    // ===================================================================
    private void bindEvents() {
        // (opsional) tombol Enter menutup dialog
        getRootPane().setDefaultButton(null);
    }

    // ===================================================================
    // 4. Refresh semua data dari DB
    // ===================================================================
    private void refreshAll() {
        // Selalu ambil versi terbaru dari DB agar konsisten setelah edit
        try {
            Product fresh = service.findById(product.getId());
            if (fresh != null) this.product = fresh;
        } catch (SQLException ex) {
            showError("Gagal memuat data produk: " + ex.getMessage());
        }

        renderHeaderAndInfo();
        renderSummary();
        loadHistory();
    }

    private void renderHeaderAndInfo() {
        lblName.setText(product.getName());
        lblCategory.setText(product.getCategoryName());
        lblPriceHero.setText(RUPIAH.format(product.getPrice()));

        lblSkuValue.setText(product.getSku());
        lblStockValue.setText(product.getStock() + " " + product.getUnit());
        lblUnitValue.setText(product.getUnit());
        txtDescription.setText(product.getDescription() == null
                ? "-" : product.getDescription());
        txtDescription.setCaretPosition(0);
    }

    private void renderSummary() {
        lblStockBig.setText(product.getStock() + " " + product.getUnit());
        lblPriceBig.setText(RUPIAH.format(product.getPrice()));
    }

    // ===================================================================
    // 5. Riwayat stok
    // ===================================================================
    private void loadHistory() {
        historyModel.setRowCount(0);
        try {
            List<StockHistory> histories =
                    service.getStockHistory(product.getId(), 5);

            for (StockHistory h : histories) {
                String waktu = (h.getCreatedAt() == null)
                        ? "-" : DATE_FMT.format(h.getCreatedAt());
                String jumlah = (h.getType() == StockHistory.Type.IN ? "+" : "-")
                        + h.getQuantity();
                String jenis = (h.getType() == StockHistory.Type.IN)
                        ? "Masuk" : "Keluar";
                String note = (h.getNote() == null || h.getNote().isBlank())
                        ? "-" : h.getNote();

                historyModel.addRow(new Object[]{waktu, jumlah, jenis, note});
            }

            if (histories.isEmpty()) {
                historyModel.addRow(new Object[]{"-", "-", "-",
                        "Belum ada riwayat perubahan stok."});
            }
        } catch (SQLException ex) {
            showError("Gagal memuat riwayat stok: " + ex.getMessage());
        }
    }

    // ===================================================================
    // 6. Aksi Edit / Hapus
    // ===================================================================
    private void onEdit() {
        ProductFormDialog dialog =
                new ProductFormDialog(SwingUtilities.getWindowAncestor(this), product);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            changed = true;
            refreshAll();
        }
    }

    private void onDelete() {
        int opt = JOptionPane.showConfirmDialog(
                this,
                "Yakin ingin menghapus produk \"" + product.getName() + "\"?\n"
                        + "Riwayat stok terkait juga akan dihapus.\n"
                        + "Tindakan ini tidak dapat dibatalkan.",
                "Konfirmasi Hapus",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (opt != JOptionPane.YES_OPTION) return;

        try {
            service.deleteProduct(product.getId());
            changed = true;
            JOptionPane.showMessageDialog(this,
                    "Produk berhasil dihapus.", "Sukses",
                    JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (SQLException ex) {
            showError("Gagal menghapus produk: " + ex.getMessage());
        }
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error",
                JOptionPane.ERROR_MESSAGE);
    }

    // ===================================================================
    // 7. Renderer badge untuk kolom "Jenis"
    // ===================================================================
    private static class TypeBadgeRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable t, Object value, boolean isSelected,
                boolean hasFocus, int row, int col) {

            JLabel lbl = (JLabel) super.getTableCellRendererComponent(
                    t, value, isSelected, hasFocus, row, col);
            lbl.setHorizontalAlignment(SwingConstants.CENTER);

            if (!isSelected) {
                String v = String.valueOf(value);
                if ("Masuk".equalsIgnoreCase(v)) {
                    lbl.setBackground(new Color(0xDFF5E7));
                    lbl.setForeground(new Color(0x1E7A46));
                } else if ("Keluar".equalsIgnoreCase(v)) {
                    lbl.setBackground(new Color(0xFDE2E2));
                    lbl.setForeground(new Color(0xB3261E));
                } else {
                    lbl.setBackground(t.getBackground());
                    lbl.setForeground(t.getForeground());
                }
            }
            return lbl;
        }
    }
}