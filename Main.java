package banking;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class Main {
    public static Random random = new Random();
    public static Scanner scanner = new Scanner(System.in);
    static String dbUrl = "jdbc:sqlite:";

    public static void main(String[] args) throws SQLException {
        dbUrl += args[1];
        createDatabase(dbUrl);

        while (true) {
            System.out.print("1. Create an account\n" +
                    "2. Log into account\n" +
                    "0. Exit\n");
            String action = scanner.nextLine();
            switch (action) {
                case "0":
                    System.out.println("\nBye");
                    System.exit(0);
                case "1":
                    createAccount();
                    break;
                case "2":
                    System.out.println("Enter your card number:");
                    String cardNumber = scanner.nextLine();
                    System.out.println("Enter your PIN:");
                    String pin = scanner.nextLine();
                    boolean isLoggedIn = logIn(cardNumber, pin);
                    while (isLoggedIn) {
                        System.out.println("1. Balance\n" +
                                "2. Log out\n" +
                                "0. Exit");
                        String actionLogged = scanner.nextLine();
                        if ("0".equals(actionLogged)) {
                            System.out.println("\nBye");
                            System.exit(0);
                        } else if ("1".equals(actionLogged)) {
                            String sqlGetBalance = "SELECT balance FROM card WHERE number = " + cardNumber;
                            try (Statement stmt = connect().createStatement()) {
                                ResultSet rs = stmt.executeQuery(sqlGetBalance);
                                while (rs.next()) {
                                    String balance = rs.getString("balance");
                                    System.out.println("\nBalance: " + balance + "\n");
                                }

                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                        } else if ("2".equals(actionLogged)) {
                            isLoggedIn = false;
                        } else {
                            System.out.println("\nWrong input!\n");
                        }
                    }
                    break;
                default:
                    System.out.println("\nInvalid input!\n");
                    break;
            }
        }
    }

    public static void createDatabase(String url) {
        String sqlCreateTable = "CREATE TABLE IF NOT EXISTS card (\n" +
                "  id INTEGER PRIMARY KEY, number TEXT, pin TEXT, balance INTEGER DEFAULT 0\n" +
                ")";
        try {
            Connection conn = DriverManager.getConnection(url);
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate(sqlCreateTable);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Connection connect() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(dbUrl);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return conn;
    }

    public static void createAccount() throws SQLException {
        ResultSet resultSet = null;
        String newCardNumber;
        do {
            newCardNumber = generateCardNumber();
            String sqlCheckIfCardExists = "SELECT number FROM card WHERE number = " + newCardNumber + ";";
            try (Statement stmt = connect().createStatement()) {
                resultSet = stmt.executeQuery(sqlCheckIfCardExists);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } while (resultSet != null && resultSet.next());
        String newPin = generatePin();
        String sqlInsertNewCard = "INSERT INTO card (number, pin) VALUES (?, ?)";
        try (PreparedStatement pstmt = connect().prepareStatement(sqlInsertNewCard)) {
            pstmt.setString(1, newCardNumber);
            pstmt.setString(2, newPin);
            pstmt.executeUpdate();
            System.out.println("\nYour card has been created\n" +
                    "Your card number:\n" +
                    newCardNumber +
                    "\nYour card PIN:\n" +
                    newPin + "\n");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static boolean logIn(String cardNumber, String pin) {
        String sqlLogin = "SELECT id, number, pin, balance FROM card WHERE number = ? AND pin = ?";
        try (PreparedStatement pstmt = connect().prepareStatement(sqlLogin)) {
            pstmt.setString(1, cardNumber);
            pstmt.setString(2, pin);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                System.out.println("\nYou have successfully logged in!\n");
                return true;
            } else {
                System.out.println("\nWrong card number or PIN!\n");
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static String generateCardNumber() {
        final String BIN = "400000";
        int card_length = 16;
        StringBuilder customerAccountNumber = new StringBuilder();
        String checksum;
        for (int i = 0; i < card_length - BIN.length() - 1; i++) {
            customerAccountNumber.append(random.nextInt(10));
        }
        checksum = generateChecksum(BIN + customerAccountNumber);
        return BIN + customerAccountNumber + checksum;
    }

    public static String generateChecksum(String s) {
        List<Integer> numbers = new ArrayList<>(15);
        for (String number : s.split("")) {
            numbers.add(Integer.parseInt(number));
        }
        for (int i = 0; i < numbers.size(); i++) {
            if (i % 2 == 0) {
                numbers.set(i, numbers.get(i) * 2);
            }
        }
        for (int i = 0; i < numbers.size(); i++) {
            if (numbers.get(i) > 9) {
                numbers.set(i, numbers.get(i) - 9);
            }
        }
        int sum = 0;
        for (Integer number : numbers) {
            sum += number;
        }
        int checksum = 10 - sum % 10;
        return checksum != 10 ? String.valueOf(checksum) : String.valueOf(0);
    }

    public static String generatePin() {
        StringBuilder newPin = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            newPin.append(random.nextInt(10));
        }
        return newPin.toString();
    }
}