package TaskTracker;

import java.util.ArrayList;
import java.util.HashMap;

public class Manager {

    private final HashMap<Integer, Task> tasks = new HashMap<>();
    private final HashMap<Integer, Epic> epics = new HashMap<>();
    private final HashMap<Integer, Subtask> subtasks = new HashMap<>();
    private Integer id = 1;

    private Integer getNewId() {
        return id++;
    }

    public ArrayList<Task> allTasks() {
        return new ArrayList<>(tasks.values());
    }

    public ArrayList<Epic> allEpics() {
        return new ArrayList<>(epics.values());
    }

    public ArrayList<Subtask> allSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    public void removeAllTasks() {
        tasks.clear();
    }

    public void removeAllEpics() {
        subtasks.clear();
        epics.clear();
    }

    public void removeAllSubtasks() {
        for (Epic epic : epics.values()) {
            epic.removeAllSubtasks();
            epic.updateStatus();
        }
        subtasks.clear();
    }

    public Task getTaskById(Integer id) {
        return tasks.get(id);
    }

    public Epic getEpicById(Integer id) {
        return epics.get(id);
    }

    public Subtask getSubtaskById(Integer id) {
        return subtasks.get(id);
    }

    public void newTask(Task newTask) {
        newTask.setId(getNewId());
        tasks.put(newTask.getId(), newTask);
    }

    public void newEpic(Epic newEpic) {
        newEpic.setId(getNewId());
        epics.put(newEpic.getId(), newEpic);
    }

    public void newSubtask(Subtask newSubtask) {
        if (epics.containsKey(newSubtask.getEpicId())) {
            newSubtask.setId(getNewId());
            subtasks.put(newSubtask.getId(), newSubtask);
            Epic epic = epics.get(newSubtask.getEpicId());
            epic.addSubtask(newSubtask);
            epic.updateStatus();
        }
    }

    public void updateTask(Task newTask) {
        tasks.put(newTask.getId(), newTask);
    }

    public void updateEpic(Epic newEpic) {
        Epic epic = epics.get(newEpic.getId());
        epic.setName(newEpic.getName());
        epic.setDescription(newEpic.getDescription());
    }

    public void updateSubtask(Subtask newSubtask) {
        subtasks.put(newSubtask.getId(), newSubtask);
        Epic epic = epics.get(newSubtask.getEpicId());
        epic.updateSubtask(newSubtask);
        epic.updateStatus();
    }

    public ArrayList<Subtask> allSubtasksOfEpic(Epic epic) {
        if (epics.containsKey(epic.getId())) {
            return epic.getSubtasks();
        } else {
            return new ArrayList<>();
        }
    }

    public void removeTaskById(Integer id) {
        tasks.remove(id);
    }


    public void removeEpicById(Integer id) {
        for (Subtask subtask : epics.get(id).getSubtasks()) {
            subtasks.remove(subtask.getId());
        }
        epics.remove(id);
    }

    public void removeSubtaskById(Integer id) {
        if (subtasks.containsKey(id)) {
            Epic epic = getEpicById(subtasks.get(id).getEpicId());
            epic.removeSubtask(id);
            epic.updateStatus();
            subtasks.remove(id);
        }
    }
}