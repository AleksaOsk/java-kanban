import entities.Epic;
import entities.Subtask;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;

public class EpicTest {

    @Test
    public void checkEpicsEqualWhenIdsEqual() {
        Epic epicOne = new Epic("", "");
        Epic epicTwo = new Epic("", "");
        epicOne.setId(1);
        epicTwo.setId(1);
        Assertions.assertEquals(epicOne, epicTwo);
    }
}