package model;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

public class Expense {
    private final String description;
    private final Participant paidBy;
    private final BigDecimal amount;
    private final Map<Participant, BigDecimal> splits; // Quanto cada um deve arcar nesta despesa

    public Expense(String description, Participant paidBy, BigDecimal amount, Map<Participant, BigDecimal> splits) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("O valor da despesa deve ser maior que zero.");
        }
        this.description = Objects.requireNonNull(description, "Descrição é obrigatória.");
        this.paidBy = Objects.requireNonNull(paidBy, "Pagador é obrigatório.");
        this.amount = amount;
        this.splits = Map.copyOf(splits);

        validateSplitsSum();
    }

    private void validateSplitsSum() {
        BigDecimal totalSplit = splits.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalSplit.compareTo(amount) != 0) {
            throw new IllegalArgumentException(
                "A soma das partes (" + totalSplit + ") não bate com o valor total da despesa (" + amount + ")."
            );
        }
    }

    public String getDescription() { return description; }
    public Participant getPaidBy() { return paidBy; }
    public BigDecimal getAmount() { return amount; }
    public Map<Participant, BigDecimal> getSplits() { return splits; }
}