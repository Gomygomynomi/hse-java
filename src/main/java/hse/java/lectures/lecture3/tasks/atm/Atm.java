package hse.java.lectures.lecture3.tasks.atm;

import java.util.*;

public class Atm {
    public enum Denomination {
        D50(50),
        D100(100),
        D200(200),
        D500(500),
        D1000(1000),
        D2000(2000),
        D5000(5000);

        private final int value;

        Denomination(int value) {
            this.value = value;
        }

        public int value() {
            return value;
        }

        public static Denomination fromInt(int value) {
            return Arrays.stream(values())
                    .filter(v -> v.value == value)
                    .findFirst()
                    .orElse(null);
        }
    }

    private final Map<Denomination, Integer> banknotes = new EnumMap<>(Denomination.class);

    public Atm() {
    }

    public void deposit(Map<Denomination, Integer> banknotes) {
        for (Map.Entry<Denomination, Integer> entry : banknotes.entrySet()) {
            Denomination denom = entry.getKey();
            Integer count = entry.getValue();
            if (denom == null || count == null || count <= 0) {
                throw new InvalidDepositException("Invalid denomination or count");
            }
        }
        for (Map.Entry<Denomination, Integer> entry : banknotes.entrySet()) {
            Denomination denom = entry.getKey();
            int count = entry.getValue();
            this.banknotes.put(denom, this.banknotes.getOrDefault(denom, 0) + count);
        }
    }

    public Map<Denomination, Integer> withdraw(int amount) {
        if (amount <= 0) {
            throw new InvalidAmountException("Amount must be positive: " + amount);
        }
        if (amount > getBalance()) {
            throw new InsufficientFundsException("Insufficient funds");
        }

        List<Denomination> sorted = new ArrayList<>(EnumSet.allOf(Denomination.class));
        sorted.sort((a, b) -> Integer.compare(b.value(), a.value()));

        Map<Denomination, Integer> result = new EnumMap<>(Denomination.class);
        int remaining = amount;

        for (Denomination denom : sorted) {
            int available = banknotes.getOrDefault(denom, 0);
            int need = remaining / denom.value();
            int take = Math.min(need, available);
            if (take > 0) {
                result.put(denom, take);
                remaining -= take * denom.value();
            }
        }

        if (remaining != 0) {
            throw new CannotDispenseException("Cannot dispense exact amount");
        }

        for (Map.Entry<Denomination, Integer> entry : result.entrySet()) {
            Denomination denom = entry.getKey();
            int newCount = banknotes.get(denom) - entry.getValue();
            if (newCount == 0) {
                banknotes.remove(denom);
            } else {
                banknotes.put(denom, newCount);
            }
        }

        return result;
    }

    public int getBalance() {
        return banknotes.entrySet().stream()
                .mapToInt(e -> e.getKey().value() * e.getValue())
                .sum();
    }
}