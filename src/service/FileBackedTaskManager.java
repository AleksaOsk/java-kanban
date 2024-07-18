package service;

import entities.*;
import exceptions.ManagerSaveException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;


public class FileBackedTaskManager extends InMemoryTaskManager {

    private final File file;
    static final String line = "id,type,name,status,description,epic";

    public FileBackedTaskManager(File file) {
        this.file = file;
        save();
    }


    public static FileBackedTaskManager loadFromFile(File file) throws IOException {
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
        Task task;
        if (splitValue.length == 6) {
            int epicId = Integer.parseInt(splitValue[5]);
            task = new Subtask(name, description, epicId);
            task.setId(id);
            task.setStatus(taskStatus);
            return task;
        } else {
            if (taskType == Type.TASK) {
                task = new Task(name, description);
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
        for (Task task : getAllTasks()) {
            sb.append("\n").append(toString(task));
        }
        for (Task task : getAllEpics()) {
            sb.append("\n").append(toString(task));
        }
        for (Task task : getAllSubtasks()) {
            sb.append("\n").append(toString(task));
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
            valueTask = new String[]{Integer.toString(task.getId()), getType(task).toString(), task.getName(), task.getStatus().toString(),
                    task.getDescription(), Integer.toString(((Subtask) task).getEpicId())};
        } else {
            valueTask = new String[]{Integer.toString(task.getId()), getType(task).toString(), task.getName(), task.getStatus().toString(),
                    task.getDescription()};
        }
        return String.join(",", valueTask);
    }

    @Override
    public Subtask createSubtask(Subtask subtask) {
        super.createSubtask(subtask);
        save();
        return subtask;
    }

    @Override
    public Task createTask(Task task) {
        super.createTask(task);
        save();
        return task;
    }

    @Override
    public Epic createEpic(Epic epic) {
        super.createEpic(epic);
        save();
        return epic;
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
        super.updateTask(newTask);
        save();
        return newTask;
    }

    @Override
    public Epic updateEpic(Epic newEpic) {
        super.updateEpic(newEpic);
        save();
        return newEpic;
    }

    @Override
    public Subtask updateSubtask(Subtask newSubtask) {
        super.updateSubtask(newSubtask);
        save();
        return newSubtask;
    }

    @Override
    public void removeTaskById(Integer id) {
        super.removeTaskById(id);
        save();
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
