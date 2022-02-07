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
                    return;
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
                    if (isLoggedIn) System.out.println("\nYou have successfully logged in!");
                    while (isLoggedIn) {
                        System.out.println("\n1. Balance\n" +
                                "2. Add income\n" +
                                "3. Do transfer\n" +
                                "4. Close account\n" +
                                "5. Log out\n" +
                                "0. Exit");
                        action = scanner.nextLine();
                        switch (action) {
                            case "0":
                                System.out.println("\nBye");
                                dataBase.closeConnection();
                                return;
                            case "1":
                                System.out.println("\nBalance: " + account.getBalance());
                                break;
                            case "2":
                                System.out.println("Enter income:");
                                int income = 0;
                                try {
                                    income = Integer.parseInt(scanner.nextLine());
                                } catch (NumberFormatException e) {
                                    System.out.println("Wrong input for income: " + e.getMessage());
                                }
                                if (income <= 0) System.out.println("Please,enter value bigger then zero");
                                else {
                                    dataBase.addIncome(income, account);
                                }
                                break;
                            case "4":
                                isLoggedIn = !dataBase.closeAccount(account);
                                break;
                            case "3":
                                System.out.println("\nTransfer\n" +
                                        "Enter card number:");
                                String cardNumberToTransfer = scanner.nextLine();
                                if (dataBase.checkCardNumberToTransfer(account, cardNumberToTransfer)) {
                                    System.out.println("Enter how much money you want to transfer:");
                                    try {
                                        int moneyToTransfer = Integer.parseInt(scanner.nextLine());
                                        dataBase.transfer(account, cardNumberToTransfer, moneyToTransfer);
                                    } catch (NumberFormatException e) {
                                        System.out.println("Please enter a number!");
                                    }
                                }
                                break;
                            case "5":
                                isLoggedIn = false;
                                break;
                            default:
                                System.out.println("\nWrong input!");
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
