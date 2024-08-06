package com.example.vccorp_task6;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SearchUsers {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/user_management";
    private static final String USER = "root";
    private static final String PASS = "";

    public static void main(String[] args) {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            long startTime;
            List<String> results;

            // Tìm người dùng có tên bắt đầu bằng chữ 'H'
            startTime = System.currentTimeMillis();
            results = searchUsersStartingWith(conn, "H");
            System.out.println("Tìm người dùng có tên bắt đầu bằng chữ 'H': " + results.size() + " kết quả");
            System.out.println("Thời gian thực thi: " + (System.currentTimeMillis() - startTime) + "ms");

            // Tìm người dùng có tên có chứa ký tự 'H'
            startTime = System.currentTimeMillis();
            results = searchUsersContaining(conn, "H");
            System.out.println("Tìm người dùng có tên có chứa ký tự 'H': " + results.size() + " kết quả");
            System.out.println("Thời gian thực thi: " + (System.currentTimeMillis() - startTime) + "ms");

            // Tìm người dùng có tên là 'Nguyen Van An'
            startTime = System.currentTimeMillis();
            results = searchExactName(conn, "Nguyen Van An");
            System.out.println("Tìm người dùng có tên là 'Nguyen Van An': " + results.size() + " kết quả");
            System.out.println("Thời gian thực thi: " + (System.currentTimeMillis() - startTime) + "ms");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static List<String> searchUsersStartingWith(Connection conn, String prefix) throws SQLException {
        String sql = "SELECT full_name FROM users WHERE full_name LIKE ? LIMIT 1000000";
        List<String> results = new ArrayList<>();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, prefix + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    results.add(rs.getString("full_name"));
                }
            }
        }
        return results;
    }

    private static List<String> searchUsersContaining(Connection conn, String substring) throws SQLException {
        String sql = "SELECT full_name FROM users WHERE MATCH(full_name) AGAINST(? IN BOOLEAN MODE) LIMIT 10000";
        List<String> results = new ArrayList<>();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "*" + substring + "*");
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    results.add(rs.getString("full_name"));
                }
            }
        }
        return results;
    }

    private static List<String> searchExactName(Connection conn, String name) throws SQLException {
        String sql = "SELECT full_name FROM users WHERE full_name = ?";
        List<String> results = new ArrayList<>();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    results.add(rs.getString("full_name"));
                }
            }
        }
        return results;
    }
}