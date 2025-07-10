package handlers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import server.DurationSerializerAdapter;
import server.LocalDateTimeSerializer;
import service.TaskManager;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;

public class BaseHttpHandler implements HttpHandler {
    protected Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeSerializer())
            .registerTypeAdapter(Duration.class, new DurationSerializerAdapter())
            .create();
    protected TaskManager taskManager;

    public BaseHttpHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        System.out.println("Обаботка запроса...");

        try {
            String requestMethod = exchange.getRequestMethod();
            InputStream inputStreamTask = exchange.getRequestBody();
            String body = new String(inputStreamTask.readAllBytes(), StandardCharsets.UTF_8);

            switch (requestMethod) {
                case "GET":
                    handleGet(exchange, body);
                    break;
                case "POST":
                    handlePost(exchange, body);
                    break;
                case "DELETE":
                    handleDelete(exchange, body);
                    break;
                default:
                    send405Error(exchange, "Method GET, POST, DELETE have not been transferred");
            }
        } catch (Exception exception) {
            exception.printStackTrace();
            send500Error(exchange, "Internal Server Error");
        } finally {
            exchange.close();
        }
    }

    protected void handleGet(HttpExchange exchange, String body) throws IOException {
    }

    protected void handlePost(HttpExchange exchange, String body) throws IOException {
    }

    protected void handleDelete(HttpExchange exchange, String body) throws IOException {
    }

    public static class IdRequest {
        private int id;

        public void setId(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }
    }

    protected void sendText(HttpExchange exchange, String text, int code) throws IOException {
        byte[] response = text.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        exchange.sendResponseHeaders(code, response.length);
        exchange.getResponseBody().write(response);
        exchange.close();
    }

    protected void send404Error(HttpExchange exchange, String error404) throws IOException {
        byte[] response = error404.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        exchange.sendResponseHeaders(404, response.length);
        exchange.getResponseBody().write(response);
        exchange.close();
    }

    protected void send405Error(HttpExchange exchange, String error405) throws IOException {
        byte[] response = error405.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        exchange.sendResponseHeaders(405, response.length);
        exchange.getResponseBody().write(response);
        exchange.close();
    }

    protected void send406Error(HttpExchange exchange, String error406) throws IOException {
        byte[] response = error406.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        exchange.sendResponseHeaders(406, response.length);
        exchange.getResponseBody().write(response);
        exchange.close();
    }

    protected void send500Error(HttpExchange exchange, String error500) throws IOException {
        byte[] response = error500.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        exchange.sendResponseHeaders(500, response.length);
        exchange.getResponseBody().write(response);
        exchange.close();
    }
}