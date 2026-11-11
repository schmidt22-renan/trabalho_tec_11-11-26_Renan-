package service;

import model.DebtSettlement;
import model.Expense;
import model.Participant;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

public class ExpenseSplitterService {
    private final List<Expense> expenses = new ArrayList<>();
    private final Set<Participant> participants = new HashSet<>();

    public void addParticipant(Participant participant) {
        participants.add(Objects.requireNonNull(participant));
    }

    /**
     * Adiciona uma despesa dividida igualmente entre os participantes informados.
     */
    public void addEqualExpense(String description, Participant paidBy, BigDecimal amount, List<Participant> sharedWith) {
        if (sharedWith == null || sharedWith.isEmpty()) {
            throw new IllegalArgumentException("A lista de compartilhamento não pode ser vazia.");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("O valor da despesa deve ser maior que zero.");
        }

        BigDecimal shareAmount = amount.divide(
                BigDecimal.valueOf(sharedWith.size()), 2, RoundingMode.HALF_UP
        );

        Map<Participant, BigDecimal> splits = new HashMap<>();
        BigDecimal accumulated = BigDecimal.ZERO;

        for (int i = 0; i < sharedWith.size(); i++) {
            Participant p = sharedWith.get(i);
            if (i == sharedWith.size() - 1) {
                // Ajuste de centavos na última parcela
                splits.put(p, amount.subtract(accumulated));
            } else {
                splits.put(p, shareAmount);
                accumulated = accumulated.add(shareAmount);
            }
        }

        addExpense(new Expense(description, paidBy, amount, splits));
    }

    public void addExpense(Expense expense) {
        expenses.add(expense);
        participants.add(expense.getPaidBy());
        participants.addAll(expense.getSplits().keySet());
    }

    /**
     * Calcula o saldo líquido de cada participante (Pago - Consumido).
     */
    public Map<Participant, BigDecimal> calculateNetBalances() {
        Map<Participant, BigDecimal> balances = new HashMap<>();
        for (Participant p : participants) {
            balances.put(p, BigDecimal.ZERO);
        }

        for (Expense expense : expenses) {
            // Quem pagou ganha crédito (+)
            balances.put(expense.getPaidBy(), balances.get(expense.getPaidBy()).add(expense.getAmount()));

            // Quem consumiu assume débito (-)
            expense.getSplits().forEach((participant, share) -> {
                balances.put(participant, balances.get(participant).subtract(share));
            });
        }

        return balances;
    }

    /**
     * Algoritmo de Simplificação de Dívidas (Greedy / Two Pointers).
     * Minimiza o número total de transações de acerto.
     */
    public List<DebtSettlement> settleDebts() {
        Map<Participant, BigDecimal> balances = calculateNetBalances();

        List<Map.Entry<Participant, BigDecimal>> debtors = new ArrayList<>();
        List<Map.Entry<Participant, BigDecimal>> creditors = new ArrayList<>();

        for (Map.Entry<Participant, BigDecimal> entry : balances.entrySet()) {
            if (entry.getValue().compareTo(BigDecimal.ZERO) < 0) {
                debtors.add(new AbstractMap.SimpleEntry<>(entry.getKey(), entry.getValue().abs()));
            } else if (entry.getValue().compareTo(BigDecimal.ZERO) > 0) {
                creditors.add(new AbstractMap.SimpleEntry<>(entry.getKey(), entry.getValue()));
            }
        }

        List<DebtSettlement> settlements = new ArrayList<>();
        int i = 0, j = 0;

        while (i < debtors.size() && j < creditors.size()) {
            var debtorEntry = debtors.get(i);
            var creditorEntry = creditors.get(j);

            BigDecimal debtorBalance = debtorEntry.getValue();
            BigDecimal creditorBalance = creditorEntry.getValue();

            BigDecimal minAmount = debtorBalance.min(creditorBalance);

            settlements.add(new DebtSettlement(debtorEntry.getKey(), creditorEntry.getKey(), minAmount));

            debtorEntry.setValue(debtorBalance.subtract(minAmount));
            creditorEntry.setValue(creditorBalance.subtract(minAmount));

            if (debtorEntry.getValue().compareTo(BigDecimal.ZERO) == 0) i++;
            if (creditorEntry.getValue().compareTo(BigDecimal.ZERO) == 0) j++;
        }

        return settlements;
    }
}