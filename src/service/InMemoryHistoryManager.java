package service;

import model.Task;

import java.util.ArrayList;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {

    private final List<Task> historyList = new ArrayList<>();
    private static final int NUMBER_OF_VIEWS = 10;

    @Override
    public List<Task> getHistory() {
        if (!historyList.isEmpty()) {
            return List.copyOf(historyList);
        } else {
            System.out.println("History is empty");
            List<Task> emptyList = new ArrayList<>();
            return emptyList;
        }
    }

    @Override
    public void add(Task task) {
        if (historyList.size() == NUMBER_OF_VIEWS) {
            historyList.remove(0); // Я до ревью пробывал воспользоваться методом removeFirst()
            // но тогда получается ошибка : cannot find symbol
            //  symbol:   method removeFirst()
            // хотя до запуска intelij не распознает эту ошибку
            historyList.add(task);
        } else {
            historyList.add(task);
        }
    }
}