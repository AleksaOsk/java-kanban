package service;

import entities.*;
import exceptions.ManagerSaveException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public class FileBackedTaskManager extends InMemoryTaskManager {

    static final String line = "id,type,name,status,description,startTime,endTime,duration,epic";
    static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
    private final File file;

    public FileBackedTaskManager(File file) {
        this.file = file;
        save();
    }


    public static FileBackedTaskManager loadFromFile(File file) {
        try {
            String[] lines = Files.readString(file.toPath()).split("\n");
            FileBackedTaskManager fileBackedTaskManager = new FileBackedTaskManager(file);


            for (String taskString : lines) {
                if (taskString.equals(line) || taskString.isBlank()) {
                    continue;
                }
                Task task = fromString(taskString);
                Type typeOfTask = getType(task);

                switch (typeOfTask) {
                    case TASK -> fileBackedTaskManager.createTask(task);
                    case EPIC -> fileBackedTaskManager.createEpic((Epic) task);
                    case SUBTASK -> fileBackedTaskManager.createSubtask((Subtask) task);
                    default -> throw new IllegalStateException("Неверное значение: " + typeOfTask);
                }
            }
            return fileBackedTaskManager;
        } catch (IOException e) {
            throw new ManagerSaveException("Не удалось востановить данные " + "\n ошибка", e.getCause());
        }
    }

    private static Type getType(Task task) {
        if (task instanceof Epic) {
            return Type.EPIC;
        } else if (task instanceof Subtask) {
            return Type.SUBTASK;
        }
        return Type.TASK;
    }

    public static Task fromString(String value) {
        String[] splitValue = value.split(",");
        int id = Integer.parseInt(splitValue[0]);
        Type taskType = Type.valueOf(splitValue[1]);
        String name = splitValue[2];
        Status taskStatus = Status.valueOf(splitValue[3]);
        String description = splitValue[4];
        String startTime = splitValue[5];
        int duration = Integer.parseInt(splitValue[7]);
        Task task;
        if (splitValue.length == 9) {
            int epicId = Integer.parseInt(splitValue[8]);
            task = new Subtask(name, description, epicId, LocalDateTime.parse(startTime, formatter),
                    Duration.ofMinutes(duration));
            task.setId(id);
            task.setStatus(taskStatus);
            return task;
        } else {
            if (taskType == Type.TASK) {
                task = new Task(name, description, LocalDateTime.parse(startTime, formatter),
                        Duration.ofMinutes(duration));
                task.setId(id);
                task.setStatus(taskStatus);
                return task;
            } else {
                task = new Epic(name, description);
                task.setId(id);
                task.setStatus(taskStatus);
                return task;
            }
        }
    }

    void save() {
        StringBuilder sb = new StringBuilder();
        sb.append(line);
        if (getAllTasks() != null || getAllSubtasks() != null || getAllEpics() != null) {
            getAllTasks().forEach(task -> sb.append("\n").append(toString(task)));
            getAllEpics().forEach(epic -> sb.append("\n").append(toString(epic)));
            getAllSubtasks().forEach(subtask -> sb.append("\n").append(toString(subtask)));
        }
        try {
            Files.writeString(Paths.get(file.toURI()), sb);
        } catch (IOException ex) {
            throw new ManagerSaveException("Не удалось сохранить данные.", ex.getCause());
        }
    }

    private String toString(Task task) {
        String[] valueTask;
        if (task instanceof Subtask) {
            valueTask = new String[]{Integer.toString(task.getId()), getType(task).toString(), task.getName(),
                    task.getStatus().toString(), task.getDescription(), task.getStartTime().format(formatter),
                    task.getEndTime().format(formatter), String.valueOf(task.getDuration().toMinutes()),
                    Integer.toString(((Subtask) task).getEpicId())};
        } else if (task instanceof Epic) {
            valueTask = new String[]{Integer.toString(task.getId()), getType(task).toString(), task.getName(),
                    task.getStatus().toString(), task.getDescription(), String.valueOf(task.getStartTime()),
                    String.valueOf(task.getEndTime()), String.valueOf(task.getDuration().toMinutes())};
        } else {
            valueTask = new String[]{Integer.toString(task.getId()), getType(task).toString(), task.getName(),
                    task.getStatus().toString(), task.getDescription(), task.getStartTime().format(formatter),
                    task.getEndTime().format(formatter), String.valueOf(task.getDuration().toMinutes())};
        }
        return String.join(",", valueTask);
    }

    @Override
    public Subtask createSubtask(Subtask subtask) {
        if (super.createSubtask(subtask) != null) {
            save();
            return subtask;
        }

        return null;
    }

    @Override
    public Task createTask(Task task) {
        if (super.createTask(task) != null) {
            save();
            return task;
        }
        return null;
    }

    @Override
    public Epic createEpic(Epic epic) {

        if (super.createEpic(epic) != null) {
            save();
            return epic;
        }
        return null;
    }

    @Override
    public void removeAllTasks() {
        super.removeAllTasks();
        save();
    }

    @Override
    public void removeAllEpics() {
        super.removeAllEpics();
        save();
    }

    @Override
    public void removeAllSubtasks() {
        super.removeAllSubtasks();
        save();
    }

    @Override
    public Task updateTask(Task newTask) {
        if (super.updateTask(newTask) != null) {
            save();
            return newTask;
        }
        return null;
    }

    @Override
    public Epic updateEpic(Epic newEpic) {
        if (super.updateEpic(newEpic) != null) {
            save();
            return newEpic;
        }
        return null;
    }

    @Override
    public Subtask updateSubtask(Subtask newSubtask) {
        if (super.updateSubtask(newSubtask) != null) {
            save();
            return newSubtask;
        }
        return null;
    }

    @Override
    public void removeTaskById(Integer id) {
        if (id != null) {
            super.removeTaskById(id);
            save();
        }
    }

    @Override
    public void removeEpicById(Integer id) {
        super.removeEpicById(id);
        save();
    }

    @Override
    public void removeSubtaskById(Integer id) {
        super.removeSubtaskById(id);
        save();
    }
}
