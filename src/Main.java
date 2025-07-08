import service.*;
import exception.TaskNotFoundException;
import model.*;

import java.time.Duration;
import java.time.LocalDateTime;

public class Main {

    public static void main(String[] args) {
        TaskManager manager = new InMemoryTaskManager(new InMemoryHistoryManager());
        // Две задачи
        manager.addTask(new Task("Задача 1", "Выполнить 1 задачу",
                Duration.ofMinutes(10), LocalDateTime.of(2025, 7, 1, 0, 10)));
        manager.addTask(new Task("Задача 2", "Выполнить 2 задачу",
                Duration.ofMinutes(10), LocalDateTime.of(2025, 8, 1, 0, 10)));
        // Эпик с двумя подзадачами
        Epic epic1 = new Epic("Эпик 1", "Очень Важный",
                Duration.ofMinutes(10), LocalDateTime.of(2025, 7, 6, 0, 10));
        manager.addEpic(epic1);
        manager.addSubtask(new Subtask("Sub - Эпика 1", "Подзадача 1",
                Duration.ofMinutes(10), LocalDateTime.of(2025, 7, 6, 0, 10), 3));
        manager.addSubtask(new Subtask("Sub - Эпика 1", "Подзадача 2",
                Duration.ofMinutes(10), LocalDateTime.of(2025, 7, 7, 0, 10), 3));
        // Эпик с одной подзадачей
        Epic epic2 = new Epic("Эпик 2", "Не менее Важный",
                Duration.ofMinutes(10), LocalDateTime.of(2025, 7, 8, 0, 10));
        manager.addEpic(epic2);
        manager.addSubtask(new Subtask("Sub - Эпика 2", "Подзадача 1",
                Duration.ofMinutes(10), LocalDateTime.of(2025, 7, 9, 0, 10), 6));
        // Печать списки Эпиков, Задач, Подзадач
        System.out.println(manager.getTaskMap());
        System.out.println(manager.getEpicMap());
        System.out.println(manager.getSubtaskMap());
        // Изменяю статус ru.yandex.java_kanban.model.Task
        manager.changeStatus(1, Status.DONE);
        manager.changeStatus(2, Status.IN_PROGRESS);
        // Изменяю статус SubTask
        manager.changeStatus(4, Status.DONE);
        manager.changeStatus(5, Status.DONE);
        manager.changeStatus(7, Status.IN_PROGRESS);
        manager.updateEpicStatus(epic1);
        manager.updateEpicStatus(epic2);
        System.out.println("Cписок истории");
        System.out.println(manager.getHistory().toString().replaceAll("[\\[\\]]", "").replaceAll("[,]", ""));
        System.out.println("-".repeat(10));
        System.out.println(" Приоритетные задачи");
        System.out.println(manager.getPrioritizedTasks());
    }
}
