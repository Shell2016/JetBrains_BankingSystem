package banking;

import java.sql.*;

public class DataBase {
    public static String DATABASE_URL;
    public static final String TABLE_NAME = "card";
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
            }
        } catch (SQLException e) {
            System.out.println("Can't close connection: " + e.getMessage());
        }
    }

    private void createTable() {
        String sqlCreateTable = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME +
                " (" + CARD_ID + " INTEGER PRIMARY KEY NOT NULL, " +
                CARD_NUMBER + " VARCHAR(16) NOT NULL, " +
                CARD_PIN + " VARCHAR(4) NOT NULL, " +
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
            String sqlCheckIfCardExists = "SELECT " + CARD_NUMBER + " FROM " + TABLE_NAME +
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
                " FROM " + TABLE_NAME + " WHERE " + CARD_NUMBER + " = ? AND " + CARD_PIN + " = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sqlLogin)) {
            pstmt.setString(1, cardNumber);
            pstmt.setString(2, pin);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new Account(rs.getString(1), rs.getString(2), rs.getInt(3));
            } else {
                System.out.println("\nWrong card number or PIN!\n");
                return null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void addIncome(int income, Account account) {
        String sqlAddIncome = "UPDATE " + TABLE_NAME + " SET " + CARD_BALANCE + " = " + CARD_BALANCE + " + ?" +
                " WHERE " + CARD_NUMBER + " = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sqlAddIncome)) {
            pstmt.setInt(1, income);
            pstmt.setString(2, account.getCARD_NUMBER());
            if (pstmt.executeUpdate() == 1) {
                System.out.println("Income was added!");
                account.setBalance(account.getBalance() + income);
            }
        } catch (SQLException e) {
            System.out.println("Error! Cannot add income: " + e.getMessage());
        }
    }

    public boolean closeAccount(Account account) {
        String sqlCloseAccount = "DELETE FROM " + TABLE_NAME + " WHERE " + CARD_NUMBER + " = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sqlCloseAccount)) {
            pstmt.setString(1, account.getCARD_NUMBER());
            if (pstmt.executeUpdate() == 1) {
                System.out.println("The account has been closed!");
                return true;
            } else {
                return false;
            }
        } catch (SQLException e) {
            System.out.println("Error! Cannot delete account: " + e.getMessage());
            return false;
        }
    }

    public boolean checkCardNumberToTransfer(Account account, String cardNumberToTransfer) {
        if (cardNumberToTransfer.equals(account.getCARD_NUMBER())) {
            System.out.println("You can't transfer money to the same account!");
            return false;
        }
        if (cardNumberToTransfer.length() != 16) {
            System.out.println("Probably you made a mistake in the card number. Please try again!");
            return false;
        }
        String checksum = CardNumberGenerator.generateChecksum(cardNumberToTransfer.substring(0, 15));
        String checksumToCheck = cardNumberToTransfer.substring(15);
        if (!checksumToCheck.equals(checksum)) {
            System.out.println("Probably you made a mistake in the card number. Please try again!");
            return false;
        } else {
            String sqlSearchAccount = "SELECT * FROM " + TABLE_NAME + " WHERE " + CARD_NUMBER + " = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sqlSearchAccount)) {
                pstmt.setString(1, cardNumberToTransfer);
                ResultSet rs = pstmt.executeQuery();
                if (!rs.next()) {
                    System.out.println("Such a card does not exist.");
                    return false;
                } else {
                    return true;
                }
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }

    }

    public void transfer(Account account, String cardNumberToTransfer, int moneyToTransfer) {
        if (moneyToTransfer > account.getBalance()) {
            System.out.println("Not enough money!");
            return;
        }
        String sqlTransferTo = "UPDATE " + TABLE_NAME + " SET " + CARD_BALANCE + " = " + CARD_BALANCE + " + ?" +
                " WHERE " + CARD_NUMBER + " = ?";
        String sqlTransferFrom = "UPDATE " + TABLE_NAME + " SET " + CARD_BALANCE + " = " + CARD_BALANCE + " - ?" +
                " WHERE " + CARD_NUMBER + " = ?";
        try {
            conn.setAutoCommit(false);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try (PreparedStatement transferTo = conn.prepareStatement(sqlTransferTo);
             PreparedStatement transferFrom = conn.prepareStatement(sqlTransferFrom)) {

            transferTo.setInt(1, moneyToTransfer);
            transferTo.setString(2, cardNumberToTransfer);
            transferTo.executeUpdate();

            transferFrom.setInt(1, moneyToTransfer);
            transferFrom.setString(2, account.getCARD_NUMBER());
            transferFrom.executeUpdate();

            conn.commit();
            conn.setAutoCommit(true);
            account.setBalance(account.getBalance() - moneyToTransfer);
            System.out.println("Success!");

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
}
