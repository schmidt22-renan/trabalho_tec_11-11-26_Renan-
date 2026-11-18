import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class WebInterface {

    public static String renderPage(ExpenseSplitterService service, String errorMessage) {
        StringBuilder html = new StringBuilder();
        html.append("""
            <!DOCTYPE html>
            <html lang="pt-BR">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Organizador de Despesas Compartilhadas</title>
                <style>
                    :root {
                        --bg: #0f172a;
                        --card: #1e293b;
                        --border: #334155;
                        --text: #f8fafc;
                        --muted: #94a3b8;
                        --primary: #3b82f6;
                        --primary-hover: #2563eb;
                        --success: #10b981;
                        --danger: #ef4444;
                    }
                    * { box-sizing: border-box; margin: 0; padding: 0; font-family: 'Segoe UI', system-ui, sans-serif; }
                    body { background-color: var(--bg); color: var(--text); padding: 2rem 1rem; }
                    .container { max-width: 1000px; margin: 0 auto; }
                    header { text-align: center; margin-bottom: 2rem; }
                    header h1 { font-size: 1.8rem; color: #60a5fa; margin-bottom: 0.3rem; }
                    header p { color: var(--muted); font-size: 0.95rem; }
                    .alert { background: rgba(239, 68, 68, 0.15); border: 1px solid var(--danger); color: #fca5a5; padding: 0.8rem 1rem; border-radius: 8px; margin-bottom: 1.5rem; }
                    .grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(300px, 1fr)); gap: 1.5rem; margin-bottom: 1.5rem; }
                    .card { background: var(--card); border: 1px solid var(--border); border-radius: 12px; padding: 1.5rem; box-shadow: 0 4px 6px -1px rgba(0,0,0,0.3); }
                    .card h2 { font-size: 1.15rem; margin-bottom: 1rem; padding-bottom: 0.5rem; border-bottom: 1px solid var(--border); color: #e2e8f0; }
                    .form-group { margin-bottom: 1rem; }
                    label { display: block; font-size: 0.85rem; color: var(--muted); margin-bottom: 0.35rem; }
                    input[type="text"], input[type="number"], select {
                        width: 100%; padding: 0.65rem 0.8rem; background: var(--bg); border: 1px solid var(--border);
                        border-radius: 8px; color: var(--text); font-size: 0.95rem;
                    }
                    input:focus, select:focus { outline: none; border-color: var(--primary); }
                    .checkbox-list { display: flex; flex-wrap: wrap; gap: 0.5rem; margin-top: 0.4rem; }
                    .checkbox-item {
                        display: flex; align-items: center; gap: 0.4rem; background: var(--bg);
                        padding: 0.4rem 0.75rem; border-radius: 6px; border: 1px solid var(--border); cursor: pointer; font-size: 0.9rem;
                    }
                    button {
                        width: 100%; padding: 0.75rem; background: var(--primary); color: white; border: none;
                        border-radius: 8px; font-weight: 600; font-size: 0.95rem; cursor: pointer; transition: 0.2s;
                    }
                    button:hover { background: var(--primary-hover); }
                    .btn-danger { background: transparent; border: 1px solid var(--danger); color: #fca5a5; margin-top: 1rem; }
                    .btn-danger:hover { background: rgba(239, 68, 68, 0.2); }
                    .badge-list { display: flex; flex-wrap: wrap; gap: 0.5rem; margin-top: 1rem; }
                    .badge { background: rgba(59, 130, 246, 0.2); color: #93c5fd; padding: 0.3rem 0.75rem; border-radius: 999px; font-size: 0.85rem; }
                    table { width: 100%; border-collapse: collapse; margin-top: 0.5rem; font-size: 0.9rem; }
                    th, td { text-align: left; padding: 0.75rem; border-bottom: 1px solid var(--border); }
                    th { color: var(--muted); font-weight: 600; }
                    .positive { color: var(--success); font-weight: 600; }
                    .negative { color: #f87171; font-weight: 600; }
                    .settlement-item {
                        background: rgba(16, 185, 129, 0.1); border-left: 4px solid var(--success);
                        padding: 0.85rem 1rem; border-radius: 6px; margin-bottom: 0.6rem; font-size: 0.95rem;
                    }
                    .empty { color: var(--muted); font-style: italic; font-size: 0.9rem; }
                </style>
            </head>
            <body>
            <div class="container">
                <header>
                    <h1>Organizador de Despesas Compartilhadas</h1>
                    <p>Divisão inteligente de contas para viagens, churrascos e repúblicas</p>
                </header>
        """);

        if (errorMessage != null && !errorMessage.isBlank()) {
            html.append("<div class='alert'>").append(escapeHtml(errorMessage)).append("</div>");
        }

        html.append("<div class='grid'>");

        // CARD 1: Participantes
        html.append("""
            <div class="card">
                <h2>1. Participantes</h2>
                <form method="POST" action="/add-participant">
                    <div class="form-group">
                        <label>Nome do Participante</label>
                        <input type="text" name="name" placeholder="Ex: Ana, Bruno, Carla..." required>
                    </div>
                    <button type="submit">Adicionar ao Grupo</button>
                </form>
                <div class="badge-list">
        """);

        List<Participant> participants = service.getParticipants();
        if (participants.isEmpty()) {
            html.append("<span class='empty'>Nenhum participante cadastrado.</span>");
        } else {
            for (Participant p : participants) {
                html.append("<span class='badge'>").append(escapeHtml(p.getName())).append("</span>");
            }
        }
        html.append("</div></div>");

        // CARD 2: Nova Despesa
        html.append("""
            <div class="card">
                <h2>2. Registrar Despesa</h2>
                <form method="POST" action="/add-expense">
                    <div class="form-group">
                        <label>Descrição</label>
                        <input type="text" name="description" placeholder="Ex: Carne, Bebidas, Airbnb..." required>
                    </div>
                    <div class="form-group">
                        <label>Valor Total (R$)</label>
                        <input type="number" step="0.01" min="0.01" name="amount" placeholder="0.00" required>
                    </div>
                    <div class="form-group">
                        <label>Quem pagou?</label>
                        <select name="paidBy" required>
        """);

        for (Participant p : participants) {
            html.append("<option value='").append(p.getId()).append("'>")
                .append(escapeHtml(p.getName())).append("</option>");
        }

        html.append("""
                        </select>
                    </div>
                    <div class="form-group">
                        <label>Dividir entre:</label>
                        <div class="checkbox-list">
        """);

        if (participants.isEmpty()) {
            html.append("<span class='empty'>Cadastre participantes primeiro.</span>");
        } else {
            for (Participant p : participants) {
                html.append("<label class='checkbox-item'>")
                    .append("<input type='checkbox' name='sharedWith' value='").append(p.getId()).append("' checked> ")
                    .append(escapeHtml(p.getName()))
                    .append("</label>");
            }
        }

        html.append("""
                        </div>
                    </div>
                    <button type="submit">Lançar Despesa</button>
                </form>
            </div>
        </div>
        """);

        // RESULTADOS: Saldos e Acerto Final
        html.append("<div class='grid'>");

        // CARD 3: Saldo Líquido
        html.append("<div class='card'><h2>3. Saldo Líquido Individual</h2>");
        Map<Participant, BigDecimal> balances = service.calculateNetBalances();
        if (balances.isEmpty()) {
            html.append("<p class='empty'>Aguardando lançamentos...</p>");
        } else {
            html.append("<table><tr><th>Participante</th><th>Situação</th></tr>");
            for (Map.Entry<Participant, BigDecimal> entry : balances.entrySet()) {
                BigDecimal val = entry.getValue();
                String status;
                if (val.compareTo(BigDecimal.ZERO) > 0) {
                    status = String.format("<span class='positive'>A receber R$ %.2f</span>", val);
                } else if (val.compareTo(BigDecimal.ZERO) < 0) {
                    status = String.format("<span class='negative'>A pagar R$ %.2f</span>", val.abs());
                } else {
                    status = "<span class='empty'>Quitado</span>";
                }
                html.append("<tr><td>").append(escapeHtml(entry.getKey().getName())).append("</td><td>").append(status).append("</td></tr>");
            }
            html.append("</table>");
        }
        html.append("</div>");

        // CARD 4: Plano de Quitação
        html.append("<div class='card'><h2>4. Acerto Final (Mínimo de PIXs)</h2>");
        List<DebtSettlement> settlements = service.settleDebts();
        if (settlements.isEmpty()) {
            html.append("<p class='empty'>Nenhuma transferência pendente.</p>");
        } else {
            for (DebtSettlement s : settlements) {
                html.append("<div class='settlement-item'>")
                    .append("<strong>").append(escapeHtml(s.getFrom().getName())).append("</strong>")
                    .append(" paga ")
                    .append("<strong>R$ ").append(String.format("%.2f", s.getAmount())).append("</strong>")
                    .append(" para ")
                    .append("<strong>").append(escapeHtml(s.getTo().getName())).append("</strong>")
                    .append("</div>");
            }
        }
        html.append("""
            <form method="POST" action="/clear">
                <button type="submit" class="btn-danger">Reiniciar Tudo</button>
            </form>
        </div>
        </div>
        """);

        // CARD 5: Histórico de Despesas
        html.append("<div class='card'><h2>Histórico de Despesas Lançadas</h2>");
        List<Expense> expenses = service.getExpenses();
        if (expenses.isEmpty()) {
            html.append("<p class='empty'>Nenhuma despesa registrada ainda.</p>");
        } else {
            html.append("<table><tr><th>Descrição</th><th>Pago por</th><th>Valor</th><th>Dividido entre</th></tr>");
            for (Expense e : expenses) {
                html.append("<tr>")
                    .append("<td>").append(escapeHtml(e.getDescription())).append("</td>")
                    .append("<td>").append(escapeHtml(e.getPaidBy().getName())).append("</td>")
                    .append("<td>R$ ").append(String.format("%.2f", e.getAmount())).append("</td>")
                    .append("<td>").append(e.getSplits().size()).append(" pessoa(s)</td>")
                    .append("</tr>");
            }
            html.append("</table>");
        }

        html.append("</div></div></body></html>");
        return html.toString();
    }

    private static String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;");
    }
}