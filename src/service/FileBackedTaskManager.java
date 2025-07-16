package service;

import exception.ManagerLoadException;
import exception.ManagerSaveException;
import model.*;

import java.io.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class FileBackedTaskManager extends InMemoryTaskManager {

    private final File file;
    private static final String CSV_FILE = "id,type,name,status,description,duration,startTime,epicId";

    static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    public FileBackedTaskManager(HistoryManager historyManager, File file) {
        super(historyManager);
        this.file = file;
    }

    private void save() {
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file))) {
            bufferedWriter.write(CSV_FILE);
            bufferedWriter.newLine();
            for (Task task : getTaskMap()) {
                bufferedWriter.write(toString(task) + "\n");
            }
            for (Epic epic : getEpicMap()) {
                bufferedWriter.write(toString(epic) + "\n");
            }
            for (Subtask subtask : getSubtaskMap()) {
                bufferedWriter.write(toString(subtask) + "\n");
            }
            saveHistory(bufferedWriter);
        } catch (IOException exception) {
            throw new ManagerSaveException("Файл не сохранен", exception);
        }
    }

    private String toString(Task task) {
        String epicId = task instanceof Subtask ? String.valueOf(((Subtask) task).getEpicId()) : "";
        return String.format("%s,%s,%s,%s,%s,%s,%s,%s,",
                task.getId(),
                task.getType(),
                task.getName(),
                task.getDescription(),
                task.getStatus(),
                task.getDuration() != null ? String.valueOf(task.getDuration().toMinutes()) : "null",
                task.getStartTime() != null ? task.getStartTime().format(formatter) : "null",
                epicId);
    }

    public static FileBackedTaskManager loadFromFile(File file) {

            FileBackedTaskManager taskManager = new FileBackedTaskManager(new InMemoryHistoryManager(), file);

            try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
                //bufferedReader.readLine(); // Пропускаем первую строку

                String line;
                int currentMaxId = 0;

                while ((line = bufferedReader.readLine()) != null) {
                    if (line.equals(CSV_FILE.trim())) continue; // Пропускаем заголовок файла

                    if (line.startsWith("Список id истории просмотров:")) {
                        String[] identifier = line.substring("Список id истории просмотров:".length()).split(",");
                        loadHistory(taskManager, identifier);
                    } else {
                        Task task = fromLoadString(line);
                        Type type = task.getType();
                        if (task.getId() > currentMaxId) {
                            currentMaxId = task.getId();
                        }
                        switch (type) {
                            case EPIC -> taskManager.epicMap.put(task.getId(), (Epic) task);
                            case SUBTASK -> {
                                taskManager.putSubtask((Subtask) task);
                                taskManager.addTaskPriorityList(task); // Добавление задачи в приоритетный список для subtask
                            }
                            case TASK -> {
                                taskManager.taskMap.put(task.getId(), task);
                                taskManager.addTaskPriorityList(task); // Добавление задачи в приоритетный список для task
                            }
                            default -> throw new IllegalArgumentException("Тип задачи не определён: " + type);
                        }
                    }
                }

                taskManager.count = currentMaxId; // убрал плюс, он все равно должен стать по текущему
                return taskManager;
            } catch (IOException exception) {
                throw new ManagerLoadException("Ошибка загрузки файлов " + exception.getMessage());
            }
        }

    private void putSubtask(Subtask subtask) {
        subtaskMap.put(subtask.getId(), subtask);
        Epic relatedEpic = epicMap.get(subtask.getEpicId());
        if (relatedEpic != null) {
            relatedEpic.addSubtaskList(subtask);
        }
    }

    private static Task fromLoadString(String line) {
        String[] stream = line.split(",");
        int id;
        int epicId = -1;
        String name;
        String description;
        Status status;
        Type type;
        Duration duration;
        LocalDateTime startTime;

        try {
            id = Integer.parseInt(stream[0]);
            type = Type.valueOf(stream[1]);
            name = stream[2];
            description = stream[3];
            status = Status.valueOf(stream[4]);
            duration = stream[5].equals("null") ? null : Duration.ofMinutes(Long.parseLong(stream[5]));
            startTime = stream[6].equals("null") ? null : LocalDateTime.parse(stream[6], formatter);

            if (type.equals(Type.SUBTASK)) {
                epicId = Integer.parseInt(stream[7]);
            }
        } catch (IndexOutOfBoundsException | IllegalArgumentException exception) {
            throw new IllegalArgumentException("Ошибка загружаемого потока данных:", exception);
        }

        switch (type) {
            case TASK -> {
                return new Task(name, description, id, status, duration, startTime);
            }
            case EPIC -> {
                return new Epic(name, description, id, status, duration, startTime);
            }
            case SUBTASK -> {
                return new Subtask(name, description, id, status, duration, startTime, epicId);
            }
            default -> throw new IllegalArgumentException("Тип задачи не определен: " + type);
        }
    }

    private void saveHistory(BufferedWriter bufferedWriter) {
        try {
            List<Task> history = getHistory();
            bufferedWriter.write("Список id истории просмотров:");
            for (Task task : history) {
                bufferedWriter.write(task.getId() + ",");
            }
        } catch (IOException exception) {
            throw new ManagerSaveException("История не сохранена", exception);
        }
    }

    @Override
    public Task getTaskById(Integer id) {
        Task task = super.getTaskById(id);
        save();
        return task;
    }

    @Override
    public Epic getEpicById(Integer id) {
        Epic epic = super.getEpicById(id);
        save();
        return epic;
    }

    @Override
    public Subtask getSubtaskById(Integer id) {
        Subtask subtask = super.getSubtaskById(id);
        save();
        return subtask;
    }

    @Override
    public Task addTask(Task task) {
        super.addTask(task);
        save();
        return task;
    }

    @Override
    public Epic addEpic(Epic epic) {
        super.addEpic(epic);
        save();
        return epic;
    }

    @Override
    public Subtask addSubtask(Subtask subtask) {
        super.addSubtask(subtask);
        save();
        return subtask;
    }

    @Override
    public void clearTask() {
        super.clearTask();
        save();
    }

    @Override
    public void clearEpic() {
        super.clearEpic();
        save();
    }

    @Override
    public void clearSubtask() {
        super.clearSubtask();
        save();
    }

    @Override
    public Task updateTask(Task task) {
        super.updateTask(task);
        save();
        return task;
    }

    @Override
    public Epic updateEpic(Epic epic) {
        super.updateEpic(epic);
        save();
        return epic;
    }

    @Override
    public Subtask updateSubtask(Subtask subtask) {
        super.updateSubtask(subtask);
        save();
        return subtask;
    }

    @Override
    public void deleteTaskById(Integer id) {
        super.deleteTaskById(id);
        save();
    }

    @Override
    public void deleteEpicById(Integer id) {
        super.deleteEpicById(id);
        save();
    }

    @Override
    public void deleteSubtaskById(Integer id) {
        super.deleteSubtaskById(id);
        save();
    }

    @Override
    public void changeStatus(Integer id, Status status) {
        super.changeStatus(id, status);
    }

    public static void loadHistory(FileBackedTaskManager taskManager, String[] identifier) {
        for (String id : identifier) {
            if (!id.trim().isEmpty()) {
                int taskId = Integer.parseInt(id);
                if (taskManager.taskMap.containsKey(taskId)) {
                    taskManager.getTaskById(Integer.parseInt(id));
                }
                if (taskManager.epicMap.containsKey(taskId)) {
                    taskManager.getEpicById(Integer.parseInt(id));
                }
                if (taskManager.subtaskMap.containsKey(taskId)) {
                    taskManager.getSubtaskById(Integer.parseInt(id));
                }
            }
        }
    }
}
