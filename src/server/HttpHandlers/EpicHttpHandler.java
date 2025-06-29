package server.HttpHandlers;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import entities.Epic;
import entities.Subtask;
import service.TaskManager;

import java.io.IOException;
import java.time.Duration;
import java.util.List;

public class EpicHttpHandler extends TaskHttpHandler {

    public EpicHttpHandler(TaskManager taskManager, Gson gson) {
        super(taskManager, gson);
    }


    protected void handleGet(HttpExchange exchange, String[] pathArray) throws IOException {
        if (pathArray.length == 3) {
            List<Epic> epics = taskManager.getAllEpics();
            writeResponse(epics, exchange, 200);
        } else if (pathArray.length == 4) {
            Epic epic = taskManager.getEpicById(Integer.parseInt(pathArray[3]));
            if (epic != null) {
                writeResponse(epic, exchange, 200);
            } else {
                writeResponse("Такого эпика нет", exchange, 404);
            }
        } else if (pathArray.length == 5) {
            Epic epic = taskManager.getEpicById(Integer.parseInt(pathArray[3]));
            if (epic != null) {
                List<Subtask> subtasks = taskManager.getAllSubtasksOfEpic(epic);
                if (subtasks != null) {
                    writeResponse(subtasks, exchange, 200);
                } else {
                    writeResponse("У эпика нет подзадач", exchange, 404);
                }
            } else {
                writeResponse("Такого эпика нет", exchange, 404);
            }
        }
    }

    protected void handlePost(HttpExchange exchange, String[] pathArray, Epic epic) throws IOException {

        if (epic.getStartTime() != null || epic.getEndTime() != null || epic.getDuration() != Duration.ZERO) {
            writeResponse("Для создания эпика необходимы поля только name и description", exchange, 404);
        } else {
            if (pathArray.length == 3) {
                Epic createdEpic = taskManager.createEpic(epic);
                writeResponse(createdEpic, exchange, 201);

            } else if (pathArray.length == 4) {
                epic.setId(Integer.valueOf(pathArray[3]));
                Epic updateEpic = taskManager.updateEpic(epic);
                if (updateEpic != null) {
                    writeResponse(updateEpic, exchange, 200);
                } else {
                    writeResponse("Такого эпика нет", exchange, 404);
                }
            }
        }
    }
}
