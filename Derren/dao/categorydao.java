package com.kopdes.kmp.dao;

import com.kopdes.kmp.config.DatabaseConnection;
import com.kopdes.kmp.model.Category;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategoryDAO implements GenericDAO<Category, Integer> {

    @Override
    public Category findById(Integer id) throws SQLException {
        String sql = "SELECT * FROM category WHERE id = ?";
        try (Connection c = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    @Override
    public List<Category> findAll() throws SQLException {
        String sql = "SELECT * FROM category ORDER BY name";
        List<Category> list = new ArrayList<>();
        try (Connection c = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    @Override
    public Category save(Category entity) throws SQLException {
        String sql = "INSERT INTO category(name, description) VALUES (?, ?) RETURNING id";
        try (Connection c = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, entity.getName());
            ps.setString(2, entity.getDescription());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) entity.setId(rs.getInt(1));
            }
        }
        return entity;
    }

    @Override
    public boolean update(Category entity) throws SQLException {
        String sql = "UPDATE category SET name=?, description=? WHERE id=?";
        try (Connection c = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, entity.getName());
            ps.setString(2, entity.getDescription());
            ps.setInt(3, entity.getId());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(Integer id) throws SQLException {
        String sql = "DELETE FROM category WHERE id=?";
        try (Connection c = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private Category map(ResultSet rs) throws SQLException {
        return new Category(rs.getInt("id"),
                rs.getString("name"),
                rs.getString("description"));
    }
}