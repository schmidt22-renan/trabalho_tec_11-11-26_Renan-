import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        ExpenseSplitterService service = new ExpenseSplitterService();

        Participant ana = new Participant("1", "Ana");
        Participant bruno = new Participant("2", "Bruno");
        Participant carla = new Participant("3", "Carla");
        Participant daniel = new Participant("4", "Daniel");

        List<Participant> grupoCompleto = List.of(ana, bruno, carla, daniel);

        service.addEqualExpense("Carne e Carvão", ana, new BigDecimal("200.00"), grupoCompleto);
        service.addEqualExpense("Bebidas", bruno, new BigDecimal("120.00"), grupoCompleto);
        service.addEqualExpense("Sobremesa", carla, new BigDecimal("40.00"), List.of(ana, bruno, carla));

        Map<Participant, BigDecimal> divisaoUber = Map.of(
                daniel, new BigDecimal("30.00"),
                carla, new BigDecimal("20.00")
        );
        service.addExpense(new Expense("Uber de volta", ana, new BigDecimal("50.00"), divisaoUber));

        System.out.println("=== SALDO LÍQUIDO POR PARTICIPANTE ===");
        service.calculateNetBalances().forEach((participant, balance) -> {
            if (balance.compareTo(BigDecimal.ZERO) > 0) {
                System.out.printf("%-10s -> A receber: R$ %.2f%n", participant.getName(), balance);
            } else if (balance.compareTo(BigDecimal.ZERO) < 0) {
                System.out.printf("%-10s -> A pagar:   R$ %.2f%n", participant.getName(), balance.abs());
            } else {
                System.out.printf("%-10s -> Quitado%n", participant.getName());
            }
        });

        System.out.println("\n=== TRANSFERÊNCIAS NECESSÁRIAS ===");
        List<DebtSettlement> settlements = service.settleDebts();
        if (settlements.isEmpty()) {
            System.out.println("Nenhuma transferência necessária. Tudo quitado!");
        } else {
            settlements.forEach(System.out::println);
        }
    }
}