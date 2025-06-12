package service;

import model.Epic;
import model.Status;
import model.Subtask;
import model.Task;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class InMemoryTaskManager implements TaskManager {

    protected final HashMap<Integer, Task> taskMap = new HashMap<>();
    protected final HashMap<Integer, Epic> epicMap = new HashMap<>();
    protected final HashMap<Integer, Subtask> subtaskMap = new HashMap<>();

    private int count = 1;

    protected void setCount(int currentMaxId) {
        this.count = currentMaxId;
    }
    private final HistoryManager historyManager = Managers.getDefaultHistory();

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    @Override
    public int getCount() {
        return count++;
    }

    @Override
    public Task addTask(Task task) {
        task.setId(getCount());
        task.setStatus(Status.NEW);
        taskMap.put(task.getId(), task);
        return task;
    }

    @Override
    public Epic addEpic(Epic epic) {
        epic.setId(getCount());
        epic.setStatus(Status.NEW);
        epicMap.put(epic.getId(), epic);
        return epic;
    }

    @Override
    public Subtask addSubtask(Subtask subtask) {
        subtask.setId(getCount());
        subtask.setStatus(Status.NEW);
        subtaskMap.put(subtask.getId(), subtask);
        epicMap.get(subtask.getEpicId()).addSubtaskList(subtask);
        Epic epic = epicMap.get(subtask.getEpicId());
        updateEpicStatus(epic);
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
        for (Integer taskId : taskMap.keySet()) {
            historyManager.remove(taskId);
        }
        taskMap.clear();
    }

    @Override
    public void clearEpic() {
        for (Subtask subtask : subtaskMap.values()) {
            historyManager.remove(subtask.getId());
        }
        for (Integer epicId : epicMap.keySet()) {
            historyManager.remove(epicId);
        }
        taskMap.clear();
        subtaskMap.clear();
    }

    @Override
    public void clearSubtask() {
        for (Integer subtaskId : subtaskMap.keySet()) {
            historyManager.remove(subtaskId);
        }
        subtaskMap.clear();
        for (Epic epic : epicMap.values()) {
            epic.clearSubtaskList();
            epic.setStatus(Status.NEW);
        }
    }

    @Override
    public Task getTaskById(Integer id) {
        if (id > 0) {
            Task task = taskMap.get(id);
            if (task != null) {
                historyManager.add(task);
                return task;
            }
        }
        System.out.println("Task doesn't exist");
        return null;
    }

    @Override
    public Epic getEpicById(Integer id) {
        if (id > 0) {
            Epic epic = epicMap.get(id);
            if (epic != null) {
                historyManager.add(epic);
                return epic;
            }
        }
        System.out.println("Epic doesn't exist");
        return null;
    }

    @Override
    public Subtask getSubtaskById(Integer id) {
        if (id > 0) {
            Subtask subtask = subtaskMap.get(id);
            if (subtask != null) {
                historyManager.add(subtask);
                return subtask;
            }
        }
        System.out.println("Subtask doesn't exist");
        return null;
    }

    @Override
    public Task updateTask(Task task) {
        if (task != null) {
            Integer taskID = task.getId();
            if (taskID == null || !taskMap.containsKey(taskID)) {
                return null;
            }
            taskMap.replace(taskID, task);
            return task;
        }
        System.out.println("Task doesn't exist");
        return null;
    }

    @Override
    public Epic updateEpic(Epic epic) {
        if (epic != null) {
            Integer epicId = epic.getId();
            if (epicId == null || !epicMap.containsKey(epicId)) {
                return null;
            }
            Epic existentEpic = epicMap.get(epicId);
            ArrayList<Subtask> existentEpicSubtaskList = existentEpic.getSubtaskList();
            if (!existentEpicSubtaskList.isEmpty()) {
                for (Subtask subtask : existentEpicSubtaskList) {
                    subtaskMap.remove(subtask.getId());
                }
            }
            epicMap.replace(epicId, epic);
            ArrayList<Subtask> newEpicSubtaskList = epic.getSubtaskList();
            if (!newEpicSubtaskList.isEmpty()) {
                for (Subtask subtask : newEpicSubtaskList) {
                    subtaskMap.put(subtask.getId(), subtask);
                }
            }
            updateEpicStatus(epic);
            return epic;
        }
        System.out.println("Epic doesn't exist");
        return null;
    }

    @Override
    public Subtask updateSubtask(Subtask subtask) {
        if (subtask != null) {
            Integer subtaskId = subtask.getId();
            if (subtaskId == null || !subtaskMap.containsKey(subtaskId)) {
                return null;
            }
            Integer epicId = subtask.getEpicId();
            Subtask existentSubtask = subtaskMap.get(subtaskId);
            subtaskMap.replace(subtaskId, subtask);
            Epic epic = epicMap.get(epicId);
            ArrayList<Subtask> subtaskList = epic.getSubtaskList();
            subtaskList.remove(existentSubtask);
            subtaskList.add(subtask);
            epic.setSubtaskList(subtaskList);
            updateEpicStatus(epic);
            return subtask;
        }
        return null;
    }

    @Override
    public void deleteTaskById(Integer id) {
        if (id != null) {
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
            for (Subtask subtask : epicSubtasks) {
                historyManager.remove(subtask.getId());
                subtaskMap.remove(subtask.getId());
            }
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
            }
        }
    }

    @Override
    public ArrayList<Subtask> getSubtaskByEpic(Integer epicId) {
        Epic epic = getEpicById(epicId);
        return new ArrayList<>(epic.getSubtaskList());
    }

    @Override
    public void updateEpicStatus(Epic epic) {
        int countDoneStatus = 0;
        int countNewStatus = 0;
        Collection<Subtask> list = epic.getSubtaskList();
        for (Subtask subtask : list) {
            if (subtask.getStatus() == Status.DONE) {
                countDoneStatus++;
            }
            if (subtask.getStatus() == Status.NEW) {
                countNewStatus++;
            }
        }
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
        Collection<Integer> collectionTask = taskMap.keySet();
        for (Integer key : collectionTask) {
            if (key.equals(id)) {
                Task task = taskMap.get(key);
                task.setStatus(status);
                taskMap.put(key, task);
            }
        }
        Collection<Integer> collectionSubtask = subtaskMap.keySet();
        for (Integer key : collectionSubtask) {
            if (key.equals(id)) {
                Subtask subtask = subtaskMap.get(key);
                subtask.setStatus(status);
                taskMap.put(key, subtask);
            }
        }
    }
}
