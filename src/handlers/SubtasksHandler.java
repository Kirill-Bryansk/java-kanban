package handlers;

import com.sun.net.httpserver.HttpExchange;
import exception.ManagerErrorSaveTaskTime;
import exception.TaskNotFoundException;
import model.Subtask;
import service.TaskManager;

import java.io.IOException;

public class SubtasksHandler extends BaseHttpHandler {

    public SubtasksHandler(TaskManager taskManager) {
        super(taskManager);
    }

    @Override
    protected void handleGet(HttpExchange exchange, String body) throws IOException {
        BaseHttpHandler.IdRequest idRequest = gson.fromJson(body, IdRequest.class);
        if (idRequest == null) {
            sendText(exchange, gson.toJson(taskManager.getSubtaskMap()), 200);
        } else {
            int subtaskId = idRequest.getId();
            Subtask subtask;
            try {
                subtask = taskManager.getSubtaskById(subtaskId);
                if (subtask != null) {
                    sendText(exchange, gson.toJson(subtask), 200);
                } else {
                    send404Error(exchange, "Подзадача с номером id: " + subtaskId + " не найдена");
                }
            } catch (TaskNotFoundException e) {
                send404Error(exchange, "Подзадача с номером id: " + subtaskId + " не найдена");
            }
        }
    }

    @Override
    protected void handlePost(HttpExchange exchange, String body) throws IOException {
        Subtask subtask = gson.fromJson(body, Subtask.class);
        try {
            if (!(body.contains("id")) || subtask.getId() == 0) {
                taskManager.addSubtask(subtask);
                sendText(exchange, "Подзадача добавлена", 201);
            } else {
                taskManager.updateSubtask(subtask);
                sendText(exchange, "Подзадача обновлена", 200);
            }
        } catch (ManagerErrorSaveTaskTime errorSaveTaskTime) {
            send406Error(exchange, errorSaveTaskTime.getMessage());
        } catch (IllegalArgumentException e) {
            // Обработка случая, когда эпик, субтаск не найден
            sendText(exchange, e.getMessage(), 404);
        }
    }

    @Override
    protected void handleDelete(HttpExchange exchange, String body) throws IOException {
        BaseHttpHandler.IdRequest idRequest = gson.fromJson(body, BaseHttpHandler.IdRequest.class);
        if (idRequest == null) {
            taskManager.clearSubtask();
            sendText(exchange, "Все подзадачи удалены", 200);
        } else {
            int subtaskId = idRequest.getId();
            try {
                Subtask subtask = taskManager.getSubtaskById(subtaskId);
                if (subtask == null) {
                    send404Error(exchange, "Подзадача с номером id: " + subtaskId + " не найдена");
                    return;
                }
                taskManager.deleteSubtaskById(subtaskId);
                sendText(exchange, "Подзадача удалена", 200);
            } catch (TaskNotFoundException e) {
                send404Error(exchange, "Ошибка при удалении подзадачи: " + e.getMessage());
            }
        }
    }
}

