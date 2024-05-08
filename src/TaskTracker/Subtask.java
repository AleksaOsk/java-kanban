package TaskTracker;

public class Subtask extends Task {

    private final Integer epicId;

    public Subtask(String name, String description, Integer epicId) {
        super(name, description);
        this.epicId = epicId;
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