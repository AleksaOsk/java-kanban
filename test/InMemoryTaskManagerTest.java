import entities.Epic;
import entities.Subtask;
import entities.Task;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.TaskManager;
import util.Managers;

import java.io.IOException;

public class InMemoryTaskManagerTest {

    private TaskManager manager;

    @BeforeEach
    public void init() {
        manager = Managers.getDefault();
    }

    @Test
    public void subtaskCanNotBeEpicToItself() throws IOException {
        Subtask subtask = manager.createSubtask(new Subtask("", "", 0));
        Assertions.assertNull(subtask.getId());
    }

    @Test
    public void checkThatManagerCanCreateAndGiveSubtaskById() throws IOException {
        Epic epic = manager.createEpic(new Epic("", ""));
        Subtask subtask = manager.createSubtask(new Subtask("", "", epic.getId()));
        Assertions.assertNotNull(manager.getSubtaskById(subtask.getId()));
    }

    @Test
    public void checkThatManagerCanCreateAndGiveEpicById() throws IOException {
        Epic epic = manager.createEpic(new Epic("", ""));
        Assertions.assertNotNull(manager.getEpicById(epic.getId()));
    }

    @Test
    public void checkThatManagerCanCreateAndGiveTaskById() throws IOException {
        Task task = manager.createTask(new Task("", ""));
        Assertions.assertNotNull(manager.getTaskById(task.getId()));
    }

    @Test
    public void checkImmutabilityOfEpicWhenCreatedValueChanged() throws IOException {
        Epic created = manager.createEpic(new Epic("a", ""));
        created.setName("b");
        Assertions.assertNotEquals(created.getName(), manager.getEpicById(created.getId()).getName());
    }

    @Test
    public void checkImmutabilityOfEpicWhenSourceChanged() throws IOException {
        Epic source = new Epic("a", "");
        Epic created = manager.createEpic(source);
        source.setName("b");
        Assertions.assertNotEquals(source.getName(), manager.getEpicById(created.getId()).getName());
    }

    @Test
    public void checkImmutabilityOfSubtaskWhenCreatedValueChanged() throws IOException {
        Epic epic = manager.createEpic(new Epic("", ""));
        Subtask created = manager.createSubtask(new Subtask("a", "", epic.getId()));
        created.setName("b");
        Assertions.assertNotEquals(created.getName(), manager.getSubtaskById(created.getId()).getName());
    }

    @Test
    public void checkImmutabilityOfSubtaskWhenSourceChanged() throws IOException {
        Epic epic = manager.createEpic(new Epic("", ""));
        Subtask source = new Subtask("a", "", epic.getId());
        Subtask created = manager.createSubtask(source);
        source.setName("b");
        Assertions.assertNotEquals(source.getName(), manager.getSubtaskById(created.getId()).getName());
    }

    @Test
    public void checkImmutabilityOfTaskWhenCreatedValueChanged() throws IOException {
        Task created = manager.createTask(new Task("a", ""));
        created.setName("b");
        Assertions.assertNotEquals(created.getName(), manager.getTaskById(created.getId()).getName());
    }

    @Test
    public void checkImmutabilityOfTaskWhenSourceChanged() throws IOException {
        Task source = new Task("a", "");
        Task created = manager.createTask(source);
        source.setName("b");
        Assertions.assertNotEquals(source.getName(), manager.getTaskById(created.getId()).getName());
    }
}