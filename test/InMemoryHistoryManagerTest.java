import entities.Task;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.TaskManager;
import util.Managers;

public class InMemoryHistoryManagerTest {

    private TaskManager taskManager;

    @BeforeEach
    public void init() {
        taskManager = Managers.getDefault();
    }

    @Test
    public void chekWhatOldVersionTaskDeleteToNewViewing() {
        Task task = new Task("1", "");
        taskManager.createTask(task);
        taskManager.getTaskById(task.getId());
        task.setName("2");
        taskManager.updateTask(task);
        taskManager.getTaskById(task.getId());
        Assertions.assertEquals(taskManager.getHistory().getFirst().getName(), task.getName());
    }

    @Test
    public void chekWhatByDeleteTaskAndDeleteFromHistory() {
        Task task = new Task("1", "");
        taskManager.createTask(task);
        taskManager.getTaskById(task.getId());
        Task task2 = new Task("2", "");
        taskManager.createTask(task2);
        taskManager.getTaskById(task2.getId());
        taskManager.removeTaskById(task.getId());
        Assertions.assertNotEquals(taskManager.getHistory().getFirst(), task);
    }

    @Test
    public void chekWhatTaskAddToHistory() {
        Task task = new Task("1", "");
        taskManager.createTask(task);
        taskManager.getTaskById(task.getId());
        Assertions.assertEquals(taskManager.getHistory().getFirst(), task);
    }
}