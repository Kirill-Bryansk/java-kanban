package service;

import model.Epic;
import model.Subtask;
import model.Task;
import org.junit.jupiter.api.AfterEach;
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
        FileBackedTaskManager managerEmpty = createTaskManager();
        FileBackedTaskManager loadedManager = Managers.getFileBackedTaskManager(tempFile);
        assertTrue(loadedManager.getTaskMap()
                .isEmpty(), "loadedManager должени быть пуст");
        assertTrue(loadedManager.getEpicMap()
                .isEmpty(), "loadedManager должени быть пуст");
        assertTrue(loadedManager.getSubtaskMap()
                .isEmpty(), "loadedManager должени быть пуст");
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
        List<Subtask> loadedSubTasks = loadedManager.getSubtaskMap();

        assertEquals(2, loadedTasks.size(), "Должно быть 2 задачи");
        assertEquals(1, loadedEpics.size(), "Должен быть один эпик");
        assertEquals(1, loadedSubTasks.size(), "Должна быть одна подзадача");

        assertTrue(loadedTasks.contains(taskOne), "Задача 1 должна быть загружена");
        assertTrue(loadedTasks.contains(taskTwo), "Задача 2 должна быть загружена");
        assertTrue(loadedEpics.contains(epicOne), "Эпик 1 должн быть загружена");
        assertTrue(loadedSubTasks.contains(subtaskOne), "Подзадача 1 должна быть загружена");
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