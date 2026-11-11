package model;

import java.math.BigDecimal;

public record DebtSettlement(Participant from, Participant to, BigDecimal amount) {
    @Override
    public String toString() {
        return String.format("%s deve pagar R$ %.2f para %s", from.getName(), amount, to.getName());
    }
}