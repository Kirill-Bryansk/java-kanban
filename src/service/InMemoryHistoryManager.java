package service;

import model.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryHistoryManager implements HistoryManager {
    private final Map<Integer, Node> historyMap = new HashMap<>();
    private Node head;
    private Node tail;

    @Override
    public void add(Task task) {
        if (historyMap.containsKey(task.getId())) {
            removeNode(task.getId());
        } else {
            linkLast(task);
            Node newNode = tail;
            historyMap.put(task.getId(), newNode);
        }
    }

    @Override
    public List<Task> getHistory() {
        return getTasks();
    }

    @Override
    public void remove(int id) {
        removeNode(id);
    }

    private void linkLast(Task task) {
        final Node oldTail = tail;
        final Node newNode = new Node(oldTail, task, null);
        tail = newNode;
        if (oldTail == null) {
            head = newNode;
        } else {
            oldTail.next = newNode;
        }
    }

    private List<Task> getTasks() {
        List<Task> historyList = new ArrayList<>();
        Node saveNode = head;
        while (saveNode != null) {
            historyList.add(saveNode.task);
            saveNode = saveNode.next;
        }
        return historyList;
    }

    private void removeNode(int id) {
        Node node = historyMap.remove(id);
        if (node == null) {
            System.out.println("Node is exist");
            return;
        }
        if (node == head) {
            head = node.next;
            if (head != null) {
                head.prev = null;
            } else {
                tail = null;
            }
        } else if (node == tail) {
            tail = node.prev;
            if (tail != null) {
                tail.next = null;
            } else {
                head = null;
            }
        } else {
            node.prev.next = node.next;
            node.next.prev = node.prev;
        }
        historyMap.remove(node.task.getId());
    }

    private static class Node {

        Node next;
        Task task;
        Node prev;

        public Node(Node prev, Task task, Node next) {
            this.next = next;
            this.task = task;
            this.prev = prev;
        }
    }
}