package model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class SubtaskTest {

    private Subtask subtaskFirst;
    private Subtask subtaskSecond;

    @Test
    public void shouldReturnTrue_WhenSubtaskHaveSomeId() {
        subtaskFirst = new Subtask("Подзадача №1", "Делать подзадачу №1", 1);
        subtaskSecond = new Subtask("Подзадача №2", "Делать подзадачу №2", 1);
        subtaskFirst.setId(1);
        subtaskSecond.setId(1);
        assertTrue(subtaskFirst.equals(subtaskSecond), "Подзадачи по id не равны");
    }
}
