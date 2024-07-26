package server.HttpHandlers;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import entities.Subtask;
import entities.Task;
import service.TaskManager;

import java.io.IOException;
import java.util.List;

public class TaskHttpHandler extends BaseHttpHandler {

    public TaskHttpHandler(TaskManager taskManager, Gson gson) {
        super(taskManager, gson);
    }

    protected void handleGet(HttpExchange exchange, TypeRequest type, String[] pathArray) throws IOException {
        if (pathArray.length == 3) {
            List<? extends Task> task = getAll(type);
            writeResponse(task, exchange, 200);
        } else if (pathArray.length == 4) {
            Task task = getOneOf(type, Integer.parseInt(pathArray[3]));

            if (task != null) {
                writeResponse(task, exchange, 200);
            } else {
                writeResponse("Такой задачи нет", exchange, 404);
            }
        }
    }

    protected void handlePost(HttpExchange exchange, TypeRequest type, String[] pathArray, Task task) throws IOException {
        if (pathArray.length == 3) {
            Task createdTask = create(type, task);
            if (createdTask != null) {
                writeResponse(createdTask, exchange, 201);
            } else {
                writeResponse("Задача пересекается с существующими, распредилите время корректней", exchange, 406);
            }
        } else if (pathArray.length == 4) {
            task.setId(Integer.valueOf(pathArray[3]));
            Task updateTask = update(type, task);
            if (updateTask != null) {
                writeResponse(updateTask, exchange, 201);
            } else {
                writeResponse("Задача пересекается с существующими, распредилите время корректней", exchange, 406);
            }
        }
    }

    protected void handleDelete(HttpExchange exchange, TypeRequest type, String[] pathArray) throws IOException {
        switch (type) {
            case tasks -> {
                if (taskManager.getTaskById(Integer.valueOf(pathArray[3])) != null) {
                    taskManager.removeTaskById(Integer.valueOf(pathArray[3]));
                    writeResponse("Задача удалена", exchange, 200);
                } else {
                    writeResponse("Такой задачи нет", exchange, 404);
                }
            }
            case subtasks -> {
                if (taskManager.getSubtaskById(Integer.valueOf(pathArray[3])) != null) {
                    taskManager.removeSubtaskById(Integer.valueOf(pathArray[3]));
                    writeResponse("Подзадача удалена", exchange, 200);
                } else {
                    writeResponse("Такой подзадачи нет", exchange, 404);
                }
            }
            case epics -> {
                if (taskManager.getEpicById(Integer.valueOf(pathArray[3])) != null) {
                    taskManager.removeEpicById(Integer.valueOf(pathArray[3]));
                    writeResponse("Эпик удален", exchange, 200);
                } else {
                    writeResponse("Такого эпика нет", exchange, 404);
                }
            }
        }
    }

    protected List<? extends Task> getAll(TypeRequest type) {
        switch (type) {
            case tasks -> {
                List<Task> allTasks = taskManager.getAllTasks();
                return allTasks;
            }
            case subtasks -> {
                List<Subtask> allTasks = taskManager.getAllSubtasks();
                return allTasks;
            }
        }
        return null;
    }

    protected Task getOneOf(TypeRequest type, int id) {
        return switch (type) {
            case tasks -> taskManager.getTaskById(id);
            case subtasks -> taskManager.getSubtaskById(id);
            default -> null;
        };
    }

    protected Task create(TypeRequest type, Task task) {
        return switch (type) {
            case tasks -> taskManager.createTask(task);
            case subtasks -> taskManager.createSubtask((Subtask) task);
            default -> null;
        };
    }

    protected Task update(TypeRequest type, Task task) {
        return switch (type) {
            case tasks -> taskManager.updateTask(task);
            case subtasks -> taskManager.updateSubtask((Subtask) task);
            default -> null;
        };
    }
}