package server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import handlers.BaseHttpHandler;
import model.Epic;
import model.Subtask;
import model.Task;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.*;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class HttpTaskServerTest {

    private final HistoryManager historyManager = Managers.getDefaultHistory();
    private FileBackedTaskManager manager;
    private HttpTaskServer taskServer;
    private Gson gson;


    @BeforeEach
    public void setUp() throws IOException {
        File tempFile = File.createTempFile("test", ".csv");
        manager = new FileBackedTaskManager(historyManager, tempFile);
        taskServer = new HttpTaskServer(manager);
        gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeSerializer())
                .registerTypeAdapter(Duration.class, new DurationSerializerAdapter())
                .create();

        // Очищаем все задачи, эпики и подзадачи перед каждым тестом
        manager.clearTask();
        manager.clearSubtask();
        manager.clearEpic();
        taskServer.start();
    }

    @AfterEach
    public void shutDown() {
        taskServer.stop();
    }

    @Test
    public void testAddTask() throws IOException, InterruptedException {
        // создаём задачу
        Task task = new Task("Task 1", "Description", Duration.ofMinutes(10), LocalDateTime.now());

        // конвертируем её в JSON
        String taskJson = gson.toJson(task);

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();

        // вызываем REST, отвечающий за создание задач
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // проверяем код ответа
        assertEquals(201, response.statusCode(), "Некорректный статус ответа");

        List<Task> tasksFromManager = manager.getTaskMap();

        assertNotNull(tasksFromManager, "Задачи не возвращаются");
        assertEquals(1, tasksFromManager.size(), "Некорректное количество задач");
        assertEquals("Task 1", tasksFromManager.get(0).getName(), "Некорректное имя задачи");
    }

    @Test
    public void testAddEpic() throws IOException, InterruptedException {
        // создаём эпик
        Epic epic = new Epic("Epic 1", "Epic Description", Duration.ofHours(2), LocalDateTime.now());

        // конвертируем его в JSON
        String epicJson = gson.toJson(epic);

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(epicJson))
                .build();

        // вызываем REST, отвечающий за создание эпиков
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // проверяем код ответа
        assertEquals(201, response.statusCode(), "Некорректный статус ответа");

        List<Epic> epicsFromManager = manager.getEpicMap();

        assertNotNull(epicsFromManager, "Эпики не возвращаются");
        assertEquals(1, epicsFromManager.size(), "Некорректное количество эпиков");
        assertEquals("Epic 1", epicsFromManager.get(0).getName(), "Некорректное имя эпика");
    }


    @Test
    public void testAddSubtask() throws IOException, InterruptedException {
        // Создаём эпик
        Epic epic = new Epic("Epic 1", "Epic Description", Duration.ofHours(2), LocalDateTime.now());
        String epicJson = gson.toJson(epic);

        // Отправляем запрос на создание эпика
        HttpClient client = HttpClient.newHttpClient();
        URI epicUrl = URI.create("http://localhost:8080/epics");
        HttpRequest epicRequest = HttpRequest.newBuilder()
                .uri(epicUrl)
                .POST(HttpRequest.BodyPublishers.ofString(epicJson))
                .build();

        HttpResponse<String> epicResponse = client.send(epicRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, epicResponse.statusCode(), "Некорректный статус ответа при создании эпика");

        // Создаём подзадачу
        Subtask subtask = new Subtask("Subtask 1", "Subtask Description",
                Duration.ofMinutes(5), LocalDateTime.now(), 1);
        String subtaskJson = gson.toJson(subtask);

        // Отправляем запрос на создание подзадачи
        URI subtaskUrl = URI.create("http://localhost:8080/subtasks");
        HttpRequest subtaskRequest = HttpRequest.newBuilder()
                .uri(subtaskUrl)
                .POST(HttpRequest.BodyPublishers.ofString(subtaskJson))
                .build();

        HttpResponse<String> subtaskResponse = client.send(subtaskRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, subtaskResponse.statusCode(), "Некорректный статус ответа при создании подзадачи");

        // Проверяем, что подзадачу можно получить из менеджера
        List<Subtask> subtasksFromManager = manager.getSubtaskMap();
        assertNotNull(subtasksFromManager, "Подзадачи не возвращаются");
        assertEquals(1, subtasksFromManager.size(), "Некорректное количество подзадач");
        assertEquals("Subtask 1", subtasksFromManager.get(0).getName(), "Некорректное имя подзадачи");
    }

    @Test
    public void testUpdateTask() throws IOException, InterruptedException {
        // Создаём исходную задачу
        Task task = new Task("Task 1", "Description", Duration.ofMinutes(10),
                LocalDateTime.of(2025, 7, 1, 0, 10));

        // Конвертируем её в JSON и добавляем задачу
        String taskJson = gson.toJson(task);
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode(), "Некорректный статус ответа при создании задачи");

        // Получаем добавленную задачу из менеджера
        List<Task> tasksFromManager = manager.getTaskMap();
        assertNotNull(tasksFromManager, "Задачи не возвращаются");
        assertEquals(1, tasksFromManager.size(), "Некорректное количество задач");
        Task addedTask = tasksFromManager.get(0);

        // Создаём новую задачу с обновлёнными данными
        Task updatedTask = new Task(
                "Updated Task", // Новое имя
                "Updated Description", // Новое описание
                Duration.ofMinutes(10), // Длительность
                LocalDateTime.of(2025, 7, 10, 12, 51, 0) // Новое время начала задачи без долей секунд
        );
        updatedTask.setId(addedTask.getId()); // Сохраняем идентификатор исходной задачи

        // Конвертируем обновлённую задачу в JSON и отправляем на обновление
        String updatedTaskJson = gson.toJson(updatedTask);
        url = URI.create("http://localhost:8080/tasks/" + updatedTask.getId());
        request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(updatedTaskJson)).build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Некорректный статус ответа при обновлении задачи");

        // Проверяем обновление задачи
        tasksFromManager = manager.getTaskMap();
        assertNotNull(tasksFromManager, "Задачи не возвращаются после обновления");
        assertEquals(1, tasksFromManager.size(), "Некорректное количество задач после обновления");
        Task retrievedTask = tasksFromManager.get(0);
        assertEquals("Updated Task", retrievedTask.getName(), "Некорректное имя задачи после обновления");
        assertEquals("Updated Description", retrievedTask.getDescription(), "Некорректное описание задачи после обновления");
    }

    @Test
    public void testUpdateEpic() throws IOException, InterruptedException {
        // Создаём исходный (Epic)
        Epic epic = new Epic("Epic 1", "Epic Description");

        // Конвертируем  в JSON и добавляем
        String epicJson = gson.toJson(epic);
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(epicJson)).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode(), "Некорректный статус ответа при создании эпика");

        // Получаем добавленную из менеджера
        List<Epic> epicsFromManager = manager.getEpicMap();
        assertNotNull(epicsFromManager, "Эпопеи не возвращаются");
        assertEquals(1, epicsFromManager.size(), "Некорректное количество эпиков");
        Epic addedEpic = epicsFromManager.get(0);

        // Создаём новую  с обновлёнными данными
        Epic updatedEpic = new Epic(
                "Updated Epic", // Новое имя
                "Updated Epic Description" // Новое описание
        );
        updatedEpic.setId(addedEpic.getId()); // Сохраняем идентификатор исходной

        // Конвертируем  в JSON и отправляем на обновление
        String updatedEpicJson = gson.toJson(updatedEpic);
        url = URI.create("http://localhost:8080/epics/" + updatedEpic.getId());
        request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(updatedEpicJson)).build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Некорректный статус ответа при обновлении эпика");

        // Проверяем обновление
        epicsFromManager = manager.getEpicMap();
        assertNotNull(epicsFromManager, "Эпики не возвращаются после обновления");
        assertEquals(1, epicsFromManager.size(), "Некорректное количество эпиков после обновления");
        Epic retrievedEpic = epicsFromManager.get(0);
        assertEquals("Updated Epic", retrievedEpic.getName(), "Некорректное имя эпика после обновления");
        assertEquals("Updated Epic Description", retrievedEpic.getDescription(),
                "Некорректное описание эпопеи после обновления");
    }

    @Test
    public void testUpdateSubtask() throws IOException, InterruptedException {
        // Создаём эпик
        Epic epic = new Epic("Epic 1", "Epic Description", Duration.ofHours(2), LocalDateTime.now());
        String epicJson = gson.toJson(epic);

        // Отправляем запрос на создание эпика
        HttpClient client = HttpClient.newHttpClient();
        URI epicUrl = URI.create("http://localhost:8080/epics");
        HttpRequest epicRequest = HttpRequest.newBuilder()
                .uri(epicUrl)
                .POST(HttpRequest.BodyPublishers.ofString(epicJson))
                .build();

        HttpResponse<String> epicResponse = client.send(epicRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, epicResponse.statusCode(), "Некорректный статус ответа при создании эпика");

        // Создаём подзадачу
        Subtask subtask = new Subtask("Subtask 1", "Subtask Description",
                Duration.ofMinutes(5), LocalDateTime.now(), 1);
        String subtaskJson = gson.toJson(subtask);

        // Отправляем запрос на создание подзадачи
        URI subtaskUrl = URI.create("http://localhost:8080/subtasks");
        HttpRequest subtaskRequest = HttpRequest.newBuilder()
                .uri(subtaskUrl)
                .POST(HttpRequest.BodyPublishers.ofString(subtaskJson))
                .build();

        HttpResponse<String> subtaskResponse = client.send(subtaskRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, subtaskResponse.statusCode(), "Некорректный статус ответа при создании подзадачи");

        // Проверяем, что подзадачу можно получить из менеджера
        List<Subtask> subtasksFromManager = manager.getSubtaskMap();
        assertNotNull(subtasksFromManager, "Подзадачи не возвращаются");
        assertEquals(1, subtasksFromManager.size(), "Некорректное количество подзадач");
        assertEquals("Subtask 1", subtasksFromManager.get(0).getName(), "Некорректное имя подзадачи");


        Subtask subtask1 = subtasksFromManager.get(0);
        // Создаём новую подзадачу с обновлёнными данными
        Subtask updatedSubtask = new Subtask(
                "Updated Subtask", // Новое имя
                "Updated Subtask Description", // Новое описание
                Duration.ofMinutes(15), // Длительность
                LocalDateTime.of(2025, 7, 10, 12, 51, 0), 1 // Новое время начала подзадачи без долей секунд
        );
        updatedSubtask.setId(subtask1.getId()); // Сохраняем идентификатор исходной подзадачи

        // Конвертируем обновлённую подзадачу в JSON и отправляем на обновление
        String updatedSubtaskJson = gson.toJson(updatedSubtask);
        URI url = URI.create("http://localhost:8080/subtasks/" + updatedSubtask.getId());
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(updatedSubtaskJson)).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Некорректный статус ответа при обновлении подзадачи");

        // Проверяем обновление подзадачи
        subtasksFromManager = manager.getSubtaskMap();
        assertNotNull(subtasksFromManager, "Подзадачи не возвращаются после обновления");
        assertEquals(1, subtasksFromManager.size(), "Некорректное количество подзадач после обновления");
        Subtask retrievedSubtask = subtasksFromManager.get(0);
        assertEquals("Updated Subtask", retrievedSubtask.getName(), "Некорректное имя подзадачи после обновления");
        assertEquals("Updated Subtask Description", retrievedSubtask.getDescription(), "Некорректное описание подзадачи после обновления");
    }

    @Test
    public void testDeleteTask() throws IOException, InterruptedException {
        // Создаём задачу
        Task task = new Task("Task 1", "Description", Duration.ofMinutes(10), LocalDateTime.now());

        // Конвертируем её в JSON
        String taskJson = gson.toJson(task);

        // Создаём HTTP-клиент и запрос для создания задачи
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();

        // Вызываем REST, отвечающий за создание задачи
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Проверяем код ответа
        assertEquals(201, response.statusCode(), "Некорректный статус ответа при создании задачи");

        // Получаем добавленную задачу из менеджера
        List<Task> tasksFromManager = manager.getTaskMap();
        Task addedTask = tasksFromManager.get(0);
        int taskId = addedTask.getId();
        BaseHttpHandler.IdRequest idRequest = new BaseHttpHandler.IdRequest();
        idRequest.setId(taskId);

        // Готовим запрос на удаление задачи
        url = URI.create("http://localhost:8080/tasks/id");
        HttpRequest request2 = HttpRequest.newBuilder().uri(url).
                method("DELETE", HttpRequest.BodyPublishers.ofString(gson.toJson(idRequest))).build();

        // Отправляем запрос на удаление
        response = client.send(request2, HttpResponse.BodyHandlers.ofString());

        // Проверяем код ответа на удаление
        assertEquals(200, response.statusCode(), "Некорректный статус ответа при удалении задачи");

        // Проверяем, что задача была удалена
        List<Task> tasksAfterDelete = manager.getTaskMap();
        assertEquals(0, tasksAfterDelete.size(), "Задача не была удалена");
    }

    @Test
    public void testDeleteEpic() throws IOException, InterruptedException {
        // Создаём эпик (epic)
        Epic epic = new Epic("Epic 1", "Description", Duration.ofMinutes(10), LocalDateTime.now());

        // Конвертируем её в JSON
        String epicJson = gson.toJson(epic);

        // Создаём HTTP-клиент и запрос для создания эпика
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics/");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(epicJson))
                .build();

        // Вызываем REST, отвечающий за создание эпика
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Проверяем код ответа
        assertEquals(201, response.statusCode(), "Некорректный статус ответа при создании эпопеи");

        // Получаем добавленную эпик из менеджера
        List<Epic> epicsFromManager = manager.getEpicMap();
        Epic addedEpic = epicsFromManager.get(0);
        int epicId = addedEpic.getId();
        BaseHttpHandler.IdRequest idRequest = new BaseHttpHandler.IdRequest();
        idRequest.setId(epicId);


        // Готовим запрос на удаление эпика
        url = URI.create("http://localhost:8080/epics/id");
        HttpRequest request2 = HttpRequest.newBuilder().uri(url).
                method("DELETE", HttpRequest.BodyPublishers.ofString(gson.toJson(idRequest))).build();


        // Отправляем запрос на удаление
        response = client.send(request2, HttpResponse.BodyHandlers.ofString());

        // Проверяем код ответа на удаление
        assertEquals(200, response.statusCode(), "Некорректный статус ответа при удалении эпопеи");

        // Проверяем, что эпопея была удалена
        List<Epic> epicsAfterDelete = manager.getEpicMap();
        assertEquals(0, epicsAfterDelete.size(), "Эпик не был удален");
    }

    @Test
    public void testDeleteSubtask() throws IOException, InterruptedException {
        // Создаём эпик
        Epic epic = new Epic("Epic 1", "Epic Description", Duration.ofHours(2), LocalDateTime.now());
        String epicJson = gson.toJson(epic);

        // Отправляем запрос на создание эпика
        HttpClient client = HttpClient.newHttpClient();
        URI epicUrl = URI.create("http://localhost:8080/epics");
        HttpRequest epicRequest = HttpRequest.newBuilder()
                .uri(epicUrl)
                .POST(HttpRequest.BodyPublishers.ofString(epicJson))
                .build();

        HttpResponse<String> epicResponse = client.send(epicRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, epicResponse.statusCode(), "Некорректный статус ответа при создании эпика");


        // Создаём подзадачу
        Subtask subtask = new Subtask("Subtask 1", "Description", Duration.ofMinutes(10), LocalDateTime.now(), 1);

        // Конвертируем её в JSON
        String subtaskJson = gson.toJson(subtask);

        URI url = URI.create("http://localhost:8080/subtasks/");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(subtaskJson))
                .build();

        // Вызываем REST, отвечающий за создание подзадачи
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Проверяем код ответа
        assertEquals(201, response.statusCode(), "Некорректный статус ответа при создании подзадачи");

        // Получаем добавленную подзадачу из менеджера
        List<Subtask> subtasksFromManager = manager.getSubtaskMap();
        Subtask addedSubtask = subtasksFromManager.get(0);
        int subtaskId = addedSubtask.getId();
        BaseHttpHandler.IdRequest idRequest = new BaseHttpHandler.IdRequest();
        idRequest.setId(subtaskId);

        // Готовим запрос на удаление подзадачи
        url = URI.create("http://localhost:8080/subtasks/id");
        HttpRequest request2 = HttpRequest.newBuilder().uri(url).
                method("DELETE", HttpRequest.BodyPublishers.ofString(gson.toJson(idRequest))).build();

        // Отправляем запрос на удаление
        response = client.send(request2, HttpResponse.BodyHandlers.ofString());

        // Проверяем код ответа на удаление
        assertEquals(200, response.statusCode(), "Некорректный статус ответа при удалении подзадачи");

        // Проверяем, что подзадача была удалена
        List<Subtask> subtasksAfterDelete = manager.getSubtaskMap();
        assertEquals(0, subtasksAfterDelete.size(), "Подзадача не была удалена");
    }

    @Test
    public void testDeleteAllTasks() throws IOException, InterruptedException {
        // Создаём задачу
        Task task = new Task("Task 1", "Description", Duration.ofMinutes(10), LocalDateTime.now());

        // Конвертируем её в JSON
        String taskJson = gson.toJson(task);

        // Создаём HTTP-клиент и запрос для создания задачи
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();

        // Вызываем REST, отвечающий за создание задачи
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Проверяем код ответа
        assertEquals(201, response.statusCode(), "Некорректный статус ответа при создании задачи");


        HttpRequest request2 = HttpRequest.newBuilder().uri(url).DELETE().build();

        // Отправляем запрос на удаление всех задач
        response = client.send(request2, HttpResponse.BodyHandlers.ofString());

        // Проверяем код ответа на удаление
        assertEquals(200, response.statusCode(), "Некорректный статус ответа при удалении задач");

        // Проверяем, что все задачи были удалены
        List<Task> tasksAfterDelete = manager.getTaskMap();
        assertEquals(0, tasksAfterDelete.size(), "Задачи не были удалены");
    }

    @Test
    public void testDeleteAllSubtasks() throws IOException, InterruptedException {
        // Создаём эпик
        Epic epic = new Epic("Epic 1", "Epic Description", Duration.ofHours(2), LocalDateTime.now());
        String epicJson = gson.toJson(epic);

        // Отправляем запрос на создание эпика
        HttpClient client = HttpClient.newHttpClient();
        URI epicUrl = URI.create("http://localhost:8080/epics");
        HttpRequest epicRequest = HttpRequest.newBuilder()
                .uri(epicUrl)
                .POST(HttpRequest.BodyPublishers.ofString(epicJson))
                .build();

        HttpResponse<String> epicResponse = client.send(epicRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, epicResponse.statusCode(), "Некорректный статус ответа при создании эпика");

        // Теперь попробуем удалить все подзадачи
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();

        // Отправляем запрос на удаление всех подзадач
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Проверяем код ответа на удаление
        assertEquals(200, response.statusCode(), "Некорректный статус ответа при удалении подзадач");

        // Проверяем, что все подзадачи были удалены
        List<Subtask> subtasksAfterDelete = manager.getSubtaskMap();
        assertEquals(0, subtasksAfterDelete.size(), "Подзадачи не были удалены");
    }

    @Test
    public void testDeleteAllEpics() throws IOException, InterruptedException {
        // Создаём эпик
        Epic epic = new Epic("Epic 1", "Epic Description", Duration.ofHours(2), LocalDateTime.now());

        // Конвертируем его в JSON
        String epicJson = gson.toJson(epic);

        // Создаём HTTP-клиент и запрос для создания эпика
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics/");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(epicJson))
                .build();

        // Вызываем REST, отвечающий за создание эпика
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Проверяем код ответа
        assertEquals(201, response.statusCode(), "Некорректный статус ответа при создании эпика");

        // Готовим запрос на удаление всех эпиков
        url = URI.create("http://localhost:8080/epics");
        HttpRequest request2 = HttpRequest.newBuilder().uri(url).DELETE().build();

        // Отправляем запрос на удаление всех эпиков
        response = client.send(request2, HttpResponse.BodyHandlers.ofString());

        // Проверяем код ответа на удаление
        assertEquals(200, response.statusCode(), "Некорректный статус ответа при удалении эпиков");

        // Проверяем, что все эпики были удалены
        List<Epic> epicsAfterDelete = manager.getEpicMap();
        assertEquals(0, epicsAfterDelete.size(), "Эпики не были удалены");
    }
}

