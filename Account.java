package banking;

public class Account {
    private final String CARD_NUMBER;
    private final String PIN;
    private int balance;

    public Account(String CARD_NUMBER, String PIN, int balance) {
        this.CARD_NUMBER = CARD_NUMBER;
        this.PIN = PIN;
        this.balance = balance;
    }

    public String getCARD_NUMBER() {
        return CARD_NUMBER;
    }

    public int getBalance() {
        return balance;
    }

    public void setBalance(int balance) {
        this.balance = balance;
    }
}
