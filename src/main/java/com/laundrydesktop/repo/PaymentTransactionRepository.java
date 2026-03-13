package com.laundrydesktop.repo;

import com.laundrydesktop.db.DatabaseManager;
import com.laundrydesktop.model.CashInEntry;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PaymentTransactionRepository {
    public void create(String invoiceNo, String paymentDate, int amount, String paymentMethod, String note) {
        String sql = """
                INSERT INTO payment_transactions(invoice_no, payment_date, amount, payment_method, note)
                VALUES(?,?,?,?,?)
                """;
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, invoiceNo);
            ps.setString(2, paymentDate);
            ps.setInt(3, amount);
            ps.setString(4, paymentMethod);
            ps.setString(5, note == null ? "" : note);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Gagal menyimpan transaksi kas masuk", e);
        }
    }

    public List<CashInEntry> findByDateRange(LocalDate startDate, LocalDate endDate) {
        List<CashInEntry> rows = new ArrayList<>();
        String sql = """
                SELECT p.payment_date,
                       p.invoice_no,
                       c.name AS customer_name,
                       p.payment_method,
                       p.amount,
                       p.note
                FROM payment_transactions p
                JOIN orders o ON o.invoice_no = p.invoice_no
                JOIN customers c ON c.id = o.customer_id
                WHERE p.payment_date >= ? AND p.payment_date <= ?
                ORDER BY p.payment_date, p.id
                """;
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, startDate.toString());
            ps.setString(2, endDate.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(new CashInEntry(
                            rs.getString("payment_date"),
                            rs.getString("invoice_no"),
                            rs.getString("customer_name"),
                            rs.getString("payment_method"),
                            rs.getInt("amount"),
                            rs.getString("note")
                    ));
                }
            }
            return rows;
        } catch (SQLException e) {
            throw new RuntimeException("Gagal mengambil data kas masuk", e);
        }
    }

    public int sumByDateRange(LocalDate startDate, LocalDate endDate) {
        String sql = """
                SELECT COALESCE(SUM(amount), 0)
                FROM payment_transactions
                WHERE payment_date >= ? AND payment_date <= ?
                """;
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, startDate.toString());
            ps.setString(2, endDate.toString());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Gagal menghitung total kas masuk", e);
        }
    }

    public void deleteByInvoiceNo(String invoiceNo) {
        String sql = "DELETE FROM payment_transactions WHERE invoice_no = ?";
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, invoiceNo);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Gagal menghapus histori pembayaran order", e);
        }
    }
}
