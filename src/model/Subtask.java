package model;

import java.time.Duration;
import java.time.LocalDateTime;

public class Subtask extends Task {
    private final Integer epicId;

    public Subtask(String name, String description, Integer epicId) {
        super(name, description);
        this.epicId = epicId;
    }

    public Subtask(String name, String description, Integer id, Status status, Integer epicId) {
        super(name, description, id, status);
        this.epicId = epicId;
    }

    public Subtask(String name, String description, Duration duration, LocalDateTime startTime, Integer epicId) {
        super(name, description, duration, startTime);
        this.epicId = epicId;
    }

    public Subtask(String name, String description, Integer id, Status status, Duration duration,
                   LocalDateTime startTime, Integer epicId) {
        super(name, description, id, status, duration, startTime);
        this.epicId = epicId;
    }

    public int getEpicId() {
        return epicId;
    }

    @Override
    public Type getType() {
        return Type.SUBTASK;
    }

    @Override
    public String toString() {
        return "Subtask(" +
                "Название = " + getName() +
                ", Описание = " + getDescription() +
                ", id = " + getId() +
                ", Статус = " + getStatus() +
                ", Время выполнения = " + getDuration() +
                ", Время начала = " + getStartTime() +
                ", epicId = " + epicId + ")" + "\n";
    }
}
