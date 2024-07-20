import entities.Epic;
import entities.Status;
import entities.Subtask;
import entities.Task;
import service.FileBackedTaskManager;
import service.InMemoryTaskManager;

import java.io.File;
import java.time.Duration;
import java.time.LocalDateTime;

public class Main {

    static FileBackedTaskManager manager = new FileBackedTaskManager(new File("saveFile.txt"));


    public static void main(String[] args) {


        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ НОВЫЕ ЗАДАЧИ ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");

        Task task1 = manager.createTask(new Task("1-я задача", "Описание 1-ой задачи",
                LocalDateTime.of(2024, 7, 20, 10, 30), Duration.ofMinutes(30)));
        Task task2 = manager.createTask(new Task("2-я задача", "Описание 2-ой задачи",
                LocalDateTime.of(2024, 7, 20, 10, 10), Duration.ofMinutes(90)));

        Task task3 = manager.createTask(new Task("3-я задача", "Описание 3-ей задачи",
                LocalDateTime.of(2024, 7, 20, 13, 0), Duration.ofMinutes(30)));
        Task task4 = manager.createTask(new Task("4-я задача", "Описание 4-ой задачи",
                LocalDateTime.of(2024, 7, 20, 14, 40), Duration.ofMinutes(20)));

        Epic epicOne = manager.createEpic(new Epic("1-й эпик", "Описание 1-го эпика"));
        Epic epicTwo = manager.createEpic(new Epic("2-й эпик", "Описание 2-го эпика"));

        Subtask subtask1 = manager.createSubtask(new Subtask("1-я подзадача", "Описание 1-ой подзадачи",
                epicOne.getId(), LocalDateTime.of(2024, 7, 20, 15, 30), Duration.ofMinutes(90)));
        Subtask subtask2 = manager.createSubtask(new Subtask("2-я подзадача", "Описание 2-ой подзадачи",
                epicOne.getId(), LocalDateTime.of(2024, 7, 20, 17, 0), Duration.ofMinutes(80)));

        Subtask subtask3 = manager.createSubtask(new Subtask("3-я подзадача", "Описание 3-ей подзадачи",
                epicTwo.getId(), LocalDateTime.of(2024, 7, 20, 18, 30), Duration.ofMinutes(15)));

        printTasks();
        System.out.println("\n");

        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ СТАТУСЫ ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");

        task1.setStatus(Status.IN_PROGRESS);
        manager.updateTask(task1);
        task2.setStatus(Status.DONE);
        manager.updateTask(task2);

        subtask1.setStatus(Status.DONE);
        manager.updateSubtask(subtask1);
        subtask2.setStatus(Status.NEW);
        manager.updateSubtask(subtask2);
        subtask3.setStatus(Status.DONE);
        manager.updateSubtask(subtask3);

        printTasks();
        System.out.println();

        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ ПРОСМОТР ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");

        manager.getTaskById(task1.getId());
        manager.getSubtaskById(subtask2.getId());
        manager.getEpicById(epicTwo.getId());

        printTasks();
        System.out.println();

        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ УДАЛЕНИЕ ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");

        manager.removeEpicById(epicTwo.getId());

        printTasks();
        System.out.println();

        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ ВРЕМЯ ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");

        task3.setStartTime(LocalDateTime.of(2024, 7, 20, 14, 30));
        manager.updateTask(task3);  //не обновится, тк время task3 пересекается task4
        task4.setStartTime(LocalDateTime.of(2024, 7, 20, 22, 30));
        manager.updateTask(task4);  //обновится, тк время НЕ пересекается

        subtask1.setStartTime(LocalDateTime.of(2024, 7, 20, 10, 25));
        subtask1.setDuration(Duration.ofMinutes(25));
        manager.updateSubtask(subtask1); //не обновится, тк время subtask1 пересекается c task1

        printTasks();
        System.out.println();

        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ ПРИОРИТЕТ ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        System.out.println(InMemoryTaskManager.getPrioritizedTasks());

    }

    private static void printTasks() {
        System.out.println("ЗАДАЧИ:");
        for (Task task : manager.getAllTasks()) {
            System.out.println(task);
        }
        System.out.println("ЭПИКИ:");
        for (Epic epic : manager.getAllEpics()) {
            System.out.println(epic);

            for (Subtask task : epic.getSubtasks()) {
                System.out.println("--> " + task);
            }
        }
        System.out.println("ПОДЗАДАЧИ:");
        for (Subtask subtask : manager.getAllSubtasks()) {
            System.out.println(subtask);
        }

        System.out.println("ИСТОРИЯ:");
        for (Task task : manager.getHistory()) {
            System.out.println(task);
        }
    }
}