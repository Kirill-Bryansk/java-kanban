package service;

import exception.TaskNotFoundException;
import model.Epic;
import model.Subtask;
import model.Task;
import model.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public abstract class TaskManagerTest<T extends TaskManager> {
    protected T manager;
    protected Task task;
    protected Epic epic;
    protected Subtask subtask;

    protected abstract T createTaskManager();

    @BeforeEach
    void initialization() {
        manager = createTaskManager();
        task = manager.addTask(new Task("Задача 1", "Выполнить 1 задачу",
                Duration.ofMinutes(10), LocalDateTime.of(2025, 7, 1, 0, 10)));
        epic = manager.addEpic(new Epic("Епик №1", "Делать эпик №1"));
        subtask = manager.addSubtask(new Subtask("Подзадача №1 эпика №1", "Делать подзадачу №1",
                Duration.ofMinutes(10), LocalDateTime.now(), 2));
    }

    @Test
    void shouldAddTask() throws TaskNotFoundException {
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
    void shouldGetTaskById() throws TaskNotFoundException {
        Assertions.assertEquals(task, manager.getTaskById(1));
    }

    @Test
    void shouldGetTaskMap() {
        final List<Task> tasks = manager.getTaskMap();

        Assertions.assertNotNull(tasks, "Список пуст");
        Assertions.assertEquals(tasks, manager.getTaskMap());
    }

    @Test
    void shouldDeleteTaskById() throws TaskNotFoundException {
        Task taskToDelete = task;
        manager.deleteTaskById(taskToDelete.getId());
        assertFalse(manager.getTaskMap().contains(taskToDelete));
    }

    @Test
    void shouldClearAllTasks() {
        manager.clearTask();
        final List<Task> tasks = manager.getTaskMap();

        Assertions.assertNotNull(tasks, "Список не пуст");
        Assertions.assertTrue(tasks.isEmpty(), "Список должен быть пуст");
    }

    @Test
    void shouldAddEpic() throws TaskNotFoundException {
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
    public void epicWhenAddedToManager() throws TaskNotFoundException {
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
    void shouldGetEpicById() throws TaskNotFoundException {
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
        assertFalse(manager.getEpicMap().contains(epicToDelete));
    }

    @Test
    void shouldAddSubtask() throws TaskNotFoundException {

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
        Subtask subtaskOne = new Subtask("Подзадача №2 эпика №1", "Делать подзадачу №2",
                Duration.ofMinutes(10), LocalDateTime.now().plus(Duration.ofMinutes(10)), epic.getId());
        Subtask subtaskTwo = new Subtask("Подзадача №3 эпика №1", "Делать подзадачу №3",
                Duration.ofMinutes(10), LocalDateTime.now().plus(Duration.ofMinutes(30)), 2);

        manager.addSubtask(subtaskOne);
        manager.addSubtask(subtaskTwo);

        Assertions.assertNotEquals(subtaskOne.getId(), subtaskTwo.getId(), "Subtask ids should be unique");
    }

    @Test
    void shouldUpdateSubtask() throws TaskNotFoundException {
        Subtask expectedSubtask = new Subtask("Подзадача №1", "Выполнять подзадачу", 3,
                Status.NEW, Duration.ofMinutes(10), LocalDateTime.now(), epic.getId());
        Subtask updatedSubtask = new Subtask("Подзадача №1", "Выполнять новую  подзадачу", 3,
                Status.NEW, Duration.ofMinutes(10), LocalDateTime.now().plus(Duration.ofMinutes(10)), epic.getId());
        manager.updateSubtask(updatedSubtask);
        Subtask actualSubtask = manager.getSubtaskById(updatedSubtask.getId());

        Assertions.assertEquals(expectedSubtask, actualSubtask, "Они не равны");
    }

    @Test
    void shouldGetSubtasksOfEpic() throws TaskNotFoundException {
        manager.addSubtask(new Subtask("Подзадача №2 эпика №1", "Делать подзадачу №2",
                Duration.ofMinutes(10), LocalDateTime.now().plus(Duration.ofMinutes(10)), 2));
        manager.addSubtask(new Subtask("Подзадача №3 эпика №1", "Делать подзадачу №3",
                Duration.ofMinutes(10), LocalDateTime.now().plus(Duration.ofMinutes(30)), 2));

        final List<Subtask> subtasks = manager.getSubtaskByEpic(epic.getId());

        Assertions.assertNotNull(subtasks, "Список не должен бытть пуст");
        Assertions.assertEquals(3, subtasks.size());
    }

    @Test
    void shouldGetSubtaskById() throws TaskNotFoundException {
        Subtask expectedSubtask = new Subtask("Подзадача №2 эпика №1", "Делать подзадачу №2",
                subtask.getId(), Status.NEW, 2);

        Subtask savedSubtask = manager.getSubtaskById(subtask.getId());
        Subtask actualSubtask = manager.getSubtaskById(savedSubtask.getId());

        Assertions.assertEquals(expectedSubtask, actualSubtask);
    }

    @Test
    void shouldGetAllSubtasks() {
        manager.addSubtask(new Subtask("Подзадача №2 эпика №1", "Делать подзадачу №2",
                Duration.ofMinutes(10), LocalDateTime.now().plus(Duration.ofMinutes(10)), 2));
        manager.addSubtask(new Subtask("Подзадача №3 эпика №1", "Делать подзадачу №3",
                Duration.ofMinutes(10), LocalDateTime.now().plus(Duration.ofMinutes(30)), 2));

        final List<Subtask> subtasks = manager.getSubtaskMap();

        Assertions.assertNotNull(subtasks, "Список не должен быть пуст");
        Assertions.assertEquals(3, subtasks.size());
    }

    @Test
    void shouldDeleteSubtaskById() throws TaskNotFoundException {
        Subtask subtaskToDelete = new Subtask("Подзадача №1 эпика №1", "Делать подзадачу №1", 3,
                Status.NEW, 2);
        manager.deleteSubtaskById(subtaskToDelete.getId());
        assertFalse(manager.getSubtaskMap().contains(subtaskToDelete));
    }

    @Test
    void shouldClearAllSubtasks() {
        manager.addSubtask(new Subtask("Подзадача №2 эпика №1", "Делать подзадачу №2",
                Duration.ofMinutes(10), LocalDateTime.now().plus(Duration.ofMinutes(10)), 2));
        manager.addSubtask(new Subtask("Подзадача №3 эпика №1", "Делать подзадачу №3",
                Duration.ofMinutes(10), LocalDateTime.now().plus(Duration.ofMinutes(30)), 2));

        manager.clearSubtask();
        final List<Subtask> subtasks = manager.getSubtaskMap();

        Assertions.assertNotNull(subtasks, "Список должен вовращать null");
        Assertions.assertTrue(subtasks.isEmpty(), "Список должен быть пуст");
    }
}


