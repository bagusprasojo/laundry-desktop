package com.laundrydesktop.repo;

import com.laundrydesktop.db.DatabaseManager;
import com.laundrydesktop.model.ServicePrice;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ServicePriceRepository {
    public List<ServicePrice> findAll() {
        List<ServicePrice> prices = new ArrayList<>();
        String sql = "SELECT service_name, speed_name, unit_name, price FROM service_prices ORDER BY service_name, speed_name";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                prices.add(new ServicePrice(
                        rs.getString("service_name"),
                        rs.getString("speed_name"),
                        rs.getString("unit_name"),
                        rs.getInt("price")
                ));
            }
            return prices;
        } catch (SQLException e) {
            throw new RuntimeException("Gagal mengambil harga layanan", e);
        }
    }

    public void upsert(String serviceName, String speedName, String unitName, int price) {
        String sql = """
                INSERT INTO service_prices(service_name, speed_name, unit_name, price)
                VALUES(?,?,?,?)
                ON CONFLICT(service_name, speed_name, unit_name)
                DO UPDATE SET price=excluded.price
                """;
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, serviceName);
            ps.setString(2, speedName);
            ps.setString(3, unitName);
            ps.setInt(4, price);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Gagal menyimpan harga layanan", e);
        }
    }

    public void delete(String serviceName, String speedName, String unitName) {
        String sql = "DELETE FROM service_prices WHERE service_name = ? AND speed_name = ? AND unit_name = ?";
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, serviceName);
            ps.setString(2, speedName);
            ps.setString(3, unitName);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Gagal menghapus harga layanan", e);
        }
    }

    public int countByKeyword(String keyword) {
        String search = "%" + keyword.toLowerCase() + "%";
        String sql = """
                SELECT COUNT(1)
                FROM service_prices
                WHERE lower(service_name) LIKE ?
                   OR lower(speed_name) LIKE ?
                   OR lower(unit_name) LIKE ?
                """;
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, search);
            ps.setString(2, search);
            ps.setString(3, search);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Gagal menghitung harga layanan", e);
        }
    }

    public List<ServicePrice> findPagedByKeyword(String keyword, int limit, int offset) {
        List<ServicePrice> prices = new ArrayList<>();
        String search = "%" + keyword.toLowerCase() + "%";
        String sql = """
                SELECT service_name, speed_name, unit_name, price
                FROM service_prices
                WHERE lower(service_name) LIKE ?
                   OR lower(speed_name) LIKE ?
                   OR lower(unit_name) LIKE ?
                ORDER BY service_name, speed_name, unit_name
                LIMIT ? OFFSET ?
                """;
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, search);
            ps.setString(2, search);
            ps.setString(3, search);
            ps.setInt(4, limit);
            ps.setInt(5, offset);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    prices.add(new ServicePrice(
                            rs.getString("service_name"),
                            rs.getString("speed_name"),
                            rs.getString("unit_name"),
                            rs.getInt("price")
                    ));
                }
            }
            return prices;
        } catch (SQLException e) {
            throw new RuntimeException("Gagal mengambil harga layanan paginasi", e);
        }
    }
}
