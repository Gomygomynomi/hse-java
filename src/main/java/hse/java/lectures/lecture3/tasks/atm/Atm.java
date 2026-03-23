package hse.java.lectures.lecture3.tasks.atm;

import java.util.*;

public class Atm {

    private static final List<Integer> DENOMINATIONS = List.of(5000, 2000, 1000, 500, 200, 100, 50);

    private final Map<Integer, Integer> storage = new HashMap<>();

    public Atm() {}

    public void deposit(Map<Integer, Integer> banknotes) {
        for (Map.Entry<Integer, Integer> entry : banknotes.entrySet()) {
            int nominal = entry.getKey();
            int count = entry.getValue();
            if (count <= 0) {
                throw new InvalidDepositException("Количество должно быть положительным: " + count);
            }
            if (!DENOMINATIONS.contains(nominal)) {
                throw new InvalidDepositException("Недопустимый номинал: " + nominal);
            }
        }
        // Все проверки пройдены – обновляем хранилище
        for (Map.Entry<Integer, Integer> entry : banknotes.entrySet()) {
            int nominal = entry.getKey();
            int count = entry.getValue();
            storage.put(nominal, storage.getOrDefault(nominal, 0) + count);
        }
    }

    public Map<Integer, Integer> withdraw(int amount) {
        if (amount <= 0) {
            throw new InvalidAmountException("Сумма должна быть положительной: " + amount);
        }
        if (amount > getBalance()) {
            throw new InsufficientFundsException("Недостаточно средств: запрошено " + amount + ", доступно " + getBalance());
        }

        Map<Integer, Integer> result = new HashMap<>();
        int remaining = amount;

        for (int nominal : DENOMINATIONS) {
            int available = storage.getOrDefault(nominal, 0);
            int need = remaining / nominal;
            int take = Math.min(need, available);
            if (take > 0) {
                result.put(nominal, take);
                remaining -= take * nominal;
            }
        }

        if (remaining != 0) {
            throw new CannotDispenseException("Невозможно выдать точную сумму " + amount);
        }

        for (Map.Entry<Integer, Integer> entry : result.entrySet()) {
            int nominal = entry.getKey();
            int newCount = storage.get(nominal) - entry.getValue();
            if (newCount == 0) {
                storage.remove(nominal);
            } else {
                storage.put(nominal, newCount);
            }
        }
        return result;
    }

    public int getBalance() {
        return storage.entrySet().stream()
                .mapToInt(e -> e.getKey() * e.getValue())
                .sum();
    }
}