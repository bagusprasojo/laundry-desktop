package com.laundrydesktop.repo;

import com.laundrydesktop.db.DatabaseManager;
import com.laundrydesktop.model.BusinessProfile;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class BusinessProfileRepository {
    public BusinessProfile get() {
        String sql = "SELECT business_name, address, owner_name, phone FROM business_profile WHERE id = 1";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return new BusinessProfile(
                        safe(rs.getString("business_name")),
                        safe(rs.getString("address")),
                        safe(rs.getString("owner_name")),
                        safe(rs.getString("phone"))
                );
            }
            return new BusinessProfile("Belum diatur", "", "", "");
        } catch (SQLException e) {
            throw new RuntimeException("Gagal mengambil profil usaha", e);
        }
    }

    public void upsert(BusinessProfile profile) {
        String sql = """
                INSERT INTO business_profile(id, business_name, address, owner_name, phone)
                VALUES(1, ?, ?, ?, ?)
                ON CONFLICT(id) DO UPDATE SET
                    business_name = excluded.business_name,
                    address = excluded.address,
                    owner_name = excluded.owner_name,
                    phone = excluded.phone
                """;
        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, profile.businessName());
            ps.setString(2, profile.address());
            ps.setString(3, profile.ownerName());
            ps.setString(4, profile.phone());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Gagal menyimpan profil usaha", e);
        }
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
