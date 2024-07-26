import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import entities.Epic;
import entities.Subtask;
import entities.Task;
import org.junit.jupiter.api.AfterEach;
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

import static org.junit.jupiter.api.Assertions.*;

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
    public void addTaskTest() throws IOException, InterruptedException {
        Gson gson = getGson();
        HttpClient client = HttpClient.newHttpClient();

        Task task1 = new Task("Таск 1", "Тест таск 1", LocalDateTime.of(2024, 7, 26, 12, 0), Duration.ofMinutes(5));
        String taskJson = gson.toJson(task1);
        URI url = URI.create("http://localhost:8080/TaskManager/tasks");
        HttpRequest request = getRequestPOST(taskJson, url);
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode(), "Неверный код ответа");

        Task task2 = new Task("Таск 2", "Тест таск 2", LocalDateTime.of(2024, 7, 26, 13, 0), Duration.ofMinutes(5));
        String taskJson2 = gson.toJson(task2);
        URI url2 = URI.create("http://localhost:8080/TaskManager/tasks");
        HttpRequest request2 = getRequestPOST(taskJson2, url2);
        HttpResponse<String> response2 = client.send(request2, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response2.statusCode(), "Неверный код ответа");

        List<Task> tasksFromManager = manager.getAllTasks();

        assertNotNull(tasksFromManager, "Задачи не возвращаются");
        assertEquals(2, tasksFromManager.size(), "Некорректное количество задач");
        assertEquals("Таск 1", tasksFromManager.getFirst().getName(), "Некорректное имя задачи");
        assertEquals("Таск 2", tasksFromManager.getLast().getName(), "Некорректное имя задачи");
    }

    @Test
    public void addEpicTest() throws IOException, InterruptedException {
        Gson gson = getGson();
        HttpClient client = HttpClient.newHttpClient();

        Epic epic = new Epic("Эпик 1", "Тест эпик 1");
        String taskJson = gson.toJson(epic);
        URI url = URI.create("http://localhost:8080/TaskManager/epics");
        HttpRequest request = getRequestPOST(taskJson, url);
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode(), "Неверный код ответа");

        List<Epic> tasksFromManager = manager.getAllEpics();

        assertNotNull(tasksFromManager, "Задачи не возвращаются");
        assertEquals(1, tasksFromManager.size(), "Некорректное количество задач");
        assertEquals("Эпик 1", tasksFromManager.getFirst().getName(), "Некорректное имя задачи");
    }

    @Test
    public void addSubtasksTest() throws IOException, InterruptedException {
        Gson gson = getGson();
        HttpClient client = HttpClient.newHttpClient();

        addEpicTest();

        Subtask subtask1 = new Subtask("1-я подзадача", "описание 1", 1,
                LocalDateTime.of(2024, 7, 20, 15, 30), Duration.ofMinutes(30));
        Subtask subtask2 = new Subtask("2-я подзадача", "описание 2", 1,
                LocalDateTime.of(2024, 7, 20, 16, 30), Duration.ofMinutes(90));


        String task1Json = gson.toJson(subtask1);
        String task2Json = gson.toJson(subtask2);

        URI url = URI.create("http://localhost:8080/TaskManager/subtasks");
        HttpRequest request1 = getRequestPOST(task1Json, url);
        HttpResponse<String> response1 = client.send(request1, HttpResponse.BodyHandlers.ofString());
        assertEquals(response1.statusCode(), 201, "Неверный код ответа");
        HttpRequest request2 = getRequestPOST(task2Json, url);
        HttpResponse<String> response2 = client.send(request2, HttpResponse.BodyHandlers.ofString());
        assertEquals(response2.statusCode(), 201, "Неверный код ответа");

        List<Subtask> tasksFromManager = manager.getAllSubtasks();

        assertNotNull(tasksFromManager, "Задачи не возвращаются");
        assertEquals(2, tasksFromManager.size(), "Некорректное количество задач");
        assertEquals("1-я подзадача", tasksFromManager.getFirst().getName(), "Некорректное имя подзадачи");
    }

    @Test
    public void getTasksTest() throws IOException, InterruptedException {
        addTaskTest();

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/TaskManager/tasks");
        HttpRequest requestGet = getRequestGET(url);
        HttpResponse<String> responseGet = client.send(requestGet, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, responseGet.statusCode(), "Неверный код ответа");

        String expectedBody = """
                [
                  {
                    "name": "Таск 1",
                    "description": "Тест таск 1",
                    "id": 1,
                    "status": "NEW",
                    "duration": 5,
                    "startTime": "2024-07-26T12:00"
                  },
                  {
                    "name": "Таск 2",
                    "description": "Тест таск 2",
                    "id": 2,
                    "status": "NEW",
                    "duration": 5,
                    "startTime": "2024-07-26T13:00"
                  }
                ]""";

        assertNotNull(responseGet.body(), "Задачи не возвращаются");
        assertEquals(expectedBody, responseGet.body(), "Некорректно выводятся задачи");

    }

    @Test
    public void getSubtasksTest() throws IOException, InterruptedException {
        addSubtasksTest();

        HttpClient client = HttpClient.newHttpClient();

        URI url = URI.create("http://localhost:8080/TaskManager/subtasks");
        HttpRequest requestGet = getRequestGET(url);
        HttpResponse<String> responseGet = client.send(requestGet, HttpResponse.BodyHandlers.ofString());
        assertEquals(responseGet.statusCode(), 200, "Неверный код ответа");

        String expectedBody = """
                [
                  {
                    "epicId": 1,
                    "name": "1-я подзадача",
                    "description": "описание 1",
                    "id": 2,
                    "status": "NEW",
                    "duration": 30,
                    "startTime": "2024-07-20T15:30"
                  },
                  {
                    "epicId": 1,
                    "name": "2-я подзадача",
                    "description": "описание 2",
                    "id": 3,
                    "status": "NEW",
                    "duration": 90,
                    "startTime": "2024-07-20T16:30"
                  }
                ]""";

        assertNotNull(responseGet.body(), "Задачи не возвращаются");
        assertEquals(expectedBody, responseGet.body(), "Некорректно выводятся задачи");
    }

    @Test
    public void getEpicsTest() throws IOException, InterruptedException {
        addSubtasksTest();

        Epic epic = new Epic("Эпик 2", "Тест эпик 2");
        manager.createEpic(epic);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/TaskManager/epics");

        HttpRequest requestGet = getRequestGET(url);
        HttpResponse<String> responseGet = client.send(requestGet, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, responseGet.statusCode(), "Неверный код ответа");

        String expectedBody = """
                [
                  {
                    "subtasks": {
                      "2": {
                        "epicId": 1,
                        "name": "1-я подзадача",
                        "description": "описание 1",
                        "id": 2,
                        "status": "NEW",
                        "duration": 30,
                        "startTime": "2024-07-20T15:30"
                      },
                      "3": {
                        "epicId": 1,
                        "name": "2-я подзадача",
                        "description": "описание 2",
                        "id": 3,
                        "status": "NEW",
                        "duration": 90,
                        "startTime": "2024-07-20T16:30"
                      }
                    },
                    "name": "Эпик 1",
                    "description": "Тест эпик 1",
                    "id": 1,
                    "status": "NEW",
                    "duration": 120,
                    "startTime": "2024-07-20T15:30"
                  },
                  {
                    "subtasks": {},
                    "name": "Эпик 2",
                    "description": "Тест эпик 2",
                    "id": 4,
                    "status": "NEW",
                    "duration": 0
                  }
                ]""";

        assertNotNull(responseGet.body(), "Задачи не возвращаются");
        assertEquals(expectedBody, responseGet.body(), "Некорректно выводятся задачи");
    }

    @Test
    void getTaskIdTest() throws IOException, InterruptedException {
        addTaskTest();

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/TaskManager/tasks/1");

        HttpRequest requestGet = getRequestGET(url);
        HttpResponse<String> responseGet = client.send(requestGet, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, responseGet.statusCode(), "Неверный код ответа");

        String expectedBody = """
                {
                  "name": "Таск 1",
                  "description": "Тест таск 1",
                  "id": 1,
                  "status": "NEW",
                  "duration": 5,
                  "startTime": "2024-07-26T12:00"
                }""";

        assertNotNull(responseGet.body(), "Задача не возвращается");
        assertEquals(expectedBody, responseGet.body(), "Некорректно выводится задача");
    }

    @Test
    public void getEpicIdTest() throws IOException, InterruptedException {
        getEpicsTest();

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/TaskManager/epics/1");

        HttpRequest requestGet = getRequestGET(url);
        HttpResponse<String> responseGet = client.send(requestGet, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, responseGet.statusCode(), "Неверный код ответа");

        String expectedBody = """
                {
                  "subtasks": {
                    "2": {
                      "epicId": 1,
                      "name": "1-я подзадача",
                      "description": "описание 1",
                      "id": 2,
                      "status": "NEW",
                      "duration": 30,
                      "startTime": "2024-07-20T15:30"
                    },
                    "3": {
                      "epicId": 1,
                      "name": "2-я подзадача",
                      "description": "описание 2",
                      "id": 3,
                      "status": "NEW",
                      "duration": 90,
                      "startTime": "2024-07-20T16:30"
                    }
                  },
                  "name": "Эпик 1",
                  "description": "Тест эпик 1",
                  "id": 1,
                  "status": "NEW",
                  "duration": 120,
                  "startTime": "2024-07-20T15:30"
                }""";

        assertNotNull(responseGet.body(), "Задачи не возвращаются");
        assertEquals(expectedBody, responseGet.body(), "Некорректно выводятся задачи");
    }

    @Test
    void getSubtaskIdTest() throws IOException, InterruptedException {
        addSubtasksTest();

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/TaskManager/subtasks/2");
        HttpRequest requestGet = getRequestGET(url);
        HttpResponse<String> responseGet = client.send(requestGet, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, responseGet.statusCode(), "Неверный код ответа");

        String expectedBody = """
                {
                  "epicId": 1,
                  "name": "1-я подзадача",
                  "description": "описание 1",
                  "id": 2,
                  "status": "NEW",
                  "duration": 30,
                  "startTime": "2024-07-20T15:30"
                }""";

        assertNotNull(responseGet.body(), "Задача не возвращается");
        assertEquals(expectedBody, responseGet.body(), "Некорректно выводится задача");
    }

    @Test
    void updateTaskTest() throws IOException, InterruptedException {
        addTaskTest();

        Gson gson = getGson();
        HttpClient client = HttpClient.newHttpClient();

        Task task = new Task("Задача 2", "Задача 2", LocalDateTime.of(2024, 7, 20, 16, 30), Duration.ofMinutes(90));
        String taskJsonUpdate = gson.toJson(task);
        URI urlUpdate = URI.create("http://localhost:8080/TaskManager/tasks/1");
        HttpRequest requestUpdate = getRequestPOST(taskJsonUpdate, urlUpdate);
        HttpResponse<String> responseUpdate = client.send(requestUpdate, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, responseUpdate.statusCode(), "Неверный код ответа");

        String expectedBody = """
                {
                  "name": "Задача 2",
                  "description": "Задача 2",
                  "id": 1,
                  "status": "NEW",
                  "duration": 90,
                  "startTime": "2024-07-20T16:30"
                }""";

        assertNotNull(responseUpdate.body(), "Задача не возвращается");
        assertEquals(expectedBody, responseUpdate.body(), "Некорректно выводится задача");
    }

    @Test
    void updateEpicTest() throws IOException, InterruptedException {
        addEpicTest();

        Gson gson = getGson();
        HttpClient client = HttpClient.newHttpClient();

        Epic epic = new Epic("Задача 2", "Задача 2");
        String taskJsonUpdate = gson.toJson(epic);
        URI urlUpdate = URI.create("http://localhost:8080/TaskManager/epics/1");
        HttpRequest requestUpdate = getRequestPOST(taskJsonUpdate, urlUpdate);
        HttpResponse<String> responseUpdate = client.send(requestUpdate, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, responseUpdate.statusCode(), "Неверный код ответа");

        String expectedBody = """
                {
                  "subtasks": {},
                  "name": "Задача 2",
                  "description": "Задача 2",
                  "id": 1,
                  "status": "NEW",
                  "duration": 0
                }""";

        assertNotNull(responseUpdate.body(), "Задача не возвращается");
        assertEquals(expectedBody, responseUpdate.body(), "Некорректно выводится задача");
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

        assertNotNull(epic, "Эпик пустой");
        assertNotNull(subtask, "Задача пустая");
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
        addTaskTest();

        List<Task> tasksFromManager = manager.getAllTasks();

        HttpClient client = HttpClient.newHttpClient();
        URI urlDelete = URI.create("http://localhost:8080/TaskManager/tasks/1");
        HttpRequest requestDelete = getRequestDELETE(urlDelete);
        HttpResponse<String> responseDelete = client.send(requestDelete, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, responseDelete.statusCode(), "Неверный код ответа");

        List<Task> tasksFromManagerDelete = manager.getAllTasks();

        assertEquals(tasksFromManager.size() - 1, tasksFromManagerDelete.size(), "Задача не удалена");
    }

    @Test
    void removeEpicTest() throws IOException, InterruptedException {
        addEpicTest();
        HttpClient client = HttpClient.newHttpClient();

        List<Epic> tasksFromManager = manager.getAllEpics();

        URI urlDelete = URI.create("http://localhost:8080/TaskManager/epics/1");
        HttpRequest requestDelete = getRequestDELETE(urlDelete);
        HttpResponse<String> responseDelete = client.send(requestDelete, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, responseDelete.statusCode(), "Неверный код ответа");

        List<Epic> tasksFromManagerDelete = manager.getAllEpics();

        assertEquals(tasksFromManager.size() - 1, tasksFromManagerDelete.size(), "Задача не удалена");
    }

    @Test
    void removeSubtaskTest() throws IOException, InterruptedException {
        addSubtasksTest();
        HttpClient client = HttpClient.newHttpClient();

        List<Subtask> tasksFromManager = manager.getAllSubtasks();

        URI urlDelete = URI.create("http://localhost:8080/TaskManager/subtasks/2");
        HttpRequest requestDelete = getRequestDELETE(urlDelete);

        HttpResponse<String> responseDelete = client.send(requestDelete, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, responseDelete.statusCode(), "Неверный код ответа");

        List<Subtask> tasksFromManagerDelete = manager.getAllSubtasks();

        assertEquals(tasksFromManager.size() - 1, tasksFromManagerDelete.size(), "Задача не удалена");
    }

    @Test
    void getPrioritizedTasksTest() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();

        addSubtasksTest();
        addTaskTest();

        URI url = URI.create("http://localhost:8080/TaskManager/prioritized");
        HttpRequest request = getRequestGET(url);
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Неверный код ответа");

        String expectedBody = """
                [
                  {
                    "epicId": 1,
                    "name": "1-я подзадача",
                    "description": "описание 1",
                    "id": 2,
                    "status": "NEW",
                    "duration": 30,
                    "startTime": "2024-07-20T15:30"
                  },
                  {
                    "epicId": 1,
                    "name": "2-я подзадача",
                    "description": "описание 2",
                    "id": 3,
                    "status": "NEW",
                    "duration": 90,
                    "startTime": "2024-07-20T16:30"
                  },
                  {
                    "name": "Таск 1",
                    "description": "Тест таск 1",
                    "id": 4,
                    "status": "NEW",
                    "duration": 5,
                    "startTime": "2024-07-26T12:00"
                  },
                  {
                    "name": "Таск 2",
                    "description": "Тест таск 2",
                    "id": 5,
                    "status": "NEW",
                    "duration": 5,
                    "startTime": "2024-07-26T13:00"
                  }
                ]""";

        assertEquals(expectedBody, response.body(), "Ошибка сортировки при добавлении задач");

        URI urlDelete = URI.create("http://localhost:8080/TaskManager/tasks/4");
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
                    "epicId": 1,
                    "name": "1-я подзадача",
                    "description": "описание 1",
                    "id": 2,
                    "status": "NEW",
                    "duration": 30,
                    "startTime": "2024-07-20T15:30"
                  },
                  {
                    "epicId": 1,
                    "name": "2-я подзадача",
                    "description": "описание 2",
                    "id": 3,
                    "status": "NEW",
                    "duration": 90,
                    "startTime": "2024-07-20T16:30"
                  },
                  {
                    "name": "Таск 2",
                    "description": "Тест таск 2",
                    "id": 5,
                    "status": "NEW",
                    "duration": 5,
                    "startTime": "2024-07-26T13:00"
                  }
                ]""";

        assertEquals(expectedBodyAfterDelete, response2.body(), "Ошибка сортировки при добавлении задач");
    }

    @Test
    void getHistoryTest() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        addSubtasksTest();
        addTaskTest();

        URI urlTaskGet = URI.create("http://localhost:8080/TaskManager/tasks/4");
        HttpRequest requestGetTask = getRequestGET(urlTaskGet);
        HttpResponse<String> responseGetTask = client.send(requestGetTask, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, responseGetTask.statusCode(), "Неверный код ответа");

        URI urlEpicGet = URI.create("http://localhost:8080/TaskManager/epics/1");
        HttpRequest requestGetEpic = getRequestGET(urlEpicGet);
        HttpResponse<String> responseGetEpic = client.send(requestGetEpic, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, responseGetEpic.statusCode(), "Неверный код ответа");

        URI urlSubtaskGet = URI.create("http://localhost:8080/TaskManager/subtasks/2");
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
                    "name": "Таск 1",
                    "description": "Тест таск 1",
                    "id": 4,
                    "status": "NEW",
                    "duration": 5,
                    "startTime": "2024-07-26T12:00"
                  },
                  {
                    "subtasks": {
                      "2": {
                        "epicId": 1,
                        "name": "1-я подзадача",
                        "description": "описание 1",
                        "id": 2,
                        "status": "NEW",
                        "duration": 30,
                        "startTime": "2024-07-20T15:30"
                      },
                      "3": {
                        "epicId": 1,
                        "name": "2-я подзадача",
                        "description": "описание 2",
                        "id": 3,
                        "status": "NEW",
                        "duration": 90,
                        "startTime": "2024-07-20T16:30"
                      }
                    },
                    "name": "Эпик 1",
                    "description": "Тест эпик 1",
                    "id": 1,
                    "status": "NEW",
                    "duration": 120,
                    "startTime": "2024-07-20T15:30"
                  },
                  {
                    "epicId": 1,
                    "name": "1-я подзадача",
                    "description": "описание 1",
                    "id": 2,
                    "status": "NEW",
                    "duration": 30,
                    "startTime": "2024-07-20T15:30"
                  }
                ]""";

        assertEquals(expectedBody, response.body(), "Ошибка сортировки при добавлении задач");

        URI urlDelete = URI.create("http://localhost:8080/TaskManager/tasks/4");
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
                      "2": {
                        "epicId": 1,
                        "name": "1-я подзадача",
                        "description": "описание 1",
                        "id": 2,
                        "status": "NEW",
                        "duration": 30,
                        "startTime": "2024-07-20T15:30"
                      },
                      "3": {
                        "epicId": 1,
                        "name": "2-я подзадача",
                        "description": "описание 2",
                        "id": 3,
                        "status": "NEW",
                        "duration": 90,
                        "startTime": "2024-07-20T16:30"
                      }
                    },
                    "name": "Эпик 1",
                    "description": "Тест эпик 1",
                    "id": 1,
                    "status": "NEW",
                    "duration": 120,
                    "startTime": "2024-07-20T15:30"
                  },
                  {
                    "epicId": 1,
                    "name": "1-я подзадача",
                    "description": "описание 1",
                    "id": 2,
                    "status": "NEW",
                    "duration": 30,
                    "startTime": "2024-07-20T15:30"
                  }
                ]""";
        assertEquals(expectedBodyAfterDelete, response2.body(), "Ошибка сортировки при добавлении задач");
    }
}