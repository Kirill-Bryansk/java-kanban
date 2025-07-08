package handlers;

import com.sun.net.httpserver.HttpExchange;
import exception.ManagerErrorSaveTaskTime;
import exception.TaskNotFoundException;
import model.Epic;
import service.TaskManager;

import java.io.IOException;

public class EpicsHandler extends BaseHttpHandler {

    public EpicsHandler(TaskManager taskManager) {
        super(taskManager);
    }

    @Override
    protected void handelGet(HttpExchange exchange, String body) throws IOException {
        BaseHttpHandler.IdRequest idRequest = gson.fromJson(body, IdRequest.class);
        if (idRequest == null) {
            sendText(exchange, gson.toJson(taskManager.getSubtaskMap()), 200);
        } else {
            int epicId = idRequest.getId();
            Epic epic;
            try {
                epic = taskManager.getEpicById(epicId);
                if (epic != null) {
                    sendText(exchange, gson.toJson(epic), 200);
                } else {
                    send404Error(exchange, "Эпик с номером id: " + epicId + " не найден");
                }
            } catch (TaskNotFoundException e) {
                send404Error(exchange, "Эпик с номером id: " + epicId + " не найден");
            }
        }
    }

    @Override
    protected void handlePost(HttpExchange exchange, String body) throws IOException {
        Epic epic = gson.fromJson(body, Epic.class);
        try {
            if (!(body.contains("id")) || epic.getId() == 0) {
                taskManager.addEpic(epic);
                sendText(exchange, "Эпик добавлен", 201);
            } else {
                taskManager.updateEpic(epic);
                sendText(exchange, "Эпик обновлен", 201);
            }
        } catch (ManagerErrorSaveTaskTime errorSaveTaskTime) {
            send406Error(exchange, errorSaveTaskTime.getMessage());
        }
    }

    @Override
    protected void handleDelete(HttpExchange exchange, String body) throws IOException {
        BaseHttpHandler.IdRequest idRequest = gson.fromJson(body, BaseHttpHandler.IdRequest.class);
        if (idRequest == null) {
            taskManager.clearEpic();
            sendText(exchange, "Все эпики удалены", 200);
        } else {
            int epicId = idRequest.getId();
            taskManager.deleteEpicById(epicId);
            sendText(exchange, "Эпик удален", 200);
        }
    }
}
