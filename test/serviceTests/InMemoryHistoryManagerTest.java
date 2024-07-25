package serviceTests;

import entities.Task;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.TaskManager;
import util.Managers;

import java.time.Duration;
import java.time.LocalDateTime;

public class InMemoryHistoryManagerTest {

    private TaskManager taskManager;

    @BeforeEach
    public void init() {
        taskManager = Managers.getDefault();
    }

    @Test
    public void chekWhatOldVersionTaskDeleteToNewViewing() {
        Task task = new Task("1", "",
                LocalDateTime.of(2024, 7, 20, 12, 0), Duration.ofMinutes(30));
        taskManager.createTask(task);
        taskManager.getTaskById(task.getId());
        task.setName("2");
        taskManager.updateTask(task);
        taskManager.getTaskById(task.getId());
        Assertions.assertEquals(taskManager.getHistory().getFirst().getName(), task.getName());
    }

    @Test
    public void chekWhatByDeleteTaskAndDeleteFromHistory() {
        Task task = new Task("1", "",
                LocalDateTime.of(2024, 7, 20, 12, 0), Duration.ofMinutes(30));
        taskManager.createTask(task);
        taskManager.getTaskById(task.getId());
        Task task2 = new Task("2", "",
                LocalDateTime.of(2024, 7, 20, 13, 0), Duration.ofMinutes(30));
        taskManager.createTask(task2);
        taskManager.getTaskById(task2.getId());
        taskManager.removeTaskById(task.getId());
        Assertions.assertNotEquals(taskManager.getHistory().getFirst(), task);
    }

    @Test
    public void chekWhatTaskAddToHistory() {
        Task task = new Task("1", "",
                LocalDateTime.of(2024, 7, 20, 14, 0), Duration.ofMinutes(30));
        taskManager.createTask(task);
        taskManager.getTaskById(task.getId());
        Assertions.assertEquals(taskManager.getHistory().getFirst(), task);
    }
}