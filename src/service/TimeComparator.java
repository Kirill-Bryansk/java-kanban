package service;

import model.Task;

import java.util.Comparator;
import java.util.Optional;

public class TimeComparator implements Comparator<Task> {
    @Override
    public int compare(Task task1, Task task2) {
        return Comparator.comparing(Task::getStartTime, Comparator.nullsLast(Comparator.naturalOrder()))
                .compare(task1, task2);
    }
}
