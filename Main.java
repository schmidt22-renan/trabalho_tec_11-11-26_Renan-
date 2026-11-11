import model.DebtSettlement;
import model.Participant;
import service.ExpenseSplitterService;

import java.math.BigDecimal;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        ExpenseSplitterService service = new ExpenseSplitterService();

        // 1. Cadastro dos participantes
        Participant ana = new Participant("1", "Ana");
        Participant bruno = new Participant("2", "Bruno");
        Participant carla = new Participant("3", "Carla");
        Participant daniel = new Participant("4", "Daniel");

        List<Participant> todos = List.of(ana, bruno, carla, daniel);

        // 2. Lançamento das despesas do Churrasco
        // Ana pagou a carne (R$ 200,00) para todos
        service.addEqualExpense("Carne e Carvão", ana, new BigDecimal("200.00"), todos);

        // Bruno pagou as bebidas (R$ 120,00) para todos
        service.addEqualExpense("Cerveja e Refrigerante", bruno, new BigDecimal("120.00"), todos);

        // Carla pagou a sobremesa (R$ 40,00) apenas para Ana, Bruno e Carla (Daniel não comeu)
        service.addEqualExpense("Sobremesa", carla, new BigDecimal("40.00"), List.of(ana, bruno, carla));

        // 3. Exibir Saldos Líquidos
        System.out.println("=== SALDO LÍQUIDO DE CADA UM ===");
        service.calculateNetBalances().forEach((p, balance) -> {
            String status = balance.compareTo(BigDecimal.ZERO) >= 0 ? "a receber" : "a pagar";
            System.out.printf("%s: R$ %.2f (%s)%n", p.getName(), balance.abs(), status);
        });

        // 4. Calcular pagamentos otimizados
        System.out.println("\n=== ACERTO DE CONTAS (MÍNIMO DE TRANSAÇÕES) ===");
        List<DebtSettlement> settlements = service.settleDebts();
        settlements.forEach(System.out::println);
    }
}