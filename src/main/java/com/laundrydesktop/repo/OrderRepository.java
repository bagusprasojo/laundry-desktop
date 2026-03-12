package com.laundrydesktop.repo;

import com.laundrydesktop.db.DatabaseManager;
import com.laundrydesktop.model.Order;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class OrderRepository {
    public void create(Order order) {
        String sql = """
                INSERT INTO orders(
                    invoice_no, customer_id, service_name, speed_name, unit_name, quantity,
                    unit_price, total_price, order_status, payment_status, payment_method,
                    down_payment, paid_amount, order_date, estimate_done
                ) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)
                """;
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, order.invoiceNo());
            ps.setInt(2, order.customerId());
            ps.setString(3, order.serviceName());
            ps.setString(4, order.speedName());
            ps.setString(5, order.unitName());
            ps.setDouble(6, order.quantity());
            ps.setInt(7, order.unitPrice());
            ps.setInt(8, order.totalPrice());
            ps.setString(9, order.orderStatus());
            ps.setString(10, order.paymentStatus());
            ps.setString(11, order.paymentMethod());
            ps.setInt(12, order.downPayment());
            ps.setInt(13, order.paidAmount());
            ps.setString(14, order.orderDate());
            ps.setString(15, order.estimateDone());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Gagal menyimpan order", e);
        }
    }

    public List<Order> findByDate(LocalDate date) {
        List<Order> orders = new ArrayList<>();
        String sql = """
                SELECT o.invoice_no, o.customer_id, c.name customer_name, o.service_name, o.speed_name, o.unit_name,
                       o.quantity, o.unit_price, o.total_price, o.order_status, o.payment_status, o.payment_method,
                       o.down_payment, o.paid_amount, o.order_date, o.estimate_done
                FROM orders o
                JOIN customers c ON c.id = o.customer_id
                WHERE o.order_date = ?
                ORDER BY o.id DESC
                """;
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, date.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    orders.add(map(rs));
                }
            }
            return orders;
        } catch (SQLException e) {
            throw new RuntimeException("Gagal mengambil order harian", e);
        }
    }

    public List<Order> findAll() {
        List<Order> orders = new ArrayList<>();
        String sql = """
                SELECT o.invoice_no, o.customer_id, c.name customer_name, o.service_name, o.speed_name, o.unit_name,
                       o.quantity, o.unit_price, o.total_price, o.order_status, o.payment_status, o.payment_method,
                       o.down_payment, o.paid_amount, o.order_date, o.estimate_done
                FROM orders o
                JOIN customers c ON c.id = o.customer_id
                ORDER BY o.id DESC
                """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                orders.add(map(rs));
            }
            return orders;
        } catch (SQLException e) {
            throw new RuntimeException("Gagal mengambil semua order", e);
        }
    }

    public List<Order> findByDateRange(LocalDate startDate, LocalDate endDate) {
        List<Order> orders = new ArrayList<>();
        String sql = """
                SELECT o.invoice_no, o.customer_id, c.name customer_name, o.service_name, o.speed_name, o.unit_name,
                       o.quantity, o.unit_price, o.total_price, o.order_status, o.payment_status, o.payment_method,
                       o.down_payment, o.paid_amount, o.order_date, o.estimate_done
                FROM orders o
                JOIN customers c ON c.id = o.customer_id
                WHERE o.order_date >= ? AND o.order_date <= ?
                ORDER BY o.order_date, o.id DESC
                """;
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, startDate.toString());
            ps.setString(2, endDate.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    orders.add(map(rs));
                }
            }
            return orders;
        } catch (SQLException e) {
            throw new RuntimeException("Gagal mengambil order berdasarkan periode", e);
        }
    }

    public List<Order> findNotPickedUp() {
        List<Order> orders = new ArrayList<>();
        String sql = """
                SELECT o.invoice_no, o.customer_id, c.name customer_name, o.service_name, o.speed_name, o.unit_name,
                       o.quantity, o.unit_price, o.total_price, o.order_status, o.payment_status, o.payment_method,
                       o.down_payment, o.paid_amount, o.order_date, o.estimate_done
                FROM orders o
                JOIN customers c ON c.id = o.customer_id
                WHERE o.order_status <> 'Sudah Diambil'
                ORDER BY o.id DESC
                """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                orders.add(map(rs));
            }
            return orders;
        } catch (SQLException e) {
            throw new RuntimeException("Gagal mengambil order belum diambil", e);
        }
    }

    public void updatePickup(String invoiceNo, int paidAmount, String paymentStatus, String orderStatus, String paymentMethod) {
        String sql = """
                UPDATE orders
                SET paid_amount = ?, payment_status = ?, order_status = ?, payment_method = ?
                WHERE invoice_no = ?
                """;
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, paidAmount);
            ps.setString(2, paymentStatus);
            ps.setString(3, orderStatus);
            ps.setString(4, paymentMethod);
            ps.setString(5, invoiceNo);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Gagal memproses pengambilan order", e);
        }
    }

    private Order map(ResultSet rs) throws SQLException {
        return new Order(
                rs.getString("invoice_no"),
                rs.getInt("customer_id"),
                rs.getString("customer_name"),
                rs.getString("service_name"),
                rs.getString("speed_name"),
                rs.getString("unit_name"),
                rs.getDouble("quantity"),
                rs.getInt("unit_price"),
                rs.getInt("total_price"),
                rs.getString("order_status"),
                rs.getString("payment_status"),
                rs.getString("payment_method"),
                rs.getInt("down_payment"),
                rs.getInt("paid_amount"),
                rs.getString("order_date"),
                rs.getString("estimate_done")
        );
    }
}
