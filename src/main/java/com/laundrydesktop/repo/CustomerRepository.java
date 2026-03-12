package com.laundrydesktop.repo;

import com.laundrydesktop.db.DatabaseManager;
import com.laundrydesktop.model.Customer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CustomerRepository {
    public List<Customer> findAll() {
        List<Customer> customers = new ArrayList<>();
        String sql = "SELECT id, name, phone, address, note FROM customers ORDER BY name";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                customers.add(new Customer(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("phone"),
                        rs.getString("address"),
                        rs.getString("note")
                ));
            }
            return customers;
        } catch (SQLException e) {
            throw new RuntimeException("Gagal mengambil data pelanggan", e);
        }
    }

    public void create(String name, String phone, String address, String note) {
        String sql = "INSERT INTO customers(name, phone, address, note) VALUES(?,?,?,?)";
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, phone);
            ps.setString(3, address);
            ps.setString(4, note);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Gagal menambah pelanggan", e);
        }
    }

    public void update(int id, String name, String phone, String address, String note) {
        String sql = "UPDATE customers SET name = ?, phone = ?, address = ?, note = ? WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, phone);
            ps.setString(3, address);
            ps.setString(4, note);
            ps.setInt(5, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Gagal mengubah pelanggan", e);
        }
    }

    public int countByKeyword(String keyword) {
        String sql = """
                SELECT COUNT(1)
                FROM customers
                WHERE lower(name) LIKE ? OR lower(phone) LIKE ?
                """;
        String search = "%" + keyword.toLowerCase() + "%";
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, search);
            ps.setString(2, search);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Gagal menghitung pelanggan", e);
        }
    }

    public List<Customer> findPagedByKeyword(String keyword, int limit, int offset) {
        List<Customer> customers = new ArrayList<>();
        String sql = """
                SELECT id, name, phone, address, note
                FROM customers
                WHERE lower(name) LIKE ? OR lower(phone) LIKE ?
                ORDER BY name
                LIMIT ? OFFSET ?
                """;
        String search = "%" + keyword.toLowerCase() + "%";
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, search);
            ps.setString(2, search);
            ps.setInt(3, limit);
            ps.setInt(4, offset);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    customers.add(new Customer(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getString("phone"),
                            rs.getString("address"),
                            rs.getString("note")
                    ));
                }
            }
            return customers;
        } catch (SQLException e) {
            throw new RuntimeException("Gagal mengambil pelanggan paginasi", e);
        }
    }
}
