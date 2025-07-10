package handlers;

import com.sun.net.httpserver.HttpExchange;
import service.TaskManager;

import java.io.IOException;

public class PrioritizedHandler extends BaseHttpHandler {

    public PrioritizedHandler(TaskManager taskManager) {
        super(taskManager);
    }

    @Override
    protected void handleGet(HttpExchange exchange, String body) throws IOException {
        BaseHttpHandler.IdRequest idRequest = gson.fromJson(body, IdRequest.class);
        if (idRequest == null) {
            sendText(exchange, gson.toJson(taskManager.getPrioritizedTasks()), 200);
        }
    }
}
