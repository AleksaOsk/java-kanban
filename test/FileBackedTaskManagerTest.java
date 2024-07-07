import service.FileBackedTaskManager;
import entities.Epic;
import entities.Subtask;
import entities.Task;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;

public class FileBackedTaskManagerTest {
    // Сохранение пустого файла
    @Test
    void savingAnEmptyFileTest() {
        String title = "id,type,name,status,description,epic";
        try {
            File file = File.createTempFile("test", "csv");
            FileBackedTaskManager fileManager = new FileBackedTaskManager(file);

            String[] lines = Files.readString(file.toPath()).split("\n");
            Assertions.assertEquals(lines.length, 1, "Ошибка загрузки пустого файла");
            Assertions.assertEquals(lines[0], title, "Первая строка не титульная");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // Загрузка из пустого файла
    @Test
    void loadingAnEmptyFileTest() {
        try {
            File file = File.createTempFile("test", "txt");
            FileBackedTaskManager fileManager = new FileBackedTaskManager(file);

            Assertions.assertEquals(fileManager.getAllTasks().size(), 0);
            Assertions.assertEquals(fileManager.getAllEpics().size(), 0);
            Assertions.assertEquals(fileManager.getAllSubtasks().size(), 0);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    //сохранение нескольких задач
    @Test
    void savingTasksTest() {
        try {
            File file = File.createTempFile("test", "txt");
            FileBackedTaskManager fileManager = new FileBackedTaskManager(file);

            Task task1 = new Task("Задача 1", "Описание 1");
            fileManager.createTask(task1);
            Task task2 = new Task("Задача 2", "Описание 2");
            fileManager.createTask(task2);
            Epic epic1 = new Epic("Эпик1", "Описание 1");
            fileManager.createEpic(epic1);
            Subtask subtask1 = new Subtask("Подзадача 1", "...", epic1.getId());
            Subtask subtask2 = new Subtask("Подзадача 2", "...", epic1.getId());
            fileManager.createSubtask(subtask1);
            fileManager.createSubtask(subtask2);

            Assertions.assertEquals(fileManager.getAllTasks().size(), 2, "Количество задач не совпадает");
            Assertions.assertEquals(fileManager.getAllEpics().size(), 1, "Количество эпиков не совпадает");
            Assertions.assertEquals(fileManager.getAllSubtasks().size(), 2, "Количество подзадач не совпадает");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    //загрузкa нескольких задач
    @Test
    void loadingTasksTest() {
        try {
            File file = File.createTempFile("test", "txt");

            try (FileWriter writer = new FileWriter(file)) {

                writer.write("""
                        id,type,name,status,description,epic
                        1,TASK,Задача 1,NEW,Описание задачи,
                        2,EPIC,Эпик 1,NEW,Описание эпика,
                        3,SUBTASK,Подзадача 1,NEW,Описание подзадачи,2""");
            }
            FileBackedTaskManager fileManager = FileBackedTaskManager.loadFromFile(file);
            Assertions.assertEquals(fileManager.getAllTasks().size(), 1, "Количество задач не совпадает");
            Assertions.assertEquals(fileManager.getAllEpics().size(), 1, "Количество эпиков не совпадает");
            Assertions.assertEquals(fileManager.getAllSubtasks().size(), 1, "Количество подзадач не совпадает");

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
