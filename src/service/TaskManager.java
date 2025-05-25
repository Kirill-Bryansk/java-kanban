package service;

import model.Epic;
import model.Status;
import model.Subtask;
import model.Task;

import java.util.ArrayList;
import java.util.List;

public interface TaskManager {

    int getCount();

    List<Task> getHistory();// ПоЧЕу то здесь должен быть гет хистори

    Task addTask(Task task);

    Epic addEpic(Epic epic);

    Subtask addSubtask(Subtask subtask);

    ArrayList<Task> getTaskMap();

    ArrayList<Epic> getEpicMap();

    ArrayList<Subtask> getSubtaskMap();

    void clearTask();

    void clearEpic();

    void clearSubtask();

    Task getTaskById(Integer id);

    Epic getEpicById(Integer id);

    Subtask getSubtaskById(Integer id);

    Task updateTask(Task task);

    Epic updateEpic(Epic epic);

    Subtask updateSubtask(Subtask subtask);

    void deleteTaskById(Integer id);

    void deleteEpicById(Integer id);

    void deleteSubtaskById(Integer id);

    ArrayList<Subtask> getSubtaskByEpic(Integer epicId);

    void updateEpicStatus(Epic epic);

    void changeStatus(Integer id, Status status);
}
