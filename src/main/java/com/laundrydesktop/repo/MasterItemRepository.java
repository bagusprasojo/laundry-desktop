package com.laundrydesktop.repo;

import com.laundrydesktop.db.DatabaseManager;
import com.laundrydesktop.model.MasterItem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MasterItemRepository {
    private static final Set<String> ALLOWED_TABLES = Set.of("services", "speeds", "units");

    public List<MasterItem> findAll(String table) {
        String safeTable = sanitizeTable(table);
        List<MasterItem> items = new ArrayList<>();
        String sql = "SELECT id, name FROM " + safeTable + " ORDER BY name";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                items.add(new MasterItem(rs.getInt("id"), rs.getString("name")));
            }
            return items;
        } catch (SQLException e) {
            throw new RuntimeException("Gagal mengambil data master " + safeTable, e);
        }
    }

    public int countByKeyword(String table, String keyword) {
        String safeTable = sanitizeTable(table);
        String sql = "SELECT COUNT(1) FROM " + safeTable + " WHERE lower(name) LIKE ?";
        String search = "%" + keyword.toLowerCase() + "%";
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, search);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Gagal menghitung data master " + safeTable, e);
        }
    }

    public List<MasterItem> findPagedByKeyword(String table, String keyword, int limit, int offset) {
        String safeTable = sanitizeTable(table);
        List<MasterItem> items = new ArrayList<>();
        String sql = "SELECT id, name FROM " + safeTable + " WHERE lower(name) LIKE ? ORDER BY name LIMIT ? OFFSET ?";
        String search = "%" + keyword.toLowerCase() + "%";
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, search);
            ps.setInt(2, limit);
            ps.setInt(3, offset);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    items.add(new MasterItem(rs.getInt("id"), rs.getString("name")));
                }
            }
            return items;
        } catch (SQLException e) {
            throw new RuntimeException("Gagal mengambil data master " + safeTable, e);
        }
    }

    public void create(String table, String name) {
        String safeTable = sanitizeTable(table);
        String sql = "INSERT INTO " + safeTable + "(name) VALUES(?)";
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Gagal menambah data master " + safeTable, e);
        }
    }

    public void update(String table, int id, String name) {
        String safeTable = sanitizeTable(table);
        String sql = "UPDATE " + safeTable + " SET name = ? WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setInt(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Gagal mengubah data master " + safeTable, e);
        }
    }

    public void delete(String table, int id) {
        String safeTable = sanitizeTable(table);
        String sql = "DELETE FROM " + safeTable + " WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Gagal menghapus data master " + safeTable, e);
        }
    }

    private String sanitizeTable(String table) {
        if (!ALLOWED_TABLES.contains(table)) {
            throw new IllegalArgumentException("Table master tidak valid: " + table);
        }
        return table;
    }
}
