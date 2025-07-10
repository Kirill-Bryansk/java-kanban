package service;

import exception.ManagerErrorSaveTaskTime;
import exception.TaskNotFoundException;
import model.Epic;
import model.Status;
import model.Subtask;
import model.Task;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

public class InMemoryTaskManager implements TaskManager {

    protected final HashMap<Integer, Task> taskMap = new HashMap<>();
    protected final HashMap<Integer, Epic> epicMap = new HashMap<>();
    protected final HashMap<Integer, Subtask> subtaskMap = new HashMap<>();

    protected int count = 0;

    private final HistoryManager historyManager;

    private final TimeComparator timeComparator = new TimeComparator();

    public TreeSet<Task> prioritizedTasks = new TreeSet<>(timeComparator);

    public InMemoryTaskManager(HistoryManager historyManager) {
        this.historyManager = historyManager;
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    @Override
    public int getCount() {
        count += 1;
        return count;
    }

    @Override
    public Task addTask(Task task) throws ManagerErrorSaveTaskTime {
        if (hasTimeOverlapWithPrioritizedTasks(task)) {
            throw new ManagerErrorSaveTaskTime("The task was not saved, change the start time");
        }
        task.setId(getCount());
        task.setStatus(Status.NEW);
        taskMap.put(task.getId(), task);
        prioritizedTasks.add(task);
        return task;

    }

    @Override
    public Epic addEpic(Epic epic) {
        try {
            epic.setId(getCount());
            epic.setStatus(Status.NEW);
            if (epic.getSubtaskList() == null) {
                epic.setSubtaskList(new ArrayList<>());
            }
            epicMap.put(epic.getId(), epic);
            return epic;
        } catch (Exception e) {
            System.err.println("Error saving the epic: " + e.getMessage());
            return null;
        }
    }

    @Override
    public Subtask addSubtask(Subtask subtask) throws ManagerErrorSaveTaskTime {
        if (hasTimeOverlapWithPrioritizedTasks(subtask)) {
            throw new ManagerErrorSaveTaskTime("The subtask was not saved, change the start time");
        }
        subtask.setId(getCount());
        subtask.setStatus(Status.NEW);
        subtaskMap.put(subtask.getId(), subtask);

        Epic epic = epicMap.get(subtask.getEpicId());
        if (epic == null) {
            subtaskMap.remove(subtask.getId(), subtask);
            count -= 1;
            throw new IllegalArgumentException("Epic not found for the given epicId. Please create the epic first.");
        }
        epic.addSubtaskList(subtask);
        updateEpicStatus(epic);
        calculateEpicDuration(epic.getId());
        prioritizedTasks.add(subtask);
        return subtask;
    }

    @Override
    public ArrayList<Task> getTaskMap() {
        return new ArrayList<>(taskMap.values());
    }

    @Override
    public ArrayList<Epic> getEpicMap() {
        return new ArrayList<>(epicMap.values());
    }

    @Override
    public ArrayList<Subtask> getSubtaskMap() {
        return new ArrayList<>(subtaskMap.values());
    }

    @Override
    public void clearTask() {
        taskMap.values().forEach(task -> {
            historyManager.remove(task.getId());
            prioritizedTasks.remove(task);
        });
        taskMap.clear();
    }

    @Override
    public void clearEpic() {
        subtaskMap.values().stream()
                .map(Subtask::getId)
                .forEach(historyManager::remove);

        epicMap.keySet().forEach(historyManager::remove);
        epicMap.clear();
        subtaskMap.clear();
    }

    @Override
    public void clearSubtask() {
        subtaskMap.keySet().forEach(key -> {
            historyManager.remove(key);
            prioritizedTasks.remove(subtaskMap.get(key));
        });
        subtaskMap.clear();

        epicMap.values().forEach(epic -> {
            epic.clearSubtaskList();
            epic.setStatus(Status.NEW);
            calculateEpicDuration(epic.getId());
        });
    }

    @Override
    public Task getTaskById(Integer id) {
        try {
            if (id <= 0) {
                throw new IllegalArgumentException("Id must be greater than 0");
            }
            Task task = taskMap.get(id);
            if (task != null) {
                historyManager.add(task);
                return task;
            } else {
                throw new TaskNotFoundException("Task with id " + id + " does not exist");
            }
        } catch (IllegalArgumentException | TaskNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Epic getEpicById(Integer id) {
        try {
            if (id <= 0) {
                throw new IllegalArgumentException("Id must be greater than 0");
            }
            Epic epic = epicMap.get(id);
            if (epic != null) {
                historyManager.add(epic);
                return epic;
            } else {
                throw new TaskNotFoundException("Epic with id " + id + " does not exist");
            }
        } catch (IllegalArgumentException | TaskNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Subtask getSubtaskById(Integer id) {
        try {
            if (id <= 0) {
                throw new IllegalArgumentException("Id must be greater than 0");
            }

            Subtask subtask = subtaskMap.get(id);
            if (subtask != null) {
                historyManager.add(subtask);
                return subtask;
            } else {
                throw new TaskNotFoundException("Subtask with id " + id + " does not exist");
            }
        } catch (IllegalArgumentException | TaskNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Task updateTask(Task task) throws ManagerErrorSaveTaskTime {
        if (hasTimeOverlapWithPrioritizedTasks(task)) {
            throw new ManagerErrorSaveTaskTime("The task was not updated, change the start time");
        }

        Integer taskId = task.getId();
        if (taskId == null || !taskMap.containsKey(taskId)) {
            System.out.println("Task doesn't exist");
            return null;
        }
        if (task.getStatus() == null) {
            task.setStatus(Status.NEW);
        }

        prioritizedTasks.removeIf(currentTask -> currentTask.getId().equals(taskId));
        prioritizedTasks.add(task);
        taskMap.replace(taskId, task);
        return task;
    }

    @Override
    public Epic updateEpic(Epic epic) {
        if (epic == null || epic.getId() == null || !epicMap.containsKey(epic.getId())) {
            System.out.println("Epic doesn't exist");
            return null;
        }

        Epic existentEpic = epicMap.get(epic.getId());

        existentEpic.getSubtaskList().forEach(subtask -> subtaskMap.remove(subtask.getId()));

        epicMap.replace(epic.getId(), epic);

        ArrayList<Subtask> newEpicSubtaskList = epic.getSubtaskList();
        newEpicSubtaskList.forEach(subtask -> subtaskMap.put(subtask.getId(), subtask));

        updateEpicStatus(epic);
        return epic;
    }

    @Override
    public Subtask updateSubtask(Subtask subtask) {
        if (subtask == null) {
            return null;
        }

        Integer subtaskId = subtask.getId();
        if (subtaskId == null || !subtaskMap.containsKey(subtaskId)) {
            throw new IllegalArgumentException("It is impossible to update: subtask with such ID does not exist");
        }
        if (hasTimeOverlapWithPrioritizedTasks(subtask)) {
            throw new ManagerErrorSaveTaskTime("The task was not updated, change the start time");
        }

        Epic epic = epicMap.get(subtask.getEpicId());
        ArrayList<Subtask> subtaskList = epic.getSubtaskList(); // sub прилетает не понятно откуда

        subtaskList.removeIf(currentSubtask -> currentSubtask.getId().equals(subtaskId));
        prioritizedTasks.remove(subtask);
        if (subtask.getStatus() == null) {
            subtask.setStatus(epic.getStatus());
        }
        subtaskMap.replace(subtaskId, subtask);
        subtaskList.add(subtask);
        epic.setSubtaskList(subtaskList);
        prioritizedTasks.add(subtask);
        updateEpicStatus(epic);
        calculateEpicDuration(epic.getId());
        return subtask;
    }

    @Override
    public void deleteTaskById(Integer id) {
        if (id != null) {
            prioritizedTasks.remove(taskMap.get(id));
            taskMap.remove(id);
            historyManager.remove(id);
        }
    }

    @Override
    public void deleteEpicById(Integer id) {
        if (id != null) {
            ArrayList<Subtask> epicSubtasks = epicMap.get(id).getSubtaskList();
            epicMap.remove(id);
            historyManager.remove(id);

            epicSubtasks.forEach(subtask -> {
                historyManager.remove(subtask.getId());
                subtaskMap.remove(subtask.getId());
            });
        }
    }

    @Override
    public void deleteSubtaskById(Integer id) {
        if (id != null) {
            Subtask subtask = subtaskMap.get(id);
            historyManager.remove(id);
            if (subtask == null) {
                System.out.println("Subtask doesn't exist");
            } else {
                Integer epicId = subtask.getEpicId();
                historyManager.remove(id);
                subtaskMap.remove(id);
                Epic epic = epicMap.get(epicId);
                ArrayList<Subtask> newSubtaskList = epic.getSubtaskList();
                newSubtaskList.remove(subtask);
                epic.setSubtaskList(newSubtaskList);
                updateEpicStatus(epic);
                calculateEpicDuration(epic.getId());
                prioritizedTasks.remove(subtask);
            }
        }
    }

    @Override
    public ArrayList<Subtask> getSubtaskByEpic(Integer epicId) throws TaskNotFoundException {
        Epic epic = getEpicById(epicId);
        return new ArrayList<>(epic.getSubtaskList());
    }

    @Override
    public void updateEpicStatus(Epic epic) {
        Collection<Subtask> list = epic.getSubtaskList();

        int countDoneStatus = (int) list.stream()
                .filter(subtask -> subtask.getStatus() == Status.DONE)
                .count();

        int countNewStatus = (int) list.stream()
                .filter(subtask -> subtask.getStatus() == Status.NEW)
                .count();

        if (countDoneStatus == list.size()) {
            epic.setStatus(Status.DONE);
        } else if (countNewStatus == list.size()) {
            epic.setStatus(Status.NEW);
        } else {
            epic.setStatus(Status.IN_PROGRESS);
        }
    }

    @Override
    public void changeStatus(Integer id, Status status) {
        taskMap.computeIfPresent(id, (key, task) -> {
            task.setStatus(status);
            return task;
        });

        subtaskMap.computeIfPresent(id, (key, subtask) -> {
            subtask.setStatus(status);
            return subtask;
        });
    }

    public void calculateEpicDuration(int id) {
        Epic epic = epicMap.get(id);

        Duration epicDuration = epic.getSubtaskList().stream()
                .filter(subtask -> subtask.getDuration() != null)
                .map(Subtask::getDuration)
                .reduce(Duration.ZERO, Duration::plus);

        LocalDateTime epicStartTime = epic.getSubtaskList().stream()
                .filter(subtask -> subtask.getStartTime() != null)
                .min(Comparator.comparing(Subtask::getStartTime))
                .map(Subtask::getStartTime)
                .orElse(null);

        LocalDateTime epicEndTime = epic.getSubtaskList().stream()
                .filter(subtask -> subtask.getEndTime() != null)
                .max(Comparator.comparing(Subtask::getEndTime))
                .map(Subtask::getEndTime)
                .orElse(null);

        epic.setDuration(epicDuration);
        epic.setStartTime(epicStartTime);
        epic.setEndTime(epicEndTime);
    }

    private boolean hasTimeConflict(Task firstTask, Task secondTask) {
        if (firstTask.getStartTime() == null || secondTask.getStartTime() == null) {
            return false;
        }
        LocalDateTime firstTaskStartTime = firstTask.getStartTime();
        LocalDateTime firstTaskEndTime = firstTask.getEndTime();
        LocalDateTime secondTaskStartTime = secondTask.getStartTime();
        LocalDateTime secondTaskEndTime = secondTask.getEndTime();

        return firstTaskStartTime.isBefore(secondTaskEndTime) && secondTaskStartTime.isBefore(firstTaskEndTime);
    }

    public boolean hasTimeOverlapWithPrioritizedTasks(Task task) {
        List<Task> prioritizedTasksList = getPrioritizedTasks();

        return prioritizedTasksList.stream()
                .anyMatch(isTask -> hasTimeConflict(isTask, task));
    }

    @Override
    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritizedTasks);
    }

    public void addTaskPriorityList(Task task) {
        prioritizedTasks.add(task);
    }
}
