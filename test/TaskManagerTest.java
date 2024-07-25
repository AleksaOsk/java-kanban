import entities.Epic;
import entities.Status;
import entities.Subtask;
import entities.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.InMemoryTaskManager;
import service.TaskManager;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.*;

public abstract class TaskManagerTest<T extends TaskManager> {
    protected T taskManager;

    protected abstract T createTaskManager();

    @BeforeEach
    public void manager() {
        taskManager = createTaskManager();
    }

    @Test
    public void addTaskTest() {
        Task task = new Task("111", "222", LocalDateTime.now(), Duration.ofMinutes(2));
        taskManager.createTask(task);
        int id = task.getId();
        Task taskSave = taskManager.getTaskById(id);

        assertNotNull(taskSave, "Задача не найдена.");
        assertEquals(task, taskSave, "Задачи не совпадают.");

        final List<Task> tasks = taskManager.getAllTasks();
        assertNotNull(tasks, "Задачи не возвращаются.");
        assertEquals(1, tasks.size(), "Неверное количество задач.");
        assertEquals(task, tasks.get(0), "Задачи не совпадают.");
    }

    @Test
    public void addEpicTest() {
        Epic epic = new Epic("111", "111");
        taskManager.createEpic(epic);
        int id = epic.getId();
        Epic epicSave = taskManager.getEpicById(id);

        assertNotNull(epicSave, "Задача не найдена.");
        assertEquals(epic, epicSave);

        final List<Epic> epics = taskManager.getAllEpics();
        assertNotNull(epics, "Задачи не возвращаются.");
        assertEquals(1, epics.size(), "Неверное количество задач.");
        assertEquals(epic, epics.get(0), "Задачи не совпадают.");
    }

    @Test
    public void getTasksTest() {
        Task task1 = new Task("111", "111", LocalDateTime.of(2024, 7, 20, 15, 30), Duration.ofMinutes(30));
        taskManager.createTask(task1);
        Task task2 = new Task("222", "222", LocalDateTime.of(2024, 7, 20, 16, 30), Duration.ofMinutes(90));
        taskManager.createTask(task2);

        List<Task> comparable = new ArrayList<>();
        comparable.add(task1);
        comparable.add(task2);

        assertEquals(comparable, taskManager.getAllTasks(), "Ошибка в возврате списка всех задач");
    }

    @Test
    public void getSubtasksTest() {
        Epic epic = new Epic("111", "111");
        taskManager.createEpic(epic);
        Subtask subtask1 = new Subtask("222", "222", epic.getId(), LocalDateTime.of(2024, 7, 20, 15, 30), Duration.ofMinutes(30));
        taskManager.createSubtask(subtask1);
        Subtask subtask2 = new Subtask("333", "333", epic.getId(), LocalDateTime.of(2024, 7, 20, 16, 30), Duration.ofMinutes(90));
        taskManager.createSubtask(subtask2);
        taskManager.getAllSubtasksOfEpic(epic);

        List<Subtask> comparable = new ArrayList<>();
        comparable.add(subtask1);
        comparable.add(subtask2);

        List<Subtask> actual = taskManager.getAllSubtasks();

        assertEquals(comparable, actual, "Ошибка в возврате списка всех подзадач");
    }

    @Test
    public void getEpicsTest() {
        Epic epic1 = new Epic("111", "111");
        taskManager.createEpic(epic1);
        Epic epic2 = new Epic("222", "222");
        taskManager.createEpic(epic2);

        List<Epic> comparable = new ArrayList<>();
        comparable.add(epic1);
        comparable.add(epic2);

        List<Epic> actual = taskManager.getAllEpics();

        assertEquals(comparable, actual, "Ошибка в возврате списка всех эпиков");
    }

    @Test
    public void clearTasksTest() {
        Task task1 = new Task("111", "111", LocalDateTime.of(2024, 7, 20, 15, 30), Duration.ofMinutes(30));
        taskManager.createTask(task1);
        Task task2 = new Task("222", "222", LocalDateTime.of(2024, 7, 20, 16, 30), Duration.ofMinutes(90));
        taskManager.createTask(task2);

        taskManager.removeAllTasks();
        int actual2 = taskManager.getAllTasks().size();
        assertEquals(0, actual2, "Не все задачи удалены");
    }

    @Test
    void clearSubTasksTest() {
        Epic epic = new Epic("111", "111");
        taskManager.createEpic(epic);
        Subtask subtask1 = new Subtask("222", "222", epic.getId(), LocalDateTime.of(2024, 7, 20, 15, 30), Duration.ofMinutes(30));
        taskManager.createSubtask(subtask1);
        Subtask subtask2 = new Subtask("333", "333", epic.getId(), LocalDateTime.of(2024, 7, 20, 16, 30), Duration.ofMinutes(90));
        taskManager.createSubtask(subtask2);
        subtask2.setStartTime(LocalDateTime.of(2024, 7, 20, 20, 30));
        taskManager.updateSubtask(subtask2);

        taskManager.removeAllSubtasks();
        int actual2 = taskManager.getAllSubtasks().size();
        assertEquals(0, actual2, "Не все задачи удалены");
        assertNull(epic.getStartTime(), "При удалении всех подзадач, сохраняется стартовое время эпика");
        assertEquals(Duration.ZERO, epic.getDuration(), "При удалении всех подзадач, сохраняется продолжительность эпика");
    }

    @Test
    void clearEpicTest() {
        Epic epic1 = new Epic("111", "111");
        taskManager.createEpic(epic1);
        Epic epic2 = new Epic("222", "222");
        taskManager.createEpic(epic2);


        taskManager.removeAllEpics();
        int actual2 = taskManager.getAllEpics().size();
        assertEquals(0, actual2, "Не все задачи удалены");
    }

    @Test
    void removeTaskTest() {
        Task task1 = new Task("111", "111", LocalDateTime.of(2024, 7, 20, 15, 30), Duration.ofMinutes(30));
        taskManager.createTask(task1);
        Task task2 = new Task("222", "222", LocalDateTime.of(2024, 7, 20, 16, 30), Duration.ofMinutes(90));
        taskManager.createTask(task2);

        taskManager.removeTaskById(task2.getId());
        assertNull(taskManager.getTaskById(task2.getId()), "Задача по ID не удалена");
    }

    @Test
    void removeEpicTest() {
        Epic epic1 = new Epic("111", "111");
        taskManager.createEpic(epic1);
        Epic epic2 = new Epic("222", "222");
        taskManager.createEpic(epic2);

        int id = epic1.getId();

        taskManager.removeEpicById(id);
        assertNull(taskManager.getEpicById(epic1.getId()), "Задача по ID не удалена");
    }

    @Test
    void getTaskIdTest() {
        Task task1 = new Task("111", "111", LocalDateTime.of(2024, 7, 20, 15, 30), Duration.ofMinutes(30));
        taskManager.createTask(task1);
        Task task2 = new Task("222", "222", LocalDateTime.of(2024, 7, 20, 16, 30), Duration.ofMinutes(90));
        taskManager.createTask(task2);

        assertEquals(task2, taskManager.getTaskById(task2.getId()), "Ошибка при получении задачи по Id");
    }

    @Test
    void getSubtaskIdTest() {
        Epic epic = new Epic("111", "111");
        taskManager.createEpic(epic);
        Subtask subtask1 = new Subtask("222", "222", epic.getId(), LocalDateTime.of(2024, 7, 20, 15, 30), Duration.ofMinutes(30));
        taskManager.createSubtask(subtask1);
        Subtask subtask2 = new Subtask("333", "333", epic.getId(), LocalDateTime.of(2024, 7, 20, 16, 30), Duration.ofMinutes(90));
        taskManager.createSubtask(subtask2);

        assertEquals(subtask2, taskManager.getSubtaskById(subtask2.getId()), "Ошибка при получении подзадачи по Id");
    }

    @Test
    void getEpicIdTest() {
        Epic epic1 = new Epic("111", "111");
        taskManager.createEpic(epic1);
        Epic epic2 = new Epic("222", "222");
        taskManager.createEpic(epic2);


        assertEquals(epic2, taskManager.getEpicById(epic2.getId()), "Ошибка при получении эпика по Id");
    }

    @Test
    void updateTaskTest() {
        Task task = new Task("111", "111", LocalDateTime.of(2024, 7, 20, 15, 30), Duration.ofMinutes(30));
        taskManager.createTask(task);
        task = new Task("222", "222", LocalDateTime.of(2024, 7, 20, 16, 30), Duration.ofMinutes(90));
        taskManager.updateTask(task);

        assertNotNull(task, "Задача пустая");
        assertNotEquals("111", task.getName(), "Задача не обновилась");
    }

    @Test
    void updateEpicTest() {
        Epic epic = new Epic("111", "111");
        taskManager.createEpic(epic);
        epic = new Epic("222", "222");
        taskManager.updateEpic(epic);

        assertNotNull(epic, "Задача пустая");
        assertNotEquals("111", epic.getDescription());
    }

    @Test
    void updateSubTaskTest() {
        Epic epic = new Epic("111", "111");
        taskManager.createEpic(epic);
        Subtask subtask = new Subtask("222", "222", epic.getId(), LocalDateTime.of(2024, 7, 20, 16, 30), Duration.ofMinutes(90));
        taskManager.createSubtask(subtask);
        LocalDateTime startTimeExpected = epic.getStartTime();
        LocalDateTime endTimeExpected = epic.getEndTime();
        Duration durationExpected = epic.getDuration();

        subtask.setStatus(Status.DONE);
        subtask.setStartTime(LocalDateTime.of(2024, 7, 20, 17, 0));
        subtask.setDuration(Duration.ofMinutes(34));
        taskManager.updateSubtask(subtask);

        assertNotNull(subtask, "Задача пустая");
        assertNotEquals(Status.NEW, subtask.getStatus());
        assertNotEquals(startTimeExpected, subtask.getStartTime(), "Стартовое время не обновилось");
        assertNotEquals(endTimeExpected, subtask.getEndTime(), "Время окончания не обновилось");
        assertNotEquals(durationExpected, subtask.getDuration(), "Продолжительность выполнения не обновилась");
    }

    @Test
    void getPrioritizedTasksTest() {
        Task task1 = new Task("111", "111", LocalDateTime.of(2024, 7, 20, 15, 30), Duration.ofMinutes(30));
        taskManager.createTask(task1);
        Task task2 = new Task("222", "222", LocalDateTime.of(2024, 7, 20, 16, 30), Duration.ofMinutes(30));
        taskManager.createTask(task2);
        Epic epic = new Epic("333", "333");
        taskManager.createEpic(epic);
        Subtask subtask3 = new Subtask("444", "444", epic.getId(), LocalDateTime.of(2024, 7, 20, 17, 30), Duration.ofMinutes(30));
        taskManager.createSubtask(subtask3);

        TreeSet<Task> expected = new TreeSet<>(Comparator.comparing(Task::getStartTime));
        expected.add(task1);
        expected.add(task2);
        expected.add(subtask3);

        TreeSet<Task> sortTasks = InMemoryTaskManager.getPrioritizedTasks();
        assertEquals(expected, sortTasks, "Ошибка сортировки при добавлении задач");

        taskManager.removeTaskById(task1.getId());
        sortTasks = InMemoryTaskManager.getPrioritizedTasks();
        expected.removeFirst();
        assertEquals(expected, sortTasks, "Ошибка сортировки при удалении задач по Id");

        taskManager.removeAllEpics();
        sortTasks = InMemoryTaskManager.getPrioritizedTasks();
        expected.removeLast();
        assertEquals(expected, sortTasks, "Ошибка сохранения подзадач при удалении всех эпиков");

        Epic epic2 = new Epic("555", "555");
        taskManager.createEpic(epic2);
        Subtask subtask1 = new Subtask("666", "666", epic.getId(), LocalDateTime.of(2024, 7, 20, 17, 30), Duration.ofMinutes(30));
        taskManager.createSubtask(subtask1);
        expected.add(subtask1);

        taskManager.removeEpicById(epic2.getId());
        sortTasks = InMemoryTaskManager.getPrioritizedTasks();
        expected.removeLast();
        assertEquals(expected, sortTasks, "Ошибка сохранения подзадач при удалени эпиков по id");
    }

    @Test
    void getHistoryTest() {
        Task task1 = new Task("111", "111", LocalDateTime.of(2024, 7, 20, 15, 30), Duration.ofMinutes(30));
        taskManager.createTask(task1);
        Task task2 = new Task("222", "222", LocalDateTime.of(2024, 7, 20, 16, 30), Duration.ofMinutes(30));
        taskManager.createTask(task2);
        Epic epic = new Epic("333", "333");
        taskManager.createEpic(epic);
        Subtask subtask = new Subtask("444", "444", epic.getId(), LocalDateTime.of(2024, 7, 20, 17, 30), Duration.ofMinutes(30));
        taskManager.createSubtask(subtask);

        taskManager.getTaskById(task1.getId());
        taskManager.getTaskById(task2.getId());
        taskManager.getEpicById(epic.getId());
        taskManager.getSubtaskById(subtask.getId());

        List<Task> expected = new ArrayList<>();
        expected.add(task1);
        expected.add(task2);
        expected.add(epic);
        expected.add(subtask);

        assertEquals(expected, taskManager.getHistory());
    }
}