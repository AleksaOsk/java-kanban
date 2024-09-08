package service;

import entities.Epic;
import entities.Subtask;
import entities.Task;
import util.Managers;

import java.util.*;

public class InMemoryTaskManager implements TaskManager {

    protected static TreeSet<Task> treeSet = new TreeSet<>(Comparator.comparing(Task::getStartTime));
    protected final HashMap<Integer, Task> tasks = new HashMap<>();
    protected final HashMap<Integer, Epic> epics = new HashMap<>();
    protected final HashMap<Integer, Subtask> subtasks = new HashMap<>();
    protected final HistoryManager historyManager = Managers.getDefaultHistory();
    private Integer idCounter = 1;

    @Override
    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public List<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public List<Subtask> getAllSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public void removeAllTasks() {
        tasks.clear();
        prioritizedTasks();
    }

    @Override
    public void removeAllEpics() {
        subtasks.clear();
        epics.clear();
        prioritizedTasks();
    }

    @Override
    public void removeAllSubtasks() {
        epics.values().forEach(epic -> {
            epic.removeAllSubtasks();
            epic.updateStatus();
            prioritizedTasks();
        });
        subtasks.clear();
    }

    @Override
    public Task getTaskById(Integer id) {
        if (tasks.containsKey(id)) {
            Task task = tasks.get(id);
            historyManager.add(task);
            return new Task(task);
        }
        return null;
    }

    @Override
    public Epic getEpicById(Integer id) {
        if (epics.containsKey(id)) {
            Epic epic = epics.get(id);
            historyManager.add(epic);
            return new Epic(epic);
        }
        return null;
    }

    @Override
    public Subtask getSubtaskById(Integer id) {
        if (subtasks.containsKey(id)) {
            Subtask subtask = subtasks.get(id);
            historyManager.add(subtask);
            return new Subtask(subtask);
        }
        return null;
    }

    @Override
    public Task createTask(Task newTask) {
        //в случае пересечения возвращается null
        if (timeConflict(newTask)) {
            return null;
        }
        newTask.setId(idCounter++);
        tasks.put(newTask.getId(), new Task(newTask));
        prioritizedTasks();
        return newTask;
    }

    @Override
    public Epic createEpic(Epic newEpic) {
        newEpic.setId(idCounter++);
        epics.put(newEpic.getId(), new Epic(newEpic));
        prioritizedTasks();
        return newEpic;
    }

    @Override
    public Subtask createSubtask(Subtask newSubtask) {
        // сначала проверяем что такой epic есть, потом вроверяем пересечение
        if (epics.containsKey(newSubtask.getEpicId())) {
            if (timeConflict(newSubtask)) {
                return null;
            }
            newSubtask.setId(idCounter++);
            subtasks.put(newSubtask.getId(), new Subtask(newSubtask));
            Epic epic = epics.get(newSubtask.getEpicId());
            epic.addSubtask(subtasks.get(newSubtask.getId()));
            epic.updateStatus();
            prioritizedTasks();
            epic.getDuration();
            epic.getStartTime();
            epic.getEndTime();
            return newSubtask;
        }
        return null;
    }

    @Override
    public Task updateTask(Task newTask) {
        if (tasks.containsKey(newTask.getId())) {
            if (timeConflict(newTask)) {
                return null;
            }
            tasks.put(newTask.getId(), new Task(newTask));
            prioritizedTasks();
            return newTask;
        }
        return tasks.get(newTask.getId());
    }

    @Override
    public Epic updateEpic(Epic newEpic) {
        // обновить можно только имя и описание
        if (epics.containsKey(newEpic.getId())) {
            Epic epic = epics.get(newEpic.getId());
            epic.setName(newEpic.getName());
            epic.setDescription(newEpic.getDescription());
            prioritizedTasks();

            return new Epic(epic);
        }
        return null;
    }

    @Override
    public Subtask updateSubtask(Subtask newSubtask) {
        if (subtasks.containsKey(newSubtask.getId()) && epics.containsKey(newSubtask.getEpicId())) {
            if (timeConflict(newSubtask)) {
                return null;
            }
            subtasks.put(newSubtask.getId(), new Subtask(newSubtask));
            Epic epic = epics.get(newSubtask.getEpicId());
            epic.updateSubtask(subtasks.get(newSubtask.getId()));
            epic.updateStatus();
            prioritizedTasks();
            return newSubtask;
        }
        return subtasks.get(newSubtask.getId());
    }

    @Override
    public List<Subtask> getAllSubtasksOfEpic(Epic epic) {
        if (epics.containsKey(epic.getId())) {
            return epic.getSubtasks();
        }

        return new ArrayList<>();
    }

    @Override
    public void removeTaskById(Integer id) {
        tasks.remove(id);
        historyManager.remove(id);
        prioritizedTasks();
    }

    @Override
    public void removeEpicById(Integer id) {
        if (epics.containsKey(id)) {
            if (epics.get(id).getSubtasks() != null) {
                for (Subtask subtask : epics.get(id).getSubtasks()) {
                    Integer subtaskId = subtask.getId();
                    subtasks.remove(subtaskId);
                }
            }
            epics.remove(id);
            historyManager.remove(id);
            prioritizedTasks();
        }
    }

    @Override
    public void removeSubtaskById(Integer id) {
        if (subtasks.containsKey(id)) {
            Epic epic = getEpicById(subtasks.get(id).getEpicId());
            epic.removeSubtask(id);
            epic.updateStatus();
            subtasks.remove(id);
            historyManager.remove(id);
            prioritizedTasks();
        }
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    public static TreeSet<Task> getPrioritizedTasks() {
        return treeSet;
    }

    //тут TreeSet очищается и заполняется заново при добавлении, обновлении и удалении задач
    public void prioritizedTasks() {
        treeSet.clear();

        tasks.values().stream()
                .filter(taskSet -> taskSet.getDuration().toMinutes() > 0 && taskSet.getStartTime() != null)
                .forEach(taskSet -> treeSet.add(taskSet));

        subtasks.values().stream()
                .filter(subtaskSet -> subtaskSet.getDuration().toMinutes() > 0 && subtaskSet.getStartTime() != null)
                .forEach(subtaskSet -> treeSet.add(subtaskSet));
    }

    public boolean timeConflict(Task task1) {
        if (!treeSet.isEmpty()) {
            for (Task task2 : treeSet) {
                if (
                        task1.getStartTime().isEqual(task2.getStartTime())
                        || (task1.getStartTime().isAfter(task2.getStartTime()) && task1.getStartTime().isBefore(task2.getEndTime()))
                        || (task1.getEndTime().isAfter(task2.getStartTime()) && task1.getEndTime().isBefore(task2.getEndTime()))
                        || (task2.getStartTime().isAfter(task1.getStartTime()) && task2.getStartTime().isBefore(task1.getEndTime()))
                ) {
                    if (task1.getId() != null) {
                        return !task1.getId().equals(task2.getId());
                    }
                    return true;
                }
            }
        }
        return false;
    }
}