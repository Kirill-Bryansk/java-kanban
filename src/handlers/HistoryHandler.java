package handlers;

import com.sun.net.httpserver.HttpExchange;
import service.TaskManager;

import java.io.IOException;

public class HistoryHandler extends BaseHttpHandler {
    public HistoryHandler(TaskManager taskManager) {
        super(taskManager);
    }

    @Override
    protected void handelGet(HttpExchange exchange, String body) throws IOException {
        BaseHttpHandler.IdRequest idRequest = gson.fromJson(body, IdRequest.class);
        if (idRequest == null) {
            sendText(exchange, gson.toJson(taskManager.getHistory()), 200);
        }
    }
}
