package test;

import entities.Epic;
import entities.Subtask;
import entities.Task;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.TaskManager;
import util.Managers;

public class InMemoryHistoryManagerTest {

    private TaskManager manager;

    @BeforeEach
    public void init() {
        manager = Managers.getDefault();
    }

    @Test
    public void checkThatEpicDoesNotChangeInHistory() {
        Epic epic = manager.createEpic(new Epic("a", ""));
        manager.getEpicById(epic.getId());
        epic.setName("b");
        manager.updateEpic(epic);

        Assertions.assertNotEquals(
                manager.getHistory().getFirst().getName(),
                manager.getEpicById(epic.getId()).getName());
    }

    @Test
    public void checkThatSubtaskDoesNotChangeInHistory() {
        Epic epic = manager.createEpic(new Epic("", ""));
        Subtask subtask = manager.createSubtask(new Subtask("a", "", epic.getId()));
        manager.getSubtaskById(subtask.getId());
        subtask.setName("b");
        manager.updateSubtask(subtask);

        Assertions.assertNotEquals(
                manager.getHistory().getFirst().getName(),
                manager.getSubtaskById(subtask.getId()).getName());
    }

    @Test
    public void checkThatTaskDoesNotChangeInHistory() {
        Task task = manager.createTask(new Task("a", ""));
        manager.getTaskById(task.getId());
        task.setName("b");
        manager.updateTask(task);

        Assertions.assertNotEquals(
                manager.getHistory().getFirst().getName(),
                manager.getTaskById(task.getId()).getName());
    }
}