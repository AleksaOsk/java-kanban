import entities.Subtask;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;

public class SubtaskTest {

    @Test
    public void checkSubtasksEqualWhenIdsEqual() {
        Subtask subtaskOne = new Subtask("", "", 0, null, Duration.ofMinutes(0));
        Subtask subtaskTwo = new Subtask("", "", 0, null, Duration.ofMinutes(0));
        subtaskOne.setId(1);
        subtaskTwo.setId(1);
        Assertions.assertEquals(subtaskOne, subtaskTwo);
    }
}