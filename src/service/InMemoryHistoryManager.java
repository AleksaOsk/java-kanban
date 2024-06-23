package service;

import entities.Task;

import java.util.*;

public class InMemoryHistoryManager implements HistoryManager {

    private HashMap<Integer, Node<Task>> viewHistory = new HashMap<>();
    private Node<Task> head;
    private Node<Task> tail;

    public void linkLast(Task task) {
        final Node<Task> exTail = tail;
        final Node<Task> newNode = new Node<>(exTail, task, null);
        tail = newNode;

        if (exTail == null) {
            head = newNode;
        } else {
            exTail.next = newNode;
        }
    }

    private List<Task> getTasks() {
        List<Task> task = new ArrayList<>();
        Node<Task> node = head;
        while (node != null) {
            task.add(node.item);
            node = node.next;
        }
        return task;
    }

    @Override
    public void add(Task task) {
        if (task != null) {
            remove(task.getId());
            linkLast(task);
        }
    }

    @Override
    public List<Task> getHistory() {
        return getTasks();
    }

    public void remove(int id) {
        removeNode(viewHistory.get(id));
    }

    private void removeNode(Node<Task> node) {
        if (node != null) {
            final Node<Task> next = node.next;
            final Node<Task> prev = node.prev;
            node.item = null;
            if (head == node && tail == node) {
                head = null;
                tail = null;
            } else if (head == node && tail != node) {
                head = head.next;
                head.prev = null;
            } else if (head != node && tail == node) {
                tail = tail.prev;
                tail.next = null;
            } else {
                prev.next = next;
                next.prev = prev;
            }
        }
    }
}