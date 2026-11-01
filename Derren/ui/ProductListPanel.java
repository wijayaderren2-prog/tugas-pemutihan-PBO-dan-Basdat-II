package com.kopdes.kmp.ui;

import com.kopdes.kmp.model.Category;
import com.kopdes.kmp.model.Product;
import com.kopdes.kmp.service.ProductService;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

/**
 * Panel daftar produk — halaman utama modul CRUD Management Product.
 *
 * Tanggung jawab:
 *  - Menampilkan daftar produk dalam tabel (nama, kategori, stok, harga, status).
 *  - Search berdasarkan nama & filter berdasarkan kategori.
 *  - Pagination (6 baris/halaman sesuai mockup).
 *  - Membuka form Create (tombol + Tambah Produk).
 *  - Membuka halaman Detail (double-click) via callback.
 *  - Menghapus produk (klik kanan → Hapus) dengan konfirmasi.
 */
public class ProductListPanel extends JPanel {

    // ===================================================================
    // Konstanta & dependency
    // ===================================================================
    private static final int PAGE_SIZE = 6;
    private static final NumberFormat RUPIAH =
            NumberFormat.getCurrencyInstance(new Locale("id", "ID"));

    private final ProductService service = new ProductService();

    /** Callback ke MainFrame saat user membuka detail produk. */
    private final Consumer<Product> onOpenDetail;

    // ===================================================================
    // Komponen UI
    // ===================================================================
    private JTextField txtSearch;
    private JComboBox<Category> cbCategory;
    private JButton btnAdd;

    private JTable table;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;

    private JButton btnPrev, btnNext;
    private JLabel lblPageInfo;

    // ===================================================================
    // State
    // ===================================================================
    private int currentPage = 1;
    private int totalPages  = 1;
    private int totalRows   = 0;

    // ===================================================================
    // Constructor
    // ===================================================================
    public ProductListPanel(Consumer<Product> onOpenDetail) {
        this.onOpenDetail = onOpenDetail;
        initComponents();
        layoutComponents();
        bindEvents();
        loadCategories();
        loadData();
    }

    // ===================================================================
    // 1. Inisialisasi komponen
    // ===================================================================
    private void initComponents() {
        setLayout(new BorderLayout(0, 12));
        setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
        setBackground(new Color(0xF7F8FA));

        // --- Search ---
        txtSearch = new JTextField(24);
        txtSearch.putClientProperty("JTextField.placeholderText", "Cari nama produk...");
        txtSearch.setPreferredSize(new Dimension(240, 34));

        // --- Filter kategori ---
        cbCategory = new JComboBox<>();
        cbCategory.setPreferredSize(new Dimension(180, 34));

        // --- Tombol tambah ---
        btnAdd = new JButton("+ Tambah Produk");
        btnAdd.setBackground(new Color(0x1E9E5A));
        btnAdd.setForeground(Color.WHITE);
        btnAdd.setFocusPainted(false);
        btnAdd.setFont(btnAdd.getFont().deriveFont(Font.BOLD, 13f));
        btnAdd.setPreferredSize(new Dimension(150, 34));
        btnAdd.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // --- Tabel ---
        String[] cols = {"ID", "Nama Produk", "Kategori", "Stok", "Harga", "Status"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Class<?> getColumnClass(int c) {
                return c == 3 ? Integer.class : String.class;
            }
        };
        table = new JTable(tableModel);
        table.setRowHeight(36);
        table.setShowHorizontalLines(true);
        table.setGridColor(new Color(0xE8E8E8));
        table.setSelectionBackground(new Color(0xE6F4EA));
        table.setSelectionForeground(Color.BLACK);
        table.getTableHeader().setFont(
                table.getTableHeader().getFont().deriveFont(Font.BOLD, 13f));
        table.getTableHeader().setBackground(new Color(0xF0F2F5));

        // Atur lebar kolom
        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);      // ID disembunyikan
        table.getColumnModel().getColumn(3).setMaxWidth(70);     // Stok
        table.getColumnModel().getColumn(5).setMaxWidth(110);    // Status

        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);

        // Renderer badge untuk kolom Status
        table.getColumnModel().getColumn(5).setCellRenderer(new StatusBadgeRenderer());

        // --- Pagination ---
        btnPrev = new JButton("<");
        btnNext = new JButton(">");
        stylePagerButton(btnPrev);
        stylePagerButton(btnNext);
        lblPageInfo = new JLabel("Halaman 1 dari 1");
        lblPageInfo.setFont(lblPageInfo.getFont().deriveFont(Font.PLAIN, 13f));
    }

    private void stylePagerButton(JButton b) {
        b.setPreferredSize(new Dimension(34, 30));
        b.setFocusPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    // ===================================================================
    // 2. Layout
    // ===================================================================
    private void layoutComponents() {
        // --- Top bar ---
        JPanel top = new JPanel(new BorderLayout(10, 0));
        top.setOpaque(false);

        JPanel leftFilter = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftFilter.setOpaque(false);
        leftFilter.add(txtSearch);
        leftFilter.add(cbCategory);

        JPanel rightAction = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        rightAction.setOpaque(false);
        rightAction.add(btnAdd);

        top.add(leftFilter, BorderLayout.WEST);
        top.add(rightAction, BorderLayout.EAST);

        // --- Center tabel ---
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(0xE0E0E0)));
        scroll.getViewport().setBackground(Color.WHITE);

        // --- Bottom pagination ---
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 4));
        bottom.setOpaque(false);
        bottom.add(btnPrev);
        bottom.add(lblPageInfo);
        bottom.add(btnNext);

        add(top, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);
    }

    // ===================================================================
    // 3. Event binding
    // ===================================================================
    private void bindEvents() {
        // Search realtime (debounce sederhana via reset halaman)
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e)  { resetAndReload(); }
            @Override public void removeUpdate(DocumentEvent e)  { resetAndReload(); }
            @Override public void changedUpdate(DocumentEvent e) { resetAndReload(); }
        });

        cbCategory.addActionListener(e -> resetAndReload());

        btnAdd.addActionListener(e -> openCreateForm());

        // Double-click baris → buka detail
        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedRow() != -1) {
                    openDetail(getSelectedProductId());
                }
            }
        });

        // Klik kanan → menu konteks (Detail / Edit / Hapus)
        table.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e)  { maybeShowPopup(e); }
            @Override public void mouseReleased(MouseEvent e) { maybeShowPopup(e); }
        });

        // Pagination
        btnPrev.addActionListener(e -> {
            if (currentPage > 1) { currentPage--; loadData(); }
        });
        btnNext.addActionListener(e -> {
            if (currentPage < totalPages) { currentPage++; loadData(); }
        });
    }

    private void resetAndReload() {
        currentPage = 1;
        loadData();
    }

    private void maybeShowPopup(MouseEvent e) {
        if (!e.isPopupTrigger()) return;
        int row = table.rowAtPoint(e.getPoint());
        if (row < 0) return;
        table.setRowSelectionInterval(row, row);

        JPopupMenu menu = new JPopupMenu();
        JMenuItem miDetail = new JMenuItem("Lihat Detail");
        JMenuItem miEdit   = new JMenuItem("Edit Produk");
        JMenuItem miDelete = new JMenuItem("Hapus Produk");
        miDelete.setForeground(new Color(0xC0392B));

        miDetail.addActionListener(a -> openDetail(getSelectedProductId()));
        miEdit.addActionListener(a -> openEditForm(getSelectedProductId()));
        miDelete.addActionListener(a -> confirmDelete(getSelectedProductId()));

        menu.add(miDetail);
        menu.add(miEdit);
        menu.addSeparator();
        menu.add(miDelete);
        menu.show(table, e.getX(), e.getY());
    }

    // ===================================================================
    // 4. Load kategori
    // ===================================================================
    private void loadCategories() {
        try {
            List<Category> list = service.getAllCategories();
            cbCategory.removeAllItems();
            cbCategory.addItem(new Category(0, "Semua Kategori"));
            for (Category c : list) cbCategory.addItem(c);
            cbCategory.setSelectedIndex(0);
        } catch (SQLException ex) {
            showError("Gagal memuat kategori: " + ex.getMessage());
        }
    }

    // ===================================================================
    // 5. Load data produk (paged + filter)
    // ===================================================================
    public void loadData() {
        tableModel.setRowCount(0);

        try {
            String keyword = txtSearch.getText() == null
                    ? "" : txtSearch.getText().trim();
            Category cat = (Category) cbCategory.getSelectedItem();
            int categoryId = (cat == null) ? 0 : cat.getId();

            totalRows  = service.countProducts(keyword, categoryId);
            totalPages = Math.max(1, (int) Math.ceil(totalRows / (double) PAGE_SIZE));
            if (currentPage > totalPages) currentPage = totalPages;

            List<Product> products = service.findPaged(
                    keyword, categoryId,
                    PAGE_SIZE, (currentPage - 1) * PAGE_SIZE);

            for (Product p : products) {
                tableModel.addRow(new Object[]{
                        p.getId(),
                        p.getName(),
                        p.getCategoryName(),
                        p.getStock(),
                        RUPIAH.format(p.getPrice()),
                        statusOf(p.getStock())
                });
            }

            lblPageInfo.setText("Halaman " + currentPage + " dari " + totalPages);
            btnPrev.setEnabled(currentPage > 1);
            btnNext.setEnabled(currentPage < totalPages);
        } catch (SQLException ex) {
            showError("Gagal memuat data produk: " + ex.getMessage());
        }
    }

    // ===================================================================
    // 6. Helper
    // ===================================================================
    private String statusOf(int stock) {
        if (stock <= 0)  return "Habis";
        if (stock <= 10) return "Stok Rendah";
        return "Tersedia";
    }

    private int getSelectedProductId() {
        int viewRow = table.getSelectedRow();
        int modelRow = table.convertRowIndexToModel(viewRow);
        return (int) tableModel.getValueAt(modelRow, 0);
    }

    // ===================================================================
    // 7. Aksi: Tambah / Edit / Detail / Hapus
    // ===================================================================
    private void openCreateForm() {
        Window owner = SwingUtilities.getWindowAncestor(this);
        ProductFormDialog dialog = new ProductFormDialog(owner, null);
        dialog.setVisible(true);
        if (dialog.isSaved()) loadData();
    }

    private void openEditForm(int productId) {
        try {
            Product p = service.findById(productId);
            if (p == null) { showError("Produk tidak ditemukan."); return; }

            Window owner = SwingUtilities.getWindowAncestor(this);
            ProductFormDialog dialog = new ProductFormDialog(owner, p);
            dialog.setVisible(true);
            if (dialog.isSaved()) loadData();
        } catch (SQLException ex) {
            showError("Gagal memuat produk: " + ex.getMessage());
        }
    }

    private void openDetail(int productId) {
        try {
            Product p = service.findById(productId);
            if (p == null) { showError("Produk tidak ditemukan."); return; }
            if (onOpenDetail != null) onOpenDetail.accept(p);
        } catch (SQLException ex) {
            showError("Gagal membuka detail: " + ex.getMessage());
        }
    }

    private void confirmDelete(int productId) {
        try {
            Product p = service.findById(productId);
            if (p == null) { showError("Produk tidak ditemukan."); return; }

            int opt = JOptionPane.showConfirmDialog(
                    this,
                    "Yakin ingin menghapus produk \"" + p.getName() + "\"?\n"
                            + "Tindakan ini tidak dapat dibatalkan.",
                    "Konfirmasi Hapus",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);

            if (opt == JOptionPane.YES_OPTION) {
                service.deleteProduct(productId);
                JOptionPane.showMessageDialog(this,
                        "Produk berhasil dihapus.", "Sukses",
                        JOptionPane.INFORMATION_MESSAGE);
                loadData();
            }
        } catch (SQLException ex) {
            showError("Gagal menghapus produk: " + ex.getMessage());
        }
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    // ===================================================================
    // 8. Renderer badge Status
    // ===================================================================
    private static class StatusBadgeRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable t, Object value, boolean isSelected,
                boolean hasFocus, int row, int col) {

            JLabel lbl = (JLabel) super.getTableCellRendererComponent(
                    t, value, isSelected, hasFocus, row, col);
            lbl.setHorizontalAlignment(SwingConstants.CENTER);
            lbl.setOpaque(true);
            lbl.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));

            if (isSelected) {
                lbl.setBackground(t.getSelectionBackground());
                lbl.setForeground(t.getSelectionForeground());
                return lbl;
            }

            String status = String.valueOf(value);
            switch (status) {
                case "Tersedia":
                    lbl.setBackground(new Color(0xDFF5E7));
                    lbl.setForeground(new Color(0x1E7A46));
                    break;
                case "Stok Rendah":
                    lbl.setBackground(new Color(0xFFF4D6));
                    lbl.setForeground(new Color(0xB07800));
                    break;
                case "Habis":
                    lbl.setBackground(new Color(0xFDE2E2));
                    lbl.setForeground(new Color(0xB3261E));
                    break;
                default:
                    lbl.setBackground(t.getBackground());
                    lbl.setForeground(t.getForeground());
            }
            return lbl;
        }
    }
}