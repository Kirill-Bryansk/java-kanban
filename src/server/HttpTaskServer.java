package server;

import com.sun.net.httpserver.HttpServer;
import handlers.*;
import service.FileBackedTaskManager;
import service.HistoryManager;
import service.Managers;
import service.TaskManager;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Path;

public class HttpTaskServer {
   /* private static final int PORT = 8080;
    private final HttpServer server;
    private final TaskManager taskManager;

    public HttpTaskServer(TaskManager taskManager) throws IOException {
        this.taskManager = taskManager;
        server = HttpServer.create(new InetSocketAddress("localhost", PORT), 0);
    }*/

    private final HttpServer server;
    private final TaskManager taskManager;

    private static final int PORT = 8080;
    public HttpTaskServer(FileBackedTaskManager taskService) throws IOException {
        this.taskManager = taskService;
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
        System.out.println("Http - сервер остановлен на " + PORT + "порту.");
    }

    public static void main(String[] args) throws IOException {
        Path path = Path.of("src/data/data.csv");
        File file = new File(path.toString());

        //FileBackedTaskManager fileBackedTaskManager = FileBackedTaskManager.loadFromFile(file);

        FileBackedTaskManager taskManager = new FileBackedTaskManager(Managers.getDefaultHistory(), file);
       //FileBackedTaskManager manager = taskManager.loadFromFile(file);
        //dTaskManager manager = new FileBackedTaskManager(Managers.getDefaultHistory(), file);
        //manager.loadFromFile(file); // там другие менеджеры, не грузит

        HttpTaskServer server = new HttpTaskServer(taskManager);
        server.start();

      /*  try {
            //HistoryManager historyManager = Managers.getDefaultHistory();
            FileBackedTaskManager taskManager = new FileBackedTaskManager(file);
           taskManager.loadFromFile(file);
            HttpTaskServer server = new HttpTaskServer(taskManager);

            server.server.createContext("/tasks", new TasksHandler(taskManager));
            server.server.createContext("/subtasks", new SubtasksHandler(taskManager));
            server.server.createContext("/epics", new EpicsHandler(taskManager));
            server.server.createContext("/history", new HistoryHandler(taskManager));
            server.server.createContext("/prioritized", new PrioritizedHandler(taskManager));

            server.start();
        } catch (IOException e) {
            System.err.println("Ошибка при запуске сервера: " + e.getMessage());
            e.printStackTrace();
        }*/
    }
}
