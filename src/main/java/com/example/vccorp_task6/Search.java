package com.example.vccorp_task6;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
public class Search {

        private static final String DB_URL = "jdbc:mysql://localhost:3306/user_management";
        private static final String USER = "root";
        private static final String PASS = "";
        private static final int THREAD_POOL_SIZE = 4;

        private static final ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

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
            } finally {
                executor.shutdown();
            }
        }

        private static List<String> searchUsersStartingWith(Connection conn, String prefix) throws SQLException {
            String sql = "SELECT full_name FROM users USE INDEX (idx_full_name) WHERE full_name LIKE ? LIMIT 1000000";
            return parallelSearch(conn, sql, prefix + "%");
        }

        private static List<String> searchUsersContaining(Connection conn, String substring) throws SQLException {
            String sql = "SELECT full_name FROM users USE INDEX (ft_full_name) WHERE MATCH(full_name) AGAINST(? IN BOOLEAN MODE) LIMIT 1000000";
            return parallelSearch(conn, sql, "+" + substring + "*");
        }

        private static List<String> searchExactName(Connection conn, String name) throws SQLException {
            String sql = "SELECT full_name FROM users USE INDEX (idx_full_name) WHERE full_name = ?";
            return parallelSearch(conn, sql, name);
        }

        private static List<String> parallelSearch(Connection conn, String sql, String param) throws SQLException {
            int totalCount = getTotalCount(conn, sql, param);
            int batchSize = Math.max(1000, totalCount / THREAD_POOL_SIZE);
            List<Future<List<String>>> futures = new ArrayList<>();

            for (int i = 0; i < totalCount; i += batchSize) {
                final int offset = i;
                futures.add(executor.submit(() -> searchBatch(conn, sql, param, offset, batchSize)));
            }

            List<String> results = new ArrayList<>();
            for (Future<List<String>> future : futures) {
                try {
                    results.addAll(future.get());
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }

            return results;
        }

        private static List<String> searchBatch(Connection conn, String sql, String param, int offset, int limit) throws SQLException {
            List<String> results = new ArrayList<>();
            try (PreparedStatement pstmt = conn.prepareStatement(sql + " LIMIT ? OFFSET ?")) {
                pstmt.setString(1, param);
                pstmt.setInt(2, limit);
                pstmt.setInt(3, offset);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        results.add(rs.getString("full_name"));
                    }
                }
            }
            return results;
        }

        private static int getTotalCount(Connection conn, String sql, String param) throws SQLException {
            String countSql = "SELECT COUNT(*) FROM (" + sql + ") AS count_query";
            try (PreparedStatement pstmt = conn.prepareStatement(countSql)) {
                pstmt.setString(1, param);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
            return 0;
        }
    }
