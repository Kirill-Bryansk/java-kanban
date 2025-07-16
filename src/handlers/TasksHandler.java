package handlers;

import com.sun.net.httpserver.HttpExchange;
import exception.ManagerErrorSaveTaskTime;
import exception.TaskNotFoundException;
import model.Task;
import service.TaskManager;

import java.io.IOException;

public class TasksHandler extends BaseHttpHandler {
    public TasksHandler(TaskManager taskManager) {
        super(taskManager);
    }

    @Override
    protected void handleGet(HttpExchange exchange, String body) throws IOException {
        IdRequest idRequest = gson.fromJson(body, IdRequest.class);
        if (idRequest == null) {
            sendText(exchange, gson.toJson(taskManager.getTaskMap()), 200);
        } else {
            int taskId = idRequest.getId();
            Task task;
            try {
                task = taskManager.getTaskById(taskId);
                if (task != null) {
                    sendText(exchange, gson.toJson(task), 200);
                } else {
                    send404Error(exchange, "Задача с номером id: " + taskId + " не найдена");
                }
            } catch (TaskNotFoundException e) {
                send404Error(exchange, "Задача с номером id: " + taskId + " не найдена");
            }
        }
    }

    @Override
    protected void handlePost(HttpExchange exchange, String body) throws IOException {
        Task task = gson.fromJson(body, Task.class);
        try {
            if (!(body.contains("id")) || task.getId() == 0) {
                taskManager.addTask(task);
                sendText(exchange, "Задача добавлена", 201);
            } else {
                taskManager.updateTask(task);
                sendText(exchange, "Задача обновлена", 200);
            }
        } catch (ManagerErrorSaveTaskTime errorSaveTaskTime) {
            send406Error(exchange, errorSaveTaskTime.getMessage());
        }
    }

    @Override
    protected void handleDelete(HttpExchange exchange, String body) throws IOException {
        IdRequest idRequest = gson.fromJson(body, IdRequest.class);
        if (idRequest == null) {
            taskManager.clearTask();
            sendText(exchange, "Все задачи удалены", 200);
        } else {
            int taskId = idRequest.getId();
            taskManager.deleteTaskById(taskId);
            sendText(exchange, "Задача удалена", 200);
        }
    }
}
