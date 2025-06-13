package model;

import java.util.ArrayList;

public class Epic extends Task {
    private ArrayList<Subtask> subtaskList = new ArrayList<>();

    public Epic(String name, String description) {
        super(name, description);
    }

    public Epic(String name, String description, Integer id) {
        super(name, description, id);
    }

    public Epic(String name, String description, Integer id, Status status) {
        super(name, description, id, status);
    }

    public Epic(String name, String description, Integer id, Status status, ArrayList<Subtask> subtaskList) {
        super(name, description, id, status);
        this.subtaskList = subtaskList;
    }

    public void addSubtaskList(Subtask subtask) {
        subtaskList.add(subtask);
    }

    public ArrayList<Subtask> getSubtaskList() {
        return subtaskList;
    }

    public void setSubtaskList(ArrayList<Subtask> subtaskList) {
        this.subtaskList = subtaskList;
    }

    public void clearSubtaskList() {
        subtaskList.clear();
    }

    @Override
    public Type getType() {
        return Type.EPIC;
    }

    @Override
    public String toString() {
        return "\nEpic: (" +
                "Название: " + getName() +
                ", Описание: " + getDescription() +
                ", Id = " + getId() +
                ", Статус: " + getStatus() + ") \n" +
                "Subtask эпика " + getName() + " : \n      " + subtaskList +
                "\n";
    }
}
