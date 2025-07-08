package service;

import exception.ManagerLoadException;
import exception.ManagerSaveException;
import exception.TaskNotFoundException;
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

    /*public FileBackedTaskManager(File file) {
        this.file = file;
    }*/

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
                        for (String id : identifier) {
                            if (!id.trim().isEmpty()) {
                                taskManager.getTaskById(Integer.parseInt(id));
                                taskManager.getEpicById(Integer.parseInt(id));
                                taskManager.getSubtaskById(Integer.parseInt(id));
                            }
                        }
                    } else {
                        Task task = fromLoadString(line);
                        Type type = task.getType();
                        if (task.getId() > currentMaxId) {
                            currentMaxId = task.getId();
                        }
                        switch (type) {
                            case EPIC -> taskManager.epicMap.put(task.getId(), (Epic) task);
                            case SUBTASK -> taskManager.putSubtask((Subtask) task);
                            case TASK -> taskManager.taskMap.put(task.getId(), task);
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



       /* FileBackedTaskManager taskManager = new FileBackedTaskManager(new InMemoryHistoryManager(),file);

        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
            bufferedReader.readLine();

            String line;
            int currentMaxId = 1;

            while ((line = bufferedReader.readLine()) != null) {

                if (line.equals((CSV_FILE.trim()))) continue;

                if (line.startsWith("Список id истории просмотров:")) {
                    String[] identifier = line.substring("Список id истории просмотров:".length()).split(",");
                    for (String id : identifier) {
                        if (!id.trim().isEmpty()) {
                            taskManager.getTaskById(Integer.parseInt(id));
                            taskManager.getEpicById(Integer.parseInt(id));
                            taskManager.getSubtaskById(Integer.parseInt(id));
                        }
                    }
                    continue;
                }
            }

            while ((line = bufferedReader.readLine()) != null) {
                Task task = fromLoadString(line);
                Type type = task.getType();
                if (task.getId() > currentMaxId) {
                    currentMaxId = task.getId();
                }
                switch (type) {
                    case EPIC -> taskManager.epicMap.put(task.getId(), (Epic) task);
                    case SUBTASK -> taskManager.putSubtask((Subtask) task);
                    case TASK -> taskManager.taskMap.put(task.getId(), task);
                    default -> throw new IllegalArgumentException("Тип задачи не определен: " + type);
                }
            }
            taskManager.count = currentMaxId;
            return taskManager;
        } catch (IOException exception) {
            throw new ManagerLoadException("Ошибка загрузки файлов " + exception.getMessage());
        }*/

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

    public static void main(String[] args) {
        System.out.println("----------------");

        File file = new File("src/data/data.csv");
        TaskManager taskManager;

        if (file.exists()) {
            taskManager = loadFromFile(file);
            System.out.println("Загрука данных в " + file.getName());
        } else {
            taskManager = Managers.getFileBackedTaskManager(file);
            System.out.println(
                    "Новый таск менеджер создан. File " + file.getName() +
                            " будет использоваться для сохранения данных.");
        }

        taskManager.addTask(new Task("Задача 1", "Выполнить 1 задачу"));
        taskManager.addTask(new Task("Задача 2", "Выполнить 2 задачу",
                Duration.ofMinutes(10), LocalDateTime.of(2025, 7, 7, 0, 10)));

        Epic epicOne = taskManager.addEpic(new Epic("Epic 1", "Первый эпик"));
        taskManager.addEpic(new Epic("Epic 2", "Второй эпик"));

        taskManager.addSubtask(new Subtask("Подзадача 1 для Epic 1", "Выполнить подзадачу 1",
                Duration.ofMinutes(10), LocalDateTime.of(2025, 7, 6, 0, 10), epicOne.getId()));
        taskManager.addSubtask(new Subtask("Подзадача 2 для Epic 1", "Выполнить подзадачу 2",
                Duration.ofMinutes(10), LocalDateTime.of(2025, 8, 6, 0, 10), epicOne.getId()));
        taskManager.addSubtask(new Subtask("Подзадача 3 для Epic 1", "Выполнить подзадачу 3",
                Duration.ofMinutes(10), LocalDateTime.of(2025, 9, 6, 0, 10), epicOne.getId()));

        try {
            printAllTasks(taskManager);
        } catch (TaskNotFoundException e) {
            throw new RuntimeException(e);
        }
        taskManager.changeStatus(1, Status.DONE);
        taskManager.changeStatus(2, Status.IN_PROGRESS);
        taskManager.changeStatus(4, Status.DONE);
        taskManager.changeStatus(5, Status.DONE);
        taskManager.changeStatus(7, Status.IN_PROGRESS);
        System.out.println("-".repeat(10));
        try {
            printAllTasks(taskManager);
        } catch (TaskNotFoundException e) {
            throw new RuntimeException(e);
        }

        try {
            PrintWriter writer = new PrintWriter(file);
            writer.print("");
            writer.flush();
            writer.close();

        } catch (Exception e) {
            throw new RuntimeException();
        }
    }

    public static void printAllTasks(TaskManager manager) throws TaskNotFoundException {
        System.out.println("Tasks:");
        for (Task task : manager.getTaskMap()) {
            System.out.println(task);
        }
        System.out.println("Epics:");
        for (Task epic : manager.getEpicMap()) {
            System.out.println(epic);

            for (Task task : manager.getSubtaskByEpic(epic.getId())) {
                System.out.println("--> " + task);
            }
        }
        System.out.println("Subtasks:");
        for (Task subtask : manager.getSubtaskMap()) {
            System.out.println(subtask);
        }
    }
}
