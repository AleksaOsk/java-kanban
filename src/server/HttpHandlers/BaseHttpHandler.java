package server.HttpHandlers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import entities.Epic;
import entities.Subtask;
import entities.Task;
import server.Adapters.DurationTypeAdapter;
import server.Adapters.LocalDateTimeAdapter;
import server.ErrorResponse;
import server.HttpMethod;
import service.InMemoryTaskManager;
import service.TaskManager;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;

public class BaseHttpHandler implements HttpHandler {
    protected final TaskManager taskManager;
    private final Gson gson;

    TaskHttpHandler taskHttpHandler;
    SubtaskHttpHandler subtaskHttpHandler;
    EpicHttpHandler epicHttpHandler;

    public BaseHttpHandler(TaskManager taskManager, Gson gson) {
        this.taskManager = taskManager;
        this.gson = gson;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        taskHttpHandler = new TaskHttpHandler(taskManager, gson);
        subtaskHttpHandler = new SubtaskHttpHandler(taskManager, gson);
        epicHttpHandler = new EpicHttpHandler(taskManager, gson);
        try {
            String httpMethod = exchange.getRequestMethod();
            switch (HttpMethod.valueOf(httpMethod)) {
                case GET:
                    handleGet(exchange);
                    return;
                case POST:
                    handlePost(exchange);
                    return;
                case DELETE:
                    handleDelete(exchange);
                    return;
                default:
                    writeResponse(new ErrorResponse("Неверный HTTP-метод"), exchange, 404);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            writeResponse("Что-то пошло не так, попробуйте еще раз", exchange, 500);
        }
    }

    protected void handleGet(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String[] pathArray = path.split("/");
        String type = path.split("/")[2];
        switch (type) {
            case "tasks":
                taskHttpHandler.handleGet(exchange, type, pathArray);
                return;
            case "subtasks":
                subtaskHttpHandler.handleGet(exchange, type, pathArray);
                return;
            case "epics":
                epicHttpHandler.handleGet(exchange, pathArray);
                return;
            case "history":
                writeResponse(taskManager.getHistory(), exchange, 200);
                return;
            case "prioritized"://не понимаю почему тут не получается использовать taskManager.getPrioritizedTasks(),
                //я ведь создаю экземпляр класса InMemoryTaskManager, в котором и реализован данный метод
                writeResponse(InMemoryTaskManager.getPrioritizedTasks(), exchange, 200);
                return;
            default:
                writeResponse(new ErrorResponse("""
                        Неверный URL. Существуют:
                         /tasks
                         /subtasks
                         /epics
                         /history
                         /prioritized"""), exchange, 404);
        }
    }

    protected void handlePost(HttpExchange exchange) throws IOException {
        String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .registerTypeAdapter(Duration.class, new DurationTypeAdapter())
                .create();


        String path = exchange.getRequestURI().getPath();
        String[] pathArray = path.split("/");
        String type = path.split("/")[2];

        switch (type) {
            case "tasks":
                Task task = gson.fromJson(requestBody, Task.class);
                taskHttpHandler.handlePost(exchange, type, pathArray, task);
                return;
            case "subtasks":
                Subtask subtask = gson.fromJson(requestBody, Subtask.class);
                if (subtask.getEpicId() != null) {
                    subtaskHttpHandler.handlePost(exchange, type, pathArray, subtask);
                } else {
                    writeResponse("Вы не добавили epicId, задача не сохранена", exchange, 404);
                }
                return;
            case "epics":
                Epic epic = gson.fromJson(requestBody, Epic.class);
                epicHttpHandler.handlePost(exchange, pathArray, epic);
                return;
            default:
                writeResponse(new ErrorResponse("""
                        Неверный URL. Существуют:
                         /tasks
                         /subtasks
                         /epics"""), exchange, 404);
        }
    }

    protected void handleDelete(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String[] pathArray = path.split("/");
        String type = path.split("/")[2];
        switch (type) {
            case "tasks":
                taskHttpHandler.handleDelete(exchange, type, pathArray);
                return;
            case "subtasks":
                subtaskHttpHandler.handleDelete(exchange, type, pathArray);
                return;
            case "epics":
                epicHttpHandler.handleDelete(exchange, type, pathArray);
                return;
            default:
                writeResponse(new ErrorResponse("""
                        Неверный URL. Существуют:
                         /tasks
                         /subtasks
                         /epics
                        """), exchange, 404);
        }
    }

    protected void writeResponse(Object body, HttpExchange exchange, int code) throws IOException {
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .registerTypeAdapter(Duration.class, new DurationTypeAdapter())
                .create();
        String responseJson = gson.toJson(body);
        byte[] responseBytes = responseJson.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        exchange.sendResponseHeaders(code, responseBytes.length);
        exchange.getResponseBody().write(responseBytes);
        exchange.close();
    }
}