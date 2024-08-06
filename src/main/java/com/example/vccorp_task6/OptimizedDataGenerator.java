package com.example.vccorp_task6;
import java.sql.*;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

    public class OptimizedDataGenerator {
        private static final String DB_URL = "jdbc:mysql://localhost:3306/dbtask6";
        private static final String USER = "root";
        private static final String PASS = "";

        private static final String[] FIRST_NAMES = {"Nguyen", "Tran", "Le", "Pham", "Hoang", "Huynh", "Vu", "Dang", "Bui", "Do"};
        private static final String[] MIDDLE_NAMES = {"Van", "Thi", "Huu", "Duc", "Minh", "Quang", "Thanh", "Tuan", "Hong", "Ngoc"};
        private static final String[] LAST_NAMES = {"An", "Binh", "Cuong", "Dung", "Em", "Giang", "Hai", "Khanh", "Lan", "Minh"};

        private static final int TOTAL_RECORDS = 5_000_000;
        private static final int BATCH_SIZE = 10_000;
        private static final int NUM_THREADS = Runtime.getRuntime().availableProcessors();

        public static void main(String[] args) throws SQLException, InterruptedException {
            Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
            conn.setAutoCommit(false);

            String sql = "INSERT INTO users (full_name) VALUES (?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);

            ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);

            for (int i = 0; i < NUM_THREADS; i++) {
                executor.submit(new DataGeneratorTask(conn, pstmt, TOTAL_RECORDS / NUM_THREADS));
            }

            executor.shutdown();
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);

            conn.commit();
            conn.close();

            System.out.println("Data generation completed.");
        }

        private static class DataGeneratorTask implements Runnable {
            private final Connection conn;
            private final PreparedStatement pstmt;
            private final int recordsToGenerate;
            private final Random random;

            DataGeneratorTask(Connection conn, PreparedStatement pstmt, int recordsToGenerate) {
                this.conn = conn;
                this.pstmt = pstmt;
                this.recordsToGenerate = recordsToGenerate;
                this.random = new Random();
            }

            @Override
            public void run() {
                try {
                    for (int i = 0; i < recordsToGenerate; i++) {
                        String name = FIRST_NAMES[random.nextInt(FIRST_NAMES.length)] + " " +
                                MIDDLE_NAMES[random.nextInt(MIDDLE_NAMES.length)] + " " +
                                LAST_NAMES[random.nextInt(LAST_NAMES.length)];
                        pstmt.setString(1, name);
                        pstmt.addBatch();

                        if (i % BATCH_SIZE == 0) {
                            pstmt.executeBatch();
                            conn.commit();
                        }
                    }
                    pstmt.executeBatch();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

