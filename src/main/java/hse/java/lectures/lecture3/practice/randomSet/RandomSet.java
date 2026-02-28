package hse.java.lectures.lecture3.practice.randomSet;

import java.util.Random;

public class RandomSet<T> {

    private static class Node<T> {
        T key;
        int index;

        Node(T key, int index) {
            this.key = key;
            this.index = index;
        }

    }

    private final Node<T> DELETED = new Node<>(null, -1);
    private final Random random = new Random();
    private Node<T>[] table;
    private Object[] elements;
    private int size = 0;
    private int capacity = 4;
    private final double loadFactor = 0.5;

    public RandomSet() {
        table = new Node[capacity];
        elements = new Object[2];
    }


    private int hash(T key) {
        return Math.abs(key.hashCode() % capacity);
    }

    private void rehash() {
        Node<T>[] oldTable = table;
        int oldCapacity = capacity;

        capacity *= 2;
        table = new Node[capacity];

        for (int i = 0; i < oldCapacity; i++) {
            Node<T> node = oldTable[i];
            if (node != null && node != DELETED) {
                T key = node.key;
                int idx = Math.abs(key.hashCode() % capacity);
                while (table[idx] != null) {
                    idx = (idx + 1) % capacity;
                }
                table[idx] = new Node<>(key, node.index);
            }
        }
    }

    private int findPosition(T key) {
        int index = hash(key);
        for (int i = 0; i < capacity; i++) {
            Node<T> entry = table[index];
            if (entry == null) {
                return -1;
            }
            if (entry != DELETED && entry.key.equals(key)) {
                return index;
            }
            index = (index + 1) % capacity;
        }
        return -1;
    }

    private void ensureElementsCapacity() {
        if (size < elements.length) {
            return;
        }
        int newCapacity = elements.length * 2;
        Object[] newElements = new Object[newCapacity];
        System.arraycopy(elements, 0, newElements, 0, elements.length);
        elements = newElements;
    }

    public boolean insert(T value) {
        if (findPosition(value) != -1) {
            return false;
        }

        if (size >= capacity * loadFactor) {
            rehash();
        }

        int idx = hash(value);
        while (table[idx] != null && table[idx] != DELETED) {
            idx = (idx + 1) % capacity;
        }

        ensureElementsCapacity();
        int newElementIndex = size;
        elements[size] = value;
        size++;

        table[idx] = new Node<>(value, newElementIndex);
        return true;
    }

    public boolean remove(T value) {
        int pos = findPosition(value);
        if (pos == -1) {
            return false;
        }

        int removedIndex = table[pos].index;
        table[pos] = DELETED;

        if (removedIndex != size - 1) {
            T lastValue = (T) elements[size - 1];
            elements[removedIndex] = lastValue;
            int lastPos = findPosition(lastValue);
            if (lastPos != -1) {
                table[lastPos].index = removedIndex;
            }
        }

        size--;
        elements[size] = null;

        return true;
    }

    public boolean contains(T value) {
        return findPosition(value) != -1;
    }

    public T getRandom() {
        if (size == 0) {
            throw new EmptySetException("RandomSet is empty");
        }
        int randomIndex = random.nextInt(size);
        return (T) elements[randomIndex];
    }
}