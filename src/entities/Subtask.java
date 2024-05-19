package entities;

public class Subtask extends Task {

    private final Integer epicId;

    public Subtask(String name, String description, Integer epicId) {
        super(description, name);
        this.epicId = epicId;
    }

    public Subtask(Subtask subtask) {
        super(subtask);
        this.epicId = subtask.epicId;
    }

    public Integer getEpicId() {
        return epicId;
    }

    @Override
    public String toString() {
        return "Subtask{" +
                "epicId=" + epicId +
                ", name='" + getName() + '\'' +
                ", description='" + getStatus() + '\'' +
                ", id=" + getId() +
                ", status=" + getStatus() +
                '}';
    }
}