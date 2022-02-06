package banking;

import java.util.ArrayList;
import java.util.List;

public class CardNumberGenerator {
    public static String generateCardNumber() {
        final String BIN = "400000";
        String customerAccountNumber = String.format("%09d", Main.random.nextInt(999999999));
        return BIN + customerAccountNumber + generateChecksum(BIN + customerAccountNumber);
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
        int newPin = Main.random.nextInt(9999);
        return String.format("%04d", newPin);
    }
}
