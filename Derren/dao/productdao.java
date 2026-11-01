package com.kopdes.kmp.dao;

import com.kopdes.kmp.config.DatabaseConnection;
import com.kopdes.kmp.model.Product;
import com.kopdes.kmp.model.ProductInfo;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO implements GenericDAO<Product, Integer> {

    private static final String BASE_SELECT =
            "SELECT p.*, c.name AS category_name FROM product p " +
            "JOIN category c ON c.id = p.category_id ";

    @Override
    public Product findById(Integer id) throws SQLException {
        String sql = BASE_SELECT + "WHERE p.id = ?";
        try (Connection c = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    @Override
    public List<Product> findAll() throws SQLException {
        String sql = BASE_SELECT + "ORDER BY p.name";
        List<Product> list = new ArrayList<>();
        try (Connection c = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public List<Product> search(String keyword, Integer categoryId) throws SQLException {
        StringBuilder sql = new StringBuilder(BASE_SELECT).append("WHERE 1=1 ");
        if (keyword != null && !keyword.isBlank()) sql.append("AND p.name ILIKE ? ");
        if (categoryId != null) sql.append("AND p.category_id = ? ");
        sql.append("ORDER BY p.name");

        List<Product> list = new ArrayList<>();
        try (Connection c = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql.toString())) {
            int i = 1;
            if (keyword != null && !keyword.isBlank()) ps.setString(i++, "%" + keyword + "%");
            if (categoryId != null) ps.setInt(i, categoryId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    @Override
    public Product save(Product p) throws SQLException {
        String sql = "INSERT INTO product(sku, name, category_id, description, price, stock, unit) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?) RETURNING id";
        try (Connection c = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, p);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) p.setId(rs.getInt(1));
            }
        }
        return p;
    }

    @Override
    public boolean update(Product p) throws SQLException {
        String sql = "UPDATE product SET sku=?, name=?, category_id=?, description=?, " +
                     "price=?, stock=?, unit=? WHERE id=?";
        try (Connection c = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, p);
            ps.setInt(8, p.getId());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(Integer id) throws SQLException {
        String sql = "DELETE FROM product WHERE id=?";
        try (Connection c = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private void bind(PreparedStatement ps, Product p) throws SQLException {
        ps.setString(1, p.getSku());
        ps.setString(2, p.getName());
        ps.setInt(3, p.getCategoryId());
        ps.setString(4, p.getDescription());
        ps.setBigDecimal(5, p.getPrice());
        ps.setInt(6, p.getStock());
        ps.setString(7, p.getUnit());
    }

    private Product map(ResultSet rs) throws SQLException {
        Product p = new Product(
                rs.getInt("id"),
                rs.getString("sku"),
                rs.getString("name"),
                rs.getInt("category_id"),
                rs.getString("category_name"),
                rs.getString("description"),
                rs.getBigDecimal("price"),
                rs.getInt("stock"),
                rs.getString("unit")
        );
        p.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        p.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());

        // Baca composite type product_info (ORDBMS)
        try {
            String composite = rs.getString("info");
            if (composite != null && composite.startsWith("(")) {
                // format: (nama,kategori,harga,stok)
                String inner = composite.substring(1, composite.length() - 1);
                String[] parts = inner.split(",", 4);
                if (parts.length == 4) {
                    p.setInfo(new ProductInfo(
                            parts[0],
                            parts[1],
                            new BigDecimal(parts[2].trim()),
                            Integer.parseInt(parts[3].trim())
                    ));
                }
            }
        } catch (Exception ignored) { /* fallback null */ }

        return p;
    }
}