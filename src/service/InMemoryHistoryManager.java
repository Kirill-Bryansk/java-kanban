package service;

import model.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryHistoryManager implements HistoryManager {
    private final Map<Integer, Node> historyMap = new HashMap<>();
    Node head;
    Node tail;

    @Override
    public void add(Task task) {
        if (historyMap.containsKey(task.getId())) {
            Node removeNode = historyMap.get(task.getId());
            removeNode(removeNode);
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
        if (historyMap.containsKey(id)) {
            Node node = historyMap.get(id);
            removeNode(node);
        }
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

    public List getTasks() {
        List historyList = new ArrayList<>();
        Node saveNode = head;
        while (saveNode != null) {
            historyList.add(saveNode.iteam);
            saveNode = saveNode.next;
        }
        return historyList;
    }

    public void removeNode(Node node) {
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
        historyMap.remove(node.iteam.getId());
    }

    private static class Node {
        Task iteam;
        Node next;
        Node prev;

        public Node(Node prev, Task iteam, Node next) {
            this.iteam = iteam;
            this.next = next;
            this.prev = prev;
        }
    }
}