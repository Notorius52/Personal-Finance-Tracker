package it.progettogithub.finanze.app;

import java.sql.*;
import java.time.LocalDate;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class DatabaseManager {

    private static final String DB_URL = "jdbc:sqlite:financedb.sqlite";

    public static Connection connect() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(DB_URL);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return conn;
    }

    public static void createNewTable() {
        String sql = "CREATE TABLE IF NOT EXISTS transactions ("
                   + " id integer PRIMARY KEY AUTOINCREMENT,"
                   + " date text NOT NULL,"
                   + " description text NOT NULL,"
                   + " amount real NOT NULL,"
                   + " category text NOT NULL"
                   + ");";
        
        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static ObservableList<Transaction> loadTransactions() {
        String sql = "SELECT id, date, description, amount, category FROM transactions";
        ObservableList<Transaction> data = FXCollections.observableArrayList();
        
        try (Connection conn = connect();
             Statement stmt  = conn.createStatement();
             ResultSet rs    = stmt.executeQuery(sql)){
            
            while (rs.next()) {
                data.add(new Transaction(
                    rs.getInt("id"),
                    LocalDate.parse(rs.getString("date")),
                    rs.getString("description"),
                    rs.getDouble("amount"),
                    rs.getString("category")
                ));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return data;
    }

    // --- METODO CHE MANCAVA ---
    public static void saveTransaction(Transaction transaction) {
        String sql = "INSERT INTO transactions(date, description, amount, category) VALUES(?,?,?,?)";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, transaction.getDate().toString());
            pstmt.setString(2, transaction.getDescription());
            pstmt.setDouble(3, transaction.getAmount());
            pstmt.setString(4, transaction.getCategory());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
    
    public static void updateTransaction(Transaction transaction) {
        String sql = "UPDATE transactions SET date = ? , "
                   + "description = ? , "
                   + "amount = ? , "
                   + "category = ? "
                   + "WHERE id = ?";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, transaction.getDate().toString());
            pstmt.setString(2, transaction.getDescription());
            pstmt.setDouble(3, transaction.getAmount());
            pstmt.setString(4, transaction.getCategory());
            pstmt.setInt(5, transaction.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
    
    public static void deleteTransaction(int id) {
        String sql = "DELETE FROM transactions WHERE id = ?";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}