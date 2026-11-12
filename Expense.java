import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.Objects;

public class Expense {
    private final String description;
    private final Participant paidBy;
    private final BigDecimal amount;
    private final Map<Participant, BigDecimal> splits;

    public Expense(String description, Participant paidBy, BigDecimal amount, Map<Participant, BigDecimal> splits) {
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("A descrição da despesa é obrigatória.");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("O valor da despesa deve ser maior que zero.");
        }
        if (splits == null || splits.isEmpty()) {
            throw new IllegalArgumentException("A despesa deve ser dividida com pelo menos uma pessoa.");
        }

        this.description = description;
        this.paidBy = Objects.requireNonNull(paidBy, "Quem pagou a despesa é obrigatório.");
        this.amount = amount.setScale(2, RoundingMode.HALF_UP);
        this.splits = Map.copyOf(splits);

        validateSplitsSum();
    }

    private void validateSplitsSum() {
        BigDecimal totalSplit = splits.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        if (totalSplit.compareTo(this.amount) != 0) {
            throw new IllegalArgumentException(
                String.format("A soma das divisões (R$ %.2f) difere do valor total (R$ %.2f).", totalSplit, this.amount)
            );
        }
    }

    public String getDescription() {
        return description;
    }

    public Participant getPaidBy() {
        return paidBy;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public Map<Participant, BigDecimal> getSplits() {
        return splits;
    }
}