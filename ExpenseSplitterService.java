import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

public class ExpenseSplitterService {
    private final List<Expense> expenses = new ArrayList<>();
    private final List<Participant> participants = new ArrayList<>();

    private static class BalanceEntry {
        final Participant participant;
        BigDecimal amount;

        BalanceEntry(Participant participant, BigDecimal amount) {
            this.participant = participant;
            this.amount = amount;
        }
    }

    public Participant registerParticipant(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Digite um nome válido.");
        }
        boolean exists = participants.stream().anyMatch(p -> p.getName().equalsIgnoreCase(name.trim()));
        if (exists) {
            throw new IllegalArgumentException("Participante já cadastrado.");
        }
        Participant p = new Participant(String.valueOf(participants.size() + 1), name.trim());
        participants.add(p);
        return p;
    }

    public Participant findParticipantById(String id) {
        return participants.stream()
                .filter(p -> p.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Participante não encontrado."));
    }

    public void addEqualExpense(String description, Participant paidBy, BigDecimal amount, List<Participant> sharedWith) {
        if (sharedWith == null || sharedWith.isEmpty()) {
            throw new IllegalArgumentException("Selecione pelo menos 1 participante para dividir.");
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

        expenses.add(new Expense(description, paidBy, totalAmount, splits));
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
        int i = 0, j = 0;

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

    public List<Participant> getParticipants() { return Collections.unmodifiableList(participants); }
    public List<Expense> getExpenses() { return Collections.unmodifiableList(expenses); }

    public void clearAll() {
        expenses.clear();
        participants.clear();
    }
}