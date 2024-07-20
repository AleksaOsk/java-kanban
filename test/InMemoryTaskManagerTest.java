import entities.Epic;
import entities.Status;
import entities.Subtask;
import entities.Task;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.InMemoryTaskManager;
import service.TaskManager;
import util.Managers;

import java.time.Duration;
import java.time.LocalDateTime;

public class InMemoryTaskManagerTest extends TaskManagerTest<InMemoryTaskManager>{

    private TaskManager manager;

    @BeforeEach
    public void init() {
        manager = Managers.getDefault();
    }

    @Test
    public void subtaskCanNotBeEpicToItself() {
        Subtask subtask = manager.createSubtask(new Subtask("", "", 0,null, Duration.ofMinutes(0)));
        Assertions.assertNull(subtask.getId());
    }

    @Test
    public void checkThatManagerCanCreateAndGiveSubtaskById() {
        Epic epic = manager.createEpic(new Epic("", ""));
        Subtask subtask = manager.createSubtask(new Subtask("", "", epic.getId(),
                LocalDateTime.of(2024,7,20,12,0),Duration.ofMinutes(30)));
        Assertions.assertNotNull(manager.getSubtaskById(subtask.getId()));
    }

    @Test
    public void checkThatManagerCanCreateAndGiveEpicById() {
        Epic epic = manager.createEpic(new Epic("", ""));
        Assertions.assertNotNull(manager.getEpicById(epic.getId()));
    }

    @Test
    public void checkThatManagerCanCreateAndGiveTaskById() {
        Task task = manager.createTask(new Task("", "",
                LocalDateTime.of(2024,7,20,13,0),Duration.ofMinutes(30)));
        Assertions.assertNotNull(manager.getTaskById(task.getId()));
    }

    @Test
    public void checkImmutabilityOfEpicWhenCreatedValueChanged() {
        Epic created = manager.createEpic(new Epic("a", ""));
        created.setName("b");
        Assertions.assertNotEquals(created.getName(), manager.getEpicById(created.getId()).getName());
    }

    @Test
    public void checkImmutabilityOfEpicWhenSourceChanged() {
        Epic source = new Epic("a", "");
        Epic created = manager.createEpic(source);
        source.setName("b");
        Assertions.assertNotEquals(source.getName(), manager.getEpicById(created.getId()).getName());
    }

    @Test
    public void checkImmutabilityOfSubtaskWhenCreatedValueChanged() {
        Epic epic = manager.createEpic(new Epic("", ""));
        Subtask created = manager.createSubtask(new Subtask("a", "", epic.getId(),
                LocalDateTime.of(2024,7,20,14,0),Duration.ofMinutes(30)));
        created.setName("b");
        Assertions.assertNotEquals(created.getName(), manager.getSubtaskById(created.getId()).getName());
    }

    @Test
    public void checkImmutabilityOfSubtaskWhenSourceChanged() {
        Epic epic = manager.createEpic(new Epic("", ""));
        Subtask source = new Subtask("a", "", epic.getId(),
                LocalDateTime.of(2024,7,20,15,0),Duration.ofMinutes(30));
        Subtask created = manager.createSubtask(source);
        source.setName("b");
        Assertions.assertNotEquals(source.getName(), manager.getSubtaskById(created.getId()).getName());
    }

    @Test
    public void checkImmutabilityOfTaskWhenCreatedValueChanged() {
        Task created = manager.createTask(new Task("a", "",
                LocalDateTime.of(2024,7,20,16,0),Duration.ofMinutes(30)));
        created.setName("b");
        Assertions.assertNotEquals(created.getName(), manager.getTaskById(created.getId()).getName());
    }

    @Test
    public void checkImmutabilityOfTaskWhenSourceChanged() {
        Task source = new Task("a", "",
                LocalDateTime.of(2024,7,20,17,0),Duration.ofMinutes(30));
        Task created = manager.createTask(source);
        source.setName("b");
        Assertions.assertNotEquals(source.getName(), manager.getTaskById(created.getId()).getName());
    }

    @Test
    public void checkEpicWhenAllSubtaskIsNew(){
        Epic epic = manager.createEpic(new Epic("Эпик", "Описание эпика"));

        Subtask subtask1 = manager.createSubtask(new Subtask("1-я подзадача", "Описание 1-ой подзадачи",
                epic.getId(), LocalDateTime.of(2024,7,20,15,30), Duration.ofMinutes(90)));
        Subtask subtask2 = manager.createSubtask(new Subtask("2-я подзадача", "Описание 2-ой подзадачи",
                epic.getId(), LocalDateTime.of(2024,7,20,17,0), Duration.ofMinutes(80)));
        Assertions.assertNotEquals("NEW",epic.getStatus());
    }

    @Test
    public void checkEpicWhenAllSubtaskIsDone(){
        Epic epic = manager.createEpic(new Epic("Эпик", "Описание эпика"));

        Subtask subtask1 = manager.createSubtask(new Subtask("1-я подзадача", "Описание 1-ой подзадачи",
                epic.getId(), LocalDateTime.of(2024,7,20,15,30), Duration.ofMinutes(90)));
        Subtask subtask2 = manager.createSubtask(new Subtask("2-я подзадача", "Описание 2-ой подзадачи",
                epic.getId(), LocalDateTime.of(2024,7,20,17,0), Duration.ofMinutes(80)));

        subtask1.setStatus(Status.DONE);
        subtask2.setStatus(Status.DONE);

        Assertions.assertNotEquals("DONE",epic.getStatus());
    }

    @Test
    public void checkEpicWhenAllSubtaskIsNewAndDone(){
        Epic epic = manager.createEpic(new Epic("Эпик", "Описание эпика"));

        Subtask subtask1 = manager.createSubtask(new Subtask("1-я подзадача", "Описание 1-ой подзадачи",
                epic.getId(), LocalDateTime.of(2024,7,20,15,30), Duration.ofMinutes(90)));
        Subtask subtask2 = manager.createSubtask(new Subtask("2-я подзадача", "Описание 2-ой подзадачи",
                epic.getId(), LocalDateTime.of(2024,7,20,17,0), Duration.ofMinutes(80)));

        subtask1.setStatus(Status.DONE);

        Assertions.assertNotEquals("IN_PROGRESS",epic.getStatus());
    }

    @Test
    public void checkEpicWhenAllSubtaskIsInProgress(){
        Epic epic = manager.createEpic(new Epic("Эпик", "Описание эпика"));

        Subtask subtask1 = manager.createSubtask(new Subtask("1-я подзадача", "Описание 1-ой подзадачи",
                epic.getId(), LocalDateTime.of(2024,7,20,15,30), Duration.ofMinutes(90)));
        Subtask subtask2 = manager.createSubtask(new Subtask("2-я подзадача", "Описание 2-ой подзадачи",
                epic.getId(), LocalDateTime.of(2024,7,20,17,0), Duration.ofMinutes(80)));

        subtask1.setStatus(Status.IN_PROGRESS);
        subtask2.setStatus(Status.IN_PROGRESS);

        Assertions.assertNotEquals("IN_PROGRESS",epic.getStatus());
    }

    @Override
    protected InMemoryTaskManager createTaskManager() {
        return new InMemoryTaskManager();
    }
}