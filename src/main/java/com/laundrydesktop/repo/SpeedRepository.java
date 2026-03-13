package com.laundrydesktop.repo;

import com.laundrydesktop.db.DatabaseManager;
import com.laundrydesktop.model.SpeedMaster;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SpeedRepository {
    public List<SpeedMaster> findAll() {
        List<SpeedMaster> items = new ArrayList<>();
        String sql = "SELECT id, name, duration_hours FROM speeds ORDER BY name";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                items.add(new SpeedMaster(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getInt("duration_hours")
                ));
            }
            return items;
        } catch (SQLException e) {
            throw new RuntimeException("Gagal mengambil master kecepatan", e);
        }
    }

    public int countByKeyword(String keyword) {
        String sql = "SELECT COUNT(1) FROM speeds WHERE lower(name) LIKE ?";
        String search = "%" + keyword.toLowerCase() + "%";
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, search);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Gagal menghitung data kecepatan", e);
        }
    }

    public List<SpeedMaster> findPagedByKeyword(String keyword, int limit, int offset) {
        List<SpeedMaster> items = new ArrayList<>();
        String sql = """
                SELECT id, name, duration_hours
                FROM speeds
                WHERE lower(name) LIKE ?
                ORDER BY name
                LIMIT ? OFFSET ?
                """;
        String search = "%" + keyword.toLowerCase() + "%";
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, search);
            ps.setInt(2, limit);
            ps.setInt(3, offset);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    items.add(new SpeedMaster(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getInt("duration_hours")
                    ));
                }
            }
            return items;
        } catch (SQLException e) {
            throw new RuntimeException("Gagal mengambil paginasi kecepatan", e);
        }
    }

    public void create(String name, int durationHours) {
        String sql = "INSERT INTO speeds(name, duration_hours) VALUES(?, ?)";
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setInt(2, durationHours);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Gagal menambah kecepatan", e);
        }
    }

    public void update(int id, String name, int durationHours) {
        String sql = "UPDATE speeds SET name = ?, duration_hours = ? WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setInt(2, durationHours);
            ps.setInt(3, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Gagal mengubah kecepatan", e);
        }
    }

    public void delete(int id) {
        String sql = "DELETE FROM speeds WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Gagal menghapus kecepatan", e);
        }
    }

    public int findDurationHoursByName(String speedName) {
        if (speedName == null || speedName.isBlank()) {
            return 0;
        }
        String sql = "SELECT duration_hours FROM speeds WHERE name = ? LIMIT 1";
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, speedName);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Gagal mengambil durasi estimasi kecepatan", e);
        }
    }
}
