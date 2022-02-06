package banking;

import java.sql.*;

public class DataBase {
    public static String DATABASE_URL;
    public static final String TABLE_CARD = "card";
    public static final String CARD_ID = "id";
    public static final String CARD_NUMBER = "number";
    public static final String CARD_PIN = "pin";
    public static final String CARD_BALANCE = "balance";

    private Connection conn;

    public DataBase(String fileName) {
        DATABASE_URL = "jdbc:sqlite:" + fileName;
    }

    public boolean connect() {
        try {
            conn = DriverManager.getConnection(DATABASE_URL);
            System.out.println("Connected to database test.db");
            createTable();
            return true;
        } catch (SQLException e) {
            System.out.println("Can't connect to database: " + e.getMessage());
            return false;
        }
    }

    public void closeConnection() {
        try {
            if (conn != null) {
                conn.close();
                System.out.println("Database disconnected");
            }
        } catch (SQLException e) {
            System.out.println("Can't close connection: " + e.getMessage());
        }
    }

    private void createTable() {
        String sqlCreateTable = "CREATE TABLE IF NOT EXISTS " + TABLE_CARD +
                " (" + CARD_ID + " INTEGER PRIMARY KEY NOT NULL, " +
                CARD_NUMBER + " VARCHAR(16) NOT NULL, " +
                CARD_PIN + " TEXT NOT NULL, " +
                CARD_BALANCE + " INTEGER DEFAULT 0)";
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sqlCreateTable);
        } catch (SQLException e) {
            System.out.println("Cannot create new table in DB: " + e.getMessage());
        }
    }

    public void createAccount() throws SQLException {
        ResultSet resultSet = null;
        String newCardNumber;
        do {
            newCardNumber = CardNumberGenerator.generateCardNumber();
            String sqlCheckIfCardExists = "SELECT " + CARD_NUMBER + " FROM " + TABLE_CARD +
                                          " WHERE " + CARD_NUMBER + " = " + newCardNumber;
            try (Statement stmt = conn.createStatement()) {
                resultSet = stmt.executeQuery(sqlCheckIfCardExists);
            } catch (SQLException e) {
                System.out.println("Error! Cannot check if card already exists: " + e.getMessage());
            }
        } while (resultSet != null && resultSet.next());
        String newPin = CardNumberGenerator.generatePin();
        String sqlAddNewCard = "INSERT INTO card (" + CARD_NUMBER + ", " + CARD_PIN + ") VALUES " +
                                "('" + newCardNumber + "', '" + newPin + "')";
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sqlAddNewCard);
            System.out.println("\nYour card has been created\n" +
                    "Your card number:\n" +
                    newCardNumber +
                    "\nYour card PIN:\n" +
                    newPin + "\n");
        } catch (SQLException e) {
            System.out.println("Error! Cannot add new card to DB: " + e.getMessage());
        }
    }

    public Account logIn(String cardNumber, String pin) {
        String sqlLogin = "SELECT " + CARD_NUMBER + ", " + CARD_PIN + ", " + CARD_BALANCE +
                          " FROM " + TABLE_CARD + " WHERE " + CARD_NUMBER + " = ? AND " + CARD_PIN + " = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sqlLogin)) {
            pstmt.setString(1, cardNumber);
            pstmt.setString(2, pin);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new Account(rs.getString(1), rs.getString(2), rs.getInt(3) );
            } else {
                System.out.println("\nWrong card number or PIN!\n");
                return null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

}
