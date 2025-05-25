package model;


import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class EpicTest {
    private Epic epicFirst;
    private Epic epicSecond;

    @Test
    public void shouldReturnTrueTrue_WhenEpicSomeId() {
        epicFirst = new Epic("Эпик №1", "Делать для эпика №1");
        epicSecond = new Epic("Эпик №2", "Делать для эпика №2");

        epicFirst.setId(1);
        epicSecond.setId(1);

        assertTrue(epicFirst.equals(epicSecond), "Эпики по id не равны");
    }
}
