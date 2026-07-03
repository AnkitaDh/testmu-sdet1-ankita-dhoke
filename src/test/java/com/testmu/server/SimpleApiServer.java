package com.testmu.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class SimpleApiServer {
    private final HttpServer server;
    private final Map<String, JSONObject> items = new ConcurrentHashMap<>();
    private final AtomicInteger nextId = new AtomicInteger(1);
    private final Map<String, Integer> requestCounts = new ConcurrentHashMap<>();

    public SimpleApiServer(int port) throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/api/login", new LoginHandler());
        server.createContext("/api/items", new ItemsHandler());
        server.createContext("/api/boom", new BoomHandler());
        server.createContext("/api/schema", new SchemaHandler());
        server.createContext("/api/", new NotFoundHandler());
        items.put("1", new JSONObject().put("id", "1").put("name", "Widget Alpha").put("description", "Performance widget").put("value", 120));
        items.put("2", new JSONObject().put("id", "2").put("name", "Widget Beta").put("description", "Analytics widget").put("value", 230));
        items.put("3", new JSONObject().put("id", "3").put("name", "Widget Gamma").put("description", "Security widget").put("value", 95));
    }

    public void start() {
        server.start();
    }

    public void stop() {
        server.stop(0);
    }

    public void resetState() {
        requestCounts.clear();
        items.clear();
        nextId.set(3);
        items.put("1", new JSONObject().put("id", "1").put("name", "Widget Alpha").put("description", "Performance widget").put("value", 120));
        items.put("2", new JSONObject().put("id", "2").put("name", "Widget Beta").put("description", "Analytics widget").put("value", 230));
        items.put("3", new JSONObject().put("id", "3").put("name", "Widget Gamma").put("description", "Security widget").put("value", 95));
    }

    private static String readBody(HttpExchange exchange) throws IOException {
        return new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
    }

    private static void sendJson(HttpExchange exchange, int status, String body) throws IOException {
        exchange.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(status, body.getBytes(StandardCharsets.UTF_8).length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(body.getBytes(StandardCharsets.UTF_8));
        }
    }

    private boolean authorize(HttpExchange exchange) {
        var auth = exchange.getRequestHeaders().getFirst("Authorization");
        return auth != null && auth.equals("Bearer valid-token");
    }

    private class LoginHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }
            var body = readBody(exchange);
            var payload = new JSONObject(body);
            var email = payload.optString("email");
            var password = payload.optString("password");
            if ("tester@testmu.ai" .equals(email) && "TestMu123!".equals(password)) {
                sendJson(exchange, 200, new JSONObject().put("token", "valid-token").toString());
            } else {
                sendJson(exchange, 401, new JSONObject().put("error", "Invalid credentials").toString());
            }
        }
    }

    private class ItemsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!authorize(exchange)) {
                sendJson(exchange, 401, new JSONObject().put("error", "Unauthorized").toString());
                return;
            }
            var client = exchange.getRemoteAddress().getAddress().getHostAddress();
            var count = requestCounts.getOrDefault(client, 0);
            if (count >= 5) {
                sendJson(exchange, 429, new JSONObject().put("error", "Rate limit exceeded").toString());
                return;
            }
            requestCounts.put(client, count + 1);

            switch (exchange.getRequestMethod()) {
                case "GET" -> handleGet(exchange);
                case "POST" -> handlePost(exchange);
                case "PUT" -> handlePut(exchange);
                case "DELETE" -> handleDelete(exchange);
                default -> exchange.sendResponseHeaders(405, -1);
            }
        }

        private void handleGet(HttpExchange exchange) throws IOException {
            var path = exchange.getRequestURI().getPath();
            if (path.matches("/api/items/\\d+")) {
                var id = path.substring(path.lastIndexOf('/') + 1);
                var item = items.get(id);
                if (item == null) {
                    sendJson(exchange, 404, new JSONObject().put("error", "Item not found").toString());
                } else {
                    sendJson(exchange, 200, item.toString());
                }
                return;
            }
            var allItems = new JSONArray(items.values());
            sendJson(exchange, 200, new JSONObject().put("items", allItems).toString());
        }

        private void handlePost(HttpExchange exchange) throws IOException {
            var body = readBody(exchange);
            var payload = new JSONObject(body);
            if (!payload.has("name") || payload.optString("name").isBlank() || !payload.has("value")) {
                sendJson(exchange, 400, new JSONObject().put("error", "Invalid item payload").toString());
                return;
            }
            var id = String.valueOf(nextId.incrementAndGet());
            var item = new JSONObject()
                    .put("id", id)
                    .put("name", payload.getString("name"))
                    .put("description", payload.optString("description", ""))
                    .put("value", payload.getInt("value"));
            items.put(id, item);
            sendJson(exchange, 201, item.toString());
        }

        private void handlePut(HttpExchange exchange) throws IOException {
            var path = exchange.getRequestURI().getPath();
            if (!path.matches("/api/items/\\d+")) {
                exchange.sendResponseHeaders(404, -1);
                return;
            }
            var id = path.substring(path.lastIndexOf('/') + 1);
            var item = items.get(id);
            if (item == null) {
                sendJson(exchange, 404, new JSONObject().put("error", "Item not found").toString());
                return;
            }
            var payload = new JSONObject(readBody(exchange));
            if (payload.has("name")) item.put("name", payload.getString("name"));
            if (payload.has("description")) item.put("description", payload.getString("description"));
            if (payload.has("value")) item.put("value", payload.getInt("value"));
            items.put(id, item);
            sendJson(exchange, 200, item.toString());
        }

        private void handleDelete(HttpExchange exchange) throws IOException {
            var path = exchange.getRequestURI().getPath();
            if (!path.matches("/api/items/\\d+")) {
                exchange.sendResponseHeaders(404, -1);
                return;
            }
            var id = path.substring(path.lastIndexOf('/') + 1);
            if (!items.containsKey(id)) {
                sendJson(exchange, 404, new JSONObject().put("error", "Item not found").toString());
                return;
            }
            items.remove(id);
            exchange.sendResponseHeaders(204, -1);
        }
    }

    private class BoomHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            sendJson(exchange, 500, new JSONObject().put("error", "Server error simulated").toString());
        }
    }

    private class SchemaHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            var schema = new JSONObject();
            schema.put("type", "object");
            schema.put("properties", new JSONObject().put("items", new JSONObject().put("type", "array").put("items", new JSONObject().put("type", "object").put("properties", new JSONObject().put("id", new JSONObject().put("type", "string")).put("name", new JSONObject().put("type", "string")).put("description", new JSONObject().put("type", "string")).put("value", new JSONObject().put("type", "number"))).put("required", new JSONArray().put("id").put("name").put("value")))));
            sendJson(exchange, 200, schema.toString());
        }
    }

    private static class NotFoundHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            exchange.sendResponseHeaders(404, -1);
        }
    }
}
