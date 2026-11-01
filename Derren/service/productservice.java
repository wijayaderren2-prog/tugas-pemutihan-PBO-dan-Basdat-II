package com.kopdes.kmp.service;

import com.kopdes.kmp.config.DatabaseConnection;
import com.kopdes.kmp.dao.ProductDAO;
import com.kopdes.kmp.dao.StockHistoryDAO;
import com.kopdes.kmp.model.Product;
import com.kopdes.kmp.model.StockHistory;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class ProductService {
    private final ProductDAO productDAO = new ProductDAO();
    private final StockHistoryDAO historyDAO = new StockHistoryDAO();

    public List<Product> listAll() throws SQLException {
        return productDAO.findAll();
    }

    public List<Product> search(String keyword, Integer categoryId) throws SQLException {
        return productDAO.search(keyword, categoryId);
    }

    public Product findById(int id) throws SQLException {
        return productDAO.findById(id);
    }

    /** Validasi bisnis sebelum simpan/update. */
    public void validate(Product p) {
        if (p.getSku() == null || p.getSku().isBlank())
            throw new IllegalArgumentException("SKU wajib diisi.");
        if (p.getName() == null || p.getName().isBlank())
            throw new IllegalArgumentException("Nama produk wajib diisi.");
        if (p.getCategoryId() == null)
            throw new IllegalArgumentException("Kategori wajib dipilih.");
        if (p.getPrice() == null || p.getPrice().compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Harga harus > 0.");
        if (p.getStock() < 0)
            throw new IllegalArgumentException("Stok tidak boleh negatif.");
        if (p.getUnit() == null || p.getUnit().isBlank())
            throw new IllegalArgumentException("Satuan wajib diisi.");
    }

    /**
     * Create produk + catat riwayat stok awal (transaksi).
     */
    public Product create(Product p) throws SQLException {
        validate(p);
        Connection c = DatabaseConnection.getInstance().getConnection();
        try {
            c.setAutoCommit(false);
            // Gunakan DAO dengan koneksi yang sama secara manual
            Product saved = productDAO.save(p);
            if (saved.getStock() > 0) {
                StockHistory h = new StockHistory();
                h.setProductId(saved.getId());
                h.setChangeQty(saved.getStock());
                h.setChangeType("IN");
                h.setNote("Stok awal produk baru");
                historyDAO.save(h);
            }
            c.commit();
            return saved;
        } catch (Exception e) {
            c.rollback();
            throw new SQLException(e.getMessage(), e);
        } finally {
            c.setAutoCommit(true);
            c.close();
        }
    }

    public boolean update(Product p) throws SQLException {
        validate(p);
        Product old = productDAO.findById(p.getId());
        boolean ok = productDAO.update(p);
        if (ok && old != null && old.getStock() != p.getStock()) {
            int diff = p.getStock() - old.getStock();
            StockHistory h = new StockHistory();
            h.setProductId(p.getId());
            h.setChangeQty(diff);
            h.setChangeType(diff >= 0 ? "IN" : "OUT");
            h.setNote("Update stok dari " + old.getStock() + " ke " + p.getStock());
            historyDAO.save(h);
        }
        return ok;
    }

    public boolean delete(int id) throws SQLException {
        return productDAO.delete(id);
    }

    public List<StockHistory> getHistory(int productId) throws SQLException {
        return historyDAO.findByProductId(productId);
    }
}