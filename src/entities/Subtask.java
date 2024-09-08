package entities;

import java.time.Duration;
import java.time.LocalDateTime;

public class Subtask extends Task {

    private final Integer epicId;

    public Subtask(String name, String description, Integer epicId, LocalDateTime startTime, Duration durationMinutes) {
        super(name, description, startTime, durationMinutes);
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
               "id=" + getId() +
               ", name='" + getName() + '\'' +
               ", description='" + getDescription() + '\'' +
               ", status=" + getStatus() +
               ", startTime=" + getStartTime() +
               ", endTime=" + getEndTime() +
               ", duration=" + getDuration() +
               ", epicId=" + epicId +
               "}\n";
    }
}