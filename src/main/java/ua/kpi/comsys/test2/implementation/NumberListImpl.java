/*
 * Copyright (c) 2014, NTUU KPI, Computer systems department and/or its affiliates. All rights reserved.
 * NTUU KPI PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 */

package ua.kpi.comsys.test2.implementation;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import ua.kpi.comsys.test2.NumberList;

/**
 * Custom implementation of INumberList interface.
 * Has to be implemented by each student independently.
 *
 * @author Kiper Artem Oleksandrovych, IA-34, record book 8
 */
public class NumberListImpl implements NumberList {

    private static final int RECORD_BOOK_NUMBER = 8;
    private static final int[] AVAILABLE_BASES = {2, 3, 8, 10, 16};
    private static final int C5 = RECORD_BOOK_NUMBER % 5;
    private static final int C7 = RECORD_BOOK_NUMBER % 7;
    private static final int PRIMARY_BASE = AVAILABLE_BASES[C5];
    private static final int SECONDARY_BASE = AVAILABLE_BASES[(C5 + 1) % 5];

    private static final char[] DIGITS = {
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        'A', 'B', 'C', 'D', 'E', 'F'
    };

    private static class Node {
        byte value;
        Node next;
        Node prev;

        Node(byte value) {
            this.value = value;
        }
    }

    private final int base;
    private final Node head;
    private int size;

    public NumberListImpl() {
        this(PRIMARY_BASE);
    }

    public NumberListImpl(File file) {
        this(PRIMARY_BASE);
        if (file == null || !file.exists()) {
            return;
        }
        try {
            java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.FileReader(file));
            String line = reader.readLine();
            reader.close();
            if (line != null) {
                fillFromString(line.trim());
            }
        } catch (java.io.IOException ex) {
            clear();
        }
    }

    public NumberListImpl(String value) {
        this(PRIMARY_BASE);
        fillFromString(value);
    }

    private NumberListImpl(int base) {
        this.base = base;
        this.head = new Node((byte) 0);
        head.next = head;
        head.prev = head;
        this.size = 0;
    }

    public void saveList(File file) {
        if (file == null) {
            return;
        }
        try {
            java.io.BufferedWriter writer = new java.io.BufferedWriter(new java.io.FileWriter(file, false));
            writer.write(toDecimalString());
            writer.newLine();
            writer.flush();
            writer.close();
        } catch (java.io.IOException ex) {
            // ignore write errors
        }
    }

    public static int getRecordBookNumber() {
        return RECORD_BOOK_NUMBER;
    }

    public NumberListImpl changeScale() {
        java.math.BigInteger value = toBigInteger();
        NumberListImpl primaryView = new NumberListImpl(PRIMARY_BASE);
        primaryView.fillFromBigInteger(value);

        NumberListImpl secondaryView = new NumberListImpl(SECONDARY_BASE);
        secondaryView.fillFromBigInteger(value);

        copyFrom(primaryView);
        return secondaryView;
    }

    public NumberListImpl additionalOperation(NumberList arg) {
        if (!(arg instanceof NumberListImpl)) {
            return new NumberListImpl();
        }
        java.math.BigInteger first = toBigInteger();
        java.math.BigInteger second = ((NumberListImpl) arg).toBigInteger();
        java.math.BigInteger resultValue;

        switch (C7) {
            case 0:
                resultValue = first.add(second);
                break;
            case 1:
                resultValue = first.subtract(second);
                break;
            case 2:
                resultValue = first.multiply(second);
                break;
            case 3:
                resultValue = second.signum() == 0
                        ? java.math.BigInteger.ZERO
                        : first.divide(second);
                break;
            case 4:
                resultValue = second.signum() == 0
                        ? java.math.BigInteger.ZERO
                        : first.remainder(second);
                break;
            case 5:
                resultValue = first.and(second);
                break;
            case 6:
                resultValue = first.or(second);
                break;
            default:
                resultValue = java.math.BigInteger.ZERO;
        }
        if (resultValue.signum() < 0) {
            resultValue = java.math.BigInteger.ZERO;
        }
        NumberListImpl result = new NumberListImpl(base);
        result.fillFromBigInteger(resultValue);
        return result;
    }

    public String toDecimalString() {
        return toBigInteger().toString();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        Node current = head.next;
        for (int i = 0; i < size; i++) {
            builder.append(DIGITS[current.value]);
            current = current.next;
        }
        return builder.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof NumberListImpl)) {
            return false;
        }
        NumberListImpl other = (NumberListImpl) o;
        if (this.base != other.base || this.size != other.size) {
            return false;
        }
        Node current = this.head.next;
        Node otherCurrent = other.head.next;
        for (int i = 0; i < size; i++) {
            if (current.value != otherCurrent.value) {
                return false;
            }
            current = current.next;
            otherCurrent = otherCurrent.next;
        }
        return true;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public boolean contains(Object o) {
        if (!(o instanceof Byte)) {
            return false;
        }
        Node node = head.next;
        for (int i = 0; i < size; i++) {
            if (node.value == (Byte) o) {
                return true;
            }
            node = node.next;
        }
        return false;
    }

    @Override
    public Iterator<Byte> iterator() {
        return new Iterator<Byte>() {
            private Node current = head.next;
            private int passed = 0;

            @Override
            public boolean hasNext() {
                return passed < size;
            }

            @Override
            public Byte next() {
                Byte value = current.value;
                current = current.next;
                passed++;
                return value;
            }
        };
    }

    @Override
    public Object[] toArray() {
        Byte[] arr = new Byte[size];
        int i = 0;
        for (Byte b : this) {
            arr[i++] = b;
        }
        return arr;
    }

    @Override
    public <T> T[] toArray(T[] a) {
        int required = size;
        if (a.length < required) {
            @SuppressWarnings("unchecked")
            T[] copy = (T[]) java.lang.reflect.Array.newInstance(a.getClass().getComponentType(), required);
            a = copy;
        }
        int i = 0;
        for (Byte b : this) {
            @SuppressWarnings("unchecked")
            T value = (T) b;
            a[i++] = value;
        }
        if (a.length > required) {
            a[required] = null;
        }
        return a;
    }

    @Override
    public boolean add(Byte e) {
        add(size, e);
        return true;
    }

    @Override
    public boolean remove(Object o) {
        if (!(o instanceof Byte)) {
            return false;
        }
        Node node = head.next;
        for (int i = 0; i < size; i++) {
            if (node.value == (Byte) o) {
                unlink(node);
                return true;
            }
            node = node.next;
        }
        return false;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        for (Object o : c) {
            if (!contains(o)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean addAll(Collection<? extends Byte> c) {
        return addAll(size, c);
    }

    @Override
    public boolean addAll(int index, Collection<? extends Byte> c) {
        checkPositionIndex(index);
        boolean modified = false;
        int currentIndex = index;
        for (Byte b : c) {
            add(currentIndex++, b);
            modified = true;
        }
        return modified;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        boolean modified = false;
        Iterator<Byte> it = iterator();
        while (it.hasNext()) {
            Byte value = it.next();
            if (c.contains(value)) {
                remove(value);
                modified = true;
            }
        }
        return modified;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        boolean modified = false;
        Node current = head.next;
        int checked = 0;
        while (checked < size) {
            Node next = current.next;
            if (!c.contains(current.value)) {
                unlink(current);
                modified = true;
            }
            current = next;
            checked++;
        }
        return modified;
    }

    @Override
    public void clear() {
        head.next = head;
        head.prev = head;
        size = 0;
    }

    @Override
    public Byte get(int index) {
        return node(index).value;
    }

    @Override
    public Byte set(int index, Byte element) {
        checkElement(element);
        Node target = node(index);
        byte old = target.value;
        target.value = element;
        return old;
    }

    @Override
    public void add(int index, Byte element) {
        checkElement(element);
        checkPositionIndex(index);
        Node successor = (index == size) ? head : node(index);
        Node predecessor = successor.prev;
        Node newNode = new Node(element);
        newNode.next = successor;
        newNode.prev = predecessor;
        predecessor.next = newNode;
        successor.prev = newNode;
        size++;
    }

    @Override
    public Byte remove(int index) {
        Node target = node(index);
        byte value = target.value;
        unlink(target);
        return value;
    }

    @Override
    public int indexOf(Object o) {
        if (!(o instanceof Byte)) {
            return -1;
        }
        Node current = head.next;
        for (int i = 0; i < size; i++) {
            if (current.value == (Byte) o) {
                return i;
            }
            current = current.next;
        }
        return -1;
    }

    @Override
    public int lastIndexOf(Object o) {
        if (!(o instanceof Byte)) {
            return -1;
        }
        Node current = head.prev;
        for (int i = size - 1; i >= 0; i--) {
            if (current.value == (Byte) o) {
                return i;
            }
            current = current.prev;
        }
        return -1;
    }

    @Override
    public ListIterator<Byte> listIterator() {
        return listIterator(0);
    }

    @Override
    public ListIterator<Byte> listIterator(final int index) {
        checkPositionIndex(index);
        return new ListIterator<Byte>() {
            private Node current = (index == size) ? head : node(index);
            private int currentIndex = index;

            @Override
            public boolean hasNext() {
                return currentIndex < size;
            }

            @Override
            public Byte next() {
                Byte value = current.value;
                current = current.next;
                currentIndex++;
                return value;
            }

            @Override
            public boolean hasPrevious() {
                return currentIndex > 0;
            }

            @Override
            public Byte previous() {
                current = current.prev;
                currentIndex--;
                return current.value;
            }

            @Override
            public int nextIndex() {
                return currentIndex;
            }

            @Override
            public int previousIndex() {
                return currentIndex - 1;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }

            @Override
            public void set(Byte e) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void add(Byte e) {
                throw new UnsupportedOperationException();
            }
        };
    }

    @Override
    public List<Byte> subList(int fromIndex, int toIndex) {
        checkPositionIndex(fromIndex);
        checkPositionIndex(toIndex);
        if (fromIndex > toIndex) {
            throw new IndexOutOfBoundsException();
        }
        NumberListImpl result = new NumberListImpl(base);
        for (int i = fromIndex; i < toIndex; i++) {
            result.add(get(i));
        }
        return result;
    }

    @Override
    public boolean swap(int index1, int index2) {
        if (index1 == index2) {
            return true;
        }
        Node first = node(index1);
        Node second = node(index2);
        byte temp = first.value;
        first.value = second.value;
        second.value = temp;
        return true;
    }

    @Override
    public void sortAscending() {
        if (size < 2) {
            return;
        }
        for (int i = 1; i < size; i++) {
            Node current = node(i);
            byte key = current.value;
            int j = i - 1;
            Node scan = current.prev;
            while (j >= 0 && scan.value > key) {
                scan.next.value = scan.value;
                scan = scan.prev;
                j--;
            }
            scan.next.value = key;
        }
    }

    @Override
    public void sortDescending() {
        if (size < 2) {
            return;
        }
        for (int i = 1; i < size; i++) {
            Node current = node(i);
            byte key = current.value;
            int j = i - 1;
            Node scan = current.prev;
            while (j >= 0 && scan.value < key) {
                scan.next.value = scan.value;
                scan = scan.prev;
                j--;
            }
            scan.next.value = key;
        }
    }

    @Override
    public void shiftLeft() {
        if (size <= 1) {
            return;
        }
        Node first = head.next;
        unlink(first);
        insertBefore(head, first);
    }

    @Override
    public void shiftRight() {
        if (size <= 1) {
            return;
        }
        Node last = head.prev;
        unlink(last);
        insertAfter(head, last);
    }

    private void fillFromString(String value) {
        clear();
        if (value == null || value.isEmpty() || value.startsWith("-")) {
            return;
        }
        for (int i = 0; i < value.length(); i++) {
            char ch = Character.toUpperCase(value.charAt(i));
            int digit = charToDigit(ch);
            if (digit < 0 || digit >= base) {
                clear();
                return;
            }
            add((byte) digit);
        }
    }

    private void fillFromBigInteger(java.math.BigInteger value) {
        clear();
        if (value == null || value.signum() == 0) {
            return;
        }
        java.math.BigInteger baseValue = java.math.BigInteger.valueOf(base);
        java.math.BigInteger current = value;
        java.util.ArrayList<Byte> digits = new java.util.ArrayList<>();
        while (current.signum() > 0) {
            java.math.BigInteger[] parts = current.divideAndRemainder(baseValue);
            digits.add(parts[1].byteValue());
            current = parts[0];
        }
        for (int i = digits.size() - 1; i >= 0; i--) {
            add(digits.get(i));
        }
    }

    private void copyFrom(NumberListImpl source) {
        clear();
        if (source == null) {
            return;
        }
        for (Byte digit : source) {
            add(digit);
        }
    }

    private java.math.BigInteger toBigInteger() {
        java.math.BigInteger value = java.math.BigInteger.ZERO;
        java.math.BigInteger baseValue = java.math.BigInteger.valueOf(base);
        Node current = head.next;
        for (int i = 0; i < size; i++) {
            value = value.multiply(baseValue).add(java.math.BigInteger.valueOf(current.value));
            current = current.next;
        }
        return value;
    }

    private int charToDigit(char ch) {
        if (ch >= '0' && ch <= '9') {
            return ch - '0';
        }
        if (ch >= 'A' && ch <= 'F') {
            return 10 + (ch - 'A');
        }
        return -1;
    }

    private void checkElement(Byte element) {
        if (element == null || element < 0 || element >= base) {
            throw new IllegalArgumentException("Invalid digit for base " + base);
        }
    }

    private void checkPositionIndex(int index) {
        if (index < 0 || index > size) {
            throw new IndexOutOfBoundsException();
        }
    }

    private Node node(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException();
        }
        Node x;
        if (index < (size >> 1)) {
            x = head.next;
            for (int i = 0; i < index; i++) {
                x = x.next;
            }
        } else {
            x = head.prev;
            for (int i = size - 1; i > index; i--) {
                x = x.prev;
            }
        }
        return x;
    }

    private void unlink(Node node) {
        node.prev.next = node.next;
        node.next.prev = node.prev;
        size--;
    }

    private void insertBefore(Node node, Node newNode) {
        Node prev = node.prev;
        newNode.prev = prev;
        newNode.next = node;
        prev.next = newNode;
        node.prev = newNode;
        size++;
    }

    private void insertAfter(Node node, Node newNode) {
        Node next = node.next;
        newNode.next = next;
        newNode.prev = node;
        node.next = newNode;
        next.prev = newNode;
        size++;
    }
}
