package TaskTracker;

public class Test {
    private static final Manager manager = new Manager();

    public static void main(String[] args) {
        System.out.println(" НОВЫЕ ЗАДАЧИ ");

        Task taskOne = new Task("1-я задача", "Описание 1-ой задачи");
        Task taskTwo = new Task("2-я задача", "Описание 2-ой задачи");
        manager.newTask(taskOne);
        manager.newTask(taskTwo);

        Epic epicOne = new Epic("1-й эпик", "Описание 1-го эпика");
        manager.newEpic(epicOne);

        Subtask subtaskOne = new Subtask("1-я подзадача",
                "Описание 1-ой подзадачи", epicOne.getId());
        Subtask subtaskTwo = new Subtask("2-я подзадача",
                "Описание 2-ой подзадачи", epicOne.getId());
        manager.newSubtask(subtaskOne);
        manager.newSubtask(subtaskTwo);


        Epic epicTwo = new Epic("2-й эпик", "Описание 2-го эпика");
        manager.newEpic(epicTwo);

        Subtask subtaskThree = new Subtask("3-я подзадача",
                "Описание 2-ей подзадачи", epicTwo.getId());
        manager.newSubtask(subtaskThree);

        printTasks();
        System.out.println();

        System.out.println("СТАТУСЫ");

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

        System.out.println("УДАЛЕНИЕ");

        manager.removeTaskById(taskOne.getId());
        manager.removeEpicById(epicOne.getId());

        printTasks();
    }

    private static void printTasks() {
        System.out.println("Список задач: ");
        System.out.println(manager.allTasks());
        System.out.println("Список эпиков: ");
        System.out.println(manager.allEpics());
        System.out.println("Список подзадач: ");
        System.out.println(manager.allSubtasks());
    }
}
