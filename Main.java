import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Main {
    private static final ExpenseSplitterService service = new ExpenseSplitterService();
    private static String lastError = null;

    public static void main(String[] args) throws IOException {
        int port = 8080;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext("/", exchange -> {
            String html = WebInterface.renderPage(service, lastError);
            lastError = null;
            sendHtml(exchange, html);
        });

        server.createContext("/add-participant", exchange -> {
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                Map<String, List<String>> params = parseForm(exchange);
                try {
                    String name = getFirst(params, "name");
                    service.registerParticipant(name);
                } catch (Exception e) {
                    lastError = e.getMessage();
                }
            }
            redirectHome(exchange);
        });

        server.createContext("/add-expense", exchange -> {
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                Map<String, List<String>> params = parseForm(exchange);
                try {
                    String description = getFirst(params, "description");
                    BigDecimal amount = new BigDecimal(getFirst(params, "amount").replace(",", "."));
                    Participant payer = service.findParticipantById(getFirst(params, "paidBy"));

                    List<String> sharedIds = params.getOrDefault("sharedWith", List.of());
                    List<Participant> sharedWith = new ArrayList<>();
                    for (String id : sharedIds) {
                        sharedWith.add(service.findParticipantById(id));
                    }

                    service.addEqualExpense(description, payer, amount, sharedWith);
                } catch (Exception e) {
                    lastError = e.getMessage();
                }
            }
            redirectHome(exchange);
        });

        server.createContext("/clear", exchange -> {
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                service.clearAll();
                lastError = null;
            }
            redirectHome(exchange);
        });

        server.setExecutor(null);
        server.start();

        System.out.println("=======================================================");
        System.out.println("Sistema iniciado com sucesso na porta " + port + "!");
        System.out.println("Clique em 'Abrir no Navegador' na notificação do Codespaces");
        System.out.println("ou acesse a aba 'Portas' (Ports) e abra o endereço da porta 8080.");
        System.out.println("=======================================================");
    }

    private static void sendHtml(HttpExchange exchange, String html) throws IOException {
        byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static void redirectHome(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().set("Location", "/");
        exchange.sendResponseHeaders(303, -1);
        exchange.close();
    }

    private static Map<String, List<String>> parseForm(HttpExchange exchange) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Map<String, List<String>> params = new HashMap<>();
        if (body.isBlank()) return params;

        for (String pair : body.split("&")) {
            String[] kv = pair.split("=", 2);
            String key = URLDecoder.decode(kv[0], StandardCharsets.UTF_8);
            String value = kv.length > 1 ? URLDecoder.decode(kv[1], StandardCharsets.UTF_8) : "";
            params.computeIfAbsent(key, k -> new ArrayList<>()).add(value);
        }
        return params;
    }

    private static String getFirst(Map<String, List<String>> params, String key) {
        List<String> values = params.get(key);
        if (values == null || values.isEmpty()) {
            throw new IllegalArgumentException("Campo obrigatório ausente: " + key);
        }
        return values.get(0);
    }
}