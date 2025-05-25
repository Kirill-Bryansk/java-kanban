package service;

import model.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class InMemoryTaskManagerTest {
    private TaskManager manager;
    private Task task;
    private Epic epic;
    private Subtask subtask;

    @BeforeEach
    void initialization() {
        manager = Managers.getDefault();
        task = manager.addTask(new Task("Задача №1", "Делать задачу №1"));
        epic = manager.addEpic(new Epic("Епик №1", "Делать эпик №1"));
        subtask = manager.addSubtask(new Subtask("Подзадача №1 эпика №1", "Делать подзадачу №1", 2));
    }

    @Test
    void shouldAddTask() {
        final int taskId = task.getId();
        final Task savedTask = manager.getTaskById(taskId);

        assertNotNull(savedTask, "Задача не найдена.");
        assertEquals(task, savedTask, "Задачи не совпадают.");

        final List<Task> tasks = manager.getTaskMap();

        assertNotNull(tasks, "Задачи не возвращаются.");
        assertEquals(1, tasks.size(), "Неверное количество задач.");
        assertEquals(task, tasks.get(0), "Задачи не совпадают.");
    }

    @Test
    void shouldGetTaskById() {
        Assertions.assertEquals(task, manager.getTaskById(1));
    }

    @Test
    void shouldGetTaskMap() {
        final List<Task> tasks = manager.getTaskMap();

        Assertions.assertNotNull(tasks, "Список пуст");
        Assertions.assertEquals(tasks, manager.getTaskMap());
    }

    @Test
    void shouldDeleteTaskById() {
        Task taskToDelete = task;

        manager.deleteTaskById(taskToDelete.getId());
        Task deletedTask = manager.getTaskById(taskToDelete.getId());

        Assertions.assertNull(deletedTask);
    }

    @Test
    void shouldClearAllTasks() {
        manager.clearTask();
        final List<Task> tasks = manager.getTaskMap();

        Assertions.assertNotNull(tasks, "Список не пуст");
        Assertions.assertTrue(tasks.isEmpty(), "Список должен быть пуст");
    }

    @Test
    void shouldAddEpic() {
        final int epicId = epic.getId();
        final Epic savedEpic = manager.getEpicById(epicId);

        assertNotNull(savedEpic, "Задача не найдена.");
        assertEquals(epic, savedEpic, "Задачи не совпадают.");

        final List<Epic> epics = manager.getEpicMap();

        assertNotNull(epics, "Задачи не возвращаются.");
        assertEquals(1, epics.size(), "Неверное количество задач.");
        assertEquals(epic, epics.get(0), "Задачи не совпадают.");
    }

    @Test
    public void epicsWithGeneratedAndAssignedIdsDoNotConflict() {
        Epic epicSecond = manager.addEpic(new Epic("Епик №2", "Делать эпик №2"));

        Assertions.assertNotEquals(epicSecond.getId(), epic.getId(), "Id должны быть уникальными");
    }

    @Test
    public void epicWhenAddedToManager() {
        Epic testEpic = manager.getEpicById(2);

        Assertions.assertEquals(epic.getId(), testEpic.getId());
        Assertions.assertEquals(epic.getName(), testEpic.getName());
        Assertions.assertEquals(epic.getStatus(), testEpic.getStatus());
    }

    @Test
    void shouldUpdateEpic() {
        Epic oldEpic = epic;
        ArrayList<Subtask> newSubtask = epic.getSubtaskList();
        Epic newEpic = new Epic("Обновленный эпик", "Делать эпик", epic.getId(), Status.NEW, newSubtask);
        manager.updateEpic(newEpic);

        Assertions.assertEquals(newEpic, oldEpic);
    }

    @Test
    void shouldGetEpicById() {
        Assertions.assertEquals(epic, manager.getEpicById(2));
    }

    @Test
    void shouldGetEpicMap() {
        final List<Epic> epics = manager.getEpicMap();

        Assertions.assertNotNull(epics, "Список пуст");
        Assertions.assertEquals(epics, manager.getEpicMap());
    }

    @Test
    void shouldDeleteEpicById() {
        Epic epicToDelete = epic;

        manager.deleteEpicById(epicToDelete.getId());
        Epic deletedEpic = manager.getEpicById(epicToDelete.getId());

        Assertions.assertNull(deletedEpic);
    }

    @Test
    void shouldAddSubtask() {

        final int subtaskId = subtask.getId();
        final Subtask savedSubtask = manager.getSubtaskById(subtaskId);

        assertNotNull(savedSubtask, "Задача не найдена.");
        assertEquals(subtask, savedSubtask, "Задачи не совпадают.");

        final List<Subtask> subtasks = manager.getSubtaskMap();

        assertNotNull(subtasks, "Задачи не возвращаются.");
        assertEquals(1, subtasks.size(), "Неверное количество задач.");
        assertEquals(subtask, subtasks.get(0), "Задачи не совпадают.");
    }

    @Test
    public void subtasksWithGeneratedAndAssignedIdsDoNotConflict() {
        manager.addEpic(epic);
        Subtask subtaskOne = new Subtask("Подзадача №1", "Выполнять подзадачу", epic.getId());
        Subtask subtaskTwo = new Subtask("Подзадача №2", "Выполнять подзадачу", 2);

        manager.addSubtask(subtaskOne);
        manager.addSubtask(subtaskTwo);

        Assertions.assertNotEquals(subtaskOne.getId(), subtaskTwo.getId(), "Subtask ids should be unique");
    }

    @Test
    void shouldUpdateSubtask() {
        Subtask expectedSubtask = new Subtask("Подзадача №1", "Выполнять подзадачу", 3,
                Status.NEW, epic.getId());
        Subtask updatedSubtask = new Subtask("Подзадача №1", "Выполнять новую  подзадачу", 3,
                Status.NEW, epic.getId());
        manager.updateSubtask(updatedSubtask);
        Subtask actualSubtask = manager.getSubtaskById(updatedSubtask.getId());

        Assertions.assertEquals(expectedSubtask, actualSubtask, "Они не равны");
    }

    @Test
    void shouldGetSubtasksOfEpic() {
        manager.addSubtask(new Subtask("Подзадача №2 эпика №1", "Делать подзадачу №2", 2));
        manager.addSubtask(new Subtask("Подзадача №3 эпика №1", "Делать подзадачу №3", 2));

        final List<Subtask> subtasks = manager.getSubtaskByEpic(epic.getId());

        Assertions.assertNotNull(subtasks, "Список не должен бытть пуст");
        Assertions.assertEquals(3, subtasks.size());
    }

    @Test
    void shouldGetSubtaskById() {
        Subtask expectedSubtask = new Subtask("Подзадача №2 эпика №1", "Делать подзадачу №2",
                subtask.getId(), Status.NEW, 2);

        Subtask savedSubtask = manager.getSubtaskById(subtask.getId());
        Subtask actualSubtask = manager.getSubtaskById(savedSubtask.getId());

        Assertions.assertEquals(expectedSubtask, actualSubtask);
    }

    @Test
    void shouldGetAllSubtasks() {
        manager.addSubtask(new Subtask("Подзадача №2 эпика №1", "Делать подзадачу №2", 2));
        manager.addSubtask(new Subtask("Подзадача №3 эпика №1", "Делать подзадачу №3", 2));

        final List<Subtask> subtasks = manager.getSubtaskMap();

        Assertions.assertNotNull(subtasks, "Список не должен быть пуст");
        Assertions.assertEquals(3, subtasks.size());
    }

    @Test
    void shouldDeleteSubtaskById() {
        Subtask subtaskToDelete = new Subtask("Подзадача №1 эпика №1", "Делать подзадачу №1", 3,
                Status.NEW, 2);

        manager.deleteSubtaskById(subtaskToDelete.getId());
        Subtask deletedSubtask = manager.getSubtaskById(subtaskToDelete.getId());

        Assertions.assertNull(deletedSubtask);
    }

    @Test
    void shouldClearAllSubtasks() {
        manager.addSubtask(new Subtask("Подзадача №2 эпика №1", "Делать подзадачу №2", 2));
        manager.addSubtask(new Subtask("Подзадача №3 эпика №1", "Делать подзадачу №3", 2));

        manager.clearSubtask();
        final List<Subtask> subtasks = manager.getSubtaskMap();

        Assertions.assertNotNull(subtasks, "Список должен вовращать null");
        Assertions.assertTrue(subtasks.isEmpty(), "Список должен быть пуст");
    }
}