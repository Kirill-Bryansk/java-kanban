package service;

import model.Epic;
import model.Subtask;
import model.Task;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static service.FileBackedTaskManager.loadFromFile;

class FileBackedTaskManagerTest {
    private File tempFile;

    protected FileBackedTaskManager createTaskManager() {
        try {
            tempFile = File.createTempFile("test", ".csv");
            if (tempFile == null) {
                throw new IllegalStateException("Временный файл не был создан.");
            }
            System.out.println("Task manager создан в файле " +
                    tempFile.getAbsolutePath());
        } catch (IOException e) {
            throw new IllegalStateException("Ошибка создания временного файла", e);
        }
        return Managers.getFileBackedTaskManager(tempFile);
    }

    @AfterEach
    void tearDown() {
        if (tempFile != null && tempFile.exists()) {
            tempFile.delete();
        }
    }

    @Test
    void saveAndLoadEmptyFile() {
        FileBackedTaskManager loadedManager = Managers.getFileBackedTaskManager(tempFile);
        assertTrue(loadedManager.getTaskMap()
                .isEmpty(), "loadedManager должен быть пуст");
        assertTrue(loadedManager.getEpicMap()
                .isEmpty(), "loadedManager должен быть пуст");
        assertTrue(loadedManager.getSubtaskMap()
                .isEmpty(), "loadedManager должен быть пуст");
    }

    @Test
    void shouldSaveAndLoadMultipleTasks() {
        FileBackedTaskManager manager = createTaskManager();
        Task taskOne = new Task("Задача 1", "Выполнить задачу 1");
        manager.addTask(taskOne);
        Task taskTwo = new Task("Задача 2", "Выполнить задачу 2");
        manager.addTask(taskTwo);
        Epic epicOne = new Epic("Epic 1", "Выполнить эпик 1");
        manager.addEpic(epicOne);
        Subtask subtaskOne = new Subtask("Подзадача 1", "Выполнить подзадачу 1", epicOne.getId());
        manager.addSubtask(subtaskOne);

        TaskManager loadedManager = loadFromFile(tempFile);
        List<Task> loadedTasks = loadedManager.getTaskMap();
        List<Epic> loadedEpics = loadedManager.getEpicMap();
        List<Subtask> loadedSubtasks = loadedManager.getSubtaskMap();

        assertEquals(2, loadedTasks.size(), "Должно быть 2 задачи");
        assertEquals(1, loadedEpics.size(), "Должен быть один эпик");
        assertEquals(1, loadedSubtasks.size(), "Должна быть одна подзадача");

        Task loadedTaskOne = loadedTasks.get(0);
        Task loadedTaskTwo = loadedTasks.get(1);
        Epic loadedEpicOne = loadedManager.getEpicById(3);
        Subtask loadedSubtaskOne = loadedManager.getSubtaskById(4);

        assertTrue(loadedTasks.contains(taskOne), "Задача 1 должна быть загружена");
        Assertions.assertEquals(taskOne.getType(), loadedTaskOne.getType());
        Assertions.assertEquals(taskOne.getName(), loadedTaskOne.getName());
        Assertions.assertEquals(taskOne.getDescription(), loadedTaskOne.getDescription());
        Assertions.assertEquals(taskOne.getStatus(), loadedTaskOne.getStatus());

        assertTrue(loadedTasks.contains(taskTwo), "Задача 2 должна быть загружена");
        Assertions.assertEquals(taskTwo.getType(), loadedTaskTwo.getType());
        Assertions.assertEquals(taskTwo.getName(), loadedTaskTwo.getName());
        Assertions.assertEquals(taskTwo.getDescription(), loadedTaskTwo.getDescription());
        Assertions.assertEquals(taskTwo.getStatus(), loadedTaskTwo.getStatus());

        assertTrue(loadedEpics.contains(epicOne), "Эпик 1 должн быть загружена");
        Assertions.assertEquals(epicOne.getType(), loadedEpicOne.getType());
        Assertions.assertEquals(epicOne.getName(), loadedEpicOne.getName());
        Assertions.assertEquals(epicOne.getDescription(), loadedEpicOne.getDescription());
        Assertions.assertEquals(epicOne.getStatus(), loadedEpicOne.getStatus());
        Assertions.assertEquals(epicOne.getSubtaskList(), loadedEpicOne.getSubtaskList());

        assertTrue(loadedSubtasks.contains(subtaskOne), "Подзадача 1 должна быть загружена");
        Assertions.assertEquals(subtaskOne.getType(), loadedSubtaskOne.getType());
        Assertions.assertEquals(subtaskOne.getName(), loadedSubtaskOne.getName());
        Assertions.assertEquals(subtaskOne.getDescription(), loadedSubtaskOne.getDescription());
        Assertions.assertEquals(subtaskOne.getStatus(), loadedSubtaskOne.getStatus());
        Assertions.assertEquals(subtaskOne.getEpicId(), loadedSubtaskOne.getEpicId());
    }

    @Test
    void shouldDeleteTasksCorrectly() {
        FileBackedTaskManager manager = createTaskManager();
        Task task = new Task("Задача 1", "Выполнить задачу 1");
        manager.addTask(task);

        manager.deleteTaskById(task.getId());

        TaskManager loadedManager = loadFromFile(tempFile);
        assertTrue(loadedManager.getTaskMap()
                .isEmpty(), "Все задачи должны быть удалены");
    }
}