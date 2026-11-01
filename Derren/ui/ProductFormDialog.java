package com.kopdes.kmp.ui;

import com.kopdes.kmp.model.Category;
import com.kopdes.kmp.model.Product;
import com.kopdes.kmp.service.ProductService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Dialog form untuk Create (product == null) dan Update (product != null).
 *
 * Tanggung jawab:
 *  - Menyediakan form input untuk seluruh atribut produk.
 *  - Melakukan validasi tingkat UI (required, format, range).
 *  - Mendelegasikan penyimpanan ke ProductService.
 *  - Melaporkan status "saved" lewat isSaved().
 *
 * Aturan bisnis:
 *  - SKU : wajib, alfanumerik + '-', max 30 karakter, unik (dicek DB).
 *  - Nama : wajib, 3–100 karakter.
 *  - Kategori : wajib dipilih (id > 0).
 *  - Deskripsi : opsional, max 500 karakter.
 *  - Harga : wajib, angka >= 0.
 *  - Stok : wajib, bilangan bulat >= 0.
 *  - Satuan : wajib dipilih.
 */
public class ProductFormDialog extends JDialog {

    private static final Pattern SKU_PATTERN =
            Pattern.compile("^[A-Za-z0-9\\-]{1,30}$");

    private static final String[] UNIT_OPTIONS = {
            "-- Pilih Satuan --", "pcs", "kg", "liter", "pack", "botol", "box"
    };

    private final ProductService service = new ProductService();
    private final Product existing;      // null = mode CREATE
    private boolean saved = false;

    // ================== Komponen input ==================
    private JTextField      txtSku;
    private JTextField      txtName;
    private JComboBox<Category> cbCategory;
    private JTextArea       txtDescription;
    private JTextField      txtPrice;
    private JTextField      txtStock;
    private JComboBox<String>   cbUnit;

    // ================== Label error per field ==================
    private JLabel errSku, errName, errCategory, errPrice, errStock, errUnit;

    // ================== Tombol ==================
    private JButton btnSave, btnCancel;

    // ===================================================================
    // Constructor
    // ===================================================================
    public ProductFormDialog(Window owner, Product product) {
        super(owner,
              (product == null) ? "Tambah Produk Baru" : "Edit Produk",
              ModalityType.APPLICATION_MODAL);
        this.existing = product;

        initComponents();
        layoutComponents();
        bindEvents();
        loadCategories();

        if (existing != null) fillForm(existing);

        pack();
        setMinimumSize(new Dimension(560, 0));
        setLocationRelativeTo(owner);
    }

    /** True jika user menekan Simpan dan data berhasil dipersist. */
    public boolean isSaved() {
        return saved;
    }

    // ===================================================================
    // 1. Inisialisasi komponen
    // ===================================================================
    private void initComponents() {
        txtSku  = new JTextField(20);
        txtName = new JTextField(20);

        cbCategory = new JComboBox<>();
        cbCategory.setPreferredSize(new Dimension(220, 28));

        txtDescription = new JTextArea(3, 20);
        txtDescription.setLineWrap(true);
        txtDescription.setWrapStyleWord(true);
        txtDescription.setFont(txtDescription.getFont().deriveFont(13f));

        txtPrice = new JTextField(15);
        txtStock = new JTextField(10);

        cbUnit = new JComboBox<>(UNIT_OPTIONS);

        btnSave = new JButton("Simpan");
        btnSave.setBackground(new Color(0x1E9E5A));
        btnSave.setForeground(Color.WHITE);
        btnSave.setFocusPainted(false);
        btnSave.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSave.setPreferredSize(new Dimension(100, 32));

        btnCancel = new JButton("Batal");
        btnCancel.setFocusPainted(false);
        btnCancel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCancel.setPreferredSize(new Dimension(90, 32));

        errSku      = errorLabel();
        errName     = errorLabel();
        errCategory = errorLabel();
        errPrice    = errorLabel();
        errStock    = errorLabel();
        errUnit     = errorLabel();
    }

    private JLabel errorLabel() {
        JLabel l = new JLabel(" ");
        l.setForeground(new Color(0xC0392B));
        l.setFont(l.getFont().deriveFont(Font.PLAIN, 11f));
        return l;
    }

    // ===================================================================
    // 2. Layout (GridBagLayout)
    // ===================================================================
    private void layoutComponents() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(20, 24, 10, 24));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 4, 2, 8);
        g.anchor = GridBagConstraints.WEST;
        g.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;

        // --- SKU ---
        g.gridx = 0; g.gridy = row; g.weightx = 0;
        form.add(fieldLabel("ID / SKU *"), g);
        g.gridx = 1; g.weightx = 1;
        form.add(txtSku, g);
        g.gridy = ++row; form.add(errSku, g);

        // --- Nama ---
        g.gridx = 0; g.gridy = ++row; g.weightx = 0;
        form.add(fieldLabel("Nama Produk *"), g);
        g.gridx = 1; g.weightx = 1;
        form.add(txtName, g);
        g.gridy = ++row; form.add(errName, g);

        // --- Kategori ---
        g.gridx = 0; g.gridy = ++row; g.weightx = 0;
        form.add(fieldLabel("Kategori *"), g);
        g.gridx = 1; g.weightx = 1;
        form.add(cbCategory, g);
        g.gridy = ++row; form.add(errCategory, g);

        // --- Deskripsi ---
        g.gridx = 0; g.gridy = ++row; g.weightx = 0;
        g.anchor = GridBagConstraints.NORTHWEST;
        form.add(fieldLabel("Deskripsi"), g);

        g.gridx = 1; g.weightx = 1;
        g.fill = GridBagConstraints.BOTH;
        JScrollPane descScroll = new JScrollPane(txtDescription);
        descScroll.setPreferredSize(new Dimension(300, 70));
        form.add(descScroll, g);
        g.fill = GridBagConstraints.HORIZONTAL;
        g.anchor = GridBagConstraints.WEST;

        // --- Harga ---
        g.gridx = 0; g.gridy = ++row; g.weightx = 0;
        form.add(fieldLabel("Harga (Rp) *"), g);
        g.gridx = 1; g.weightx = 1;
        form.add(txtPrice, g);
        g.gridy = ++row; form.add(errPrice, g);

        // --- Stok ---
        g.gridx = 0; g.gridy = ++row; g.weightx = 0;
        form.add(fieldLabel("Jumlah Stok *"), g);
        g.gridx = 1; g.weightx = 1;
        form.add(txtStock, g);
        g.gridy = ++row; form.add(errStock, g);

        // --- Satuan ---
        g.gridx = 0; g.gridy = ++row; g.weightx = 0;
        form.add(fieldLabel("Satuan *"), g);
        g.gridx = 1; g.weightx = 1;
        form.add(cbUnit, g);
        g.gridy = ++row; form.add(errUnit, g);

        // --- Tombol ---
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.setBorder(new EmptyBorder(5, 24, 16, 24));
        buttons.add(btnCancel);
        buttons.add(btnSave);

        // --- Header judul ---
        JLabel title = new JLabel(existing == null
                ? "Tambah Produk Baru" : "Edit Produk");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));
        title.setBorder(new EmptyBorder(18, 24, 0, 24));

        JLabel subtitle = new JLabel(existing == null
                ? "Lengkapi data produk baru di bawah ini."
                : "Perbarui informasi produk lalu simpan perubahan.");
        subtitle.setForeground(new Color(0x6B7280));
        subtitle.setFont(subtitle.getFont().deriveFont(Font.PLAIN, 12f));
        subtitle.setBorder(new EmptyBorder(2, 24, 0, 24));

        JPanel header = new JPanel(new BorderLayout());
        header.add(title,    BorderLayout.NORTH);
        header.add(subtitle, BorderLayout.SOUTH);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(header, BorderLayout.NORTH);
        getContentPane().add(form,   BorderLayout.CENTER);
        getContentPane().add(buttons, BorderLayout.SOUTH);
    }

    private JLabel fieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(l.getFont().deriveFont(Font.PLAIN, 13f));
        l.setPreferredSize(new Dimension(130, 24));
        return l;
    }

    // ===================================================================
    // 3. Event binding
    // ===================================================================
    private void bindEvents() {
        btnSave.addActionListener(e -> onSave());
        btnCancel.addActionListener(e -> dispose());

        // Enter = simpan, ESC = batal
        getRootPane().setDefaultButton(btnSave);
        getRootPane().registerKeyboardAction(
                e -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    // ===================================================================
    // 4. Load kategori
    // ===================================================================
    private void loadCategories() {
        try {
            List<Category> cats = service.getAllCategories();
            cbCategory.removeAllItems();
            cbCategory.addItem(new Category(0, "-- Pilih Kategori --"));
            for (Category c : cats) cbCategory.addItem(c);
            cbCategory.setSelectedIndex(0);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Gagal memuat kategori: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ===================================================================
    // 5. Isi form saat mode edit
    // ===================================================================
    private void fillForm(Product p) {
        txtSku.setText(p.getSku());
        txtName.setText(p.getName());
        txtPrice.setText(p.getPrice() == null
                ? "" : p.getPrice().toPlainString());
        txtStock.setText(String.valueOf(p.getStock()));
        txtDescription.setText(p.getDescription() == null
                ? "" : p.getDescription());

        // pilih kategori
        for (int i = 0; i < cbCategory.getItemCount(); i++) {
            if (cbCategory.getItemAt(i).getId() == p.getCategoryId()) {
                cbCategory.setSelectedIndex(i);
                break;
            }
        }
        // pilih satuan
        if (p.getUnit() != null) {
            for (int i = 0; i < cbUnit.getItemCount(); i++) {
                if (cbUnit.getItemAt(i).equalsIgnoreCase(p.getUnit())) {
                    cbUnit.setSelectedIndex(i);
                    break;
                }
            }
        }

        // SKU tidak boleh diubah saat edit
        txtSku.setEditable(false);
        txtSku.setBackground(new Color(0xF0F0F0));
    }

    // ===================================================================
    // 6. Simpan (create / update)
    // ===================================================================
    private void onSave() {
        if (!validateForm()) return;

        try {
            Product p = (existing == null) ? new Product() : existing;

            p.setSku(txtSku.getText().trim().toUpperCase());
            p.setName(txtName.getText().trim());
            p.setCategoryId(((Category) cbCategory.getSelectedItem()).getId());
            p.setDescription(txtDescription.getText().trim());
            p.setPrice(parsePrice(txtPrice.getText().trim()));
            p.setStock(Integer.parseInt(txtStock.getText().trim()));
            p.setUnit((String) cbUnit.getSelectedItem());

            if (existing == null) {
                service.createProduct(p);
            } else {
                service.updateProduct(p);
            }

            saved = true;
            JOptionPane.showMessageDialog(this,
                    existing == null
                            ? "Produk berhasil ditambahkan."
                            : "Produk berhasil diperbarui.",
                    "Sukses", JOptionPane.INFORMATION_MESSAGE);
            dispose();

        } catch (SQLException ex) {
            if (ex.getMessage() != null
                    && ex.getMessage().toLowerCase().contains("sku")) {
                setError(errSku, "SKU sudah digunakan, gunakan yang lain.");
            } else {
                JOptionPane.showMessageDialog(this,
                        "Gagal menyimpan produk:\n" + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this,
                    ex.getMessage(), "Validasi", JOptionPane.WARNING_MESSAGE);
        }
    }

    /** Harga boleh ditulis dengan titik ribuan: "65.000" → 65000. */
    private BigDecimal parsePrice(String raw) {
        return new BigDecimal(raw.replace(".", "").replace(",", "."));
    }

    // ===================================================================
    // 7. Validasi form
    // ===================================================================
    private boolean validateForm() {
        boolean ok = true;

        // --- SKU ---
        String sku = txtSku.getText().trim();
        if (existing == null) {  // SKU hanya wajib & dicek format saat create
            if (sku.isEmpty()) {
                setError(errSku, "SKU wajib diisi."); ok = false;
            } else if (!SKU_PATTERN.matcher(sku).matches()) {
                setError(errSku,
                        "SKU hanya boleh huruf, angka, dan '-' (maks 30).");
                ok = false;
            } else clearError(errSku);
        }

        // --- Nama ---
        String name = txtName.getText().trim();
        if (name.length() < 3 || name.length() > 100) {
            setError(errName, "Nama produk harus 3–100 karakter."); ok = false;
        } else clearError(errName);

        // --- Kategori ---
        Category cat = (Category) cbCategory.getSelectedItem();
        if (cat == null || cat.getId() == 0) {
            setError(errCategory, "Pilih kategori terlebih dahulu."); ok = false;
        } else clearError(errCategory);

        // --- Deskripsi ---
        if (txtDescription.getText().length() > 500) {
            JOptionPane.showMessageDialog(this,
                    "Deskripsi maksimal 500 karakter.",
                    "Validasi", JOptionPane.WARNING_MESSAGE);
            ok = false;
        }

        // --- Harga ---
        String priceStr = txtPrice.getText().trim();
        if (priceStr.isEmpty()) {
            setError(errPrice, "Harga wajib diisi."); ok = false;
        } else {
            try {
                BigDecimal price = parsePrice(priceStr);
                if (price.compareTo(BigDecimal.ZERO) < 0) {
                    setError(errPrice, "Harga tidak boleh negatif."); ok = false;
                } else clearError(errPrice);
            } catch (NumberFormatException ex) {
                setError(errPrice, "Harga harus berupa angka."); ok = false;
            }
        }

        // --- Stok ---
        String stockStr = txtStock.getText().trim();
        if (stockStr.isEmpty()) {
            setError(errStock, "Stok wajib diisi."); ok = false;
        } else {
            try {
                int stock = Integer.parseInt(stockStr);
                if (stock < 0) {
                    setError(errStock, "Stok tidak boleh negatif."); ok = false;
                } else clearError(errStock);
            } catch (NumberFormatException ex) {
                setError(errStock,
                        "Stok harus berupa bilangan bulat."); ok = false;
            }
        }

        // --- Satuan ---
        if (cbUnit.getSelectedIndex() <= 0) {
            setError(errUnit, "Pilih satuan produk."); ok = false;
        } else clearError(errUnit);

        return ok;
    }

    private void setError(JLabel lbl, String msg) {
        lbl.setText(msg);
        lbl.setForeground(new Color(0xC0392B));
    }

    private void clearError(JLabel lbl) {
        lbl.setText(" ");
    }
}