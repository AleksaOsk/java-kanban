package serverTests;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import entities.Epic;
import entities.Subtask;
import entities.Task;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.Adapters.DurationTypeAdapter;
import server.Adapters.LocalDateTimeAdapter;
import server.HttpTaskServer;
import service.FileBackedTaskManager;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class HttpTaskServerTest {

    FileBackedTaskManager manager = FileBackedTaskManager.loadFromFile(File.createTempFile("test", "csv"));
    HttpTaskServer taskServer = new HttpTaskServer(manager);
    Gson gson = HttpTaskServer.getGson();
    HttpRequest request;

    public HttpTaskServerTest() throws IOException {
    }

    @BeforeEach
    public void setUp() throws IOException {
        manager.removeAllTasks();
        manager.removeAllSubtasks();
        manager.removeAllEpics();
        HttpTaskServer.start();
    }

    @AfterEach
    public void shutDown() {
        taskServer.stop();
    }

    public Gson getGson() {
        return gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .registerTypeAdapter(Duration.class, new DurationTypeAdapter())
                .create();
    }

    public HttpRequest getRequestPOST(String taskJson, URI url) {
        return request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .uri(url)
                .version(HttpClient.Version.HTTP_1_1)
                .build();

        // return request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();
    }

    public HttpRequest getRequestGET(URI url) {
        return request = HttpRequest.newBuilder()
                .GET()
                .uri(url)
                .version(HttpClient.Version.HTTP_1_1)
                .build();
    }

    public HttpRequest getRequestDELETE(URI url) {
        return request = HttpRequest.newBuilder()
                .DELETE()
                .uri(url)
                .version(HttpClient.Version.HTTP_1_1)
                .build();
    }


    @Test
    public void testAddTask() throws IOException, InterruptedException {
        Task task = new Task("Таск 1", "Тест таск 1", LocalDateTime.now(), Duration.ofMinutes(5));

        Gson gson = getGson();
        String taskJson = gson.toJson(task);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/TaskManager/tasks");
        HttpRequest request = getRequestPOST(taskJson, url);

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode(), "Неверный код ответа");

        List<Task> tasksFromManager = manager.getAllTasks();

        assertNotNull(tasksFromManager, "Задачи не возвращаются");
        assertEquals(1, tasksFromManager.size(), "Некорректное количество задач");
        assertEquals("Таск 1", tasksFromManager.get(0).getName(), "Некорректное имя задачи");
    }

    @Test
    public void testAddEpic() throws IOException, InterruptedException {
        Epic epic = new Epic("Эпик 1", "Тест эпик 1");
        Gson gson = getGson();
        String taskJson = gson.toJson(epic);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/TaskManager/epics");
        HttpRequest request = getRequestPOST(taskJson, url);

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode(), "Неверный код ответа");

        List<Epic> tasksFromManager = manager.getAllEpics();

        assertNotNull(tasksFromManager, "Задачи не возвращаются");
        assertEquals(1, tasksFromManager.size(), "Некорректное количество задач");
        assertEquals("Эпик 1", tasksFromManager.get(0).getName(), "Некорректное имя задачи");
    }

    @Test
    public void getTasksTest() throws IOException, InterruptedException {
        Task task1 = new Task("Задача 1", "описание 1 задачи", LocalDateTime.of(2024, 7, 20, 15, 30), Duration.ofMinutes(30));
        manager.createTask(task1);
        Task task2 = new Task("Задача 2", "описание 2 задачи", LocalDateTime.of(2024, 7, 20, 16, 30), Duration.ofMinutes(90));
        manager.createTask(task2);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/TaskManager/tasks");

        HttpRequest requestGet = getRequestGET(url);
        HttpResponse<String> responseGet = client.send(requestGet, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, responseGet.statusCode(), "Неверный код ответа");
    }

    @Test
    public void getEpicTest() throws IOException, InterruptedException {
        Epic epic = new Epic("Эпик 1", "описание 1");
        manager.createEpic(epic);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/TaskManager/epics");

        HttpRequest requestGet = getRequestGET(url);
        HttpResponse<String> responseGet = client.send(requestGet, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, responseGet.statusCode(), "Неверный код ответа");
    }

    @Test
    public void addAndGetSubtasksTest() throws IOException, InterruptedException {
        Epic epic = new Epic("Эпик", "Описание эпика");
        Subtask subtask1 = new Subtask("1-я подзадача", "описание 1", 1,
                LocalDateTime.of(2024, 7, 20, 15, 30), Duration.ofMinutes(30));
        Subtask subtask2 = new Subtask("2-я подзадача", "описание 2", 1,
                LocalDateTime.of(2024, 7, 20, 16, 30), Duration.ofMinutes(90));

        Gson gson = getGson();
        String taskJson = gson.toJson(epic);
        String task1Json = gson.toJson(subtask1);
        String task2Json = gson.toJson(subtask2);

        HttpClient client = HttpClient.newHttpClient();

        URI url = URI.create("http://localhost:8080/TaskManager/epics");
        HttpRequest request = getRequestPOST(taskJson, url);
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(response.statusCode(), 201, "Неверный код ответа");

        URI url1 = URI.create("http://localhost:8080/TaskManager/subtasks");
        HttpRequest request1 = getRequestPOST(task1Json, url1);
        HttpResponse<String> response1 = client.send(request1, HttpResponse.BodyHandlers.ofString());
        assertEquals(response1.statusCode(), 201, "Неверный код ответа");
        HttpRequest request2 = getRequestPOST(task2Json, url1);
        HttpResponse<String> response2 = client.send(request2, HttpResponse.BodyHandlers.ofString());
        assertEquals(response2.statusCode(), 201, "Неверный код ответа");

        List<Subtask> tasksFromManager = manager.getAllSubtasks();

        assertNotNull(tasksFromManager, "Задачи не возвращаются");
        assertEquals(tasksFromManager.size(), 2, "Некорректное количество задач");

        HttpRequest requestGet = getRequestGET(url1);
        HttpResponse<String> responseGet = client.send(requestGet, HttpResponse.BodyHandlers.ofString());
        assertEquals(responseGet.statusCode(), 200, "Неверный код ответа");
    }

    @Test
    void getTaskIdTest() throws IOException, InterruptedException {
        Task task1 = new Task("Задача 1", "Задача 1", LocalDateTime.of(2024, 7, 20, 15, 30), Duration.ofMinutes(30));
        manager.createTask(task1);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/TaskManager/tasks/1");

        HttpRequest requestGet = getRequestGET(url);
        HttpResponse<String> responseGet = client.send(requestGet, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, responseGet.statusCode(), "Неверный код ответа");
    }

    @Test
    public void getEpicIdTest() throws IOException, InterruptedException {
        Epic epic = new Epic("Эпик 1", "описание 1");
        manager.createEpic(epic);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/TaskManager/epics/1");

        HttpRequest requestGet = getRequestGET(url);
        HttpResponse<String> responseGet = client.send(requestGet, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, responseGet.statusCode(), "Неверный код ответа");
    }

    @Test
    void getSubtaskIdTest() throws IOException, InterruptedException {
        Epic epic = new Epic("Задача 1", "Задача 1");
        manager.createEpic(epic);
        Subtask subtask1 = new Subtask("Задача 2", "Задача 2", epic.getId(), LocalDateTime.of(2024, 7, 20, 15, 30), Duration.ofMinutes(30));
        manager.createSubtask(subtask1);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/TaskManager/subtasks/2");

        HttpRequest requestGet = getRequestGET(url);
        HttpResponse<String> responseGet = client.send(requestGet, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, responseGet.statusCode(), "Неверный код ответа");
    }

    @Test
    void updateTaskTest() throws IOException, InterruptedException {
        Task task = new Task("Задача 1", "Задача 1", LocalDateTime.of(2024, 7, 20, 15, 30), Duration.ofMinutes(30));
        String nameExpected = task.getName();
        String descriptionExpected = task.getDescription();
        LocalDateTime startTimeExpected = task.getStartTime();
        LocalDateTime endTimeExpected = task.getEndTime();
        Duration durationExpected = task.getDuration();

        Gson gson = getGson();
        String taskJson = gson.toJson(task);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/TaskManager/tasks");
        HttpRequest request = getRequestPOST(taskJson, url);

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode(), "Неверный код ответа");

        task = new Task("Задача 2", "Задача 2", LocalDateTime.of(2024, 7, 20, 16, 30), Duration.ofMinutes(90));

        String taskJsonUpdate = gson.toJson(task);

        URI urlUpdate = URI.create("http://localhost:8080/TaskManager/tasks/1");
        HttpRequest requestUpdate = getRequestPOST(taskJsonUpdate, urlUpdate);

        HttpResponse<String> responseUpdate = client.send(requestUpdate, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, responseUpdate.statusCode(), "Неверный код ответа");

        Assertions.assertNotNull(task, "Задача пустая");
        assertNotEquals(nameExpected, task.getName(), "Название не обновилось");
        assertNotEquals(descriptionExpected, task.getDescription(), "Описание не обновилось");
        assertNotEquals(startTimeExpected, task.getStartTime(), "Стартовое время не обновилось");
        assertNotEquals(endTimeExpected, task.getEndTime(), "Время окончания не обновилось");
        assertNotEquals(durationExpected, task.getDuration(), "Продолжительность выполнения не обновилась");
    }

    @Test
    void updateEpicTest() throws IOException, InterruptedException {
        Epic epic = new Epic("Задача 1", "Задача 1");
        String nameExpected = epic.getName();
        String descriptionExpected = epic.getDescription();

        Gson gson = getGson();
        String taskJson = gson.toJson(epic);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/TaskManager/epics");
        HttpRequest request = getRequestPOST(taskJson, url);

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode(), "Неверный код ответа");

        epic = new Epic("Задача 2", "Задача 2");

        String taskJsonUpdate = gson.toJson(epic);

        URI urlUpdate = URI.create("http://localhost:8080/TaskManager/epics/1");
        HttpRequest requestUpdate = getRequestPOST(taskJsonUpdate, urlUpdate);

        HttpResponse<String> responseUpdate = client.send(requestUpdate, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, responseUpdate.statusCode(), "Неверный код ответа");

        Assertions.assertNotNull(epic, "Эпик пустой");
        assertNotEquals(nameExpected, epic.getName(), "Название не обновилось");
        assertNotEquals(descriptionExpected, epic.getDescription(), "Описание не обновилось");
    }

    @Test
    void updateSubTaskTest() throws IOException, InterruptedException {
        Epic epic = new Epic("Задача 1", "Задача 1");

        LocalDateTime startTimeExpectedEpic = epic.getStartTime();
        LocalDateTime endTimeExpectedEpic = epic.getEndTime();
        Duration durationExpectedEpic = epic.getDuration();

        Gson gson = getGson();
        String taskJson = gson.toJson(epic);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/TaskManager/epics");
        HttpRequest request = getRequestPOST(taskJson, url);

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode(), "Неверный код ответа");

        Subtask subtask = new Subtask("Задача 2", "Задача 2", 1, LocalDateTime.of(2024, 7, 20, 16, 30), Duration.ofMinutes(90));

        String nameExpectedSubtask = subtask.getName();
        String descriptionExpectedSubtask = subtask.getDescription();
        LocalDateTime startTimeExpectedSubtask = subtask.getStartTime();
        LocalDateTime endTimeExpectedSubtask = subtask.getEndTime();
        Duration durationExpectedSubtask = subtask.getDuration();

        String taskJsonUpdate = gson.toJson(subtask);

        URI urlUpdate = URI.create("http://localhost:8080/TaskManager/subtasks");
        HttpRequest requestUpdate = getRequestPOST(taskJsonUpdate, urlUpdate);

        HttpResponse<String> responseUpdate = client.send(requestUpdate, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, responseUpdate.statusCode(), "Неверный код ответа");

        subtask = new Subtask("Задача 3", "Задача 3", 1, LocalDateTime.of(2024, 8, 20, 16, 30), Duration.ofMinutes(80));
        Epic epic1 = manager.getEpicById(subtask.getEpicId());

        Assertions.assertNotNull(epic, "Эпик пустой");
        Assertions.assertNotNull(subtask, "Задача пустая");
        assertNotEquals(nameExpectedSubtask, subtask.getName(), "Название подзадачи не обновилось");
        assertNotEquals(descriptionExpectedSubtask, subtask.getDescription(), "Описание подзадачи не обновилось");
        assertNotEquals(startTimeExpectedSubtask, subtask.getStartTime(), "Стартовое время подзадачи не обновилось");
        assertNotEquals(endTimeExpectedSubtask, subtask.getEndTime(), "Время окончания подзадачи не обновилось");
        assertNotEquals(durationExpectedSubtask, subtask.getDuration(), "Продолжительность выполнения подзадачи не обновилась");

        assertNotEquals(startTimeExpectedEpic, epic1.getStartTime(), "Стартовое время эпика не обновилось");
        assertNotEquals(endTimeExpectedEpic, epic1.getEndTime(), "Время окончания эпика не обновилось");
        assertNotEquals(durationExpectedEpic, epic1.getDuration(), "Продолжительность эпика выполнения не обновилась");
    }


    @Test
    void removeTaskTest() throws IOException, InterruptedException {
        Task task = new Task("Задача 1", "Задача 1", LocalDateTime.of(2024, 7, 20, 15, 30), Duration.ofMinutes(30));
        Gson gson = getGson();
        String taskJson = gson.toJson(task);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/TaskManager/tasks");
        HttpRequest request = getRequestPOST(taskJson, url);

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode(), "Неверный код ответа");

        List<Task> tasksFromManager = manager.getAllTasks();

        assertNotNull(tasksFromManager, "Задачи не возвращаются");
        assertEquals(1, tasksFromManager.size(), "Некорректное количество задач");
        assertEquals("Задача 1", tasksFromManager.get(0).getName(), "Некорректное имя задачи");

        URI urlDelete = URI.create("http://localhost:8080/TaskManager/tasks/1");
        HttpRequest requestDelete = getRequestDELETE(urlDelete);

        HttpResponse<String> responseDelete = client.send(requestDelete, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, responseDelete.statusCode(), "Неверный код ответа");

        List<Task> tasksFromManagerDelete = manager.getAllTasks();

        assertNotEquals(1, tasksFromManagerDelete.size(), "Задача не удалена");
    }

    @Test
    void removeEpicTest() throws IOException, InterruptedException {
        Epic epic = new Epic("Задача 1", "Задача 1");
        Gson gson = getGson();
        String taskJson = gson.toJson(epic);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/TaskManager/epics");
        HttpRequest request = getRequestPOST(taskJson, url);

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode(), "Неверный код ответа");

        List<Epic> tasksFromManager = manager.getAllEpics();

        assertNotNull(tasksFromManager, "Задачи не возвращаются");
        assertEquals(1, tasksFromManager.size(), "Некорректное количество задач");
        assertEquals("Задача 1", tasksFromManager.get(0).getName(), "Некорректное имя задачи");

        URI urlDelete = URI.create("http://localhost:8080/TaskManager/epics/1");
        HttpRequest requestDelete = getRequestDELETE(urlDelete);

        HttpResponse<String> responseDelete = client.send(requestDelete, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, responseDelete.statusCode(), "Неверный код ответа");

        List<Epic> tasksFromManagerDelete = manager.getAllEpics();

        assertNotEquals(1, tasksFromManagerDelete.size(), "Задача не удалена");
    }

    @Test
    void removeSubtaskTest() throws IOException, InterruptedException {
        Epic epic = new Epic("Задача 1", "Задача 1");
        Gson gson = getGson();
        String taskJson = gson.toJson(epic);

        HttpClient client = HttpClient.newHttpClient();

        URI url = URI.create("http://localhost:8080/TaskManager/epics");
        HttpRequest request = getRequestPOST(taskJson, url);

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode(), "Неверный код ответа");

        Subtask subtask = new Subtask("Задача 2", "Задача 2", 1, LocalDateTime.of(2024, 7, 20, 15, 30), Duration.ofMinutes(30));
        String taskJson1 = gson.toJson(subtask);

        URI url1 = URI.create("http://localhost:8080/TaskManager/subtasks");
        HttpRequest request1 = getRequestPOST(taskJson1, url1);

        HttpResponse<String> response1 = client.send(request1, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response1.statusCode(), "Неверный код ответа");

        List<Subtask> tasksFromManager = manager.getAllSubtasks();

        assertNotNull(tasksFromManager, "Задачи не возвращаются");
        assertEquals(1, tasksFromManager.size(), "Некорректное количество задач");
        assertEquals("Задача 2", tasksFromManager.get(0).getName(), "Некорректное имя задачи");

        URI urlDelete = URI.create("http://localhost:8080/TaskManager/subtasks/2");
        HttpRequest requestDelete = getRequestDELETE(urlDelete);

        HttpResponse<String> responseDelete = client.send(requestDelete, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, responseDelete.statusCode(), "Неверный код ответа");

        List<Subtask> tasksFromManagerDelete = manager.getAllSubtasks();

        assertNotEquals(1, tasksFromManagerDelete.size(), "Задача не удалена");
    }


    @Test
    void getPrioritizedTasksTest() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        Gson gson = getGson();

        Task task1 = new Task("1-я задача", "Описание 1-ой задачи",
                LocalDateTime.of(2024, 7, 20, 10, 30), Duration.ofMinutes(30));
        String taskJson1 = gson.toJson(task1);
        URI urlTask = URI.create("http://localhost:8080/TaskManager/tasks");
        HttpRequest requestTask1 = getRequestPOST(taskJson1, urlTask);
        HttpResponse<String> responseTask1 = client.send(requestTask1, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, responseTask1.statusCode(), "Неверный код ответа");


        Task task2 = new Task("2-я задача", "Описание 2-ой задачи",
                LocalDateTime.of(2024, 7, 20, 12, 0), Duration.ofMinutes(30));
        String taskJson2 = gson.toJson(task2);
        HttpRequest requestTask2 = getRequestPOST(taskJson2, urlTask);
        HttpResponse<String> responseTask2 = client.send(requestTask2, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, responseTask2.statusCode(), "Неверный код ответа");


        Epic epic = new Epic("1-й эпик", "Описание 1-го эпика");
        String epicJson = gson.toJson(epic);
        URI urlEpic = URI.create("http://localhost:8080/TaskManager/epics");
        HttpRequest requestEpic = getRequestPOST(epicJson, urlEpic);
        HttpResponse<String> responseEpic = client.send(requestEpic, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, responseEpic.statusCode(), "Неверный код ответа");


        Subtask subtask3 = new Subtask("1-я подзадача", "Описание 1-ой подзадачи", 3,
                LocalDateTime.of(2024, 7, 21, 15, 30), Duration.ofMinutes(90));
        String subtaskJson = gson.toJson(subtask3);
        URI urlSubtask = URI.create("http://localhost:8080/TaskManager/subtasks");
        HttpRequest requestSubtask = getRequestPOST(subtaskJson, urlSubtask);
        HttpResponse<String> responseSubtask = client.send(requestSubtask, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, responseSubtask.statusCode(), "Неверный код ответа");


        URI url = URI.create("http://localhost:8080/TaskManager/prioritized");
        HttpRequest request = getRequestGET(url);
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Неверный код ответа");

        String expectedBody = """
                [
                  {
                    "name": "1-я задача",
                    "description": "Описание 1-ой задачи",
                    "id": 1,
                    "status": "NEW",
                    "duration": 30,
                    "startTime": "2024-07-20T10:30"
                  },
                  {
                    "name": "2-я задача",
                    "description": "Описание 2-ой задачи",
                    "id": 2,
                    "status": "NEW",
                    "duration": 30,
                    "startTime": "2024-07-20T12:00"
                  },
                  {
                    "epicId": 3,
                    "name": "1-я подзадача",
                    "description": "Описание 1-ой подзадачи",
                    "id": 4,
                    "status": "NEW",
                    "duration": 90,
                    "startTime": "2024-07-21T15:30"
                  }
                ]""";

        Assertions.assertEquals(expectedBody, response.body(), "Ошибка сортировки при добавлении задач");

        URI urlDelete = URI.create("http://localhost:8080/TaskManager/tasks/2");
        HttpRequest requestDelete = getRequestDELETE(urlDelete);
        HttpResponse<String> responseDelete = client.send(requestDelete, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, responseDelete.statusCode(), "Неверный код ответа");

        URI url2 = URI.create("http://localhost:8080/TaskManager/prioritized");
        HttpRequest request2 = getRequestGET(url2);
        HttpResponse<String> response2 = client.send(request2, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response2.statusCode(), "Неверный код ответа");

        String expectedBodyAfterDelete = """
                [
                  {
                    "name": "1-я задача",
                    "description": "Описание 1-ой задачи",
                    "id": 1,
                    "status": "NEW",
                    "duration": 30,
                    "startTime": "2024-07-20T10:30"
                  },
                  {
                    "epicId": 3,
                    "name": "1-я подзадача",
                    "description": "Описание 1-ой подзадачи",
                    "id": 4,
                    "status": "NEW",
                    "duration": 90,
                    "startTime": "2024-07-21T15:30"
                  }
                ]""";

        Assertions.assertEquals(expectedBodyAfterDelete, response2.body(), "Ошибка сортировки при добавлении задач");
    }

    @Test
    void getHistoryTest() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        Gson gson = getGson();

        Task task1 = new Task("1-я задача", "Описание 1-ой задачи",
                LocalDateTime.of(2024, 7, 20, 10, 30), Duration.ofMinutes(30));
        String taskJson1 = gson.toJson(task1);
        URI urlTask = URI.create("http://localhost:8080/TaskManager/tasks");
        HttpRequest requestTask1 = getRequestPOST(taskJson1, urlTask);
        HttpResponse<String> responseTask1 = client.send(requestTask1, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, responseTask1.statusCode(), "Неверный код ответа");


        Epic epic = new Epic("1-й эпик", "Описание 1-го эпика");
        String epicJson = gson.toJson(epic);
        URI urlEpic = URI.create("http://localhost:8080/TaskManager/epics");
        HttpRequest requestEpic = getRequestPOST(epicJson, urlEpic);
        HttpResponse<String> responseEpic = client.send(requestEpic, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, responseEpic.statusCode(), "Неверный код ответа");


        Subtask subtask3 = new Subtask("1-я подзадача", "Описание 1-ой подзадачи", 2,
                LocalDateTime.of(2024, 7, 21, 15, 30), Duration.ofMinutes(90));
        String subtaskJson = gson.toJson(subtask3);
        URI urlSubtask = URI.create("http://localhost:8080/TaskManager/subtasks");
        HttpRequest requestSubtask = getRequestPOST(subtaskJson, urlSubtask);
        HttpResponse<String> responseSubtask = client.send(requestSubtask, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, responseSubtask.statusCode(), "Неверный код ответа");

        URI urlTaskGet = URI.create("http://localhost:8080/TaskManager/tasks/1");
        HttpRequest requestGetTask = getRequestGET(urlTaskGet);
        HttpResponse<String> responseGetTask = client.send(requestGetTask, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, responseGetTask.statusCode(), "Неверный код ответа");

        URI urlEpicGet = URI.create("http://localhost:8080/TaskManager/epics/2");
        HttpRequest requestGetEpic = getRequestGET(urlEpicGet);
        HttpResponse<String> responseGetEpic = client.send(requestGetEpic, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, responseGetEpic.statusCode(), "Неверный код ответа");

        URI urlSubtaskGet = URI.create("http://localhost:8080/TaskManager/subtasks/3");
        HttpRequest requestGetSubtask = getRequestGET(urlSubtaskGet);
        HttpResponse<String> responseGetSubtask = client.send(requestGetSubtask, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, responseGetSubtask.statusCode(), "Неверный код ответа");

        URI url = URI.create("http://localhost:8080/TaskManager/history");
        HttpRequest request = getRequestGET(url);
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Неверный код ответа");

        String expectedBody = """
                [
                  {
                    "name": "1-я задача",
                    "description": "Описание 1-ой задачи",
                    "id": 1,
                    "status": "NEW",
                    "duration": 30,
                    "startTime": "2024-07-20T10:30"
                  },
                  {
                    "subtasks": {
                      "3": {
                        "epicId": 2,
                        "name": "1-я подзадача",
                        "description": "Описание 1-ой подзадачи",
                        "id": 3,
                        "status": "NEW",
                        "duration": 90,
                        "startTime": "2024-07-21T15:30"
                      }
                    },
                    "name": "1-й эпик",
                    "description": "Описание 1-го эпика",
                    "id": 2,
                    "status": "NEW",
                    "duration": 90,
                    "startTime": "2024-07-21T15:30"
                  },
                  {
                    "epicId": 2,
                    "name": "1-я подзадача",
                    "description": "Описание 1-ой подзадачи",
                    "id": 3,
                    "status": "NEW",
                    "duration": 90,
                    "startTime": "2024-07-21T15:30"
                  }
                ]""";

        Assertions.assertEquals(expectedBody, response.body(), "Ошибка сортировки при добавлении задач");

        URI urlDelete = URI.create("http://localhost:8080/TaskManager/tasks/1");
        HttpRequest requestDelete = getRequestDELETE(urlDelete);
        HttpResponse<String> responseDelete = client.send(requestDelete, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, responseDelete.statusCode(), "Неверный код ответа");

        URI url2 = URI.create("http://localhost:8080/TaskManager/history");
        HttpRequest request2 = getRequestGET(url2);
        HttpResponse<String> response2 = client.send(request2, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response2.statusCode(), "Неверный код ответа");

        String expectedBodyAfterDelete = """
                [
                  {
                    "subtasks": {
                      "3": {
                        "epicId": 2,
                        "name": "1-я подзадача",
                        "description": "Описание 1-ой подзадачи",
                        "id": 3,
                        "status": "NEW",
                        "duration": 90,
                        "startTime": "2024-07-21T15:30"
                      }
                    },
                    "name": "1-й эпик",
                    "description": "Описание 1-го эпика",
                    "id": 2,
                    "status": "NEW",
                    "duration": 90,
                    "startTime": "2024-07-21T15:30"
                  },
                  {
                    "epicId": 2,
                    "name": "1-я подзадача",
                    "description": "Описание 1-ой подзадачи",
                    "id": 3,
                    "status": "NEW",
                    "duration": 90,
                    "startTime": "2024-07-21T15:30"
                  }
                ]""";
        Assertions.assertEquals(expectedBodyAfterDelete, response2.body(), "Ошибка сортировки при добавлении задач");
    }
}