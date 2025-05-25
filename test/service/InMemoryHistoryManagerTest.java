package service;

import model.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

public class InMemoryHistoryManagerTest {
    private HistoryManager historyManager;

    @BeforeEach
    void initialization() {
        historyManager = Managers.getDefaultHistory();
    }

    @Test
    void shouldAddTasksAtHistoryList() {
        Epic epic = new Epic("Очень важная задача", "Выполнить для эпика");
        Task task = new Task("Задача №1", "Выполнить задачу №1");
        Subtask subTask = new Subtask ("Подзадача №1", "Выполнить подзадачу №1", 1);
        historyManager.add(epic);
        historyManager.add(task);
        historyManager.add(subTask);
        final List<Task> history = historyManager.getHistory();
        Assertions.assertNotNull(history, "Список просмотров пуст");
    }

    @Test
    void shouldAddNewTaskAtTheEndOfHistoryList() {
        Task taskFirst = new Task("Задача №1", "Что то там");
        Task taskSecond = new Task("Задача №2", "Еще что то");

        historyManager.add(taskFirst);
        historyManager.add(taskSecond);

        final List<Task> history = historyManager.getHistory();
        Assertions.assertEquals(taskSecond, history.get(
                history.size() - 1), "Задача не последнеея в списке ");
    }
}
