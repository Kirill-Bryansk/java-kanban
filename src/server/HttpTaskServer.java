package server;

import com.sun.net.httpserver.HttpServer;
import handlers.*;
import service.FileBackedTaskManager;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Path;

public class HttpTaskServer {

    private final HttpServer server;

    private static final int PORT = 8080;

    public HttpTaskServer(FileBackedTaskManager taskService) throws IOException {
        server = HttpServer.create(new InetSocketAddress(PORT), 0);

        server.createContext("/tasks", new TasksHandler(taskService));
        server.createContext("/subtasks", new SubtasksHandler(taskService));
        server.createContext("/epics", new EpicsHandler(taskService));
        server.createContext("/history", new HistoryHandler(taskService));
        server.createContext("/prioritized", new PrioritizedHandler(taskService));
    }

    public void start() {
        server.start();
        System.out.println("Http - сервер запущен на " + PORT + " порту.");
    }

    public void stop() {
        server.stop(0);
        System.out.println("Http - сервер остановлен на " + PORT + " порту.");
    }

    public static void main(String[] args) throws IOException {
        Path path = Path.of("src/data/data.csv");
        File file = new File(path.toString());

        FileBackedTaskManager manager = FileBackedTaskManager.loadFromFile(file);

        HttpTaskServer server = new HttpTaskServer(manager);
        server.start();
    }
}
