import entities.Epic;
import entities.Status;
import entities.Subtask;
import entities.Task;
import service.TaskManager;
import util.Managers;

public class Main {

    private static final TaskManager manager = Managers.getDefault();

    public static void main(String[] args) {
        System.out.println("~ НОВЫЕ ЗАДАЧИ ~");

        Task taskOne = manager.createTask(new Task("1-я задача", "Описание 1-ой задачи"));
        Task taskTwo = manager.createTask(new Task("2-я задача", "Описание 2-ой задачи"));

        Epic epicOne = manager.createEpic(new Epic("1-й эпик", "Описание 1-го эпика"));
        Epic epicTwo = manager.createEpic(new Epic("2-й эпик", "Описание 2-го эпика"));

        Subtask subtaskOne = manager.createSubtask(new Subtask("1-я подзадача",
                "Описание 1-ой подзадачи", epicOne.getId()));
        Subtask subtaskTwo = manager.createSubtask(new Subtask("2-я подзадача",
                "Описание 2-ой подзадачи", epicOne.getId()));

        Subtask subtaskThree = manager.createSubtask(new Subtask("3-я подзадача",
                "Описание 2-ей подзадачи", epicTwo.getId()));

        printTasks();
        System.out.println();

        System.out.println("~ СТАТУСЫ ~");

        taskOne.setStatus(Status.IN_PROGRESS);
        manager.updateTask(taskOne);
        taskTwo.setStatus(Status.DONE);
        manager.updateTask(taskTwo);

        subtaskOne.setStatus(Status.DONE);
        manager.updateSubtask(subtaskOne);
        subtaskTwo.setStatus(Status.NEW);
        manager.updateSubtask(subtaskTwo);
        subtaskThree.setStatus(Status.DONE);
        manager.updateSubtask(subtaskThree);

        printTasks();
        System.out.println();

        System.out.println("~ УДАЛЕНИЕ ~");

        manager.removeEpicById(epicOne.getId());

        manager.getTaskById(taskOne.getId());
        manager.getTaskById(taskTwo.getId());

        printTasks();
    }

    private static void printTasks() {
        System.out.println("Задачи:");
        for (Task task : manager.getAllTasks()) {
            System.out.println(task);
        }
        System.out.println("Эпики:");
        for (Epic epic : manager.getAllEpics()) {
            System.out.println(epic);

            for (Subtask task : epic.getSubtasks()) {
                System.out.println("--> " + task);
            }
        }
        System.out.println("Подзадачи:");
        for (Subtask subtask : manager.getAllSubtasks()) {
            System.out.println(subtask);
        }

        System.out.println("История:");
        for (Task task : manager.getHistory()) {
            System.out.println(task);
        }
    }
}