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
import java.time.Duration;
import java.time.LocalDateTime;

public class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTaskManager>{
    // Сохранение пустого файла
    @Test
    void savingAnEmptyFileTest() {
        String title = "id,type,name,status,description,startTime,endTime,duration,epic";
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

            Task task1 = new Task("Задача 1", "Описание 1",
                    LocalDateTime.of(2024,7,20,12,0), Duration.ofMinutes(30));
            fileManager.createTask(task1);
            Task task2 = new Task("Задача 2", "Описание 2",
                    LocalDateTime.of(2024,7,20,13,0),Duration.ofMinutes(30));
            fileManager.createTask(task2);
            Epic epic1 = new Epic("Эпик1", "Описание 1");
            fileManager.createEpic(epic1);
            Subtask subtask1 = new Subtask("Подзадача 1", "...", epic1.getId(),
                    LocalDateTime.of(2024,7,20,14,0),Duration.ofMinutes(30));
            Subtask subtask2 = new Subtask("Подзадача 2", "...", epic1.getId(),
                    LocalDateTime.of(2024,7,20,15,0),Duration.ofMinutes(30));
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
                        id,type,name,status,description,startTime,endTime,duration,epic
                        1,TASK,1-я задача,IN_PROGRESS,Описание 1-ой задачи, 10:30:01-20.07.2024 , 11:00:01-20.07.2024 ,PT30M
                        2,TASK,3-я задача,NEW,Описание 3-ей задачи, 13:00:00-20.07.2024 , 13:30:00-20.07.2024 ,PT30M
                        3,EPIC,1-й эпик,NEW,Описание 1-го эпика, 2024-07-20T15:30,2024-07-20T18:20,PT2H50M
                        4,SUBTASK,1-я подзадача,NEW,Описание 1-ой подзадачи, 15:30:00-20.07.2024 , 17:00:00-20.07.2024 ,PT1H30M,3""");
            }
            FileBackedTaskManager fileManager = FileBackedTaskManager.loadFromFile(file);
            Assertions.assertEquals(fileManager.getAllTasks().size(), 2, "Количество задач не совпадает");
            Assertions.assertEquals(fileManager.getAllEpics().size(), 1, "Количество эпиков не совпадает");
            Assertions.assertEquals(fileManager.getAllSubtasks().size(), 1, "Количество подзадач не совпадает");

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected FileBackedTaskManager createTaskManager() {
        return new FileBackedTaskManager(new File("test.csv"));
    }
}
