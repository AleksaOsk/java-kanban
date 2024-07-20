package entities;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Epic extends Task {

    private final HashMap<Integer, Subtask> subtasks;

    public Epic(String name, String description) {
        super(name, description, null, Duration.ofMinutes(0));
        subtasks = new HashMap<>();
    }

    public Epic(Epic epic) {
        super(epic);
        this.subtasks = deepCopyHashMap(epic);
    }

    private HashMap<Integer, Subtask> deepCopyHashMap(Epic epic) {
        HashMap<Integer, Subtask> map = epic.subtasks.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> new Subtask(entry.getValue()), (a, b) -> b, HashMap::new));

        return map;
    }

    private List<Subtask> deepCopyArrayList() {
        List<Subtask> arrayList = new ArrayList<>();

        for (Subtask subtask : subtasks.values()) {
            arrayList.add(new Subtask(subtask));
        }

        return arrayList;
    }

    public void addSubtask(Subtask newSubtask) {
        subtasks.putIfAbsent(newSubtask.getId(), newSubtask);
    }

    public void updateSubtask(Subtask newSubtask) {
        if (subtasks.containsKey(newSubtask.getId())) {
            subtasks.put(newSubtask.getId(), newSubtask);
        }
    }

    public List<Subtask> getSubtasks() {
        return deepCopyArrayList();
    }

    public void removeSubtask(Integer id) {
        subtasks.remove(id);
    }

    public void removeAllSubtasks() {
        subtasks.clear();
    }

    @Override
    public void setStatus(Status status) {
    }

    public void updateStatus() {
        // Если список подзадач пуст, то ставим статус NEW и ничего не проверяем
        if (subtasks.isEmpty()) {
            setStatus(Status.NEW);
            return;
        }

        int countDone = 0;
        int countNew = 0;

        for (Subtask subtask : subtasks.values()) {
            if (subtask.getStatus() == Status.NEW) {
                countNew++;
            } else if (subtask.getStatus() == Status.DONE) {
                countDone++;
            }
        }

        if (countDone == subtasks.size()) {
            setStatus(Status.DONE);
        } else if (countNew == subtasks.size()) {
            setStatus(Status.NEW);
        } else {
            setStatus(Status.IN_PROGRESS);
        }
    }

    @Override
    public LocalDateTime getStartTime() {
        LocalDateTime startTime = LocalDateTime.now().plusYears(1000);
        if (!subtasks.isEmpty()) {
            for (Subtask subtask : subtasks.values()) {
                if (subtask.getStartTime().isBefore(startTime)) {
                    startTime = subtask.getStartTime();
                }
            }
            return startTime;
        }
        return null;
    }

    @Override
    public LocalDateTime getEndTime() {
        LocalDateTime endTime = LocalDateTime.now().minusYears(1000);
        if (!subtasks.isEmpty()) {
            for (Subtask subtask : getSubtasks()) {
                if (subtask.getEndTime().isAfter(endTime)) {
                    endTime = subtask.getEndTime();
                }
            }
            return endTime;
        }
        return null;
    }

    @Override
    public Duration getDuration() {
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start;
        if (!subtasks.isEmpty()) {
            for (Subtask subtask : subtasks.values()) {
                end = end.plus(subtask.getDuration());
            }
            return Duration.between(start, end);
        }
        return Duration.ZERO;
    }

    @Override
    public String toString() {
        return "Epic{" + "id=" + getId() +
                ", name='" + getName() + '\'' +
                ", description='" + getStatus() + '\'' +
                ", status=" + getStatus() +
                ", startTime=" + getStartTime() +
                ", endTime=" + getEndTime() +
                ", duration=" + getDuration() +
                ", subtasks=" + subtasks + "}";
    }
}