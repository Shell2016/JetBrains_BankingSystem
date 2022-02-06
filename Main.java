package banking;

import java.sql.SQLException;
import java.util.Random;
import java.util.Scanner;

public class Main {
    public final static Random random = new Random();
    public final static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        String fileName;
        if (args.length > 1 && "-fileName".equals(args[0])) {
            fileName = args[1];
        } else {
            System.out.println("You must specify filename for database: -fileName <name.db>");
            return;
        }

        DataBase dataBase = new DataBase(fileName);
        if (!dataBase.connect()) {
            System.out.println("Cannot establish connection to database!");
            return;
        }

        while (true) {
            System.out.print("1. Create an account\n" +
                    "2. Log into account\n" +
                    "0. Exit\n");
            String action = scanner.nextLine();
            switch (action) {
                case "0":
                    System.out.println("\nBye");
                    dataBase.closeConnection();
                    System.exit(0);
                case "1":
                    try {
                        dataBase.createAccount();
                    } catch (SQLException e) {
                        System.out.println("Error! Cannot create new account: " + e.getMessage());
                    }
                    break;
                case "2":
                    System.out.println("Enter your card number:");
                    String cardNumber = scanner.nextLine();
                    System.out.println("Enter your PIN:");
                    String pin = scanner.nextLine();
                    Account account = dataBase.logIn(cardNumber, pin);
                    boolean isLoggedIn = account != null;
                    if (isLoggedIn) System.out.println("\nYou have successfully logged in!\n");

                    while (isLoggedIn) {
                        System.out.println("1. Balance\n" +
                                "2. Log out\n" +
                                "0. Exit");
                        action = scanner.nextLine();
                        switch (action) {
                            case "0":
                                System.out.println("\nBye");
                                dataBase.closeConnection();
                                System.exit(0);
                            case "1":
                                System.out.println("\nBalance: " + account.getBalance() + "\n");
                                break;
                            case "2":
                                isLoggedIn = false;
                                break;
                            default:
                                System.out.println("\nWrong input!\n");
                                break;
                        }
                    }
                    break;
                default:
                    System.out.println("\nInvalid input!\n");
                    break;
            }
        }
    }
}
