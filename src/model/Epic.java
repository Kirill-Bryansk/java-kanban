package model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class Epic extends Task {

    private LocalDateTime endTime;

    private ArrayList<Subtask> subtaskList = new ArrayList<>();

    public Epic(String name, String description) {
        super(name, description);
    }

    public Epic(String name, String description, Integer id, Status status) {
        super(name, description, id, status);
    }

    public Epic(String name, String description, Integer id, Status status, ArrayList<Subtask> subtaskList) {
        super(name, description, id, status);
        this.subtaskList = subtaskList;
    }

    public Epic(String name, String description, Duration duration, LocalDateTime startTime) {
        super(name, description, duration, startTime);
    }

    public Epic(String name, String description, int id, Status status, Duration duration, LocalDateTime startTime) {
        super(name, description, id, status, duration, startTime);
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

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    @Override
    public Type getType() {
        return Type.EPIC;
    }

    @Override
    public LocalDateTime getEndTime() {
        return super.getEndTime();
    }

    @Override
    public String toString() {
        return "Epic: (" +
                "Название = " + getName() +
                ", Описание = " + getDescription() +
                ", Id = " + getId() +
                ", Статус = " + getStatus() +
                ", Время выполнения = " + getDuration() +
                ", Время начала = " + getStartTime() + ")" + "\n" +
                " Subtask эпика = " + getName() + " : \n  " + subtaskList +
                "\n";
    }
}
