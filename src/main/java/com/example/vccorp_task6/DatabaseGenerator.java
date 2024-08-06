package com.example.vccorp_task6;
import java.sql.*;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class DatabaseGenerator {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/user_management";
    private static final String USER = "root";
    private static final String PASS = "";

    private static final String[] FIRST_NAMES = {"Nguyen", "Tran", "Le", "Pham", "Ho", "Huynh", "Vu", "Bui", "Dang", "Phan"};
    private static final String[] MIDDLE_NAMES = {"Van", "Thi", "Huu", "Cong", "Quoc", "Minh", "Tuan", "Thanh", "Ngoc", "Thuy"};
    private static final String[] LAST_NAMES = {"An", "Binh", "Cuong", "Dung", "Giang", "Ha", "Khanh", "Linh", "Mai", "Phuong"};

    private static final int BATCH_SIZE = 10000;
    private static final int TOTAL_RECORDS = 5_000_000;
    private static final int THREAD_COUNT = 4;

    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return;
        }

        AtomicInteger counter = new AtomicInteger(0);

        Thread[] threads = new Thread[THREAD_COUNT];
        for (int i = 0; i < THREAD_COUNT; i++) {
            threads[i] = new Thread(() -> insertData(counter));
            threads[i].start();
        }

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        long endTime = System.currentTimeMillis();
        System.out.println("Total execution time: " + (endTime - startTime) + " ms");
    }

    private static void insertData(AtomicInteger counter) {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            conn.setAutoCommit(false);
            String sql = "INSERT INTO users (full_name) VALUES (?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                Random random = new Random();

                while (true) {
                    int start = counter.getAndAdd(BATCH_SIZE);
                    if (start >= TOTAL_RECORDS) break;

                    for (int i = 0; i < BATCH_SIZE && start + i < TOTAL_RECORDS; i++) {
                        String fullName = FIRST_NAMES[random.nextInt(FIRST_NAMES.length)] + " " +
                                MIDDLE_NAMES[random.nextInt(MIDDLE_NAMES.length)] + " " +
                                LAST_NAMES[random.nextInt(LAST_NAMES.length)];
                        pstmt.setString(1, fullName);
                        pstmt.addBatch();
                    }

                    pstmt.executeBatch();
                    conn.commit();

                    System.out.println("Thread " + Thread.currentThread().getId() + " inserted " + BATCH_SIZE + " records");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}