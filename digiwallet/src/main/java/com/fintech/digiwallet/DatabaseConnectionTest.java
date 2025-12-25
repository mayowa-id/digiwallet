package com.fintech.digiwallet;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class DatabaseConnectionTest {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://localhost:5432/wallet_dev";
        String username = "postgres";
        String password = "postgres";

        System.out.println("Testing database connection...");
        System.out.println("URL: " + url);
        System.out.println("Username: " + username);
        System.out.println("Password: " + password);

        try {
            Class.forName("org.postgresql.Driver");
            System.out.println("✓ PostgreSQL Driver loaded");

            Connection conn = DriverManager.getConnection(url, username, password);
            System.out.println("✓ Database connection successful!");

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT version()");

            if (rs.next()) {
                System.out.println("✓ PostgreSQL version: " + rs.getString(1));
            }

            rs.close();
            stmt.close();
            conn.close();

            System.out.println("✓ All tests passed!");

        } catch (Exception e) {
            System.err.println("✗ Connection failed!");
            e.printStackTrace();
        }
    }
}