package com.kopdes.kmp.dao;

import com.kopdes.kmp.config.DatabaseConnection;
import com.kopdes.kmp.model.StockHistory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StockHistoryDAO {

    public List<StockHistory> findByProductId(int productId) throws SQLException {
        String sql = "SELECT * FROM stock_history WHERE product_id=? ORDER BY changed_at DESC";
        List<StockHistory> list = new ArrayList<>();
        try (Connection c = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    StockHistory h = new StockHistory(
                            rs.getInt("id"),
                            rs.getInt("product_id"),
                            rs.getInt("change_qty"),
                            rs.getString("change_type"),
                            rs.getString("note"),
                            rs.getTimestamp("changed_at").toLocalDateTime()
                    );
                    list.add(h);
                }
            }
        }
        return list;
    }

    public void save(StockHistory h) throws SQLException {
        String sql = "INSERT INTO stock_history(product_id, change_qty, change_type, note) " +
                     "VALUES (?, ?, ?, ?) RETURNING id";
        try (Connection c = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, h.getProductId());
            ps.setInt(2, h.getChangeQty());
            ps.setString(3, h.getChangeType());
            ps.setString(4, h.getNote());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) h.setId(rs.getInt(1));
            }
        }
    }
}