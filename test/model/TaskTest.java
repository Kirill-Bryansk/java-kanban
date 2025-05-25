package model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class TaskTest {
    private Task taskFirst;
    private Task taskSecond;

    @Test
    public void shouldReturnTrue_WhenTasksHaveTheSameId() {
        taskFirst = new Task("Задача №1", "Делать задачу №1");
        taskSecond = new Task("Задача №2", "Делать задачу №2");
        taskFirst.setId(1);
        taskSecond.setId(1);
        assertTrue(taskFirst.equals(taskSecond), "Задачи по id не равны");
    }
}


