package hse.java.lectures.lecture3.practice.randomSet;

import java.util.Random;

public class RandomSet<T> {

    private static final int INITIAL_CAPACITY = 16;
    private static final double LOAD_FACTOR = 0.75;
    private static final Object DELETED = new Object();

    private Object[] elements;
    private int size;

    // Хеш-таблица: два параллельных массива
    private Object[] keys;
    private int[] values;
    private int tableSize;
    private int entriesCount;

    private final Random random;

    public RandomSet() {
        elements = new Object[INITIAL_CAPACITY];
        size = 0;
        tableSize = INITIAL_CAPACITY;
        keys = new Object[tableSize];
        values = new int[tableSize];
        for (int i = 0; i < tableSize; i++) {
            values[i] = -1;
        }
        entriesCount = 0;
        random = new Random();
    }

    public boolean insert(T value) {
        int index = findIndex(value);
        if (index != -1 && keys[index] != DELETED && keys[index] != null && keys[index].equals(value)) {
            return false;
        }

        ensureCapacity();

        int pos = findFreeSlot(value);
        keys[pos] = value;
        values[pos] = size;
        entriesCount++;

        elements[size] = value;
        size++;

        if (entriesCount > tableSize * LOAD_FACTOR) {
            resizeTable();
        }
        return true;
    }

    public boolean remove(T value) {
        if (value == null) return false;

        int pos = findIndex(value);
        if (pos == -1 || keys[pos] == DELETED || keys[pos] == null || !keys[pos].equals(value)) {
            return false; // элемента нет
        }

        int elementIndex = values[pos];

        int lastIndex = size - 1;
        T lastValue = (T) elements[lastIndex];

        if (elementIndex != lastIndex) {
            elements[elementIndex] = lastValue;
            int lastPos = findIndex(lastValue);
            if (lastPos != -1) {
                values[lastPos] = elementIndex;
            }
        }
        elements[lastIndex] = null;

        keys[pos] = DELETED;
        values[pos] = -1;
        entriesCount--;

        size--;
        return true;
    }

    public boolean contains(T value) {
        if (value == null) return false;
        int pos = findIndex(value);
        return pos != -1 && keys[pos] != DELETED && keys[pos] != null && keys[pos].equals(value);
    }

    public T getRandom() {
        int randomIndex = random.nextInt(size);
        return (T) elements[randomIndex];
    }

    private void ensureCapacity() {
        if (size == elements.length) {
            int newCapacity = elements.length * 2;
            Object[] newElements = new Object[newCapacity];
            System.arraycopy(elements, 0, newElements, 0, size);
            elements = newElements;
        }
    }

    private int findIndex(T key) {
        int hash = key.hashCode();
        int start = Math.abs(hash % tableSize);
        int i = start;
        do {
            if (keys[i] == null) {
                return -1;
            }
            if (keys[i] != DELETED && keys[i].equals(key)) {
                return i;
            }
            i = (i + 1) % tableSize;
        } while (i != start);
        return -1;
    }

    private int findFreeSlot(T key) {
        int hash = key.hashCode();
        int start = Math.abs(hash % tableSize);
        int i = start;
        do {
            if (keys[i] == null || keys[i] == DELETED) {
                return i;
            }
            i = (i + 1) % tableSize;
        } while (i != start);

        throw new RuntimeException("(");
    }

    private void resizeTable() {
        int newTableSize = tableSize * 2;
        Object[] newKeys = new Object[newTableSize];
        int[] newValues = new int[newTableSize];
        for (int i = 0; i < newTableSize; i++) {
            newValues[i] = -1;
        }

        for (int i = 0; i < size; i++) {
            T elem = (T) elements[i];
            int hash = elem.hashCode();
            int start = Math.abs(hash % newTableSize);
            int pos = start;
            while (newKeys[pos] != null) {
                pos = (pos + 1) % newTableSize;
            }
            newKeys[pos] = elem;
            newValues[pos] = i;
        }

        keys = newKeys;
        values = newValues;
        tableSize = newTableSize;
        entriesCount = size;
    }
}