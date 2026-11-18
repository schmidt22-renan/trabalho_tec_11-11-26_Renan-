import java.math.BigDecimal;

public class DebtSettlement {
    private final Participant from;
    private final Participant to;
    private final BigDecimal amount;

    public DebtSettlement(Participant from, Participant to, BigDecimal amount) {
        this.from = from;
        this.to = to;
        this.amount = amount;
    }

    public Participant getFrom() { return from; }
    public Participant getTo() { return to; }
    public BigDecimal getAmount() { return amount; }

    @Override
    public String toString() {
        return String.format("%s deve pagar R$ %.2f para %s", from.getName(), amount, to.getName());
    }
}