import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

public class ExpenseSplitterService {
    private final List<Expense> expenses = new ArrayList<>();
    private final Set<Participant> participants = new LinkedHashSet<>();

    private static class BalanceEntry {
        final Participant participant;
        BigDecimal amount;

        BalanceEntry(Participant participant, BigDecimal amount) {
            this.participant = participant;
            this.amount = amount;
        }
    }

    public void addParticipant(Participant participant) {
        participants.add(Objects.requireNonNull(participant, "Participante não pode ser nulo."));
    }

    public void addEqualExpense(String description, Participant paidBy, BigDecimal amount, List<Participant> sharedWith) {
        if (sharedWith == null || sharedWith.isEmpty()) {
            throw new IllegalArgumentException("A lista de participantes para divisão não pode ser vazia.");
        }

        BigDecimal totalAmount = amount.setScale(2, RoundingMode.HALF_UP);
        BigDecimal shareAmount = totalAmount.divide(
                BigDecimal.valueOf(sharedWith.size()), 2, RoundingMode.HALF_UP
        );

        Map<Participant, BigDecimal> splits = new HashMap<>();
        BigDecimal accumulated = BigDecimal.ZERO;

        for (int i = 0; i < sharedWith.size(); i++) {
            Participant p = sharedWith.get(i);
            if (i == sharedWith.size() - 1) {
                splits.put(p, totalAmount.subtract(accumulated));
            } else {
                splits.put(p, shareAmount);
                accumulated = accumulated.add(shareAmount);
            }
        }

        addExpense(new Expense(description, paidBy, totalAmount, splits));
    }

    public void addExpense(Expense expense) {
        Objects.requireNonNull(expense, "A despesa não pode ser nula.");
        expenses.add(expense);
        participants.add(expense.getPaidBy());
        participants.addAll(expense.getSplits().keySet());
    }

    public Map<Participant, BigDecimal> calculateNetBalances() {
        Map<Participant, BigDecimal> balances = new LinkedHashMap<>();
        for (Participant p : participants) {
            balances.put(p, BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        }

        for (Expense expense : expenses) {
            Participant payer = expense.getPaidBy();
            balances.put(payer, balances.get(payer).add(expense.getAmount()));

            expense.getSplits().forEach((participant, share) -> {
                balances.put(participant, balances.get(participant).subtract(share));
            });
        }

        return balances;
    }

    public List<DebtSettlement> settleDebts() {
        Map<Participant, BigDecimal> balances = calculateNetBalances();

        List<BalanceEntry> debtors = new ArrayList<>();
        List<BalanceEntry> creditors = new ArrayList<>();

        for (Map.Entry<Participant, BigDecimal> entry : balances.entrySet()) {
            BigDecimal balance = entry.getValue();
            if (balance.compareTo(BigDecimal.ZERO) < 0) {
                debtors.add(new BalanceEntry(entry.getKey(), balance.abs()));
            } else if (balance.compareTo(BigDecimal.ZERO) > 0) {
                creditors.add(new BalanceEntry(entry.getKey(), balance));
            }
        }

        debtors.sort((a, b) -> b.amount.compareTo(a.amount));
        creditors.sort((a, b) -> b.amount.compareTo(a.amount));

        List<DebtSettlement> settlements = new ArrayList<>();
        int i = 0;
        int j = 0;

        while (i < debtors.size() && j < creditors.size()) {
            BalanceEntry debtor = debtors.get(i);
            BalanceEntry creditor = creditors.get(j);

            BigDecimal transferAmount = debtor.amount.min(creditor.amount);

            settlements.add(new DebtSettlement(debtor.participant, creditor.participant, transferAmount));

            debtor.amount = debtor.amount.subtract(transferAmount);
            creditor.amount = creditor.amount.subtract(transferAmount);

            if (debtor.amount.compareTo(BigDecimal.ZERO) == 0) i++;
            if (creditor.amount.compareTo(BigDecimal.ZERO) == 0) j++;
        }

        return settlements;
    }

    public List<Expense> getExpenses() {
        return Collections.unmodifiableList(expenses);
    }
}