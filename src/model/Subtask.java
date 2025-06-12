package model;

public class Subtask extends Task {
    private final Integer epicId;

    public Subtask(String name, String description, Integer epicId) {
        super(name, description);
        this.epicId = epicId;
        this.setType(Type.SUBTASK);
    }

    public Subtask(String name, String description, Integer id, Status status, Integer epicId) {
        super(name, description, id, status);
        this.epicId = epicId;
        this.setType(Type.SUBTASK);
    }

    public int getEpicId() {
        return epicId;
    }

    @Override
    public Type getType() {
        return super.getType();
    }

    @Override
    public void setType(Type type) {
        super.setType(type);
    }

    @Override
    public String toString() {
        return "\n Subtask (" +
                "Название: " + getName() +
                ", Описание: " + getDescription() +
                ", id=" + getId() +
                ", Статус: " + getStatus() +
                ", epicId=" + epicId + ")" + "\n";
    }
}
