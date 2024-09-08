import entities.Task;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;

public class TaskTest {

    @Test
    public void checkTasksEqualWhenIdsEqual() {
        Task taskOne = new Task("", "", null, Duration.ofMinutes(0));
        Task taskTwo = new Task("", "", null, Duration.ofMinutes(0));
        taskOne.setId(1);
        taskTwo.setId(1);
        Assertions.assertEquals(taskOne, taskTwo);
    }
}