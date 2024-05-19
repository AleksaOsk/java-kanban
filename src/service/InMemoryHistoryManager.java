package service;

import entities.Task;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {

    private static final int HISTORY_LENGTH = 10;

    private final List<Task> viewHistory = new LinkedList<>();

    @Override
    public void add(Task task) {
        if (task != null) {
            if (viewHistory.size() > HISTORY_LENGTH) {
                viewHistory.removeFirst();
                viewHistory.add(new Task(task));
            }
        }
    }

    @Override
    public List<Task> getHistory() {
        return deepCopyViewHistory();
    }

    private List<Task> deepCopyViewHistory() {
        List<Task> list = new ArrayList<>();

        for (Task task: viewHistory) {
            list.add(new Task(task));
        }

        return list;
    }
}