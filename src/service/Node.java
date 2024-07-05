package service;

import java.util.Objects;

public class Node<T> {
    T item;
    Node<T> next;
    Node<T> prev;

    public Node(Node<T> prev, T item, Node<T> next) {
        this.prev = prev;
        this.item = item;
        this.next = next;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Node<?> node = (Node<?>) o;
        return Objects.equals(item, node.item) && Objects.equals(next, node.next) && Objects.equals(prev, node.prev);
    }

    @Override
    public int hashCode() {
        return Objects.hash(item);
    }
}
