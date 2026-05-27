package by.grsu.tracker;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Paths;

public class WebServer {

    public void start() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        server.createContext("/", exchange -> {
            try {
                serveFile(exchange, "web/index.html", "text/html; charset=UTF-8");
            } catch (Exception e) {
                sendError(exchange, e.getMessage());
            }
        });

        server.createContext("/app.js", exchange -> {
            try {
                serveFile(exchange, "web/app.js", "application/javascript; charset=UTF-8");
            } catch (Exception e) {
                sendError(exchange, e.getMessage());
            }
        });

        server.createContext("/style.css", exchange -> {
            try {
                serveFile(exchange, "web/style.css", "text/css; charset=UTF-8");
            } catch (Exception e) {
                sendError(exchange, e.getMessage());
            }
        });

        server.createContext("/api/parse", exchange -> {
            if ("POST".equals(exchange.getRequestMethod())) {
                try {
                    new TelegramParser().parseAll();
                    new Classifier().classifyAll();
                    sendJson(exchange, "{\"status\":\"ok\",\"message\":\"Парсинг завершён\"}");
                } catch (Exception e) {
                    sendJson(exchange, "{\"status\":\"error\",\"message\":\"" + e.getMessage() + "\"}");
                }
            }
        });

        server.createContext("/api/events", exchange -> {
            if ("GET".equals(exchange.getRequestMethod())) {
                try {
                    byte[] data = Files.readAllBytes(Paths.get("data/events.json"));
                    exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
                    exchange.sendResponseHeaders(200, data.length);
                    try (OutputStream os = exchange.getResponseBody()) { os.write(data); }
                } catch (Exception e) {
                    sendJson(exchange, "[]");
                }
            }
        });

        server.createContext("/api/report", exchange -> {
            if ("POST".equals(exchange.getRequestMethod())) {
                String query = exchange.getRequestURI().getQuery();
                String period = (query != null && query.startsWith("period=")) ? query.substring(7) : "текущий";
                new ReportGenerator().generate(period);
                sendJson(exchange, "{\"status\":\"ok\",\"message\":\"Отчёт сгенерирован\"}");
            }
        });

        server.setExecutor(null);
        server.start();
        System.out.println("Сервер запущен: http://localhost:8080");
    }

    private void serveFile(HttpExchange exchange, String resource, String contentType) throws IOException {
        // Берём абсолютный путь от корня проекта
        String projectRoot = System.getProperty("user.dir");
        File file = new File(projectRoot + File.separator + "src" + File.separator + "main"
                + File.separator + "resources" + File.separator + resource.replace("/", File.separator));

        System.out.println("Ищу файл: " + file.getAbsolutePath() + " | exists: " + file.exists());

        if (!file.exists()) {
            String msg = "404: " + file.getAbsolutePath();
            exchange.sendResponseHeaders(404, msg.getBytes("UTF-8").length);
            try (OutputStream os = exchange.getResponseBody()) { os.write(msg.getBytes("UTF-8")); }
            return;
        }

        byte[] data = Files.readAllBytes(file.toPath());
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.sendResponseHeaders(200, data.length);
        try (OutputStream os = exchange.getResponseBody()) { os.write(data); }
    }

    private void sendJson(HttpExchange exchange, String json) throws IOException {
        byte[] response = json.getBytes("UTF-8");
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(200, response.length);
        try (OutputStream os = exchange.getResponseBody()) { os.write(response); }
    }

    private void sendError(HttpExchange exchange, String message) throws IOException {
        String msg = "Ошибка: " + message;
        exchange.sendResponseHeaders(500, msg.length());
        try (OutputStream os = exchange.getResponseBody()) { os.write(msg.getBytes()); }
    }
}