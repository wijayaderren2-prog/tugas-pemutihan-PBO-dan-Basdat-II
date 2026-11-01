package com.kopdes;
 
import java.sql.Connection;
import java.sql.DriverManager;

public class KoneksiDB {
    private static final String URL = "jdbc:postgresql://localhost:5432/kopdes_db";
    private static final String USER = "postgres"; // Sesuaikan user postgres Mac kamu
    private static final String PASS = "postgres"; // Sesuaikan password postgres Mac kamu

    public static Connection getConnection() throws Exception {
        Class.forName("org.postgresql.Driver");
        return DriverManager.getConnection(URL, USER, PASS);
    }
}